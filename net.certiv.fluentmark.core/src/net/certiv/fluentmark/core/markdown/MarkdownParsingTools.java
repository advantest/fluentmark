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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class MarkdownParsingTools {
	
	public static final String REGEX_ANY_LINE_SEPARATOR = "(\\r\\n|\\n)";

	public static final String REGEX_HEADING_WITH_ANCHOR_CAPTURING_GROUP_ANCHOR = "anchor";
	
	// the following regex contains a named capturing group, name is "anchor", syntax: (?<name>Sally)
	public static final String REGEX_HEADING_WITH_ANCHOR = "#+\\s.*\\{#(?<" + REGEX_HEADING_WITH_ANCHOR_CAPTURING_GROUP_ANCHOR + ">.*)\\}\\s*";
	public static final String REGEX_VALID_ANCHOR_ID = "[A-Za-z][A-Za-z0-9-_:\\.]*";
	
	public static Set<String> findValidSectionAnchorsInMarkdownCode(String markdownCode) {
		return Arrays.stream(markdownCode.split(REGEX_ANY_LINE_SEPARATOR))
				.filter(line -> line.matches(REGEX_HEADING_WITH_ANCHOR))
				.map(lineWithAnchor -> {
					int startIndex = lineWithAnchor.lastIndexOf("{#") + 2;
					int endIndex = lineWithAnchor.lastIndexOf("}");
					return lineWithAnchor.substring(startIndex, endIndex);
				})
				.filter(anchor -> anchor.matches(REGEX_VALID_ANCHOR_ID))
				.collect(Collectors.toSet());
	}
}
