/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.validation.uri;

import java.net.http.HttpClient;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import net.certiv.fluentmark.core.validation.IValidationResultReporter;

public interface IUriValidator extends IValidationResultReporter {
	
	boolean isResponsibleFor(String uriText);
	
	void checkUri(String uriText, IFile file, Map<String, String> contextDetails, int lineNumber, int offset, HttpClient defaultHttpClient);
}
