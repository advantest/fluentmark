/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

public interface ITypedRegionValidator {
	
	void setupDocumentPartitioner(IDocument document, IFile file);
	
	ITypedRegion[] computePartitioning(IDocument document) throws BadLocationException;
	
	boolean isValidatorFor(IFile file);
	
	boolean isValidatorFor(ITypedRegion region, IFile file);
	
	void validateRegion(ITypedRegion region, IDocument document, IFile resource) throws CoreException;
	
}
