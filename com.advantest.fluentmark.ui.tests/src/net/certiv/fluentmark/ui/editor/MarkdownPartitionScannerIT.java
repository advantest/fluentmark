/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.junit.jupiter.api.Test;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.ui.editor.text.MarkdownPartioningTools;

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

}
