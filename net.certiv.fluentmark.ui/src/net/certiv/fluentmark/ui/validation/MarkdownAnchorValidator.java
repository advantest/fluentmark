/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.markdown.MarkdownParsingTools;
import net.certiv.fluentmark.core.markdown.RegexMatch;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.editor.text.partitioning.MarkdownPartioningTools;
import net.certiv.fluentmark.ui.markers.ITypedRegionMarkerCalculator;
import net.certiv.fluentmark.ui.markers.MarkerCalculator;

public class MarkdownAnchorValidator implements ITypedRegionMarkerCalculator {
	
	@Override
	public void setupDocumentPartitioner(IDocument document, IFile file) {
		MarkdownPartioningTools.getTools().setupDocumentPartitioner(document);
	}
	
	@Override
	public ITypedRegion[] computePartitioning(IDocument document, IFile file) throws BadLocationException {
		return MarkdownPartioningTools.getTools().computePartitioning(document);
	}

	@Override
	public boolean isValidatorFor(IFile file) {
		return FileUtils.isMarkdownFile(file);
	}

	@Override
	public boolean isValidatorFor(ITypedRegion region, IFile file) {
		return IDocument.DEFAULT_CONTENT_TYPE.equals(region.getType());
	}

	@Override
	public void validateRegion(ITypedRegion region, IDocument document, IFile file) throws CoreException {
		String regionContent;
		try {
			regionContent = document.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			return;
		}
		
		// collect all anchor declarations per anchor id
		Map<String,List<RegexMatch>> anchors = new HashMap<>();
		MarkdownParsingTools.findHeadingAnchorIds(regionContent)
			.forEach(match -> {
				List<RegexMatch> matches = anchors.get(match.matchedText);
				if (matches == null) {
					matches = new ArrayList<>(2);
					anchors.put(match.matchedText, matches);
				}
				matches.add(match);
			});
		
		// go through all anchor declarations and check they use only allowed characters
		anchors.values().stream()
			.flatMap(matchForAnAnchor -> matchForAnAnchor.stream())
			.filter(anchorIdMatch -> !MarkdownParsingTools.isValidAnchorIdentifier(anchorIdMatch.matchedText))
			.forEach(anchorIdMatch -> {
				int offset = region.getOffset() + anchorIdMatch.startIndex - 1;
				int endOffset = region.getOffset() + anchorIdMatch.endIndex;
				int lineNumber = getLineForOffset(document, offset);
				
				try {
					MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
							"The anchor identifier \"" + anchorIdMatch.matchedText + "\" is invalid."
							+ " It has to contain at least one character, must start with a letter,"
							+ " and is allowed to contain any number of the following characters in the remainder:"
							+ " letters ([A-Za-z]), digits ([0-9]), hyphens (\"-\"), underscores (\"_\"), colons (\":\"), and periods (\".\").",
							lineNumber,
							offset,
							endOffset);
				} catch (CoreException e) {
					FluentUI.log(IStatus.WARNING, "Could not create validation marker.", e);
				}
			});
		
		// go through all non-unique anchor declarations and create markers
		anchors.keySet().stream()
			.filter(anchor -> anchors.get(anchor).size() > 1)
			.forEach(anchor -> {
				String lines = anchors.get(anchor).stream()
					.map(anchorDeclaration -> getLineForOffset(document,
							region.getOffset() + anchorDeclaration.startIndex))
					.map(line -> String.valueOf(line))
					.reduce("", (text1, text2) -> text1.isBlank() ? text2 : text1 + ", " + text2);
				
				for (RegexMatch anchorDeclaration : anchors.get(anchor)) {
					int offset = region.getOffset() + anchorDeclaration.startIndex - 1;
					int endOffset = region.getOffset() + anchorDeclaration.endIndex;
					int lineNumber = getLineForOffset(document, offset);
					
					try {
						MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
								"The anchor identifier \"" + anchor + "\" is not unique."
								+ " The same identifier is used in the following lines: " + lines,
								lineNumber,
								offset,
								endOffset);
					} catch (CoreException e) {
						FluentUI.log(IStatus.WARNING, "Could not create validation marker.", e);
					}
				}
				
			});
	}
	
	protected int getLineForOffset(IDocument document, int offset) {
		try {
			return document.getLineOfOffset(offset) + 1;
		} catch (BadLocationException e) {
			return -1;
		}
	}

}
