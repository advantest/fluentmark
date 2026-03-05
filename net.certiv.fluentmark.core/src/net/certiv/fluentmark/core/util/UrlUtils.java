/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.net.UrlEscapers;

public class UrlUtils {

	public static String removeQueryParametersFromUrl(String urlText) {
		if (urlText.contains("?")) {
			String[] urlParts = urlText.split("\\?");
			if (urlParts != null && urlParts.length >= 1) {
				return urlParts[0];
			}
			
		}
		return urlText;
	}
	
	public static String removeAnchorFromUrl(String urlText) {
		if (urlText.contains("#")) {
			String[] urlParts = urlText.split("#");
			if (urlParts != null && urlParts.length >= 1) {
				return urlParts[0];
			}
			
		}
		return urlText;
	}
	
	public static String extractLastUrlSegment(String urlText) {
		if (urlText.contains("/")) {
			String[] urlParts = urlText.split("/");
			if (urlParts != null && urlParts.length >= 1) {
				return urlParts[urlParts.length - 1];
			}
		}
		return urlText;
	}
	
	public static String encodeQueryParameter(String text) {
		return URLEncoder.encode(text, StandardCharsets.UTF_8);
	}
	
	public static String encodePathVariableValue(String text) {
		return UrlEscapers.urlPathSegmentEscaper().escape(text);
	}
	
	// detect unescaped brackets, regex: (?<!\\)[\(\)\[\]\<\>]
	private static final Pattern REGEX_CHARS_TO_ESCAPE = Pattern.compile("(?<!\\\\)[\\(\\)\\[\\]\\<\\>]");
	
	// detect escaped brackets, regex: \\[\(\)\[\]\<\>]
	private static final Pattern REGEX_ESCAPED_CHARS = Pattern.compile("\\\\[\\(\\)\\[\\]\\<\\>]");
	
	public static String escapeBracketsInMethodReference(String methodReference) {
		Matcher matcher = REGEX_CHARS_TO_ESCAPE.matcher(methodReference);
		StringBuilder builder = new StringBuilder();
		int index = 0;
		while (matcher.find()) {
			builder.append(methodReference.substring(index, matcher.start()));
			builder.append('\\');
			builder.append(methodReference.substring(matcher.start(), matcher.end()));
			index = matcher.end();
		}
		builder.append(methodReference.substring(index, methodReference.length()));
		return builder.toString();
	}
	
	public static String unescapeBracketsInMethodReference(String methodReference) {
		Matcher matcher = REGEX_ESCAPED_CHARS.matcher(methodReference);
		StringBuilder builder = new StringBuilder();
		int index = 0;
		while (matcher.find()) {
			builder.append(methodReference.substring(index, matcher.start()));
			builder.append(methodReference.substring(matcher.start() + 1, matcher.end()));
			index = matcher.end();
		}
		builder.append(methodReference.substring(index, methodReference.length()));
		return builder.toString();
	}
}
