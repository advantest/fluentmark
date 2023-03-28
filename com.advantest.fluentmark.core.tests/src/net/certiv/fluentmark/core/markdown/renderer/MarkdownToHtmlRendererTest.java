/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.markdown.renderer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.certiv.fluentmark.core.TestFileUtil;
import net.certiv.fluentmark.core.render.html.MarkdownToHtmlRenderer;

public class MarkdownToHtmlRendererTest {
	
	private static String markdownSourceCode;
	private MarkdownToHtmlRenderer renderer;
	
	@BeforeAll
	public static void setUpBeforeAll() throws Exception {
		markdownSourceCode = TestFileUtil.readTextFromFile("resources/parsing/feature-overview.md");
	}
	
	@BeforeEach
	public void setUp() {
		renderer = new MarkdownToHtmlRenderer();
	}
	
	@AfterEach
	public void tearDown() {
		renderer = null;
	}
	
	@Test
	public void rendering_common_elements() {
		String htmlSourceCode = renderer.renderHtml(markdownSourceCode);
		
		Assertions.assertNotNull(htmlSourceCode);
		Assertions.assertFalse(htmlSourceCode.isBlank());
	}

}
