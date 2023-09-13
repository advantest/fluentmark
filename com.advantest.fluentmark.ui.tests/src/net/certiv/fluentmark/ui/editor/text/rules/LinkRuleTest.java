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

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class LinkRuleTest {
	
	private ICharacterScanner scanner;
	private LinkRule rule;
	private IToken successToken;
	private String linkTokenKey = "Link";
	
	@Before
	public void setUp() {
		successToken = new Token(linkTokenKey);
		rule = new LinkRule(successToken);
	}
	
	@After
	public void tearDown() {
		successToken = null;
		rule = null;
		scanner = null;
	}
	
	@Test
	public void simpleHttpLinkMatches() {
		scanner = new CharacterScannerMock("[Solunar](https://www.solunar.de)");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
	}
	
	@Test
	public void simpleFileLinkMatches() {
		scanner = new CharacterScannerMock("[Some text with almost any symbol :;.,-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.puml)");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
	}
	
	@Test
	public void emptyLinkMatches() {
		scanner = new CharacterScannerMock("[Some link title]()");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
	}
	
	@Test
	public void intermediateCharsDontMatch() {
		scanner = new CharacterScannerMock("[Text]someChars(path)");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
	}
	
	@Test
	public void wrongCharCombinationDoesntMatch_HMR_102() {
		scanner = new CharacterScannerMock("[Text (more)]");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
	}
	
	
	// for reference links, see HMR-102 and https://spec.commonmark.org/0.30/#reference-link
	
	@Ignore
	@Test
	public void fullRefenceLinkMatches() {
		scanner = new CharacterScannerMock("[Link title][link path or URL]");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
	}
	
	@Ignore
	@Test
	public void collapsedRefenceLinkMatches() {
		scanner = new CharacterScannerMock("[Link label][]");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
	}
	
	@Ignore
	@Test
	public void shortcutReferenceLinkMatches() {
		scanner = new CharacterScannerMock("[Link label]");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
	}

}
