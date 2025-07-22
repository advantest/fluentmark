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
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.markdown.partitions.MarkdownPartitions;
import net.certiv.fluentmark.core.partitions.FluentPartitioningTools;
import net.certiv.fluentmark.core.plantuml.partitions.PlantUmlPartitions;
import net.certiv.fluentmark.core.util.DocumentUtils;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.core.validation.ITypedRegionValidator;
import net.certiv.fluentmark.core.validation.IValidationResultConsumer;

public class PlantUmlTaskMarkerFinder implements ITypedRegionValidator {
	
	private static final String TODO_FIXME_REGEX = "(?<keyword>TODO|FIXME):?[ \\t](?<message>.*?(?='\\/)|.*(?!'\\/))";
	private static final String REGEX_CAPTURING_GROUP_KEYWORD = "keyword";
	private static final String REGEX_CAPTURING_GROUP_MESSAGE = "message";
	
	private Pattern pattern;
	
	private IValidationResultConsumer issueConsumer;
	
	public PlantUmlTaskMarkerFinder() {
		pattern = Pattern.compile(TODO_FIXME_REGEX, Pattern.CASE_INSENSITIVE);
	}
	
	@Override
	public String getRequiredPartitioning(IFile file) {
		if (FileUtils.isPumlFile(file)) {
			return PlantUmlPartitions.FLUENT_PLANTUML_PARTITIONING;
		} else {
			return MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING;
		}
	}

	@Override
	public boolean isValidatorFor(IFile file) {
		return FileUtils.isPumlFile(file) || FileUtils.isMarkdownFile(file);
	}

	@Override
	public boolean isValidatorFor(ITypedRegion region, IFile file) {
		return (PlantUmlPartitions.COMMENT.equals(region.getType()) && FileUtils.isPumlFile(file))
				|| (MarkdownPartitions.UMLBLOCK.equals(region.getType()) && FileUtils.isMarkdownFile(file));
	}
	
	@Override
	public void setValidationResultConsumer(IValidationResultConsumer issueConsumer) {
		this.issueConsumer = issueConsumer;
	}

	@Override
	public void validateRegion(ITypedRegion region, IDocument document, IFile resource) {
		String regionContent;
		try {
			regionContent = document.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			return;
		}
		
		if (FileUtils.isMarkdownFile(resource)
				&& MarkdownPartitions.UMLBLOCK.equals(region.getType())) {
			IDocument codeBlockDocument = new Document(regionContent);
			IDocumentPartitioner partitioner = MarkdownPartitions.get().createDocumentPartitioner();
			FluentPartitioningTools.setupDocumentPartitioner(codeBlockDocument, partitioner, PlantUmlPartitions.FLUENT_PLANTUML_PARTITIONING);
			ITypedRegion[] inCodeBlockRegions = PlantUmlPartitions.get().computePartitions(codeBlockDocument);
			
			for (ITypedRegion inCodeBlockRegion: inCodeBlockRegions) {
				if (PlantUmlPartitions.COMMENT.equals(inCodeBlockRegion.getType())) {
					String inCodeBlockRegionContent;
					try {
						inCodeBlockRegionContent = codeBlockDocument.get(inCodeBlockRegion.getOffset(), inCodeBlockRegion.getLength());
					} catch (BadLocationException e) {
						continue;
					}
					doValidateRegion(inCodeBlockRegion, inCodeBlockRegionContent, document, resource, region.getOffset());
				}
			}
		} else {
			doValidateRegion(region, regionContent, document, resource, 0);
		}
	}
	
	private void doValidateRegion(ITypedRegion region, String regionContent, IDocument document, IFile file, int additionalOffset) {
		Matcher patternMatcher = this.pattern.matcher(regionContent);
		boolean found = patternMatcher.find();
		int startIndex = -1;
		int endIndex = -1;
		
		while (found) {
			startIndex = patternMatcher.start();
			endIndex = patternMatcher.end();
			String todoOrFixmeText = patternMatcher.group(REGEX_CAPTURING_GROUP_KEYWORD);
			String message = patternMatcher.group(REGEX_CAPTURING_GROUP_MESSAGE).trim();
			
			int offset = region.getOffset() + startIndex + additionalOffset;
			int lineNumber = DocumentUtils.getLineNumberForOffset(document, offset);
			int endOffset = region.getOffset() + endIndex + additionalOffset;
			int priority = "FIXME".equalsIgnoreCase(todoOrFixmeText) ? IMarker.PRIORITY_HIGH : IMarker.PRIORITY_NORMAL;
			
			issueConsumer.reportValidationResult(file,
					TaskTypes.PLANTUML_TASK,
					priority,
					todoOrFixmeText + " " + message,
					lineNumber,
					offset,
					endOffset);
			
			found = patternMatcher.find();
		}
	}
	
}
