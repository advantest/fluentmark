/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown.parser;

import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.data.SharedDataKeys;

import java.util.Arrays;

/**
 * Markdown source code parser based on
 * <a href="https://github.com/vsch/flexmark-java">Flexmark</a>
 * with a selection of extensions.
 */
public class MarkdownParser {
	
	private final MutableDataSet options;
	private final Parser parser;
	
	public MarkdownParser() {
		options = new MutableDataSet();
		options.set(Parser.BLANK_LINES_IN_AST, false);
        options.set(Parser.EXTENSIONS, Arrays.asList(
        		
        		// see https://github.com/vsch/flexmark-java/wiki/Tables-Extension
        		TablesExtension.create(),
        		
        		// see https://github.com/vsch/flexmark-java/wiki/Extensions#gfm-strikethroughsubscript
        		StrikethroughExtension.create(),
        		
        		// see https://github.com/vsch/flexmark-java/wiki/Extensions#autolink
        		AutolinkExtension.create(),
        		
        		// see https://github.com/vsch/flexmark-java/wiki/Attributes-Extension
        		// needed e.g. for setting custom heading anchor IDs
        		AttributesExtension.create()));
        options.set(SharedDataKeys.INDENT_SIZE, 2);
        options.set(SharedDataKeys.GENERATE_HEADER_ID, false);
        parser = Parser.builder(options).build();
	}
	
	public MutableDataSet getSettings() {
		return this.options;
	}
	
	public Document parseMarkdownCode(String sourceCode) {
        return parser.parse(sourceCode);
	}

}
