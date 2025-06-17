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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MarkdownParsingToolsTest {
	
	private String getLabel(RegexMatch match) {
		RegexMatch labelMatch = match.subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_LABEL);
		if (labelMatch != null) {
			return labelMatch.matchedText;
		}
		return null;
	}
	
	private String getTarget(RegexMatch match) {
		RegexMatch targetMatch = match.subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_TARGET);
		if (targetMatch != null) {
			return targetMatch.matchedText;
		}
		return null;
	}
	
	@ParameterizedTest
	@CsvSource({"""
			[label](target),label,target
			[Some link title](),Some link title,
			[](https://something.com),,https://something.com
			[](),,
			[Solunar](https://www.solunar.de),Solunar,https://www.solunar.de
			[Some text with almost any symbol :;.-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.puml),Some text with almost any symbol :;.-_<>!\"§$%&/()=?`´´#')\\{},some/path/to/a_file.puml
			"""
	})
	public void testSimpleLinksFound(String statement, String label, String target) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinksAndImages(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(statement, match.get().matchedText);
		assertEquals(label, getLabel(match.get()));
		assertEquals(target, getTarget(match.get()));
	}
	
	// TODO test escaping brackets

}
