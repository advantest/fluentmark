/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.markdown.parsing.MarkdownParsingTools;
import net.certiv.fluentmark.core.markdown.parsing.RegexMatch;
import net.certiv.fluentmark.core.markdown.partitions.MarkdownPartitions;
import net.certiv.fluentmark.core.util.DocumentUtils;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.core.validation.ITypedRegionValidator;
import net.certiv.fluentmark.core.validation.IValidationResultConsumer;
import net.certiv.fluentmark.core.validation.IssueTypes;

public class MarkdownAnchorValidator implements ITypedRegionValidator {
	
	private IValidationResultConsumer issueConsumer;
	
	@Override
	public String getRequiredPartitioning(IFile file) {
		return MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING;
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
	public void setValidationResultConsumer(IValidationResultConsumer issueConsumer) {
		this.issueConsumer = issueConsumer;
	}

	@Override
	public void validateRegion(ITypedRegion region, IDocument document, IFile file) {
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
				int lineNumber = DocumentUtils.getLineNumberForOffset(document, offset);
				
				issueConsumer.reportValidationResult(file,
						IssueTypes.MARKDOWN_ISSUE,
						IMarker.SEVERITY_ERROR,
						"The anchor identifier \"" + anchorIdMatch.matchedText + "\" is invalid."
						// see MarkdownParsingTools.REGEX_VALID_ANCHOR_ID
						+ " It has to contain at least one character, must start with a letter,"
						+ " and is allowed to contain any number of the following characters in the remainder:"
						+ " letters ([A-Za-z]), digits ([0-9]), hyphens (\"-\"), underscores (\"_\"), colons (\":\"), and periods (\".\").",
						lineNumber,
						offset,
						endOffset);
			});
		
		// go through all non-unique anchor declarations and create markers
		anchors.keySet().stream()
			.filter(anchor -> anchors.get(anchor).size() > 1)
			.forEach(anchor -> {
				String lines = anchors.get(anchor).stream()
					.map(anchorDeclaration -> DocumentUtils.getLineNumberForOffset(document,
							region.getOffset() + anchorDeclaration.startIndex))
					.map(line -> String.valueOf(line))
					.reduce("", (text1, text2) -> text1.isBlank() ? text2 : text1 + ", " + text2);
				
				for (RegexMatch anchorDeclaration : anchors.get(anchor)) {
					int offset = region.getOffset() + anchorDeclaration.startIndex - 1;
					int endOffset = region.getOffset() + anchorDeclaration.endIndex;
					int lineNumber = DocumentUtils.getLineNumberForOffset(document, offset);
					
					issueConsumer.reportValidationResult(file,
							IssueTypes.MARKDOWN_ISSUE,
							IMarker.SEVERITY_ERROR,
							"The anchor identifier \"" + anchor + "\" is not unique."
							+ " The same identifier is used in the following lines: " + lines,
							lineNumber,
							offset,
							endOffset);
				}
				
			});
	}

}
