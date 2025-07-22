/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class DocumentEditorUtils {
	
	public static IDocument findDocumentFor(IFile file) {
		AbstractTextEditor editor = EditorUtils.findDirtyEditorFor(AbstractTextEditor.class, file);
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			IDocumentProvider provider = editor.getDocumentProvider();
			if (provider != null) {
				IDocument document = provider.getDocument(input);
				if ( document != null) {
					return document;
				}
			}
		}
		return null;
	}

}
