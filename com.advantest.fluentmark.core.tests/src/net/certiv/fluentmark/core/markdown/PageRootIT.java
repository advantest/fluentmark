/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2022 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import net.certiv.fluentmark.core.TestFileUtil;


public class PageRootIT {
	
	private static final int TAB_WIDTH = 4;
	
	private PageRoot pageModel = null;
	private OffsetProviderMock offsetProvider = null;
	private IFile markdownFile = null;
	
	@BeforeEach
	public void setUp() {
		offsetProvider = new OffsetProviderMock();
		pageModel = new PageRoot(offsetProvider, "\n", TAB_WIDTH);
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project  = root.getProject("Test-FluentMark-Project");
		IFolder folder = project.getFolder("src");
		markdownFile = folder.getFile("test.md");
	}
	
	@AfterEach
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
	public void testParsing_Bug_HMR_43() throws Exception {
		// given
		String text = TestFileUtil.readTextFromFile("resources/md/bug-hmr-43.md");
		
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
		assertEquals(CodeBlockConstants.CODE_BLOCK_UML, pagePart.getMeta());
	}
	
	@Test
	public void testParsing_Comments() throws Exception {
		// given
		String text = TestFileUtil.readTextFromFile("resources/md/comments.md");
		
		// when
		pageModel.updateModel(markdownFile, text);
		
		// then
		List<IParent> documentModelChildren = pageModel.getChildList();
		assertEquals(1, documentModelChildren.size());
		
		IParent part = documentModelChildren.get(0);
		assertEquals(Type.HEADER, part.getKind());
		assertEquals("# Header Level 1\n", part.getContent());
		
		documentModelChildren = part.getChildList();
		assertEquals(4, documentModelChildren.size());
		
		part = documentModelChildren.get(0);
		assertEquals(Type.BLANK, part.getKind());
		
		part = documentModelChildren.get(1);
		assertEquals(Type.TEXT, part.getKind());
		assertEquals("Some text\n", part.getContent());
		
		part = documentModelChildren.get(2);
		assertEquals(Type.BLANK, part.getKind());
		
		part = documentModelChildren.get(3);
		assertEquals(Type.HEADER, part.getKind());
		assertEquals("## Header Level 2\n", part.getContent());
		
		documentModelChildren = part.getChildList();
		//assertEquals(7, documentModelChildren.size());
		
		part = documentModelChildren.get(0);
		assertEquals(Type.TEXT, part.getKind());
		assertEquals("More text\n", part.getContent());
		
		part = documentModelChildren.get(1);
		assertEquals(Type.BLANK, part.getKind());
		
		part = documentModelChildren.get(2);
		assertEquals(Type.HTML_BLOCK, part.getKind());
		assertEquals("<!-- HTML-style comment -->\n", part.getContent());
		
		part = documentModelChildren.get(3);
		assertEquals(Type.BLANK, part.getKind());
		
		part = documentModelChildren.get(4);
		assertEquals(Type.TEXT, part.getKind());
		assertEquals("Even more text\n", part.getContent());
		
		part = documentModelChildren.get(5);
		assertEquals(Type.BLANK, part.getKind());
		
		part = documentModelChildren.get(6);
		assertEquals(Type.COMMENT, part.getKind());
		assertEquals("<!--- hidden comment --->\n", part.getContent());
		
		part = documentModelChildren.get(7);
		assertEquals(Type.BLANK, part.getKind());
		
		part = documentModelChildren.get(8);
		assertEquals(Type.TEXT, part.getKind());
		assertEquals("Text again\n", part.getContent());
		
		part = documentModelChildren.get(9);
		assertEquals(Type.BLANK, part.getKind());
		
		part = documentModelChildren.get(10);
		assertEquals(Type.COMMENT, part.getKind());
		assertEquals("<!--- hidden --->\n", part.getContent());
		
		part = documentModelChildren.get(11);
		assertEquals(Type.BLANK, part.getKind());
	}
	
	// TODO Add tests for parsing HTML code and comments
	// see https://spec.commonmark.org/0.30/#html-blocks
	// and https://spec.commonmark.org/0.30/#raw-html
	
}
