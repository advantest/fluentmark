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

import java.util.List;

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
import net.certiv.fluentmark.core.extensionpoints.ResourcesValidationFiltersManager;
import net.certiv.fluentmark.core.validation.FileValidator;

abstract class AbstractFileValidationVisitor implements IResourceVisitor {
	
	protected final String PROJECT_NATURE_ID = "org.eclipse.jdt.core.javanature";
	
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
		
		List<IResourcesValidationFilter> filters = ResourcesValidationFiltersManager.getInstance().getResourceValidationFilters();
		
		if (resource instanceof IWorkspaceRoot) {
			return true;
		} else if (resource instanceof IProject) {
			IProject project = (IProject) resource;
			boolean ignore = filters.stream()
					.anyMatch(filter -> filter.ignore(project));
			if (ignore) {
				return false;
			}
			
			return resource.isAccessible();
		} else if (resource instanceof IFolder) {
			// TODO ignore build output folders?
			// e.g. for JDT: check build.properties, find entry like "output.. = bin/" and ignore bin folder
			// also for JDT: check .classpath file, find entry like "<classpathentry kind="output" path="target/classes"/>", and ignore target/classes folder
			// Maybe do similar checks for C/C++ projects
			IFolder folder = (IFolder) resource;
			boolean ignore = filters.stream()
					.anyMatch(filter -> filter.ignore(folder));
			if (ignore) {
				return false;
			}
			
			return resource.isAccessible();
		} else if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			boolean ignore = filters.stream()
					.anyMatch(filter -> filter.ignore(file));
			if (ignore) {
				return false;
			}
			
			if (validator.hasApplicablePartitionValidatorsFor(file)) {
				handleFile(file);
			}
			progressMonitor.worked(1);
		}
		
		FluentCore.log(IStatus.WARNING, "Found unexpected resource type: " + resource.getClass().getName());
		
		return false;
	}
	
	abstract protected void handleFile(IFile file);
}
