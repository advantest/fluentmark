/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.builders;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.validation.MarkerCalculator;

public abstract class AbstractMarkerCalculationResourceVisitor implements IMarkerCalculationResourcesVisitor {
	
	protected SubMonitor monitor;

	@Override
	public void setMonitor(SubMonitor monitor) {
		this.monitor = monitor;
	}

	@Override
	public void checkFile(IFile file) {
		// Sometimes file.exists() returns true, although the file does no longer exist => We refresh the file system state to avoid that.
		if (file != null) {
			try {
				file.refreshLocal(IResource.DEPTH_ZERO, this.monitor);
			} catch (CoreException e) {
				FluentUI.getDefault().getLog().error("Could not refresh file status for file " + file.getLocation().toString(), e);
			}
		}
		
		if (file == null || !file.exists()) {
			return;
		}
		
		try {
			String fileContents = FileUtils.readFileContents(file);
			Document document = new Document(fileContents);
			
			checkFile(document, file);
		} catch (Exception e) {
			FluentUI.getDefault().getLog().error("Failed reading / valdating file " + file.getLocation().toString(), e);
		}
	}

	@Override
	public void checkFile(IDocument document, IFile file) {
		MarkerCalculator.get().scheduleMarkerCalculation(document, file);
	}

}
