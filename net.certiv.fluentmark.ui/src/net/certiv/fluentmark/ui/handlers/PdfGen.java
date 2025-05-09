/*******************************************************************************
 * Copyright (c) 2016 - 2018 Certiv Analytics and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.handlers;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.nio.charset.Charset;

import net.certiv.fluentmark.core.convert.ConverterType;
import net.certiv.fluentmark.core.convert.IConfigurationProvider;
import net.certiv.fluentmark.core.markdown.CodeBlockConstants;
import net.certiv.fluentmark.core.markdown.ISourceRange;
import net.certiv.fluentmark.core.markdown.PagePart;
import net.certiv.fluentmark.core.markdown.PageRoot;
import net.certiv.fluentmark.core.markdown.SourceRange;
import net.certiv.fluentmark.core.markdown.Type;
import net.certiv.fluentmark.core.util.Cmd;
import net.certiv.fluentmark.core.util.Cmd.CmdResult;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.core.util.Strings;
import net.certiv.fluentmark.core.util.Temps;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.Log;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

public class PdfGen {

	private static final String[] DOT2PDF = new String[] { "", "-Tpdf", "-o", "" };

	private static final String[] PDF_OPS = new String[] { //
			"--standalone", "--variable", "graphics", "-f", "markdown", "-t", "latex" //
	};

	// TODO: support caption and label?
	// "\\caption{%s}" + Strings.EOL + //
	// "\\label{%s}" + Strings.EOL + //
	private static final String GRAPHIC = "\\begin{figure}[htp]" + Strings.EOL +  //
			"\\begin{center}" + Strings.EOL + //
			"\\graphicspath{{%s}}" + Strings.EOL + //
			"\\includegraphics[width=0.8\\textwidth]{%s}" + Strings.EOL + //
			"\\end{center}" + Strings.EOL + //
			"\\end{figure}" + Strings.EOL;
	
	private IConfigurationProvider configurationProvider;

	public PdfGen(IConfigurationProvider configProvider) {
		this.configurationProvider = configProvider;
	}

	/**
	 * Generate and Save PDF_OPS
	 *
	 * @param base the working directory
	 * @param doc the source document
	 * @param model contains the dot code blocks
	 * @param template the latex template
	 * @param pathname the output pathname
	 */
	public Job save(String base, IDocument doc, PageRoot model, String template, String pathname) {

		Job job = new Job("Pdf generating") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// generate images to 'dir' and patch document
				File dir = null;
				try {
					try {
						dir = buildGraphs(doc, model);
					} catch (IOException e) {
						return makeStatus(IStatus.ERROR, e.getMessage());
					}

					// send document to pandoc to convert & save
					File out = new File(pathname);
					String content = doc.get();
					String err = convert(base, template, content, out);

					cleanup(dir);
					if (!err.isEmpty()) {
						return makeStatus(IStatus.ERROR, "Error: " + err);
					}

					return makeStatus(IStatus.OK, "");

				} finally {
					cleanup(dir);
				}
			}
		};

		job.setPriority(Job.SHORT);
		job.schedule();
		
		return job;
	}

	protected File buildGraphs(IDocument doc, PageRoot model) throws IOException {
		File dir = getTmpFolder();

		// evaluate parts representing dot and uml code blocks
		// build latex graphics include blocks
		MultiTextEdit edit = new MultiTextEdit();
		for (PagePart part : model.getPageParts(Type.CODE_BLOCK)) {
			ISourceRange range = part.getSourceRange();

			switch (part.getMeta()) {
				case CodeBlockConstants.CODE_BLOCK_DOT:
					ISourceRange subRange = calcDotRange(range);
					if (subRange == null) continue;

					File tmpfile = createTmpFile(dir, "pdf");
					if (tmpfile == null) continue;

					String text = extractText(doc, subRange);
					if (text == null) continue;

					// convert to pdf
					boolean ok = dot2pdf(tmpfile, text);

					// splice in latex blocks to include the pdf
					if (ok) edit.addChild(splice(doc, range, tmpfile));
					break;

				case CodeBlockConstants.CODE_BLOCK_UML:
					tmpfile = createTmpFile(dir, "eps");
					if (tmpfile == null) continue;

					text = extractText(doc, range);
					if (text == null) continue;

					// convert to pdf
					ok = uml2pdf(tmpfile, text);

					// splice in latex blocks to include the pdf
					if (ok) edit.addChild(splice(doc, range, tmpfile));
					break;
			}
		}

		try {
			edit.apply(doc);
		} catch (MalformedTreeException | BadLocationException e) {
			String msg = String.format("Failed applying DOT block edits (%s)", e.getMessage());
			throw new IOException(msg);
		}
		return dir;
	}

	protected String convert(String base, String template, String content, File out) {
		List<String> ops = new ArrayList<>();

		String cmd = configurationProvider.getPandocCommand();
		if (cmd.trim().isEmpty()) return "";

		ops.add(cmd);
		ops.addAll(Arrays.asList(PDF_OPS));

		ops.add("--pdf-engine=xelatex");
		if (!template.isEmpty()) ops.add("--template=" + template);
		if (configurationProvider.addTableOfContents(ConverterType.PANDOC)) ops.add("--table-of-contents");

		ops.add("-o");
		ops.add(out.getPath());

		String[] args = ops.toArray(new String[ops.size()]);
		CmdResult result = Cmd.process(args, base, content);
		
		if (result.hasErrors()) {
			FluentUI.log(IStatus.ERROR, "Problems occured while generating PDF file:\n\n" + result.errOutput);
		}
		return result.stdOutput;
	}

	// clean up temporary files
	protected static void cleanup(File dir) {
		try {
			Temps.deleteFolder(dir);
		} catch (IOException e) {}
	}

	protected static IStatus makeStatus(int type, String msg) {
		return new Status(type, FluentUI.PLUGIN_ID, msg);
	}

	private static ISourceRange calcDotRange(ISourceRange clipRange) {
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

	private boolean dot2pdf(File file, String data) {
		String cmd = configurationProvider.getDotCommand();
		if (cmd.trim().isEmpty() || data.trim().isEmpty()) return false;

		// generate a new value by executing dot
		String[] args = DOT2PDF;
		args[0] = cmd;
		args[DOT2PDF.length - 1] = file.getPath();

		CmdResult result = Cmd.process(args, null, data);
		
		if (result.hasErrors()) {
			FluentUI.log(IStatus.ERROR, "Problems occured while translating DOT diagram to PDF file:\n\n" + result.errOutput);
			return false;
		}
		
		return true;
	}

	private static boolean uml2pdf(File file, String text) {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			SourceStringReader reader = new SourceStringReader(text);
			reader.generateImage(os, new FileFormatOption(FileFormat.EPS));
			String result = new String(os.toByteArray(), Charset.forName("UTF-8"));
			FileUtils.write(file, result);
			return true;
		} catch (IOException e) {
			Log.error("Uml exception on" + Strings.EOL + text, e);
		}
		return false;
	}

	private static TextEdit splice(IDocument doc, ISourceRange range, File tmpfile) {
		String dir = tmpfile.getParent().replace("\\", "/") + "/";
		String name = tmpfile.getName();

		String figure = GRAPHIC.replaceAll("\\R", PageRoot.MODEL.getLineDelim());
		String latex = String.format(figure, dir, name);
		return new ReplaceEdit(range.getOffset(), range.getLength(), latex);
	}

	private static String extractText(IDocument doc, ISourceRange range) {
		try {
			return doc.get(range.getOffset(), range.getLength());
		} catch (BadLocationException e) {
			Log.error(String.format("Failed extracting text at %s (%s)", range, e.getMessage()), e);
			return null;
		}
	}

	private static File getTmpFolder() throws IOException {
		try {
			return Temps.createFolder("mk_" + Temps.nextRandom());
		} catch (IOException e) {
			String msg = String.format("Failed creating tmp folder (%s)", e.getMessage());
			throw new IOException(msg, e);
		}
	}

	private static File createTmpFile(File dir, String ext) {
		try {
			return Temps.createFile("fluent_", "." + ext, dir);
		} catch (IOException e) {
			Log.error(String.format("Failed creating tmp file (%s)", e.getMessage()), e);
			return null;
		}
	}
}
