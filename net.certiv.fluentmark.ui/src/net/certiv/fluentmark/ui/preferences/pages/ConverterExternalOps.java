/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.preferences.pages;

import static net.certiv.fluentmark.ui.preferences.Prefs.EDITOR_EXTERNAL_COMMAND;
import static net.certiv.fluentmark.ui.preferences.Prefs.KEY_EXTERNAL;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

import net.certiv.fluentmark.ui.preferences.AbstractOptionsBlock;

public class ConverterExternalOps extends AbstractOptionsBlock {

	private StringFieldEditor exCmd;

	public ConverterExternalOps(PrefPageConvert page, Composite parent, String title) {
		super(page, parent, title);
	}

	@Override
	protected void createControls(Composite comp) {
		exCmd = new StringFieldEditor(EDITOR_EXTERNAL_COMMAND, "", comp);
		addField(exCmd);
	}

	@Override
	public boolean validateSettings() {
		return getPage().isSelected(KEY_EXTERNAL) ? checkPathExe(exCmd.getStringValue(), null) : true;
	}
}
