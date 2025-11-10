/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.refactoring;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import net.certiv.fluentmark.ui.FluentUI;

public class UndoCreateTextFileChange extends Change {
	
	private final IFile createdFile;
	
	public UndoCreateTextFileChange(IFile createdFile) {
		Assert.isNotNull(createdFile);
		this.createdFile = createdFile;
	}

	@Override
	public String getName() {
		return "Undo creating file " + createdFile.getProjectRelativePath();
	}

	@Override
	public void initializeValidationData(IProgressMonitor pm) {
	}
	
	@Override
	public IFile getModifiedElement() {
		return this.createdFile;
	}

	@Override
	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (!this.createdFile.exists() || !this.createdFile.isAccessible()) {
			return RefactoringStatus.createFatalErrorStatus("Cannot undo file creation. File does not exist or is not accessible.");
		}
		return new RefactoringStatus();
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		if (!this.createdFile.exists() || !this.createdFile.isAccessible()) {
			throw new CoreException(new Status(IStatus.ERROR, FluentUI.PLUGIN_ID, "Cannot undo file creation. File is missing or is not accessible: " + this.createdFile.getFullPath()));
		}
		
		String fileContents = this.createdFile.readString();
		this.createdFile.delete(IResource.KEEP_HISTORY, SubMonitor.convert(pm));
		CreateTextFileChange redoChange = new CreateTextFileChange(createdFile, fileContents);
		return redoChange;
	}

}
