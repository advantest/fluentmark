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

import org.eclipse.core.resources.IResource;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ReplaceSvgWithPlantUmlUserInputPage extends UserInputWizardPage {
	
	private final IResource rootResource;
	private Button deleteSvgFilesCheckBox;

	public ReplaceSvgWithPlantUmlUserInputPage(IResource rootResource) {
		super("User Input Page");
		this.rootResource = rootResource;
		
		this.setTitle("Replace *.svg with *.puml in selected Markdown files");
		this.setDescription("Replace all references to *.svg files in selected Markdown files"
				+ " if equally named *.puml (PlantUML) files can be found in the same folders."
				+ " If you want, this will also remove the obsolete *.svg files."
				+ " Other references to these *.svg files are not checked.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setFont(parent.getFont());
		
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Process all Markdown files in '" + this.rootResource.getFullPath() + "'.");
		
		deleteSvgFilesCheckBox = new Button(composite, SWT.CHECK);
		deleteSvgFilesCheckBox.setLayoutData(new GridData());
		deleteSvgFilesCheckBox.setText("Delete obsolete *.svg files");
		deleteSvgFilesCheckBox.setSelection(true);
		
		setControl(composite);
	}
	
	public boolean isDeleteObsoleteSvgFilesChecked() {
		if (deleteSvgFilesCheckBox != null) {
			return deleteSvgFilesCheckBox.getSelection();
		}
		return true;
	}

}
