/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.extensionpoints;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.validation.ITypedRegionValidator;

public class TypedRegionValidatorsManager {

private static final String EXTENSION_POINT_ID_TYPED_REGION_VALIDATOR = "net.certiv.fluentmark.ui.validator.typedRegion";
	
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
	
	private void init() {
		IExtensionPoint validatorExtensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID_TYPED_REGION_VALIDATOR);
		IExtension[] validatorExtensions = validatorExtensionPoint.getExtensions();
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
				FluentUI.log(IStatus.ERROR, "Could not load ITypedRegionValidator extension", e);
			}
		}
	}
	
	
	
	public List<ITypedRegionValidator> getTypedRegionValidators() {
		return Collections.unmodifiableList(validators);
	}
	
}
