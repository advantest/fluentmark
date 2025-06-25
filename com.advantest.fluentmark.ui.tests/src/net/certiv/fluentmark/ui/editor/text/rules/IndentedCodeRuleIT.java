/*)
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
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

import com.advantest.fluentmark.tests.text.rules.IObservableCharacterScanner;
import com.advantest.fluentmark.tests.text.rules.ScannerTools;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;


public class IndentedCodeRuleIT {
	
	private IObservableCharacterScanner scanner;
	private IndentedCodeRule rule;
	private IToken successToken;
	
	@BeforeEach
	public void setUp() {
		successToken = new Token(MarkdownPartitions.CODEBLOCK);
		rule = new IndentedCodeRule(successToken);
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
	
	@ParameterizedTest(name = "[{index}] Single indented line {0} does match as a code block")
	@ValueSource(strings = {
			"    Arbitrary text with at least 4 preceeding spaces...",
			"     More than 4 spaces.",
			"                                                          Far more than 4 spaces.",
			"    \t!\"§$%&/()=?`´²³{[]}ß\\~+*'#.:,;µ-_<>| Special characters...",
			"    ![Some label](../../path/to/some_image.gif)",
			"    ![diagram](path/to/diagram.puml)"
			})
	public void indentedSingleLineTextMatches(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(input, scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Multi-line indented code block does match")
	@ValueSource(strings = {
			"""
			line 1
			line 2
			""",
			"""
			<html>
			    <header></header>
			    <body>
			    </body>
			</html>
			""",
			"""
			{"image": { 
			    "src": "images/image1.png",
			    "name": "my_diagram1",
			    "width": 250,
			    "height": 150,
			    "alignment": "center"
			    }
			}
			""",
			"""
			# Markdown is a mark-up language
			
			Here comes a paragraph with a [link](https://plantuml.com)
			spanning two lines.
			
			```
			This is a code block
			in a code block.
			```
			
			Some more text following.
			"""
			})
	public void indentedCodeBlockMatches(String input) {
		String expectedText = input.indent(4);
		scanner = createScanner(expectedText);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(expectedText, scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] Indented text {0} with less than 3 preceeding spaces does not match")
	@ValueSource(strings = {
			" ![Some label](../../path/to/some_image.gif)", 
			"  ![any text \\[with escaped square brackets\\]](any/PATH/to/some_image.jpg)",
			"   ![Some text with almost any symbol (some details!)](any/PATH/to/some_image.png)",
			"""
			  text in line 1
			  text in line 2
			""",
			"""
			  - item 1
			  - item 2
			""",
			"""
			  1 item a
			  2 item b
			  10 item c
			""",
			"""
			   A
			   B
			   C
			"""
			})
	public void textIndendedLessThanFourSpacesDoesNotMatch(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
		assertEquals("", scanner.getConsumedText());
	}
	
	@ParameterizedTest(name = "[{index}] list does not match")
	@ValueSource(strings = {
			"""
			    - item 1
			    - item 2
			""",
			"""
			    1 item a
			    2 item b
			    10 item c
			""",
			"""
			    134 item a
			    135 item b
			    136 item c
			"""
			})
	public void listsDoNotMatch(String input) {
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
		assertEquals("", scanner.getConsumedText());
	}
	
	@Test
	public void codeBlockEndsWithUnindentedNonEmptyLine() {
		String input = """
				    Some code
				    Another code line
				      more indented line
				    another line of code
				   First paragraph line with less than 4 spaces of indentation.
				""";
		String expected = """
			    Some code
			    Another code line
			      more indented line
			    another line of code
			""";
		
		scanner = createScanner(input);
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(successToken, resultToken);
		assertEquals(expected, scanner.getConsumedText());
	}
	
	@Test
	public void noCodeBlockWithNonEmptyPrecedingLine() {
		String input = """
				   non empty non-code-block line
				    Some code
				    Another code line
				""";
		
		scanner = ScannerTools.createMarkdownScanner(input, "   non empty non-code-block line\n".length());
		
		IToken resultToken = rule.evaluate(scanner);
		
		assertEquals(Token.UNDEFINED, resultToken);
	}
}
