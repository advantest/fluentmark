/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.text.partitioning;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.core.util.FluentPartitioningTools;

public class MarkdownPartioningTools {
	
	private static MarkdownPartioningTools INSTANCE = null;
	
	private MarkdownPartitionScanner partitionScanner;
	
	private MarkdownPartioningTools() {}
	
	public static MarkdownPartioningTools getTools() {
		if (INSTANCE == null) {
			INSTANCE = new MarkdownPartioningTools();
		}
		return INSTANCE;
	}
	
	private IPartitionTokenScanner getPartitionScanner() {
		if (partitionScanner == null) {
			partitionScanner = new MarkdownPartitionScanner();
		}
		return partitionScanner;
	}
	
	public IDocumentPartitioner createDocumentPartitioner() {
		IPartitionTokenScanner scanner = getPartitionScanner();
		return new FastPartitioner(scanner, MarkdownPartitions.getLegalContentTypes());
	}
	
	public void setupDocumentPartitioner(IDocument document) {
		if (document == null) {
			throw new IllegalArgumentException();
		}
		
		IDocumentPartitioner partitioner = FluentPartitioningTools.getDocumentPartitioner(document, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
		
		if (partitioner == null) {
			partitioner = MarkdownPartioningTools.getTools().createDocumentPartitioner();
			FluentPartitioningTools.setupDocumentPartitioner(document, partitioner, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
		}
	}
	
	public ITypedRegion[] computePartitioning(IDocument document) throws BadLocationException {
		return MarkdownPartitions.computePartitions(document);
	}

}
