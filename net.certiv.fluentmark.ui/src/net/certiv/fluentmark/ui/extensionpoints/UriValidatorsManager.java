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
import net.certiv.fluentmark.ui.editor.markers.IUriValidator;

public class UriValidatorsManager {
	
	private static final String EXTENSION_POINT_ID_URI_VALIDATOR = "net.certiv.fluentmark.ui.uriValidator";
	
	private static UriValidatorsManager INSTANCE = null;
	
	private final List<IUriValidator> validators = new ArrayList<>();
	
	private UriValidatorsManager() {
		this.init();
	}
	
	private void init() {
		IExtensionPoint validatorExtensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID_URI_VALIDATOR);
		IExtension[] validatorExtensions = validatorExtensionPoint.getExtensions();
		for (IExtension validatorExtension: validatorExtensions) {
			IConfigurationElement[] configElements = validatorExtension.getConfigurationElements();
			
			try {
				for (IConfigurationElement configElement : configElements) {
					Object obj = configElement.createExecutableExtension("class");
					if (obj instanceof IUriValidator) {
						validators.add((IUriValidator) obj);
					}
				}
			} catch (CoreException e) {
				FluentUI.log(IStatus.ERROR, "Could not load IUriValidator extension", e);
			}
		}
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
