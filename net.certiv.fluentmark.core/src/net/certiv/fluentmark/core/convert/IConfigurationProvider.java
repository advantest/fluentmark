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

import net.certiv.fluentmark.core.FluentCore;

public interface IConfigurationProvider {
	
	String CSS_RESOURCE_DIR = "resources/css/";
	String CSS = "css";
	String CSS_DEFAULT = "advantest.css";
	
	String EDITOR_CSS_BUILTIN = FluentCore.PLUGIN_ID + ".markdown_Css";
	String EDITOR_CSS_EXTERNAL = "";
	
	
	ConverterType getConverterType();
	
	String getPandocCommand();
	
	String getDotCommand();
	
	boolean useMathJax();
	
	boolean addTableOfContents();
	
	boolean addTableOfContents(ConverterType converter);
	
	boolean isSmartMode();
	
	boolean isDotEnabled();
	
	boolean isPlantUMLEnabled();
	
	String getCustomCssSettingsFile();
	
	String getBuiltinCssSettingsFile();

}
