package net.certiv.fluentmark.ui.refactoring;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import net.certiv.fluentmark.core.plantuml.parsing.PlantUmlParsingTools;
import net.certiv.fluentmark.core.util.FileUtils;

public class InlinePlantUmlCodeRefactoring extends AbstractMarkdownRefactoring {
	
	private final static String MSG_INLINE_CODE = "Replace *.puml references (images) with in-lined PlantUML code blocks in Markdown files";
	private final static String MSG_AND_DELETE_PUMLS = " and remove the no longer referenced *.puml files";
	
	private boolean deleteObsoletePlantUmlFiles = true;
	private boolean useFencedCodeBlocks = true;
	
	/*
	 * Questions to clarify:
	 * - use current text selection or a set of directories, projects, and Markdown /PlantUML files as starting point?
	 * - current puml reference only or all uses of referenced puml file?
	 * - also delete no longer referenced puml files?
	 * - plain PlantUML code blocks or fenced PlantUML code blocks?
	 */
	
	public InlinePlantUmlCodeRefactoring(IFile markdownFile, IDocument document, ITextSelection textSelection) {
		super(markdownFile, document, textSelection);
	}
	
	public InlinePlantUmlCodeRefactoring(List<IResource> rootResources) {
		super(rootResources);
	}
	
	@Override
	public String getName() {
		return MSG_INLINE_CODE + MSG_AND_DELETE_PUMLS;
	}
	
	public void setDeletePumlFiles(boolean deleteObsoletePlantUmlFiles) {
		this.deleteObsoletePlantUmlFiles = deleteObsoletePlantUmlFiles;
	}
	
	@Override
	protected boolean getDeleteReferencedImageFiles() {
		return deleteObsoletePlantUmlFiles;
	}
	
	public void setUseFencedCodeBlocks(boolean useFencedCodeBlocks) {
		this.useFencedCodeBlocks = useFencedCodeBlocks;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		for (IResource rootResource: rootResources) {
			if (deleteObsoletePlantUmlFiles && !(rootResource instanceof IProject)) {
				IFolder parentDocFolder = FileUtils.getParentDocFolder(rootResource);
				if (parentDocFolder != null && !rootResource.equals(parentDocFolder)) {
					return RefactoringStatus.createWarningStatus("There might be Markdown files in other folders"
							+ " of your documentation that point to *.puml files that you are going to delete."
							+ " Avoid that by selecting your selected resource's (" + rootResource.getFullPath() + ") parent project or documentation folder "
							+ parentDocFolder.getFullPath().toString());
				}
			}
		}
		
		return new RefactoringStatus(); // ok status -> go to preview page, no error page
	}
	
	@Override
	protected String getOverallChangeName(boolean alsoDeleteReferencedFiles) {
		return MSG_INLINE_CODE + (alsoDeleteReferencedFiles ? MSG_AND_DELETE_PUMLS : "");
	}
	
	@Override
	protected String getSingleMarkdownFileChangeName(IFile markdownFile) {
		return "Replace .puml file reference in \"" + markdownFile.getLocation().toString() + "\" with PlantUML code block";
	}
	
	@Override
	protected TextEdit createMarkdownImageReplacementEdit(IFile markdownFile, Image imageNode, IPath resolvedImageFilePath) {
		// abort if we have not a PlantUML file target path
		if (resolvedImageFilePath == null
				|| resolvedImageFilePath.getFileExtension() == null
				|| !FileUtils.FILE_EXTENSION_PLANTUML.equals(resolvedImageFilePath.getFileExtension().toLowerCase())) {
			return null;
		}
		
		IFile imageFile = FileUtils.resolveToWorkspaceFile(resolvedImageFilePath);
		if (imageFile == null || !imageFile.isAccessible()) {
			return null;
		}
		
		String pumlFileContents = FileUtils.readFileContents(imageFile);
		
		if (PlantUmlParsingTools.getNumberOfDiagrams(pumlFileContents) != 1) {
			return null;
		}
		
		// Add edit operation: replace Markdown image statement with a PlantUML code block
		String lineSeparator = FileUtils.getPreferredLineSeparatorFor(markdownFile);
		int startOffset = imageNode.getStartOffset();
		int imageStatementLength = imageNode.getEndOffset() - startOffset;
		
		// TODO somehow also add the image label to the in-lined PlantUML code?
		// TODO Create a warning if some puml file content is in-lined more than once?
		
		StringBuilder replacementTextBuilder = new StringBuilder();
		replacementTextBuilder.repeat(lineSeparator, getNumberOfPreceedingLineSeparatorsToAdd(imageNode));
		if (useFencedCodeBlocks) {
			replacementTextBuilder.append("```plantuml");
			replacementTextBuilder.append(lineSeparator);
		}
		replacementTextBuilder.append(pumlFileContents.trim());
		if (useFencedCodeBlocks) {
			replacementTextBuilder.append(lineSeparator);
			replacementTextBuilder.append("```");
		}
		replacementTextBuilder.repeat(lineSeparator, getNumberOfSuccedingLineSeparatorsToAdd(imageNode));
		
		String replacementText = replacementTextBuilder.toString();
		
		return new ReplaceEdit(startOffset, imageStatementLength, replacementText);
	}
	
	private int getNumberOfPreceedingLineSeparatorsToAdd(Image imageNode) {
		boolean imageAtLineStart = (imageNode.getStartOffset() == imageNode.getStartOfLine());
		
		if (!imageAtLineStart) {
			return 2;
		}
		
		boolean hasEmptyPrecedingLine = false;
		int lineNumber = imageNode.getLineNumber();
		if (lineNumber > 0) {
			BasedSequence prevLine = imageNode.getDocument().getChars().lineAtAnyEOL(imageNode.getStartOfLine() - 1);
			hasEmptyPrecedingLine = prevLine.isBlank();
		}
		
		int numLinesToAdd = 0;
		
		if (!hasEmptyPrecedingLine) {
			numLinesToAdd++;
		}
		
		return numLinesToAdd;
	}
	
	private int getNumberOfSuccedingLineSeparatorsToAdd(Image imageNode) {
		boolean imageAtLineEnd = (imageNode.getEndOffset() == imageNode.getEndOfLine());
		
		if (!imageAtLineEnd) {
			return 2;
		}
		
		boolean hasEmptySuccedingLine = false;
		int lineNumber = imageNode.getLineNumber();
		Document markdownDocument = imageNode.getDocument();
		if (lineNumber + 1 < markdownDocument.getLineCount()) {
			BasedSequence nextLine = markdownDocument.getChars().lineAtAnyEOL(imageNode.getEndOfLine() + 1);
			hasEmptySuccedingLine = nextLine.isBlank();
		}
		
		int numLinesToAdd = 0;
		
		if (!hasEmptySuccedingLine) {
			numLinesToAdd++;
		}
		
		return numLinesToAdd;
	}
}
