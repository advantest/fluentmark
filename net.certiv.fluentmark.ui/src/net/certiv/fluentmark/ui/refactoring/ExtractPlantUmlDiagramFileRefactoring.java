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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import net.certiv.fluentmark.core.plantuml.parsing.PlantUmlParsingTools;
import net.certiv.fluentmark.ui.util.FlexmarkUiUtil;


public class ExtractPlantUmlDiagramFileRefactoring extends Refactoring {

	protected final IFile markdownFile;
	protected final IDocument markdownFileDocument;
	protected final ITextSelection textSelection;
	
	public ExtractPlantUmlDiagramFileRefactoring(IFile markdownFile, IDocument markdownFileDocument, ITextSelection textSelection) {
		this.markdownFile = markdownFile;
		this.markdownFileDocument = markdownFileDocument;
		this.textSelection = textSelection;
	}

	@Override
	public String getName() {
		return "Extract the selected PlantUML code block to a separate diagram file.";
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus status = new RefactoringStatus();
		
		if (this.textSelection == null
				|| this.textSelection.isEmpty()
				|| this.textSelection.getEndLine() - this.textSelection.getStartLine() < 1
				|| PlantUmlParsingTools.getNumberOfDiagrams(FlexmarkUiUtil.getLinesForTextSelection(this.markdownFileDocument, this.textSelection)) != 1) {
			status.addError("Text selection is empty or does not contain exactly one PlantUML diagram.");
			return status;
		}
		
		return status;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public IContainer getSuggestedParentForNewPlantUmlFile() {
		return markdownFile.getParent();
	}
}
