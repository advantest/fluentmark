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
import net.certiv.fluentmark.core.validation.IAnchorResolver;

public class AnchorResolversManager {
	
	private static final String EXTENSION_POINT_ID_ANCHOR_RESOLVER = FluentCore.PLUGIN_ID + ".anchorResolver";
	
	private static AnchorResolversManager INSTANCE = null;
	
	private final List<IAnchorResolver> resolvers = new ArrayList<>();
	
	private AnchorResolversManager() {
		this.init();
	}
	
	private void init() {
		resolvers.addAll(
				ExtensionsUtil.createExecutableExtensionsFor(
						EXTENSION_POINT_ID_ANCHOR_RESOLVER, 
						IAnchorResolver.class));
	}
	
	public static AnchorResolversManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AnchorResolversManager();
		}
		return INSTANCE;
	}
	
	public List<IAnchorResolver> getAnchorResolvers() {
		return Collections.unmodifiableList(resolvers);
	}

}
