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

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import net.certiv.fluentmark.core.markdown.parsing.MarkdownParsingTools;
import net.certiv.fluentmark.core.plantuml.parsing.PlantUmlParsingTools;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.util.FlexmarkUiUtil;


public class ExtractPlantUmlDiagramFileRefactoring extends Refactoring {

	protected final IFile markdownFile;
	protected final IDocument markdownFileDocument;
	protected final ITextSelection textSelection;
	private IFile pumlFileToCreate = null;
	
	public ExtractPlantUmlDiagramFileRefactoring(IFile markdownFile, IDocument markdownFileDocument, ITextSelection textSelection) {
		if (markdownFile == null || markdownFileDocument == null || textSelection == null) {
			throw new IllegalArgumentException();
		}
		
		this.markdownFile = markdownFile;
		this.markdownFileDocument = markdownFileDocument;
		this.textSelection = textSelection;
	}
	
	public void setPlantUmlFileToCreate(IFile pumlFile) {
		this.pumlFileToCreate = pumlFile;
	}

	@Override
	public String getName() {
		return "Extract the selected PlantUML code block to a separate diagram file.";
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus status = new RefactoringStatus();
		
		if (this.textSelection.isEmpty()
				|| this.textSelection.getEndLine() - this.textSelection.getStartLine() < 1
				|| PlantUmlParsingTools.getNumberOfDiagrams(FlexmarkUiUtil.getLinesForTextSelection(this.markdownFileDocument, this.textSelection)) != 1) {
			status.addFatalError("Text selection is empty or does not contain exactly one PlantUML diagram.");
			return status;
		}
		
		return status;
	}
	
	private String findRenderedPlantUmlCodeInTextSelection() {
		String selectedCodeBlock = FlexmarkUiUtil.getLinesForTextSelection(markdownFileDocument, textSelection);
		return MarkdownParsingTools.getPlantUmlCodeFromMarkdownCodeBlock(selectedCodeBlock);
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus status = new RefactoringStatus();
		
		if (this.pumlFileToCreate == null) {
			status.addFatalError("Cannot create PlantUML file " + this.pumlFileToCreate);
			return status;
		}
		
		if (this.pumlFileToCreate.exists()) {
			status.addFatalError("PlantUML file " + this.pumlFileToCreate + " already exists.");
			return status;
		}
		
		if (!this.markdownFile.isAccessible()) {
			status.addFatalError("Cannot modify Markdown file " + this.markdownFile);
			return status;
		}
		
		if (findRenderedPlantUmlCodeInTextSelection() == null) {
			status.addFatalError("There is no rendered PlantUML diagram in the selected text.");
			return status;
		}
		
		// TODO Check if the same diagram appears more than once in the open Markdown file or its parent project?
		return status;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		ArrayList<Change> changes = new ArrayList<>(2);
		
		String pumlCode = findRenderedPlantUmlCodeInTextSelection();
		
		if (pumlCode != null) {
			TextChange markdownFileChange = new DocumentChange("Replace PlantUML code block with file reference", markdownFileDocument);
			MultiTextEdit rootEditOnMarkdownFile = new MultiTextEdit();
			markdownFileChange.setEdit(rootEditOnMarkdownFile);
			
			TextEdit replacementEdit = createCodeBlockReplacementEdit();
			if (replacementEdit != null) {
				rootEditOnMarkdownFile.addChild(replacementEdit);
				
				CreateTextFileChange createPumlFileChange = createNewTextFileChange(pumlCode);
				
				if (createPumlFileChange != null) {
					changes.add(markdownFileChange);
					changes.add(createPumlFileChange);
				}
			}
		}
		
		CompositeChange change = new CompositeChange(
				"Extract PlantUML code block to " + this.pumlFileToCreate.getName(),
				changes.toArray(new Change[changes.size()]));
		return change;
	}
	
	private TextEdit createCodeBlockReplacementEdit() {
		try {
			int startLine = textSelection.getStartLine();
			int endLine = textSelection.getEndLine();
			int startOffset = markdownFileDocument.getLineOffset(startLine);
			int endOffset = markdownFileDocument.getLineOffset(endLine) + markdownFileDocument.getLineLength(endLine);
			
			// keep the line break at the end of the code block
			String codeBlockLastLineDelimiter = markdownFileDocument.getLineDelimiter(endLine);
			if (codeBlockLastLineDelimiter != null) {
				endOffset -= codeBlockLastLineDelimiter.length();
			}
			
			int length = endOffset - startOffset;
			// TODO is a project-relative path enough? What about files in other projects?
			IPath markdownFilePath = this.markdownFile.getProjectRelativePath();
			IPath pumlFilePath = this.pumlFileToCreate.getProjectRelativePath();
			IPath relativePumlFilePath = pumlFilePath.makeRelativeTo(markdownFilePath.removeLastSegments(1));
			String replacementText = String.format("![](%s)", relativePumlFilePath.toString());
			
			return new ReplaceEdit(startOffset, length, replacementText);
		} catch (Exception e) {
			FluentUI.log(IStatus.ERROR, "Could not create Markdown file modification.", e);
		}
		
		return null;
	}
	
	private CreateTextFileChange createNewTextFileChange(String fileContents) {
		Assert.isNotNull(fileContents);
		
		return new CreateTextFileChange(this.pumlFileToCreate, fileContents);
	}
	
	public IContainer getSuggestedParentForNewPlantUmlFile() {
		return markdownFile.getParent();
	}
}
