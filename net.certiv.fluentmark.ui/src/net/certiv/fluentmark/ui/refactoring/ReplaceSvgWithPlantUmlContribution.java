/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.refactoring;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

public class ReplaceSvgWithPlantUmlContribution extends RefactoringContribution {

	@Override
	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment,
			Map<String, String> arguments, int flags) throws IllegalArgumentException {
		// TODO check the arguments
		return new ReplaceSvgWithPlantUmlRefactoringDescriptor(id, project, description, comment, flags);
	}

}
