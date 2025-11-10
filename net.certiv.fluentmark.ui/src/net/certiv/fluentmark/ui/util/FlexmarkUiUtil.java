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

import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ext.plantuml.PlantUmlBlockNode;
import com.vladsch.flexmark.ext.plantuml.PlantUmlFencedCodeBlockNode;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.Document;

import net.certiv.fluentmark.core.util.FlexmarkUtil;

public class FlexmarkUiUtil {
	
	public static Document parseMarkdownAst(IDocument markdownDocument) {
		if (markdownDocument == null) {
			return null;
		}
		
		String markdownFileContent = markdownDocument.get();
		return FlexmarkUtil.parseMarkdown(markdownFileContent);
	}
	
	public static Image findMarkdownImageForTextSelection(IDocument document, ITextSelection textSelection) {
		if (document == null || textSelection == null || textSelection.getStartLine() != textSelection.getEndLine()) {
			return null;
		}
		
		return findMarkdownImageForTextSelection(parseMarkdownAst(document), textSelection);
	}
	
	public static Image findMarkdownImageForTextSelection(Document markdownAst, ITextSelection textSelection) {
		if (markdownAst == null || textSelection == null || textSelection.getStartLine() != textSelection.getEndLine()) {
			return null;
		}
		
		return FlexmarkUtil.getStreamOfDescendants(markdownAst)
			.filter(node -> node.getLineNumber() <= textSelection.getEndLine())
			.filter(node -> node instanceof Image)
			.map(node -> (Image) node)
			.filter(imageNode -> imageNode.getStartOffset() <= textSelection.getOffset()
					&& imageNode.getEndOffset() >= textSelection.getOffset() + textSelection.getLength())
			.findFirst()
			.orElse(null);
	}
	
	public static Block findPlantUmlCodeBlockForTextSelection(IDocument document, ITextSelection textSelection) {
		if (document == null || textSelection == null) {
			return null;
		}
		
		return findPlantUmlCodeBlockForTextSelection(parseMarkdownAst(document), textSelection);
	}
	
	public static Block findPlantUmlCodeBlockForTextSelection(Document markdownAst, ITextSelection textSelection) {
		if (markdownAst == null || textSelection == null) {
			return null;
		}
		
		return FlexmarkUtil.getStreamOfDescendants(markdownAst)
			.filter(node -> textSelection.getStartLine() >= node.getStartLineNumber() && textSelection.getEndLine() <= node.getEndLineNumber())
			.filter(node -> node instanceof PlantUmlBlockNode || node instanceof PlantUmlFencedCodeBlockNode)
			.map(node -> (Block) node)
			.findFirst()
			.orElse(null);
	}
	
}
