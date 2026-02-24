/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;

import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.util.EditorUtils;
import net.certiv.fluentmark.ui.views.FluentPreview;

public class JumpToElementInPreviewHandler extends OpenMdViewHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// open the preview first
		super.execute(event);
		
		IWorkbenchPage activePage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		IViewPart fluentPreview = activePage.findView(FluentPreview.ID);
		
		if (!(fluentPreview instanceof FluentPreview)) {
			return null;
		}
		
		FluentPreview preview = (FluentPreview) fluentPreview;
		
		FluentEditor editor = EditorUtils.getActiveFluentEditor();
		if (editor == null || editor.getSelectionProvider() == null) {
			return null;
		}
		
		ISelection selection = editor.getSelectionProvider().getSelection();
		
		if (!(selection instanceof TextSelection)) {
			return null;
		}
		
		TextSelection textSelection = (TextSelection) selection;
		int offset = textSelection.getOffset();
		int length = textSelection.getLength();
		
		preview.scrollToElement(offset, length);
		
		return null;
	}

}
