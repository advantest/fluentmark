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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;


public class ReplaceSvgWithPlantUmlRefactoring extends AbstractReplaceMarkdownImageRefactoring {
	
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
	protected boolean getDeleteReferencedImageFiles() {
		return deleteObsoleteSvgFiles;
	}
	
	@Override
	protected String getImageFileExtensionToReplace() {
		return FileUtils.FILE_EXTENSION_SVG;
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
	protected String getOverallChangeName(boolean alsoDeleteReferencedFiles) {
		return MSG_ADAPT_LINKS + (alsoDeleteReferencedFiles ? MSG_AND_DELETE_SVGS : "");
	}
	
	@Override
	protected String getSingleMarkdownFileChangeName(IFile markdownFile) {
		return "Replace .svg with .puml in \"" + markdownFile.getFullPath().toString() + "\"";
	}
	
	@Override
	protected TextEdit createMarkdownImageReplacementEdit(IFile markdownFile, Image imageNode, IPath resolvedImageFilePath) {
		// abort if we have not an SVG file target path
		if (resolvedImageFilePath == null
				|| resolvedImageFilePath.getFileExtension() == null
				|| !FileUtils.FILE_EXTENSION_SVG.equals(resolvedImageFilePath.getFileExtension().toLowerCase())) {
			return null;
		}
		
		BasedSequence urlSequence = imageNode.getUrl();
		String path = urlSequence.toString();
		
		String svgFileName = resolvedImageFilePath.lastSegment();
		String pumlFileName = svgFileName.substring(0, svgFileName.lastIndexOf('.')) + "." + FileUtils.FILE_EXTENSION_PLANTUML;
		IPath resolvedPumlTargetPath = resolvedImageFilePath.removeLastSegments(1).append(pumlFileName);
		
		List<IFile> foundPumlFiles = FileUtils.findExistingFilesForLocation(resolvedPumlTargetPath);
		
		if (foundPumlFiles.isEmpty()) {
			return null;
		}
		
		if (foundPumlFiles.size() > 1) {
			FluentUI.log(IStatus.WARNING, "Found more than one PlantUML file for the path " + resolvedPumlTargetPath + ". Skipping this case.");
			return null;
		}
		
		// Add edit operation: replace .svg with .puml file in given Markdown image
		int startOffset = urlSequence.getStartOffset() + path.toLowerCase().indexOf("." + FileUtils.FILE_EXTENSION_SVG) + 1;
		return new ReplaceEdit(startOffset, FileUtils.FILE_EXTENSION_SVG.length(), FileUtils.FILE_EXTENSION_PLANTUML);
	}
	
}
