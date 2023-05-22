/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

import net.certiv.fluentmark.core.convert.Partitions;

public class PartitionCalculator {
	
	public static ITypedRegion[] computePartitions(IDocument document) {
		int beg = 0;
		int len = document.getLength();

		try {
			return TextUtilities.computePartitioning(document, Partitions.PARTITIONING, beg, len, false);
		} catch (BadLocationException e) {
			return new ITypedRegion[0];
		}
	}

}
