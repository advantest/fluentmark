/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown.flexmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.advantest.markdown.MarkdownParserAndHtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;

import net.certiv.fluentmark.core.extensionpoints.MarkdownParserAndRendererCustomizerManager;

public class MarkdownParserAndHtmlRendererWithSourceTracking extends MarkdownParserAndHtmlRenderer {
	
	@Override
	protected MutableDataSet createOptions() {
		MutableDataSet options = super.createOptions();

		Collection<Extension> extensionsFromParent = Parser.EXTENSIONS.get(options);

		List<Extension> allExtensions = new ArrayList<>(extensionsFromParent);
		allExtensions.add(SourcePositionAttributesRendererExtension.create());
		
		options.set(Parser.EXTENSIONS, allExtensions);
		
		// Customize the options via extensions
		MarkdownParserAndRendererCustomizerManager.getInstance().getMarkdownParserAndRendererCustomizers().stream()
			.forEach(customizer -> customizer.customizeOptions(options));

		return options;
	}
	
}
