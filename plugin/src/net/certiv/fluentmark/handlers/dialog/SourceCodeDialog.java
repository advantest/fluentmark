/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2022 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.handlers.dialog;

import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SourceCodeDialog extends Dialog {
	
	private final String sourceCode;
	private final String title;

	public SourceCodeDialog(Shell parentShell, String title, String sourceCode) {
		super(parentShell);
		this.sourceCode = sourceCode;
		this.title = title;
	}
	
	@Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        Text textField = new Text(container, SWT.MULTI | SWT.READ_ONLY);
        textField.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        textField.setText(sourceCode);
        return container;
    }
	
	@Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(this.title);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(1200, 900);
    }
    
    @Override
	protected boolean isResizable() {
		return true;
	}

}
