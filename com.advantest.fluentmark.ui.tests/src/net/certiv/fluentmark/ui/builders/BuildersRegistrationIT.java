/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.builders;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class BuildersRegistrationIT {
	
	private static IExtensionRegistry extensionRegistry;
	
	@BeforeAll
	public static void beforeAll() {
		extensionRegistry = Platform.getExtensionRegistry();
	}
	
	@AfterAll
	public static void afterAll() {
		extensionRegistry = null;
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
			IncrementalMarkdownValidationProjectBuilder.BUILDER_ID,
			IncrementalPlantUmlValidationProjectBuilder.BUILDER_ID
			})
	public void allBuildersRegistered(String builderId) {
		IExtension builderExtension = extensionRegistry.getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, builderId);
		
		assertNotNull(builderExtension);
	}

}
