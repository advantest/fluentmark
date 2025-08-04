/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.validation.visitor;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.extensionpoints.DocumentResolversManager;
import net.certiv.fluentmark.core.validation.FileValidator;

public class AdaptableFilesValidatingVisitor extends AbstractFileValidationVisitor {

	public AdaptableFilesValidatingVisitor(FileValidator validator, IProgressMonitor monitor) {
		super(validator, monitor);
	}
	
	@Override
	protected void handleFile(IFile file) {
		// Sometimes file.exists() returns true, although the file does no longer exist => We refresh the file system state to avoid that.
		try {
			file.refreshLocal(IResource.DEPTH_ZERO, this.progressMonitor);
		} catch (CoreException e) {
			FluentCore.log(IStatus.ERROR, "Could not refresh file status for file " + file.getLocation().toString(), e);
			return;
		}
		
		if (!file.exists()) {
			return;
		}
		
		if (!file.isAccessible()) {
			FluentCore.log(IStatus.WARNING, "Can't check file. It is not accessible. File path: " + file.getLocation().toString());
			return;
		}
		
		// Try finding IDocuments, e.g. from open editors (implemented by extensions)
		List<IDocumentResolver> resolvers = DocumentResolversManager.getInstance().getDocumentResolvers();
		Optional<IDocument> document = resolvers.stream()
			.filter(resolver -> resolver.isApplicableTo(file))
			.map(resolver -> resolver.resolveDocumentFor(file))
			.findFirst();
		
		// TODO schedule file validation if run in GUI
		if (document.isPresent()) {
			validator.performResourceValidation(document.get(), file, progressMonitor);
		} else {
			validator.performResourceValidation(file, this.progressMonitor);
		}
	}

}
