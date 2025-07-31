/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.convert;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class PlantUmlInMarkdownExtensionConverterIT extends AbstractConverterIT {
	
	
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
		String resultWithoutLinebreaks = result.replace("\n", " ");
		assertTrue(resultWithoutLinebreaks.contains("<span style=\"color:red\">PlantUML file"));
		assertTrue(resultWithoutLinebreaks.contains("does not exist.</span>"));
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
		
		Matcher regexMatcher = Pattern.compile("<figure>\\s*<svg.*<\\/svg>\\s*<figcaption.*>Some diagram<\\/figcaption>\\s*<\\/figure>\\s*", Pattern.DOTALL).matcher(result);
		assertTrue(regexMatcher.find());
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
		
		Matcher regexMatcher = Pattern.compile("<figure>\\s*<svg.*<\\/svg>.*<\\/figure>\\s*", Pattern.DOTALL).matcher(result);
		assertTrue(regexMatcher.find());
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void ensureCorrectPlantUmlSettings(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		String markdownFileContent = """
				@startuml
					title PlantUML security profile: %getenv("PLANTUML_SECURITY_PROFILE")
					footer PlantUML version: %version()
				@enduml
				""";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("read-plantuml-security-profile.md", markdownFileContent);
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertTrue(result.contains("PlantUML security profile: UNSECURE"));
	}

}
