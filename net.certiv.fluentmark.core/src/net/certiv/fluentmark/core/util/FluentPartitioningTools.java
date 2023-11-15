/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

public class FluentPartitioningTools {
	
	public static void setupDocumentPartitioner(IDocument document, IDocumentPartitioner partitioner, String partitioning) {
		if (document == null || partitioner == null || partitioning == null || StringUtils.isBlank(partitioning)) {
			throw new IllegalArgumentException();
		}
		
		partitioner.connect(document);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(partitioning, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
	}
	
	public static ITypedRegion[] computePartitions(IDocument document, String partitioning) {
		int beg = 0;
		int len = document.getLength();
		
		if (len <= 0) {
			return new ITypedRegion[0];
		}

		try {
			return TextUtilities.computePartitioning(document, partitioning, beg, len, false);
		} catch (BadLocationException e) {
			return new ITypedRegion[0];
		}
	}

}
