/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * @author Telmo Brugnara
 * 10 Feb 2014
 */
package net.certiv.fluentmark.ui.editor.text.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class HeaderUnderlineRule implements IRule {

	IToken successToken = null;
	
	public HeaderUnderlineRule(IToken token) {
		successToken = token;
	}
	
	public IToken evaluate(ICharacterScanner scanner) {
		int c = -1;
		int scanCount = 0;
		if (scanner.getColumn()==0) {
			do {
				c = scanner.read();
				scanCount++;
			} while (!isNewLine((char) c) && c != ICharacterScanner.EOF);
			if(c == ICharacterScanner.EOF) {
				// is not a header
				for(int i=0;i<scanCount;i++) { scanner.unread(); }
				return Token.UNDEFINED;
			}
			c = scanner.read();
			scanCount++;
			if(c == '\r') {
				c = scanner.read();
				scanCount++;
			}
			if(!isUnderline((char) c)) {
				// is not a header
				for(int i=0;i<scanCount;i++) { scanner.unread(); }
				return Token.UNDEFINED;
			}
			do {
				c = scanner.read();
				scanCount++;
				if(isNewLine((char) c) || c == ICharacterScanner.EOF) {
					//scanner.unread();
					return successToken;
				}
				if(!isUnderline((char) c) && !isWhitespace((char) c) && c != '\r') {
					// is not a header
					for(int i=0;i<scanCount;i++) { scanner.unread(); }
					return Token.UNDEFINED;
				}
			} while (true);
		}

		return Token.UNDEFINED;
	}
	
	boolean isNewLine(char c) {
		return c == '\n';
	}

	boolean isUnderline(char c) {
		return c == '=' || c == '-';
	}

	boolean isWhitespace(char c) {
		return c == ' ' || c == '\t';
	}
}
