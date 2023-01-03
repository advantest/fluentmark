/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2022 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.editor.text.rules;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.certiv.fluentmark.ui.editor.Partitions;
import net.certiv.fluentmark.ui.editor.text.rules.PumlFileInclusionRule;

public class PumlFileInclusionRuleTest {
	
	private ICharacterScanner scanner;
	private PumlFileInclusionRule rule;
	private IToken successToken;
	
	@Before
	public void setUp() {
		successToken = new Token(Partitions.PLANTUML_INCLUDE);
		rule = new PumlFileInclusionRule(successToken);
	}
	
	@After
	public void tearDown() {
		successToken = null;
		rule = null;
		scanner = null;
	}
	
	@Test
	public void pumlFileInclusionMatches() {
		scanner = new CharacterScannerMock("![Some text with almost any symbol :;.,-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.puml)");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
	}
	
	@Test
	public void imageFileInclusionDoesNotMatch() {
		scanner = new CharacterScannerMock("![Some text with almost any symbol (some details!)](any/PATH/to/some_image.png)");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
	}
	
}
