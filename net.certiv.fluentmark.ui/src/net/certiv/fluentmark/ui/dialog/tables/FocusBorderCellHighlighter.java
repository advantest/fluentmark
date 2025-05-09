/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.dialog.tables;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class FocusBorderCellHighlighter extends FocusCellHighlighter {

	public FocusBorderCellHighlighter(ColumnViewer viewer) {
		super(viewer);
		hookListener(viewer);
	}

	private void markFocusedCell(Event event, ViewerCell cell) {
		GC gc = event.gc;
		Rectangle rect = event.getBounds();
		gc.drawFocus(rect.x, rect.y, rect.width, rect.height);
		event.detail &= ~SWT.SELECTED;
	}

	private void removeSelectionInformation(Event event, ViewerCell cell) {}

	private void hookListener(final ColumnViewer viewer) {

		Listener listener = new Listener() {

			@Override
			public void handleEvent(Event event) {
				if ((event.detail & SWT.SELECTED) > 0) {
					ViewerCell focusCell = getFocusCell();
					ViewerRow row = focusCell.getViewerRow();

					Assert.isNotNull(row, "Internal structure invalid. Item without associated row is not possible."); //$NON-NLS-1$

					ViewerCell cell = row.getCell(event.index);

					if (focusCell == null || !cell.equals(focusCell)) {
						removeSelectionInformation(event, cell);
					} else {
						markFocusedCell(event, cell);
					}
				}
			}
		};
		viewer.getControl().addListener(SWT.EraseItem, listener);
	}

	/**
	 * @param cell the cell which is colored
	 * @return the color
	 */
	protected Color getSelectedCellBackgroundColor(ViewerCell cell) {
		return null;
	}

	/**
	 * @param cell the cell which is colored
	 * @return the color
	 */
	protected Color getSelectedCellForegroundColor(ViewerCell cell) {
		return null;
	}

	@Override
	protected void focusCellChanged(ViewerCell newCell, ViewerCell oldCell) {
		super.focusCellChanged(newCell, oldCell);

		// Redraw new area
		if (newCell != null) {
			Rectangle rect = newCell.getBounds();
			int x = newCell.getColumnIndex() == 0 ? 0 : rect.x;
			int width = newCell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
			// 1 is a fix for Linux-GTK
			newCell.getControl().redraw(x, rect.y - 1, width, rect.height + 1, true);
		}

		if (oldCell != null) {
			Rectangle rect = oldCell.getBounds();
			int x = oldCell.getColumnIndex() == 0 ? 0 : rect.x;
			int width = oldCell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
			// 1 is a fix for Linux-GTK
			oldCell.getControl().redraw(x, rect.y - 1, width, rect.height + 1, true);
		}
	}
}
