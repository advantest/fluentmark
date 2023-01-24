/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.convert;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConverterIT {
	
	private IConfigurationProvider configProvider;
	private Converter converter;
	
	@TempDir File tempDir;
	
	@BeforeEach
	public void setUp() {
		configProvider = new ConfigurationProviderMock();
		converter = new Converter(configProvider);
	}
	
	@Test
	public void test() throws IOException {
		// given
		Path path = Paths.get("resources/parsing/feature-overview.md");
		String documentContent = Files.readString(path);
		IDocument document = new Document(documentContent);
		File tmpFile = new File(tempDir, "feature-overview.md");
		Files.copy(path, tmpFile.toPath());
		
		// when
		String result = converter.convert(new org.eclipse.core.runtime.Path(tmpFile.getAbsolutePath()), tempDir.getAbsolutePath(), document, Kind.VIEW);
		
		// then
		Assertions.assertNotNull(result);
		Assertions.assertFalse(result.isBlank());
		
		// TODO check result contents
	}

}
