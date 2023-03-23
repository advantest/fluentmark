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

import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.Arrays;

public class MarkdownParser {
	
	private final MutableDataSet options;
	private final Parser parser;
	
	public MarkdownParser() {
		options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
        		TablesExtension.create(),
        		StrikethroughExtension.create(),
        		AutolinkExtension.create()));
        parser = Parser.builder(options).build();
	}
	
	public MutableDataSet getSettings() {
		return this.options;
	}
	
	public Document parseMarkdownCode(String sourceCode) {
        return parser.parse(sourceCode);
	}

}
