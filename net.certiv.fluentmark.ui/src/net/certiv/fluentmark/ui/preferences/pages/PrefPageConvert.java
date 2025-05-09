/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.preferences.pages;

import org.eclipse.ui.IWorkbench;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.preferences.BaseFieldEditorPreferencePage;
import net.certiv.fluentmark.ui.preferences.Prefs;
import net.certiv.fluentmark.ui.preferences.editors.ComboSelectFieldEditor;
import net.certiv.fluentmark.ui.util.SwtUtil;

public class PrefPageConvert extends BaseFieldEditorPreferencePage implements Prefs {

	private static final String[][] converters = new String[][] {
			{ "Flexmark", KEY_FLEXMARK },
			{ "Pandoc", KEY_PANDOC }
	};

	private Composite base;
	private Composite stack;
	private StackLayout stackSel;
	private String selectedKey;

	private ComboSelectFieldEditor combo;
	private ConverterPandocOps pandoc;
	private ConverterFlexmarkOps flexmark;

	public PrefPageConvert() {
		super(GRID);
		setDescription("");
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(FluentUI.getDefault().getPreferenceStore());
	}

	/** Create fields controlling converter selection */
	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		base = SwtUtil.makeGroupComposite(parent, 3, 1, "Conversion filter selection");

		// Converter selection
		combo = new ComboSelectFieldEditor(EDITOR_MD_CONVERTER, "Converter:", converters, base);
		addField(combo);

		stack = SwtUtil.makeCompositeStack(parent, 3, 1);
		stackSel = (StackLayout) stack.getLayout();

		// stacked options
		pandoc = new ConverterPandocOps(this, stack, "Pandoc Options");
		flexmark = new ConverterFlexmarkOps(this, stack, "Flexmark Options");

		// init converter option selection
		updateConverter(stack, getPreferenceStore().getString(EDITOR_MD_CONVERTER));
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		validateSettings(getPreferenceStore().getString(EDITOR_MD_CONVERTER));
		return contents;
	}

	public boolean isSelected(String target) {
		return selectedKey.equals(target);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource() == combo && !event.getNewValue().equals(event.getOldValue())) {
			String key = combo.getKey();
			updateConverter(stack, key);
			validateSettings(key);
		}
		super.propertyChange(event);
	}

	private void updateConverter(Composite stack, String key) {
		this.selectedKey = key;

		switch (key) {
			case KEY_PANDOC:
				stackSel.topControl = pandoc.getFrame();
				break;
			
			case KEY_FLEXMARK:
				stackSel.topControl = flexmark.getFrame();
				break;

			default:
				stackSel.topControl = flexmark.getFrame();
				break;
		}
		stack.layout();
	}

	private void validateSettings(String key) {
		setErrorMessage(null);
		setMessage(null);

		switch (key) {
			case KEY_PANDOC:
				pandoc.validateSettings();
				break;
				
			case KEY_FLEXMARK:
				flexmark.validateSettings();
				break;

			default:
				flexmark.validateSettings();
				break;
		}
	}

	@Override
	protected void adjustSubLayout() {}

	@Override
	public void addField(FieldEditor editor) {
		super.addField(editor);
	}
}
