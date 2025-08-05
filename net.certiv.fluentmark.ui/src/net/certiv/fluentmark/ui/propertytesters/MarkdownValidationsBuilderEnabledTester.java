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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;

import net.certiv.fluentmark.ui.builders.MarkerCalculatingFileValidationBuilder;

public class MarkdownValidationsBuilderEnabledTester extends PropertyTester {

	private static final String IS_ENABLED = "isEnabled";

	public MarkdownValidationsBuilderEnabledTester() {
	}

	@Override
	public boolean test(final Object receiver, final String property, final Object[] args, final Object expectedValue) {

		if (IS_ENABLED.equals(property)) {
			final IProject project = (IProject) Platform.getAdapterManager().getAdapter(receiver, IProject.class);

			if (project != null)
				return MarkerCalculatingFileValidationBuilder.hasBuilder(project);
		}

		return false;
	}
}
