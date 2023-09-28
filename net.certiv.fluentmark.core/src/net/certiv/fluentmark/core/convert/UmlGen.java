/*******************************************************************************
 * Copyright (c) 2016 - 2018 Certiv Analytics and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.convert;

import org.eclipse.core.resources.IFile;

import java.util.Collections;
import java.util.List;

import java.io.ByteArrayOutputStream;
import java.io.File;

import java.nio.charset.Charset;

import net.certiv.fluentmark.core.util.Strings;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.dot.GraphvizUtils;
import net.sourceforge.plantuml.preproc.Defines;

public class UmlGen {

	private IConfigurationProvider configurationProvider;

	public UmlGen(IConfigurationProvider configProvider) {
		this.configurationProvider = configProvider;
	}

	public String uml2svg(List<String> lines) {
		return uml2svg(String.join(Strings.EOL, lines));
	}

	public String uml2svg(String data) {
		String dotexe = configurationProvider.getDotCommand();
		if (!dotexe.isEmpty()) {
			GraphvizUtils.setDotExecutable(dotexe);
		}

		System.setProperty("PLANTUML_SECURITY_PROFILE", "UNSECURE");
		
		String value;
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			SourceStringReader reader = new SourceStringReader(data);
			reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
			value = new String(os.toByteArray(), Charset.forName("UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException("PlantUML exception on" + Strings.EOL + data, e);
		}

		return value;
	}
	
	public IFile uml2svg(IFile pumlSourceFile) {
		
		String dotexe = configurationProvider.getDotCommand();
		if (!dotexe.isEmpty()) {
			GraphvizUtils.setDotExecutable(dotexe);
		}
		
		System.setProperty("PLANTUML_SECURITY_PROFILE", "UNSECURE");
		
		File sourceFile = new File(pumlSourceFile.getLocation().toString());
		File targetDir = sourceFile.getParentFile();
		File targetFile = null;
		SourceFileReader reader;
		try {
			reader = new SourceFileReader(Defines.createWithFileName(sourceFile),
					sourceFile, targetDir, Collections.<String>emptyList(), "UTF-8", new FileFormatOption(FileFormat.SVG));
			reader.setCheckMetadata(true);
			List<GeneratedImage> list = reader.getGeneratedImages();
			
			if (!list.isEmpty()) {
				GeneratedImage img = list.get(0);
				targetFile = img.getPngFile();
			}
		} catch (Exception e) {
			throw new RuntimeException("PlantUML exception on file " + pumlSourceFile.getLocation().toString(), e);
		}
		
		if (targetFile != null) {
			IFile file = (IFile) pumlSourceFile.getParent().findMember(targetFile.getName());
			return file;
		}

		return null;
	}
	
}
