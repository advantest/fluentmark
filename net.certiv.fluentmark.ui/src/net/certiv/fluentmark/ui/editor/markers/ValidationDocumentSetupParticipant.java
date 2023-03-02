/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.markers;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;

import java.util.ArrayList;
import java.util.List;

import net.certiv.fluentmark.core.convert.Partitions;

public class ValidationDocumentSetupParticipant implements IDocumentSetupParticipant {
	
	private List<ITypedRegionValidator> validators;
	
	public ValidationDocumentSetupParticipant() {
		this.validators = new ArrayList<>();
		this.validators.add(new LinkValidator());
	}
	
	// TODO Move marker calculation to FluentEditor class and to doSetInput method in order to validate a document directly after opening it?
	
	@Override
	public void setup(IDocument document) {
		// listen to document content changes
		document.addDocumentListener(new IDocumentListener() {
			
			private Job markerCalculatingJob;
			
			@Override
			public void documentChanged(DocumentEvent event) {
				IDocument document = event.getDocument();
				
				IEditorPart activeEditor = getActiveEditor();
				
				// if there is no active editor open, we cannot access the resource
				// and, thus, cannot create markers (or I don't know how, yet)
				
                if (activeEditor != null) {
                    IEditorInput editorInput = activeEditor.getEditorInput();
                    IResource resource = editorInput.getAdapter(IResource.class);
                    if (markerCalculatingJob != null) {
                    	markerCalculatingJob.cancel();
                    }
                    markerCalculatingJob = Job.create("Re-calculating problem markers",
                    		(ICoreRunnable) monitor -> calculateMarkers(document, resource)); 
                    markerCalculatingJob.setUser(false);
                    markerCalculatingJob.setPriority(Job.DECORATE);
                    
                    // set a delay before reacting to user action to handle continuous typing
                    markerCalculatingJob.schedule(500); 
                }
			}
			
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
				// do nothing here
			}
		});

	}
	
	private IEditorPart getActiveEditor() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null && !workbench.isClosing()) {
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window != null && !window.isClosing()) {
				IWorkbenchPage page = window.getActivePage();

				if (page != null) {
					return page.getActiveEditor();
				}
			}
		}
		
		return null;
	}
	
	private void calculateMarkers(IDocument document, IResource resource) throws CoreException {
		
		IMarker[] markers = resource.findMarkers(MarkerConstants.MARKER_ID_MARKDOWN_PROBLEM, true, IResource.DEPTH_INFINITE);
		for (IMarker marker: markers) {
			marker.delete();
		}
		
		ITypedRegion[] typedRegions;
		try {
			typedRegions = TextUtilities.computePartitioning(document, Partitions.PARTITIONING, 0, document.getLength(), false);
		} catch (BadLocationException e) {
			return;
		}
		
		for (ITypedRegion region: typedRegions) {
			for (ITypedRegionValidator validator: validators) {
				if (validator.isValidatorFor(region, document) ) {
					validator.validateRegion(region, document, resource);
				}
			}
		}
	}

}
