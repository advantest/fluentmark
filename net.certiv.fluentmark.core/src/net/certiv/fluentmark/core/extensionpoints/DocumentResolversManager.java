/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.extensionpoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.util.ExtensionsUtil;
import net.certiv.fluentmark.core.validation.visitor.IDocumentResolver;

public class DocumentResolversManager {
	
	private static final String EXTENSION_POINT_ID_DOCUMENT_RESOLVER = FluentCore.PLUGIN_ID + ".documentResolver";
	
	private static DocumentResolversManager INSTANCE = null;
	
	private final List<IDocumentResolver> resolvers = new ArrayList<>();
	
	private DocumentResolversManager() {
		this.init();
	}
	
	private void init() {
		resolvers.addAll(
				ExtensionsUtil.createExecutableExtensionsFor(
						EXTENSION_POINT_ID_DOCUMENT_RESOLVER, 
						IDocumentResolver.class));
	}
	
	public static DocumentResolversManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DocumentResolversManager();
		}
		return INSTANCE;
	}
	
	public List<IDocumentResolver> getDocumentResolvers() {
		return Collections.unmodifiableList(resolvers);
	}

}
