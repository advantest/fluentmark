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
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IDocument;

/**
 * 
 */
public interface IMarkerCalculationResourcesVisitor extends IResourceVisitor, IResourceDeltaVisitor {

	void setMonitor(SubMonitor monitor);
	
	void checkFile(IFile file);
	
	void checkFile(IDocument document, IFile file);
}
