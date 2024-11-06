/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.hyperlinks;

import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.browser.IWebBrowser;

import net.certiv.fluentmark.ui.util.EditorUtils;

public class FluentUrlHyperlink extends org.eclipse.jface.text.hyperlink.URLHyperlink {

	/**
	 * Creates a new URL hyperlink.
	 *
	 * @param region the region
	 * @param urlString the URL string
	 */
	public FluentUrlHyperlink(IRegion region, String urlString) {
		super(region, urlString);
	}
	
	@Override
	public void open() {
		// use our custom browser to open links in Markdown files
		IWebBrowser browser = EditorUtils.openUrlInWebBrowser(getURLString());
		
		if (browser == null) {
			super.open();
		}
	}
}
