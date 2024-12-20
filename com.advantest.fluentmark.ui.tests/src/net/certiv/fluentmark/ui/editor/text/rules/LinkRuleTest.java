/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.text.rules;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.advantest.fluentmark.tests.text.rules.CharacterScannerMock;


public class LinkRuleTest {
	
	private CharacterScannerMock scanner;
	private LinkRule rule;
	private IToken successToken;
	private String linkTokenKey = "Link";
	
	@BeforeEach
	public void setUp() {
		successToken = new Token(linkTokenKey);
		rule = new LinkRule(successToken);
	}
	
	@AfterEach
	public void tearDown() {
		successToken = null;
		rule = null;
		scanner = null;
	}
	
	@ParameterizedTest(name = "[{index}] Link {0} is successfully parsed")
	@ValueSource(strings = { "[Solunar](https://www.solunar.de)",
			"[Some text with almost any symbol :;.,-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.puml)"})
	public void simpleHttpLinkMatches(String input) {
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@Test
	public void emptyLinkMatches() {
		String input = "[Some link title]()";
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@Test
	public void intermediateCharsDontMatch() {
		scanner = new CharacterScannerMock("[Text]someChars(path)");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals("[Text]", scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Image / Link {0} is successfully parsed")
	@ValueSource(strings = {
			"![Image](some/path/to/file.png)",
			"![Diagram X](some/path/to/file.svg)",
			"![More to see here!](../../relativ/path/diagram.puml)",
			"![Some text with almost any symbol :;.,-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.puml)"})
	public void imageLinksDoMatch(String input) {
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Text {0} should not be matched as a link")
	@ValueSource(strings = { "[Solunar\\](https://www.solunar.de)",
			"\\[label](some/path/to/a_file.puml)",
			"[label]:",
			"[label]: ",
			"[label]: \n\n some/path/to/a_file.puml",
			"[label]: \n \t \n https://www.advantest.com"})
	public void stringsNotMatchedAsLinks(String input) {
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
		assertEquals("", scanner.getConsumedText());
	}
	
	
	// for reference links, see HMR-102 and https://spec.commonmark.org/0.30/#reference-link
	
	@ParameterizedTest(name = "[{index}] Full reference link {0} is successfully parsed")
	@ValueSource(strings = {
			"[Link title][link label]",
			"[Some longer text! Yes! With special characters !?=)/(//%$§\"!°.#'][special]",
			"[text][key]",
			"[Some \\] escaped brackets \\[ are ignored here][REF]"})
	public void fullRefenceLinkMatches(String input) {
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@Test
	public void collapsedRefenceLinkMatches() {
		String input = "[Link label][]";
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@Test
	public void shortcutReferenceLinkMatches() {
		String input = "[Link label]";
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	// for link reference definitions see https://spec.commonmark.org/0.30/#link-reference-definition
	
	@ParameterizedTest(name = "[{index}] Link reference definition {0} is successfully parsed")
	@ValueSource(strings = {
				"[adv]: https://www.advantest.com",
				"[adv]:https://www.advantest.com",
				"[adv]:\nhttps://www.advantest.com"})
	public void linkReferenceDefinitionsMatch(String input) {
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@Test
	public void linkReferenceDefinitionsMatchWithoutLabel() {
		String match = "[adv]: https://www.advantest.com";
		String input = match + " \"Advantest Europe\"";
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(match, scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Text with escaped brackets {0} doesn't match as a link")
	@ValueSource(strings = {
				"[\\]()",
				"\\[label](link)",
				"[label\\](link)",
				"[\\]",
				"\\[]",
				"\\[][key\\]",
				"\\[Solunar](https://www.solunar.de)",
				"\\[adv]: https://www.advantest.com",
				"[adv\\]:https://www.advantest.com",
				"[adv\\]:\nhttps://www.advantest.com"})
	public void escapedBracketsDontMatch(String input) {
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
		assertEquals("", scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Footnote definition {0} is not parsed to a link reference definition")
	@ValueSource(strings = {
				"[^1]: Footnote 1",
				"[^9731]:Another footnote",
				"[^footnote]: Some explaining text.",
				"[^adv]:https://www.advantest.com",
				"[^key]:\nhttps://www.advantest.com"})
	public void footnoteDefinitionsNotParsedAslinkReferenceDefinitions(String input) {
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
		assertEquals("", scanner.getConsumedText());
	}
}
