/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.extensionpoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.markdown.flexmark.IMarkdownParserAndRendererCustomizer;
import net.certiv.fluentmark.core.util.ExtensionsUtil;

public class MarkdownParserAndRendererCustomizerManager {
	
	private static final String EXTENSION_POINT_ID_MARKDOWN_PARSER_CUSTOMIZER = FluentCore.PLUGIN_ID + ".markdownParserRendererCustomizer";
	
	private static MarkdownParserAndRendererCustomizerManager INSTANCE = null;
	
	private final List<IMarkdownParserAndRendererCustomizer> customizers = new ArrayList<>();
	
	private MarkdownParserAndRendererCustomizerManager() {
		this.init();
	}
	
	private void init() {
		customizers.addAll(
				ExtensionsUtil.createExecutableExtensionsFor(
						EXTENSION_POINT_ID_MARKDOWN_PARSER_CUSTOMIZER, 
						IMarkdownParserAndRendererCustomizer.class));
	}
	
	public static MarkdownParserAndRendererCustomizerManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MarkdownParserAndRendererCustomizerManager();
		}
		return INSTANCE;
	}
	
	public List<IMarkdownParserAndRendererCustomizer> getMarkdownParserAndRendererCustomizers() {
		return Collections.unmodifiableList(customizers);
	}

}
