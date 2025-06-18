/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.editor.text.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.preferences.Prefs;

public class IndentedCodeRule implements IPredicateRule {

	private IToken token;
	private boolean found;
	private int tabWidth;
	private int line;

	public IndentedCodeRule(IToken token) {
		this.token = token;
	}

	// called from the partition scanner
	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		if (resume) Log.error("Resuming");
		return evaluate(scanner);
	}

	// called from the reconcile scanners
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		found = false;

		int c = scanner.read();
		scanner.unread();
		if (isHws(c)) {
			int col = scanner.getColumn();
			if (col == 1) {
				scanner.unread();
				int p = scanner.read();
				if (p == '\n') col = 0;
			}
			if (col == 0) {
				tabWidth = FluentUI.getDefault().getPreferenceStore().getInt(Prefs.EDITOR_TAB_WIDTH);
				if (tabWidth < 2) tabWidth = 4;
				line = 0;
				while (evaluateLine(scanner)) {
					found = true;
					line++;
				}
			}
		}
		if (found) return token;
		return Token.UNDEFINED;
	}

	@Override
	public IToken getSuccessToken() {
		return token;
	}

	private boolean evaluateLine(ICharacterScanner scanner) {
		boolean found = false;
		String indents = "";
		int mark = scanner.getColumn();			// mark BOL
		int c = scanner.read();

		if (isEol(c)) {							// stop if blank line
			rewindToMark(scanner, mark);		// rewind to BOL
			return false;
		}

		while (isHws(c)) {						// collect the indent
			indents += (char) c;
			c = scanner.read();
			if (isEol(c) && line == 0) {		// stop if first line is blank
				rewindToMark(scanner, mark);	// rewind to BOL
				return false;
			}
		}
		scanner.unread();

		if (line == 0) {						// check only first line of codeblock
			if (isList(scanner)) {				// stop if a list
				rewindToMark(scanner, mark);	// rewind to BOL
				return false;
			}
		}

		if (isCodeIndent(indents)) {			// consume through EOL if code indent
			found = true;
			gotoEOL(scanner, true);
		} else {								// otherwise rewind to BOL
			rewindToMark(scanner, mark);
		}
		return found;
	}

	// rewind on false is handled in #evaluateLine
	// otherwise, winds to the beginning of the blank line at the EOB
	private boolean isList(ICharacterScanner scanner) {
		int n = scanner.read();
		if (isEol(n)) { // blank line
			scanner.unread();
			return false;
		}

		if (n == '-' || n == '+' || n == '*') {
			if (spaceFollows(scanner)) {			// skip unordered lists
				scanner.unread();
				return true;
			}
			scanner.unread();
			return false;
		}
		if (Character.isDigit(n)) {
			if (isDigitSequenceFollowedBySpace(scanner)) {		// skip ordered lists
				scanner.unread();
				return true;
			}
		}
		scanner.unread();
		return false;
	}
	
	private boolean isDigitSequenceFollowedBySpace(ICharacterScanner scanner) {
		int currentIndex = scanner.getColumn();
		readDigits(scanner);
		
		if (spaceFollows(scanner)) {
			rewindToMark(scanner, currentIndex);
			return true;
		}
		
		rewindToMark(scanner, currentIndex);
		return false;
	}


	private void readDigits(ICharacterScanner scanner) {
		int d;
		do {
			d = scanner.read();
		} while (Character.isDigit(d));
		scanner.unread();
	}

	private boolean gotoEOL(ICharacterScanner scanner, boolean consumeEOL) {
		boolean blank = true;
		int b = scanner.read();
		while (!isEol(b)) {
			blank = blank && Character.isWhitespace(b);
			b = scanner.read();
		}
		scanner.unread();
		if (consumeEOL) {
			b = scanner.read();
			if (b == '\r') {
				b = scanner.read();
			}
			if (b == ICharacterScanner.EOF) scanner.unread();
		}
		return blank;
	}

	private boolean spaceFollows(ICharacterScanner scanner) {
		int s = scanner.read();
		scanner.unread();
		if (s == ICharacterScanner.EOF) return false;
		if (!isHws(s)) return false;
		return true;
	}

	private void rewindToMark(ICharacterScanner scanner, int mark) {
		for (int idx = scanner.getColumn(); idx > mark; idx--) {
			scanner.unread();
		}
	}

	private boolean isHws(int c) {
		return c == ' ' || c == '\t';
	}

	private boolean isEol(int c) {
		return c == '\r' || c == '\n' || c == ICharacterScanner.EOF;
	}

	private boolean isCodeIndent(String indents) {
		// exclude lists & quotes
		int level = net.certiv.fluentmark.core.util.Indent.measureIndentInSpaces(indents, tabWidth);
		if (level >= 4) return true;
		return false;
	}
}
