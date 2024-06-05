/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.preferences.pages;

import static net.certiv.fluentmark.ui.preferences.Prefs.EDITOR_TXTMARK_EXTENDED;
import static net.certiv.fluentmark.ui.preferences.Prefs.EDITOR_TXTMARK_SAFEMODE;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Composite;

import net.certiv.fluentmark.ui.preferences.AbstractOptionsBlock;
import net.certiv.fluentmark.ui.util.SwtUtil;

public class ConverterTxtmarkOps extends AbstractOptionsBlock {

	public ConverterTxtmarkOps(PrefPageConvert page, Composite parent, String title) {
		super(page, parent, title);
	}

	@Override
	protected void createControls(Composite comp) {
		Composite bools = SwtUtil.makeComposite(comp, 3, 1);
		addField(new BooleanFieldEditor(EDITOR_TXTMARK_SAFEMODE, "Use safe mode", bools));
		addField(new BooleanFieldEditor(EDITOR_TXTMARK_EXTENDED, "Use extended profile", bools));
	}

	@Override
	public boolean validateSettings() {
		return true;
	}
}
