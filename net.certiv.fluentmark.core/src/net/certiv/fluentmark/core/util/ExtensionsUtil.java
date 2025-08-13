/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.util;

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

public class ExtensionsUtil {
	
	public static IExtension[] getExtensionsFor(String extensionPointId) {
		if (extensionPointId == null || extensionPointId.isBlank()) {
			throw new IllegalArgumentException();
		}
		
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(extensionPointId);
		if (extensionPoint == null) {
			return null;
		}
		
		return extensionPoint.getExtensions();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T createExecutableExtensionFrom(IConfigurationElement configurationElement, String javaClassPropertyName, Class<T> instanceType) {
		if (configurationElement == null || javaClassPropertyName == null || instanceType == null) {
			throw new IllegalArgumentException();
		}
		
		try {
			return (T) configurationElement.createExecutableExtension(javaClassPropertyName);
		} catch (CoreException e) {
			FluentCore.log(IStatus.ERROR, "Could not create executable extension for type " + instanceType.getName(), e);
		}
		return null;
	}
	
	public static <T> T createExecutableExtensionFrom(IConfigurationElement configurationElement, Class<T> instanceType) {
		// assume the Java class is specified in a property named "class"
		return createExecutableExtensionFrom(configurationElement, "class", instanceType);
	}
	
	public static <T> List<T> createExecutableExtensionsFor(String extensionPointId, Class<T> instanceType) {
		IExtension[] extensions = getExtensionsFor(extensionPointId);
		
		if (extensions != null) {
			List<T> instances = new ArrayList<>();
			for (IExtension extension: extensions) {
				// assume, the classes to be instantiated are specified in the extension point's direct children (in the plugin.xml)
				IConfigurationElement[] configElements = extension.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {
					T instance = createExecutableExtensionFrom(configElement, instanceType);
					instances.add(instance);
				}
			}
			return instances;
		}
		
		return Collections.emptyList();
	}

}
