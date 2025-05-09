/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
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
import net.certiv.fluentmark.core.util.FileUtils;

public class ConfigurationProviderMock implements IConfigurationProvider {
	
	private static final ConverterType DEFAULT_CONVERTER_TYPE = ConverterType.FLEXMARK;
	
	private ConverterType converterType;
	
	
	public ConfigurationProviderMock() {
		this(DEFAULT_CONVERTER_TYPE);
	}
	
	public ConfigurationProviderMock(ConverterType converterType) {
		this.converterType = converterType;
	}

	@Override
	public ConverterType getConverterType() {
		return this.converterType;
	}
	
	public void setConverterType(ConverterType converterType) {
		this.converterType = converterType;
	}

	@Override
	public String getPandocCommand() {
		if (FileUtils.isOsLinuxOrUnix()) {
			return "/usr/bin/pandoc";
		} else if (FileUtils.isOsMacOs()) {
			return "/usr/local/bin/pandoc";
		} else if (FileUtils.isOsWindows()) {
			return "C:\\Program Files\\Pandoc\\pandoc.exe";
		} else {
			throw new IllegalStateException("Pandoc command not found.");
		}
	}

	@Override
	public String getDotCommand() {
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
			url = new URL(url.toString().replace(" ", "%20"));
			return url.toURI().toString();
		} catch (IOException | URISyntaxException e) {
			return null;
		}
	}

	@Override
	public String getPreferredLineEnding() {
		return "\n";
	}

}
