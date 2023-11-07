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

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IJavaPartitionerManager;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.Charset;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.editor.FluentDocumentSetupParticipant;

public class MarkerCalculator {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private static final String JOB_NAME = "Re-calculating problem markers";
	
	private static final MarkerCalculator INSTANCE = new MarkerCalculator();
	
	private List<ITypedRegionValidator> validators;
	
	private Job markerCalculatingJob;
	
	private final LinkedList<IFile> filesQueue = new LinkedList<>();
	private final Map<IResource, IDocument> filesDocumentsMap = new HashMap<>();
	
	private JavaTextTools javaTextTools;
	
	private MarkerCalculator() {
		this.validators = new ArrayList<>();
		this.validators.add(new MarkdownLinkValidator());
		this.validators.add(new JavaLinkValidator());
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
	
	public void scheduleMarkerCalculation(IDocument document, IFile file) {
		if (file == null || !file.isAccessible()) {
			return;
		}
		
		if (!isMarkdownFile(file) && !isJavaFile(file)) {
			Log.log(IStatus.WARNING, 0, "Got unexpected file format during marker calculation: " + file.getFileExtension(), null);
		}
		
		synchronized(filesQueue) {
			if (!filesQueue.contains(file)) {
				filesQueue.addLast(file);
			}
		}
		
		synchronized(filesDocumentsMap) {
			if (document != null) {
				filesDocumentsMap.put(file, document);
			}
		}
		
		scheduleMarkerCalculation();
	}
	
	private boolean isMarkdownFile(IFile file) {
		return file != null && "md".equalsIgnoreCase(file.getFileExtension());
	}
	
	private boolean isJavaFile(IFile file) {
		return file != null && "java".equalsIgnoreCase(file.getFileExtension());
	}
	
	public void scheduleMarkerCalculation(IFile file) {
		scheduleMarkerCalculation(null, file);
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
							String fileContents = readFileContents(file);
							document = new Document(fileContents);
							
							
							if (isMarkdownFile(file)) {
								connectPartitioningToElement(document, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
							} else if (isJavaFile(file)) {
								connectPartitioningToElement(document, IJavaPartitions.JAVA_PARTITIONING);
							}
						}
						
						try {
							calculateMarkers(monitor, document, file);
						}
						catch (Exception e) {
							FluentUI.log(IStatus.WARNING, "Problem marker calculation failed.", e);
						}
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
	
	private JavaTextTools getJavaTextTools() {
		if (this.javaTextTools == null) {
			this.javaTextTools = new JavaTextTools(PreferenceConstants.getPreferenceStore()); 
		}
		return this.javaTextTools;
	}
	
	private void connectPartitioningToElement(IDocument document, String partitioningId) {
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension = (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(partitioningId) == null) {
				if (MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING.equals(partitioningId)) {
					FluentDocumentSetupParticipant participant = new FluentDocumentSetupParticipant(FluentUI.getDefault().getTextTools());
					participant.setup(document);
				} else {
					JavaTextTools textTools= getJavaTextTools();
					IJavaPartitionerManager pManager= textTools.getJavaPartitionerManager();
					if (pManager instanceof IJavaPartitionerManager) {
						IJavaPartitionerManager jpManager= (IJavaPartitionerManager) pManager;
						IDocumentPartitioner javaPartitioner = jpManager.createDocumentPartitioner();
						
						if (javaPartitioner != null) {
							javaPartitioner.connect(document);
							if (document instanceof IDocumentExtension3) {
								IDocumentExtension3 extension3 = (IDocumentExtension3) document;
								extension3.setDocumentPartitioner(partitioningId, javaPartitioner);
							} else {
								document.setDocumentPartitioner(javaPartitioner);
							}
						}
					}
				} 
			}
		}
	}
	
	private void calculateMarkers(IProgressMonitor monitor, IDocument document, IFile file) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}		
		
		// TODO only delete markers that are going to be re-calculated, e.g. Markdown or Java problems
		monitor.subTask("Delete obsolete markers");
		IMarker[] markers = file.findMarkers(MarkerConstants.MARKER_ID_MARKDOWN_PROBLEM, true, IResource.DEPTH_INFINITE);
		
		for (IMarker marker: markers) {
			if (marker.exists()) {
				marker.delete();
			}
		}
		
		if (monitor.isCanceled()) {
			return;
		}
		
		monitor.subTask("Calculate document partitions");
		ITypedRegion[] typedRegions = null;
		
		if (isMarkdownFile((IFile) file)) {
			typedRegions = MarkdownPartitions.computePartitions(document);
		} else if (isJavaFile((IFile) file)) {
			try {
				typedRegions = TextUtilities.computePartitioning(document, IJavaPartitions.JAVA_PARTITIONING, 0, document.getLength(), false);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
		if (typedRegions == null || typedRegions.length == 0) {
			FluentUI.log(IStatus.WARNING, String.format("Could not calculate partitions for file %s.", file.getLocation().toString()));
			
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
				
				if (validator.isValidatorFor(region, document, file.getFileExtension()) ) {
					validator.validateRegion(region, document, file);
				}
			}
		}
	}

}
