/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.preferences.editors;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class ComboSelectFieldEditor extends ComboFieldEditor {

	private String key = ""; // current key key
	private String[][] entryNamesAndValues;

	public ComboSelectFieldEditor(String name, String labelText, String[][] entryNamesAndValues, Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
		this.entryNamesAndValues = entryNamesAndValues;
	}

	public String getKey() {
		return key;
	}

	public String getValue(String key) {
		for (String[] entry : entryNamesAndValues) {
			if (key.equals(entry[0])) return entry[1];
		}
		return "";
	}

	@Override
	protected void fireValueChanged(String property, Object oldValue, Object newValue) {
		key = (String) newValue;
		super.fireValueChanged(property, oldValue, newValue);
	}
}
