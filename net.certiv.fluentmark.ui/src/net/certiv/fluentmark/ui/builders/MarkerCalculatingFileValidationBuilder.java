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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import net.certiv.fluentmark.core.builders.AbstractFileValidationBuilder;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.markers.MarkerCreator;

public class MarkerCalculatingFileValidationBuilder extends AbstractFileValidationBuilder {
	
	public static final String BUILDER_ID = FluentUI.PLUGIN_ID + ".builder";
	
	public MarkerCalculatingFileValidationBuilder() {
		super(new MarkerCreator());
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
		
		monitor.subTask("Delete obsolete markers");
		MarkerCreator.deleteAllDocumentationProblemMarkers(project);
		MarkerCreator.deleteAllMarkdownTaskMarkers(project);
		MarkerCreator.deleteAllPlantUmlTaskMarkers(project);
	}

}
