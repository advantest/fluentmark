/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.util;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import com.advantest.markdown.MarkdownParserAndHtmlRenderer;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.collection.iteration.ReversiblePeekingIterator;

public class FlexmarkUtil {
	
	public static Image findMarkdownImageForTextSelection(IDocument document, ITextSelection textSelection) {
		if (document == null || textSelection == null || textSelection.getStartLine() != textSelection.getEndLine()) {
			return null;
		}
		
		String markdownFileContent = document.get();
		
		MarkdownParserAndHtmlRenderer markdownParser = new MarkdownParserAndHtmlRenderer();
		Document markdownAst = markdownParser.parseMarkdown(markdownFileContent);
		
		ReversiblePeekingIterator<Node> iter = markdownAst.getDescendants().iterator();
		while (iter.hasNext()) {
			Node node = iter.next();
			
			if (node.getLineNumber() > textSelection.getEndLine()) {
				break;
			}
			
			if (node instanceof Image) {
				Image imageNode = (Image) node;
				
				if (imageNode.getStartOffset() <= textSelection.getOffset()
						&& imageNode.getEndOffset() >= textSelection.getOffset() + textSelection.getLength()) {
					return imageNode;
				}
			}
		}
		
		return null;
	}

}
