/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.convert;

public interface IConfigurationProvider {
	
	String CSS_RESOURCE_DIR = "resources/css/";
	String CSS = "css";
	String CSS_DEFAULT = "advantest.css";
	
	String EDITOR_CSS_BUILTIN = "net.certiv.fluentmark.markdown_Css";
	String EDITOR_CSS_EXTERNAL = "";
	
	
	ConverterType getConverterType();
	
	String getPandocCommand();
	
	String getDotCommand();
	
	String getBlackFridayCommand();
	
	String getExternalCommand();
	
	boolean useMathJax();
	
	boolean addTableOfContents();
	
	boolean isSmartMode();
	
	boolean isTxtMarkSafeMode();
	
	boolean isTxtMarkExtendedMode();
	
	boolean isDotEnabled();
	
	boolean isPlantUMLEnabled();

}
