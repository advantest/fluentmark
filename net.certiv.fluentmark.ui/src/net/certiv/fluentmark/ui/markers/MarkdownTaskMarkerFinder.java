/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.markers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.markdown.partitions.MarkdownPartitioner;
import net.certiv.fluentmark.core.util.DocumentUtils;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.core.validation.ITypedRegionValidator;
import net.certiv.fluentmark.core.validation.IValidationResultConsumer;

public class MarkdownTaskMarkerFinder implements ITypedRegionValidator {
	
	private static final String TODO_FIXME_REGEX = "(?<keyword>TODO|FIXME):?[ \\t](?<message>.*?(?=-?-->)|.*(?!-->))";
	private static final String REGEX_CAPTURING_GROUP_KEYWORD = "keyword";
	private static final String REGEX_CAPTURING_GROUP_MESSAGE = "message";
	
	private Pattern pattern;
	
	private IValidationResultConsumer issueConsumer;
	
	public MarkdownTaskMarkerFinder() {
		pattern = Pattern.compile(TODO_FIXME_REGEX, Pattern.CASE_INSENSITIVE);
	}
	
	@Override
	public String getRequiredPartitioning(IFile file) {
		return MarkdownPartitioner.FLUENT_MARKDOWN_PARTITIONING;
	}

	@Override
	public boolean isValidatorFor(IFile file) {
		return FileUtils.isMarkdownFile(file);
	}

	@Override
	public boolean isValidatorFor(ITypedRegion region, IFile file) {
		return MarkdownPartitioner.COMMENT.equals(region.getType())
				&& isValidatorFor(file);
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
		
		Matcher patternMatcher = this.pattern.matcher(regionContent);
		boolean found = patternMatcher.find();
		int startIndex = -1;
		int endIndex = -1;
		
		while (found) {
			startIndex = patternMatcher.start();
			endIndex = patternMatcher.end();
			String todoOrFixmeText = patternMatcher.group(REGEX_CAPTURING_GROUP_KEYWORD);
			String message = patternMatcher.group(REGEX_CAPTURING_GROUP_MESSAGE).trim();
			
			int offset = region.getOffset() + startIndex;
			int lineNumber = DocumentUtils.getLineNumberForOffset(document, offset);
			int endOffset = region.getOffset() + endIndex;
			int priority = "FIXME".equalsIgnoreCase(todoOrFixmeText) ? IMarker.PRIORITY_HIGH : IMarker.PRIORITY_NORMAL;
			
			issueConsumer.reportValidationResult(file,
					TaskTypes.MARKDOWN_TASK,
					priority,
					todoOrFixmeText + " " + message,
					lineNumber,
					offset,
					endOffset);
			
			found = patternMatcher.find();
		}
	}

}
