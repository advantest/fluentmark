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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.advantest.fluentmark.tests.text.rules.IObservableCharacterScanner;
import com.advantest.fluentmark.tests.text.rules.ScannerTools;


public class LinkRuleIT {
	
	private IObservableCharacterScanner scanner;
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
	
	private IObservableCharacterScanner createScanner(String input) {
		return ScannerTools.createMarkdownScanner(input);
	}
	
	@ParameterizedTest(name = "[{index}] Link {0} is successfully parsed")
	@ValueSource(strings = { "[Solunar](https://www.solunar.de)",
			"[Some text with almost any symbol :;.,-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.puml)"})
	public void simpleHttpLinkMatches(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@Test
	public void emptyLinkMatches() {
		String input = "[Some link title]()";
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@Test
	public void intermediateCharsDontMatch() {
		scanner = createScanner("[Text]someChars(path)");
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals("[Text]", scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] File link {0} is successfully parsed")
	@ValueSource(strings = {
			"[Image](some/path/to/file.png)",
			"[File X](some/path/to/file.txt)",
			"[More to see here!](../../relative/path/SomeClass.java)",
			"[](../../../Test Markdown and PlantUML/doc/subsection/section.md)",
			"[Some text with almost any symbol :;.,-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.cpp)"})
	public void fileLinksDoMatch(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Image / Link {0} is successfully parsed")
	@ValueSource(strings = {
			"![Image](some/path/to/file.png)",
			"![Diagram X](some/path/to/file.svg)",
			"![More to see here!](../../relative/path/diagram.puml)",
			"![](../../../Test Markdown and PlantUML/doc/subsection/diag.puml)",
			"![Some text with almost any symbol :;.,-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.puml)"
	})
	public void imageLinksDoMatch(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Text {0} should not be matched as a link")
	@ValueSource(strings = {
			"[Solunar\\](https://www.solunar.de)",
			"\\[label](some/path/to/a_file.puml)",
			"\\[link-like text 1\\](https://www.something1.com)",
			"\\[link-like text 2](https://www.something2.com)",
			"[link-like text 3\\](https://www.something3.com)"
	})
	public void stringsNotMatchedAsLinks(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
		assertEquals("", scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Text {0} should not be matched as a link")
	@ValueSource(strings = {
			"[label]:",
			"[label]: ",
			"[label]: \n\n some/path/to/a_file.puml",
			"[label]: \n \t \n https://www.advantest.com"
	})
	public void emptyLinkReferenceDefinitionsMatchedAsLinks(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals("[label]:", scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Text {0} should not be matched as a link")
	@CsvSource({
			"[link-like text 4]\\(https://www.something-else-1.com\\),[link-like text 4]",
			"[link-like text 5]\\(https://www.something-else-2.com),[link-like text 5]",
			"[link-like text 6](https://www.something-else-3.com\\),[link-like text 6]"})
	public void stringsNotMatchedAsCompleteLinksButAsRefLinks(String inputText, String consumedText) {
		scanner = createScanner(inputText);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(consumedText, scanner.getConsumedText());
	}
	
	// for reference links, see HMR-102 and https://spec.commonmark.org/0.30/#reference-link
	
	@ParameterizedTest(name = "[{index}] Full reference link {0} is successfully parsed")
	@ValueSource(strings = {
			"[Link title][link label]",
			"[Some longer text! Yes! With special characters !?=)/(//%$§\"!°.#'][special]",
			"[text][key]",
			"[Some \\] escaped brackets \\[ are ignored here][REF]"})
	public void fullRefenceLinkMatches(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@Test
	public void collapsedRefenceLinkMatches() {
		String input = "[Link label][]";
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@Test
	public void shortcutReferenceLinkMatches() {
		String input = "[Link label]";
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	// for link reference definitions see https://spec.commonmark.org/0.30/#link-reference-definition
	
	@ParameterizedTest(name = "[{index}] Link reference definition {0} is successfully parsed")
	@ValueSource(strings = {
				"[adv]: https://www.advantest.com",
				"[adv]:https://www.advantest.com",
				"[adv]:\nhttps://www.advantest.com",
				"[adv]:  \n \t  https://www.advantest.com",
				"[]: https://plantuml.com",
				"[Some \\\\] escaped brackets \\\\[ are ignored here]: REF",
				"[adv]:\nhttps://www.advantest.com",
				"[key]:",
				"[]:"
	})
	public void linkReferenceDefinitionsMatch(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Link reference definition {0} is successfully parsed")
	@ValueSource(strings = {
				"[key]:\n\n"
	})
	public void linkReferenceDefinitionsMatchWithoutTralingWhitespace(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input.trim(), scanner.getConsumedText());
	}
	
	@Test
	public void linkReferenceDefinitionsMatchWithoutLabel() {
		String match = "[adv]: https://www.advantest.com";
		String input = match + " \"Advantest Europe\"";
		scanner = createScanner(input);
		
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
		scanner = createScanner(input);
		
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
	public void footnoteDefinitionsNotParsedAsLinkReferenceDefinitions(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
		assertEquals("", scanner.getConsumedText());
	}
}
