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
import net.certiv.fluentmark.core.validation.ITypedRegionValidator;

public class TypedRegionValidatorsManager {
	
	private static final String EXTENSION_POINT_ID_TYPED_REGION_VALIDATORS = "net.certiv.fluentmark.core.validators";

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
		IExtensionPoint validatorsExtensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID_TYPED_REGION_VALIDATORS);
		IExtension[] validatorExtensions = validatorsExtensionPoint.getExtensions();
		for (IExtension validatorExtension: validatorExtensions) {
			IConfigurationElement[] configElements = validatorExtension.getConfigurationElements();
			
			try {
				for (IConfigurationElement configElement : configElements) {
					Object obj = configElement.createExecutableExtension("class");
					if (obj instanceof ITypedRegionValidator) {
						validators.add((ITypedRegionValidator) obj);
					}
				}
			} catch (CoreException e) {
				FluentCore.log(IStatus.ERROR, "Could not load ITypedRegionValidator from extension", e);
			}
		}
	}
}
