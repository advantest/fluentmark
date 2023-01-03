/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.convert;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.markdownj.MarkdownProcessor;
import org.pegdown.PegDownProcessor;

import com.github.rjeschke.txtmark.BlockEmitter;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Configuration.Builder;
import com.github.rjeschke.txtmark.Processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.List;

import java.net.URISyntaxException;

import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.certiv.fluentmark.FluentUI;
import net.certiv.fluentmark.Log;
import net.certiv.fluentmark.core.markdown.Lines;
import net.certiv.fluentmark.core.util.Cmd;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.editor.Partitions;

public class Converter {

	private static final Pattern DOTBEG = Pattern.compile("(~~~+|```+)\\s*dot\\s*", Pattern.DOTALL);
	private static final Pattern DOTEND = Pattern.compile("(~~~+|```+)\\s*", Pattern.DOTALL);
	
	private final IConfigurationProvider configurationProvider;
	private final DotGen dotGen;
	private final UmlGen umlGen;
	private final BlockEmitter emitter;

	public Converter(IConfigurationProvider configProvider) {
		this.configurationProvider = configProvider;
		this.dotGen = new DotGen(configProvider);
		this.umlGen = new UmlGen(configProvider);
		this.emitter = new DotCodeBlockEmitter(dotGen);
	}


	public String convert(IPath filePath, String basepath, IDocument doc, ITypedRegion[] regions, Kind kind) {
		String text;
		switch (configurationProvider.getConverterType()) {
			case PANDOC:
				text = getText(filePath, doc, regions, true);
				return usePandoc(basepath, text, kind);
			case BLACKFRIDAY:
				text = getText(filePath, doc, regions, false);
				return useBlackFriday(basepath, text);
			case MARKDOWNJ:
				text = getText(filePath, doc, regions, false);
				return useMarkDownJ(basepath, text);
			case PEGDOWN:
				text = getText(filePath, doc, regions, false);
				return usePegDown(basepath, text);
			case COMMONMARK:
				text = getText(filePath, doc, regions, false);
				return useCommonMark(basepath, text);
			case TXTMARK:
				text = getText(filePath, doc, regions, false);
				return useTxtMark(basepath, text);
			case OTHER:
				text = getText(filePath, doc, regions, false);
				return useExternal(basepath, text);
		}
		return "";
	}

	// Use Pandoc
	private String usePandoc(String basepath, String text, Kind kind) {
		String cmd = configurationProvider.getPandocCommand();
		if (cmd.trim().isEmpty()) return "";

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
			args.add("markdown-smart");
		} else {
			args.add("--ascii");
		}
		return Cmd.process(args.toArray(new String[args.size()]), basepath, text);
	}

	// Use BlackFriday
	private String useBlackFriday(String basepath, String text) {
		String cmd = configurationProvider.getBlackFridayCommand();
		if (cmd.trim().isEmpty()) return "";

		List<String> args = new ArrayList<>();
		args.add(cmd);
		if (configurationProvider.addTableOfContents()) {
			args.add("-toc");
		}
		if (configurationProvider.isSmartMode()) {
			args.add("-smartypants");
			args.add("-fractions");
		}

		return Cmd.process(args.toArray(new String[args.size()]), basepath, text);
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
			return Cmd.process(args, basepath, text);
		}
		return "";
	}

	private String getText(IPath filePath, IDocument doc, ITypedRegion[] regions, boolean inclFM) {
		List<String> parts = new ArrayList<>();
		for (ITypedRegion region : regions) {
			String text = null;
			try {
				text = doc.get(region.getOffset(), region.getLength());
			} catch (BadLocationException e) {
				continue;
			}
			switch (region.getType()) {
				case Partitions.FRONT_MATTER:
					if (!inclFM) continue;
					break;
				case Partitions.DOTBLOCK:
					if (configurationProvider.isDotEnabled()) {
						text = filter(text, DOTBEG, DOTEND);
						text = translateDotCodeToHtmlFigure(text);
					}
					break;
				case Partitions.UMLBLOCK:
					if (configurationProvider.isPlantUMLEnabled()) {
						text = translatePumlCodeToHtmlFigure(text);
					}
					break;
				case Partitions.PLANTUML_INCLUDE:
					if (configurationProvider.isPlantUMLEnabled()) {
						text = translatePumlIncludeLineToHtml(text, filePath);
					}
					break;
				default:
					break;
			}
			parts.add(text);
		}

		return String.join(" ", parts);
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
		String figureCaption = readCaptionFrom(markdownCodeWithPumlIncludeStatement);
		IPath relativePumlFilePath = readPumlFilePath(markdownCodeWithPumlIncludeStatement);
        String remainingLine = getRemainderOfThePumlIncludeLine(markdownCodeWithPumlIncludeStatement);
        
        String pumlFileContent = readPumlFile(currentMarkdownFilePath, relativePumlFilePath);
        String pumlDiagram = convertPlantUml2Svg(pumlFileContent);
        
        String htmlFigure = createHtmlFigure(pumlDiagram, figureCaption);
        
        if (htmlFigure != null) {
        	return htmlFigure + remainingLine;
        }
        
        return markdownCodeWithPumlIncludeStatement;
	}
	
	private String getRemainderOfThePumlIncludeLine(String pumlIncludeLine) {
		if (pumlIncludeLine == null) {
			return null;
		}
		
		String remainingLine = "";
        int endOfPattern = findEndOfPumlFileInclusionStatement(pumlIncludeLine);
        if (endOfPattern < pumlIncludeLine.length()) {
        	remainingLine = pumlIncludeLine.substring(endOfPattern);
        }
        return remainingLine;
	}
	
	private int findEndOfPumlFileInclusionStatement(String text) {
		Pattern p = Pattern.compile(Lines.PATTERN_PLANTUML_INCLUDE);
        Matcher m = p.matcher(text);
        m.find();
        return m.end();
	}
	
	private IPath readPumlFilePath(String pumlFileInclusionStatement) {
		// we get something like ![any text](path/to/some/file.puml)
		// and want to extract the path to the puml file
		Pattern p = Pattern.compile("!\\[.+\\]\\("); // compare Lines.PATTERN_PLANTUML_INCLUDE
        Matcher m = p.matcher(pumlFileInclusionStatement);
        
        m.find();
        int indexOfFirstPathCharacter = m.end();
        String pumlFilePath = pumlFileInclusionStatement.substring(indexOfFirstPathCharacter);
        
        int indexOfFirstCharacterAfterPath = pumlFilePath.lastIndexOf(')');
        pumlFilePath = pumlFilePath.substring(0, indexOfFirstCharacterAfterPath);
        
        return new Path(pumlFilePath);
	}
	
	private String readPumlFile(IPath currentMarkdownFilePath, IPath relativePumlFilePath) {
		// remove file name, so that we get the current folder's path, then append the relative path of the target puml file
        IPath absolutePumlFilePath = currentMarkdownFilePath.removeLastSegments(1).append(relativePumlFilePath);
        
        File file = absolutePumlFilePath.toFile();
        String pumlFileContent = readTextFromFile(file);
        
        return pumlFileContent;
	}
	
	private String readCaptionFrom(String pumlFileInclusionStatement) {
		// we get something like ![The image's caption](path/to/some/file.puml)
		// and want to extract the caption text
		Assert.isTrue(pumlFileInclusionStatement != null && pumlFileInclusionStatement.startsWith("!["));
		
		String caption = pumlFileInclusionStatement.substring(2);
		
		int indexOfLastCaptionCharacter = caption.indexOf(']');
		return caption.substring(0, indexOfLastCaptionCharacter);
	}
	
	private String readTextFromFile(File file) {
		if (file != null && file.exists() && file.isFile()) {
			try {
				return Files.readString(
						Paths.get(file.getAbsolutePath()),
						StandardCharsets.UTF_8);
			} catch (IOException e) {
				Log.error(String.format("Could not read PlantUML file %s", file.getAbsolutePath()), e);
			}
		}
		return null;
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
        }
        return svgDiagram;
	}
	
	private String createHtmlFigure(String svgCode, String figureCaption) {
		String figureText;
		try {
			figureText = FileUtils.fromBundle("resources/html/puml-include.html", FluentUI.PLUGIN_ID);
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
			figureText = FileUtils.fromBundle("resources/html/figure.html", FluentUI.PLUGIN_ID);
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
