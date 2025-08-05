/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.builders;

import java.util.Map;

import org.eclipse.core.resources.IIncrementalProjectBuilder2;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.extensionpoints.DocumentPartitionersManager;
import net.certiv.fluentmark.core.extensionpoints.TypedRegionValidatorsManager;
import net.certiv.fluentmark.core.validation.FileValidator;
import net.certiv.fluentmark.core.validation.IValidationResultConsumer;
import net.certiv.fluentmark.core.validation.visitor.AdaptableFilesCountingVisitor;
import net.certiv.fluentmark.core.validation.visitor.AdaptableFilesValidatingVisitor;

public class FileValidationBuilder extends IncrementalProjectBuilder implements IIncrementalProjectBuilder2 {
	
	public static final String BUILDER_ID = FluentCore.PLUGIN_ID + ".builder";
	
	private final FileValidator fileValidator;
	
	public FileValidationBuilder(IValidationResultConsumer validationResultConsumer) {
		if (validationResultConsumer == null) {
			throw new IllegalArgumentException();
		}
		
		fileValidator = new FileValidator(
				DocumentPartitionersManager.getInstance().getDocumentPartitioners(),
				TypedRegionValidatorsManager.getInstance().getTypedRegionValidators(),
				validationResultConsumer);
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		
		AdaptableFilesCountingVisitor filesCountingVisitor = new AdaptableFilesCountingVisitor(fileValidator);
		project.accept(filesCountingVisitor);
		int numFiles = filesCountingVisitor.getNumFiles();
		
		if (numFiles == 0) {
			return null;
		}
		
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Analyzing Markdown, PlantUML and other files", numFiles);
		
		switch (kind) {
			case FULL_BUILD:
				fullBuild(subMonitor);
				break;
	
			case INCREMENTAL_BUILD:
			case AUTO_BUILD:
				IResourceDelta resourceDelta = getDelta(project);
				if (resourceDelta == null) {
					fullBuild(subMonitor);
				} else {
					// TODO Implement incremental build?
					// We do a full build as long as incremental build is not implemented
					fullBuild(subMonitor);
				}
				break;
		}
		
		return null;
	}
	
	private void fullBuild(SubMonitor monitor) throws CoreException {
		AdaptableFilesValidatingVisitor filesValidatingVisitor = new AdaptableFilesValidatingVisitor(fileValidator, monitor);
		getProject().accept(filesValidatingVisitor);
	}

	@Override
	public void clean(Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		// Do nothing yet. Subclasses may override this method
	}

}
