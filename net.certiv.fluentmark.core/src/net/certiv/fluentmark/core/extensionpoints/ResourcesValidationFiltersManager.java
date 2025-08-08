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
import net.certiv.fluentmark.core.validation.visitor.IResourcesValidationFilter;

public class ResourcesValidationFiltersManager {
	
	private static final String EXTENSION_POINT_ID_RESOURCES_VALIDATION_FILTER = FluentCore.PLUGIN_ID + ".resourcesValidationFilter";
	
	private static ResourcesValidationFiltersManager INSTANCE = null;
	
	private final List<IResourcesValidationFilter> filters = new ArrayList<>();
	
	private ResourcesValidationFiltersManager() {
		this.init();
	}
	
	private void init() {
		filters.addAll(
				ExtensionsUtil.createExecutableExtensionsFor(
						EXTENSION_POINT_ID_RESOURCES_VALIDATION_FILTER, 
						IResourcesValidationFilter.class)); 
	}
	
	public static ResourcesValidationFiltersManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ResourcesValidationFiltersManager();
		}
		return INSTANCE;
	}
	
	public List<IResourcesValidationFilter> getResourceValidationFilters() {
		return Collections.unmodifiableList(filters);
	}

}
