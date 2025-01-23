/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.refactoring.wizards;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import net.certiv.fluentmark.ui.refactoring.ReplaceSvgWithPlantUmlRefactoring;

public class ReplaceSvgWithPlantUmlWizard extends RefactoringWizard {

	public ReplaceSvgWithPlantUmlWizard(ReplaceSvgWithPlantUmlRefactoring refactoring) {
		super(refactoring, RefactoringWizard.WIZARD_BASED_USER_INTERFACE);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void addUserInputPages() {
		addPage(new ReplaceSvgWithPlantUmlUserInputPage());
	}

}
