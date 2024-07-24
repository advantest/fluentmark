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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import net.certiv.fluentmark.core.util.FileUtils;

public class IncrementalPlantUmlValidationProjectBuilder extends IncrementalProjectBuilder {
	
	public static final String BUILDER_ID = "net.certiv.fluentmark.ui.builders.plantuml";
	
	private PlantUMLValidationVisitor plantUmlResourceVisitor = new PlantUMLValidationVisitor();

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		
		PlantUMLCodeBlocksCountingVisitor plantUmlCodeBlocksCounter = new PlantUMLCodeBlocksCountingVisitor(); 
		project.accept(plantUmlCodeBlocksCounter);
		int numCodeBlocks = plantUmlCodeBlocksCounter.getNumCodeBlocksFound();
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Analyzing PlantUML code", numCodeBlocks);

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
		plantUmlResourceVisitor.setMonitor(monitor);
		getProject().accept(plantUmlResourceVisitor);
	}
	
	private void incrementalBuild(IResourceDelta resourceDelta, SubMonitor monitor) throws CoreException {
		plantUmlResourceVisitor.setMonitor(monitor);
		resourceDelta.accept(plantUmlResourceVisitor);
	}
	
	private static class PlantUMLCodeBlocksCountingVisitor implements IResourceVisitor, IResourceDeltaVisitor {
		
		private int numCodeBlocksFound = 0;

		@Override
		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IContainer) {
				return PlantUMLValidationVisitor.shouldVisitMembers((IContainer) resource);
			}
			
			if (resource instanceof IFile) {
				if (FileUtils.isPumlFile((IFile) resource)) {
					numCodeBlocksFound++;
				} else if (FileUtils.isMarkdownFile((IFile) resource)) {
					// TODO find PlantUML code blocks and count these, too?
					numCodeBlocksFound++; // just count the files for now
				}
			}
			
			return false;
		}

		@Override
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			return visit(resource);
		}
		
		public int getNumCodeBlocksFound() {
			return this.numCodeBlocksFound;
		}
		
	}
	
}
