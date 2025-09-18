/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.propertytesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import com.vladsch.flexmark.ast.Image;

import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.util.EditorUtils;
import net.certiv.fluentmark.ui.util.FlexmarkUtil;

public class ActiveEditorPartTester extends PropertyTester {
	
	private static final String ACTIVE_EDITOR_PART = "activeEditorPart";
	private static final String IS_ACTIVE_FLUENT_EDITOR = "isActiveFluentEditor";
	private static final String IS_MARKDOWN_SVG_IMAGE_TEXT_SELECTION = "isMarkdownSvgImageTextSelection";
	private static final String IS_MARKDOWN_PUML_IMAGE_TEXT_SELECTION = "isMarkdownPlantUmlImageTextSelection";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		
		if (ACTIVE_EDITOR_PART.equals(property)
				&& expectedValue != null && expectedValue instanceof String
				&& !((String) expectedValue).isBlank()) {
			
			String editorPartId = (String) expectedValue;
			
			IEditorPart activeEditorPart = EditorUtils.getActiveEditorPart();
			if (activeEditorPart == null) {
				return false;
			}
			
			String editorSiteId = activeEditorPart.getEditorSite().getId();
			
			return editorPartId.equals(editorSiteId);
		} else if (IS_ACTIVE_FLUENT_EDITOR.equals(property)) {
			IEditorPart activeEditorPart = EditorUtils.getActiveEditorPart();
			return (activeEditorPart instanceof FluentEditor);
		} else if (IS_MARKDOWN_SVG_IMAGE_TEXT_SELECTION.equals(property)
				|| IS_MARKDOWN_PUML_IMAGE_TEXT_SELECTION.equals(property)) {
			ISelection selection = EditorUtils.getCurrentSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				
				if (textSelection.getStartLine() == textSelection.getEndLine()) {
					FluentEditor activeFluentEditor = EditorUtils.getActiveFluentEditor();
					
					IDocument document = activeFluentEditor.getDocument();
					
					Image imageNodesInSelection = FlexmarkUtil.findMarkdownImageForTextSelection(document, textSelection);
					if (imageNodesInSelection != null) {
						String lowerCaseUrl = imageNodesInSelection.getUrl().toString().toLowerCase();
						
						if (lowerCaseUrl.startsWith("http")) {
							return false;
						}
						
						if (IS_MARKDOWN_SVG_IMAGE_TEXT_SELECTION.equals(property)) {
							return lowerCaseUrl.endsWith(".svg");
						} else if (IS_MARKDOWN_PUML_IMAGE_TEXT_SELECTION.equals(property)) {
							return lowerCaseUrl.endsWith(".puml");
						}
					}
				}
			}
		}

		return false;
	}
	
}
