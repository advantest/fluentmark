/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.certiv.fluentmark.ui.markers.MarkerConstants;


public class MarkdownLinkValidatorIT {
	
	private MarkdownLinkValidator linkValidator;
	private IFile file;
	private IDocument document;
	
	@TempDir
	File temporaryFolder;
	
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
		when(file.exists()).thenReturn(accessible);
		when(file.getFileExtension()).thenReturn(fileExtension);
		return file;
	}
	
	private IDocument validateDocument(String documentContents) throws Exception {
		document = createDocument(documentContents);
		
		ITypedRegion[] regions = findPartitions(document);
		
		for (ITypedRegion region: regions) {
			if (linkValidator.isValidatorFor(region, file)) {
				linkValidator.validateRegion(region, document, file);
			}
		}
		
		return document;
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
		
		// when
		document = validateDocument(fileContents);
		
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
		
		// when
		document = validateDocument(fileContents);
		
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
		
		// when
		document = validateDocument(fileContents);
		
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
	
	@Test
	public void testFileAndFolderPathValidation() throws Exception {
		// given
		String fileContents = """
				# Links to files and folders
				
				There is this important [file](important.txt).
				Here is a missing [file](missing.txt).
				
				Sometimes, we have links to folders like a [tests folder](tests/).
				But paths to folders have to end with a '/'.
				Thus, a [path without a trailing '/'](tests) produces a warning.
				
				[Missing folders][folder-missing], of course. produce warnings as well.
				Paths to files with trailing '/' also produce errors: [existing file](important.txt/).
				
				[folder-missing]: missing/
				""";
		linkValidator = new MarkdownLinkValidator();
		File newFile = new File(temporaryFolder, "markdown.md");
		assertTrue(newFile.createNewFile());
		assertTrue(new File(temporaryFolder, "important.txt").createNewFile());
		assertTrue(new File(temporaryFolder, "tests").mkdir());
		
		IPath path = Path.fromOSString(newFile.getAbsolutePath());
		when(file.getLocation()).thenReturn(path);
		when(file.createMarker(anyString())).thenAnswer(invocation -> {
				String markerType = invocation.getArgument(0);
				return TestMarker.create(file, markerType);
			});
		;
		
		// when
		document = validateDocument(fileContents);
		
		// then
		Optional<TestMarker> marker = findFirstMarkerForLine(3, file);
		assertFalse(marker.isPresent());
		
		marker = findFirstMarkerForLine(4, file);
		assertTrue(marker.isPresent());
		assertEquals(MarkerConstants.MARKER_ID_DOCUMENTATION_PROBLEM, marker.get().getType());
		String markerMessage = (String) marker.get().getAttribute(IMarker.MESSAGE);
		assertNotNull(markerMessage);
		assertTrue(markerMessage.contains("does not exist"));
		assertEquals(IMarker.SEVERITY_ERROR, marker.get().getAttribute(IMarker.SEVERITY, -1));
		
		marker = findFirstMarkerForLine(6, file);
		assertFalse(marker.isPresent());
		
		marker = findFirstMarkerForLine(8, file);
		assertTrue(marker.isPresent());
		assertEquals(MarkerConstants.MARKER_ID_DOCUMENTATION_PROBLEM, marker.get().getType());
		markerMessage = (String) marker.get().getAttribute(IMarker.MESSAGE);
		assertNotNull(markerMessage);
		assertTrue(markerMessage.contains("not a file"));
		assertTrue(markerMessage.contains("Please add a trailing '/'"));
		assertEquals(IMarker.SEVERITY_WARNING, marker.get().getAttribute(IMarker.SEVERITY, -1));
		
		marker = findFirstMarkerForLine(11, file);
		assertTrue(marker.isPresent());
		assertEquals(MarkerConstants.MARKER_ID_DOCUMENTATION_PROBLEM, marker.get().getType());
		markerMessage = (String) marker.get().getAttribute(IMarker.MESSAGE);
		assertNotNull(markerMessage);
		assertTrue(markerMessage.contains("not a file"));
		assertTrue(markerMessage.contains("remove the trailing '/'"));
		assertEquals(IMarker.SEVERITY_ERROR, marker.get().getAttribute(IMarker.SEVERITY, -1));
		
		marker = findFirstMarkerForLine(13, file);
		assertTrue(marker.isPresent());
		assertEquals(MarkerConstants.MARKER_ID_DOCUMENTATION_PROBLEM, marker.get().getType());
		markerMessage = (String) marker.get().getAttribute(IMarker.MESSAGE);
		assertNotNull(markerMessage);
		assertTrue(markerMessage.contains("does not exist"));
		assertEquals(IMarker.SEVERITY_ERROR, marker.get().getAttribute(IMarker.SEVERITY, -1));
	}
	
	private Optional<TestMarker> findFirstMarkerForLine(int lineNumer, IResource resource) {
		List<TestMarker> markers = TestMarker.MARKERS.get(resource);
		return markers.stream()
			.filter(marker -> {
				try {
					return marker.getAttribute(IMarker.LINE_NUMBER) instanceof Integer
						&& ((Integer) marker.getAttribute(IMarker.LINE_NUMBER)).intValue() == lineNumer;
				} catch (CoreException e) {
					e.printStackTrace();
					fail();
				}
				return false;
			})
			.findFirst();
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
	
	public static class TestMarker implements IMarker {
		
		public static final Map<IResource, List<TestMarker>> MARKERS = new HashMap<>();
		
		private static final String MESSAGE = "Not implemented in this test.";
		
		public static TestMarker create(IResource resource, String type) {
			TestMarker marker = new TestMarker(resource, type);
			
			List<TestMarker> resourceMarkers = MARKERS.get(resource);
			if (resourceMarkers == null) {
				resourceMarkers = new ArrayList<>();
				MARKERS.put(resource, resourceMarkers);
			}
			resourceMarkers.add(marker);
			
			return marker;
		}
		
		private final IResource resource;
		private final String type;
		private final Map<String, Object> attributes = new HashMap<>();
		
		private TestMarker(IResource resource, String type) {
			this.resource = resource;
			this.type = type;
		}

		@Override
		public <T> T getAdapter(Class<T> adapter) {
			throw new NotImplementedException(MESSAGE);
		}

		@Override
		public void delete() throws CoreException {
			throw new NotImplementedException(MESSAGE);
		}

		@Override
		public boolean exists() {
			throw new NotImplementedException(MESSAGE);
		}

		@Override
		public Object getAttribute(String attributeName) throws CoreException {
			return attributes.get(attributeName);
		}

		@Override
		public int getAttribute(String attributeName, int defaultValue) {
			Integer value = (Integer) attributes.get(attributeName);
			if (value == null) {
				return defaultValue;
			}
			return value.intValue();
		}

		@Override
		public String getAttribute(String attributeName, String defaultValue) {
			String value = (String) attributes.get(attributeName);
			if (value == null) {
				return defaultValue;
			}
			return value;
		}

		@Override
		public boolean getAttribute(String attributeName, boolean defaultValue) {
			Boolean value = (Boolean) attributes.get(attributeName);
			if (value == null) {
				return defaultValue;
			}
			return value.booleanValue();
		}

		@Override
		public Map<String, Object> getAttributes() throws CoreException {
			return attributes;
		}

		@Override
		public Object[] getAttributes(String[] attributeNames) throws CoreException {
			throw new NotImplementedException(MESSAGE);
		}

		@Override
		public long getCreationTime() throws CoreException {
			throw new NotImplementedException(MESSAGE);
		}

		@Override
		public long getId() {
			throw new NotImplementedException(MESSAGE);
		}

		@Override
		public IResource getResource() {
			return this.resource;
		}

		@Override
		public String getType() throws CoreException {
			return this.type;
		}

		@Override
		public boolean isSubtypeOf(String superType) throws CoreException {
			throw new NotImplementedException(MESSAGE);
		}

		@Override
		public void setAttribute(String attributeName, int value) throws CoreException {
			attributes.put(attributeName, value);
		}

		@Override
		public void setAttribute(String attributeName, Object value) throws CoreException {
			attributes.put(attributeName, value);
		}

		@Override
		public void setAttribute(String attributeName, boolean value) throws CoreException {
			attributes.put(attributeName, value);
		}

		@Override
		public void setAttributes(String[] attributeNames, Object[] values) throws CoreException {
			throw new NotImplementedException(MESSAGE);
		}

		@Override
		public void setAttributes(Map<String, ? extends Object> attributes) throws CoreException {
			throw new NotImplementedException(MESSAGE);
		}
		
	}

}
