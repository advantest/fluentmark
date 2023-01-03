/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.editor;

import org.eclipse.jface.preference.IPreferenceStore;

import net.certiv.fluentmark.FluentUI;
import net.certiv.fluentmark.convert.IConfigurationProvider;
import net.certiv.fluentmark.preferences.Prefs;

public class ConfigurationProvider implements IConfigurationProvider {
	
	private final IPreferenceStore store = FluentUI.getDefault().getPreferenceStore();

	@Override
	public boolean useMathJax() {
		switch (store.getString(Prefs.EDITOR_MD_CONVERTER)) {
		case Prefs.KEY_PANDOC:
			return store.getBoolean(Prefs.EDITOR_PANDOC_MATHJAX);
		default:
			return false;
	}
	}

}
