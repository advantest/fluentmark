/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.handlers;

import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants2;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.builders.MarkerCalculatingFileValidationBuilder;

public class RecalculateMarkersHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Set<IProject> projectSet = AddMarkerCalculationBuilderHandler.getProjects(event);
		recalculateMarkers(projectSet);
		
		return null;
	}
	
	static void recalculateMarkers(Set<IProject> projectSet) {
		final List<IProject> projectsWithOurBuilder = projectSet.stream()
				.filter(project -> project.isAccessible())
				.filter(project -> MarkerCalculatingFileValidationBuilder.hasBuilder(project))
				.toList();
		
		Job job = new WorkspaceJob("Re-calculate markers and references") {

			@Override
			public boolean belongsTo(Object family) {
				return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
			}

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor, projectsWithOurBuilder.size() * 100 + 1);
				
				for (IProject project: projectsWithOurBuilder) {
					if(subMonitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					
					subMonitor.setTaskName("Performing marker re-calculation on project " + project.getName() + "...");
					
					try {
						// trigger the builder's clean operation
						project.build(
								IncrementalProjectBuilder.CLEAN_BUILD, 
								MarkerCalculatingFileValidationBuilder.BUILDER_ID,
								null,
								subMonitor.split(1));
						
						// trigger a full build
						project.build(
								IncrementalProjectBuilder.FULL_BUILD, 
								MarkerCalculatingFileValidationBuilder.BUILDER_ID,
								null,
								subMonitor.split(100));
					} catch (CoreException e) {
						FluentUI.log(IStatus.ERROR, "Could not re-calculate markers on project " + project.getName(), e);
						return e.getStatus();
					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
		job.setUser(true);
		job.schedule();
	}

}
