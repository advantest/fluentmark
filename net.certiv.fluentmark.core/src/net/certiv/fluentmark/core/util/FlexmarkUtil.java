/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025-2026 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.util;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.advantest.flexmark.ext.sourcetracking.SourcePositionAttributesRendererExtension;
import com.advantest.markdown.MarkdownParserAndHtmlRenderer;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator;

import net.certiv.fluentmark.core.extensionpoints.MarkdownParserAndRendererCustomizerManager;
import net.certiv.fluentmark.core.plantuml.parsing.PlantUmlParsingTools;

/**
 * @see PlantUmlParsingTools
 */
public class FlexmarkUtil {
	
	private static final MarkdownParserAndHtmlRenderer markdownParserHtmlRenderer = createMarkdownParserAndHtmlRenderer();
	
	private static MarkdownParserAndHtmlRenderer createMarkdownParserAndHtmlRenderer() {
		return MarkdownParserAndHtmlRenderer.builder()
				// add flexmark extension for adding HTML source tracking attributes 
				.withExtension(SourcePositionAttributesRendererExtension.create())
				// customize the options via plug-in extensions
				.withCustomization(options ->
					MarkdownParserAndRendererCustomizerManager.getInstance().getMarkdownParserAndRendererCustomizers()
							.forEach(customizer -> customizer.customizeOptions(options))
				)
				.build();
	}
	
	public static Document parseMarkdown(String markdownCode) {
		if (markdownCode == null) {
			throw new IllegalArgumentException();
		}
		
		return markdownParserHtmlRenderer.parseMarkdown(markdownCode);
	}
	
	public static String renderHtml(Document markdownAst) {
		return markdownParserHtmlRenderer.renderHtml(markdownAst);
	}
	
	public static Stream<Node> getStreamOfDescendants(Document markdownAst) {
		ReversiblePeekingIterator<Node> iterator = markdownAst.getDescendants().iterator();
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
	}

	@SuppressWarnings("unchecked")
	public static <T> Stream<T> getStreamOf(Document markdownAst, Class<T> elementType) {
		return getStreamOfDescendants(markdownAst)
				.filter(node -> elementType.isInstance(node))
				.map(node -> (T) node);
	}
	
}
