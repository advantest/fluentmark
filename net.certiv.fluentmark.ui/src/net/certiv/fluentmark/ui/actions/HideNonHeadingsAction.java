/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.actions;

import org.eclipse.jface.action.Action;

import net.certiv.fluentmark.ui.FluentImages;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.outline.FluentOutlinePage.OutlineViewer;
import net.certiv.fluentmark.ui.preferences.Prefs;

public class HideNonHeadingsAction extends Action {
	
	private final OutlineViewer viewer;
	private FluentEditor editor;
	
	public HideNonHeadingsAction(OutlineViewer viewer, FluentEditor editor) {
		super(ActionMessages.HeadingsOnlyAction_0);
		this.viewer = viewer;
		this.editor = editor;

		setToolTipText(ActionMessages.HeadingsOnlyAction_1);
		FluentUI.getDefault().getImageProvider().setImageDescriptors(this, FluentImages.OBJ,
				FluentImages.DESC_OBJ_HEADER);
		
		boolean headingsOnlyFilterEnabled = editor.getPrefsStore().getBoolean(Prefs.OUTLINE_VIEW_HEADINGS_ONLY);
		setChecked(headingsOnlyFilterEnabled);
		viewer.setHeadingsOnlyFilter(headingsOnlyFilterEnabled);
	}
	
	@Override
	public void run() {
		boolean headingsOnlyFilterEnabled = isChecked();
		
		editor.getPrefsStore().setValue(Prefs.OUTLINE_VIEW_HEADINGS_ONLY, headingsOnlyFilterEnabled);
		
		viewer.setHeadingsOnlyFilter(headingsOnlyFilterEnabled);
	}

}
