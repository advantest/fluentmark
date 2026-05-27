/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown.flexmark;

import org.jetbrains.annotations.NotNull;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlRenderer.Builder;
import com.vladsch.flexmark.util.data.MutableDataHolder;

public class SourcePositionAttributesRendererExtension implements HtmlRenderer.HtmlRendererExtension {

	@Override
	public void rendererOptions(@NotNull MutableDataHolder options) {
		// no changes
	}

	@Override
	public void extend(@NotNull Builder htmlRendererBuilder, @NotNull String rendererType) {
		htmlRendererBuilder.attributeProviderFactory(
				SourcePositionAttributesProvider.factory());
	}

	public static SourcePositionAttributesRendererExtension create() {
		return new SourcePositionAttributesRendererExtension();
	}
	
}
