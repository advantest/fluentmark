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

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ReplaceSvgWithPlantUmlUserInputPage extends UserInputWizardPage {

	public ReplaceSvgWithPlantUmlUserInputPage() {
		super("User Input Page");
	}

	@Override
	public void createControl(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText("TODO");
		setControl(label);
	}

}
