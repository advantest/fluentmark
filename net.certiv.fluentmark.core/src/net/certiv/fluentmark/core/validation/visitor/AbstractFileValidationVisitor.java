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
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.validation.FileValidator;

abstract class AbstractFileValidationVisitor implements IResourceVisitor {
	
	protected final FileValidator validator;
	protected final IProgressMonitor progressMonitor;
	
	public AbstractFileValidationVisitor(FileValidator validator, IProgressMonitor monitor) {
		if (validator == null) {
			throw new IllegalArgumentException();
		}
		
		this.validator = validator;
		
		if (monitor != null) {
			this.progressMonitor = monitor;
		} else {
			this.progressMonitor = new NullProgressMonitor();
		}
	}

	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (progressMonitor.isCanceled()) {
			return false;
		}
		
		if (resource instanceof IWorkspaceRoot) {
			return true;
		} else if (resource instanceof IProject) {
			// TODO apply filters?
			return resource.isAccessible();
		} else if (resource instanceof IFolder) {
			// TODO apply filters?
			// check Markdown only in doc folders?
			// ignore build target / generated folders?
			return resource.isAccessible();
		} else if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			handleFile(file);
			progressMonitor.worked(1);
		}
		
		FluentCore.log(IStatus.WARNING, "Found unexpected resource type: " + resource.getClass().getName());
		
		return false;
	}
	
	abstract protected void handleFile(IFile file);
}
