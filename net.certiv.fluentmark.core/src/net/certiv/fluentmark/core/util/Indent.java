/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.util;

public class Indent {

	/**
	 * Returns the indentation of the given line in tab equivalents. Partial tabs are not counted.
	 */
	public static int measureIndentInTabs(String line, int tabWidth) {
		int spaces = measureIndentInSpaces(line, tabWidth);
		return spaces / tabWidth;
	}

	/**
	 * Returns the indentation of the given line in space equivalents.
	 * <p>
	 * Tab characters are counted using the given <code>tabWidth</code> and every other indent
	 * character as one. This method analyzes the content of <code>line</code> up to the first
	 * non-whitespace character.
	 *
	 * @param line the string to measure the indent of
	 * @param tabWidth the width of one tab in space equivalents
	 * @return the measured indent width in space equivalents
	 * @exception IllegalArgumentException if:
	 *                <ul>
	 *                <li>the given <code>line</code> is null</li>
	 *                <li>the given <code>tabWidth</code> is lower than zero</li>
	 *                </ul>
	 */
	public static int measureIndentInSpaces(String line, int tabWidth) {
		if (tabWidth < 0 || line == null) throw new IllegalArgumentException();

		int length = 0;
		int max = line.length();
		for (int i = 0; i < max; i++) {
			char ch = line.charAt(i);
			if (ch == '\t') {
				int reminder = length % tabWidth;
				length += tabWidth - reminder;
			} else if (isIndentChar(ch)) {
				length++;
			} else {
				return length;
			}
		}
		return length;
	}

	/**
	 * Returns <code>true</code> if the given character is an indentation character. Indentation
	 * character are all whitespace characters except the line delimiter characters.
	 *
	 * @param ch the given character
	 * @return Returns <code>true</code> if this the character is a indent character,
	 *         <code>false</code> otherwise
	 */
	public static boolean isIndentChar(char ch) {
		return Character.isWhitespace(ch) && !isLineDelimiterChar(ch);
	}

	/**
	 * Returns <code>true</code> if the given character is a line delimiter character.
	 *
	 * @param ch the given character
	 * @return Returns <code>true</code> if this the character is a line delimiter character,
	 *         <code>false</code> otherwise
	 */
	private static boolean isLineDelimiterChar(char ch) {
		return ch == '\n' || ch == '\r';
	}

}
