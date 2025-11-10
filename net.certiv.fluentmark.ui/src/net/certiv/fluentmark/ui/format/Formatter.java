/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.format;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;

import java.util.ArrayList;
import java.util.List;

import net.certiv.fluentmark.core.markdown.model.ISourceRange;
import net.certiv.fluentmark.core.markdown.model.PagePart;
import net.certiv.fluentmark.core.markdown.model.PageRoot;
import net.certiv.fluentmark.core.util.Strings;
import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.dialog.tables.TableModel;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.preferences.Prefs;

public class Formatter {

	private static int cols;
	private static int tabWidth;
	private static int docLength;

	public static void format(FluentEditor editor, ITextSelection sel) {
		IPreferenceStore store = editor.getPrefsStore();
		cols = store.getInt(Prefs.EDITOR_FORMATTING_COLUMN);
		tabWidth = store.getInt(Prefs.EDITOR_TAB_WIDTH);

		doFormat(editor, sel);
	}

	public static void unwrap(FluentEditor editor, ITextSelection sel) {
		IPreferenceStore store = editor.getPrefsStore();
		cols = 0;
		tabWidth = store.getInt(Prefs.EDITOR_TAB_WIDTH);

		doFormat(editor, sel);
	}

	private static void doFormat(FluentEditor editor, ITextSelection sel) {
		IDocument doc = editor.ensureLastLineBlank().getDocument();
		if (doc == null || doc.getLength() == 0) return;

		docLength = doc.getLength();

		try {
			PageRoot model = editor.getPageModel();
			List<PagePart> parts = model.getPageParts();
			if (sel != null && sel.getLength() > 0) {
				parts = selectedParts(model, sel);
			}

			IDocumentUndoManager undoMgr = DocumentUndoManagerRegistry.getDocumentUndoManager(doc);
			undoMgr.beginCompoundChange();
			TextEdit edit = new MultiTextEdit();
			for (PagePart part : parts) {
				formatPagePart(part, edit);
			}
			edit.apply(editor.getDocument());
			undoMgr.endCompoundChange();
		} catch (Exception ex) {
			Log.error("Bad location error occurred during formatting", ex);
		}
	}

	private static List<PagePart> selectedParts(PageRoot model, ITextSelection sel) {
		List<PagePart> selected = new ArrayList<>();
		int begOffset = sel.getOffset();
		int endOffset = sel.getOffset() + sel.getLength();
		PagePart beg = model.partAtOffset(begOffset);
		PagePart end = model.partAtOffset(endOffset);

		for (int idx = beg.getPartIdx(); idx <= end.getPartIdx(); idx++) {
			selected.add(model.getPagePart(idx));
		}
		return selected;
	}

	private static void formatPagePart(PagePart part, TextEdit edit) {
		switch (part.getKind()) {
			case BLANK:
				formatBlank(part, edit);
				break;
			case TEXT:
				formatText(part, edit, cols);
				break;
			case LIST:
				formatList(part, edit, cols, tabWidth);
				break;
			case TABLE:
				formatTable(part, edit);
				break;
			default:
				break;
		}
	}

	private static boolean textsDiffer(String originalText, String formattedText) {
		if (originalText != null && formattedText != null) {
			return originalText.hashCode() != formattedText.hashCode();
		} else if (originalText != null && formattedText == null) {
			return true;
		} else if (originalText == null && formattedText != null) {
			return true;
		}
		
		return false;
	}
	
	private static void formatBlank(PagePart part, TextEdit edit) {
		ISourceRange range = part.getSourceRange();
		int eols = range.getEndLine() - range.getBeginLine() + 1;
		String rep = Strings.dup(part.getLineDelim(), eols);
		if (range.getOffset() + range.getLength() <= docLength
				&& textsDiffer(part.getContent(), rep)) {
			edit.addChild(new ReplaceEdit(range.getOffset(), range.getLength(), rep));
		}
	}

	private static void formatTable(PagePart part, TextEdit edit) {
		TableModel table = new TableModel();
		table.load(part);
		ISourceRange range = part.getSourceRange();
		
		String formattedTable = table.build();
		String oldTable = part.getContent();
		
		if (textsDiffer(oldTable, formattedTable)) {
			edit.addChild(new ReplaceEdit(range.getOffset(), range.getLength(), formattedTable));
		}
	}

	private static void formatText(PagePart part, TextEdit edit, int cols) {
		ISourceRange range = part.getSourceRange();
		int offset = range.getOffset();
		int len = range.getLength() - part.getLineDelim().length();
		if (len <= 0) return;

		String oldContent = part.getContent(true);
		String newContent = TextFormatter.wrap(oldContent, cols, part.getLineDelim());
		
		if (textsDiffer(oldContent, newContent)) {
			edit.addChild(new ReplaceEdit(offset, len, newContent));
		}
	}

	private static void formatList(PagePart part, TextEdit edit, int cols, int tabWidth) {
		for (int mark : part.getListMarkedLines()) {
			formatListItem(part, mark, edit, cols, tabWidth);
		}
	}

	private static void formatListItem(PagePart part, int mark, TextEdit edit, int cols, int tabWidth) {
		ISourceRange range = part.getSublistRange(mark);
		int offset = range.getOffset();
		int len = range.getLength() - part.getLineDelim().length();
		if (len <= 0) return;

		int itemOffsetInContent = offset - part.getSourceRange().getOffset();
		String oldListItem = part.getContent().substring(itemOffsetInContent, itemOffsetInContent + len);
		String listItem = part.getSublistContent(mark);
		int indent = net.certiv.fluentmark.core.util.Indent.measureIndentInTabs(listItem, tabWidth);
		int markWidth = listMarkWidth(listItem);
		String formattedListItem = TextFormatter.wrap(listItem, cols, part.getLineDelim(), tabWidth * indent,
				(tabWidth * indent) + markWidth);

		if (textsDiffer(oldListItem, formattedListItem)) {
			edit.addChild(new ReplaceEdit(offset, len, formattedListItem));
		}
	}

	private static int listMarkWidth(String item) {
		String line = item.trim();
		char c = line.charAt(0);
		switch (c) {
			case '-':
			case '+':
			case '*':
				return 2;

			default:
				int dot = line.indexOf('.');
				if (dot > 0) return dot + 2;
		}
		return 0;
	}
}
