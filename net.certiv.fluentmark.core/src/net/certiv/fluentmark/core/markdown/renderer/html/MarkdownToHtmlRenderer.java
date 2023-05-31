/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown.renderer.html;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

import net.certiv.fluentmark.core.markdown.parser.MarkdownParser;

public class MarkdownToHtmlRenderer {
	
	private final MarkdownParser parser;
	private final HtmlRenderer htmlRenderer;
	
	public MarkdownToHtmlRenderer() {
		parser = new MarkdownParser();
		MutableDataSet settings = parser.getSettings();
		settings.set(HtmlRenderer.RENDER_HEADER_ID, true);
		settings.set(HtmlRenderer.GENERATE_HEADER_ID, false);
		this.htmlRenderer = HtmlRenderer.builder(parser.getSettings()).build();
	}
	
	public String renderHtml(Node markdownAstNode) {
		return htmlRenderer.render(markdownAstNode);
	}
	
	public String renderHtml(String markdownSourceCode) {
		Document markdownAst = this.parser.parseMarkdownCode(markdownSourceCode);
		return renderHtml(markdownAst);
	}

}
