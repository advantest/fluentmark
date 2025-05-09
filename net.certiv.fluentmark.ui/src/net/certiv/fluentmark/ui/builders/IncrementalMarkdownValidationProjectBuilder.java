/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.builders;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.util.MarkdownFileCountingVisitor;


public class IncrementalMarkdownValidationProjectBuilder extends IncrementalProjectBuilder {
	
	public static final String BUILDER_ID = FluentUI.PLUGIN_ID + ".builders.markdown";
	
	private MarkdownFileValidationVisitor markdownFileVisitor = new MarkdownFileValidationVisitor();
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		
		MarkdownFileCountingVisitor mdFileCounter = new MarkdownFileCountingVisitor(); 
		project.accept(mdFileCounter);
		int numMdFiles = mdFileCounter.getNumMdFilesFound();
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Analyzing Markdown files", numMdFiles);

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
					// TODO Do a full build as long as incremental build is not implemented
					fullBuild(subMonitor);
					//incrementalBuild(resourceDelta, subMonitor);
				}
				break;
		}

		return null;
	}
	
	private void fullBuild(SubMonitor monitor) throws CoreException {
		markdownFileVisitor.setMonitor(monitor);
		getProject().accept(markdownFileVisitor);
	}
	
	private void incrementalBuild(IResourceDelta resourceDelta, SubMonitor monitor) throws CoreException {
		markdownFileVisitor.setMonitor(monitor);
		resourceDelta.accept(markdownFileVisitor);
	}
}
