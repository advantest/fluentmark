/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;

import net.certiv.fluentmark.core.convert.ConverterType;
import net.certiv.fluentmark.core.convert.IConfigurationProvider;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.preferences.Prefs;
import net.certiv.fluentmark.ui.preferences.pages.PrefPageEditor;

public class ConfigurationProvider implements IConfigurationProvider {
	
	private final IPreferenceStore store = FluentUI.getDefault().getPreferenceStore();

	@Override
	public ConverterType getConverterType() {
		String converterKey = store.getString(Prefs.EDITOR_MD_CONVERTER);
		switch (converterKey) {
			case Prefs.KEY_PANDOC:
				return ConverterType.PANDOC;
			case Prefs.KEY_BLACKFRIDAY:
				return ConverterType.BLACKFRIDAY;
			case Prefs.KEY_MARDOWNJ:
				return ConverterType.MARKDOWNJ;
			case Prefs.KEY_PEGDOWN:
				return ConverterType.PEGDOWN;
			case Prefs.KEY_COMMONMARK:
				return ConverterType.COMMONMARK;
			case Prefs.KEY_TXTMARK:
				return ConverterType.TXTMARK;
			case Prefs.EDITOR_EXTERNAL_COMMAND:
				return ConverterType.OTHER;
			default:
			    return ConverterType.PANDOC;
		}
	}
	
	@Override
	public boolean isPlantUMLEnabled() {
		return store.getBoolean(Prefs.EDITOR_UMLMODE_ENABLED);
	}
	
	@Override
	public boolean isDotEnabled() {
		return store.getBoolean(Prefs.EDITOR_DOTMODE_ENABLED);
	}

	@Override
	public boolean useMathJax() {
		switch (store.getString(Prefs.EDITOR_MD_CONVERTER)) {
			case Prefs.KEY_PANDOC:
				return store.getBoolean(Prefs.EDITOR_PANDOC_MATHJAX);
			default:
				return false;
		}
	}

	@Override
	public String getPandocCommand() {
		return store.getString(Prefs.EDITOR_PANDOC_PROGRAM);
	}
	
	@Override
	public String getDotCommand() {
		return store.getString(Prefs.EDITOR_DOT_PROGRAM);
	}
	
	@Override
	public String getBlackFridayCommand() {
		return store.getString(Prefs.EDITOR_BLACKFRIDAY_PROGRAM);
	}
	
	@Override
	public String getExternalCommand() {
		return store.getString(PrefPageEditor.EDITOR_EXTERNAL_COMMAND);
	}

	@Override
	public boolean addTableOfContents() {
		return addTableOfContents(getConverterType());
	}
	
	@Override
	public boolean addTableOfContents(ConverterType converter) {
		switch (converter) {
			case PANDOC:
				return store.getBoolean(Prefs.EDITOR_PANDOC_ADDTOC);
			case BLACKFRIDAY:
				return store.getBoolean(Prefs.EDITOR_BLACKFRIDAY_ADDTOC);
			default:
				return false;
		}
	}

	@Override
	public boolean isSmartMode() {
		switch (getConverterType()) {
			case PANDOC:
				return !store.getBoolean(Prefs.EDITOR_PANDOC_SMART);
			case BLACKFRIDAY:
				return store.getBoolean(Prefs.EDITOR_BLACKFRIDAY_SMART);
			default:
				return false;
		}
	}

	@Override
	public boolean isTxtMarkSafeMode() {
		return store.getBoolean(Prefs.EDITOR_TXTMARK_SAFEMODE);
	}

	@Override
	public boolean isTxtMarkExtendedMode() {
		return store.getBoolean(Prefs.EDITOR_TXTMARK_EXTENDED);
	}

	@Override
	public String getCustomCssSettingsFile() {
		return store.getString(IConfigurationProvider.EDITOR_CSS_EXTERNAL);
	}

	@Override
	public String getBuiltinCssSettingsFile() {
		return store.getString(IConfigurationProvider.EDITOR_CSS_BUILTIN);
	}

}
