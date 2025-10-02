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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
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
import net.certiv.fluentmark.core.util.FlexmarkUtil;
import net.certiv.fluentmark.ui.util.EditorUtils;
import net.certiv.fluentmark.ui.util.FlexmarkUiUtil;

public class InlinePlantUmlCodeRefactoring extends AbstractReplaceMarkdownImageRefactoring {
	
	private final static String MSG_INLINE_CODE = "Replace *.puml references (images) with in-lined PlantUML code blocks in Markdown files";
	private final static String MSG_AND_DELETE_PUMLS = " and remove the no longer referenced *.puml files";
	
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
	
	@Override
	protected String getImageFileExtensionToReplace() {
		return FileUtils.FILE_EXTENSION_PLANTUML;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {
		
		RefactoringStatus status = new RefactoringStatus(); // ok status -> go to preview page, no error page
		
		for (IResource rootResource: rootResources) {
			if (!(rootResource instanceof IProject)) {
				IFolder parentDocFolder = FileUtils.getParentDocFolder(rootResource);
				if (parentDocFolder != null && !rootResource.equals(parentDocFolder)) {
					String message = "There might be Markdown files in other folders"
							+ " of your documentation that point to *.puml files that you are going to delete."
							+ " Avoid that by selecting your selected resource's parent project ("
							+ rootResource.getProject().getName() + ") or documentation folder";
					
					boolean containsMessage = Arrays.stream(status.getEntries())
							.anyMatch(entry -> message.equals(entry.getMessage()));
					
					if (!containsMessage) {
						status.addWarning(message);
					}
				}
			}
		}
		
		checkDuplicationsInInlinedCode(monitor, status);
		
		return status;
	}
	
	private void checkDuplicationsInInlinedCode(IProgressMonitor monitor, RefactoringStatus status) throws CoreException {
		// TODO Check occurrences in the whole workspace instead of the parent projects of the selected resources?
		// TODO Start from selected puml files to search for Markdown files pointing to the puml files
		// TODO update checking pre-conditions
		
		if (hasTextSelection()) {
			IFile markdownFile = (IFile) rootResources.iterator().next();
			Image imageNode = FlexmarkUiUtil.findMarkdownImageForTextSelection(markdownFileDocument, textSelection);
			
			if (imageNode == null) {
				status.addFatalError("Could not find a Markdown image for the given text selection.");
				return;
			}
			
			String urlOrPath = imageNode.getUrl().toString();
			
			if (urlOrPath.toLowerCase().startsWith("http")
					|| !urlOrPath.toLowerCase().endsWith(".puml")) {
				status.addFatalError("Selected Markdown image does not reference a local PlantUML file (*.puml). Only PlantUML files can be in-lined.");
				return;
			}
			
			IPath pumlFilePath = FileUtils.resolveToAbsoluteResourcePath(urlOrPath, markdownFile);
			
			if (pumlFilePath == null) {
				status.addFatalError(String.format("Cannot resolve file path %s.", urlOrPath));
				return;
			}
			
			String foundPumlFilePath = pumlFilePath.toString();
			
			Document markdownAst = imageNode.getDocument();
			
			List<Integer> resolvedPathCount = new ArrayList<>(1);
			FlexmarkUtil.getStreamOf(markdownAst, Image.class)
					.filter(image -> !image.getUrl().toString().toLowerCase().startsWith("http"))
					.map(image -> FileUtils.resolveToAbsoluteResourcePath(image.getUrl().toString(), markdownFile))
					.filter(path -> path != null)
					.map(path -> path.toString())
					.filter(path -> path.equals(foundPumlFilePath))
					.forEach(path -> {
						if (resolvedPathCount.size() == 0) {
							resolvedPathCount.addFirst(1);
						} else {
							resolvedPathCount.set(0, resolvedPathCount.getFirst() + 1);
						}
					});
			if (resolvedPathCount.getFirst() > 1) {
				String message = String.format("There are %s references to %s in %s. In-lining its content would duplicate code. ", resolvedPathCount.getFirst(), urlOrPath, markdownFile.getName());
				status.addError(message);
			}
			
			if (!imageNode.getText().isBlank()) {
				status.addWarning(String.format("The selected Markdown image statement uses a label (caption) \"%s\" that would be lost by in-lining the referenced PlantUML code.", imageNode.getText()));
			}
		} else {
			boolean wouldLooseCaptions = false;
			Map<IFile, IDocument> collectedMarkdownFiles = collectMarkdownFilesInRootResources(monitor);
			
			SubMonitor subMonitor = SubMonitor.convert(monitor, "Analyzing Markdown files", collectedMarkdownFiles.size() * 10);
			
			// go through all markdown files in the given resource selection
			Map<String, Integer> resolvedPathCounts = new HashMap<>();
			Map<String, Set<IFile>> resolvedPathToReferencingFileMap = new HashMap<>();
			
			for (IFile markdownFile : collectedMarkdownFiles.keySet()) {
				IDocument markdownDocument = collectedMarkdownFiles.get(markdownFile);
				
				// read file contents
				String markdownFileContents = getMarkdownFileContents(markdownFile, markdownDocument);
				subMonitor.worked(1);
				
				// parse markdown code
				Document markdownAst = FlexmarkUtil.parseMarkdown(markdownFileContents);
				subMonitor.worked(5);
				
				List<Image> nonHttpImages = FlexmarkUtil.getStreamOf(markdownAst, Image.class)
					.filter(image -> !image.getUrl().toString().toLowerCase().startsWith("http"))
					.toList();
				
				nonHttpImages.stream()
					.map(image -> FileUtils.resolveToAbsoluteResourcePath(image.getUrl().toString(), markdownFile))
					.filter(path -> path != null)
					.map(path -> path.toString())
					.forEach(path -> {
						Integer count = resolvedPathCounts.get(path);
						if (count == null) {
							resolvedPathCounts.put(path, 1);
						} else {
							resolvedPathCounts.put(path, count + 1);
						}
						
						Set<IFile> files = resolvedPathToReferencingFileMap.get(path);
						if (files == null) {
							files = new HashSet<>();
							resolvedPathToReferencingFileMap.put(path, files);
						}
						files.add(markdownFile);
				});
				
				// also check if we would loose a caption from the Markdown image statement; create only one warning if there is at least one such case
				if (!wouldLooseCaptions) {
					for (Image image: nonHttpImages) {
						if (!image.getText().isBlank()) {
							wouldLooseCaptions = true;
						}
					}
				}
				
				subMonitor.worked(4);
			}
			
			resolvedPathCounts.keySet().stream()
				.filter(path -> resolvedPathCounts.get(path) > 1)
				.forEach(path -> {
					Set<IFile> referencingFiles = resolvedPathToReferencingFileMap.get(path);
					String message = String.format("There are multiple references to %s. In-lining its content would duplicate code. ", path);
					
					if (referencingFiles.size() > 3) {
						message += referencingFiles.size() + " are referencing that PlantUML file.";
					} else {
						List<String> fileNames = referencingFiles.stream().map(file -> file.getName()).toList();
						message += "Referencing files: " + String.join(", ", fileNames);
					}
					
					status.addError(message);
				});
			
			if (wouldLooseCaptions) {
				status.addWarning("At least one Markdown image statement uses a label (caption) that would be lost by in-lining the referenced PlantUML code.");
			}
		}
	}
	
	@Override
	protected String getOverallChangeName(boolean alsoDeleteReferencedFiles) {
		return MSG_INLINE_CODE + (alsoDeleteReferencedFiles ? MSG_AND_DELETE_PUMLS : "");
	}
	
	@Override
	protected String getSingleMarkdownFileChangeName(IFile markdownFile) {
		return "Replace .puml file reference in \"" + markdownFile.getFullPath().toString() + "\" with PlantUML code block";
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
		
		// Check if the file is already open in some text editor.
		// If so, use the potentially modified, unsaved document instead of its saved version.
		String pumlFileContents = null;
		IDocument document = EditorUtils.findDocumentFromDirtyTextEditorFor(imageFile);
		if (document != null) {
			pumlFileContents = document.get();
		} else {
			pumlFileContents = FileUtils.readFileContents(imageFile);
		}
		
		if (PlantUmlParsingTools.getNumberOfDiagrams(pumlFileContents) != 1) {
			return null;
		}
		
		// Add edit operation: replace Markdown image statement with a PlantUML code block
		String lineSeparator = FileUtils.getPreferredLineSeparatorFor(markdownFile);
		int startOffset = imageNode.getStartOffset();
		int imageStatementLength = imageNode.getEndOffset() - startOffset;
		
		// TODO somehow also add the image label to the in-lined PlantUML code?
		
		StringBuilder replacementTextBuilder = new StringBuilder();
		replacementTextBuilder.repeat(lineSeparator, getNumberOfPreceedingLineSeparatorsToAdd(imageNode));
		replacementTextBuilder.append("```plantuml");
		replacementTextBuilder.append(lineSeparator);
		replacementTextBuilder.append(pumlFileContents.trim());
		replacementTextBuilder.append(lineSeparator);
		replacementTextBuilder.append("```");
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
