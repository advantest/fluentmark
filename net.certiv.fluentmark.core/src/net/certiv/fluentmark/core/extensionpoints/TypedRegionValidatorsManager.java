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
import net.certiv.fluentmark.core.validation.ITypedRegionValidator;

public class TypedRegionValidatorsManager {
	
	private static final String EXTENSION_POINT_ID_TYPED_REGION_VALIDATORS = FluentCore.PLUGIN_ID + ".partitionValidator";

	private static TypedRegionValidatorsManager INSTANCE = null;
	
	public static TypedRegionValidatorsManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new TypedRegionValidatorsManager();
		}
		return INSTANCE;
	}
	
	private final List<ITypedRegionValidator> validators = new ArrayList<>();
	
	private TypedRegionValidatorsManager() {
		this.init();
	}
	
	public List<ITypedRegionValidator> getTypedRegionValidators() {
		return Collections.unmodifiableList(validators);
	}
	
	private void init() {
		validators.addAll(
				ExtensionsUtil.createExecutableExtensionsFor(
						EXTENSION_POINT_ID_TYPED_REGION_VALIDATORS, 
						ITypedRegionValidator.class));
	}
}
