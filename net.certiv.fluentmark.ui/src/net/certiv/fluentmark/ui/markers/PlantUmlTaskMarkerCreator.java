/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.markers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.core.util.FluentPartitioningTools;
import net.certiv.fluentmark.ui.editor.text.MarkdownPartioningTools;
import net.certiv.fluentmark.ui.editor.text.PlantUmlPartitioningTools;
import net.certiv.fluentmark.ui.editor.text.PlantUmlPartitions;

public class PlantUmlTaskMarkerCreator implements ITypedRegionMarkerCalculator {
	
	private static final String TODO_FIXME_REGEX = "(?<keyword>TODO|FIXME):?[ \\t](?<message>.*?(?='\\/)|.*(?!'\\/))";
	private static final String REGEX_CAPTURING_GROUP_KEYWORD = "keyword";
	private static final String REGEX_CAPTURING_GROUP_MESSAGE = "message";
	
	private Pattern pattern;
	
	public PlantUmlTaskMarkerCreator() {
		pattern = Pattern.compile(TODO_FIXME_REGEX, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public void setupDocumentPartitioner(IDocument document, IFile file) {
		if (document == null) {
			throw new IllegalArgumentException();
		}
		
		IDocumentPartitioner partitioner;
		if (FileUtils.isPumlFile(file)) {
			partitioner = FluentPartitioningTools.getDocumentPartitioner(document, PlantUmlPartitions.FLUENT_PLANTUML_PARTITIONING);
			if (partitioner == null) {
				partitioner = PlantUmlPartitioningTools.getTools().createDocumentPartitioner();
				FluentPartitioningTools.setupDocumentPartitioner(document, partitioner, PlantUmlPartitions.FLUENT_PLANTUML_PARTITIONING);
			}
		} else if (FileUtils.isMarkdownFile(file)) {
			partitioner = FluentPartitioningTools.getDocumentPartitioner(document, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
			if (partitioner == null) {
				partitioner = MarkdownPartioningTools.getTools().createDocumentPartitioner();
				FluentPartitioningTools.setupDocumentPartitioner(document, partitioner, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
			}
		}
	}

	@Override
	public ITypedRegion[] computePartitioning(IDocument document, IFile file) throws BadLocationException {
		if (FileUtils.isPumlFile(file)) {
			return PlantUmlPartitions.computePartitions(document);
		} else {
			return MarkdownPartitions.computePartitions(document);
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
	public void validateRegion(ITypedRegion region, IDocument document, IFile resource) throws CoreException {
		String regionContent;
		try {
			regionContent = document.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			return;
		}
		
		if (FileUtils.isMarkdownFile(resource)
				&& MarkdownPartitions.UMLBLOCK.equals(region.getType())) {
			IDocument codeBlockDocument = new Document(regionContent);
			IDocumentPartitioner partitioner = PlantUmlPartitioningTools.getTools().createDocumentPartitioner();
			FluentPartitioningTools.setupDocumentPartitioner(codeBlockDocument, partitioner, PlantUmlPartitions.FLUENT_PLANTUML_PARTITIONING);
			ITypedRegion[] inCodeBlockRegions = PlantUmlPartitions.computePartitions(codeBlockDocument);
			
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
	
	private void doValidateRegion(ITypedRegion region, String regionContent, IDocument document, IFile resource, int additionalOffset) throws CoreException {
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
			int lineNumber = getLineForOffset(document, offset);
			int endOffset = region.getOffset() + endIndex + additionalOffset;
			int priority = "FIXME".equalsIgnoreCase(todoOrFixmeText) ? IMarker.PRIORITY_HIGH : IMarker.PRIORITY_NORMAL;
			
			MarkerCalculator.createPlantUmlTaskMarker(resource, priority, todoOrFixmeText + " " + message, lineNumber, offset, endOffset);
			
			found = patternMatcher.find();
		}
	}
	
	protected int getLineForOffset(IDocument document, int offset) {
		try {
			return document.getLineOfOffset(offset) + 1;
		} catch (BadLocationException e) {
			return -1;
		}
	}

}
