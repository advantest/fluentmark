/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.markers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITypedRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.Charset;

import net.certiv.fluentmark.core.convert.Partitions;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.editor.FluentDocumentSetupParticipant;

public class MarkerCalculator {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private static final String JOB_NAME = "Re-calculating problem markers";
	
	private static final MarkerCalculator INSTANCE = new MarkerCalculator();
	
	private List<ITypedRegionValidator> validators;
	
	private Job markerCalculatingJob;
	
	private final LinkedList<IFile> filesQueue = new LinkedList<>();
	private final Map<IResource, IDocument> filesDocumentsMap = new HashMap<>();
	
	private MarkerCalculator() {
		this.validators = new ArrayList<>();
		this.validators.add(new LinkValidator());
	}
	
	public static MarkerCalculator get() {
		return INSTANCE;
	}
	
	public static IMarker createMarkdownMarker(IResource resource, int markerSeverity, String markerMessage,
			Integer lineNumber, Integer startOffset, Integer endOffset)
			throws CoreException {
		IMarker marker;
		try {
			marker  = resource.createMarker(MarkerConstants.MARKER_ID_MARKDOWN_PROBLEM);
			marker.setAttribute(IMarker.MESSAGE, markerMessage);
			marker.setAttribute(IMarker.SEVERITY, markerSeverity);
			marker.setAttribute(IMarker.LOCATION, String.format("line %s", lineNumber != null && lineNumber.intValue() > 0 ? lineNumber.intValue() : "unknown"));
			if (startOffset != null && endOffset != null) {
				marker.setAttribute(IMarker.CHAR_START, startOffset.intValue());
			    marker.setAttribute(IMarker.CHAR_END, endOffset.intValue());
			}
			if (lineNumber != null && lineNumber.intValue() > 0) {
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			}
			
	        return marker;
        } catch (CoreException e) {
        	FluentUI.log(IStatus.WARNING, "Problem marker couldn't be created.", e);
        }
		return null;
	}
	
	public void scheduleMarkerCalculation(IDocument document, IFile markdownFile) {
		if (markdownFile == null || !markdownFile.isAccessible()) {
			return;
		}
		
		if (!"md".equalsIgnoreCase(markdownFile.getFileExtension())) {
			throw new IllegalArgumentException(
					String.format("Expected a Markdown file, but got %s.", markdownFile.getName()));
		}
		
		synchronized(filesQueue) {
			filesQueue.addLast(markdownFile);
		}
		
		synchronized(filesDocumentsMap) {
			if (document != null) {
				filesDocumentsMap.put(markdownFile, document);
			}
		}
		
		scheduleMarkerCalculation();
	}
	
	public void scheduleMarkerCalculation(IFile markdownFile) {
		scheduleMarkerCalculation(null, markdownFile);
	}
	
	private void scheduleMarkerCalculation() {
		if (markerCalculatingJob == null) {
			markerCalculatingJob = Job.create(JOB_NAME, new ICoreRunnable() {

				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					monitor.beginTask(JOB_NAME, filesQueue.size());
					
					while (!monitor.isCanceled()
							&& !filesQueue.isEmpty()) {
						
						IFile file;
						synchronized (filesQueue) {
							file = filesQueue.pollFirst();
						}
						monitor.subTask(file.getLocation().toString());
						
						IDocument document;
						synchronized (filesDocumentsMap) {
							document = filesDocumentsMap.get(file);
							
							if (document != null) {
								filesDocumentsMap.remove(file);
							}
						}
						
						if (document == null) {
							String markdownFileContents = readFileContents(file);
							document = new Document(markdownFileContents);
							connectPartitioningToElement(document);
						}
						
						calculateMarkers(monitor, document, file);
						monitor.worked(1);
					}
				}
				
			});
			markerCalculatingJob.setUser(false);
			markerCalculatingJob.setPriority(Job.DECORATE);
		}

		if (markerCalculatingJob.getState() != Job.RUNNING) {
			// set a delay before reacting to user action to handle continuous typing
			markerCalculatingJob.schedule(1000);
		}
	}
	
	private String readFileContents(IFile markdownFile) {
		try (InputStream fileInputStream = markdownFile.getContents()) {
			return new String(fileInputStream.readAllBytes(), UTF8);
		} catch (IOException | CoreException e) {
			FluentUI.log(IStatus.ERROR,
					String.format("Couldn't read file %s", markdownFile.getFullPath().toString()), e);
		}
		
		return null;
	}
	
	private void connectPartitioningToElement(IDocument document) {
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension = (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(Partitions.PARTITIONING) == null) {
				FluentDocumentSetupParticipant participant = new FluentDocumentSetupParticipant(FluentUI.getDefault().getTextTools());
				participant.setup(document);
			}
		}
	}
	
	
	private void calculateMarkers(IProgressMonitor monitor, IDocument document, IResource resource) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}		
		
		monitor.subTask("Delete obsolete markers");
		IMarker[] markers = resource.findMarkers(MarkerConstants.MARKER_ID_MARKDOWN_PROBLEM, true, IResource.DEPTH_INFINITE);
		
		for (IMarker marker: markers) {
			if (marker.exists()) {
				marker.delete();
			}
		}
		
		if (monitor.isCanceled()) {
			return;
		}
		
		monitor.subTask("Calculate document partitions");
		ITypedRegion[] typedRegions = Partitions.computePartitions(document);
		if (typedRegions == null || typedRegions.length == 0) {
			FluentUI.log(IStatus.WARNING, String.format("Could not calculate partitions for file %s.", resource.getLocation().toString()));
			
			return;
		}
		
		if (monitor.isCanceled()) {
			return;
		}
		
		monitor.subTask("Calculate new markers");
		for (ITypedRegion region: typedRegions) {
			for (ITypedRegionValidator validator: validators) {
				if (monitor.isCanceled()) {
					return;
				}
				
				if (validator.isValidatorFor(region, document) ) {
					validator.validateRegion(region, document, resource);
				}
			}
		}
	}

}
