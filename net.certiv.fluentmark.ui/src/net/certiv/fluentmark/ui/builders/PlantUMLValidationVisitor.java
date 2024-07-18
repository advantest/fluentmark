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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.editor.FluentEditor;

public class PlantUMLValidationVisitor extends AbstractMarkerCalculationResourceVisitor {

	@Override
	public boolean visit(IResource resource) throws CoreException {
		/// look into projects
		if (resource instanceof IContainer) {
			return shouldVisitMembers((IContainer) resource);
		}
		
		// only work on *.md and *.puml files
		if (resource instanceof IFile
				&& (FileUtils.isMarkdownFile((IFile) resource) || FileUtils.isPumlFile((IFile) resource))) {
			IFile file = (IFile) resource;
			
			if (file.isAccessible()) {
				// Check if the file is already open in a FluentMark editor.
				// If so, validate the potentially modified, unsaved document instead of its saved version.
				// Otherwise the marker placements in the open editor may be incorrect.
				if (FileUtils.isMarkdownFile(file)) {
					FluentEditor openDirtyEditor = FluentEditor.findDirtyEditorFor(file);
					if (openDirtyEditor != null) {
						IDocument document = openDirtyEditor.getDocument();
						checkFile(document, file);
					} else {
						checkFile(file);
					}
				} else {
					checkFile(file);
				}
			}
			
			this.monitor.worked(1);
		}
		
		return false;
	}
	
	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		// TODO implement incremental build
		
		int deltaKind = delta.getKind();
		switch (deltaKind) {
			case IResourceDelta.CHANGED:
			case IResourceDelta.ADDED:
				break;
				
			case IResourceDelta.REMOVED:
				break;
	
			default:
				break;
		}
		return false;
	}
	
	static boolean shouldVisitMembers(IContainer container) {
		// look into projects
		if (container instanceof IProject) {
			return true;
		}
		
		// only look into doc folder and its children
		if (container instanceof IFolder
				&& FileUtils.isInDocFolder(container)) {
			return container.isAccessible();
		}
		
		return false;
	}
	
}
