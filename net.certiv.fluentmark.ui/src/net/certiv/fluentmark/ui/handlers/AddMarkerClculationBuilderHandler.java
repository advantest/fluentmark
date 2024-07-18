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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.builders.IncrementalMarkdownValidationProjectBuilder;
import net.certiv.fluentmark.ui.builders.IncrementalPlantUmlValidationProjectBuilder;
import net.certiv.fluentmark.ui.decorators.MarkdownFileValidationsDecorator;
import net.certiv.fluentmark.ui.extensionpoints.MarkerCalculationBuilderManager;
import net.certiv.fluentmark.ui.propertytesters.MarkdownValidationsBuilderEnabledTester;

public class AddMarkerClculationBuilderHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Set<IProject> projectSet = getProjects(event);
		MarkerCalculationBuilderManager builderManager = MarkerCalculationBuilderManager.getInstance();

		for (IProject project: projectSet) {
			try {
				// verify already registered builders
				if (MarkdownValidationsBuilderEnabledTester.hasBuilder(project)) {
					return null;
				}

				// add builder to project properties
				IProjectDescription description = project.getDescription();
				
				final List<ICommand> commands = new ArrayList<ICommand>();
				commands.addAll(Arrays.asList(description.getBuildSpec()));
				
				ICommand builderCommand = description.newCommand();
				builderCommand.setBuilderName(IncrementalMarkdownValidationProjectBuilder.BUILDER_ID);
				commands.add(builderCommand);
				
				builderCommand = description.newCommand();
				builderCommand.setBuilderName(IncrementalPlantUmlValidationProjectBuilder.BUILDER_ID);
				commands.add(builderCommand);
				
				// add builders from extensions
				Set<String> builderIds = builderManager.getMarkerCalculationBuilderIdsFromExtensions();
				for (String builderId: builderIds) {
					Set<String> natureIds = builderManager.getProjectNaturesForBuilder(builderId);
					
					if (natureIds.isEmpty() || hasAtLeastOneOfNatures(project, natureIds)) {
						builderCommand = description.newCommand();
						builderCommand.setBuilderName(builderId);
						commands.add(builderCommand);
					}
				}
				
				description.setBuildSpec(commands.toArray(new ICommand[commands.size()]));
				project.setDescription(description, null);
				
				IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
				decoratorManager.update(MarkdownFileValidationsDecorator.DECORATOR_ID);
			} catch (final CoreException e) {
				Log.error("Could not add marker calculation builder(s) to project " + project.getName(), e);
			}
		}
		
		// trigger initial marker calculation
		RecalculateMarkersHandler.recalculateMarkers(projectSet);

		return null;
	}
	
	private boolean hasAtLeastOneOfNatures(IProject project, Set<String> natureIds) throws CoreException {
		IProjectDescription description = project.getDescription();
		for (String projectNatureId: description.getNatureIds()) {
			for (String natureIdToFind: natureIds) {
				if (projectNatureId.equals(natureIdToFind)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Set<IProject> getProjects(final ExecutionEvent event) {
		final ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			Iterator<?> iterator = ((IStructuredSelection) selection).iterator();
			Set<IProject> projectSet = new HashSet<>();
			IAdapterManager adapterManager = Platform.getAdapterManager();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				IProject project = adapterManager.getAdapter(element, IProject.class);
				
				if (project != null) {
					projectSet.add(project);
				}
			}

			return projectSet;
		}

		return Collections.emptySet();
	}

}
