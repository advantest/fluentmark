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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;

public class MarkerCreator {
	
	public static IMarker createMarkdownMarker(IResource resource, int markerSeverity, String markerMessage,
			Integer lineNumber, Integer startOffset, Integer endOffset)
			throws CoreException {
		IMarker marker = resource.createMarker(MarkerConstants.MARKER_ID_MARKDOWN_PROBLEM);
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
	}

}
