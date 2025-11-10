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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
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
		
		RefactoringStatus status = new RefactoringStatus();
		
		// TODO Check occurrences in the whole workspace instead of the parent projects of the selected resources?
		// TODO Start from selected puml files to search for Markdown files pointing to the puml files
		
		Map<IFile, IDocument> allMarkdownFilesFromSelectedProjects = collectMarkdownFilesInSelectedRootResourcesProjects(SubMonitor.convert(monitor));
		Set<String> resolvedImagePathsToReplace = new HashSet<>();
		
		Map<String, Integer> resolvedPathCounters = new HashMap<>();
		Map<String, Set<IFile>> resolvedPathToReferencingFileMap = new HashMap<>();
		
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Analyzing Markdown files", allMarkdownFilesFromSelectedProjects.size() * 10);
		
		if (hasTextSelection()) {
			IFile markdownFile = (IFile) rootResources.iterator().next();
			Image imageNode = FlexmarkUiUtil.findMarkdownImageForTextSelection(markdownFileDocument, textSelection);
			
			if (!isChangeApplicableTo(imageNode)) {
				status.addFatalError("Selected Markdown image does not reference a local PlantUML file (*.puml). Only PlantUML files can be in-lined.");
				return status;
			}
			
			String urlOrPath = imageNode.getUrl().toString();
			IPath pumlFilePath = FileUtils.resolveToAbsoluteResourcePath(urlOrPath, markdownFile);
			
			if (pumlFilePath == null) {
				status.addFatalError(String.format("Cannot resolve file path %s.", urlOrPath));
				return status;
			}
			
			String foundPumlFilePath = pumlFilePath.toString();
			resolvedImagePathsToReplace.add(foundPumlFilePath);
			
			for (IFile currentMarkdownFile : allMarkdownFilesFromSelectedProjects.keySet()) {
				IDocument markdownDocument = allMarkdownFilesFromSelectedProjects.get(currentMarkdownFile);
				
				streamOfResolvedPathsFromMarkdownImagesInGivenFile(currentMarkdownFile, markdownDocument)
					.map(path -> path.toString())
					.filter(path -> resolvedImagePathsToReplace.contains(path))
					.forEach(path -> {
						incrementResolvedPathCounter(resolvedPathCounters, path);
						rememberReferencingFile(path, currentMarkdownFile, resolvedPathToReferencingFileMap);
				});
				
				subMonitor.worked(10);
			}
			
			if (!imageNode.getText().isBlank()) {
				status.addWarning(String.format("The selected Markdown image statement uses a label (caption) \"%s\" that would be lost by in-lining the referenced PlantUML code.", imageNode.getText()));
			}
		} else {
			boolean wouldLooseCaptions = false;
			
			Map<IFile, IDocument> selectedMarkdownFiles = collectMarkdownFilesInRootResources(monitor);
			
			// check Markdown files from selected resources first and collect all references to puml files
			for (IFile markdownFile : selectedMarkdownFiles.keySet()) {
				IDocument markdownDocument = selectedMarkdownFiles.get(markdownFile);
				
				List<Image> pumlImages = streamOfMarkdownImagesInGivenFile(markdownFile, markdownDocument).toList();
				
				streamOfResolvedPathsFromMarkdownImagesInGivenFile(pumlImages.stream(), markdownFile)
					.map(path -> path.toString())
					.forEach(path -> {
						resolvedImagePathsToReplace.add(path);
						
						incrementResolvedPathCounter(resolvedPathCounters, path);
						rememberReferencingFile(path, markdownFile, resolvedPathToReferencingFileMap);
				});
				
				// also check if we would loose a caption from the Markdown image statement; create only one warning if there is at least one such case
				for (Image image: pumlImages) {
					if (!wouldLooseCaptions && !image.getText().isBlank()) {
						wouldLooseCaptions = true;
						break;
					}
				}
				
				subMonitor.worked(10);
			}
			
			// then check all remaining Markdown files for references to the same puml files to count references
			for (IFile markdownFile : allMarkdownFilesFromSelectedProjects.keySet()) {
				if (selectedMarkdownFiles.containsKey(markdownFile)) {
					continue;
				}
				
				IDocument markdownDocument = allMarkdownFilesFromSelectedProjects.get(markdownFile);
				
				streamOfResolvedPathsFromMarkdownImagesInGivenFile(markdownFile, markdownDocument)
					.map(path -> path.toString())
					.filter(path -> resolvedImagePathsToReplace.contains(path))
					.forEach(path -> {
						incrementResolvedPathCounter(resolvedPathCounters, path);
						rememberReferencingFile(path, markdownFile, resolvedPathToReferencingFileMap);
				});
				
				subMonitor.worked(10);
			}
			
			if (wouldLooseCaptions) {
				status.addWarning("At least one Markdown image statement uses a label (caption) that would be lost by in-lining the referenced PlantUML code.");
			}
		}
		
		resolvedPathCounters.keySet().stream()
			.filter(path -> resolvedPathCounters.get(path) > 1)
			.forEach(path -> {
				Set<IFile> referencingFiles = resolvedPathToReferencingFileMap.get(path);
				int numReferencingFiles = referencingFiles.size();
				int numReferences = resolvedPathCounters.get(path);
				IFile targetFile = FileUtils.resolveToWorkspaceFile(new Path(path));
				
				String shortPath = path;
				if (targetFile != null) {
					IPath fullPath = targetFile.getFullPath();
					if (fullPath != null) {
						shortPath = fullPath.toOSString();
					}
				}
				
				String message = String.format("There are %s references in %s file(s) to %s. In-lining its content would duplicate code.", numReferences, numReferencingFiles, shortPath);
				
				if (numReferencingFiles <= 3) {
					List<String> fileNames = referencingFiles.stream().map(file -> file.getName()).toList();
					message += " Referencing files: " + String.join(", ", fileNames);
				}
				
				status.addError(message);
			});
		
		return status;
	}

	private void rememberReferencingFile(String resolvedPath, IFile markdownFile,
			Map<String, Set<IFile>> resolvedPathToReferencingFileMap) {
		
		Set<IFile> files = resolvedPathToReferencingFileMap.get(resolvedPath);
		if (files == null) {
			files = new HashSet<>();
			resolvedPathToReferencingFileMap.put(resolvedPath, files);
		}
		files.add(markdownFile);
	}

	private void incrementResolvedPathCounter(Map<String, Integer> resolvedPathCounters, String resolvedPath) {
		Integer count = resolvedPathCounters.get(resolvedPath);
		if (count == null) {
			resolvedPathCounters.put(resolvedPath, 1);
		} else {
			resolvedPathCounters.put(resolvedPath, count + 1);
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
