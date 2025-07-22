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
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

public interface ITypedRegionValidator extends IValidationResultReporter {
	
	String getRequiredPartitioning(IFile file);
	
	boolean isValidatorFor(IFile file);
	
	boolean isValidatorFor(ITypedRegion region, IFile file);
	
	void validateRegion(ITypedRegion region, IDocument document, IFile file);

}


// TODO Add a validation calculator similar to MarkerCalculator, but without having to create IMarker
// TODO Add interface and two implementations for (a) printing validation failures to console and (b) create markers 
// TODO Add extension point for validators
// TODO Add extension point for partitioners