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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants2;

import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.builders.IMarkerCalculationResourcesVisitor;
import net.certiv.fluentmark.ui.builders.IncrementalMarkdownValidationProjectBuilder;
import net.certiv.fluentmark.ui.builders.IncrementalPlantUmlValidationProjectBuilder;
import net.certiv.fluentmark.ui.builders.MarkdownFileValidationVisitor;
import net.certiv.fluentmark.ui.builders.PlantUMLValidationVisitor;
import net.certiv.fluentmark.ui.extensionpoints.MarkerCalculationBuildersManager;
import net.certiv.fluentmark.ui.markers.MarkerCalculator;
import net.certiv.fluentmark.ui.markers.MarkerConstants;

public class RecalculateMarkersHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Set<IProject> projectSet = AddMarkerCalculationBuilderHandler.getProjects(event);
		recalculateMarkers(projectSet);
		
		return null;
	}
	
	static void recalculateMarkers(Set<IProject> projectSet) {
		final Map<IProject, Set<String>> projectBuilderIds = new HashMap<>();
		final Map<String, IMarkerCalculationResourcesVisitor> builderIdsToVisitorMap = MarkerCalculationBuildersManager.getInstance().getAllMarkerCalculationResourceVisitors();
		
		for (IProject project: projectSet) {
			if (project == null || !project.isAccessible()) {
				continue;
			}
			
			try {
				Set<String> builderIds = new HashSet<>();
				for (final ICommand buildSpec : project.getDescription().getBuildSpec()) {
					builderIds.add(buildSpec.getBuilderName());
				}
				projectBuilderIds.put(project, builderIds);
			} catch (CoreException e) {
				Log.error("Could not re-calculate markers on project " + project.getName(), e);
			}
		}
		
		Job job = new WorkspaceJob("Re-calculate markers and references") {

			@Override
			public boolean belongsTo(Object family) {
				return ResourcesPlugin.FAMILY_MANUAL_BUILD.equals(family);
			}

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) {
				for (IProject project: projectBuilderIds.keySet()) {
					Set<String> builderIds = projectBuilderIds.get(project);
					
					SubMonitor progress = SubMonitor.convert(monitor, builderIds.size());
					progress.setTaskName("Performing marker re-calculation on project " + project.getName() + "...");
					
					try {
						// First, remove all our markers on given project
						MarkerCalculator markerCalculator = MarkerCalculator.get();
						Set<String> markerIds = new HashSet<>();
						markerIds.add(MarkerConstants.MARKER_ID_DOCUMENTATION_PROBLEM);
						markerIds.add(MarkerConstants.MARKER_ID_TASK_MARKDOWN);
						markerIds.add(MarkerConstants.MARKER_ID_TASK_PLANTUML);
						for (String builderId: projectBuilderIds.get(project)) {
							for (String markerId: MarkerCalculationBuildersManager.getInstance().getMarkersForBuilder(builderId)) {
								markerIds.add(markerId);
							}
						}
						for (String markerId: markerIds) {
							markerCalculator.deleteAllMarkersOfType(project, markerId);
						}
						
						// Then, calculate new markers 
						for (String builderId: projectBuilderIds.get(project)) {
							if (IncrementalMarkdownValidationProjectBuilder.BUILDER_ID.equals(builderId)) {
								IMarkerCalculationResourcesVisitor visitor = new MarkdownFileValidationVisitor();
								visitor.setMonitor(progress.split(1));
								project.accept(visitor);
							}
							
							if (IncrementalPlantUmlValidationProjectBuilder.BUILDER_ID.equals(builderId)) {
								IMarkerCalculationResourcesVisitor visitor = new PlantUMLValidationVisitor();
								visitor.setMonitor(progress.split(1));
								project.accept(visitor);
							}
							
							for (String additionalBuilderId: builderIdsToVisitorMap.keySet()) {
								if (builderId.equals(additionalBuilderId)) {
									IMarkerCalculationResourcesVisitor visitor = builderIdsToVisitorMap.get(additionalBuilderId);
									visitor.setMonitor(progress.split(1));
									project.accept(visitor);
								}
							}
						}
					} catch (CoreException e) {
						return e.getStatus();
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
		job.setUser(true);
		job.schedule();
	}

}
