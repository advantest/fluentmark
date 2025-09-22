/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.util;

import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import com.advantest.markdown.MarkdownParserAndHtmlRenderer;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator;

public class FlexmarkUtil {
	
	private static final MarkdownParserAndHtmlRenderer markdownParserHtmlRenderer = new MarkdownParserAndHtmlRenderer();
	
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
		Builder<Node> streamBuilder = Stream.builder();
		
		ReversiblePeekingIterator<Node> iterator = markdownAst.getDescendants().iterator();
		while (iterator.hasNext()) {
			Node astNode = iterator.next();
			streamBuilder.accept(astNode);
		}
		
		return streamBuilder.build();
	}

	@SuppressWarnings("unchecked")
	public static <T> Stream<T> getStreamOf(Document markdownAst, Class<T> elementType) {
		return getStreamOfDescendants(markdownAst)
				.filter(node -> elementType.isInstance(node))
				.map(node -> (T) node);
	}
	
}
