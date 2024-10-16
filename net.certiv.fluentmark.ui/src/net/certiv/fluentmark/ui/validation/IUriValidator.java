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

import java.util.Map;

import java.net.http.HttpClient;

public interface IUriValidator {
	
	boolean isResponsibleFor(String uriText);
	
	IMarker checkUri(String uriText, IFile file, Map<String, String> contextDetails, int lineNumber, int offset, HttpClient defaultHttpClient) throws CoreException;
}
