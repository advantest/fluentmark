/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.validation.visitor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import net.certiv.fluentmark.core.validation.FileValidator;

public class AdaptableFilesCountingVisitor extends AbstractFileValidationVisitor {
	
	private int numFiles = 0;

	public AdaptableFilesCountingVisitor(FileValidator validator) {
		this(validator, new NullProgressMonitor());
	}
	
	public AdaptableFilesCountingVisitor(FileValidator validator, IProgressMonitor monitor) {
		super(validator, monitor);
	}
	
	public int getNumFiles() {
		return numFiles;
	}

	@Override
	protected void handleFile(IFile file) {
		if (validator.hasApplicablePartitionValidatorsFor(file)) {
			numFiles++;
		}
	}
}
