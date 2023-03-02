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

import org.eclipse.core.resources.IResource;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

public class ValidationDocumentSetupParticipant implements IDocumentSetupParticipant {
	
	@Override
	public void setup(IDocument document) {
		// listen to document content changes
		document.addDocumentListener(new IDocumentListener() {
			
			@Override
			public void documentChanged(DocumentEvent event) {
				IDocument document = event.getDocument();
				
				IEditorPart activeEditor = getActiveEditor();
				
				// if there is no active editor open, we cannot access the resource
				// and, thus, cannot create markers (or I don't know how, yet)
				
                if (activeEditor != null) {
                    IEditorInput editorInput = activeEditor.getEditorInput();
                    IResource resource = editorInput.getAdapter(IResource.class);
                    
                    MarkerCalculator.get().scheduleMarkerCalculation(document, resource);
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
	
}