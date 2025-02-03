/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.editor.text.rules;

import java.util.Arrays;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/** Symbols consist of '\' followed by one charecter. */
public class MathSymbolRule implements IPredicateRule {

	private static final char mark = '\\';
	private static final char[] attribs = { '\\', '_', '$', ' ', '\n', '\t', '\r', '{', '}', '!', '.', ',', '?', '"',
			'�', '%', '^', '&', '*', ':', ';', '@', '\'', '#', '~', '/', '+', '|', '<', '>', '=', (char) 65535,
			// '-', '[', ']', '(', ')',
	};

	private IToken success;

	public MathSymbolRule(IToken success) {
		this.success = success;
		Arrays.sort(attribs);
	}

	public IToken getSuccessToken() {
		return this.success;
	}

	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		if (resume) {
			return evaluateNext(scanner) ? success : Token.UNDEFINED;
		} else {
			return evaluate(scanner);
		}
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();
		if (c == mark && evaluateNext(scanner)) return success;

		scanner.unread();
		return Token.UNDEFINED;
	}

	private boolean evaluateNext(ICharacterScanner scanner) {
		int c = scanner.read();
		if (c != ICharacterScanner.EOF) {
			if (Arrays.binarySearch(attribs, (char) c) >= 0) return true;
			scanner.unread();
		}
		return false;
	}
}
