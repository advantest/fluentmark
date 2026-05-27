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

import com.vladsch.flexmark.util.data.MutableDataSet;

public interface IMarkdownParserAndRendererCustomizer {

	/**
	 * Allows other plug-ins to add flexmark extensions
	 * or adapt flexmark settings for parsing and rendering.
	 * Client are expected not to remove any extension, only additions are allowed.
	 * There are no guarantees on the execution order of multiple
	 * customizer extensions.
	 * 
	 * @param parserAndRendererOptions the default settings to be adapted
	 */
	void customizeOptions(MutableDataSet parserAndRendererOptions);

}
