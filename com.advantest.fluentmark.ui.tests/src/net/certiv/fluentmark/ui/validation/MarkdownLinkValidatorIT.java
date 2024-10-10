/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class MarkdownLinkValidatorIT {
	
	private MarkdownLinkValidator linkValidator;
	private IFile file;
	private IDocument document;
	
	@BeforeEach
	public void setUp() {
		linkValidator = new MarkdownLinkValidatorFake();
		linkValidator = spy(linkValidator);
		file = prepareFileMock("md", true);
	}
	
	@AfterEach
	public void tearDown() {
		linkValidator = null;
		file = null;
		document = null;
	}
	
	private ITypedRegion[] findPartitions(IDocument document) throws Exception {
		assertNotNull(document);
		
		linkValidator.setupDocumentPartitioner(document, file);
		return linkValidator.computePartitioning(document, file);
	}
	
	private IDocument createDocument(String content) {
		return new Document(content);
	}
	
	private IFile prepareFileMock(String fileExtension, boolean accessible) {
		IFile file = mock(IFile.class);
		when(file.isAccessible()).thenReturn(accessible);
		when(file.getFileExtension()).thenReturn(fileExtension);
		return file;
	}
	
	@Test
	public void linksAndImagesAreFoundInMarkdown() throws Exception {
		// given
		String fileContents = """
				# Some heading
				
				Some text in paragraph
				followed by a [PlantUML](https://plantuml.com) link.
				
				More text with [Graphviz](https://graphviz.org/) multiple links [UML](https://www.omg.org/spec/UML/2.5.1/About-UML) in one line.
				
				![some image](file/path/to/image.png)
				
				More text.
				""";
		document = createDocument(fileContents);
		
		ITypedRegion[] regions = findPartitions(document);
		
		// when
		for (ITypedRegion region: regions) {
			if (linkValidator.isValidatorFor(region, file)) {
				linkValidator.validateRegion(region, document, file);
			}
		}
		
		// then
		verify(linkValidator, atLeastOnce()).validateLinkStatement(any(), eq(document), eq(file), eq("[PlantUML](https://plantuml.com)"), anyInt(), any());
		verify(linkValidator, atLeastOnce()).validateLinkStatement(any(), eq(document), eq(file), eq("[Graphviz](https://graphviz.org/)"), anyInt(), any());
		verify(linkValidator, atLeastOnce()).validateLinkStatement(any(), eq(document), eq(file), eq("[UML](https://www.omg.org/spec/UML/2.5.1/About-UML)"), anyInt(), any());
		verify(linkValidator, atLeastOnce()).validateLinkStatement(any(), eq(document), eq(file), eq("![some image](file/path/to/image.png)"), anyInt(), any());
	}
	
	@Test
	public void linkReferenceDefinitionsAreFoundInMarkdown() throws Exception {
		// given
		String fileContents = """
				[PlantUML]: https://plantuml.com
				
				# Some heading
				
				Some text in paragraph
				followed by a [PlantUML] link.
				
				More text with [DOT][Graphviz] multiple links [UML][] in one line.
				
				![some image](file/path/to/image.png)
				
				More text.
				
				[Graphviz]:
				   https://graphviz.org/
				   "Graphviz and DOT main web site"
				
				[UML]: https://www.omg.org/spec/UML/2.5.1/About-UML
				""";
		document = createDocument(fileContents);
		
		ITypedRegion[] regions = findPartitions(document);
		
		// when
		for (ITypedRegion region: regions) {
			if (linkValidator.isValidatorFor(region, file)) {
				linkValidator.validateRegion(region, document, file);
			}
		}
		
		// then
		verify(linkValidator, atLeastOnce()).validateLinkReferenceDefinitionStatement(any(), eq(document), eq(file), eq("[PlantUML]: https://plantuml.com"), anyInt());
		verify(linkValidator, atLeastOnce()).validateLinkReferenceDefinitionStatement(any(), eq(document), eq(file), eq("[Graphviz]:\n   https://graphviz.org/"), anyInt());
		verify(linkValidator, atLeastOnce()).validateLinkReferenceDefinitionStatement(any(), eq(document), eq(file), eq("[UML]: https://www.omg.org/spec/UML/2.5.1/About-UML"), anyInt());
	}
	
	@Test
	public void referenceLinksAreFoundInMarkdown() throws Exception {
		// given
		String fileContents = """
				[PlantUML]: https://plantuml.com
				
				# Some heading
				
				Some text in paragraph
				followed by a [PlantUML] link.
				
				More text with [DOT][Graphviz] multiple links [UML][] in one line.
				
				![some image](file/path/to/image.png)
				
				More text.
				
				[Graphviz]:
				   https://graphviz.org/
				   "Graphviz and DOT main web site"
				
				[UML]: https://www.omg.org/spec/UML/2.5.1/About-UML
				
				
				## More cases
				
				Some text with undefined reference link label [ref1].

				Other text [ref2][] sdfsdf sdf 
				sdf 
				sd f [ref3] sdf psidmfüp os,df 
				
				[sdf](sdf) sojdf ![sdf](sdf) sdf sd
				
				sdfimg odf [ref4] sdkf lsdf  [ref5][] sdf sdf [bla blub][ref6] sdf sdf
				sdfsdf 
				sdf sdf   [ref5] sdf sdf [ref6] sdf [ref4] sdf sdfd
				
				jhjkh [bla blub][ref1] oij  [bla blub][ref4]  pij oij  [bla blub][ref6] fsdds 
				
				[ref4]: https://www.plantuml.com

				[ref5]:
				   https://www.plantuml.com
				
				[ref6]:
				   https://www.plantuml.com
				   "text"
				""";
		document = createDocument(fileContents);
		
		ITypedRegion[] regions = findPartitions(document);
		
		// when
		for (ITypedRegion region: regions) {
			if (linkValidator.isValidatorFor(region, file)) {
				linkValidator.validateRegion(region, document, file);
			}
		}
		
		// then
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[PlantUML]"), eq("PlantUML"), anyInt());
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[DOT][Graphviz]"), eq("Graphviz"), anyInt());
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[UML][]"), eq("UML"), anyInt());
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[ref1]"), eq("ref1"), anyInt());
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[ref2][]"), eq("ref2"), anyInt());
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[ref3]"), eq("ref3"), anyInt());
		verify(linkValidator, times(2)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[ref4]"), eq("ref4"), anyInt());
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[ref5][]"), eq("ref5"), anyInt());
		verify(linkValidator, times(2)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[bla blub][ref6]"), eq("ref6"), anyInt());
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[ref5]"), eq("ref5"), anyInt());
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[ref6]"), eq("ref6"), anyInt());
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[bla blub][ref1]"), eq("ref1"), anyInt());
		verify(linkValidator, times(1)).validateReferenceLinkLabel(any(), eq(document), eq(file), eq("[bla blub][ref4]"), eq("ref4"), anyInt());
	}
	
	// This sub-class is needed to spy the calls of validate methods, Mockito cannot spy non-public methods
	public static class MarkdownLinkValidatorFake extends MarkdownLinkValidator {
		public void validateLinkStatement(ITypedRegion region, IDocument document, IFile file,
				String linkStatement, int linkStatementStartIndexInRegion, String regionContent) throws CoreException {
			// do nothing
		}
	
		public void validateLinkReferenceDefinitionStatement(ITypedRegion region, IDocument document, IFile file,
				String linkRefDefStatement, int linkStatementStartIndexInRegion) throws CoreException { 
			// do nothing
		}
		
		public void validateReferenceLinkLabel(ITypedRegion region, IDocument document, IFile file,
				String referenceLinkStatement, String linkLabel, int referenceLinkStatementStartIndexInRegion) throws CoreException {
			// do nothing
		}
	}

}
