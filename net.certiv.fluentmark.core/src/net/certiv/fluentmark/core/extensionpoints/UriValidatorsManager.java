/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.extensionpoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.util.ExtensionsUtil;
import net.certiv.fluentmark.core.validation.uri.IUriValidator;

public class UriValidatorsManager {
	
	private static final String EXTENSION_POINT_ID_URI_VALIDATOR = FluentCore.PLUGIN_ID + ".uriValidator";
	
	private static UriValidatorsManager INSTANCE = null;
	
	private final List<IUriValidator> validators = new ArrayList<>();
	
	private UriValidatorsManager() {
		this.init();
	}
	
	private void init() {
		validators.addAll(
				ExtensionsUtil.createExecutableExtensionsFor(
						EXTENSION_POINT_ID_URI_VALIDATOR, 
						IUriValidator.class));
	}
	
	public static UriValidatorsManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new UriValidatorsManager();
		}
		return INSTANCE;
	}
	
	public List<IUriValidator> getUriValidators() {
		return Collections.unmodifiableList(validators);
	}

}
