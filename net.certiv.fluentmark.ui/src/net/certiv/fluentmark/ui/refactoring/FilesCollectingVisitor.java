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
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.util.EditorUtils;

public class FilesCollectingVisitor implements IResourceVisitor {
	
	private final SubMonitor monitor;
	
	private final Map<IFile, IDocument> markdownFilesDocumentsMap = new HashMap<>();
	private final Map<IFile, IDocument> plantUmlFilesDocumentsMap = new HashMap<>();
	private final Map<IFile, IDocument> svgFilesDocumentsMap      = new HashMap<>();
	
	private final boolean collectMarkdownFiles;
	private final boolean collectPlantUmlFiles;
	private final boolean collectSvgFiles;
	
	public FilesCollectingVisitor(SubMonitor monitor, boolean collectMarkdownFiles, boolean collectPlantUmlFiles, boolean collectSvgFiles) {
		Assert.isNotNull(monitor);
		
		this.monitor = monitor;
		this.collectMarkdownFiles = collectMarkdownFiles;
		this.collectPlantUmlFiles = collectPlantUmlFiles;
		this.collectSvgFiles = collectSvgFiles;
	}

	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (monitor.isCanceled()) {
			return false;
		}
		
		if (resource instanceof IContainer) {
			return resource.isAccessible();
		}
		
		if (!(resource instanceof IFile) || !((IFile) resource).isAccessible()) {
			return false;
		}
		
		IFile file = (IFile) resource;
		
		if (collectMarkdownFiles && FileUtils.isMarkdownFile(file)) {
			// Check if the file is already open in a FluentMark editor.
			// If so, use the potentially modified, unsaved document instead of its saved version.
			FluentEditor openDirtyEditor = FluentEditor.findDirtyEditorFor(file);
			if (openDirtyEditor != null) {
				markdownFilesDocumentsMap.put(file, openDirtyEditor.getDocument());
			} else {
				markdownFilesDocumentsMap.put(file, null);
			}
			
			this.monitor.worked(1);
		} else if (collectPlantUmlFiles && FileUtils.isPumlFile(file)) {
			// Check if the file is already open in some text editor.
			// If so, use the potentially modified, unsaved document instead of its saved version.
			IDocument document = EditorUtils.findDocumentFromDirtyTextEditorFor(file);
			plantUmlFilesDocumentsMap.put(file, document);
		} else if (collectSvgFiles && FileUtils.isSvgFile(file)) {
			svgFilesDocumentsMap.put(file, null);
		}
		
		return false;
	}
	
	public Map<IFile, IDocument> getCollectedMarkdownFiles() {
		return markdownFilesDocumentsMap;
	}
	
	public Map<IFile, IDocument> getCollectedPlantUmlFiles() {
		return plantUmlFilesDocumentsMap;
	}
	
	public Map<IFile, IDocument> getCollectedSvgFiles() {
		return svgFilesDocumentsMap;
	}

}
