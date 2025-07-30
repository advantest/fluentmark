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

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.extensionpoints.UriValidatorsManager;
import net.certiv.fluentmark.core.validation.IValidationResultConsumer;
import net.certiv.fluentmark.core.validation.IValidationResultReporter;

public class LinkValidator implements IValidationResultReporter {

	private DefaultUriValidator defaultUriValidator;
	private IValidationResultConsumer issueConsumer;
	
	public LinkValidator() {
		defaultUriValidator = DefaultUriValidator.getDefaultUriValidator();
	}
	
	@Override
	public void setValidationResultConsumer(IValidationResultConsumer issueConsumer) {
		this.issueConsumer = issueConsumer;
		this.defaultUriValidator.setValidationResultConsumer(issueConsumer);
	}
	
	public void checkHttpUri(String uriText, IFile file, Map<String, String> contextDetails, int lineNumber, int offset) throws CoreException {
		if (uriText == null) {
			return;
		}
		
		// run all the URI validators from extensions, first
		List<IUriValidator> uriValidators = UriValidatorsManager.getInstance().getUriValidators();
		for (IUriValidator uriValidator : uriValidators) {
			if (uriValidator.isResponsibleFor(uriText)) {
				
				ISafeRunnable runnable = new ISafeRunnable() {
					@Override
					public void handleException(Throwable e) {
						Exception ex;
						if (e instanceof Exception) {
							ex = (Exception) e;
						} else {
							ex = new RuntimeException(e);
						}
						FluentCore.log(IStatus.WARNING,
								String.format("Could not run URI validator \"%s\".", uriValidator.getClass().getName()),
								ex);
					}

					@Override
					public void run() throws Exception {
						uriValidator.setValidationResultConsumer(issueConsumer);
						uriValidator.checkUri(uriText, file, contextDetails, lineNumber, offset,
								defaultUriValidator.getHttpClient());
					}
				};
				SafeRunner.run(runnable);

				// do not run other validations if we found a responsible validator from
				// extensions
				return;
			}
		}
		
		defaultUriValidator.checkUri(uriText, file, contextDetails, lineNumber, offset, defaultUriValidator.getHttpClient());
	}
	
}
