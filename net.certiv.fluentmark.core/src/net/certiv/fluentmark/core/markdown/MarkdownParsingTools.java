/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown;

public class MarkdownParsingTools {
	
	public static final String REGEX_ANY_LINE_SEPARATOR = "(\\r\\n|\\n)";

	public static final String REGEX_HEADING_WITH_ANCHOR_CAPTURING_GROUP_ANCHOR = "anchor";
	
	// the following regex contains a named capturing group, name is "anchor", syntax: (?<name>Sally)
	public static final String REGEX_HEADING_WITH_ANCHOR = "#+\\s.*\\{#(?<" + REGEX_HEADING_WITH_ANCHOR_CAPTURING_GROUP_ANCHOR + ">.*)\\}\\s*";
	public static final String REGEX_VALID_ANCHOR_ID = "[A-Za-z][A-Za-z0-9-_:\\.]*";
	
}
