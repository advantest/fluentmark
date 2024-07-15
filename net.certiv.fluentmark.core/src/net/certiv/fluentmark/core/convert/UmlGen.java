/*******************************************************************************
 * Copyright (c) 2016 - 2018 Certiv Analytics and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.convert;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.advantest.plantuml.PlantUmlToSvgRenderer;

import net.certiv.fluentmark.core.util.Strings;

public class UmlGen {

	private IConfigurationProvider configurationProvider;
	private PlantUmlToSvgRenderer renderer;

	public UmlGen(IConfigurationProvider configProvider) {
		this.configurationProvider = configProvider;
		this.renderer = new PlantUmlToSvgRenderer();
	}

	public String uml2svg(List<String> lines) {
		return uml2svg(String.join(Strings.EOL, lines));
	}

	public String uml2svg(String plantUmlCode) {
		String dotexe = configurationProvider.getDotCommand();
		renderer.setDotExecutable(dotexe);

		System.setProperty("PLANTUML_SECURITY_PROFILE", "UNSECURE");
		
		return renderer.plantUmlToSvg(plantUmlCode);
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
		renderer.setDotExecutable(dotexe);
		
		System.setProperty("PLANTUML_SECURITY_PROFILE", "UNSECURE");
		
		return renderer.plantUmlToSvg(pumlSourceFile, targetDirectory);
	}
	
}
