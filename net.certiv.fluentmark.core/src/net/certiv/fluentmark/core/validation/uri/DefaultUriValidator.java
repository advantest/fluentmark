/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.validation.uri;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import net.certiv.fluentmark.core.validation.IValidationResultConsumer;
import net.certiv.fluentmark.core.validation.IssueTypes;

public class DefaultUriValidator implements IUriValidator {
	
	private static DefaultUriValidator instance = null;
	
	public static DefaultUriValidator getDefaultUriValidator() {
		if (instance == null) {
			instance = new DefaultUriValidator();
		}
		return instance;
	}
	
	private HttpClient httpClient;
	
	private final ConcurrentMap<String, HttpResponse<Void>> uriResponseCache = CacheBuilder.newBuilder()
			.maximumSize(100000)
			.removalListener(new RemovalListener<String, HttpResponse<Void>>() {
				@Override
				public void onRemoval(RemovalNotification<String, HttpResponse<Void>> notification) {
					// Do nothing, the listener only constrains the builder's type variables
				}
			})
			.build()
			.asMap();
	
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
	
	private IValidationResultConsumer issueConsumer;
	
	@Override
	public void setValidationResultConsumer(IValidationResultConsumer issueConsumer) {
		this.issueConsumer = issueConsumer;
	}
	
	protected HttpRequest createHttpHeadRequest(String uriText) throws URISyntaxException {
		URI uri = new URI(uriText);
		
		// we only need HTTP HEAD, no page content, just reachability
		return HttpRequest.newBuilder()
				.method("HEAD", HttpRequest.BodyPublishers.noBody())
				.uri(uri)
				// Some web sites / servers check the user agent header and expect certain common values, otherwise they answer with http status code 403.
				// Hint: call curl -v https://your-domain.com to check, which user agent header is sent by curl (which is usually successful)
				// and use the same use agent value here
				.header("User-Agent", "curl/8.11.0")
				.header("Accept", "*/*")
				.timeout(Duration.ofMillis(200))
				.build();
	}
	
	protected void evaluateHttpResponse(String uriText, HttpResponse<Void> httpResponse, IFile file, Map<String, String> contextDetails, int lineNumber, int offset) {
		int statusCode = httpResponse.statusCode();
		
		if (statusCode >= 400) {
			issueConsumer.reportValidationResult(file,
					IssueTypes.MARKDOWN_ISSUE,
					IMarker.SEVERITY_ERROR,
					String.format("The referenced web address '%s' is not reachable (HTTP status code %s).", uriText, statusCode),
					lineNumber,
					offset,
					offset + uriText.length());
		}
	}
	
	@Override
	public void checkUri(String uriText, IFile file, Map<String, String> contextDetails, int lineNumber, int offset,
			HttpClient httpClient) {
		
		if (!uriText.toLowerCase().startsWith("http://")
			&& !uriText.toLowerCase().startsWith("https://")) {
			issueConsumer.reportValidationResult(file,
					IssueTypes.MARKDOWN_ISSUE,
					IMarker.SEVERITY_ERROR,
					String.format("The referenced web address '%s' seems not to be a valid HTTP web address. It has to start with https:// or http://", uriText),
					lineNumber,
					offset,
					offset + uriText.length());
			return;
		}
		
		HttpResponse<Void> response = uriResponseCache.get(uriText);
		if (response != null) {
			evaluateHttpResponse(uriText, response, file, contextDetails, lineNumber, offset);
			return;
		}
		
		HttpRequest headRequest = null;
		try {
			headRequest = createHttpHeadRequest(uriText);
		} catch (URISyntaxException | IllegalArgumentException e) {
			issueConsumer.reportValidationResult(file,
					IssueTypes.MARKDOWN_ISSUE,
					IMarker.SEVERITY_ERROR,
					String.format("The referenced web address '%s' seems not to be a valid HTTP web address. " + e.getMessage(), uriText),
					lineNumber,
					offset,
					offset + uriText.length());
			return;
		}
		
		try {
			response = httpClient.send(headRequest, BodyHandlers.discarding());
			uriResponseCache.put(uriText, response);
		} catch (IOException | InterruptedException e) {
			String errorMessage = e.getMessage();
			if (errorMessage == null) {
				errorMessage = e.getClass().getName();
			}
			
			issueConsumer.reportValidationResult(file,
					IssueTypes.MARKDOWN_ISSUE,
					IMarker.SEVERITY_WARNING,
					String.format("The referenced web address '%s' seems not to exist. (Error message: %s)", uriText, errorMessage),
					lineNumber,
					offset,
					offset + uriText.length());
			return;
		}
		
		evaluateHttpResponse(uriText, response, file, contextDetails, lineNumber, offset);
	}

}
