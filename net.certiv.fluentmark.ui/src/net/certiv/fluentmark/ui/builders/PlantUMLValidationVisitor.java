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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.core.util.FluentPartitioningTools;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.editor.text.MarkdownPartioningTools;

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
	
	@Override
	public void checkFile(IDocument document, IFile file) {
		if (FileUtils.isMarkdownFile(file)) {
			IDocumentPartitioner partitioner = document.getDocumentPartitioner();
			
			if (document instanceof IDocumentExtension3) {
				partitioner = ((IDocumentExtension3) document).getDocumentPartitioner(MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
			}
			
			if (partitioner == null) {
				partitioner = MarkdownPartioningTools.getTools().createDocumentPartitioner();
				FluentPartitioningTools.setupDocumentPartitioner(document, partitioner, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
			}
			
			ITypedRegion[] markdownPartitions = MarkdownPartitions.computePartitions(document);
			for (ITypedRegion region: markdownPartitions) {
				if (region.getType().equals(MarkdownPartitions.UMLBLOCK)) {
					String umlBlockContents;
					try {
						umlBlockContents = document.get(region.getOffset(), region.getLength());
						Document umlBlockDocument = new Document(umlBlockContents);
						
						// TODO schedule job for calculating PlantUML markers in that region
					} catch (BadLocationException e) {
						// ignore
					} 
				}
			}
		} else {
			super.checkFile(document, file);
		}
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
