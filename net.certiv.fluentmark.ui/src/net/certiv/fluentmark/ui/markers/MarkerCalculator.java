/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.markers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.extensionpoints.DocumentPartitionersManager;
import net.certiv.fluentmark.core.extensionpoints.TypedRegionValidatorsManager;
import net.certiv.fluentmark.core.validation.FileValidator;
import net.certiv.fluentmark.core.validation.IValidationResultConsumer;
import net.certiv.fluentmark.ui.FluentUI;

public class MarkerCalculator {

	private static final String JOB_NAME = "Re-calculating markers";
	
	private static MarkerCalculator INSTANCE = null;
	
	public static MarkerCalculator get() {
		if (INSTANCE == null) {
			INSTANCE = new MarkerCalculator();
		}
		return INSTANCE;
	}
	
	private final FileValidator fileValidator;
	
	private Job markerCalculatingJob;
	
	private final LinkedList<IFile> filesQueue = new LinkedList<>();
	private final Map<IResource, IDocument> filesDocumentsMap = new HashMap<>();
	
	private MarkerCalculator() {
		fileValidator = new FileValidator(
				DocumentPartitionersManager.getInstance().getDocumentPartitionerss(),
				TypedRegionValidatorsManager.getInstance().getTypedRegionValidators(),
				new MarkerCreator());
	}
	
	private class MarkerCreator implements IValidationResultConsumer {

		@Override
		public void reportValidationResult(IFile file, String issueTypeId, int issueSeverity, String message,
				Integer issueLineNumber, Integer issueStartOffset, Integer issueEndOffset) {
			
			switch (issueTypeId) {
				case MarkerConstants.MARKER_ID_DOCUMENTATION_PROBLEM ->
					createDocumentationProblemMarker(file, issueSeverity, message,
							issueLineNumber, issueStartOffset, issueEndOffset);
				
				case MarkerConstants.MARKER_ID_TASK_MARKDOWN ->
					createMarkdownTaskMarker(file, issueSeverity, message,
							issueLineNumber, issueStartOffset, issueEndOffset);
				
				case MarkerConstants.MARKER_ID_TASK_PLANTUML ->
					createPlantUmlTaskMarker(file, issueSeverity, message,
						issueLineNumber, issueStartOffset, issueEndOffset);
				
				default ->
					throw new IllegalArgumentException("Unexpected issue type ID: " + issueTypeId);
			}
		}
		
	}
	
	public static IMarker createDocumentationProblemMarker(IResource resource, int markerSeverity, String markerMessage,
			Integer lineNumber, Integer startOffset, Integer endOffset) {
		IMarker marker;
		try {
			marker  = resource.createMarker(MarkerConstants.MARKER_ID_DOCUMENTATION_PROBLEM);
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
	
	public static IMarker createMarkdownTaskMarker(IResource resource, int markerPriority, String markerMessage,
			Integer lineNumber, Integer startOffset, Integer endOffset) {
		return createTaskMarker(resource, MarkerConstants.MARKER_ID_TASK_MARKDOWN, markerPriority, markerMessage, lineNumber, startOffset, endOffset);
	}
	
	public static IMarker createPlantUmlTaskMarker(IResource resource, int markerPriority, String markerMessage,
			Integer lineNumber, Integer startOffset, Integer endOffset) {
		return createTaskMarker(resource, MarkerConstants.MARKER_ID_TASK_PLANTUML, markerPriority, markerMessage, lineNumber, startOffset, endOffset);
	}
	
	private static IMarker createTaskMarker(IResource resource, String markerType, int markerPriority, String markerMessage,
			Integer lineNumber, Integer startOffset, Integer endOffset) {
		IMarker marker;
		try {
			marker  = resource.createMarker(markerType);
			marker.setAttribute(IMarker.MESSAGE, markerMessage);
			marker.setAttribute(IMarker.PRIORITY, markerPriority);
			marker.setAttribute(IMarker.LOCATION, String.format("line %s", lineNumber != null && lineNumber.intValue() > 0 ? lineNumber.intValue() : "unknown"));
			marker.setAttribute(IMarker.USER_EDITABLE, false);
			if (startOffset != null && endOffset != null) {
				marker.setAttribute(IMarker.CHAR_START, startOffset.intValue());
				marker.setAttribute(IMarker.CHAR_END, endOffset.intValue());
			}
			if (lineNumber != null && lineNumber.intValue() > 0) {
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			}
			
			return marker;
		} catch (CoreException e) {
			FluentUI.log(IStatus.WARNING, "Task marker couldn't be created.", e);
		}
		return null;
	}
	
	public void scheduleMarkerCalculation(IDocument document, IFile file) {
		if (file == null || !file.isAccessible()) {
			return;
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
	
	public void deleteAllMarkersOfType(IResource resource, String markerTypeId) throws CoreException {
		IMarker[] markers = resource.findMarkers(markerTypeId, true, IResource.DEPTH_INFINITE);
		
		for (IMarker marker: markers) {
			if (marker.exists()) {
				marker.delete();
			}
		}
	} 
	
	private void calculateMarkers(IProgressMonitor monitor, IDocument document, IFile file) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}		
		
		// the file could have been deleted / moved after scheduling the marker calculation
		if (!file.exists()) {
			return;
		}
		
		monitor.subTask("Delete obsolete markers");
		deleteAllMarkersOfType(file, MarkerConstants.MARKER_ID_DOCUMENTATION_PROBLEM);
		deleteAllMarkersOfType(file, MarkerConstants.MARKER_ID_TASK_MARKDOWN);
		deleteAllMarkersOfType(file, MarkerConstants.MARKER_ID_TASK_PLANTUML);
		
		if (monitor.isCanceled()) {
			return;
		}
		
		monitor.subTask("Calculate new markers");
		fileValidator.performResourceValidation(document, file, monitor);
	}

}
