/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;

public class FilePathValidator {
	
	// pattern for headings with anchors, e.g. #### 1.2 Section {#topic-x}
	private static final String REGEX_HEADING_ANCHOR_PREFIX = "#{1,6}( |\\t)+.*\\{#";
	private static final String REGEX_HEADING_WITH_ANCHOR = REGEX_HEADING_ANCHOR_PREFIX + "\\S+\\}";
	
	private final Pattern HEADING_WITH_ANCHOR_PREFIX_PATTERN;
	private final Pattern HEADING_WITH_ANCHOR_PATTERN;
	
	private JavaCodeMemberResolver javaMemberResolver;
	
	private String workspacePath = null;
	
	public FilePathValidator() {
		HEADING_WITH_ANCHOR_PREFIX_PATTERN = Pattern.compile(REGEX_HEADING_ANCHOR_PREFIX);
		HEADING_WITH_ANCHOR_PATTERN = Pattern.compile(REGEX_HEADING_WITH_ANCHOR);
		
		javaMemberResolver = new JavaCodeMemberResolver();
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
		
		IMarker problemMarker = checkFileExists(resourceRelativePath, currentFile, lineNumber, offset, endOffset);
		
		// check fragment if file exists
		if (problemMarker == null && fragment != null) {
			
			// adapt positions to the fragment only
			int indexOfHashTag = completeLink.indexOf('#');
			offset += indexOfHashTag;
			endOffset = offset + fragment.length() + 1;
			lineNumber = getLineForOffset(document, offset);
			
			
			if (resourceRelativePath.equals(currentFile.getLocation())) {
				// are we looking for sections in current Markdown file
				
				checkSectionAnchorExists(fragment, document, currentFile, lineNumber, offset, endOffset);
				
			} else if (FileUtils.FILE_EXTENSION_MARKDOWN.equalsIgnoreCase(resourceRelativePath.getFileExtension())) {
				// we're looking for sections in another Markdown file
				
				checkSectionAnchorExists(resourceRelativePath, fragment, currentFile, lineNumber, offset, endOffset);
				
			} else if (FileUtils.FILE_EXTENSION_JAVA.equalsIgnoreCase(resourceRelativePath.getFileExtension())) {
				// we're looking for members in a Java file, e.g. a method or a field
				
				checkJavaMemberExists(resourceRelativePath, fragment, currentFile, lineNumber, offset, endOffset);
			}
		}
	}
	
	private IMarker checkFileExists(IPath resourceRelativePath, IResource resource, int lineNumber, int offset, int endOffset) throws CoreException {
		// try resolving the file
		IPath absolutePath = toAbsolutePath(resourceRelativePath, resource);
		File targetFile = absolutePath.toFile();
		
		if (!targetFile.exists()) {
			String workspacePath = getWorkspacePathFromEnvVariable();
			String adaptedPath = targetFile.getAbsolutePath();
			if (workspacePath.length() > 0 && adaptedPath.startsWith(workspacePath)) {
				adaptedPath = adaptedPath.replace(workspacePath, "$WORKSPACE");
			}
			
			return MarkerCalculator.createMarkdownMarker(resource, IMarker.SEVERITY_ERROR,
					String.format("The referenced file '%s' does not exist. Target path: %s", resourceRelativePath.toString(), adaptedPath),
					lineNumber,
					offset,
					endOffset);
		}
		
		if (!targetFile.isFile()) {
			return MarkerCalculator.createMarkdownMarker(resource, IMarker.SEVERITY_WARNING,
					String.format("The referenced file '%s' is actually not a file (it seems to be a directory). Target path: %s",
							resourceRelativePath.toString(), targetFile.getAbsolutePath()),
					lineNumber,
					offset,
					endOffset);
		}
		
		return null;
	}
	
	private IPath toAbsolutePath(IPath resourceRelativePath, IResource currentResource) {
		IPath absolutePath;
		if (resourceRelativePath.equals(currentResource.getLocation())) {
			absolutePath = currentResource.getLocation();
		} else {
			absolutePath = currentResource.getLocation().removeLastSegments(1).append(resourceRelativePath);
		}
		return absolutePath;
	}
	
	private IMarker checkSectionAnchorExists(String sectionAnchor, IDocument currentDocument, IResource currentResource, int lineNumber, int offset, int endOffset) throws CoreException {
		String markdownFileContent = currentDocument.get();
		
		return checkSectionAnchorExists(sectionAnchor, markdownFileContent, currentResource, lineNumber, offset, endOffset);
	}
	
	private IMarker checkSectionAnchorExists(IPath targetFileWithAnchor, String sectionAnchor, IResource currentResource, int lineNumber, int offset, int endOffset) throws CoreException {
		IPath absolutePath = toAbsolutePath(targetFileWithAnchor, currentResource);
		File file = absolutePath.toFile();
        String mdFileContent = readTextFromFile(file);
        
        if (mdFileContent != null) {
        	return checkSectionAnchorExists(sectionAnchor, mdFileContent, currentResource, lineNumber, offset, endOffset);
        }
		
		return null;
	}
	
	private IMarker checkSectionAnchorExists(String sectionAnchor, String markdownFileContent, IResource currentResource, int lineNumber, int offset, int endOffset) throws CoreException {
		Matcher headingMatcher = HEADING_WITH_ANCHOR_PATTERN.matcher(markdownFileContent);
		boolean found = headingMatcher.find();
		
		// go through all the headings in the document and check their anchors
		while (found) {
			String currentHeadingMatch = headingMatcher.group();
			
			Matcher prefixMatcher = HEADING_WITH_ANCHOR_PREFIX_PATTERN.matcher(currentHeadingMatch);
			boolean foundPrefix = prefixMatcher.find();
			Assert.isTrue(foundPrefix);
			int indexBeginAnchor = prefixMatcher.end();
			
			// cut off heading and "{#" and "}" parts to extract the anchor in between
			String currentAnchor = currentHeadingMatch.substring(indexBeginAnchor, currentHeadingMatch.length() - 1);
			
			if (sectionAnchor.equals(currentAnchor)) {
				// we found the target, no need to create markers
				return null;
			}
			
			found = headingMatcher.find();
		}
		
		// we didn't find any target anchor ==> create a marker
		return MarkerCalculator.createMarkdownMarker(currentResource, IMarker.SEVERITY_WARNING,
				String.format("There is no section with the given anchor '%s' in this Markdown document '%s'.", sectionAnchor, currentResource.getLocation().toString()),
				lineNumber,
				offset,
				endOffset);
	}
	
	private String readTextFromFile(File file) {
		try {
			return FileUtils.readTextFromFile(file);
		} catch (Exception e) {
			FluentUI.log(IStatus.WARNING, String.format("Could not read Markdown file '%s'", file.getAbsolutePath()), e);
			return null;
		}
	}
	
	protected int getLineForOffset(IDocument document, int offset) {
		try {
			return document.getLineOfOffset(offset) + 1;
		} catch (BadLocationException e) {
			return -1;
		}
	}
	
	private IMarker checkJavaMemberExists(IPath targetJavaFile, String memberReference, IResource currentResource, int lineNumber, int offset, int endOffset) throws CoreException {
		
		IPath absolutePath = toAbsolutePath(targetJavaFile, currentResource);
		File targetFile = absolutePath.toFile();
		 
		if (!targetFile.exists() || !targetFile.isFile()) {
			return null;
		}
		
		IFile[] filesFound = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(targetFile.toURI());
		if (filesFound.length != 1) {
			return null;
		}
		
		IMember member = this.javaMemberResolver.findJavaMember(filesFound[0], memberReference);
		
		if (member == null || !member.exists()) {
			// we didn't find the referenced class member ==> create a problem marker
			return MarkerCalculator.createMarkdownMarker(currentResource, IMarker.SEVERITY_WARNING,
					String.format("There is no class member (field or method) corresponding to the given anchor '%s' in the Java file '%s'.", memberReference, absolutePath.toString()),
					lineNumber,
					offset,
					endOffset);
		}
		
		return null;
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
