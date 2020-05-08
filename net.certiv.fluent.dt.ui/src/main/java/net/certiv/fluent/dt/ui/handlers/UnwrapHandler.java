/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluent.dt.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import net.certiv.dsl.core.preferences.PrefsManager;
import net.certiv.fluent.dt.core.preferences.Prefs;
import net.certiv.fluent.dt.ui.editor.FluentEditor;

public class UnwrapHandler extends AbstractHandler {

	public UnwrapHandler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IEditorPart active = HandlerUtil.getActiveEditor(event);
		if (active instanceof FluentEditor) {
			FluentEditor editor = (FluentEditor) active;
			PrefsManager store = editor.getPrefsMgr();
			if (store.getBoolean(Prefs.FORMATTER_ENABLED)) {
				store.setValue(Prefs.FORMATTER_UNWRAP, true);
				ITextOperationTarget target = editor.getAdapter(ITextOperationTarget.class);
				if (target.canDoOperation(ISourceViewer.FORMAT)) {
					target.doOperation(ISourceViewer.FORMAT);
				}
				store.setValue(Prefs.FORMATTER_UNWRAP, false);
			}
		}
		return null;
	}
}
