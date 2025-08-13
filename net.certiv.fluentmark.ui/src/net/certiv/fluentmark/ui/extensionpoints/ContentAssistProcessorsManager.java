/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.extensionpoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import net.certiv.fluentmark.core.util.ExtensionsUtil;
import net.certiv.fluentmark.ui.FluentUI;

public class ContentAssistProcessorsManager {
	
	private static final String EXTENSION_POINT_ID_CONTENT_ASSIST_PROCESSOR = FluentUI.PLUGIN_ID + ".content.assist.processor";
	
	private static ContentAssistProcessorsManager INSTANCE = null;
	
	private final List<IContentAssistProcessor> additionalContentAssistProcessors = new ArrayList<>();
	
	public static ContentAssistProcessorsManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ContentAssistProcessorsManager();
		}
		return INSTANCE;
	}
	
	private ContentAssistProcessorsManager() {
		this.init();
	}
	
	private void init() {
		additionalContentAssistProcessors.addAll(
				ExtensionsUtil.createExecutableExtensionsFor(
						EXTENSION_POINT_ID_CONTENT_ASSIST_PROCESSOR,
						IContentAssistProcessor.class));
	}
	
	public List<IContentAssistProcessor> getAdditionalContentAssistProcessors() {
		return Collections.unmodifiableList(additionalContentAssistProcessors);
	}

}
