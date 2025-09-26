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

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import com.vladsch.flexmark.ast.Image;

import net.certiv.fluentmark.core.plantuml.parsing.PlantUmlConstants;
import net.certiv.fluentmark.core.plantuml.parsing.PlantUmlParsingTools;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.util.EditorUtils;
import net.certiv.fluentmark.ui.util.FlexmarkUiUtil;

public class ActiveEditorPartTester extends PropertyTester {
	
	private static final String ACTIVE_EDITOR_PART = "activeEditorPart";
	private static final String IS_ACTIVE_FLUENT_EDITOR = "isActiveFluentEditor";
	private static final String IS_MARKDOWN_SVG_IMAGE_TEXT_SELECTION = "isMarkdownSvgImageTextSelection";
	private static final String IS_MARKDOWN_PUML_IMAGE_TEXT_SELECTION = "isMarkdownPlantUmlImageTextSelection";
	private static final String IS_MARKDOWN_PUML_CODE_BLOCK_TEXT_SELECTION = "isMarkdownPlantUmlCodeBlockTextSelection";

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
				|| IS_MARKDOWN_PUML_IMAGE_TEXT_SELECTION.equals(property)
				|| IS_MARKDOWN_PUML_CODE_BLOCK_TEXT_SELECTION.equals(property)) {
			ISelection selection = EditorUtils.getCurrentSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				
				if (textSelection.getStartLine() == textSelection.getEndLine()) {
					IDocument document = getActiveEditorsDocument();
					
					Image imageNodesInSelection = FlexmarkUiUtil.findMarkdownImageForTextSelection(document, textSelection);
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
				} else if (IS_MARKDOWN_PUML_CODE_BLOCK_TEXT_SELECTION.equals(property)
						&& textSelection.getEndLine() - textSelection.getStartLine() > 1) {
					
					IDocument document = getActiveEditorsDocument();
					String selectedLines = FlexmarkUiUtil.getLinesForTextSelection(document, textSelection);
					int numDiagrams = PlantUmlParsingTools.getNumberOfDiagrams(selectedLines);
					
					if (numDiagrams != 1) {
						return false;
					}
					
					return exactlyOnePlantUmlCodeBlockSelected(selectedLines);
					// TODO also ensure, the selection is not in a code block?
				}
			}
		}

		return false;
	}
	
	private boolean exactlyOnePlantUmlCodeBlockSelected(String selectedLines) {
		List<String> selectionLines = selectedLines.lines().toList();
		
		if (selectionLines.size() < 2) {
			return false;
		}
		
		String firstLine = selectionLines.getFirst();
		String lastLine = selectionLines.getLast();
		
		if (firstLine.startsWith(PlantUmlConstants.PREFIX_START)) {
			return lastLine.startsWith(PlantUmlConstants.PREFIX_END);
		}
		
		if ((firstLine.startsWith("```") && lastLine.startsWith("```"))
				|| (firstLine.startsWith("~~~") && lastLine.startsWith("~~~")) ) {
			if (selectionLines.size() < 4) {
				return false;
			}
			
			String secondLine = selectionLines.get(1);
			String prevToLastLine = selectionLines.get(selectionLines.size() - 2);
			
			return (secondLine.startsWith(PlantUmlConstants.PREFIX_START)
					&& prevToLastLine.startsWith(PlantUmlConstants.PREFIX_END));
		}
		return false;
	}
	
	private IDocument getActiveEditorsDocument() {
		FluentEditor activeFluentEditor = EditorUtils.getActiveFluentEditor();
		if (activeFluentEditor != null) {
			return activeFluentEditor.getDocument();
		}
		return null;
	}
}
