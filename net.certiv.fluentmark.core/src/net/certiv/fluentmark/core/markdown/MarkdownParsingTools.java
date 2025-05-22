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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MarkdownParsingTools {
	
	public static final String REGEX_ANY_LINE_SEPARATOR = "(\\r\\n|\\n)";
	
	public static final String CAPTURING_GROUP_LABEL = "label";
	public static final String CAPTURING_GROUP_TARGET = "target";
	public static final String CAPTURING_GROUP_ANCHOR = "anchor";
	
	// pattern for images and links, e.g. ![](../image.png) or [some text](https://www.advantext.com)
	// search non-greedy ("?" parameter) for "]" and ")" brackets, otherwise we match the last ")" in the following example
	// (link to [Topic Y](#topic-y))
	private static final String REGEX_LINK_PREFIX = "(!){0,1}\\[.*?\\]\\(";
	private static final String REGEX_LINK = "(!){0,1}\\[(?<" + CAPTURING_GROUP_LABEL
			+ ">.*)?\\]\\((?<" + CAPTURING_GROUP_TARGET + ">.*)?\\)";
	
	// pattern for link reference definitions, like [label]: https://www.plantuml.com "title",
	// but excludes footnote definitions like [^label]: Some text
	public static final String REGEX_LINK_REF_DEF_OPENING_BRACKET = "\\[";
	public static final String REGEX_LINK_REF_DEF_PART = "\\]:( |\\t|\\n)?( |\\t)*";
	public static final String REGEX_LINK_REF_DEF_SUFFIX = "\\S+";
	private static final String REGEX_LINK_REF_DEF_PREFIX = REGEX_LINK_REF_DEF_OPENING_BRACKET + "[^^\\n]+?" + REGEX_LINK_REF_DEF_PART;
	//private static final String REGEX_LINK_REF_DEFINITION = REGEX_LINK_REF_DEF_PREFIX + REGEX_LINK_REF_DEF_SUFFIX;
	private static final String REGEX_LINK_REF_DEFINITION = "\\[(?<" + CAPTURING_GROUP_LABEL
			+ ">[^^\\n]+?)\\]:( |\\t|\\n)?( |\\t)*(?<" + CAPTURING_GROUP_TARGET + ">\\S+)";
	
	// patterns for reference links like the following three variants specified in CommonMark: https://spec.commonmark.org/0.31.2/#reference-link
	// * full reference link:      [Markdown specification][CommonMark]
	// * collapsed reference link: [CommonMark][]
	// * shortcut reference link:  [CommonMark]
	private static final String REGEX_REF_LINK_FULL_OR_COLLAPSED_PREFIX = "\\[[^\\]]*?\\]\\[";
	private static final String REGEX_REF_LINK_FULL_OR_COLLAPSED = REGEX_REF_LINK_FULL_OR_COLLAPSED_PREFIX + "[^\\]]*?\\]";
	private static final String REGEX_REF_LINK_SHORTCUT = "(?<!\\]|\\\\)(\\[[^\\]]*?\\])(?!(\\[|\\(|:))";

	// the following regex contains a named capturing group, name is "anchor", syntax: (?<name>Sally)
	private static final String REGEX_HEADING_WITH_ANCHOR = "#+\\s.*\\{#(?<" + CAPTURING_GROUP_ANCHOR + ">.*)\\}\\s*";
	public static final String REGEX_VALID_ANCHOR_ID = "[A-Za-z][A-Za-z0-9-_:\\.]*";
	
	private static final Pattern LINK_PATTERN = Pattern.compile(REGEX_LINK);
	public static final Pattern LINK_PREFIX_PATTERN = Pattern.compile(REGEX_LINK_PREFIX);
	public static final Pattern LINK_REF_DEF_PATTERN_PREFIX = Pattern.compile(REGEX_LINK_REF_DEF_PREFIX);
	private static final Pattern LINK_REF_DEF_PATTERN = Pattern.compile(REGEX_LINK_REF_DEFINITION);
	public static final Pattern REF_LINK_PEFIX_PATTERN = Pattern.compile(REGEX_REF_LINK_FULL_OR_COLLAPSED_PREFIX);
	private static final Pattern REF_LINK_FULL_PATTERN = Pattern.compile(REGEX_REF_LINK_FULL_OR_COLLAPSED);
	private static final Pattern REF_LINK_SHORT_PATTERN = Pattern.compile(REGEX_REF_LINK_SHORTCUT);
	
	private static final Pattern HEADING_PATTERN = Pattern.compile(MarkdownParsingTools.REGEX_HEADING_WITH_ANCHOR);
	
	
	public static Set<String> findValidSectionAnchorsInMarkdownCode(String markdownCode) {
		return Arrays.stream(markdownCode.split(REGEX_ANY_LINE_SEPARATOR))
				.filter(line -> line.matches(REGEX_HEADING_WITH_ANCHOR))
				.map(lineWithAnchor -> {
					int startIndex = lineWithAnchor.lastIndexOf("{#") + 2;
					int endIndex = lineWithAnchor.lastIndexOf("}");
					return lineWithAnchor.substring(startIndex, endIndex);
				})
				.filter(anchor -> anchor.matches(REGEX_VALID_ANCHOR_ID))
				.collect(Collectors.toSet());
	}
	
	public static Stream<RegexMatch> findLinksAndImages(String markdownCode) {
		return findMatches(markdownCode, LINK_PATTERN, CAPTURING_GROUP_LABEL, CAPTURING_GROUP_TARGET);
	}
	
	public static Stream<RegexMatch> findLinkReferenceDefinitions(String markdownCode) {
		return findMatches(markdownCode, LINK_REF_DEF_PATTERN, CAPTURING_GROUP_LABEL, CAPTURING_GROUP_TARGET);
	}
	
	public static Stream<RegexMatch> findFullAndCollapsedReferenceLinks(String markdownCode) {
		return findMatches(markdownCode, REF_LINK_FULL_PATTERN);
	}
	
	public static Stream<RegexMatch> findShortcutReferenceLinks(String markdownCode) {
		return findMatches(markdownCode, REF_LINK_SHORT_PATTERN);
	}
	
	public static Stream<RegexMatch> findHeadingAnchorIds(String markdownCode) {
		return findMatches(markdownCode, HEADING_PATTERN, CAPTURING_GROUP_ANCHOR)
				.map(match -> match.subMatches.get(CAPTURING_GROUP_ANCHOR));
	}
	
	private static Stream<RegexMatch> findMatches(String textToCheck, Pattern patternToFind, String... capturingGroupNames) {
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
}
