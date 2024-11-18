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
import org.junit.jupiter.api.io.TempDir;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.core.util.FluentPartitioningTools;
import net.certiv.fluentmark.ui.editor.text.MarkdownPartioningTools;

public abstract class AbstractConverterIT {
	
	protected ConfigurationProviderMock configProvider;
	protected Converter converter;
	
	@TempDir
	File tempDir;
	
	@BeforeEach
	public void setUp() {
		configProvider = new ConfigurationProviderMock();
		converter = new Converter(configProvider);
	}
	
	protected void setupDocumentPartitioner(IDocument document) {
		FluentPartitioningTools.setupDocumentPartitioner(
				document,
				MarkdownPartioningTools.getTools().createDocumentPartitioner(),
				MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
	}
	
	protected String readFileContentFrom(String resourcePath) throws IOException {
		Path path = Paths.get(resourcePath);
		return Files.readString(path);
	}
	
	protected File copyFileFromResourceToTempFolder(String srcFilePath, String targetFilePath) throws IOException {
		Path srcPath = Paths.get(srcFilePath);
		File targetFile = new File(tempDir, targetFilePath);
		Files.copy(srcPath, targetFile.toPath());
		return targetFile;
	}
	
	protected File createFileWithContent(String targetFilePath, String fileContent) throws IOException {
		File newFile = new File(tempDir, targetFilePath);
		Path newFilePath = Files.createFile(newFile.toPath());
		Files.writeString(newFilePath, fileContent, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		return newFile;
	}
	
	protected IDocument prepareDocument(String documentContent) {
		IDocument document = new Document(documentContent);
		setupDocumentPartitioner(document);
		return document;
	}
	
	protected String convert(File markdownFile, IDocument document) {
		return converter.convert(
				new org.eclipse.core.runtime.Path(markdownFile.getAbsolutePath()),
				tempDir.getAbsolutePath(),
				document,
				Kind.VIEW);
	}
	
}
