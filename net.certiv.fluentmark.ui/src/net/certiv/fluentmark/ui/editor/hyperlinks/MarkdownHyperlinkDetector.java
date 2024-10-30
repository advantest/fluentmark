/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.hyperlinks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetectorExtension;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.util.DocumentUtils;

public class MarkdownHyperlinkDetector extends AbstractHyperlinkDetector
		implements IHyperlinkDetector, IHyperlinkDetectorExtension {
	
	private static final String REGEX_LINK_PREFIX = "(!){0,1}\\[.*?\\]\\(";
	private static final String REGEX_LINK = REGEX_LINK_PREFIX + ".*?\\)";
	
	private final Pattern LINK_PATTERN = Pattern.compile(REGEX_LINK);
	private final Pattern LINK_PREFIX_PATTERN = Pattern.compile(REGEX_LINK_PREFIX);

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}
		
		IDocument document = textViewer.getDocument();
		
		if (document == null ) {
			return null;
		}
		
		int offset = region.getOffset();
		
		IRegion lineRegion;
		String line;
		try {
			lineRegion = document.getLineInformationOfOffset(offset);
			line = document.get(lineRegion.getOffset(), lineRegion.getLength());
		} catch (BadLocationException ex) {
			return null;
		}
		
		int offsetInLine = offset - lineRegion.getOffset();
		
		Matcher linkMatcher = LINK_PATTERN.matcher(line);
		int startIndex = 0;
		while (linkMatcher.find(startIndex) && linkMatcher.end() < offsetInLine) {
			startIndex = linkMatcher.end();
		}
		
		if (!linkMatcher.hasMatch()) {
			return null;
		}
		
		if (linkMatcher.start() > offsetInLine || linkMatcher.end() <= offsetInLine) {
			return null;
		}
		
		String linkStatement = linkMatcher.group();
		Matcher prefixMatcher = LINK_PREFIX_PATTERN.matcher(linkStatement);
		prefixMatcher.find();
		int linkTargetStartIndex = prefixMatcher.end();
		
		String linkTarget = linkStatement.substring(linkTargetStartIndex, linkStatement.length() - 1);
		
		IRegion linkTargetRegion= new Region(lineRegion.getOffset() + linkMatcher.start() + linkTargetStartIndex, linkTarget.length());
		
		if (linkTarget.startsWith("https://")) {
			// this case is handled in URLHyperlinkDetector
			return null;
		}
		
		IFile currentFile = DocumentUtils.findFileFor(document);
		if (currentFile == null) {
			return null;
		}
		
		String[] parts = linkTarget.split("#");
		
		
		// TODO also handle fragment, i.e. try navigating to anchors in case of markdown files
		String targetFilePath = parts[0];
		String fragment = null;
		if (parts.length == 2) {
			fragment = parts[1];
		}
		
		if (targetFilePath == null || targetFilePath.isBlank()) {
			return null;
		}
		
		IPath resourceRelativePath = new Path(targetFilePath);
		IPath absolutePath = toAbsolutePath(resourceRelativePath, currentFile);
		
		IFile targetFile = null;
		try {
			targetFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(absolutePath);
		} catch (Exception e) {
			FluentUI.log(IStatus.ERROR, "Could not find file " + absolutePath, e);
		}
		
		if (targetFile != null) {
			return new IHyperlink[] {new FileHyperlink(targetFile, linkTargetRegion)};
		}
		
		IFileStore fileOutsideWorkspace = EFS.getLocalFileSystem().getStore(absolutePath);
		if (fileOutsideWorkspace == null
				|| !fileOutsideWorkspace.fetchInfo().exists()
				|| fileOutsideWorkspace.fetchInfo().isDirectory()) {
			return null;
		}
		
		return new IHyperlink[] {new FileHyperlink(fileOutsideWorkspace, linkTargetRegion)};
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

}
