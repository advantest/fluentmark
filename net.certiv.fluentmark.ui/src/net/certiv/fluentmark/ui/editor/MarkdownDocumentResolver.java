/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.core.validation.visitor.IDocumentResolver;

public class MarkdownDocumentResolver implements IDocumentResolver {

	@Override
	public boolean isApplicableTo(IFile file) {
		return file != null && FileUtils.isMarkdownFile(file);
	}

	@Override
	public IDocument resolveDocumentFor(IFile file) {
		// Check if the file is already open in a FluentMark editor.
		// If so, validate the potentially modified, unsaved document instead of its saved version.
		// Otherwise the marker placements in the open editor may be incorrect.
		FluentEditor openDirtyEditor = FluentEditor.findDirtyEditorFor(file);
		if (openDirtyEditor != null) {
			return openDirtyEditor.getDocument();
		}
		
		return null;
	}

}
