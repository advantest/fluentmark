/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.builders.MarkdownFileValidationVisitor;

public class MarkdownFileCountingVisitor implements IResourceVisitor, IResourceDeltaVisitor {
	
	private int numMdFilesFound = 0;

	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (resource instanceof IContainer) {
			return MarkdownFileValidationVisitor.shouldVisitMembers((IContainer) resource);
		}
		
		if (resource instanceof IFile
				&& FileUtils.isMarkdownFile((IFile) resource)) {
			numMdFilesFound++;
		}
		
		return false;
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		return visit(resource);
	}
	
	public int getNumMdFilesFound() {
		return this.numMdFilesFound;
	}
}