/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2022 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.model;



import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import junit.framework.TestCase;
import net.certiv.fluentmark.convert.UmlGen;


public class PageRootTest extends TestCase {
	
//	private static SWTBot bot;
//	
//	@BeforeEach
//    public void beforeClass() throws Exception {
//        // don't use SWTWorkbenchBot here which relies on Platform 3.x
//        bot = new SWTBot();
//    }
	
	private PageRoot pageModel = null;
	private OffsetProviderMock offsetProvider = null;
	private IFile markdownFile = null;
	
	@Before
	public void setUp() {
		offsetProvider = new OffsetProviderMock();
		pageModel = new PageRoot(offsetProvider, "\n");
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject("Test-FluentMark-Project");
		IFolder folder = project.getFolder("src");
		markdownFile = folder.getFile("test.md");
	}
	
	@After
	public void tearDown() {
		pageModel.dispose();
		offsetProvider = null;
		pageModel = null;
		markdownFile = null;
	}
	
	private static class OffsetProviderMock implements IOffsetProvider {
		
		private int cursorOffset = 0;
		
		public void setCursorOffset(int newOffset) {
			this.cursorOffset = newOffset;
		}

		@Override
		public int getCursorOffset() {
			return cursorOffset;
		}
		
	}
	
	@Test
	public void testParsing_Bug_HMR_43() throws IOException {
		// given
		String text = readTextFromFile("resources/md/bug-hmr-43.md");
		
		// when
		pageModel.updateModel(markdownFile, text);
		
		// then
		List<IParent> documentModelChildren = pageModel.getChildList();
		assertEquals(1, documentModelChildren.size());
		IParent part = documentModelChildren.get(0);
		assertEquals(Type.CODE_BLOCK, part.getKind());
		assertEquals(0, part.getChildList().size());
		assertTrue(part instanceof PagePart);
		PagePart pagePart = (PagePart) part;
		assertEquals(UmlGen.UML, pagePart.getMeta());
	}
	
	private String readTextFromFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		byte[] bytes = Files.readAllBytes(path);
		return new String(bytes);
	}

}
