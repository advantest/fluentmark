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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.certiv.fluentmark.ui.refactoring.InlinePlantUmlCodeRefactoring;

public class InlinePlantUmlCodeUserInputPage extends UserInputWizardPage {
	
	private static final String TITLE_FILE_SET           = "Inline the PlantUML diagram code from all .puml files referenced in selected Markdown file(s)";
	private static final String TITLE_SINGLE_PUML_IMAGE   = "Inline the PlantUML diagram code from the referenced .puml file";
	
	private static final String DESCRIPTION_FILE_SET = "Replace .puml file references (Markdown images) with PlantUML code blocks from the referenced .puml files."
			+ "\nThis will replace all references to the same .puml files and remove the obsolete .puml files.";
	private static final String DESCRIPTION_SINGLE_PUML_IMAGE = "Replace the .puml file reference (Markdown image) with a PlantUML code block from the referenced .puml file."
			+ "\nThis will replace all references to the same .puml file and remove the obsolete .puml file.";

	private final InlinePlantUmlCodeRefactoring refactoring;
	
	public InlinePlantUmlCodeUserInputPage(InlinePlantUmlCodeRefactoring refactoring) {
		super("User Input Page");
		this.refactoring = refactoring;
		
		if (refactoring.hasTextSelection()) {
			this.setTitle(TITLE_SINGLE_PUML_IMAGE);
			this.setDescription(DESCRIPTION_SINGLE_PUML_IMAGE);
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
		
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		
		Set<String> projectNames = refactoring.getRootResources().stream()
				.map(rootResource -> rootResource.getProject())
				.map(project -> project.getName())
				.collect(Collectors.toSet());
		String projectList = String.join("\n", projectNames);
		
		if (refactoring.hasTextSelection()) {
			label.setText("This refactoring will check all Markdown files in project " + projectList
					+ "\nand handle all references to the PlantUML diagram file from the selected Markdown image.");
		} else {
			label.setText("This refactoring will process all Markdown files in the following projects:\n"
					+ projectList);
		}
		
		setControl(composite);
	}
	
}
