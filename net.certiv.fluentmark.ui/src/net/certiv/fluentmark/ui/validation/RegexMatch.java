/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class RegexMatch {
	
	public final String matchedText;
	public final int startIndex;
	public final int endIndex;
	
	public RegexMatch(String matchedText, int startIndex, int endIndex) {
		this.matchedText = matchedText;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}
	
	public static Stream<RegexMatch> findMatches(String textToCheck, Pattern patternToFind, String regexMatchGroupName) {
		List<RegexMatch> matches = new ArrayList<>();
		
		Matcher textMatcher = patternToFind.matcher(textToCheck);
		boolean found = textMatcher.find();
		
		while (found) {
			String currentTextMatch = regexMatchGroupName != null ? textMatcher.group(regexMatchGroupName) : textMatcher.group();
			int startIndex = regexMatchGroupName != null ? textMatcher.start(regexMatchGroupName) : textMatcher.start();
			int endIndex = regexMatchGroupName != null ? textMatcher.end(regexMatchGroupName) : textMatcher.end();
			
			matches.add(new RegexMatch(currentTextMatch, startIndex, endIndex));
			
			found = textMatcher.find();
		}
		
		return matches.stream();
	}
}