/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.hyperlinks;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IRegion;

import net.certiv.fluentmark.ui.util.EditorUtils;

public class JavaMemberHyperlink extends FileHyperlink {
	
	private final String javaMemberReference;
	
	public JavaMemberHyperlink(IFile javaTargetFile, IRegion linkTargetRegion, String javaMemberReference) {
		super (javaTargetFile, linkTargetRegion);
		
		Assert.isNotNull(javaMemberReference);
		Assert.isTrue(!javaMemberReference.isBlank());
		
		this.javaMemberReference = javaMemberReference;
	}
	
	@Override
	public String getHyperlinkText() {
		return super.getHyperlinkText() + ", Member: " + javaMemberReference;
	}
	
	@Override
	public void open() {
		EditorUtils.openFileInJavaEditor(fileInWorkspace, javaMemberReference);
	}

}
