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

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.jobs.Job;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.extensionpoints.UriValidatorsManager;
import net.certiv.fluentmark.core.validation.IValidationResultConsumer;
import net.certiv.fluentmark.core.validation.IValidationResultReporter;

public class LinkValidator implements IValidationResultReporter {

	private DefaultUriValidator defaultUriValidator;
	
	public LinkValidator() {
		defaultUriValidator = DefaultUriValidator.getDefaultUriValidator();
	}
	
	@Override
	public void setValidationResultConsumer(IValidationResultConsumer issueConsumer) {
		this.defaultUriValidator.setValidationResultConsumer(issueConsumer);
		
		List<IUriValidator> uriValidators = UriValidatorsManager.getInstance().getUriValidators();
		for (IUriValidator uriValidator : uriValidators) {
			uriValidator.setValidationResultConsumer(issueConsumer);
		}
	}
	
	public void checkHttpUri(String uriText, IFile file, Map<String, String> contextDetails, int lineNumber, int offset) throws CoreException {
		if (uriText == null) {
			return;
		}
		
		// run all the URI validators from extensions, first
		List<IUriValidator> uriValidators = UriValidatorsManager.getInstance().getUriValidators();
		for (IUriValidator uriValidator : uriValidators) {
			if (uriValidator.isResponsibleFor(uriText)) {
				
				asyncCheckHttpUri(uriValidator, defaultUriValidator.getHttpClient(), uriText, file,
						contextDetails, lineNumber, offset);
				
				// do not run other validations if we found a responsible validator from extensions
				return;
			}
		}
		
		asyncCheckHttpUri(defaultUriValidator, defaultUriValidator.getHttpClient(), uriText, file,
				contextDetails, lineNumber, offset);
	}
	
	private void asyncCheckHttpUri(IUriValidator uriValidator, HttpClient httpClient, String uriText, IFile file,
			Map<String, String> contextDetails, int lineNumber, int offset) {
		Job checkingJob = Job.create("Checking URI " + uriText, new ICoreRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
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
						if (monitor.isCanceled()) {
							return;
						}
						
						uriValidator.checkUri(uriText, file, contextDetails, lineNumber, offset, httpClient);
					}
				};
				SafeRunner.run(runnable);
			}
			
		});
		
		checkingJob.setUser(false);
		checkingJob.setPriority(Job.DECORATE);
		checkingJob.setRule(file);
		checkingJob.schedule();
	}
}
