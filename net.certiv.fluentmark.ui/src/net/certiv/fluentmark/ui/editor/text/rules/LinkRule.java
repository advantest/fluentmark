/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.editor.text.rules;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * @author Amir Pakdel (initial implementation)
 */
public class LinkRule implements IRule {

	private static char[][] fDelimiters = null;
	protected IToken fToken;

	public LinkRule(IToken token) {
		Assert.isNotNull(token);
		fToken = token;
	}

	public IToken getSuccessToken() {
		return fToken;
	}

	protected boolean sequenceDetected(ICharacterScanner scanner, char[] sequence, boolean eofAllowed) {
		for (int i = 1; i < sequence.length; i++) {
			int c = scanner.read();
			if (c == ICharacterScanner.EOF && eofAllowed) {
				return true;
			} else if (c != sequence[i]) {
				// rewind; do not unread the first character.
				scanner.unread();
				for (int j = i - 1; j > 0; j--)
					scanner.unread();
				return false;
			}
		}
		return true;
	}
	
	protected boolean lineDelimiterFound(int currentChar, ICharacterScanner scanner) {
		for (char[] fDelimiter : fDelimiters) {
			if (currentChar == fDelimiter[0] && sequenceDetected(scanner, fDelimiter, true)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();
		
		boolean firstCharIsExclamationMark = (c == '!');
		
		// try reading any URI without any brackets
		if (c != '[' && c != '!') {
			if ((c != 'h' || (!sequenceDetected(scanner, "http://".toCharArray(), false)
					&& !sequenceDetected(scanner, "https://".toCharArray(), false)))
					&& (c != 'f' || !sequenceDetected(scanner, "ftp://".toCharArray(), false))) {
				// Not even a non-standard link
				if (c != ICharacterScanner.EOF) {
					scanner.unread();
				}
				return Token.UNDEFINED;
			}

			// + preventing NPE (Non-standard link should not be below as comment above suggests) by
			// Paul Verest
			if (fDelimiters == null) {
				scanner.unread();
				return Token.UNDEFINED;
			}

			// Non-standard link
			c = scanner.read();
			while (c != ICharacterScanner.EOF && !Character.isWhitespace(c)) {
				if (lineDelimiterFound(c, scanner)) {
					return fToken;
				}
				c = scanner.read();
			}
			return fToken;
		}
		
		if (fDelimiters == null) {
			fDelimiters = scanner.getLegalLineDelimiters();
		}
		
		int readCount = 1;
		int lastChar;
		
		if (firstCharIsExclamationMark) {
			// we might have an image link, try to read '[' as next char
			lastChar = c;
			c = scanner.read();
			readCount++;
			
			// no, we have no image link; unread both chars and stop
			if (c != '[') {
				scanner.unread();
				scanner.unread();
				return Token.UNDEFINED;
			}
		}

		// We've read a '[' before. Now, we search for '](' and then for ')' or
		// for '][' and later ']' or for ']' only.
		// See https://spec.commonmark.org/0.30/#links,
		// https://spec.commonmark.org/0.30/#reference-link,
		// and https://spec.commonmark.org/0.30/#link-reference-definition
		
		// find next non-escaped ']' or end of String
		
		do {
			lastChar = c;
			c = scanner.read();
			readCount++;
		} while (c != ICharacterScanner.EOF && !(c == ']' && lastChar != '\\') && !lineDelimiterFound(c, scanner));
		
		// we didn't find an unescaped ']'? Then cancel.
		if (c != ']' || lastChar == '\\') {
			// un-read read chars
			if (c != ICharacterScanner.EOF) {
				while (readCount > 0) {
					scanner.unread();
					readCount--;
				}
			}
			return Token.UNDEFINED;
		}
		
		// We've read something like '[label]' or ![label]. If we didn't read '!', then that's likely a link, thus, we will return fToken because of a successful scan.
		// '[label]' might be a shortcut reference link,
		// but there may be some more characters following which belong to the same link, e.g. one of the following:
		// ![label](path/to/image.svg)
		// [label](https://www.advantest.com)
		// [label](https://www.advantest.com "Link title")
		// [label][key]
		// [key][]
		// [label]: https://www.advantest.com
		// [label]: https://www.advantest.com "Link title"
		
		int readCountFirstClosingSquareBracket = readCount;
		
		// read next char
		lastChar = c;
		c = scanner.read();
		readCount++;
		
		// return if nothing expected is coming
		if (c != ':' && c != '(' && c != '[') {
			if (c != ICharacterScanner.EOF) {
				scanner.unread();
			}
			return fToken;
		}
		
		if (c == '(') {
			// try finding the remainder of the URI or path
			do {
				lastChar = c;
				c = scanner.read();
				readCount++;
			} while (c != ICharacterScanner.EOF && !(c == ')' && lastChar != '\\'));
			
			// we didn't find an unescaped ')'? Then un-read the chars and return.
			if (c != ')' || lastChar == '\\') {
				// un-read chars read after first ']'
				while (readCount > readCountFirstClosingSquareBracket) {
					scanner.unread();
					readCount--;
				}
				return fToken;
			} else {
				return fToken;
			}
		} else if (c == '[') {
			if (firstCharIsExclamationMark) {
				// We read something like "![label][". That can't be an image since we expected a "(" instead of "[".
				// un-read all chars that we read before and cancel
				while (readCount > 0) {
					scanner.unread();
					readCount--;
				}
				return Token.UNDEFINED;
			}
			
			// try finding the closing bracket: ']'
			do {
				lastChar = c;
				c = scanner.read();
				readCount++;
			} while (c != ICharacterScanner.EOF && !(c == ']' && lastChar != '\\'));
			
			// we didn't find an unescaped ']'? Then un-read the chars and return.
			if (c != ']' || lastChar == '\\') {
				// un-read chars read after first ']'
				while (readCount > readCountFirstClosingSquareBracket) {
					scanner.unread();
					readCount--;
				}
				return fToken;
			} else {
				return fToken;
			}
		} else { // case: c == ':'
			if (firstCharIsExclamationMark) {
				// We read something like "![label]:". That can't be an image since we expected a "(" instead of ":".
				// un-read all chars that we read before and cancel
				while (readCount > 0) {
					scanner.unread();
					readCount--;
				}
				return Token.UNDEFINED;
			}
			
			// try reading until we find the first non-whitespace char
			int lineBreaks = 0;
			do {
				lastChar = c;
				c = scanner.read();
				readCount++;
				
				// check if we have found a line break and count it
				for (int i = 0; i < fDelimiters.length; i++) {
					if ( c == fDelimiters[i][0]) {
						if (fDelimiters[i].length == 1) {
							lineBreaks++;
						} else { // we have a two-char line break, i.e. \r\n
							// look ahead
							int next = scanner.read();
							scanner.unread();
							
							if (next == fDelimiters[i][1] && fDelimiters.length == 2) {
								lineBreaks++;
							}
						}
					}
				}
			} while (c != ICharacterScanner.EOF && Character.isWhitespace(c) && lineBreaks < 2);
			
			// do we have a non-whitespace char now?
			if (lineBreaks > 1 || c == ICharacterScanner.EOF || Character.isWhitespace(c)) {
				// un-read read chars
				while (readCount > 0) {
					scanner.unread();
					readCount--;
				}
				
				return Token.UNDEFINED;
			}
			
			// now, let's read the link reference definition's URI or path
			do {
				lastChar = c;
				c = scanner.read();
				readCount++;
			} while (c != ICharacterScanner.EOF && !Character.isWhitespace(c));
			
			if (Character.isWhitespace(c)) {
				scanner.unread();
			}
			
			return fToken;
		}
	}
	
}
