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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import com.advantest.markdown.MarkdownParserAndHtmlRenderer;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.util.MarkdownFileCountingVisitor;


public class ReplaceSvgWithPlantUmlRefactoring extends Refactoring {
	
	private final IResource rootResource;
	private Map<IFile, IDocument> collectedMarkdownFiles;
	private final MarkdownParserAndHtmlRenderer markdownParser = new MarkdownParserAndHtmlRenderer();
	
	public ReplaceSvgWithPlantUmlRefactoring(IResource rootResource) {
		this.rootResource = rootResource;
	}

	@Override
	public String getName() {
		return "Replace *.svg links with *.puml links in Markdown files in selected resources";
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		if (rootResource == null || !rootResource.exists() || !rootResource.isAccessible()) {
			return RefactoringStatus.createErrorStatus("Not applicable to given resource");
		}
		
		return RefactoringStatus.createInfoStatus("ok");
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return RefactoringStatus.createInfoStatus("finish");
	}

	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		MarkdownFileCountingVisitor mdFileCounter = new MarkdownFileCountingVisitor();
		rootResource.accept(mdFileCounter);
		int numMdFiles = mdFileCounter.getNumMdFilesFound();
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Analyzing Markdown files", numMdFiles * 10);
		
		MarkdownFilesCollectingVisitor markdownFilesCollector = new MarkdownFilesCollectingVisitor();
		markdownFilesCollector.setMonitor(subMonitor);
		rootResource.accept(markdownFilesCollector);
		collectedMarkdownFiles = markdownFilesCollector.getCollectedMarkdownFiles();
		
		ArrayList<Change> fileModifications = new ArrayList<>();
		ArrayList<Change> fileDeletions = new ArrayList<>();
		
		// go through all markdown files in the given resource selection
		for (IFile markdownFile : collectedMarkdownFiles.keySet()) {
			IDocument markdownDocument = collectedMarkdownFiles.get(markdownFile);
			
			String markdownFileContents;
			if (markdownDocument == null) {
				markdownFileContents = FileUtils.readFileContents(markdownFile);
			} else {
				markdownFileContents = markdownDocument.get();
			}
			
			// parse markdown code
			Document markdownAst = markdownParser.parseMarkdown(markdownFileContents);
			subMonitor.worked(5);
			
			// go through all image links
			ReversiblePeekingIterator<Node> iterator = markdownAst.getChildIterator();
			while (iterator.hasNext()) {
				Node astNode = iterator.next();
				if (astNode instanceof Image) {
					Image imageNode = (Image) astNode;
					BasedSequence urlSequence = imageNode.getUrl();
					String urlOrPath = urlSequence.toString();
					
					// check only svg file links
					if (!urlOrPath.toLowerCase().startsWith("http")
							&& urlOrPath.toLowerCase().endsWith(".svg")) {
						
						// check if there is an equally named puml file
						IPath resolvedSvgTargetPath =FileUtils.resolveToAbsoluteResourcePath(urlOrPath, markdownFile);
						String svgFileName = resolvedSvgTargetPath.lastSegment();
						int indexOfLastDot = svgFileName.lastIndexOf('.');
						String pumlFileName = svgFileName.substring(0, indexOfLastDot) + ".puml";
						IPath resolvedPumlTargetPath = resolvedSvgTargetPath.removeLastSegments(1).append(pumlFileName);
						
						List<IFile> foundPumlFiles = FileUtils.findFilesForLocation(resolvedPumlTargetPath);
						List<IFile> foundSvgFiles = FileUtils.findFilesForLocation(resolvedSvgTargetPath); 
						
						if (!foundPumlFiles.isEmpty() && foundPumlFiles.size() == 1) {
							IFile pumlFile = foundPumlFiles.getFirst();
							
							// Add edit operation: replace .svg with .puml file
							int startOffset = urlSequence.getStartOffset() + urlOrPath.toLowerCase().indexOf(".svg") + 1;
							TextEdit editOperation = new ReplaceEdit(startOffset, 3, "puml");
							
							String changeName = "Replace .svg with .puml in \"" + urlOrPath + "\"";
							if (markdownDocument != null) {
								DocumentChange markdownFileDocumentChange = new DocumentChange(changeName, markdownDocument);
								markdownFileDocumentChange.setEdit(editOperation);
								fileModifications.add(markdownFileDocumentChange);
							} else {
								TextFileChange markdownFileChange = new TextFileChange(changeName, markdownFile);
								markdownFileChange.setEdit(editOperation);
								fileModifications.add(markdownFileChange);
							}
							
							// Add delete operation: delete obsolete .svg file
							if (!foundSvgFiles.isEmpty() && foundSvgFiles.size() == 1) {
								IFile svgFile = foundSvgFiles.getFirst();
								Optional<Change> deleteChangeForGivenSvg = fileDeletions.stream()
									.filter(change -> change instanceof DeleteFileChange)
									.filter(change -> ((DeleteFileChange) change).getResourcePath().equals(svgFile.getFullPath()))
									.findAny();
								if (deleteChangeForGivenSvg.isEmpty()) {
									DeleteFileChange deleteSvgFileChange = new DeleteFileChange(svgFile.getFullPath(), false);
									fileDeletions.add(deleteSvgFileChange);
								}
							}
						}
					}
				}
			}
			
			subMonitor.worked(5);
		}
		
		CompositeChange change = new CompositeChange(getName());
		CompositeChange modifications = new CompositeChange("Replace *.svg links with *.puml links in Markdown", fileModifications.toArray(new Change[fileModifications.size()]) );
		CompositeChange deletions = new CompositeChange("Delete obsolete *.svg files", fileDeletions.toArray(new Change[fileDeletions.size()]) );
		change.add(modifications);
		change.add(deletions);
		
		return change;
	}

}
