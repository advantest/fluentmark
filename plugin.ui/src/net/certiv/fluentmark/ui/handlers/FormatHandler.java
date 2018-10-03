/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import net.certiv.fluentmark.core.preferences.Prefs;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.format.MdFormatter;

public class FormatHandler extends AbstractHandler {

	public FormatHandler() {
		super();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart active = HandlerUtil.getActiveEditor(event);
		if (active instanceof FluentEditor) {
			FluentEditor editor = (FluentEditor) active;
			IPreferenceStore store = editor.getPrefsStore();
			if (store.getBoolean(Prefs.EDITOR_FORMATTING_ENABLED)) {
				ITextSelection sel = (ITextSelection) editor.getSelectionProvider().getSelection();
				MdFormatter.format(editor, sel);
			}
		}
		return null;
	}
}
