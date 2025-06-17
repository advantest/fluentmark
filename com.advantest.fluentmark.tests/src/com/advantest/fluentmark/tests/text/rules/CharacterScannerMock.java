/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2025 Advantest Europe GmbH. All rights reserved.
 */
package com.advantest.fluentmark.tests.text.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;

public class CharacterScannerMock implements IObservableCharacterScanner {
	
	public static final int UNDEFINED= -1;
	
	private final String text;
	private final String[] textLines;
	private int currentCharIndex = UNDEFINED;
	private int currentColumnIndex = UNDEFINED;
	private int currentLineIndex = 0;
	
	
	private static final char[][] LEGAL_LINE_DELIMITERS = { { '\n' }, {'\r'}, {'\r', '\n'}  };
	
	public CharacterScannerMock(String text) {
		this.text = text;
		this.textLines = text.split("[\n|\r\n|\r]");
	}
	
	@Override
	public char[][] getLegalLineDelimiters() {
		return LEGAL_LINE_DELIMITERS;
	}

	@Override
	public int getColumn() {
		if (currentColumnIndex == UNDEFINED) {
			return 0;
		}
		return currentColumnIndex;
	}

	@Override
	public int read() {
		if (currentCharIndex + 1 < text.length()) {
			currentCharIndex++;
			currentColumnIndex++;
			
			char c = text.charAt(currentCharIndex);
			
			if (c == '\n'
					|| (c == '\r'
						&& (currentCharIndex + 1 >= text.length() || text.charAt(currentCharIndex + 1) != '\n'))) {
				currentColumnIndex = -1;
				currentLineIndex++;
			}
			return c;
		} else {
			return ICharacterScanner.EOF;
		}
	}

	@Override
	public void unread() {
		if (currentCharIndex - 1 >= -1) {
			currentCharIndex--;
			currentColumnIndex--;
			
			if (currentColumnIndex < -1) {
				currentLineIndex--;
				currentColumnIndex = textLines[currentLineIndex].length();
			}
		}
	}
	
	public String getConsumedText() {
		if (this.currentCharIndex < 0) {
			return "";
		}
		return this.text.substring(0, this.currentCharIndex + 1);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("index: ");
		builder.append(currentCharIndex);
		builder.append(", column: ");
		builder.append(currentColumnIndex);
		builder.append(", current char: ");
		Character currentChar = currentCharIndex >= 0 && currentCharIndex < text.length() ? text.charAt(currentCharIndex) : null;
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
