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

import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.util.FileUtils;

public class JavaLinkValidator implements ITypedRegionValidator {

	@Override
	public boolean isValidatorFor(ITypedRegion region, IDocument document, String fileExtension) {
		if (!FileUtils.FILE_EXTENSION_JAVA.equalsIgnoreCase(fileExtension)) {
			return false;
		}
		
		return IJavaPartitions.JAVA_DOC.equals(region.getType());
				// || IDocument.DEFAULT_CONTENT_TYPE.equals(region.getType());
	}

	@Override
	public void validateRegion(ITypedRegion region, IDocument document, IResource resource) throws CoreException {
		String regionContent;
		try {
			regionContent = document.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			return;
		}

		// TODO implement the link search and check here
	}

}
