/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.text.partitioning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.junit.jupiter.api.Test;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;

public class MarkdownPartitionScannerIT {

	private IDocument createDocument(String content) {
		return new Document(content);
	}
	
	private ITypedRegion[] computePartitions(IDocument document) throws Exception {
		MarkdownPartioningTools.getTools().setupDocumentPartitioner(document);
		return MarkdownPartioningTools.getTools().computePartitioning(document);
	}
	
	private void assertRegion(ITypedRegion region, IDocument document, String expectedRegionType, String expectedText) throws BadLocationException {
		assertNotNull(region);
		assertEquals(expectedRegionType, region.getType());
		
		String text = document.get(region.getOffset(), region.getLength());
		assertEquals(expectedText, text);
	}

	@Test
	public void checkDetectingSingleLineComments() throws Exception {
		String markdown = """
				<!-- comment 1 -->
				# Heading
				
				<!--- comment 2 --->
				
				Paragraph
				
				Some
				more
				<!-- comment 3 -->
				text following...
				
				<!--comment 4-->
				""";
		
		IDocument document = createDocument(markdown);
		ITypedRegion[] regions = computePartitions(document);
		
		assertNotNull(regions);
		assertRegion(regions[0], document, MarkdownPartitions.COMMENT, "<!-- comment 1 -->");
		assertRegion(regions[1], document, IDocument.DEFAULT_CONTENT_TYPE, "\n# Heading\n\n");
		assertRegion(regions[2], document, MarkdownPartitions.COMMENT, "<!--- comment 2 --->");
		assertRegion(regions[3], document, IDocument.DEFAULT_CONTENT_TYPE, "\n\nParagraph\n\nSome\nmore\n");
		assertRegion(regions[4], document, MarkdownPartitions.COMMENT, "<!-- comment 3 -->");
		assertRegion(regions[5], document, IDocument.DEFAULT_CONTENT_TYPE, "\ntext following...\n\n");
		assertRegion(regions[6], document, MarkdownPartitions.COMMENT, "<!--comment 4-->");
		assertRegion(regions[7], document, IDocument.DEFAULT_CONTENT_TYPE, "\n");
	}
	
	@Test
	public void checkDetectingMultiLineComments() throws Exception {
		String markdown = """
				<!-- comment 1
				spanning
				 multiple
				  lines
				   ...
				    very long -->
				# Heading
				
				<!---
				comment 2
				--->
				
				Paragraph
				
				Some
				more
				<!-- comment 3 -->
				<!-- comment 4
				directly folowing
				another comment -->
				text following...""";
		
		IDocument document = createDocument(markdown);
		ITypedRegion[] regions = computePartitions(document);
		
		assertNotNull(regions);
		assertRegion(regions[0], document, MarkdownPartitions.COMMENT, """
				<!-- comment 1
				spanning
				 multiple
				  lines
				   ...
				    very long -->""");
		assertRegion(regions[1], document, IDocument.DEFAULT_CONTENT_TYPE, "\n# Heading\n\n");
		assertRegion(regions[2], document, MarkdownPartitions.COMMENT, "<!---\ncomment 2\n--->");
		assertRegion(regions[3], document, IDocument.DEFAULT_CONTENT_TYPE, "\n\nParagraph\n\nSome\nmore\n");
		assertRegion(regions[4], document, MarkdownPartitions.COMMENT, "<!-- comment 3 -->");
		assertRegion(regions[5], document, IDocument.DEFAULT_CONTENT_TYPE, "\n");
		assertRegion(regions[6], document, MarkdownPartitions.COMMENT, """
				<!-- comment 4
				directly folowing
				another comment -->""");
		assertRegion(regions[7], document, IDocument.DEFAULT_CONTENT_TYPE, "\ntext following...");
	}
	
	@Test
	public void checkDetectingCodeBlocks() throws Exception {
		String markdown = """
				# Heading
				
				```markdown
				Some Markdown code
				with links like this [example](path/to/missing-file.md).
				
				![](path/to/image.png)
				
				[PlantUML](https://plantuml.com)
				```
				
				A link [to ensure](https://www.test.de) some links are found.
				
				~~~
				More code with [Markdown links](path/to/file.txt)
				
				![](path/to/image.puml)
				
				[Bitbucket](https://www.bitbucket.com)
				~~~
				
				Indented code block:
				
				    This is code, too
				    Links here [are ignored](https://missing.org)
				    
				    ![diagram](file.svg)
				    [](no/file.md)
				""";
		
		// work-around to indent empty line in indented code block
		String[] lines = markdown.split("\n");
		int lineIndex = lines.length - 3;
		String lineToIndent = lines[lineIndex];
		lineToIndent = "    " + lineToIndent;
		lines[lineIndex] = lineToIndent;
		markdown = String.join("\n", lines);
		
		IDocument document = createDocument(markdown);
		ITypedRegion[] regions = computePartitions(document);
		
		assertNotNull(regions);
		assertRegion(regions[0], document, IDocument.DEFAULT_CONTENT_TYPE, "# Heading\n\n");
		assertRegion(regions[1], document, MarkdownPartitions.CODEBLOCK, """
				```markdown
				Some Markdown code
				with links like this [example](path/to/missing-file.md).
				
				![](path/to/image.png)
				
				[PlantUML](https://plantuml.com)
				```""");
		assertRegion(regions[2], document, IDocument.DEFAULT_CONTENT_TYPE, "\n\nA link [to ensure](https://www.test.de) some links are found.\n\n");
		assertRegion(regions[3], document, MarkdownPartitions.CODEBLOCK, """
				~~~
				More code with [Markdown links](path/to/file.txt)
				
				![](path/to/image.puml)
				
				[Bitbucket](https://www.bitbucket.com)
				~~~""");
		assertRegion(regions[4], document, IDocument.DEFAULT_CONTENT_TYPE, "\n\nIndented code block:\n\n");
		assertRegion(regions[5], document, MarkdownPartitions.CODEBLOCK, """
				This is code, too
				Links here [are ignored](https://missing.org)
				
				![diagram](file.svg)
				[](no/file.md)""".indent(4).stripTrailing());
	}
	
	@Test
	public void checkDetectingCodeSpans() throws Exception {
		String markdown = """
				# Heading
				
				Some paragraph with `variables`
				and other `complex = expressions - 5 * a` in code spans.
				
				```
				Some code
				in a code block
				```
				
				```
				More code with `backticks` symbols in it.
				```
				
				Another `paragraph` with in-line code spans
				`that have to be detected` correctly.
				""";
		
		IDocument document = createDocument(markdown);
		ITypedRegion[] regions = computePartitions(document);
		
		assertNotNull(regions);
		assertRegion(regions[0], document, IDocument.DEFAULT_CONTENT_TYPE, "# Heading\n\nSome paragraph with ");
		assertRegion(regions[1], document, MarkdownPartitions.CODESPAN, "`variables`");
		assertRegion(regions[2], document, IDocument.DEFAULT_CONTENT_TYPE, "\nand other ");
		assertRegion(regions[3], document, MarkdownPartitions.CODESPAN, "`complex = expressions - 5 * a`");
		assertRegion(regions[4], document, IDocument.DEFAULT_CONTENT_TYPE, " in code spans.\n\n");
		assertRegion(regions[5], document, MarkdownPartitions.CODEBLOCK, """
				```
				Some code
				in a code block
				```""");
		assertRegion(regions[6], document, IDocument.DEFAULT_CONTENT_TYPE, "\n\n");
		assertRegion(regions[7], document, MarkdownPartitions.CODEBLOCK, """
				```
				More code with `backticks` symbols in it.
				```""");
		assertRegion(regions[8], document, IDocument.DEFAULT_CONTENT_TYPE, "\n\nAnother ");
		assertRegion(regions[9], document, MarkdownPartitions.CODESPAN, "`paragraph`");
		assertRegion(regions[10], document, IDocument.DEFAULT_CONTENT_TYPE, " with in-line code spans\n");
		assertRegion(regions[11], document, MarkdownPartitions.CODESPAN, "`that have to be detected`");
		assertRegion(regions[12], document, IDocument.DEFAULT_CONTENT_TYPE, " correctly.\n");
	}
	
	@Test
	public void checkEscapingInCodeSpans() throws Exception {
		String markdown = """
				# Heading
				
				Some text `this is code` with code.
				
				More text with \\`not code`.
				
				Even more `code with a backslash\\` ending here` that
				does not escape a backtick in a code span.
				""";
		
		IDocument document = createDocument(markdown);
		ITypedRegion[] regions = computePartitions(document);
		
		assertNotNull(regions);
		assertRegion(regions[0], document, IDocument.DEFAULT_CONTENT_TYPE, "# Heading\n\nSome text ");
		assertRegion(regions[1], document, MarkdownPartitions.CODESPAN, "`this is code`");
		assertRegion(regions[2], document, IDocument.DEFAULT_CONTENT_TYPE, " with code.\n\nMore text with \\`not code`.\n\nEven more ");
		assertRegion(regions[3], document, MarkdownPartitions.CODESPAN, "`code with a backslash\\`");
		assertRegion(regions[4], document, IDocument.DEFAULT_CONTENT_TYPE, " ending here` that\ndoes not escape a backtick in a code span.\n");
	}

}
