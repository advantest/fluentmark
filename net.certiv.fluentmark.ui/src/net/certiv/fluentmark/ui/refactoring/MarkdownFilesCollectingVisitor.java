/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.builders.MarkdownFileValidationVisitor;
import net.certiv.fluentmark.ui.editor.FluentEditor;

public class MarkdownFilesCollectingVisitor implements IResourceVisitor {
	
	protected SubMonitor monitor;
	
	private Map<IFile, IDocument> filesDocumentsMap = new HashMap<>();;

	public void setMonitor(SubMonitor monitor) {
		this.monitor = monitor;
	}
	
	@Override
	public boolean visit(IResource resource) throws CoreException {
		// look into projects and visit Markdown files in doc/doc_... folders
		if (resource instanceof IContainer) {
			return MarkdownFileValidationVisitor.shouldVisitMembers((IContainer) resource);
		}
		
		// only work on *.md files
		if (resource instanceof IFile
				&& FileUtils.isMarkdownFile((IFile) resource)) {
			IFile markdownFile = (IFile) resource;
			
			if (markdownFile.isAccessible()) {
				// Check if the file is already open in a FluentMark editor.
				// If so, use the potentially modified, unsaved document instead of its saved version.
				FluentEditor openDirtyEditor = FluentEditor.findDirtyEditorFor(markdownFile);
				if (openDirtyEditor != null) {
					IDocument document = openDirtyEditor.getDocument();
					handleMarkdownFileAndDocument(document, markdownFile);
				} else {
					handleMarkdownFile(markdownFile);
				}
			}
			
			this.monitor.worked(1);
		}
		
		return false;
	}
	
	protected void handleMarkdownFile(IFile markdownFile) {
		// Sometimes file.exists() returns true, although the file does no longer exist => We refresh the file system state to avoid that.
		if (markdownFile != null) {
			try {
				markdownFile.refreshLocal(IResource.DEPTH_ZERO, this.monitor);
			} catch (CoreException e) {
				FluentUI.getDefault().getLog().error("Could not refresh file status for file " + markdownFile.getLocation().toString(), e);
			}
		}
		
		if (markdownFile == null || !markdownFile.exists()) {
			return;
		}
		
		filesDocumentsMap.put(markdownFile, null);
	}
	
	protected void handleMarkdownFileAndDocument(IDocument document, IFile markdownFile) {
		filesDocumentsMap.put(markdownFile, document);
	}
	
	public Map<IFile, IDocument> getCollectedMarkdownFiles() {
		return filesDocumentsMap;
	}

}
