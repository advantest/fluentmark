/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2022 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.text.rules;

import static net.certiv.fluentmark.ui.editor.text.rules.RuleTools.charIsEofOrLineEnding;
import static net.certiv.fluentmark.ui.editor.text.rules.RuleTools.sequenceFound;
import static net.certiv.fluentmark.ui.editor.text.rules.RuleTools.tryReadingSequence;
import static net.certiv.fluentmark.ui.editor.text.rules.RuleTools.unreadCharacters;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class PumlFileInclusionRule implements IPredicateRule {
	
	protected IToken successToken;
	
	public PumlFileInclusionRule(IToken successToken) {
		Assert.isNotNull(successToken);
		this.successToken = successToken;
	}
	
	@Override
	public IToken getSuccessToken() {
		return successToken;
	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		return evaluate(scanner);
	}
	
	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		// read "!["
		if (!tryReadingSequence(scanner, "![")) {
			return Token.UNDEFINED;
		}

		// read any character until "]" or end of file/line reached
		int charactersRead = 0;
		int charAsInt;
		do {
			charAsInt = scanner.read();
			charactersRead++;
		} while (!charIsEofOrLineEnding(scanner, charAsInt) && charAsInt != ']');
		
		// rewind and abort in case of end of file or line ending
		if (charIsEofOrLineEnding(scanner, charAsInt)) {
			unreadCharacters(scanner, charactersRead);
			return Token.UNDEFINED;
		}
		
		// read "("
		charAsInt = scanner.read();
		charactersRead++;
		
		if (charAsInt != '(') {
			unreadCharacters(scanner, charactersRead);
			return Token.UNDEFINED;
		}
		
		// read any file name until ".puml)"
		do {
			charAsInt = scanner.read();
			charactersRead++;
		} while (charIsValidFilePathCharacter(charAsInt)
				&& !( charAsInt == '.' && sequenceFound(scanner, "puml)") ) );
		
		boolean expectedEnd = tryReadingSequence(scanner, "puml)");
		if (!expectedEnd) {
			unreadCharacters(scanner, charactersRead);
			return Token.UNDEFINED;
		}
		
		return successToken;
	}
	
	private boolean charIsValidFilePathCharacter(int charAsInt) {
		return (Character.isLetterOrDigit(charAsInt)
				|| charAsInt == '_'
				|| charAsInt == '-'
				|| charAsInt == ' '
				|| charAsInt == '.'
				|| charAsInt == '/');
	}
	
}
