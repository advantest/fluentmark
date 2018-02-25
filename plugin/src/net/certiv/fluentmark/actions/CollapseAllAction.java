/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;

import net.certiv.fluentmark.FluentUI;
import net.certiv.fluentmark.outline.FluentOutlinePage.OutlineViewer;
import net.certiv.fluentmark.FluentMkImages;

public class CollapseAllAction extends Action {

	private TreeViewer viewer;

	public CollapseAllAction(OutlineViewer viewer) {
		super(ActionMessages.CollapseAllAction_0);
		this.viewer = viewer;

		setToolTipText(ActionMessages.CollapseAllAction_1);
		FluentUI.getDefault().getImageProvider().setImageDescriptors(this, FluentMkImages.OBJ,
				FluentMkImages.IMG_MENU_COLLAPSE_ALL);
	}

	@Override
	public void run() {
		if (viewer != null) {
			viewer.collapseAll();
		}
	}
}
