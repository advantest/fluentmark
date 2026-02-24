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

import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.AttributeProviderFactory;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.CoreNodeRenderer;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.html.MutableAttributes;

/**
 * Attribute provider for flexmark for adding source code position details to rendered HTML elements
 * for enabling navigation from source code to the rendered element in HTML / Markdown preview and vice versa.
 */
public class SourcePositionAttributesProvider implements AttributeProvider {

	@Override
	public void setAttributes(Node node, AttributablePart part, MutableAttributes attributes) {
		if (part == AttributablePart.NODE || part == AttributablePart.LINK
				|| part == CoreNodeRenderer.LOOSE_LIST_ITEM  || part == CoreNodeRenderer.TIGHT_LIST_ITEM
				/*|| part == CoreNodeRenderer.CODE_CONTENT*/) {
			attributes.addValue("source-offset", String.valueOf(node.getStartOffset()));
			attributes.addValue("source-length", String.valueOf(node.getEndOffset() - node.getStartOffset()));
		}
	}
	
	public static AttributeProviderFactory factory() {
		return new IndependentAttributeProviderFactory() {
			@Override
			public SourcePositionAttributesProvider apply(LinkResolverContext context) {
				return new SourcePositionAttributesProvider();
			}
		};
	}
}
