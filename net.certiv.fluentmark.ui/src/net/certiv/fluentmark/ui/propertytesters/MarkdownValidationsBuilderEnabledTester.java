/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.propertytesters;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.expressions.PropertyTester;

import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.builders.IncrementalMarkdownValidationProjectBuilder;

public class MarkdownValidationsBuilderEnabledTester extends PropertyTester {

	private static final String IS_ENABLED = "isEnabled";

	public MarkdownValidationsBuilderEnabledTester() {
	}

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {

		if (IS_ENABLED.equals(property)) {
			final IProject project = (IProject) Platform.getAdapterManager().getAdapter(receiver, IProject.class);

			if (project != null)
				return hasBuilder(project);
		}

		return false;
	}
	
	public static final boolean hasBuilder(final IProject project) {
		if (project == null || !project.isAccessible()) {
			return false;
		}
		
		try {
			for (final ICommand buildSpec : project.getDescription().getBuildSpec()) {
				if (IncrementalMarkdownValidationProjectBuilder.BUILDER_ID.equals(buildSpec.getBuilderName())) {
					return true;
				}
			}
		} catch (final CoreException e) {
			Log.error("Could not check project's build configuration, project: " + project.getName(), e);
		}

		return false;
	}

}
