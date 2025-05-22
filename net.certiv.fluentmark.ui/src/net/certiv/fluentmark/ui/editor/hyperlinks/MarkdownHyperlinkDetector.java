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

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetectorExtension;

import net.certiv.fluentmark.core.util.FileUtils;
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
			// Since the default hyper link detector does not correctly detect URLs in Markdown files,
			// we need to replace it with our own detector and create our custom URLHyperlinks.
			// See FluentUI.start()
			return new IHyperlink[] { new FluentUrlHyperlink(linkTargetRegion, linkTarget) };
			//return null;
		}
		
		IFile currentFile = DocumentUtils.findFileFor(document);
		if (currentFile == null) {
			return null;
		}
		
		String[] parts = linkTarget.split("#");
		
		String targetFilePath = parts[0];
		String fragment = null;
		if (parts.length == 2) {
			fragment = parts[1];
		}
		
		IFile targetFile = null;
		
		// do we have an anchor within our opened Markdown file?
		if (fragment != null && !fragment.isBlank() && (targetFilePath == null || targetFilePath.isBlank())) {
			targetFile = currentFile;
			
			return new IHyperlink[] { new MarkdownFileHyperlink(targetFile, linkTargetRegion, fragment) };
		}
		
		if (targetFilePath == null || targetFilePath.isBlank()) {
			return null;
		}
		
		IPath absolutePath = FileUtils.resolveToAbsoluteResourcePath(targetFilePath, currentFile);
		
		try {
			targetFile = FileUtils.resolveToWorkspaceFile(absolutePath);
		} catch (Exception e) {
			FluentUI.log(IStatus.WARNING, "Could not find file " + absolutePath, e);
			
			// do not return, try to find IFileStore instead
		}
		
		if (targetFile != null) {
			if (FileUtils.isMarkdownFile(targetFile)) {
				return new IHyperlink[] { new MarkdownFileHyperlink(targetFile, linkTargetRegion, fragment) };
			}
			
			if (fragment != null && FileUtils.isJavaFile(targetFile)) {
				return new IHyperlink[] { new JavaMemberHyperlink(targetFile, linkTargetRegion, fragment) };
			}
			return new IHyperlink[] { new FileHyperlink(targetFile, linkTargetRegion) };
		}
		
		IFileStore fileOutsideWorkspace = FileUtils.resolveToNonWorkspaceFile(absolutePath); 
		if (!FileUtils.isExistingFile(fileOutsideWorkspace)) {
			return null;
		}
		
		return new IHyperlink[] { new FileHyperlink(fileOutsideWorkspace, linkTargetRegion) };
	}

}
