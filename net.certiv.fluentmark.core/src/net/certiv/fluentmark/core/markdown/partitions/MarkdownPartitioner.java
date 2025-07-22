/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown.partitions;

import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import net.certiv.fluentmark.core.extensionpoints.DocumentPartitionersManager;
import net.certiv.fluentmark.core.partitions.AbstractDocumentPartitioner;

public class MarkdownPartitioner extends AbstractDocumentPartitioner {

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
	
	private static MarkdownPartitioner INSTANCE = null;
	
	public static MarkdownPartitioner get() {
		if (INSTANCE == null ) {
			INSTANCE = (MarkdownPartitioner) DocumentPartitionersManager.getInstance()
					.getDocumentPartitioner(FLUENT_MARKDOWN_PARTITIONING);
		}
		return INSTANCE;
	}
	
	@Override
	public String getSupportedPartitioning() {
		return FLUENT_MARKDOWN_PARTITIONING;
	}
	
	@Override
	protected IPartitionTokenScanner createPartitionScanner() {
		return new MarkdownPartitionScanner();
	}
	
	@Override
	public String[] getLegalContentTypes() {
		return LEGAL_TYPES;
	}
}
