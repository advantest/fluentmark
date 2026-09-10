/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.validation.uri;

import java.net.URI;

public class UriDto {

	public String uri;
	public String scheme;
	public String path;
	public String fragment;
	
	public UriDto(String uri, String schema, String path, String fragment) {
		this.uri = uri;
		this.scheme = schema;
		this.path = path;
		this.fragment = fragment;
	}
	
	/**
	 * Parses the given URI text into its parts.
	 * 
	 * <p>The fragment is returned without the leading <code>#</code>, whichever way the text had to
	 * be parsed: <code>doc.md#section</code> has the fragment <code>section</code>.</p>
	 * 
	 * @param uriText the text to be parsed
	 * @return the parts of the given URI
	 */
	public static UriDto parseUri(String uriText) {
		String path = null;
		String scheme = null;
		String fragment = null;
		
		URI uri;
		try {
			uri = URI.create(uriText);
			
			scheme = uri.getScheme();
			fragment = uri.getFragment();
			path = uri.getPath();
		} catch (IllegalArgumentException e) {
			// we seem not to have a standard-compliant URI, try parsing it ourselves
			int indexOfColon = uriText.indexOf(':');
			int indexOfHashtag = uriText.indexOf('#');
			
			path = uriText;
			
			if (indexOfHashtag > -1) {
				fragment = uriText.substring(indexOfHashtag + 1);
				path = uriText.substring(0, indexOfHashtag);
			}
			
			if (indexOfColon > -1) {
				scheme = uriText.substring(0, indexOfColon);
				if (indexOfColon + 1 < path.length()) {
					path = path.substring(indexOfColon + 1);
				}
			}
		}
		
		return new UriDto(uriText, scheme, path, fragment);
	}
}
