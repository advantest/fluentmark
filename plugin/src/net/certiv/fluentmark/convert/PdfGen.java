/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.convert;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import net.certiv.fluentmark.FluentMkUI;
import net.certiv.fluentmark.Log;
import net.certiv.fluentmark.model.ISourceRange;
import net.certiv.fluentmark.model.PagePart;
import net.certiv.fluentmark.model.PageRoot;
import net.certiv.fluentmark.model.SourceRange;
import net.certiv.fluentmark.preferences.Prefs;
import net.certiv.fluentmark.util.Cmd;
import net.certiv.fluentmark.util.Strings;
import net.certiv.fluentmark.util.Temps;

public class PdfGen {

	private static final String[] DOT2PDF = new String[] { "", "-Tpdf", "-o", "" };
	private static final String[] PDF = new String[] { "", "--standalone", "--variable", "graphics",
			"--number-sections", "-f", "markdown", "-t", "latex", "-o", "" };

	// TODO: support caption and label?
	private static final String FIGURE = "\\begin{figure}[htp]" + Strings.EOL //
			+ "\\begin{center}" + Strings.EOL //
			+ "\\graphicspath{{%s}}" + Strings.EOL //
			+ "\\includegraphics[width=0.8\\textwidth]{%s}" + Strings.EOL //
			// + "\\caption{%s}" + Strings.EOL //
			// + "\\label{%s}" + Strings.EOL //
			+ "\\end{center}" + Strings.EOL //
			+ "\\end{figure}" + Strings.EOL;

	private PdfGen() {}

	public static void save(String pathname, List<PagePart> parts, IDocument doc) {

		Job job = new Job("PDF generation job") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				File dir = null;
				if (!parts.isEmpty()) {
					try {
						dir = Temps.createFolder("mk_" + Temps.nextRandom());
					} catch (IOException e) {
						String msg = "Failed creating tmp folder " + " (" + e.getMessage() + ")";
						return new Status(IStatus.ERROR, FluentMkUI.PLUGIN_ID, msg);
					}

					MultiTextEdit edit = new MultiTextEdit();
					for (PagePart part : parts) {
						if (part.getMeta().equals(DotGen.DOT)) {
							ISourceRange clipRange = part.getSourceRange();
							ISourceRange dotRange = calcDotRange(clipRange);
							if (dotRange == null) continue;

							// extract the dot block
							String dot;
							try {
								dot = doc.get(dotRange.getOffset(), dotRange.getLength());
							} catch (BadLocationException e) {
								Log.error("Failed extracting DOT block for conversion at " + dotRange + " ("
										+ e.getMessage() + ")");
								continue;
							}

							File tmpfile;
							try {
								tmpfile = Temps.createFile("fluentMk_", ".pdf", dir);
							} catch (IOException e) {
								Log.error("Failed creating tmp file " + " (" + e.getMessage() + ")");
								continue;
							}

							// convert to pdf
							boolean ok = dot2pdf(tmpfile, dot);

							// splice in latex blocks to include the pdfs
							if (ok) edit.addChild(splice(doc, clipRange, tmpfile));
						}
					}

					try {
						edit.apply(doc);
					} catch (MalformedTreeException | BadLocationException e) {
						String msg = "Failed applying DOT block edits " + " (" + e.getMessage() + ")";
						cleanup(dir);
						return new Status(IStatus.ERROR, FluentMkUI.PLUGIN_ID, msg);
					}
				}

				// send to pandoc to convert & save
				File outFile = new File(pathname);
				String out = doc.get();
				String err = convert(out, outFile);
				cleanup(dir);
				if (!err.isEmpty()) {
					return new Status(IStatus.ERROR, FluentMkUI.PLUGIN_ID, "Pdf generation failed: " + err);
				}

				if (FluentMkUI.getDefault().getPreferenceStore().getBoolean(Prefs.EDITOR_PDF_OPEN)) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().open(outFile);
						} catch (Exception e) {
							// no application registered for PDFs
							String msg = "Cannot open " + pathname + " (" + e.getMessage() + ")";
							return new Status(IStatus.ERROR, FluentMkUI.PLUGIN_ID, msg);
						}
					}
				}

				return new Status(IStatus.OK, FluentMkUI.PLUGIN_ID, "");
			}
		};
		job.setPriority(Job.LONG);
		job.schedule();
	}

	public static String convert(String data, File file) {
		String cmd = FluentMkUI.getDefault().getPreferenceStore().getString(Prefs.EDITOR_PANDOC_PROGRAM);
		if (data.trim().isEmpty() || cmd.trim().isEmpty()) return "";

		// generate by executing pandoc to generate pdf from markdown
		String[] args = PDF;
		args[0] = cmd;
		args[PDF.length - 1] = file.getPath();

		return Cmd.process(args, data);
	}

	// clean up temporary files
	protected static void cleanup(File dir) {
		try {
			Temps.deleteFolder(dir);
		} catch (IOException e) {}
	}

	protected static ISourceRange calcDotRange(ISourceRange clipRange) {
		int dotBegLine = clipRange.getBeginLine() + 1;
		int dotEndLine = clipRange.getEndLine() - 1;
		PageRoot model = PageRoot.MODEL;
		while (dotEndLine > dotBegLine) {
			String text = model.getText(dotEndLine);
			if (!text.trim().isEmpty() && !text.startsWith("~~~") && !text.startsWith("```")) {
				break;
			}
			dotEndLine--;
		}
		if (dotBegLine >= dotEndLine) return null;

		int begOffset = model.getOffset(dotBegLine);
		int endOffset = model.getOffset(dotEndLine);
		endOffset += model.getTextLength(dotEndLine) + model.getLineDelim().length();
		return new SourceRange(begOffset, endOffset - begOffset, dotBegLine, dotEndLine);
	}

	protected static boolean dot2pdf(File tmpfile, String content) {
		String cmd = FluentMkUI.getDefault().getPreferenceStore().getString(Prefs.EDITOR_DOT_PROGRAM);
		if (cmd.trim().isEmpty() || content.trim().isEmpty()) return false;

		// generate a new value by executing dot
		String[] args = DOT2PDF;
		args[0] = cmd;
		args[DOT2PDF.length - 1] = tmpfile.getPath();

		Cmd.process(args, content);
		return true;
	}

	protected static TextEdit splice(IDocument doc, ISourceRange range, File tmpfile) {
		String dir = tmpfile.getParent().replace("\\", "/") + "/";
		String name = tmpfile.getName();

		String figure = FIGURE.replaceAll("\\R", PageRoot.MODEL.getLineDelim());
		String latex = String.format(figure, dir, name);
		return new ReplaceEdit(range.getOffset(), range.getLength(), latex);
	}
}
