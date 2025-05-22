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

import java.util.Optional;
import java.util.regex.Matcher;

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

import net.certiv.fluentmark.core.markdown.MarkdownParsingTools;
import net.certiv.fluentmark.core.markdown.RegexMatch;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.util.DocumentUtils;

public class MarkdownHyperlinkDetector extends AbstractHyperlinkDetector
		implements IHyperlinkDetector, IHyperlinkDetectorExtension {
	
	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}
		
		IDocument document = textViewer.getDocument();
		
		if (document == null ) {
			return null;
		}
		
		// the given region has length 0, so we have to use the offset only
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
		
		// check for regular links and images
		Optional <RegexMatch> linkMatch = MarkdownParsingTools.findLinksAndImages(line)
			// find the link that belongs to the given offset
			.filter(match -> match.startIndex < offsetInLine && match.endIndex > offsetInLine)
			.findFirst();
		
		if (linkMatch.isPresent()) {
			RegexMatch targetMatch = linkMatch.get().subMatches.get(MarkdownParsingTools.REGEX_LINK_CAPTURING_GROUP_TARGET);
			if (targetMatch != null) {
				String linkTarget = targetMatch.matchedText;
				
				IRegion linkTargetRegion= new Region(lineRegion.getOffset() + targetMatch.startIndex, linkTarget.length());
				
				return wrap(createHyperLink(linkTarget, linkTargetRegion, document));
			}
		}
		
		// TODO check for link reference definitions
		
		// TODO check for reference links
		
		return null;
	}
	
	private IHyperlink createHyperLink(String linkTarget, IRegion linkTargetRegion, IDocument currentDocument) {
		if (linkTarget.startsWith("https://")) {
			// Since the default hyper link detector does not correctly detect URLs in Markdown files,
			// we need to replace it with our own detector and create our custom URLHyperlinks.
			// See FluentUI.start()
			return new FluentUrlHyperlink(linkTargetRegion, linkTarget);
		}
		
		IFile currentFile = DocumentUtils.findFileFor(currentDocument);
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
			
			return new MarkdownFileHyperlink(targetFile, linkTargetRegion, fragment);
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
				return new MarkdownFileHyperlink(targetFile, linkTargetRegion, fragment);
			}
			
			if (fragment != null && FileUtils.isJavaFile(targetFile)) {
				return new JavaMemberHyperlink(targetFile, linkTargetRegion, fragment);
			}
			return new FileHyperlink(targetFile, linkTargetRegion);
		}
		
		IFileStore fileOutsideWorkspace = FileUtils.resolveToNonWorkspaceFile(absolutePath); 
		if (!FileUtils.isExistingFile(fileOutsideWorkspace)) {
			return null;
		}
		
		return new FileHyperlink(fileOutsideWorkspace, linkTargetRegion);
	}
	
	private IHyperlink[] wrap(IHyperlink hyperLink) {
		return new IHyperlink[] { hyperLink };
	}

}
