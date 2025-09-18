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

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.certiv.fluentmark.ui.refactoring.ReplaceSvgWithPlantUmlRefactoring;

public class ReplaceSvgWithPlantUmlUserInputPage extends UserInputWizardPage {
	
	private static final String TITLE_FILE_SET       = "Replace *.svg references with *.puml in selected Markdown file(s)";
	private static final String TITLE_SINGLE_IMAGE   = "Replace *.svg reference with *.puml in selected Markdown image";
	private static final String DESCRIPTION_FILE_SET = "Replace all references to *.svg files in selected Markdown files"
			+ " if equally named *.puml (PlantUML) files can be found in the same folders."
			+ "\nIf you want, this will also remove the obsolete *.svg files."
			+ " Other references to these *.svg files are not checked.";
	private static final String DESCRIPTION_SINGLE_IMAGE = "Replace reference to *.svg file in selected Markdown image"
			+ " if an equally named *.puml (PlantUML) file can be found in the same folder."
			+ "\nIf you want, this will also remove the obsolete *.svg file."
			+ " Other references to this *.svg file are not checked.";
	
	private final ReplaceSvgWithPlantUmlRefactoring refactoring;
	private Button deleteSvgFilesCheckBox;

	public ReplaceSvgWithPlantUmlUserInputPage(ReplaceSvgWithPlantUmlRefactoring refactoring) {
		super("User Input Page");
		this.refactoring = refactoring;
		
		if (refactoring.hasTextSelection()) {
			this.setTitle(TITLE_SINGLE_IMAGE);
			this.setDescription(DESCRIPTION_SINGLE_IMAGE);
		} else {
			this.setTitle(TITLE_FILE_SET);
			this.setDescription(DESCRIPTION_FILE_SET);
		}
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setFont(parent.getFont());
		
		Set<String> resourceNames = refactoring.getRootResources().stream().map(rootResource -> "'" + rootResource.getFullPath() + "'").collect(Collectors.toSet());
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		
		if (refactoring.hasTextSelection()) {
			label.setText("This refactoring will handle the selected Markdown image in the Markdown file "
					+ resourceNames.iterator().next() + ".");
		} else {
			label.setText("This refactoring will process all Markdown files in "
					+ (resourceNames.size() > 1 ? "\n" : "")
					+ String.join("\n", resourceNames) + ".");
		}
		
		
		deleteSvgFilesCheckBox = new Button(composite, SWT.CHECK);
		deleteSvgFilesCheckBox.setLayoutData(new GridData());
		deleteSvgFilesCheckBox.setText("Delete obsolete *.svg file" + (refactoring.hasTextSelection() ? "" : "s"));
		deleteSvgFilesCheckBox.setSelection(!refactoring.hasTextSelection());
		updateRefactoringSettings();
		
		deleteSvgFilesCheckBox.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateRefactoringSettings();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		setControl(composite);
	}
	
	private void updateRefactoringSettings() {
		refactoring.setDeleteSvgFiles(deleteSvgFilesCheckBox.getSelection());
	}
}
