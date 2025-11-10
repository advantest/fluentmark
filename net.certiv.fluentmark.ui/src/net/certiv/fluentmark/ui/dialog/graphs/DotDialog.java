/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.dialog.graphs;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;

import net.certiv.fluentmark.core.markdown.model.PagePart;


public class DotDialog extends MessageDialog {

	@SuppressWarnings("unused") private PagePart part;
	@SuppressWarnings("unused") private int style;

	/**
	 * Creates a new TableDialog with default SWT styles.
	 */
	public DotDialog(PagePart part) {
		this(part, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
	}

	/**
	 * Creates a new TableDialog with the given style.
	 *
	 * @param editor
	 * @param part
	 */
	public DotDialog(PagePart part, int style) {
		super(Display.getCurrent().getActiveShell(), "Dot Editor", null, null, MessageDialog.NONE,
				new String[] { "Done", "Cancel" }, 1);
		this.part = part;
		this.style = style;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	public String build() {
		return null;
	}

	@Override
	protected Control createCustomArea(final Composite parent) {
		Composite body = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(body);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(body);

		ToolBar bar = new ToolBar(body, SWT.HORIZONTAL);
		ToolBarManager barMgr = new ToolBarManager(bar);
		// barMgr.add(new AlignLeftAction());
		// barMgr.add(new AlignCenterAction());
		// barMgr.add(new AlignRightAction());
		// barMgr.add(new InsColBeforeAction());
		// barMgr.add(new InsColAfterAction());
		// barMgr.add(new RmvColAction());
		// barMgr.add(new InsRowAboveAction());
		// barMgr.add(new InsRowBelowAction());
		// barMgr.add(new RmvRowAction());
		barMgr.update(true);
		bar.pack();

		// viewer = new TableViewer(body, style);
		// GridDataFactory.fillDefaults().grab(true, true).span(2, 1).applyTo(viewer.getControl());
		//
		// table = viewer.getTable();
		// table.setHeaderVisible(true);
		// table.setLinesVisible(true);

		return body;

	}
}
