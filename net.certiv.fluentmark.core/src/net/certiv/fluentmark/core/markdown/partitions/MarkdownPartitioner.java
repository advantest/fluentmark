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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.partitions.IFluentDocumentPartitioner;

public class MarkdownPartitioner implements IFluentDocumentPartitioner {

	@Override
	public String getSupportedPartitioning() {
		return MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING;
	}

	@Override
	public void setupDocumentPartitioner(IDocument document, IFile file) {
		MarkdownPartitions.get().setupDocumentPartitioner(document);

	}

	@Override
	public ITypedRegion[] computePartitioning(IDocument document, IFile file) {
		return MarkdownPartitions.get().computePartitions(document);
	}

}
