/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import org.eclipse.core.runtime.CoreException;

import net.certiv.fluentmark.ui.markers.MarkerCalculator;

import java.util.Map;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import java.io.IOException;

import java.time.Duration;

public class DefaultUriValidator implements IUriValidator {
	
	private static DefaultUriValidator instance = null;
	
	public static DefaultUriValidator getDefaultUriValidator() {
		if (instance == null) {
			instance = new DefaultUriValidator();
		}
		return instance;
	}
	
	private HttpClient httpClient;
	
	HttpClient getHttpClient() {
		if (this.httpClient == null) {
			this.httpClient = HttpClient.newBuilder()
					.version(Version.HTTP_2)
					.followRedirects(Redirect.NORMAL)
					.build();
		}
		return this.httpClient;
	}

	@Override
	public boolean isResponsibleFor(String uriText) {
		return uriText != null && !uriText.isBlank();
	}

	@Override
	public IMarker checkUri(String uriText, IFile file, Map<String, String> contextDetails, int lineNumber, int offset,
			HttpClient defaultHttpClient) throws CoreException {
		
		if (!uriText.toLowerCase().startsWith("http://")
			&& !uriText.toLowerCase().startsWith("https://")) {
			return MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
					String.format("The referenced web address '%s' seems not to be a valid HTTP web address. It has to start with https:// or http://", uriText),
					lineNumber,
					offset,
					offset + uriText.length());
		}
			
		URI uri = null;
		try {
			uri = new URI(uriText);
		} catch (URISyntaxException e) {
			return MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
					String.format("The referenced web address '%s' seems not to be a valid HTTP web address. " + e.getMessage(), uriText),
					lineNumber,
					offset,
					offset + uriText.length());
		}
			
		// we only need HTTP HEAD, no page content, just reachability
		HttpRequest headRequest = HttpRequest.newBuilder()
			      .method("HEAD", HttpRequest.BodyPublishers.noBody())    
			      .uri(uri)
			      .timeout(Duration.ofSeconds(2))
			      .build();
		
		int statusCode = -1;
		String errorMessage = null;
		try {
			statusCode = defaultHttpClient.send(headRequest, BodyHandlers.discarding()).statusCode(); 
		} catch (IOException | InterruptedException e) {
			errorMessage = e.getMessage();
			statusCode = -404;
		}
		
		if (statusCode >= 400) {
			return MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
					String.format("The referenced web address '%s' is not reachable (HTTP status code %s).", uriText, statusCode),
					lineNumber,
					offset,
					offset + uriText.length());
		} else if (statusCode == -404) {
			return MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_WARNING,
					String.format("The referenced web address '%s' seems not to exist. (Error message: %s)", uriText, errorMessage),
					lineNumber,
					offset,
					offset + uriText.length());
		}
		
		return null;
	}

}
