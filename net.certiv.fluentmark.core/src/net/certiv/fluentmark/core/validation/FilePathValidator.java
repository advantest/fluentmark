/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.validation;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.markdown.parsing.MarkdownParsingTools;
import net.certiv.fluentmark.core.util.DocumentUtils;
import net.certiv.fluentmark.core.util.FileUtils;

public class FilePathValidator implements IValidationResultReporter {
	
	private final List<IAnchorResolver> resolvers;
	private IValidationResultConsumer issueConsumer;
	
	private String workspacePath = null;
	
	public FilePathValidator(List<IAnchorResolver> resolvers) {
		this.resolvers = resolvers;
	}
	
	@Override
	public void setValidationResultConsumer(IValidationResultConsumer issueConsumer) {
		this.issueConsumer = issueConsumer;
	}
	
	public void checkFilePathAndFragment(String completeLink, String targetFilePath, String fragment,
			IDocument document, IFile currentFile, int lineNumber, int offset, int endOffset) throws CoreException {
		
		String path = targetFilePath;
		
		// in case of an existing fragment, the path may be missing -> we assume the path of the current file
		if ((targetFilePath == null || targetFilePath.isBlank())
				&& fragment != null && !fragment.isBlank()) {
			path = currentFile.getLocation().toString();
		}
		
		IPath resourceRelativePath = new Path(path);
		
		boolean fileExists = checkFileExists(resourceRelativePath, currentFile, lineNumber, offset, endOffset);
		
		// check fragment if file exists
		if (fileExists && fragment != null) {
			
			// adapt positions to the fragment only
			int indexOfHashTag = completeLink.indexOf('#');
			offset += indexOfHashTag;
			endOffset = offset + fragment.length() + 1;
			lineNumber = DocumentUtils.getLineNumberForOffset(document, offset);
			
			
			if (resourceRelativePath.equals(currentFile.getLocation())) {
				// are we looking for sections in current Markdown file
				
				checkSectionAnchorExists(fragment, document, currentFile, lineNumber, offset, endOffset);
				
			} else if (FileUtils.FILE_EXTENSION_MARKDOWN.equalsIgnoreCase(resourceRelativePath.getFileExtension())) {
				// we're looking for sections in another Markdown file
				
				checkSectionAnchorExists(resourceRelativePath, fragment, currentFile, lineNumber, offset, endOffset);
				
			} else {
				// we're looking for targets in the file, e.g. method or a field in a Java file
				
				checkAnchorTargetExists(resourceRelativePath, fragment, currentFile, lineNumber, offset, endOffset);
			}
		}
	}
	
	private boolean checkFileExists(IPath resourceRelativePath, IFile currentFile, int lineNumber, int offset, int endOffset) throws CoreException {
		// try resolving the file
		IPath absolutePath = FileUtils.toAbsolutePath(resourceRelativePath, currentFile);
		File targetFile = absolutePath.toFile();
		
		if (!targetFile.exists()) {
			// resolve WORKSPACE variable (if used in path) for error message
			String workspacePath = getWorkspacePathFromEnvVariable();
			String adaptedPath = targetFile.getAbsolutePath();
			if (workspacePath.length() > 0 && adaptedPath.startsWith(workspacePath)) {
				adaptedPath = adaptedPath.replace(workspacePath, "$WORKSPACE");
			}
			
			issueConsumer.reportValidationResult(currentFile,
					IssueTypes.MARKDOWN_ISSUE,
					IMarker.SEVERITY_ERROR,
					String.format("The referenced file or directory '%s' does not exist. Target path: %s", resourceRelativePath.toString(), adaptedPath),
					lineNumber,
					offset,
					endOffset);
			return false;
		}
		
		if (targetFile.isFile()) {
			if (resourceRelativePath.toString().endsWith("/")) {
				issueConsumer.reportValidationResult(currentFile,
						IssueTypes.MARKDOWN_ISSUE,
						IMarker.SEVERITY_ERROR,
						String.format("The file path '%s' ends with a '/' which usually indicates a directory, not a file. Please remove the trailing '/' if you mean a file.", resourceRelativePath.toString()),
						lineNumber,
						offset,
						endOffset);
				return false;
			}
		} else if (targetFile.isDirectory()) {
			if (!resourceRelativePath.toString().endsWith("/")) {
				issueConsumer.reportValidationResult(currentFile,
						IssueTypes.MARKDOWN_ISSUE,
						IMarker.SEVERITY_WARNING,
						String.format("The given path '%s' is a directory, not a file. Please add a trailing '/' if you really mean a directory.",
								resourceRelativePath.toString()),
						lineNumber,
						offset,
						endOffset);
				return false;
			}
		}
		return true;
	}
	
	private void checkSectionAnchorExists(String sectionAnchor, IDocument currentDocument, IFile currentFile, int lineNumber, int offset, int endOffset) throws CoreException {
		String markdownFileContent = currentDocument.get();
		
		checkSectionAnchorExists(sectionAnchor, markdownFileContent, currentFile, lineNumber, offset, endOffset);
	}
	
	private void checkSectionAnchorExists(IPath targetFileWithAnchor, String sectionAnchor, IFile currentFile, int lineNumber, int offset, int endOffset) throws CoreException {
		IPath absolutePath = FileUtils.toAbsolutePath(targetFileWithAnchor, currentFile);
		File file = absolutePath.toFile();
		String mdFileContent = readTextFromFile(file);
		
		if (mdFileContent != null) {
			checkSectionAnchorExists(sectionAnchor, mdFileContent, currentFile, lineNumber, offset, endOffset);
		}
	}
	
	private void checkSectionAnchorExists(String sectionAnchor, String markdownFileContent, IFile currentFile, int lineNumber, int offset, int endOffset) throws CoreException {
		Set<String> anchors = MarkdownParsingTools.findValidSectionAnchorsInMarkdownCode(markdownFileContent);
		if (anchors.contains(sectionAnchor)) {
			// we found the target, no need to report issues
			return;
		}
		
		// we didn't find any (valid) target anchor ==> report an issue
		issueConsumer.reportValidationResult(currentFile,
				IssueTypes.MARKDOWN_ISSUE,
				IMarker.SEVERITY_ERROR,
				String.format("There is no section with the given anchor '%s' in the Markdown document '%s' or the anchor is invalid.", sectionAnchor, currentFile.getLocation().toString()),
				lineNumber,
				offset,
				endOffset);
	}
	
	private void checkAnchorTargetExists(IPath targetFilePath, String anchor, IFile currentFile, int lineNumber, int offset, int endOffset) {
		IPath absolutePath = FileUtils.toAbsolutePath(targetFilePath, currentFile);
		File targetFile = absolutePath.toFile();
		 
		if (!targetFile.exists() || !targetFile.isFile()) {
			// TODO log a warning?
			return;
		}
		
		IFile[] filesFound = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(targetFile.toURI());
		if (filesFound.length != 1) {
			// TODO log a warning?
			return;
		}
		
		IFile targetIFile = filesFound[0];
		Optional<IAnchorResolver> resolver = resolvers.stream()
				.filter(r -> r.isResponsibleFor(targetIFile))
				.findFirst();
	
		if (resolver.isEmpty()) {
			FluentCore.log(IStatus.WARNING, 
					String.format("Cannot check anchor \"%s\" for file with extension \"%s\". No anchor resolver found for that file extension.",
							anchor, targetIFile.getFileExtension()));
			return;
		}
		
		boolean anchorTargetExists = resolver.get().doesAnchorTargetExist(targetIFile, anchor);
		if (!anchorTargetExists) {
			issueConsumer.reportValidationResult(currentFile,
					IssueTypes.MARKDOWN_ISSUE,
					IMarker.SEVERITY_WARNING,
					String.format("There is no target corresponding to the given anchor '%s' in the file '%s'.",
							anchor, targetIFile.getLocation().toString()),
					lineNumber,
					offset,
					endOffset);
		}
	}
	
	private String readTextFromFile(File file) {
		try {
			return FileUtils.readTextFromFile(file);
		} catch (Exception e) {
			FluentCore.log(IStatus.WARNING, String.format("Could not read Markdown file '%s'", file.getAbsolutePath()), e);
			return null;
		}
	}
	
	private String getWorkspacePathFromEnvVariable() {
		if (this.workspacePath == null) {
			try {
				this.workspacePath = System.getenv("WORKSPACE");
			}
			catch (SecurityException e) {
				// do nothing
			}
			
			// avoid reading a missing or not accessible environment variable again and again
			if (this.workspacePath == null) {
				this.workspacePath = "";
			}
		}
		return this.workspacePath; 
	}

}
