/*******************************************************************************
 * Copyright (c) 2016 - 2018 Certiv Analytics and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.convert;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;

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
		File sourceFile = new File(pumlSourceFile.getLocation().toString());
		File targetFile = uml2svg(sourceFile);
		
		if (targetFile != null) {
			IContainer parentFolder = pumlSourceFile.getParent();
			try {
				parentFolder.refreshLocal(IResource.DEPTH_ONE, null);
				IFile file = (IFile) parentFolder.findMember(targetFile.getName());
				return file;
			} catch (CoreException e) {
				throw new RuntimeException(
						String.format(
								"Could not refresh (read files in) path %s",
								parentFolder.getLocation().toString()),
						e);
			}
		}
		
		return null;
	}
	
	public File uml2svg(File pumlSourceFile) {
		return uml2svg(pumlSourceFile, null);
	}


	public File uml2svg(File pumlSourceFile, File targetDirectory) {
		
		String dotexe = configurationProvider.getDotCommand();
		if (!dotexe.isEmpty()) {
			GraphvizUtils.setDotExecutable(dotexe);
		}
		
		System.setProperty("PLANTUML_SECURITY_PROFILE", "UNSECURE");
		
		File targetDir = targetDirectory == null ? pumlSourceFile.getParentFile() : targetDirectory;
		File targetFile = null;
		SourceFileReader reader;
		try {
			reader = new SourceFileReader(Defines.createWithFileName(pumlSourceFile),
					pumlSourceFile, targetDir, Collections.<String>emptyList(), "UTF-8", new FileFormatOption(FileFormat.SVG));
			reader.setCheckMetadata(true);
			List<GeneratedImage> list = reader.getGeneratedImages();
			
			if (!list.isEmpty()) {
				GeneratedImage img = list.get(0);
				targetFile = img.getPngFile();
			}
		} catch (Exception e) {
			throw new RuntimeException("PlantUML exception on file " + pumlSourceFile.getAbsolutePath(), e);
		}
		
		return targetFile;
	}
	
}
