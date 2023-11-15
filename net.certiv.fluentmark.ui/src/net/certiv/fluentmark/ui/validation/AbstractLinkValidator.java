/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.net.URI;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.extensionpoints.UriValidatorsManager;

public abstract class AbstractLinkValidator {
	
	private DefaultUriValidator defaultUriValidator;
	
	public AbstractLinkValidator() {
		defaultUriValidator = DefaultUriValidator.getDefaultUriValidator();
	}

	protected IMarker checkHttpUri(String uriText, IFile file, Map<String, String> contextDetails, int lineNumber, int offset) throws CoreException {
		if (uriText == null) {
			return null;
		}
		
		// run all the URI validators from extensions, first
		List<IUriValidator> uriValidators = UriValidatorsManager.getInstance().getUriValidators();
		for (IUriValidator uriValidator : uriValidators) {
			if (uriValidator.isResponsibleFor(uriText)) {
				final List<IMarker> markers = new ArrayList<>(1);
				
				ISafeRunnable runnable = new ISafeRunnable() {
		            @Override
		            public void handleException(Throwable e) {
		            	Exception ex;
		            	if (e instanceof Exception) {
		            		ex = (Exception) e;
		            	} else {
		            		ex = new RuntimeException(e);
		            	}
		            	FluentUI.log(IStatus.WARNING, String.format("Could not run URI validator \"%s\".", uriValidator.getClass().getName()), ex);
		            }

		            @Override
		            public void run() throws Exception {
		            	if (uriValidator.isResponsibleFor(uriText)) {
		            		markers.add(uriValidator.checkUri(uriText, file, contextDetails, lineNumber, offset, defaultUriValidator.getHttpClient()));
		            	}
		            }
		        };
		        SafeRunner.run(runnable);
		        
		        if (markers.size() > 0) {
		        	return markers.get(0);
		        }
		        
		        // do not run other validations if we found a responsible validator from extensions
		        return null;
			}
		}
		
		return defaultUriValidator.checkUri(uriText, file, contextDetails, lineNumber, offset, defaultUriValidator.getHttpClient());
	}
	
	protected int getLineForOffset(IDocument document, int offset) {
		try {
			return document.getLineOfOffset(offset) + 1;
		} catch (BadLocationException e) {
			return -1;
		}
	}
	
	protected UriDto parseUri(String uriText) {
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
				fragment = uriText.substring(indexOfHashtag);
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
	
	protected static class UriDto {
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
	}
	
}
