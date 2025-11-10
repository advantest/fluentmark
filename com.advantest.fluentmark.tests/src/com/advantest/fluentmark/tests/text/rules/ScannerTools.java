/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package com.advantest.fluentmark.tests.text.rules;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.markdown.partitions.MarkdownPartitionScanner;

public class ScannerTools {
	
	public static IObservableCharacterScanner createMarkdownScanner(String input) {
		MarkdownPartitionScanner realScanner = new MarkdownPartitionScanner();
		IDocument document = new Document(input);
		realScanner.setRange(document, 0, input.length());
		IObservableCharacterScanner scanner =  new CharacterScannerDelegate(input, realScanner);
		return scanner;
	}
	
	public static IObservableCharacterScanner createMarkdownScanner(String input, int scannerOffset) {
		MarkdownPartitionScanner realScanner = new MarkdownPartitionScanner();
		IDocument document = new Document(input);
		realScanner.setRange(document, scannerOffset, input.length() - scannerOffset);
		IObservableCharacterScanner scanner =  new CharacterScannerDelegate(input, realScanner);
		return scanner;
	}

}
