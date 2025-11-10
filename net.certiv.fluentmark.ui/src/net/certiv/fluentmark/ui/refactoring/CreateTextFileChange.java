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

import java.nio.charset.StandardCharsets;

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
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;

import net.certiv.fluentmark.ui.FluentUI;

public class CreateTextFileChange extends ResourceChange {
	
	private final IFile file;
	private final String fileContents;
	
	public CreateTextFileChange(IFile fileToCreate, String fileContents) {
		Assert.isNotNull(fileToCreate);
		Assert.isNotNull(fileContents);
		
		this.file = fileToCreate;
		this.fileContents = fileContents;
	}
	
	@Override
	public IFile getModifiedResource() {
		return this.file;
	}
	
	@Override
	public String getName() {
		return "Create file " + this.file.getProjectRelativePath().toOSString();
	}
	
	@Override
	public RefactoringStatus isValid(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		if (getModifiedResource() == null) {
			return RefactoringStatus.createFatalErrorStatus("No input element provided");
		}
		return new RefactoringStatus();
	}
	
	@Override
	public Change perform(IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor= SubMonitor.convert(monitor, getName(), 10);
		
		if (file.exists()) {
			throw new CoreException(new Status(IStatus.ERROR, FluentUI.PLUGIN_ID, "The file to create already exists: " + this.file.getFullPath()));
		}
		
		file.create(this.fileContents.getBytes(), IResource.KEEP_HISTORY, subMonitor.newChild(8));
		file.setCharset(StandardCharsets.UTF_8.name(), subMonitor.newChild(2));
		UndoCreateTextFileChange undoChange = new UndoCreateTextFileChange(file);
		return undoChange;
	}
	
	public String getPreview() {
		return this.fileContents;
	}
}
