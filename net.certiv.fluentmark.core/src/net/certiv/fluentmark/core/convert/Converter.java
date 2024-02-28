/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.convert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.markdownj.MarkdownProcessor;
import org.pegdown.PegDownProcessor;

import com.github.rjeschke.txtmark.BlockEmitter;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Configuration.Builder;
import com.github.rjeschke.txtmark.Processor;
import com.google.common.html.HtmlEscapers;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.core.util.Cmd;
import net.certiv.fluentmark.core.util.Cmd.CmdResult;
import net.certiv.fluentmark.core.util.FileUtils;

public class Converter {

	private static final Pattern DOTBEG = Pattern.compile("(~~~+|```+)\\s*dot\\s*", Pattern.DOTALL);
	private static final Pattern DOTEND = Pattern.compile("(~~~+|```+)\\s*", Pattern.DOTALL);
	
	private static final String PANDOC_EXTENSIONS_FOR_MARKDOWN = "+raw_html+header_attributes+auto_identifiers+implicit_figures+implicit_header_references+strikeout+footnotes+backtick_code_blocks+fenced_code_blocks+fenced_code_attributes+startnum+simple_tables+multiline_tables+grid_tables+pipe_tables+table_captions+task_lists+subscript+superscript+tex_math_dollars";
	
	private final IConfigurationProvider configurationProvider;
	private final DotGen dotGen;
	private final UmlGen umlGen;
	private final BlockEmitter emitter;
	private final PumlIncludeStatementConverter pumlInclusionConverter;

	public Converter(IConfigurationProvider configProvider) {
		this.configurationProvider = configProvider;
		this.dotGen = new DotGen(configProvider);
		this.umlGen = new UmlGen(configProvider);
		this.emitter = new DotCodeBlockEmitter(dotGen);
		this.pumlInclusionConverter = new PumlIncludeStatementConverter();
	}

	public String convert(IPath filePath, String basepath, IDocument document, Kind kind) {
		ITypedRegion[] typedRegions = MarkdownPartitions.computePartitions(document);
		
		try {
			String text;
			switch (configurationProvider.getConverterType()) {
				case PANDOC:
					text = getText(filePath, document, typedRegions, true);
					return usePandoc(basepath, text, kind);
				case BLACKFRIDAY:
					text = getText(filePath, document, typedRegions, false);
					return useBlackFriday(basepath, text);
				case MARKDOWNJ:
					text = getText(filePath, document, typedRegions, false);
					return useMarkDownJ(basepath, text);
				case PEGDOWN:
					text = getText(filePath, document, typedRegions, false);
					return usePegDown(basepath, text);
				case COMMONMARK:
					text = getText(filePath, document, typedRegions, false);
					return useCommonMark(basepath, text);
				case TXTMARK:
					text = getText(filePath, document, typedRegions, false);
					return useTxtMark(basepath, text);
				case OTHER:
					text = getText(filePath, document, typedRegions, false);
					return useExternal(basepath, text);
			}
		} catch (Exception e) {
			String message =  "<span style=\"color:red;\">Could not convert Markdown to HTML. %s</span>";
			message = String.format(message, HtmlEscapers.htmlEscaper().escape(e.getMessage()));
			return message;
		}
		return "";
	}
	
	private String combineOutputsForHtml(CmdResult result) {
		if (result.errOutput == null || result.errOutput.isEmpty()) {
			return result.stdOutput;
		}
		
		int indexOfBodyTag = result.stdOutput.indexOf("<body>");
		if (indexOfBodyTag < 0) {
			indexOfBodyTag = result.stdOutput.indexOf("<BODY>");
		}
		
		if (indexOfBodyTag < 0) {
			return StringEscapeUtils.escapeHtml4(result.errOutput) + result.stdOutput; 
		}
		
		// 6 is the length of "<body>"
		int insertIndex = indexOfBodyTag + 6;
		
		StringBuilder stringBuilder = new StringBuilder();
		// append everything from stdOut including <body>
		stringBuilder.append(result.stdOutput.substring(0, insertIndex));
		
		// append the error messages and warnings, escaped for HTML, and on a separate paragraph
		stringBuilder.append("<p>");
		stringBuilder.append(StringEscapeUtils.escapeHtml4(result.errOutput));
		stringBuilder.append("</p>");
		
		// append the remainder of the stdOut
		stringBuilder.append(result.stdOutput.substring(insertIndex));
		
		return stringBuilder.toString();
	}

	// Use Pandoc
	private String usePandoc(String basepath, String text, Kind kind) {
		String cmd = configurationProvider.getPandocCommand();
		if (cmd.trim().isEmpty()) {
			throw new IllegalStateException("No pandoc command set in preferences.");
		}

		List<String> args = new ArrayList<>();
		args.add(cmd);
		args.add("--no-highlight"); // use highlightjs instead
		if (Kind.EXPORT.equals(kind)) {
			args.add("-s");
		}
		if (configurationProvider.addTableOfContents()) args.add("--toc");
		if (configurationProvider.useMathJax()) args.add("--mathjax");
		if (configurationProvider.isSmartMode()) {
			args.add("-f");
			args.add("markdown-smart" + PANDOC_EXTENSIONS_FOR_MARKDOWN);
		} else {
			args.add("--ascii");
			args.add("-f");
			args.add("markdown_strict" + PANDOC_EXTENSIONS_FOR_MARKDOWN);
		}
		
		CmdResult result = Cmd.process(args.toArray(new String[args.size()]), basepath, text);
		return combineOutputsForHtml(result);
	}

	// Use BlackFriday
	private String useBlackFriday(String basepath, String text) {
		String cmd = configurationProvider.getBlackFridayCommand();
		if (cmd.trim().isEmpty()) {
			throw new IllegalStateException("No BlackFriday command set in preferences.");
		}

		List<String> args = new ArrayList<>();
		args.add(cmd);
		if (configurationProvider.addTableOfContents()) {
			args.add("-toc");
		}
		if (configurationProvider.isSmartMode()) {
			args.add("-smartypants");
			args.add("-fractions");
		}
		
		CmdResult result = Cmd.process(args.toArray(new String[args.size()]), basepath, text);
		return combineOutputsForHtml(result);
	}

	// Use MarkdownJ
	private String useMarkDownJ(String basepath, String text) {
		MarkdownProcessor markdown = new MarkdownProcessor();
		return markdown.markdown(text);
	}

	// Use PegDown
	private String usePegDown(String basepath, String text) {
		PegDownProcessor pegdown = new PegDownProcessor();
		return pegdown.markdownToHtml(text);
	}

	// Use CommonMark
	private String useCommonMark(String basepath, String text) {
		Parser parser = Parser.builder().build();
		Node document = parser.parse(text);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		return renderer.render(document);
	}

	// Use TxtMark
	private String useTxtMark(String basepath, String text) {
		boolean safeMode = configurationProvider.isTxtMarkSafeMode();
		boolean extended = configurationProvider.isTxtMarkExtendedMode();
		boolean dotMode = configurationProvider.isDotEnabled();

		Builder builder = Configuration.builder();
		if (safeMode) builder.enableSafeMode();
		if (extended || dotMode) builder.forceExtentedProfile();
		if (dotMode) builder.setCodeBlockEmitter(emitter);
		Configuration config = builder.build();
		return Processor.process(text, config);
	}

	// Use external command
	private String useExternal(String basepath, String text) {
		String cmd = configurationProvider.getExternalCommand();
		if (cmd.trim().isEmpty()) {
			return "Specify an external markdown converter command in preferences.";
		}

		String[] args = Cmd.parse(cmd);
		if (args.length > 0) {
			CmdResult result = Cmd.process(args, basepath, text);
			return combineOutputsForHtml(result); 
		}
		return "";
	}
	
	private String getText(IPath filePath, IDocument document, ITypedRegion[] typedRegions, boolean includeFrontMatter) {
		if (typedRegions == null || typedRegions.length == 0) {
			return document.get();
		}
		
		List<String> parts = new ArrayList<>();
		
		String text, regionType;
		for (ITypedRegion typedRegion: typedRegions) {
			
			try {
				text = document.get(typedRegion.getOffset(), typedRegion.getLength());
			} catch (BadLocationException e) {
				return document.get();
			}
			
			regionType = typedRegion.getType();
			
			switch (regionType) {
				case MarkdownPartitions.FRONT_MATTER:
					if (!includeFrontMatter) continue;
					break;
				case MarkdownPartitions.DOTBLOCK:
					if (configurationProvider.isDotEnabled()) {
						text = filter(text, DOTBEG, DOTEND);
						text = translateDotCodeToHtmlFigure(text);
					}
					break;
				case MarkdownPartitions.UMLBLOCK:
					if (configurationProvider.isPlantUMLEnabled()) {
						text = translatePumlCodeToHtmlFigure(text);
					}
					break;
				case MarkdownPartitions.PLANTUML_INCLUDE:
					if (configurationProvider.isPlantUMLEnabled()) {
						text = translatePumlIncludeLineToHtml(text, filePath);
					}
					break;
				case MarkdownPartitions.COMMENT:
					if (!text.isEmpty()
							&& text.startsWith("<!---")
							&& text.endsWith("--->")) {
						// hide "hidden comment" (remove / ignore it)
						continue;
					}
					break;
				default:
					break;
			}
			parts.add(text);
		}

		return String.join("", parts);
	}

	private String translateDotCodeToHtmlFigure(String dotSourcCode) {
		String dotDiagram = convertDot2Svg(dotSourcCode);
		return createHtmlFigure(dotDiagram);
	}
	
	private String translatePumlCodeToHtmlFigure(String pumlSourcCode) {
		String pumlDiagram = convertPlantUml2Svg(pumlSourcCode);
		return createHtmlFigure(pumlDiagram);
	}
	
	private String translatePumlIncludeLineToHtml(String markdownCodeWithPumlIncludeStatement, IPath currentMarkdownFilePath) {
		String figureCaption = pumlInclusionConverter.readCaptionFrom(markdownCodeWithPumlIncludeStatement);
		IPath relativePumlFilePath = pumlInclusionConverter.readPumlFilePath(markdownCodeWithPumlIncludeStatement);
        String remainingLine = pumlInclusionConverter.getRemainderOfThePumlIncludeLine(markdownCodeWithPumlIncludeStatement);
        
        IPath absolutePumlFilePath = pumlInclusionConverter.toAbsolutePumlFilePath(currentMarkdownFilePath, relativePumlFilePath);
        File pumlFile = absolutePumlFilePath.toFile();
        
        Path tempDirPath;
		try {
			tempDirPath = Files.createTempDirectory("puml2svg-");
		} catch (IOException e) {
			throw new RuntimeException("Could not create temporary directory for generating SVG file.", e);
		}
        File svgFile = umlGen.uml2svg(pumlFile, tempDirPath.toFile());
        String svgDiagram = "";
        
        if (svgFile != null && svgFile.exists()) {
        	String svgFileContent = FileUtils.readTextFromFile(svgFile);
        	svgDiagram = removeSvgMetaInfos(svgFileContent);
        	
        	boolean fileDeleted = svgFile.delete();
        	if (fileDeleted) {
        		tempDirPath.toFile().delete();
        	}
        }
        
        String htmlFigure = createHtmlFigure(svgDiagram, figureCaption);
        
        if (htmlFigure != null) {
        	return htmlFigure + remainingLine;
        }
        
        return markdownCodeWithPumlIncludeStatement;
	}
	
	private String convertDot2Svg(String dotCode) {
		String svgDiagram = "";
        if (dotCode != null) {
        	svgDiagram = dotGen.runDot(dotCode);
        }
        return svgDiagram;
	}
	
	private String convertPlantUml2Svg(String plantUmlCode) {
		String svgDiagram = "";
        if (plantUmlCode != null) {
        	svgDiagram = umlGen.uml2svg(plantUmlCode);
        	svgDiagram = removeSvgMetaInfos(svgDiagram);
        }
        return svgDiagram;
	}
	
	private String removeSvgMetaInfos(String svgSources) {
		String svgDiagram = svgSources;
		
		if (StringUtils.isBlank(svgSources)) {
			return svgDiagram;
		}
		
		// remove meta-infos since we're only interested in the SVG tag contents
    	// meta-infos example: <?xml version="1.0" encoding="us-ascii" standalone="no"?>
    	if (svgSources.startsWith("<?xml ")) {
    		int indexOfEndTag = svgSources.indexOf("?>");
    		if (indexOfEndTag >= 0) {
    			svgDiagram = svgDiagram.substring(indexOfEndTag + 2);
    		}
    	}
    	
    	return svgDiagram;
	}
	
	private String createHtmlFigure(String svgCode, String figureCaption) {
		String figureText;
		try {
			figureText = FileUtils.fromBundle("resources/html/puml-include.html", FluentCore.PLUGIN_ID);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
        
        if (figureText != null) {
        	figureText = figureText.replace("%image%", svgCode);
        	figureText = figureText.replace("%caption%", figureCaption);
        	
        	return figureText;
        }
        
        return null;
	}
	
	private String createHtmlFigure(String svgCode) {
		String figureText;
		try {
			figureText = FileUtils.fromBundle("resources/html/figure.html", FluentCore.PLUGIN_ID);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
        
        if (figureText != null) {
        	figureText = figureText.replace("%image%", svgCode);
        	
        	return figureText;
        }
        
        return null;
	}

	private String filter(String text, Pattern beg, Pattern end) {
		String result = text;
		if (beg != null) {
			result = beg.matcher(result).replaceFirst("");
		}
		if (end != null) {
			Matcher m = end.matcher(result);
			int idx = -1;
			while (m.find()) {
				idx = m.start();
			}
			if (idx > -1) {
				result = result.substring(0, idx);
			}
		}
		return result;
	}
}
