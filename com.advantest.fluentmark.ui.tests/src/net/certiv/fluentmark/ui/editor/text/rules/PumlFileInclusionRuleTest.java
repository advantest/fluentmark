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

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;


public class PumlFileInclusionRuleTest {
	
	private CharacterScannerMock scanner;
	private PumlFileInclusionRule rule;
	private IToken successToken;
	
	@BeforeEach
	public void setUp() {
		successToken = new Token(MarkdownPartitions.PLANTUML_INCLUDE);
		rule = new PumlFileInclusionRule(successToken);
	}
	
	@AfterEach
	public void tearDown() {
		successToken = null;
		rule = null;
		scanner = null;
	}
	
	@Test
	public void pumlFileInclusionMatches() {
		String input = "![Some text with almost any symbol :;.,-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.puml)";
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Image inclusion {0} does not match (as expected)")
	@ValueSource(strings = {
			"![Some label](../../path/to/some_image.gif)", 
			"![any text \\[with escaped square brackets\\]](any/PATH/to/some_image.jpg)",
			"![Some text with almost any symbol (some details!)](any/PATH/to/some_image.png)"
			})
	public void imageFileInclusionDoesNotMatch(String input) {
		scanner = new CharacterScannerMock(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
		assertEquals("", scanner.getConsumedText());
	}
	
}
