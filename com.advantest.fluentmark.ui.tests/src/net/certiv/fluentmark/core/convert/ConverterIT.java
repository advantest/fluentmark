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

public class ConverterIT extends AbstractConverterIT {
	
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
	
	@Test
	public void includeNonExistingPumlFileDoesntThrowException() throws Exception {
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
		assertFalse(result.isBlank());
		assertTrue(result.contains("Could not convert Markdown to HTML."));
	}

}
