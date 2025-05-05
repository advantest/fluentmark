/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.handlers.dialog;

import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SourceCodeDialog extends Dialog {
	
	private static final String CONTENTS_SOURCE_CODE = "contents: 'Ready...',";
	
	private final String sourceCode;
	private final String contents;
	private final String title;
	
	public SourceCodeDialog(Shell parentShell, String title, String pageSourceCode, String contentsSourceCode) {
		super(parentShell);
		this.sourceCode = pageSourceCode;
		this.contents = contentsSourceCode;
		this.title = title;
	}
	
	@Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        
        String text = this.sourceCode;
        
        if (this.contents != null && !this.contents.isBlank()) {
        	String replacementContent = this.contents.lines()
        			.map(line -> "\t\t\t" + line)
        			.collect(Collectors.joining("\n"));
        	
        	replacementContent = CONTENTS_SOURCE_CODE.replace("'Ready...'", "`" + replacementContent + "`");
        	text = text.replace(CONTENTS_SOURCE_CODE, replacementContent);
        	
        	// clean up code if browser engine is edge
        	if (!text.contains("<span id=\"app\" v-html=\"contents\">")) {
        		text = text.replace("<span id=\"app\">", "<span id=\"app\" v-html=\"contents\">");
        	}
        }
        
        Text textField = new Text(container, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
            textField.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
            textField.setText(text);
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
