/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.hyperlinks;

import java.util.Optional;

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

import net.certiv.fluentmark.core.markdown.parsing.MarkdownParsingTools;
import net.certiv.fluentmark.core.markdown.parsing.RegexMatch;
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
		
		IHyperlink hyperlinkInLinkOrImage = detectHyperlinkInLinkOrImage(document, offset);
		if (hyperlinkInLinkOrImage != null) {
			return wrap(hyperlinkInLinkOrImage);
		}
		
		IHyperlink hyperlinkInLinkRefDefinition = detectHyperlinkInLinkReferenceDefinition(document, offset);
		if (hyperlinkInLinkRefDefinition != null) {
			return wrap(hyperlinkInLinkRefDefinition);
		}
		
		IHyperlink hyperlinkInReferenceLink = detectHyperlinkInReferenceLink(document, offset);
		if (hyperlinkInReferenceLink != null) {
			return wrap(hyperlinkInReferenceLink);
		}
		
		return null;
	}
	
	private IHyperlink detectHyperlinkInLinkOrImage(IDocument currentDocument, int offset) {
		IRegion lineRegion;
		String line;
		try {
			lineRegion = currentDocument.getLineInformationOfOffset(offset);
			line = currentDocument.get(lineRegion.getOffset(), lineRegion.getLength());
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
			RegexMatch targetMatch = linkMatch.get().subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_TARGET);
			if (targetMatch != null) {
				String linkTarget = targetMatch.matchedText;
				
				IRegion linkTargetRegion = new Region(lineRegion.getOffset() + targetMatch.startIndex, linkTarget.length());
				
				return createFileOrWebHyperlink(linkTarget, linkTargetRegion, currentDocument);
			}
		}
		
		return null;
	}
	
	private IHyperlink detectHyperlinkInReferenceLink(IDocument currentDocument, int offset) {
		IRegion lineRegion;
		String line;
		try {
			lineRegion = currentDocument.getLineInformationOfOffset(offset);
			line = currentDocument.get(lineRegion.getOffset(), lineRegion.getLength());
		} catch (BadLocationException ex) {
			return null;
		}
		
		int offsetInLine = offset - lineRegion.getOffset();
		
		// check for full and collapsed reference links, i.e.
		// * full reference link:      [Markdown specification][CommonMark]
		// * collapsed reference link: [CommonMark][]
		Optional <RegexMatch> linkMatch = MarkdownParsingTools.findFullAndCollapsedReferenceLinks(line)
			// find the link that belongs to the given offset
			.filter(match -> match.startIndex < offsetInLine && match.endIndex > offsetInLine)
			.findFirst();
		
		if (linkMatch.isPresent()) {
			RegexMatch labelMatch = linkMatch.get().subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_LABEL);
			RegexMatch targetMatch = linkMatch.get().subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_TARGET);
			
			String linkTarget = null;
			IRegion linkTargetRegion = null;
			if (targetMatch != null && !targetMatch.matchedText.isBlank()) {
				// we have a full reference link
				linkTarget = targetMatch.matchedText;
				linkTargetRegion = new Region(lineRegion.getOffset() + targetMatch.startIndex, linkTarget.length());
			} else if (labelMatch != null && !labelMatch.matchedText.isBlank() && targetMatch != null && targetMatch.matchedText.isBlank()) {
				// we have a collapsed reference link => label = target
				linkTarget = labelMatch.matchedText;
				linkTargetRegion = new Region(lineRegion.getOffset() + labelMatch.startIndex, linkTarget.length());
			}
			
			if (linkTarget != null && !linkTarget.isBlank()) {
				return createReferenceHyperlink(linkTarget, linkTargetRegion, currentDocument);
			}
		}
		
		// check for shortcut reference links, i.e.
		// * shortcut reference link:  [CommonMark]
		linkMatch = MarkdownParsingTools.findShortcutReferenceLinks(line)
			// find the link that belongs to the given offset
			.filter(match -> match.startIndex < offsetInLine && match.endIndex > offsetInLine)
			.findFirst();
		
		if (linkMatch.isPresent()) {
			RegexMatch targetMatch = linkMatch.get().subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_TARGET);
			
			if (targetMatch != null && !targetMatch.matchedText.isBlank()) {
				String linkTarget = targetMatch.matchedText;
				
				IRegion linkTargetRegion = new Region(lineRegion.getOffset() + targetMatch.startIndex, linkTarget.length());
				
				return createReferenceHyperlink(linkTarget, linkTargetRegion, currentDocument);
			}
		}
		
		return null;
	}
	
	private IHyperlink detectHyperlinkInLinkReferenceDefinition(IDocument currentDocument, int offset) {
		// we extend the region to the preceding line since link reference definitions might span multiple lines and the target text might be in the second line
		// It might be something like the following (we ignore the title, since the target is in the first or second line):
		// [label]: https://www.plantuml.com "title"
		// or
		// [label]:
		//    https://www.plantuml.com
		//    "Title"
		IRegion multipleLinesRegion;
		String multipleLines;
		try {
			IRegion currentLineRegion = currentDocument.getLineInformationOfOffset(offset);
			if (currentLineRegion.getOffset() - 1 >= 0) {
				IRegion preceedingLineRegion = currentDocument.getLineInformationOfOffset(currentLineRegion.getOffset() - 1);
				// the line regions do not contain line delimiter lengths, thus, we have consider both lines' offsets
				int length = currentLineRegion.getOffset() - preceedingLineRegion.getOffset() + currentLineRegion.getLength();
				multipleLinesRegion = new Region(preceedingLineRegion.getOffset(), length);
				multipleLines = currentDocument.get(preceedingLineRegion.getOffset(), multipleLinesRegion.getLength());
			} else {
				// in case there is no preceding line, let's only consider the current line
				multipleLinesRegion = new Region(currentLineRegion.getOffset(), currentLineRegion.getLength());
				multipleLines = currentDocument.get(currentLineRegion.getOffset(), multipleLinesRegion.getLength());
			}
		} catch (BadLocationException ex) {
			return null;
		}
		
		int offsetInMultipleLines = offset - multipleLinesRegion.getOffset();
		
		Optional <RegexMatch> linkRefDefMatch = MarkdownParsingTools.findLinkReferenceDefinitions(multipleLines)
			// find the link reference definition that belongs to the given offset
			.filter(match -> match.startIndex < offsetInMultipleLines && match.endIndex > offsetInMultipleLines)
			.findFirst();
		
		if (linkRefDefMatch.isPresent()) {
			RegexMatch targetMatch = linkRefDefMatch.get().subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_TARGET);
			if (targetMatch != null) {
				String linkTarget = targetMatch.matchedText;
				
				IRegion linkTargetRegion= new Region(multipleLinesRegion.getOffset() + targetMatch.startIndex, linkTarget.length());
				
				return createFileOrWebHyperlink(linkTarget, linkTargetRegion, currentDocument);
			}
		}
		
		return null;
	}
	
	private IHyperlink createFileOrWebHyperlink(String linkTarget, IRegion linkTargetRegion, IDocument currentDocument) {
		if (linkTarget.startsWith("https://") || linkTarget.startsWith("HTTPS://")
				|| linkTarget.startsWith("http://") || linkTarget.startsWith("HTTP://")) {
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
			if (!targetFile.exists()) {
				return null;
			}
			
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
	
	private IHyperlink createReferenceHyperlink(String linkReferenceDefinitionName, IRegion linkTargetRegion, IDocument currentDocument) {
		IFile currentFile = DocumentUtils.findFileFor(currentDocument);
		if (currentFile == null) {
			return null;
		}
		
		if (MarkdownParsingTools.findLinkReferenceDefinition(currentDocument.get(), linkReferenceDefinitionName).isEmpty()) {
			return null;
		}
		
		return new MarkdownReferenceHyperlink(currentFile, linkTargetRegion, linkReferenceDefinitionName);
	}
	
	private IHyperlink[] wrap(IHyperlink hyperLink) {
		return new IHyperlink[] { hyperLink };
	}

}
