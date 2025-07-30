/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown.scanner.rules;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class CodeSpanRule implements IPredicateRule {
	
	private static char[][] fDelimiters = null;
	
	protected final IToken successToken;

	public CodeSpanRule(IToken token) {
		Assert.isNotNull(token);
		successToken = token;
	}

	public IToken getSuccessToken() {
		return successToken;
	}
	
	private boolean nextCharIs(char expectedSymbol, ICharacterScanner scanner) {
		int nextChar = scanner.read();
		scanner.unread();
		
		return (nextChar == expectedSymbol);
	}
	
	protected boolean lineDelimiterFound(int currentChar, ICharacterScanner scanner) {
		for (char[] fDelimiter : fDelimiters) {
			if (currentChar == fDelimiter[0]
					&& (fDelimiter.length == 1 || nextCharIs(fDelimiter[1], scanner))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		if (fDelimiters == null) {
			fDelimiters = scanner.getLegalLineDelimiters();
		}
		
		boolean precedingBackslash = false;
		
		// read preceding character
		int column = scanner.getColumn();
		if (column > 0) {
			scanner.unread();
			int precedingChar = scanner.read();
			precedingBackslash = (precedingChar == '\\');
		}
		
		// read first character, cancel if we have no unescaped ` symbol
		int currentChar = scanner.read();
		int readCount = 1;
		
		if (currentChar != '`' || (precedingBackslash && currentChar == '`')) {
			scanner.unread();
			return Token.UNDEFINED;
		}
		
		// Read until we see the next ` symbol or end of line / file
		// We do ignore backslashes \ in code spans complying with the CommonMark specification
		// See https://spec.commonmark.org/0.31.2/#example-338
		do {
			currentChar = scanner.read();
			readCount++;
		} while (currentChar != '`' && currentChar != ICharacterScanner.EOF && !lineDelimiterFound(currentChar, scanner));
		
		// if we still have no backtick, unread all characters and return
		if (currentChar != '`') {
			while (readCount > 0) {
				scanner.unread();
				readCount--;
			}
			return Token.UNDEFINED;
		}
		
		return successToken;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		return evaluate(scanner);
	}


}
