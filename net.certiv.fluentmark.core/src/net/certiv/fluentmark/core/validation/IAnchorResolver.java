/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.validation;

import org.eclipse.core.resources.IFile;

public interface IAnchorResolver {

	String[] getSupportedFileExtensions();
	
	default boolean doesAnchorTargetExist(IFile fileToCheck, String anchor) {
		Object anchorTarget = resolveAnchor(fileToCheck, anchor);
		return (anchorTarget != null);
	}
	
	Object resolveAnchor(IFile fileWithAnchorTarget, String anchor);
	
	default boolean isResponsibleFor(IFile file) {
		if (file == null) {
			throw new IllegalArgumentException();
		}
		
		for (String fileExtension : this.getSupportedFileExtensions()) {
			if (file.getFileExtension() != null
					&& file.getFileExtension().equalsIgnoreCase(fileExtension)) {
				return true;
			}
		}
		return false;
	}

}
