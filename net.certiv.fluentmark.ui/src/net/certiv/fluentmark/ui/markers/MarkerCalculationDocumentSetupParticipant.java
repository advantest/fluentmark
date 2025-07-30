/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.markers;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import net.certiv.fluentmark.core.util.DocumentUtils;


/**
 * This class registers an {@link IDocumentListener} that triggers marker calculation each time
 * a document is modified (e.g. in an open FluentEditor).
 */
public class MarkerCalculationDocumentSetupParticipant implements IDocumentSetupParticipant {
	
	@Override
	public void setup(IDocument document) {
		// listen to document content changes
		document.addDocumentListener(new IDocumentListener() {
			
			@Override
			public void documentChanged(DocumentEvent event) {
				IDocument document = event.getDocument();
				
				// If we don't find a file, the reason might be that we got the event
				// because the document is being opened right now.
				// In that case, the FluentEditor triggers marker calculation anyway
				// after setting its input.
				IFile file = DocumentUtils.findFileFor(document);
				if (file != null && file.exists()) {
					MarkerCalculator.get().scheduleMarkerCalculation(document, file);
				}
			}
			
			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {
				// do nothing here
			}
		});

	}
	
}
