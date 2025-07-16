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

import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.markdown.scanner.IScannerExt;

/**
 * Test class, especially for debugging purposes. Uses a "real" scanner, but
 * offers a few observability features like a human readable output in toString() method
 * and the consumed text in a getter method.
 */
public class CharacterScannerDelegate implements IObservableCharacterScanner {
	
	private final IScannerExt scannerDelegate;
	private final String text;
	private int currentIndex = -1;
	private final int startOffset;
	
	public CharacterScannerDelegate(String text, IScannerExt delegate) {
		this.text = text;
		this.scannerDelegate = delegate;
		this.startOffset = delegate.getOffset();
		this.currentIndex = startOffset;
	}

	@Override
	public char[][] getLegalLineDelimiters() {
		return scannerDelegate.getLegalLineDelimiters();
	}

	@Override
	public int getColumn() {
		return scannerDelegate.getColumn();
	}
	
	@Override
	public IDocument getDocument() {
		return scannerDelegate.getDocument();
	}

	@Override
	public int getOffset() {
		return scannerDelegate.getOffset();
	}

	@Override
	public int getRangeEnd() {
		return scannerDelegate.getRangeEnd();
	}

	@Override
	public int read() {
		currentIndex++;
		return scannerDelegate.read();
	}

	@Override
	public void unread() {
		currentIndex--;
		scannerDelegate.unread();
	}

	@Override
	public String getConsumedText() {
		if (currentIndex - 1 < 0 || currentIndex - 1 < startOffset || currentIndex - 1 > text.length()) {
			return "";
		}
		return text.substring(startOffset, Math.min(text.length(), currentIndex));
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("index: ");
		builder.append(currentIndex);
		builder.append(", column: ");
		builder.append(scannerDelegate.getColumn());
		builder.append(", current char: ");
		Character currentChar = currentIndex - 1 >= 0 && currentIndex - 1 >= startOffset && currentIndex - 1 < text.length() ? text.charAt(currentIndex - 1) : null;
		if (currentChar == null) {
			builder.append(currentChar);
		} else {
			builder.append("'");
			builder.append(currentChar);
			builder.append("'");
		}
		
		builder.append(", consumed:");
		String consumedText = getConsumedText();
		if (consumedText != null) {
			builder.append("\n\"");
			builder.append(consumedText);
			builder.append("\"");
		} else {
			builder.append(" ");
			builder.append(consumedText);
		}
		return builder.toString();
	}
}
