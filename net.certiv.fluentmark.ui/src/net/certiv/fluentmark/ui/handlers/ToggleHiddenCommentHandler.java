/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.handlers;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.editor.FluentEditor;

// if selection is in a comment, uncomment
// if selected is not empty, comment selected
public class ToggleHiddenCommentHandler extends AbstractHandler {

	protected static final String HIDDEN_BEG = "<!---"; //$NON-NLS-1$
	protected static final String HIDDEN_END = "--->"; //$NON-NLS-1$
	protected static final String VISIBLE_BEG = "<!--"; //$NON-NLS-1$
	protected static final String VISIBLE_END = "-->"; //$NON-NLS-1$

	private static final int SAME = 0;
	private static final int DIFF = 1;
	private static final int LAPD = 2;
	private static final int NONE = 3;
	private static final int UNKN = 4;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart edPart = HandlerUtil.getActiveEditor(event);
		if (edPart instanceof FluentEditor) {
			FluentEditor editor = (FluentEditor) edPart;
			IDocument doc = editor.getDocument();
			if (doc != null) {
				ISelection sel = HandlerUtil.getCurrentSelection(event);
				if (sel instanceof TextSelection) {
					TextSelection tsel = (TextSelection) sel;
					int beg = tsel.getOffset();
					int len = tsel.getLength();

					switch (checkPartition(doc, beg, len)) {
						case NONE:
							addComment(doc, beg, len);
							break;
						case SAME:
							removeComment(doc, beg);
							break;
					}
				}
			}
		}
		return null;
	}

	private int checkPartition(IDocument doc, int beg, int len) {
		try {
			boolean begCmt = TextUtilities.getContentType(doc, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING, beg, false)
					.equals(MarkdownPartitions.COMMENT);
			boolean endCmt = TextUtilities.getContentType(doc, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING, beg + len - 1, false)
					.equals(MarkdownPartitions.COMMENT);

			if (begCmt && endCmt) {
				ITypedRegion begPar = TextUtilities.getPartition(doc, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING, beg, false);
				ITypedRegion endPar = TextUtilities.getPartition(doc, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING, beg + len - 1, false);
				if (begPar.getOffset() == endPar.getOffset()) return SAME;
				return DIFF;
			}
			if ((begCmt && !endCmt) || (!begCmt && endCmt)) return LAPD;
			return NONE;
		} catch (BadLocationException e) {
			Log.error("Bad comment partitioning " + e.getMessage(), e);
			return UNKN;
		}
	}

	private void addComment(IDocument doc, int beg, int len) {
		IDocumentUndoManager undoMgr = DocumentUndoManagerRegistry.getDocumentUndoManager(doc);
		undoMgr.beginCompoundChange();

		MultiTextEdit edit = new MultiTextEdit();
		edit.addChild(new InsertEdit(beg, getCommentBeg()));
		edit.addChild(new InsertEdit(beg + len, getCommentEnd()));
		try {
			edit.apply(doc);
			undoMgr.endCompoundChange();
		} catch (MalformedTreeException | BadLocationException e) {
			Log.error("Failure creating comment " + e.getMessage(), e);
		}
	}

	private void removeComment(IDocument doc, int offset) {
		try {
			IDocumentUndoManager undoMgr = DocumentUndoManagerRegistry.getDocumentUndoManager(doc);
			undoMgr.beginCompoundChange();

			ITypedRegion par = TextUtilities.getPartition(doc, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING, offset, false);
			int beg = par.getOffset();
			int len = par.getLength();

			String comment = doc.get(beg, len);
			int eLen = markerLen(comment);
			int bLen = eLen + 1;

			MultiTextEdit edit = new MultiTextEdit();
			edit.addChild(new DeleteEdit(beg, bLen));
			edit.addChild(new DeleteEdit(beg + len - eLen, eLen));
			edit.apply(doc);
			undoMgr.endCompoundChange();
		} catch (MalformedTreeException | BadLocationException e) {
			Log.error("Failure removing comment " + e.getMessage(), e);
		}
	}

	private int markerLen(String comment) {
		if (comment.startsWith(HIDDEN_BEG) && comment.endsWith(HIDDEN_END)) return HIDDEN_END.length();
		return VISIBLE_END.length();
	}

	/** Returns the text to be inserted at the selection start. */
	protected String getCommentBeg() {
		return HIDDEN_BEG;
	}

	/** Returns the text to be inserted at the selection end. */
	protected String getCommentEnd() {
		return HIDDEN_END;
	}
}
