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

import net.certiv.fluentmark.ui.refactoring.InlinePlantUmlCodeRefactoring;

public class InlinePlantUmlCodeUserInputPage extends UserInputWizardPage {
	
	private static final String TITLE_FILE_SET           = "Inline PlantUML code from referenced *.puml files in selected Markdown file(s)";
	private static final String TITLE_SINGLE_PUML_IMAGE   = "Inline PlantUML code from the referenced *.puml file";
	private static final String DESCRIPTION_FILE_SET = "Inline PlantUML code from referenced *.puml files in selected Markdown files."
			+ "\n(Replace *.puml images with PlantUML code blocks in your Markdown code.)"
			+ "\nIf you want, this will also remove the obsolete *.puml files."
			+ " Other references to these *.puml files are not checked.";
	private static final String DESCRIPTION_SINGLE_PUML_IMAGE = "Inline the PlantUML diagram code from the referenced *.puml file."
			+ "\n(Replace the *.puml image with the PlantUML code from the referenced *.puml file.)"
			+ "\nIf you want, this will also remove the obsolete *.puml file."
			+ " Other references to this *.puml file are not checked.";

	private final InlinePlantUmlCodeRefactoring refactoring;
	private Button deletePumlFilesCheckBox;
	private Button useFencedCodeBlocksCheckBox;
	
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
		
		Set<String> resourceNames = refactoring.getRootResources().stream().map(rootResource -> "'" + rootResource.getFullPath() + "'").collect(Collectors.toSet());
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		
		if (refactoring.hasTextSelection()) {
			label.setText("This refactoring will handle the selected Markdown image (PlantUML diagram) in the Markdown file "
					+ resourceNames.iterator().next() + ".");
		} else {
			label.setText("This refactoring will process all Markdown files in "
					+ (resourceNames.size() > 1 ? "\n" : "")
					+ String.join("\n", resourceNames) + ".");
		}
		
		
		deletePumlFilesCheckBox = new Button(composite, SWT.CHECK);
		deletePumlFilesCheckBox.setLayoutData(new GridData());
		deletePumlFilesCheckBox.setText("Delete obsolete *.puml file" + (refactoring.hasTextSelection() ? "" : "s"));
		deletePumlFilesCheckBox.setSelection(!refactoring.hasTextSelection());
		
		useFencedCodeBlocksCheckBox = new Button(composite, SWT.CHECK);
		useFencedCodeBlocksCheckBox.setLayoutData(new GridData());
		useFencedCodeBlocksCheckBox.setText("Use fenced code blocks to wrap PlantUML code");
		useFencedCodeBlocksCheckBox.setSelection(true);
		
		updateRefactoringSettings();
		
		deletePumlFilesCheckBox.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateRefactoringSettings();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		useFencedCodeBlocksCheckBox.addSelectionListener(new SelectionListener() {
			
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
		refactoring.setDeletePumlFiles(deletePumlFilesCheckBox.getSelection());
		refactoring.setUseFencedCodeBlocks(useFencedCodeBlocksCheckBox.getSelection());
	}
}
