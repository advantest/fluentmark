/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.util;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;

public class DocumentUtils {
	
	public static IFile findFileFor(IDocument document) {
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		if (bufferManager != null) {
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(document);
			if (textFileBuffer != null) {
				IPath fileLocation = textFileBuffer.getLocation();
				if (fileLocation != null) {
					return fileLocation.segmentCount() == 1 ?
							ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(fileLocation)
							: ResourcesPlugin.getWorkspace().getRoot().getFile(fileLocation);
				}
			}
		}
		return null;
	}

}
