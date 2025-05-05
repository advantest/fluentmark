/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.preferences.pages;

import static net.certiv.fluentmark.ui.preferences.Prefs.EDITOR_PANDOC_ADDTOC;
import static net.certiv.fluentmark.ui.preferences.Prefs.EDITOR_PANDOC_MATHJAX;
import static net.certiv.fluentmark.ui.preferences.Prefs.EDITOR_PANDOC_PROGRAM;
import static net.certiv.fluentmark.ui.preferences.Prefs.EDITOR_PANDOC_SMART;
import static net.certiv.fluentmark.ui.preferences.Prefs.EDITOR_PANDOC_TEMPLATES;
import static net.certiv.fluentmark.ui.preferences.Prefs.KEY_PANDOC;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.preferences.AbstractOptionsBlock;
import net.certiv.fluentmark.ui.preferences.editors.ProgramFieldEditor;
import net.certiv.fluentmark.ui.util.SwtUtil;

public class ConverterPandocOps extends AbstractOptionsBlock {

	private static final String PANDOC = "pandoc";
	private static final String[] PD_MSG = { "Invalid Pandoc executable",
			"Full pathname of the Pandoc executable [pandoc|pandoc.exe]", "pandoc" };

	private ProgramFieldEditor pandocExe;
	private DirectoryFieldEditor pandocTmplDir;

	public ConverterPandocOps(PrefPageConvert page, Composite parent, String title) {
		super(page, parent, title);
	}

	@Override
	protected void createControls(Composite comp) {
		Label label = new Label(comp, SWT.NONE);
		label.setText("Version: " + FluentUI.getDefault().getConverter().getPandocVersion());
		
		SwtUtil.addSpacer(comp, 3);
		
		Composite bools = SwtUtil.makeComposite(comp, 3, 1);
		addField(new BooleanFieldEditor(EDITOR_PANDOC_SMART, "Use smart typography", bools));
		addField(new BooleanFieldEditor(EDITOR_PANDOC_ADDTOC, "Add table of contents", bools));
		addField(new BooleanFieldEditor(EDITOR_PANDOC_MATHJAX, "Enable Mathjax rendering", bools));

		SwtUtil.addSpacer(comp, 3);
		pandocExe = new ProgramFieldEditor(EDITOR_PANDOC_PROGRAM, "Program:", comp, PD_MSG);
		addField(pandocExe);

		pandocTmplDir = new DirectoryFieldEditor(EDITOR_PANDOC_TEMPLATES, "LaTex Templates:", comp);
		addField(pandocTmplDir);
	}

	@Override
	public boolean validateSettings() {
		boolean ok = true;
		if (getPage().isSelected(KEY_PANDOC)) {
			ok = ok && checkPathExe(pandocExe.getStringValue(), PANDOC);
			ok = ok && checkPathDir(pandocTmplDir.getStringValue(), true);
		}
		return ok;
	}
}
