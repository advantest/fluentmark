/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.text.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;

public class RuleTools {

	public static boolean charIsEofOrLineEnding(ICharacterScanner scanner, int charAsInt) {
		if (charAsInt == ICharacterScanner.EOF) {
			return true;
		}
		
		char[][] legalLineDelimiters = scanner.getLegalLineDelimiters();
		for (char[] lineDelimiter : legalLineDelimiters) {
			if (lineDelimiter.length == 1 && charAsInt == lineDelimiter[0]) {
				return true;
			}
			
			if (lineDelimiter.length == 2) {
				boolean charsEqual = (charAsInt == lineDelimiter[0]);
				if (charsEqual) {
					int nextChar = scanner.read();
					charsEqual = (nextChar == lineDelimiter[1]);
					scanner.unread();
				}
				
				if (charsEqual) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean sequenceFound(ICharacterScanner scanner, String sequence) {
		boolean success = tryReadingSequence(scanner, sequence);
		if (success) {
			unreadCharacters(scanner, sequence.length());
		}
		return success;
	}
	
	public static boolean tryReadingSequence(ICharacterScanner scanner, String sequence) {
		int charAsInt;
		char[] characterSequence = sequence.toCharArray();
		
		for (int i = 0; i < characterSequence.length; i++) {
			charAsInt = scanner.read();
			
			if (charAsInt != characterSequence[i]) {
				unreadCharacters(scanner, i + 1);
				return false;
			}
		}
		
		return true;
	}
	
	public static void unreadCharacters(ICharacterScanner scanner, int numberOfCharactersToUnread) {
		for (int i = 1; i <= numberOfCharactersToUnread; i++) {
			scanner.unread();
		}
	}
	
}
