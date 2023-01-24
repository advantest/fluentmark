/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.convert;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.osgi.framework.Bundle;

import java.net.URISyntaxException;
import java.net.URL;

import java.io.IOException;

import net.certiv.fluentmark.core.FluentCore;

public class ConfigurationProviderMock implements IConfigurationProvider {

	@Override
	public ConverterType getConverterType() {
		return ConverterType.PANDOC;
	}

	@Override
	public String getPandocCommand() {
		return "/usr/local/bin/pandoc";
	}

	@Override
	public String getDotCommand() {
		return "";
	}

	@Override
	public String getBlackFridayCommand() {
		return "";
	}

	@Override
	public String getExternalCommand() {
		return "";
	}

	@Override
	public boolean useMathJax() {
		return true;
	}

	@Override
	public boolean addTableOfContents() {
		return false;
	}

	@Override
	public boolean addTableOfContents(ConverterType converter) {
		return false;
	}

	@Override
	public boolean isSmartMode() {
		return false;
	}

	@Override
	public boolean isTxtMarkSafeMode() {
		return false;
	}

	@Override
	public boolean isTxtMarkExtendedMode() {
		return false;
	}

	@Override
	public boolean isDotEnabled() {
		return true;
	}

	@Override
	public boolean isPlantUMLEnabled() {
		return true;
	}

	@Override
	public String getCustomCssSettingsFile() {
		return null;
	}

	@Override
	public String getBuiltinCssSettingsFile() {
		Bundle bundle = Platform.getBundle(FluentCore.PLUGIN_ID);
		URL url = FileLocator.find(bundle, new Path(IConfigurationProvider.CSS_RESOURCE_DIR + IConfigurationProvider.CSS_DEFAULT), null);
		try {
			url = FileLocator.toFileURL(url);
			return url.toURI().toString();
		} catch (IOException | URISyntaxException e) {
			return null;
		}
	}

}
