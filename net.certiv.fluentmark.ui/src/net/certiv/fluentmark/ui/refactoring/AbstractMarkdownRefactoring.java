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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

import com.advantest.markdown.MarkdownParserAndHtmlRenderer;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.util.FlexmarkUtil;

public abstract class AbstractMarkdownRefactoring extends Refactoring {
	
	protected final Set<IResource> rootResources = new HashSet<>();
	protected IDocument markdownFileDocument;
	protected ITextSelection textSelection;
	protected final MarkdownParserAndHtmlRenderer markdownParser = new MarkdownParserAndHtmlRenderer();
	
	public AbstractMarkdownRefactoring(IFile markdownFile, IDocument document, ITextSelection textSelection) {
		this.rootResources.add(markdownFile);
		this.markdownFileDocument = document;
		this.textSelection = textSelection;
	}
	
	public AbstractMarkdownRefactoring(List<IResource> rootResources) {
		this.rootResources.addAll(rootResources);
	}
	
	public boolean hasTextSelection() {
		return textSelection != null;
	}
	
	public Set<IResource> getRootResources() {
		return this.rootResources;
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		for (IResource rootResource: rootResources) {
			if (rootResource == null || !rootResource.exists() || !rootResource.isAccessible()) {
				return RefactoringStatus.createFatalErrorStatus("Refactoring not applicable to the given resource."
						+ " The following resource is not accessible. Resource: " + rootResource);
			}
		}
		
		return new RefactoringStatus(); // ok status -> go to preview page, no error page
	}
	
	protected abstract String getOverallChangeName(boolean alsoDeleteReferencedFiles);
	
	protected abstract String getSingleMarkdownFileChangeName(IFile markdownFile);
	
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
		String changeName = getOverallChangeName(fileDeletions.size() > 0);
		CompositeChange change = new CompositeChange(changeName, allChanges.toArray(new Change[allChanges.size()]));
		return change;
	}
	
	private void createEditsAndDeletionsForSingleMarkdownImage(ArrayList<Change> fileModifications,
			ArrayList<Change> fileDeletions, IProgressMonitor monitor) {
		
		IFile markdownFile = (IFile) rootResources.iterator().next();
		Image imageNode = FlexmarkUtil.findMarkdownImageForTextSelection(markdownFileDocument, textSelection);
		
		if (imageNode != null) {
			TextChange markdownFileChange = createEmptyMarkdownFileChange(markdownFile, markdownFileDocument, getSingleMarkdownFileChangeName(markdownFile));
			MultiTextEdit rootEditOnMarkdownFile = new MultiTextEdit();
			markdownFileChange.setEdit(rootEditOnMarkdownFile);
			
			createAndCollectEditsAndDeletionsForImage(markdownFile, imageNode,
					markdownFileChange, rootEditOnMarkdownFile, fileModifications, fileDeletions);
		}
	}
	
	private void createEditsAndDeletionsForResourcesSet(ArrayList<Change> fileModifications,
			ArrayList<Change> fileDeletions, IProgressMonitor monitor) throws CoreException {
		
		Map<IFile, IDocument> collectedMarkdownFiles = collectMarkdownFilesInRootResources(monitor);
		
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Analyzing Markdown files", collectedMarkdownFiles.size() * 10);
		
		// go through all markdown files in the given resource selection
		for (IFile markdownFile : collectedMarkdownFiles.keySet()) {
			IDocument markdownDocument = collectedMarkdownFiles.get(markdownFile);
			
			// Concerning edit operations and refactoring see https://www.eclipse.org/articles/Article-LTK/ltk.html
			
			// prepare root change, but only add it to file modification edits if we find at least one text edit
			TextChange markdownFileChange = createEmptyMarkdownFileChange(markdownFile, markdownDocument, getSingleMarkdownFileChangeName(markdownFile));
			MultiTextEdit rootEditOnMarkdownFile = new MultiTextEdit();
			markdownFileChange.setEdit(rootEditOnMarkdownFile);
			
			// read file contents
			String markdownFileContents = getMarkdownFileContents(markdownFile, markdownDocument);
			subMonitor.worked(1);
			
			// parse markdown code
			Document markdownAst = markdownParser.parseMarkdown(markdownFileContents);
			subMonitor.worked(5);
			
			// go through all Markdown images and create text edits
			createAndCollectEditsAndDeletionsForImagesInMarkdownFile(markdownFile, markdownAst,
					markdownFileChange, rootEditOnMarkdownFile, fileModifications, fileDeletions);
			subMonitor.worked(4);
		}
	}

	private TextChange createEmptyMarkdownFileChange(IFile markdownFile, IDocument markdownDocument, String changeName) {
		TextChange fileChange;
		if (markdownDocument != null) {
			fileChange = new DocumentChange(changeName, markdownDocument);
		} else {
			fileChange = new TextFileChange(changeName, markdownFile);
		}
		return fileChange;
	}
	
	private void createAndCollectEditsAndDeletionsForImage(IFile markdownFile, Image imageNode, TextChange markdownFileChange,
			MultiTextEdit rootEditOnMarkdownFile, ArrayList<Change> fileModifications, ArrayList<Change> fileDeletions) {
		
		BasedSequence urlSequence = imageNode.getUrl();
		String urlOrPath = urlSequence.toString();
		
		// check only references to files
		if (urlOrPath.toLowerCase().startsWith("http")) {
			return;
		}
		
		IPath resolvedImageFilePath = FileUtils.resolveToAbsoluteResourcePath(urlOrPath, markdownFile);
		
		TextEdit replacementEdit = createMarkdownImageReplacementEdit(markdownFile, imageNode, resolvedImageFilePath);
		if (replacementEdit != null) {
			rootEditOnMarkdownFile.addChild(replacementEdit);
			
			// add our file change, since we now know we have at least one edit in that file
			if (!fileModifications.contains(markdownFileChange)) {
				fileModifications.add(markdownFileChange);
			}
			
			// Add delete operation: delete obsolete image file
			addDeleteChange(fileDeletions, resolvedImageFilePath);
		}
	}
	
	private void createAndCollectEditsAndDeletionsForImagesInMarkdownFile(IFile markdownFile, Document markdownAst, TextChange markdownFileChange,
			MultiTextEdit rootEditOnMarkdownFile, ArrayList<Change> fileModifications, ArrayList<Change> fileDeletions) {
		
		ReversiblePeekingIterator<Node> iterator = markdownAst.getDescendants().iterator();
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
	
	protected abstract TextEdit createMarkdownImageReplacementEdit(IFile markdownFile, Image imageNode, IPath resolvedImageFilePath);
	
	protected abstract boolean getDeleteReferencedImageFiles();
	
	private void addDeleteChange(List<Change> fileDeletions, IPath resolvedImageFilePath) {
		if (!getDeleteReferencedImageFiles()) {
			return;
		}
		
		List<IFile> imageFilesToDelete = FileUtils.findExistingFilesForLocation(resolvedImageFilePath);
		
		if (imageFilesToDelete.isEmpty()) {
			return;
		}
		
		if (imageFilesToDelete.size() > 1) {
			FluentUI.log(IStatus.WARNING, "Found more than one file for the path " + resolvedImageFilePath + ". Skipping this case (will not delete the file).");
			return;
		}
		
		IFile imageFile = imageFilesToDelete.getFirst();
		
		Optional<Change> deleteChangeForGivenImage = fileDeletions.stream()
			.filter(change -> change instanceof DeleteFileChange)
			.filter(change -> ((DeleteFileChange) change).getResourcePath().equals(imageFile.getFullPath()))
			.findAny();
		
		if (deleteChangeForGivenImage.isEmpty()) {
			DeleteFileChange deleteImageFileChange = new DeleteFileChange(imageFile.getFullPath(), false);
			fileDeletions.add(deleteImageFileChange);
		}
	}
	
	private Map<IFile, IDocument> collectMarkdownFilesInRootResources(IProgressMonitor monitor) throws CoreException {
		Map<IFile, IDocument> collectedMarkdownFiles = new HashMap<IFile, IDocument>();
		
		for (IResource rootResource: rootResources) {
			MarkdownFilesCollectingVisitor markdownFilesCollector = new MarkdownFilesCollectingVisitor();
			markdownFilesCollector.setMonitor(SubMonitor.convert(monitor));
			rootResource.accept(markdownFilesCollector);
			addMissingFiles(collectedMarkdownFiles, markdownFilesCollector.getCollectedMarkdownFiles());
		}
		return collectedMarkdownFiles;
	}

	private void addMissingFiles(Map<IFile, IDocument> markdownFilesCollection, Map<IFile, IDocument> markdownFilesToAdd) {
		// avoid ConcurrentModificationException -> first collect all elements to add, than add them to the map
		Map<IFile, IDocument> missingFilesSubset = new HashMap<>();
		markdownFilesToAdd.keySet().stream()
			.filter(file -> !markdownFilesCollection.containsKey(file))
			.forEach(file -> missingFilesSubset.put(file, markdownFilesToAdd.get(file)));
		markdownFilesCollection.putAll(missingFilesSubset);
	}
	
	private String getMarkdownFileContents(IFile markdownFile, IDocument markdownDocument) {
		if (markdownDocument == null) {
			return FileUtils.readFileContents(markdownFile);
		} else {
			return markdownDocument.get();
		}
	}
	
}
