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

/**
 * @author Daniel Winterstein
 */
public class ListRule implements IRule {

	// private ArrayList<Integer> markerList;
	protected IToken fToken;

	public ListRule(IToken token) {
		Assert.isNotNull(token);
		fToken = token;
	}

	/*
	 * @see IRule#evaluate(ICharacterScanner)
	 * 
	 * @since 2.0
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		if (scanner.getColumn() != 0) {
			return Token.UNDEFINED;
		}
		// // Fast mode
		// if (scanner.read() != '-') {
		// scanner.unread();
		// return Token.UNDEFINED;
		// }
		// if (Character.isWhitespace(scanner.read())) {
		// return fToken;
		// }
		// scanner.unread();
		// scanner.unread();
		// return Token.UNDEFINED;
		// // Fast mode
		int readCount = 0;
		int c;
		while ((c = scanner.read()) != ICharacterScanner.EOF) {
			readCount++;
			if (!Character.isWhitespace(c)) {
				int after = scanner.read();
				// readCount++;
				scanner.unread();
				// if ( markerList.contains(c) && Character.isWhitespace( after ) ) {
				if ((c == '-' || c == '+' || c == '*') && Character.isWhitespace(after)) {
					return fToken;
				} else {
					for (; readCount > 0; readCount--)
						scanner.unread();
					return Token.UNDEFINED;
				}
			}
		}
		// Reached ICharacterScanner.EOF
		for (; readCount > 0; readCount--)
			scanner.unread();
		return Token.UNDEFINED;
	}
}
