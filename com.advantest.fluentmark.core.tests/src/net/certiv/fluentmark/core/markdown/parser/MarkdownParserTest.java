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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlCommentBlock;
import com.vladsch.flexmark.util.ast.BlankLine;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

import net.certiv.fluentmark.core.TestFileUtil;


public class MarkdownParserTest {
	
	@Test
	public void test() throws Exception {
		String markdownSourceCode = TestFileUtil.readTextFromFile("resources/parsing/feature-overview.md");
		
		MarkdownParser parser = new MarkdownParser();
		Document markdownAstRootNode = parser.parseMarkdownCode(markdownSourceCode);

		Node currentNode = markdownAstRootNode.getFirstChild();
		
		assertNotNull(currentNode);
		assertTrue(currentNode instanceof Heading);
		
		Heading firstHeading = (Heading) currentNode;
		
		assertEquals("Short FluentMark features and syntax overview", firstHeading.getText().toString());
		
		currentNode = currentNode.getNext();
		
		assertNotNull(currentNode);
		assertTrue(currentNode instanceof BlankLine);
		
		currentNode = currentNode.getNext();
		
		assertNotNull(currentNode);
		assertTrue(currentNode instanceof HtmlCommentBlock);
		
		HtmlCommentBlock commentBlock = (HtmlCommentBlock) currentNode;
		
		assertEquals("<!--- Should here be an introduction? --->\n", commentBlock.getChars().toString());
	}

}
