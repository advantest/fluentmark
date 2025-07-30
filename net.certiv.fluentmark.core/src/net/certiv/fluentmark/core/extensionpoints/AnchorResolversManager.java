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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.validation.IAnchorResolver;

public class AnchorResolversManager {
	
	private static final String EXTENSION_POINT_ID_ANCHOR_RESOLVER = FluentCore.PLUGIN_ID + ".anchorResolver";
	
	private static AnchorResolversManager INSTANCE = null;
	
	private final List<IAnchorResolver> resolvers = new ArrayList<>();
	
	private AnchorResolversManager() {
		this.init();
	}
	
	private void init() {
		IExtensionPoint validatorExtensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID_ANCHOR_RESOLVER);
		IExtension[] validatorExtensions = validatorExtensionPoint.getExtensions();
		for (IExtension validatorExtension: validatorExtensions) {
			IConfigurationElement[] configElements = validatorExtension.getConfigurationElements();
			
			try {
				for (IConfigurationElement configElement : configElements) {
					Object obj = configElement.createExecutableExtension("class");
					if (obj instanceof IAnchorResolver) {
						resolvers.add((IAnchorResolver) obj);
					}
				}
			} catch (CoreException e) {
				FluentCore.log(IStatus.ERROR, "Could not load IAnchorResolver extension", e);
			}
		}
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
