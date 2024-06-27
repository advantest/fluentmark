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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.core.util.FluentPartitioningTools;
import net.certiv.fluentmark.ui.editor.text.MarkdownPartioningTools;

public class ConverterIT {
	
	private IConfigurationProvider configProvider;
	private Converter converter;
	
	@TempDir
	File tempDir;
	
	@BeforeEach
	public void setUp() {
		configProvider = new ConfigurationProviderMock();
		converter = new Converter(configProvider);
	}
	
	private void setupDocumentPartitioner(IDocument document) {
		FluentPartitioningTools.setupDocumentPartitioner(
				document,
				MarkdownPartioningTools.getTools().createDocumentPartitioner(),
				MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
	}
	
	@Test
	public void test() throws IOException {
		// given
		Path path = Paths.get("resources/feature-overview.md");
		String documentContent = Files.readString(path);
		IDocument document = new Document(documentContent);
		File tmpFile = new File(tempDir, "feature-overview.md");
		Files.copy(path, tmpFile.toPath());
		setupDocumentPartitioner(document);
		
		// when
		String result = converter.convert(
				new org.eclipse.core.runtime.Path(tmpFile.getAbsolutePath()),
				tempDir.getAbsolutePath(),
				document,
				Kind.VIEW);
		
		// then
		assertNotNull(result);
		assertFalse(result.isBlank());
		
		// TODO check result contents
	}
	
	@Test
	public void includeNonExistingPumlFileDoesntThrowException() throws Exception {
		String markdownFileContent = "# Test\n\n![alt text](../diagrams/none.puml) ";
		IDocument document = new Document(markdownFileContent);
		File markdownFile = new File(tempDir, "include_missing_puml_file.md");
		Path markdownFilePath = Files.createFile(markdownFile.toPath());
		Files.writeString(markdownFilePath, markdownFileContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		setupDocumentPartitioner(document);
		
		String result = null;
		try {
			result = converter.convert(
					new org.eclipse.core.runtime.Path(markdownFile.getAbsolutePath()),
					tempDir.getAbsolutePath(),
					document,
					Kind.VIEW);
		} catch (Exception e) {
			fail("Converter is not expected to throw exceptions.", e);
		}
		
		
		assertNotNull(result);
		assertFalse(result.isBlank());
		assertTrue(result.contains("Could not convert Markdown to HTML."));
	}

}
