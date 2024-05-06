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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class PlantUmlInMarkdownExtensionConverterIT extends AbstractConverterIT {
	
	@Test
	public void test() throws IOException {
		// given
		String srcMarkdownFile = "resources/feature-overview.md";
		String documentContent = readFileContentFrom(srcMarkdownFile);
		IDocument document = prepareDocument(documentContent);
		File testMarkdownFile = copyFileFromResourceToTempFolder(srcMarkdownFile, "feature-overview.md");
		
		// when
		String result = convert(testMarkdownFile, document);
		
		// then
		assertNotNull(result);
		assertFalse(result.isBlank());
		
		// TODO check result contents
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void includeNonExistingPumlFileDoesntThrowException(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		String markdownFileContent = "# Test\n\n![alt text](../diagrams/none.puml) ";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("include_missing_puml_file.md", markdownFileContent);
		
		String result = null;
		try {
			result = convert(markdownFile, document);
		} catch (Exception e) {
			fail("Converter is not expected to throw exceptions.", e);
		}
		
		
		assertNotNull(result);
		assertTrue(result.contains("<span style=\"color:red\">PlantUML file"));
		assertTrue(result.contains("does not exist.</span>"));
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void includedPlantUmlImagesRenderedToSvg(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		String markdownFileContent = "![Some diagram](classes.puml) ";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("include_puml_file.md", markdownFileContent);
		copyFileFromResourceToTempFolder("resources/PlantUML/classes.puml", "classes.puml");
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertTrue(result.matches("<figure>\\s*<svg(.|\\s)*<\\/svg>\\s*<figcaption.*>Some diagram<\\/figcaption>\\s*<\\/figure>\\s*"));
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void plantUmlCodeBlockRenderedToSvg(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		String markdownFileContent = readFileContentFrom("resources/PlantUML/classes.puml");
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("puml_code_block.md", markdownFileContent);
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertTrue(result.matches("<figure>\\s*<svg(.|\\s)*<\\/svg>(.|\\s)*<\\/figure>\\s*"));
	}

}
