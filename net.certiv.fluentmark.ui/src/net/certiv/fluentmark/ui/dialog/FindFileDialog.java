/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.dialog;

import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;


public class FindFileDialog extends FilteredResourcesSelectionDialog {

	public FindFileDialog(Shell parentShell, IContainer container) {
		super(parentShell, false, container, IResource.FILE);
		setTitle("Find a file in Eclipse workspace");
		setMessage("Enter file name prefix, path prefix, or pattern (?, * or camel case):");
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		GridLayout parentLayout = (GridLayout)parent.getLayout();
		parentLayout.makeColumnsEqualWidth = false;

		new Label(parent, SWT.NONE).setLayoutData(new GridData(5, 0));
		parentLayout.numColumns++;

		Button okButton = createButton(parent, IDialogConstants.OK_ID, "Select file", true);
		Button cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		GridData cancelLayoutData = (GridData) cancelButton.getLayoutData();
		GridData okLayoutData = (GridData) okButton.getLayoutData();
		int buttonWidth = Math.max(cancelLayoutData.widthHint, okLayoutData.widthHint);
		cancelLayoutData.widthHint = buttonWidth;
		okLayoutData.widthHint = buttonWidth;
	}

	@Override
	protected void updateButtonsEnableState(IStatus status) {
		Button okButton = getOkButton();
		if (isButtonReady(okButton)) {
			if (status.matches(IStatus.ERROR)
					|| getSelectedItems().size() == 0) {
				okButton.setEnabled(false);
			} else if (getSelectedItems().size() == 1
					&& getSelectedItems().getFirstElement() instanceof IFile) {
				okButton.setEnabled(true);
			} else {
				okButton.setEnabled(false);
			}
		}
	}

	private boolean isButtonReady(Button button) {
		return button != null && !button.isDisposed();
	}
	
	@Override
	public IFile[] getResult() {
		Object[] result = super.getResult();

		if (result == null
				|| result.length < 1
				|| !(result[0] instanceof IFile))
			return null;

		return new IFile[] { (IFile) result[0] };
	}

}
