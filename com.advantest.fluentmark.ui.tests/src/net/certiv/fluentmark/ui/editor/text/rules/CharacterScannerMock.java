/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2022 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.text.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;

public class CharacterScannerMock implements ICharacterScanner {
	
	private final String text;
	private final String[] textLines;
	private int currentCharIndex = -1;
	private int currentColumnIndex = -1;
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
		if (currentCharIndex - 1 >= 0) {
			currentCharIndex--;
			currentColumnIndex--;
			
			if (currentColumnIndex < -1) {
				currentLineIndex--;
				currentColumnIndex = textLines[currentLineIndex].length();
			}
		}
	}

}
