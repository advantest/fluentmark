/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class MarkdownParsingToolsTest {
	
	private String getLabel(RegexMatch match) {
		RegexMatch labelMatch = match.subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_LABEL);
		if (labelMatch != null) {
			return labelMatch.matchedText;
		}
		return null;
	}
	
	private String getTarget(RegexMatch match) {
		RegexMatch targetMatch = match.subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_TARGET);
		if (targetMatch != null) {
			return targetMatch.matchedText;
		}
		return null;
	}
	
	@ParameterizedTest
	@CsvSource({
			"[label](target),label,target",
			"[Some link title](),Some link title,''",
			"[](https://something.com),'',https://something.com",
			"[](),'',''",
			"[Solunar](https://www.solunar.de),Solunar,https://www.solunar.de",
			"[Some text with almost any symbol :;.-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.puml),Some text with almost any symbol :;.-_<>!\"§$%&/()=?`´´#')\\{},some/path/to/a_file.puml",
			"[Image](some/path/to/file.png),Image,some/path/to/file.png",
			"[File X](some/path/to/file.txt),File X,some/path/to/file.txt",
			"[More to see here!](../../relative/path/SomeClass.java),More to see here!,../../relative/path/SomeClass.java",
			"[](../../../Test Markdown and PlantUML/doc/subsection/section.md),'',../../../Test Markdown and PlantUML/doc/subsection/section.md",
			"[Some text with almost any symbol :;.-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.cpp),Some text with almost any symbol :;.-_<>!\"§$%&/()=?`´´#')\\{},some/path/to/a_file.cpp",
			"[Text \\] with escaped \\[ brackets](target),Text \\] with escaped \\[ brackets,target",
			"[Text with escaped brackets](target with \\( escaped \\) brackets),Text with escaped brackets,target with \\( escaped \\) brackets",
			"[Text \\] with escaped \\[ brackets](target with \\( escaped \\) brackets),Text \\] with escaped \\[ brackets,target with \\( escaped \\) brackets"
	})
	public void linksAndImagesAreFound(String statement, String label, String target) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinksAndImages(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(statement, match.get().matchedText);
		assertEquals(label, getLabel(match.get()));
		assertEquals(target, getTarget(match.get()));
	}
	
	@ParameterizedTest
	@CsvSource({
			"![Diagram [x] is here](some/path/to/file.svg)",
			"![Diagram x is here](some text ( sd)",
			"![Diagram x is here](some text (a) sd)"
	})
	public void textNotMatchedAsLink(String statement) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinksAndImages(statement).findFirst();
		
		assertTrue(match.isEmpty());
	}
	
	@ParameterizedTest
	@CsvSource({
			"[File [  X](some/path/to/file.txt),[  X](some/path/to/file.txt)",
			"![Diagram [a](some/path/to/file.puml),[a](some/path/to/file.puml)",
			"![Diagram x is here](some text ) sd),![Diagram x is here](some text )"
	})
	public void textPartiallyMatchedAsLinkOrImage(String statement, String expectedMatch) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinksAndImages(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(expectedMatch, match.get().matchedText);
	}
	
	@ParameterizedTest
	@CsvSource({
			"![label](target),label,target",
			"![Some link title](),Some link title,''",
			"![](some/path/file.svg),'',some/path/file.svg",
			"![](),'',''",
			"![important](../../path/to/file.png),important,../../path/to/file.png",
			"![Some text with almost any symbol :;.-_<>!\"§$%&/()=?`´´#')\\{}](some/path/to/a_file.puml),Some text with almost any symbol :;.-_<>!\"§$%&/()=?`´´#')\\{},some/path/to/a_file.puml"
	})
	public void imagesAreFound(String statement, String label, String target) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinksAndImages(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(statement, match.get().matchedText);
		assertEquals(label, getLabel(match.get()));
		assertEquals(target, getTarget(match.get()));
	}
	
	@ParameterizedTest
	@CsvSource({
			"[label][refKey],label,refKey",
			"[Link title][link label],Link title,link label",
			"[Some longer text! Yes! With special characters !?=)/(//%$§\"!°.#'][special],Some longer text! Yes! With special characters !?=)/(//%$§\"!°.#',special",
			"[text][key],text,key",
			"[Some \\] escaped brackets \\[ are ignored here][REF],Some \\] escaped brackets \\[ are ignored here,REF"
	})
	public void fullReferenceLinksAreFound(String statement, String label, String referenceKey) {
		Optional <RegexMatch> match = MarkdownParsingTools.findFullAndCollapsedReferenceLinks(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(statement, match.get().matchedText);
		assertEquals(label, getLabel(match.get()));
		assertEquals(referenceKey, getTarget(match.get()));
	}
	
	@ParameterizedTest
	@CsvSource({
			"[label][],label,",
			"[Link label][],Link label",
			"[Some longer text with special characters !?=)/(//%$§\"!°.#'][],Some longer text with special characters !?=)/(//%$§\"!°.#'",
			"[Some \\] escaped brackets \\[ are ignored here][],Some \\] escaped brackets \\[ are ignored here"
	})
	public void collapsedReferenceLinksAreFound(String statement, String label) {
		Optional <RegexMatch> match = MarkdownParsingTools.findFullAndCollapsedReferenceLinks(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(statement, match.get().matchedText);
		assertEquals(label, getLabel(match.get()));
	}
	
	@ParameterizedTest
	@CsvSource({
			"[label],label",
			"[Link label],Link label",
			"[],''",
			"[Some longer text with special characters !?=)/(//%$§\"!°.#'],Some longer text with special characters !?=)/(//%$§\"!°.#'",
			"[Some \\] escaped brackets \\[ are ignored here],Some \\] escaped brackets \\[ are ignored here"
	})
	public void shortcutReferenceLinksAreFound(String statement, String label) {
		Optional <RegexMatch> match = MarkdownParsingTools.findShortcutReferenceLinks(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(statement, match.get().matchedText);
		assertEquals(label, getTarget(match.get()));
	}
	
	@ParameterizedTest
	@CsvSource({
			"[Text]someChars(path),[Text],Text",
			"[some text] sdf ](sdf),[some text],some text",
			"[link-like text 4]\\(https://www.something-else-1.com\\),[link-like text 4],link-like text 4",
			"[link-like text 5]\\(https://www.something-else-2.com),[link-like text 5],link-like text 5",
	})
	public void textNotMatchedAsCompleteLinksButAsShortcutReferenceLinks(String statement, String matchedText, String referenceKey) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinksAndImages(statement).findFirst();
		
		assertTrue(match.isEmpty());
		
		match = MarkdownParsingTools.findShortcutReferenceLinks(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(matchedText, match.get().matchedText);
		assertEquals(referenceKey, getTarget(match.get()));
	}
	
	@ParameterizedTest
	@CsvSource({
			"[link-like text 6](https://www.something-else-3.com\\)", // we don't allow a "(" after "[...]"
			"[link-like text 7][]", // we don't allow a "[" after "[...]"
			"[link-like text 8][\\]",
			"[text\\][key]"
	})
	public void textNotMatchedAsShortcutReferenceLinks(String statement) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinksAndImages(statement).findFirst();
		
		assertTrue(match.isEmpty());
		
		match = MarkdownParsingTools.findShortcutReferenceLinks(statement).findFirst();
		
		assertTrue(match.isEmpty());
	}
	
	@ParameterizedTest
	@CsvSource({
			"[ sdf sdf sd ] sd ][sdf sd]",
			"[ sdf sdf sd ][sdf [ sd]",
			"[text\\][key]",
			"[link-like text][\\]",
			"\\[][]",
			"[\\][]",
			"[]\\[]",
			"[][\\]",
			"[]:[]"
	})
	public void textNotMatchedAsFullOrCollapsedReferenceLinks(String statement) {
		Optional <RegexMatch> match = MarkdownParsingTools.findFullAndCollapsedReferenceLinks(statement).findFirst();
		
		assertTrue(match.isEmpty());
	}
	
	@ParameterizedTest
	@CsvSource({
			"[ prefix [ label ][target],[ label ][target]",	
			"[ label ][target ] sd],[ label ][target ]",
			"[[label][target]],[label][target]",
			"[surrounding [label][target] text...],[label][target]",
			"[[][]],[][]"
	})
	public void textWithUnEscapedBracketsMatchedAsFullOrCollapsedReferenceLinks(String statement, String expectedMatch) {
		Optional <RegexMatch> match = MarkdownParsingTools.findFullAndCollapsedReferenceLinks(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(expectedMatch, match.get().matchedText);
	}
	
	@ParameterizedTest
	@CsvSource({
			"[label]: target,label,target",
			"[]: https://plantuml.com,'',https://plantuml.com",
			"[key]:,key,''",
			"[]:,'',''",
			"[adv]: https://www.advantest.com,adv,https://www.advantest.com",
			"[adv]:https://www.advantest.com,adv,https://www.advantest.com",
			"[Some longer text! Yes! With special characters !?=)/(//%$§\"!°.#']: path/to/file.txt,Some longer text! Yes! With special characters !?=)/(//%$§\"!°.#',path/to/file.txt",
			"[Some \\] escaped brackets \\[ are ignored here]: REF,Some \\] escaped brackets \\[ are ignored here,REF"
	})
	public void linkReferenceDefinitionsAreFound(String statement, String label, String referenceKey) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinkReferenceDefinitions(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(statement, match.get().matchedText);
		assertEquals(label, getLabel(match.get()));
		assertEquals(referenceKey, getTarget(match.get()));
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
			"[adv]:\nhttps://www.advantest.com",
			"[adv]: https://www.advantest.com \"Advantest Europe\"",
			"[adv]:\nhttps://www.advantest.com\n\"Some description\""
	})
	public void linkReferenceDefinitionsSpanningMultipleLinesOrHavingDescriptionAreFound(String statement) {
		String target = "https://www.advantest.com";
		
		Optional <RegexMatch> match = MarkdownParsingTools.findLinkReferenceDefinitions(statement).findFirst();
		
		assertTrue(match.isPresent());
		
		int indexOfTarget = statement.indexOf(target);
		String matchedPart = statement.substring(0, indexOfTarget + target.length());
		assertEquals(matchedPart, match.get().matchedText);
		
		assertEquals("adv", getLabel(match.get()));
		assertEquals(target, getTarget(match.get()));
	}
	
	@ParameterizedTest
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
			"[adv\\]:\nhttps://www.advantest.com",
			" [adv]: https://www.advantest.com",
			"\t[adv]: https://www.advantest.com",
			"lsadfkj sdfk sd [adv]: https://www.advantest.com sdf sd",
			"[bla [ blub]: link"
	})
	public void textDoesNotMatchAtAll(String statement) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinksAndImages(statement).findFirst();
		
		assertTrue(match.isEmpty());
		
		match = MarkdownParsingTools.findLinkReferenceDefinitions(statement).findFirst();
		
		assertTrue(match.isEmpty());
		
		match = MarkdownParsingTools.findFullAndCollapsedReferenceLinks(statement).findFirst();
		
		assertTrue(match.isEmpty());
		
		match = MarkdownParsingTools.findShortcutReferenceLinks(statement).findFirst();
		
		assertTrue(match.isEmpty());
	}
	
	@ParameterizedTest
	@CsvSource({
			"[bla ] blub]: link,[bla ]"
	})
	public void textDoesNotMatchAtAllExceptAsShortcutReferenceLink(String statement, String expectedMatch) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinksAndImages(statement).findFirst();
		
		assertTrue(match.isEmpty());
		
		match = MarkdownParsingTools.findLinkReferenceDefinitions(statement).findFirst();
		
		assertTrue(match.isEmpty());
		
		match = MarkdownParsingTools.findFullAndCollapsedReferenceLinks(statement).findFirst();
		
		assertTrue(match.isEmpty());
		
		match = MarkdownParsingTools.findShortcutReferenceLinks(statement).findFirst();
		
		assertTrue(match.isPresent());
		assertEquals(expectedMatch, match.get().matchedText);
	}
	
	@ParameterizedTest
	@CsvSource({
			"[^1]: Footnote 1",
			"[^9731]:Another footnote",
			"[^footnote]: Some explaining text.",
			"[^adv]:https://www.advantest.com",
			"[^key]:\nhttps://www.advantest.com"
	})
	public void footnoteDefinitionsNotParsedAslinkReferenceDefinitions(String statement) {
		Optional <RegexMatch> match = MarkdownParsingTools.findLinkReferenceDefinitions(statement).findFirst();
		
		assertTrue(match.isEmpty());
	}
	
	// TODO Add tests for anchor search
	// TODO Add tests for findings sets of links in longer texts
}
