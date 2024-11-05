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

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import net.certiv.fluentmark.ui.util.EditorUtils;

public class FileHyperlink implements IHyperlink {
	
	protected final IFile fileInWorkspace;
	protected final IRegion linkTargetRegion;
	protected final IFileStore fileOutsideWorkspace;
	
	public FileHyperlink(IFile targetFile, IRegion linkTargetRegion) {
		Assert.isNotNull(linkTargetRegion);
		Assert.isNotNull(targetFile);
		Assert.isTrue(targetFile.exists());
		
		this.fileInWorkspace = targetFile;
		this.linkTargetRegion = linkTargetRegion;
		this.fileOutsideWorkspace = null;
	}
	
	public FileHyperlink(IFileStore fileOutsideWorkspace, IRegion linkTargetRegion) {
		Assert.isNotNull(linkTargetRegion);
		Assert.isNotNull(fileOutsideWorkspace);
		Assert.isTrue(fileOutsideWorkspace.fetchInfo().exists());
		
		this.fileInWorkspace = null;
		this.linkTargetRegion = linkTargetRegion;
		this.fileOutsideWorkspace = fileOutsideWorkspace;
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return linkTargetRegion;
	}

	@Override
	public String getTypeLabel() {
		return "Target file";
	}

	@Override
	public String getHyperlinkText() {
		return "Open file " + fileInWorkspace != null ? fileInWorkspace.getName() : fileOutsideWorkspace.getName();
	}

	@Override
	public void open() {
		if (fileInWorkspace != null) {
			EditorUtils.openFileInDefaultEditor(fileInWorkspace);
		} else {
			EditorUtils.openFileInDefaultEditor(fileOutsideWorkspace);
		}
	}

}
