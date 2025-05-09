/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.actions;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class CopyToClipboardAction extends Action {

	private StructuredViewer fViewer;

	public CopyToClipboardAction(StructuredViewer viewer) {
		this();
		Assert.isNotNull(viewer);
		fViewer = viewer;
	}

	public CopyToClipboardAction() {
		setText(ActionMessages.CopyToClipboardAction_label);
		setToolTipText(ActionMessages.CopyToClipboardAction_tooltip);
		ISharedImages workbenchImages = PlatformUI.getWorkbench().getSharedImages();
		setDisabledImageDescriptor(workbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setImageDescriptor(workbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setHoverImageDescriptor(workbenchImages.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));

	}

	/**
	 * @param viewer The viewer to set.
	 */
	public void setViewer(StructuredViewer viewer) {
		fViewer = viewer;
	}

	@Override
	public void runWithEvent(Event event) {
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			String sel = null;
			if (event.widget instanceof Combo) {
				Combo combo = (Combo) event.widget;
				sel = combo.getText();
				Point selection = combo.getSelection();
				sel = sel.substring(selection.x, selection.y);
			} else if (event.widget instanceof Text) {
				Text text = (Text) event.widget;
				sel = text.getSelectionText();
			}
			if (sel != null) {
				if (sel.length() > 0) {
					copyToClipboard(sel, shell);
				}
				return;
			}
		}

		run();
	}

	@Override
	public void run() {
		Shell shell = getActiveWorkbenchShell();
		if (shell == null || fViewer == null) return;

		IBaseLabelProvider labelProvider = fViewer.getLabelProvider();
		String lineDelim = System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buf = new StringBuffer();
		Iterator<?> iter = getSelection();
		while (iter.hasNext()) {
			if (buf.length() > 0) {
				buf.append(lineDelim);
			}
			buf.append(getText(labelProvider, iter.next()));
		}

		if (buf.length() > 0) {
			copyToClipboard(buf.toString(), shell);
		}
	}

	private Shell getActiveWorkbenchShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	private static String getText(IBaseLabelProvider labelProvider, Object object) {
		if (labelProvider instanceof ILabelProvider)
			return ((ILabelProvider) labelProvider).getText(object);
		else if (labelProvider instanceof DelegatingStyledCellLabelProvider)
			return ((DelegatingStyledCellLabelProvider) labelProvider).getStyledStringProvider().getStyledText(object)
					.toString();
		else
			return object.toString();
	}

	private void copyToClipboard(String text, Shell shell) {
		text = TextProcessor.deprocess(text);
		Clipboard clipboard = new Clipboard(shell.getDisplay());
		try {
			copyToClipboard(clipboard, text, shell);
		} finally {
			clipboard.dispose();
		}
	}

	private Iterator<?> getSelection() {
		ISelection s = fViewer.getSelection();
		if (s instanceof IStructuredSelection) return ((IStructuredSelection) s).iterator();
		return Collections.emptyList().iterator();
	}

	private void copyToClipboard(Clipboard clipboard, String str, Shell shell) {
		try {
			clipboard.setContents(new String[] { str }, new Transfer[] { TextTransfer.getInstance() });
		} catch (SWTError ex) {
			if (ex.code != DND.ERROR_CANNOT_SET_CLIPBOARD) throw ex;
			String title = ActionMessages.CopyToClipboardAction_error_title;
			String message = ActionMessages.CopyToClipboardAction_error_message;
			if (MessageDialog.openQuestion(shell, title, message)) copyToClipboard(clipboard, str, shell);
		}
	}
}
