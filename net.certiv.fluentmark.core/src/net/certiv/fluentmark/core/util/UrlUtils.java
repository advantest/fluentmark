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
	
}
