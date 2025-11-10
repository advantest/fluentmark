/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.markdown.scanner;

public final class Chars {

	private Chars() {}

	/** Returns true if a character is punctuation, and false otherwise. */
	public static boolean isPunctuation(char c) {
		switch (Character.getType(c)) {
			case Character.START_PUNCTUATION:
			case Character.END_PUNCTUATION:
			case Character.OTHER_PUNCTUATION:
			case Character.CONNECTOR_PUNCTUATION:
			case Character.DASH_PUNCTUATION:
			case Character.INITIAL_QUOTE_PUNCTUATION:
			case Character.FINAL_QUOTE_PUNCTUATION:
				return true;
		}
		return false;
	}

	/** Returns true if a character is a symbol, and false otherwise. */
	public static boolean isSymbol(char c) {
		switch (Character.getType(c)) {
			case Character.MATH_SYMBOL:
			case Character.CURRENCY_SYMBOL:
			case Character.MODIFIER_SYMBOL:
			case Character.OTHER_SYMBOL:
				return true;
		}
		return false;
	}

	/** Returns true if a character is a control character, and false otherwise. */
	public static boolean isControl(char c) {
		return Character.getType(c) == Character.CONTROL;
	}

	public static boolean isSeparator(char ch) {
		switch (Character.getType(ch)) {
			case Character.SPACE_SEPARATOR:
			case Character.LINE_SEPARATOR:
			case Character.PARAGRAPH_SEPARATOR:
				return true;
		}
		return false;
	}
}
