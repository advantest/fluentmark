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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlCommentBlock;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.Paragraph;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.attributes.AttributesNode;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.ext.tables.TableSeparator;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator;

import net.certiv.fluentmark.core.TestFileUtil;


public class MarkdownParserTest {
	
	private static String markdownSourceCode;
	MarkdownParser parser;
	
	@BeforeAll
	public static void setUpBeforeAll() throws Exception {
		markdownSourceCode = TestFileUtil.readTextFromFile("resources/parsing/feature-overview.md");
	}
	
	@BeforeEach
	public void setUp() {
		parser = new MarkdownParser();
	}
	
	@AfterEach
	public void tearDown() {
		parser = null;
	}
	
	@Test
	public void parsing_common_elements() throws Exception {
		Document markdownAstRootNode = parser.parseMarkdownCode(markdownSourceCode);
		
		Node currentNode = markdownAstRootNode.getFirstChild();
		
		assertNotNull(currentNode);
		assertTrue(currentNode instanceof Heading);
		
		Heading heading = (Heading) currentNode;
		
		assertEquals("Short FluentMark features and syntax overview", heading.getText().toString());
		assertEquals(1, heading.getLevel());
		
		
		
		currentNode = currentNode.getNext();
		
		assertNotNull(currentNode);
		assertTrue(currentNode instanceof HtmlCommentBlock);
		
		HtmlCommentBlock commentBlock = (HtmlCommentBlock) currentNode;
		
		assertEquals("<!--- Should here be an introduction? --->\n", commentBlock.getChars().toString());
		
		
		
		currentNode = currentNode.getNext();
		
		assertNotNull(currentNode);
		assertTrue(currentNode instanceof Heading);
		
		heading = (Heading) currentNode;

		assertTrue(heading.getFirstChild() instanceof Text);
		assertEquals("Images", heading.getFirstChild().getChars().toString());
		assertEquals(2, heading.getLevel());
		assertEquals("section-images", heading.getAnchorRefId());
		assertTrue(heading.getFirstChild().getNext() instanceof AttributesNode);
		
		
		
		currentNode = currentNode.getNext();
		
		assertNotNull(currentNode);
		assertTrue(currentNode instanceof Paragraph);
		
		Paragraph paragraph = (Paragraph) currentNode;
		
		assertTrue(paragraph.getFirstChild() instanceof Image);
		assertEquals(paragraph.getFirstChild(), paragraph.getLastChild());
		
		Image image = (Image) paragraph.getFirstChild();
		
		assertEquals("The FluentMark Logo PNG image (bitmap graphics)", image.getText().toString());
		assertEquals("../../doc/Logo110x80.png", image.getPageRef().toString());
		
		
		
		currentNode = currentNode.getNext();
		currentNode = currentNode.getNext();
		currentNode = currentNode.getNext();
		
		assertNotNull(currentNode);
		assertTrue(currentNode instanceof FencedCodeBlock);
		
		FencedCodeBlock codeBlock = (FencedCodeBlock) currentNode;
		
		assertEquals(1, codeBlock.getLineCount());
		assertEquals("![External PlantUML diagram file](PlantUML/classes.puml)\n", codeBlock.getLineChars(0).toString());
		assertEquals("![External PlantUML diagram file](PlantUML/classes.puml)\n", codeBlock.getContentLines(0, 1).get(0).toString());
		
		
		
		currentNode = currentNode.getNext();
		
		assertNotNull(currentNode);
		assertTrue(currentNode instanceof Paragraph);
		
		paragraph = (Paragraph) currentNode;
		
		assertTrue(paragraph.getFirstChild() instanceof Image);
		assertEquals(paragraph.getFirstChild(), paragraph.getLastChild());
		
		image = (Image) paragraph.getFirstChild();
		
		assertEquals("External PlantUML diagram file", image.getText().toString());
		assertEquals("PlantUML/classes.puml", image.getPageRef().toString());
		
		
		
		while (!(currentNode instanceof TableBlock)) {
			currentNode = currentNode.getNext();
		}
		
		TableBlock tableBlock = (TableBlock) currentNode;
		
		assertEquals(4, tableBlock.getLineCount());
		assertTrue(tableBlock.getFirstChild() instanceof TableHead);
		assertTrue(tableBlock.getFirstChild().getNext() instanceof TableSeparator);
		assertTrue(tableBlock.getFirstChild().getNext().getNext() instanceof TableBody);
		
		TableBody tableBody = (TableBody) tableBlock.getFirstChild().getNext().getNext();
		
		assertTrue(tableBody.getFirstChild() instanceof TableRow);

		TableRow tableRow = (TableRow) tableBody.getFirstChild(); 
		
		assertEquals(1, tableRow.getRowNumber());
		assertEquals(4, countCellsInRow(tableRow));
		assertEquals("first cell", ((TableCell) tableRow.getFirstChild()).getText().toString());
	}
	
	private int countCellsInRow(TableRow row) {
		ReversiblePeekingIterator<Node> iterator = row.getChildIterator();
		int numberOfRows = 0;
		Node childNode = null;
		while (iterator.hasNext()) {
			childNode = iterator.next();
			if (childNode instanceof TableCell) {
				numberOfRows++;
			}
		}
		return numberOfRows;
	}

}
