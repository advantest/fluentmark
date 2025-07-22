/*******************************************************************************
 * Copyright (c) 2016 - 2018 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.markdown.partitions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import net.certiv.fluentmark.core.partitions.FluentPartitioningTools;

public class MarkdownPartitions {

	// unique partitioning type
	public final static String FLUENT_MARKDOWN_PARTITIONING = "__fluent_partitioning";

	// specialized partition content types
	public static final String FRONT_MATTER = "__frontmatter";
	public static final String COMMENT = "__comment";
	public static final String CODEBLOCK = "__codeblock";
	public static final String CODESPAN = "__codespan";
	public static final String HTMLBLOCK = "__htmlblock";
	public static final String DOTBLOCK = "__dotblock";
	public static final String UMLBLOCK = "__umlblock";
	public static final String MATHBLOCK = "__mathblock";
	public static final String PLANTUML_INCLUDE = "__plantuml_include";

	/** Partition type groups by similar treatment */
	public static final String[] LEGAL_TYPES = new String[] { COMMENT, CODEBLOCK, CODESPAN, HTMLBLOCK, DOTBLOCK, UMLBLOCK,
			MATHBLOCK, PLANTUML_INCLUDE, FRONT_MATTER };

	private static MarkdownPartitions INSTANCE = null;
	
	private MarkdownPartitionScanner partitionScanner;
	
	private MarkdownPartitions() {}
	
	public static MarkdownPartitions get() {
		if (INSTANCE == null) {
			INSTANCE = new MarkdownPartitions();
		}
		return INSTANCE;
	}

	public static String[] getLegalContentTypes() {
		return LEGAL_TYPES;
	}
	
	public ITypedRegion[] computePartitions(IDocument document) {
		return FluentPartitioningTools.computePartitions(document, FLUENT_MARKDOWN_PARTITIONING);
	}
	
	private IPartitionTokenScanner getPartitionScanner() {
		if (partitionScanner == null) {
			partitionScanner = new MarkdownPartitionScanner();
		}
		return partitionScanner;
	}
	
	public IDocumentPartitioner createDocumentPartitioner() {
		IPartitionTokenScanner scanner = getPartitionScanner();
		return new FastPartitioner(scanner, getLegalContentTypes());
	}
	
	public void setupDocumentPartitioner(IDocument document) {
		if (document == null) {
			throw new IllegalArgumentException();
		}
		
		IDocumentPartitioner partitioner = FluentPartitioningTools.getDocumentPartitioner(document, FLUENT_MARKDOWN_PARTITIONING);
		
		if (partitioner == null) {
			partitioner = createDocumentPartitioner();
			FluentPartitioningTools.setupDocumentPartitioner(document, partitioner, FLUENT_MARKDOWN_PARTITIONING);
		}
	}
}
