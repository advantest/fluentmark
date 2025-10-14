/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vladsch.flexmark.ext.plantuml.PlantUmlBlockNode;
import com.vladsch.flexmark.ext.plantuml.PlantUmlFencedCodeBlockNode;
import com.vladsch.flexmark.util.ast.Block;


public class MarkdownParsingTools {
	
	public static final String REGEX_ANY_LINE_SEPARATOR = "(\\r\\n|\\n)";
	
	// TODO replace all \\n in regex with REGEX_ANY_LINE_SEPARATOR
	
	public static final String CAPTURING_GROUP_LABEL = "label";
	public static final String CAPTURING_GROUP_TARGET = "target";
	public static final String CAPTURING_GROUP_ANCHOR = "anchor";
	
	// pattern for images and links, e.g. ![](../image.png) or [some text](https://www.advantext.com)
	// search non-greedy (e.g. ".*?" or "[^\n]*?") for label and target capturing groups, otherwise we match the last ")" in the following example
	// (link to [Topic Y](#topic-y))
	// "(?<!\\)" is a negative look-behind disallowing preceding "\" chars,
	// the look-behind is placed before \] and \[ and before \) (before "(" we explicitly require "]")
	
	// (!){0,1}(?<!\\)\[(?<label>([^\n](?<![^\\](\]|\[)))*?)?(?<!\\)\]\((?<target>([^\n](?<![^\\](\)|\()))*?)?(?<!\\)\)
	private static final String REGEX_LINK = "(!){0,1}(?<!\\\\)\\[(?<" + CAPTURING_GROUP_LABEL
			+ ">([^\\n](?<![^\\\\](\\]|\\[)))*?)?(?<!\\\\)\\]\\((?<" + CAPTURING_GROUP_TARGET
			+ ">([^\\n](?<![^\\\\](\\)|\\()))*?)?(?<!\\\\)\\)";
	
	// pattern for link reference definitions, like [label]: https://www.plantuml.com "title",
	// but excludes footnote definitions like [^label]: Some text
	
	// ^ {0,3}\[(?<label>([^^\n](?<![^\\](\]|\[)))*?)(?<!\\)\]:([ \t]*\n?[ \t]*(?=[^ \n\r\t\f\v\]\[]))?(?<target>[^ \n\r\t\f\v\]\[]*)
	private static final String REGEX_LINK_REF_DEFINITION = "^ {0,3}\\[(?<" + CAPTURING_GROUP_LABEL
			+ ">([^^\\n](?<![^\\\\](\\]|\\[)))*?)(?<!\\\\)\\]:([ \\t]*\\n?[ \\t]*(?=[^ \\n\\r\\t\\f\\v\\]\\[]))?(?<"
			+ CAPTURING_GROUP_TARGET + ">[^ \\n\\r\\t\\f\\v\\]\\[]*)";
	
	// patterns for reference links like the following three variants specified in CommonMark: https://spec.commonmark.org/0.31.2/#reference-link
	// * full reference link:      [Markdown specification][CommonMark]
	// * collapsed reference link: [CommonMark][]
	// * shortcut reference link:  [CommonMark]
	
	// (?<!\\)\[(?<label>([^\n](?<![^\\](\]|\[)))*?)(?<!\\)\]\[(?<target>([^\n](?<![^\\](\]|\[)))*?)(?<!\\)\]
	private static final String REGEX_REF_LINK_FULL_OR_COLLAPSED = "(?<!\\\\)\\[(?<"
			+ CAPTURING_GROUP_LABEL + ">([^\\n](?<![^\\\\](\\]|\\[)))*?)(?<!\\\\)\\]\\[(?<"
			+ CAPTURING_GROUP_TARGET + ">([^\\n](?<![^\\\\](\\]|\\[)))*?)(?<!\\\\)\\]";
	
	// (?<!\]|\\)(\[(?<target>([^\n](?<![^\\](\]|\[)))*?)(?<!\\)\])(?!(\[|\(|:))
	private static final String REGEX_REF_LINK_SHORTCUT = "(?<!\\]|\\\\)(\\[(?<"
			+ CAPTURING_GROUP_TARGET + ">([^\\n](?<![^\\\\](\\]|\\[)))*?)(?<!\\\\)\\])(?!(\\[|\\(|:))";

	// the following regex contains a named capturing group, name is "anchor", syntax: (?<name>expressionToMatch)
	private static final String REGEX_HEADING_WITH_ANCHOR = "#+\\s.*\\{#(?<" + CAPTURING_GROUP_ANCHOR + ">.*)\\}\\s*";
	private static final String REGEX_VALID_LINK_REF_DEF_LABEL = "[A-Za-z0-9-_:\\. /]+";
	private static final String REGEX_VALID_ANCHOR_ID = "[A-Za-z][A-Za-z0-9-_:\\.]*";
	
	private static final Pattern LINK_PATTERN = Pattern.compile(REGEX_LINK);
	private static final Pattern LINK_REF_DEF_PATTERN = Pattern.compile(REGEX_LINK_REF_DEFINITION, Pattern.MULTILINE);
	private static final Pattern REF_LINK_FULL_PATTERN = Pattern.compile(REGEX_REF_LINK_FULL_OR_COLLAPSED);
	private static final Pattern REF_LINK_SHORT_PATTERN = Pattern.compile(REGEX_REF_LINK_SHORTCUT);
	
	private static final Pattern HEADING_PATTERN = Pattern.compile(MarkdownParsingTools.REGEX_HEADING_WITH_ANCHOR);
	
	public static boolean isValidLinkReferenceDefinitionIdentifier(String identifier) {
		return identifier != null && !identifier.isBlank() && identifier.matches(REGEX_VALID_LINK_REF_DEF_LABEL);
	}
	
	public static boolean isValidAnchorIdentifier(String identifier) {
		return identifier != null && identifier.matches(REGEX_VALID_ANCHOR_ID);
	}
	
	public static Set<String> findValidSectionAnchorsInMarkdownCode(String markdownCode) {
		return Arrays.stream(markdownCode.split(REGEX_ANY_LINE_SEPARATOR))
				.filter(line -> line.matches(REGEX_HEADING_WITH_ANCHOR))
				.map(lineWithAnchor -> {
					int startIndex = lineWithAnchor.lastIndexOf("{#") + 2;
					int endIndex = lineWithAnchor.lastIndexOf("}");
					return lineWithAnchor.substring(startIndex, endIndex);
				})
				.filter(anchor -> isValidAnchorIdentifier(anchor))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Detects Markdown links of the form <code>[label](target)</code>
	 * (so called <a href="https://spec.commonmark.org/0.31.2/#inline-link">inline links</a>)
	 * and Markdown images of the form <code>![label](target)</code>
	 * in the given Markdown source code.
	 * Does not capture reference links like <code>[key]</code>, <code>[key][]</code>, or <code>[label][key]</code>.
	 * The <em>label</em> and <em>target</em> matches are captured in sub-group matches with the capturing group names
	 * {@link #CAPTURING_GROUP_LABEL} and {@link #CAPTURING_GROUP_TARGET}.
	 * 
	 * @param markdownCode Markdown source code
	 * @return the detected regular expression matches for links and images
	 * 
	 * @see {@link #CAPTURING_GROUP_LABEL}
	 * @see {@link #CAPTURING_GROUP_TARGET}
	 * @see {@link #findFullAndCollapsedReferenceLinks(String)}
	 * @see {@link #findShortcutReferenceLinks(String)}
	 */
	public static Stream<RegexMatch> findLinksAndImages(String markdownCode) {
		return findMatches(markdownCode, LINK_PATTERN, CAPTURING_GROUP_LABEL, CAPTURING_GROUP_TARGET);
	}
	
	/**
	 * Detects Markdown links of the form <code>[label](target)</code>
	 * (so called <a href="https://spec.commonmark.org/0.31.2/#inline-link">inline links</a>)
	 * in the given Markdown source code.
	 * Does not capture reference links like <code>[key]</code>, <code>[key][]</code>, or <code>[label][key]</code>.
	 * The <em>label</em> and <em>target</em> matches are captured in sub-group matches with the capturing group names
	 * {@link #CAPTURING_GROUP_LABEL} and {@link #CAPTURING_GROUP_TARGET}.
	 * 
	 * @param markdownCode Markdown source code
	 * @return the detected regular expression matches for inline links
	 * 
	 * @see {@link #CAPTURING_GROUP_LABEL}
	 * @see {@link #CAPTURING_GROUP_TARGET}
	 * @see {@link #findFullAndCollapsedReferenceLinks(String)}
	 * @see {@link #findShortcutReferenceLinks(String)}
	 * @see {@link #findLinksAndImages(String)}
	 */
	public static Stream<RegexMatch> findInlineLinks(String markdownCode) {
		return findLinksAndImages(markdownCode)
				.filter(match -> !match.matchedText.startsWith("!"));
	}
	
	/**
	 * Detects <a href="https://spec.commonmark.org/0.31.2/#link-reference-definition">link reference definitions</a> of the form <code>[label]: target</code>,
	 * does not capture the title in definitions like <code>[label]: target "title"</code>,
	 * excludes footnote definitions like <code>[^label]: Some text</code>.
	 * The <em>label</em> and <em>target</em> matches are captured in sub-group matches with the capturing group names
	 * {@link #CAPTURING_GROUP_LABEL} and {@link #CAPTURING_GROUP_TARGET}.
	 * 
	 * @param markdownCode Markdown source code
	 * @return the detected regular expression matches for link reference definitions
	 * 
	 * @see {@link #CAPTURING_GROUP_LABEL}
	 * @see {@link #CAPTURING_GROUP_TARGET}
	 */
	public static Stream<RegexMatch> findLinkReferenceDefinitions(String markdownCode) {
		return findMatches(markdownCode, LINK_REF_DEF_PATTERN, CAPTURING_GROUP_LABEL, CAPTURING_GROUP_TARGET);
	}
	
	public static Optional<RegexMatch> findLinkReferenceDefinition(String markdownCode, String linkReferenceDefinitionName) {
		if (linkReferenceDefinitionName == null || linkReferenceDefinitionName.isBlank()) {
			throw new IllegalArgumentException();
		}
		
		return findLinkReferenceDefinitions(markdownCode)
			.filter(match -> {
				RegexMatch labelMatch = match.subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_LABEL);
				return labelMatch != null && labelMatch.matchedText.equals(linkReferenceDefinitionName);
			})
			.findFirst();
	}
	
	/**
	 * Detects full reference links of the form <code>[label][target]</code>
	 * and collapsed reference links of the form <code>[target][]</code>.
	 * Does not cover shortcut reference links of the form <code>[target]</code>.
	 * The <em>label</em> and <em>target</em> matches are captured in sub-group matches with the capturing group names
	 * {@link #CAPTURING_GROUP_LABEL} and {@link #CAPTURING_GROUP_TARGET}.
	 * 
	 * @param markdownCode Markdown source code
	 * @return the detected regular expression matches for full and collapsed reference links
	 * 
	 * @see {@link #CAPTURING_GROUP_LABEL}
	 * @see {@link #CAPTURING_GROUP_TARGET}
	 * @see {@link #findShortcutReferenceLinks(String)}
	 */
	public static Stream<RegexMatch> findFullAndCollapsedReferenceLinks(String markdownCode) {
		return findMatches(markdownCode, REF_LINK_FULL_PATTERN, CAPTURING_GROUP_LABEL, CAPTURING_GROUP_TARGET);
	}
	
	/**
	 * Detects shortcut reference links of the form <code>[target]</code>.
	 * Does not cover full reference links of the form <code>[label][target]</code>
	 * and collapsed reference links of the form <code>[target][]</code>.
	 * The <em>target</em> matches are captured in sub-group matches with the capturing group name
	 * {@link #CAPTURING_GROUP_TARGET}.
	 * 
	 * @param markdownCode Markdown source code
	 * @return the detected regular expression matches for shortcut reference links
	 * 
	 * @see {@link #CAPTURING_GROUP_TARGET}
	 * @see {@link #findFullAndCollapsedReferenceLinks(String)}
	 */
	public static Stream<RegexMatch> findShortcutReferenceLinks(String markdownCode) {
		return findMatches(markdownCode, REF_LINK_SHORT_PATTERN, CAPTURING_GROUP_TARGET);
	}
	
	public static Stream<RegexMatch> findHeadingAnchorIds(String markdownCode) {
		return findMatches(markdownCode, HEADING_PATTERN, CAPTURING_GROUP_ANCHOR)
				.map(match -> match.subMatches.get(CAPTURING_GROUP_ANCHOR));
	}
	
	private static Stream<RegexMatch> findMatches(String textToCheck, Pattern patternToFind, String... capturingGroupNames) {
		if (textToCheck == null) {
			throw new IllegalArgumentException();
		}
		
		List<RegexMatch> matches = new ArrayList<>();
		
		Matcher textMatcher = patternToFind.matcher(textToCheck);
		boolean found = textMatcher.find();
		
		while (found) {
			String currentTextMatch = textMatcher.group();
			int startIndex = textMatcher.start();
			int endIndex = textMatcher.end();
			
			RegexMatch match = new RegexMatch(currentTextMatch, startIndex, endIndex);
			
			for (String capturingGroupName : capturingGroupNames) {
				String subMatchText = textMatcher.group(capturingGroupName);
				int subMatchStartIndex = textMatcher.start(capturingGroupName);
				int subMatchEndIndex = textMatcher.end(capturingGroupName);
				match.addSubMatch(capturingGroupName, new RegexMatch(subMatchText, subMatchStartIndex, subMatchEndIndex));
			}
			
			matches.add(match);
			
			found = textMatcher.find();
		}
		
		return matches.stream();
	}
	
	public static String getPlantUmlCodeFromMarkdownCodeBlock(Block codeBlock) {
		if (codeBlock instanceof PlantUmlFencedCodeBlockNode) {
			PlantUmlFencedCodeBlockNode fencedCodeBlock = (PlantUmlFencedCodeBlockNode) codeBlock;
			return fencedCodeBlock.getContentChars().toString();
		} else if (codeBlock instanceof PlantUmlBlockNode) {
			return codeBlock.getChars().toString();
		}
		return null;
	}
}
