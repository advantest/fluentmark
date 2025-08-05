/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.markers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import net.certiv.fluentmark.core.validation.IValidationResultConsumer;
import net.certiv.fluentmark.core.validation.IssueTypes;
import net.certiv.fluentmark.ui.FluentUI;

public class MarkerCreator implements IValidationResultConsumer {

	@Override
	public void reportValidationResult(IFile file, String issueTypeId, int issueSeverity, String message,
			Integer issueLineNumber, Integer issueStartOffset, Integer issueEndOffset) {
		
		switch (issueTypeId) {
			case IssueTypes.MARKDOWN_ISSUE ->
				createDocumentationProblemMarker(file, issueSeverity, message,
					issueLineNumber, issueStartOffset, issueEndOffset);

			case IssueTypes.PLANTUML_ISSUE ->
				createDocumentationProblemMarker(file, issueSeverity, message,
					issueLineNumber, issueStartOffset, issueEndOffset);
			
			case TaskTypes.MARKDOWN_TASK ->
				createMarkdownTaskMarker(file, issueSeverity, message,
						issueLineNumber, issueStartOffset, issueEndOffset);
			
			case TaskTypes.PLANTUML_TASK ->
				createPlantUmlTaskMarker(file, issueSeverity, message,
					issueLineNumber, issueStartOffset, issueEndOffset);
			
			default ->
				throw new IllegalArgumentException("Unexpected issue type ID: " + issueTypeId);
		}
	}
	
	private IMarker createDocumentationProblemMarker(IResource resource, int markerSeverity, String markerMessage,
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
	
	private IMarker createMarkdownTaskMarker(IResource resource, int markerPriority, String markerMessage,
			Integer lineNumber, Integer startOffset, Integer endOffset) {
		return createTaskMarker(resource, MarkerConstants.MARKER_ID_TASK_MARKDOWN, markerPriority, markerMessage, lineNumber, startOffset, endOffset);
	}
	
	private IMarker createPlantUmlTaskMarker(IResource resource, int markerPriority, String markerMessage,
			Integer lineNumber, Integer startOffset, Integer endOffset) {
		return createTaskMarker(resource, MarkerConstants.MARKER_ID_TASK_PLANTUML, markerPriority, markerMessage, lineNumber, startOffset, endOffset);
	}
	
	private IMarker createTaskMarker(IResource resource, String markerType, int markerPriority, String markerMessage,
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
	
	private static void deleteAllMarkersOfType(IResource resource, String markerTypeId) throws CoreException {
		IMarker[] markers = resource.findMarkers(markerTypeId, true, IResource.DEPTH_INFINITE);
		
		for (IMarker marker: markers) {
			if (marker.exists()) {
				marker.delete();
			}
		}
	}
	
	public static void deleteAllDocumentationProblemMarkers(IResource resource) throws CoreException {
		deleteAllMarkersOfType(resource, MarkerConstants.MARKER_ID_DOCUMENTATION_PROBLEM);
	}
	
	public static void deleteAllMarkdownTaskMarkers(IResource resource) throws CoreException {
		deleteAllMarkersOfType(resource, MarkerConstants.MARKER_ID_TASK_MARKDOWN);
	}
	
	public static void deleteAllPlantUmlTaskMarkers(IResource resource) throws CoreException {
		deleteAllMarkersOfType(resource, MarkerConstants.MARKER_ID_TASK_PLANTUML);
	}
}
