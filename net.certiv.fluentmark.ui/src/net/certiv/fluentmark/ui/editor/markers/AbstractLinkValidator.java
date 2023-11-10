/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.markers;

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

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.extensionpoints.UriValidatorsManager;

public abstract class AbstractLinkValidator {
	
	private DefaultUriValidator defaultUriValidator;
	
	public AbstractLinkValidator() {
		defaultUriValidator = DefaultUriValidator.getDefaultUriValidator();
	}

	protected IMarker checkHttpUri(String uriText, IFile file, int lineNumber, int offset) throws CoreException {
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
		            		markers.add(uriValidator.checkUri(uriText, file, lineNumber, offset, defaultUriValidator.getHttpClient()));
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
		
		return defaultUriValidator.checkUri(uriText, file, lineNumber, offset, defaultUriValidator.getHttpClient());
	}
	
	protected int getLineForOffset(IDocument document, int offset) {
		try {
			return document.getLineOfOffset(offset) + 1;
		} catch (BadLocationException e) {
			return -1;
		}
	}
	
}
