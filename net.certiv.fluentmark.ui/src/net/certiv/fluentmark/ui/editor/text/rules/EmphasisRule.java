/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * Copyright winterwell Mathematics Ltd.
 * @author Daniel Winterstein
 * 11 Jan 2007
 */
package net.certiv.fluentmark.ui.editor.text.rules;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class EmphasisRule implements IRule {

	private static char[][] fDelimiters = null;
	private char[] fSequence;
	protected IToken fToken;

	public EmphasisRule(String marker, IToken token) {
		assert marker.equals("*") || marker.equals("**") || marker.equals("_") || marker.equals("__")
				|| marker.equals("~~") || marker.equals("`") || marker.equals("``");
		Assert.isNotNull(token);
		fSequence = marker.toCharArray();
		fToken = token;
	}

	protected boolean sequenceDetected(ICharacterScanner scanner, char[] sequence, boolean eofAllowed) {
		for (int i = 1; i < sequence.length; i++) {
			int c = scanner.read();
			if (c == ICharacterScanner.EOF && eofAllowed) {
				return true;
			} else if (c != sequence[i]) {
				// rewind; do not unread the first character.
				for (int j = i; j > 0; j--)
					scanner.unread();
				return false;
			}
		}
		return true;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		boolean sawSpaceBefore = false;
		
		if (scanner.getColumn() > 0) {
			scanner.unread();
			sawSpaceBefore = Character.isWhitespace(scanner.read());
		}

		// Should be connected only on the right side
		if (!sawSpaceBefore && scanner.getColumn() != 0) {
			return Token.UNDEFINED;
		}

		int c = scanner.read();
		// Should be connected only on right side
		if (c != fSequence[0] || !sequenceDetected(scanner, fSequence, false)) {
			scanner.unread();
			return Token.UNDEFINED;
		}
		int readCount = fSequence.length;
		if (fDelimiters == null) {
			fDelimiters = scanner.getLegalLineDelimiters();
		}
		// Start sequence detected
		int delimiterFound = 0;
		// Is it a list item marker, or just a floating *?
		if (sawSpaceBefore) {
			boolean after = Character.isWhitespace(scanner.read());
			scanner.unread();
			if (after) delimiterFound = 2;
		}

		while (delimiterFound < 2 && (c = scanner.read()) != ICharacterScanner.EOF) {
			readCount++;

			if (!sawSpaceBefore && c == fSequence[0] && sequenceDetected(scanner, fSequence, false)) {
				return fToken;
			}

			int i;
			for (i = 0; i < fDelimiters.length; i++) {
				if (c == fDelimiters[i][0] && sequenceDetected(scanner, fDelimiters[i], true)) {
					delimiterFound++;
					break;
				}
			}
			if (i == fDelimiters.length) delimiterFound = 0;
			sawSpaceBefore = Character.isWhitespace(c);
		}
		// Reached ICharacterScanner.EOF
		for (; readCount > 0; readCount--) {
			scanner.unread();
		}
		return Token.UNDEFINED;
	}
}
