/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2022 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class LinesTest {
	
	@Test
	public void testParsing_Comments() throws IOException {
		// given
		String text = readTextFromFile("resources/md/comments.md");
		
		// when
		Lines linesFromFile = new Lines(text, "\n");
		
		// then
		assertEquals(35, linesFromFile.length());
		assertTextAndKind(linesFromFile, 0, "# Header Level 1", Type.HEADER);
		assertTextAndKind(linesFromFile, 1, "", Type.BLANK);
		assertTextAndKind(linesFromFile, 2, "Some text", Type.TEXT);
		assertTextAndKind(linesFromFile, 4, "## Header Level 2", Type.HEADER);
		assertTextAndKind(linesFromFile, 7, "<!-- HTML-style comment -->", Type.HTML_BLOCK);
		assertTextAndKind(linesFromFile, 11, "<!--- hidden comment --->", Type.COMMENT);
		assertTextAndKind(linesFromFile, 31, "<!-- a multi-line", Type.HTML_BLOCK);
//		assertTextAndKind(linesFromFile, 32, "HTML-style", Type.HTML_BLOCK);
//		assertTextAndKind(linesFromFile, 33, "  ...", Type.HTML_BLOCK);
//		assertTextAndKind(linesFromFile, 34, "     comment -->", Type.HTML_BLOCK);
	}
	
	private void assertTextAndKind(Lines lines, int lineIndex, String text, Type kind) {
		assertEquals(text, lines.getText(lineIndex));
		assertEquals(kind, lines.identifyKind(lineIndex));
	}
	
	private String readTextFromFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		byte[] bytes = Files.readAllBytes(path);
		return new String(bytes);
	}
	
}
