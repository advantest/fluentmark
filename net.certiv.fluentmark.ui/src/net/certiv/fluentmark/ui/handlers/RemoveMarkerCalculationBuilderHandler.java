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
import java.util.HashSet;
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
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;

import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.builders.IncrementalMarkdownValidationProjectBuilder;
import net.certiv.fluentmark.ui.decorators.MarkdownFileValidationsDecorator;
import net.certiv.fluentmark.ui.extensionpoints.MarkerCalculationBuilderManager;
import net.certiv.fluentmark.ui.validation.MarkerCalculator;
import net.certiv.fluentmark.ui.validation.MarkerConstants;

public class RemoveMarkerCalculationBuilderHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Set<IProject> projectSet = AddMarkerClculationBuilderHandler.getProjects(event);

		for (IProject project: projectSet) {
			try {
				final IProjectDescription description = project.getDescription();
				final List<ICommand> commands = new ArrayList<ICommand>();
				commands.addAll(Arrays.asList(description.getBuildSpec()));
				
				Set<String> builderIds = MarkerCalculationBuilderManager.getInstance().getMarkerCalculationBuilderIdsFromExtensions();
				
				Set<String> markerIds = new HashSet<>();
				markerIds.add(MarkerConstants.MARKER_ID_DOCUMENTATION_PROBLEM);
				markerIds.add(MarkerConstants.MARKER_ID_DOCUMENTATION_TASK);

				for (final ICommand buildSpec : description.getBuildSpec()) {
					if (IncrementalMarkdownValidationProjectBuilder.BUILDER_ID.equals(buildSpec.getBuilderName())) {
						commands.remove(buildSpec);
					}
					
					for (String builderId: builderIds) {
						if (buildSpec.getBuilderName().equals(builderId)) {
							commands.remove(buildSpec);
							
							for (String additionalMarkerId: MarkerCalculationBuilderManager.getInstance()
									.getMarkersForBuilder(builderId)) {
								markerIds.add(additionalMarkerId);
							}
						}
					}
				}

				description.setBuildSpec(commands.toArray(new ICommand[commands.size()]));
				project.setDescription(description, null);
				
				IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
				decoratorManager.update(MarkdownFileValidationsDecorator.DECORATOR_ID);
				
				MarkerCalculator markerCalculator = MarkerCalculator.get();
				for (String markerId: markerIds) {
					markerCalculator.deleteAllMarkersOfType(project, markerId);
				}
			} catch (final CoreException e) {
				Log.error("Could not remove Markdown marker calculation builder on project " + project.getName(), e);
			}
		}

		return null;
	}
	
}
