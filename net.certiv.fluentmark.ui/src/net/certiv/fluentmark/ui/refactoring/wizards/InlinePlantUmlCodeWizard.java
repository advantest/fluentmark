package net.certiv.fluentmark.ui.refactoring.wizards;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import net.certiv.fluentmark.ui.refactoring.InlinePlantUmlCodeRefactoring;

public class InlinePlantUmlCodeWizard extends RefactoringWizard {

private final InlinePlantUmlCodeRefactoring refactoring;
	
	public InlinePlantUmlCodeWizard(InlinePlantUmlCodeRefactoring refactoring) {
		super(refactoring, RefactoringWizard.WIZARD_BASED_USER_INTERFACE);
		this.refactoring = refactoring;
	}

	@Override
	protected void addUserInputPages() {
		addPage(new InlinePlantUmlCodeUserInputPage(refactoring));
	}
	
}
