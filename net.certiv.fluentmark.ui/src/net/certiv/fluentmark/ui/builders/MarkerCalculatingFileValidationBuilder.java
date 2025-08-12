/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;

import net.certiv.fluentmark.core.builders.AbstractFileValidationBuilder;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.markers.MarkerCreator;

public class MarkerCalculatingFileValidationBuilder extends AbstractFileValidationBuilder {
	
	public static final String BUILDER_ID = FluentUI.PLUGIN_ID + ".builder";
	
	// TODO Some day remove the obsolete builder IDs
	public static final String OBSOLETE_BUILDER_ID_MARKDOWN = FluentUI.PLUGIN_ID + ".builders.markdown";
	
	private static final String[] OBSOLETE_BUILDER_IDS = {
			OBSOLETE_BUILDER_ID_MARKDOWN,
			FluentUI.PLUGIN_ID + ".builders.plantuml",
			"com.advantest.fluentmark.extensions.validations.builder.java.links",
			"com.advantest.fluentmark.extensions.validations.builder.cpp.links",
			"com.advantest.fluentmark.extensions.validations.builder.slang.links",
			"com.advantest.fluentmark.extensions.validations.builder.ruby.links"
	};
	
	public MarkerCalculatingFileValidationBuilder() {
		super(new MarkerCreator());
	}
	
	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		
		IProject[] result = super.build(kind, args, subMonitor.split(99));
		
		replaceObsoleteBuilderIds(getProject(), subMonitor.split(1));
		
		return result;
	}

	@Override
	public void clean(Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		if (monitor.isCanceled()) {
			return;
		}
		
		IProject project = getProject();
		if (!project.isAccessible()) {
			return;
		}
		SubMonitor subMonitor = SubMonitor.convert(monitor, 3);
		
		monitor.subTask("Delete obsolete markers");
		MarkerCreator.deleteAllDocumentationProblemMarkers(project);
		subMonitor.worked(1);
		
		MarkerCreator.deleteAllMarkdownTaskMarkers(project);
		subMonitor.worked(1);
		
		MarkerCreator.deleteAllPlantUmlTaskMarkers(project);
		subMonitor.worked(1);
	}
	
	public static boolean hasBuilder(final IProject project) {
		if (project == null || !project.isAccessible()) {
			return false;
		}
		
		try {
			for (final ICommand buildSpec : project.getDescription().getBuildSpec()) {
				if (MarkerCalculatingFileValidationBuilder.BUILDER_ID.equals(buildSpec.getBuilderName())
						|| MarkerCalculatingFileValidationBuilder.OBSOLETE_BUILDER_ID_MARKDOWN.equals(buildSpec.getBuilderName())) {
					return true;
				}
			}
		} catch (final CoreException e) {
			FluentUI.log(IStatus.ERROR, "Could not read build configuration for project " + project.getName(), e);
		}

		return false;
	}
	
	public static boolean isObsoleteBuilderId(String builderId) {
		if (builderId == null || builderId.isBlank()) {
			throw new IllegalArgumentException();
		}
		
		for (String obsoleteId : OBSOLETE_BUILDER_IDS) {
			if (obsoleteId.equals(builderId)) {
				return true;
			}
		}
		
		return false;
	}
	
	private void replaceObsoleteBuilderIds(IProject project, IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Update builder configuration (replace obsolete builders)");
		
		final IProjectDescription description = project.getDescription();
		final List<ICommand> commands = new ArrayList<ICommand>();
		commands.addAll(Arrays.asList(description.getBuildSpec()));
		
		// remove obsolete builder IDs
		for (final ICommand buildSpec : description.getBuildSpec()) {
			if (MarkerCalculatingFileValidationBuilder.isObsoleteBuilderId(buildSpec.getBuilderName())) {
				commands.remove(buildSpec);
			}
		}
		
		// add non-obsolete builder ID as replacement for the obsolete ones
		boolean containsNonObsoleteBuilderId = commands.stream()
				.map(command -> command.getBuilderName())
				.filter(command -> command.equals(BUILDER_ID))
				.findFirst()
				.isPresent();
		if (!containsNonObsoleteBuilderId) {
			ICommand builderCommand = description.newCommand();
			builderCommand.setBuilderName(MarkerCalculatingFileValidationBuilder.BUILDER_ID);
			commands.add(builderCommand);
		}
		
		// update project's build configuration
		description.setBuildSpec(commands.toArray(new ICommand[commands.size()]));
		project.setDescription(description, monitor);
	}

}
