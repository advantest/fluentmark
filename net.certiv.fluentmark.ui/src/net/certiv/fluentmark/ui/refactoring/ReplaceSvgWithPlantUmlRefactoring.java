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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.util.FlexmarkUtil;


public class ReplaceSvgWithPlantUmlRefactoring extends AbstractMarkdownRefactoring {
	
	private final static String MSG_ADAPT_LINKS = "Replace *.svg references (images) with *.puml references (images) in Markdown files";
	private final static String MSG_AND_DELETE_SVGS = " and remove obsolete *.svg files";
	
	private boolean deleteObsoleteSvgFiles = true;
	
	public ReplaceSvgWithPlantUmlRefactoring(IFile markdownFile, IDocument document, ITextSelection textSelection) {
		super(markdownFile, document, textSelection);
	}
	
	public ReplaceSvgWithPlantUmlRefactoring(List<IResource> rootResources) {
		super(rootResources);
	}
	
	@Override
	public String getName() {
		return MSG_ADAPT_LINKS + MSG_AND_DELETE_SVGS;
	}
	
	public void setDeleteSvgFiles(boolean deleteObsoleteSvgFiles) {
		this.deleteObsoleteSvgFiles = deleteObsoleteSvgFiles;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		for (IResource rootResource: rootResources) {
			if (deleteObsoleteSvgFiles && !(rootResource instanceof IProject)) {
				IFolder parentDocFolder = FileUtils.getParentDocFolder(rootResource);
				if (parentDocFolder != null && !rootResource.equals(parentDocFolder)) {
					return RefactoringStatus.createWarningStatus("There might be Markdown files in other folders"
							+ " of your documentation that point to *.svg files that you are going to delete."
							+ " Avoid that by selecting your selected resource's (" + rootResource.getFullPath() + ") parent project or documentation folder "
							+ parentDocFolder.getFullPath().toString());
				}
			}
		}
		
		return new RefactoringStatus(); // ok status -> go to preview page, no error page
	}

	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		ArrayList<Change> fileModifications = new ArrayList<>();
		ArrayList<Change> fileDeletions = new ArrayList<>();
		
		if (textSelection != null && rootResources.size() == 1 && markdownFileDocument != null) {
			createEditsAndDeletionsForSingleMarkdownImage(fileModifications, fileDeletions, monitor);
		} else {
			createEditsAndDeletionsForResourcesSet(fileModifications, fileDeletions, monitor);
		}
		
		ArrayList<Change> allChanges = new ArrayList<>(fileModifications.size() + fileDeletions.size());
		allChanges.addAll(fileModifications);
		allChanges.addAll(fileDeletions);
		String changeName = MSG_ADAPT_LINKS + (fileDeletions.size() > 0 ? MSG_AND_DELETE_SVGS : "");
		CompositeChange change = new CompositeChange(changeName, allChanges.toArray(new Change[allChanges.size()]));
		return change;
	}
	
	private void createEditsAndDeletionsForSingleMarkdownImage(ArrayList<Change> fileModifications,
			ArrayList<Change> fileDeletions, IProgressMonitor monitor) {
		
		IFile markdownFile = (IFile) rootResources.iterator().next();
		Image imageNode = FlexmarkUtil.findMarkdownImageForTextSelection(markdownFileDocument, textSelection);
		
		if (imageNode != null) {
			TextChange markdownFileChange = createMarkdownFileChange(markdownFile, markdownFileDocument);
			MultiTextEdit rootEditOnMarkdownFile = new MultiTextEdit();
			markdownFileChange.setEdit(rootEditOnMarkdownFile);
			
			createAndCollectEditsAndDeletionsForImage(markdownFile, imageNode,
					markdownFileChange, rootEditOnMarkdownFile, fileModifications, fileDeletions);
		}
	}

	private void createEditsAndDeletionsForResourcesSet(ArrayList<Change> fileModifications,
			ArrayList<Change> fileDeletions, IProgressMonitor monitor) throws CoreException {
		
		Map<IFile, IDocument> collectedMarkdownFiles = new HashMap<IFile, IDocument>();
		
		for (IResource rootResource: rootResources) {
			MarkdownFilesCollectingVisitor markdownFilesCollector = new MarkdownFilesCollectingVisitor();
			markdownFilesCollector.setMonitor(SubMonitor.convert(monitor));
			rootResource.accept(markdownFilesCollector);
			addMissingFiles(collectedMarkdownFiles, markdownFilesCollector.getCollectedMarkdownFiles());
		}
		
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Analyzing Markdown files", collectedMarkdownFiles.size() * 10);
		
		// go through all markdown files in the given resource selection
		for (IFile markdownFile : collectedMarkdownFiles.keySet()) {
			IDocument markdownDocument = collectedMarkdownFiles.get(markdownFile);
			
			// Concerning edit operations and refactoring see https://www.eclipse.org/articles/Article-LTK/ltk.html
			
			// prepare root change, but only add it to file modification edits if we find at least one text edit
			TextChange markdownFileChange = createMarkdownFileChange(markdownFile, markdownDocument);
			MultiTextEdit rootEditOnMarkdownFile = new MultiTextEdit();
			markdownFileChange.setEdit(rootEditOnMarkdownFile);
			
			// read file contents
			String markdownFileContents = getMarkdownFileContents(markdownFile, markdownDocument);
			subMonitor.worked(1);
			
			// parse markdown code
			Document markdownAst = markdownParser.parseMarkdown(markdownFileContents);
			subMonitor.worked(5);
			
			// go through all image links and create text edits
			createAndCollectEditsAndDeletions(markdownFile, markdownFileChange, rootEditOnMarkdownFile, markdownAst,
					fileModifications, fileDeletions);
			subMonitor.worked(4);
		}
	}

	private void createAndCollectEditsAndDeletionsForImage(IFile markdownFile, Image imageNode, TextChange markdownFileChange,
			MultiTextEdit rootEditOnMarkdownFile, ArrayList<Change> fileModifications, ArrayList<Change> fileDeletions) {
		
		BasedSequence urlSequence = imageNode.getUrl();
		String urlOrPath = urlSequence.toString();
		
		// check only svg file links
		if (urlOrPath.toLowerCase().startsWith("http")
				|| !urlOrPath.toLowerCase().endsWith(".svg")) {
			return;
		}
		
		// check if there is an equally named puml file
		IPath resolvedSvgTargetPath = FileUtils.resolveToAbsoluteResourcePath(urlOrPath, markdownFile);
		
		TextEdit replacementEdit = createImagePathReplacementEdit(urlOrPath, resolvedSvgTargetPath, urlSequence.getStartOffset());
		if (replacementEdit != null) {
			rootEditOnMarkdownFile.addChild(replacementEdit);
			
			// add our file change, since we now know we have at least one edit in that file
			if (!fileModifications.contains(markdownFileChange)) {
				fileModifications.add(markdownFileChange);
			}
			
			// Add delete operation: delete obsolete .svg file
			addDeleteChange(fileDeletions, resolvedSvgTargetPath);
		}
	}

	private void createAndCollectEditsAndDeletions(IFile markdownFile, TextChange markdownFileChange,
			MultiTextEdit rootEditOnMarkdownFile, Document markdownAst, ArrayList<Change> fileModifications,
			ArrayList<Change> fileDeletions) {
		
		ReversiblePeekingIterator<Node> iterator = markdownAst.getChildIterator();
		while (iterator.hasNext()) {
			Node astNode = iterator.next();
			
			if (!(astNode instanceof Image)) {
				continue;
			}
			
			Image imageNode = (Image) astNode;
			createAndCollectEditsAndDeletionsForImage(markdownFile, imageNode,
					markdownFileChange, rootEditOnMarkdownFile, fileModifications, fileDeletions);
		}
	}
	
	private TextChange createMarkdownFileChange(IFile markdownFile, IDocument markdownDocument) {
		TextChange fileChange;
		String changeName = "Replace .svg with .puml in \"" + markdownFile.getLocation().toString() + "\"";
		if (markdownDocument != null) {
			fileChange = new DocumentChange(changeName, markdownDocument);
		} else {
			fileChange = new TextFileChange(changeName, markdownFile);
		}
		return fileChange;
	}
	
	private String getMarkdownFileContents(IFile markdownFile, IDocument markdownDocument) {
		if (markdownDocument == null) {
			return FileUtils.readFileContents(markdownFile);
		} else {
			return markdownDocument.get();
		}
	}
	
	private TextEdit createImagePathReplacementEdit(String originalUrlOrPath, IPath resolvedSvgTargetPath, int urlStartOffset) {
		String svgFileName = resolvedSvgTargetPath.lastSegment();
		String pumlFileName = svgFileName.substring(0, svgFileName.lastIndexOf('.')) + ".puml";
		IPath resolvedPumlTargetPath = resolvedSvgTargetPath.removeLastSegments(1).append(pumlFileName);
		
		List<IFile> foundPumlFiles = FileUtils.findExistingFilesForLocation(resolvedPumlTargetPath);
		
		if (foundPumlFiles.isEmpty()) {
			return null;
		}
		
		if (foundPumlFiles.size() > 1) {
			FluentUI.log(IStatus.WARNING, "Found more than one PlantUML file for the path " + resolvedPumlTargetPath + ". Skipping this case.");
			return null;
		}
		
		// Add edit operation: replace .svg with .puml file in given Markdown image
		int startOffset = urlStartOffset + originalUrlOrPath.toLowerCase().indexOf(".svg") + 1;
		return new ReplaceEdit(startOffset, 3, "puml");
	}
	
	private void addDeleteChange(List<Change> fileDeletions, IPath resolvedSvgTargetPath) {
		if (!deleteObsoleteSvgFiles) {
			return;
		}
		
		List<IFile> svgFilesToDelete = FileUtils.findExistingFilesForLocation(resolvedSvgTargetPath);
		
		if (svgFilesToDelete.isEmpty()) {
			return;
		}
		
		if (svgFilesToDelete.size() > 1) {
			FluentUI.log(IStatus.WARNING, "Found more than one SVG file for the path " + resolvedSvgTargetPath + ". Skipping this case.");
			return;
		}
		
		IFile svgFile = svgFilesToDelete.getFirst();
		
		Optional<Change> deleteChangeForGivenSvg = fileDeletions.stream()
			.filter(change -> change instanceof DeleteFileChange)
			.filter(change -> ((DeleteFileChange) change).getResourcePath().equals(svgFile.getFullPath()))
			.findAny();
		
		if (deleteChangeForGivenSvg.isEmpty()) {
			DeleteFileChange deleteSvgFileChange = new DeleteFileChange(svgFile.getFullPath(), false);
			fileDeletions.add(deleteSvgFileChange);
		}
	}
	
	private void addMissingFiles(Map<IFile, IDocument> markdownFilesCollection, Map<IFile, IDocument> markdownFilesToAdd) {
		// avoid ConcurrentModificationException -> first collect all elements to add, than add them to the map
		Map<IFile, IDocument> missingFilesSubset = new HashMap<>();
		markdownFilesToAdd.keySet().stream()
			.filter(file -> !markdownFilesCollection.containsKey(file))
			.forEach(file -> missingFilesSubset.put(file, markdownFilesToAdd.get(file)));
		markdownFilesCollection.putAll(missingFilesSubset);
	}

}
