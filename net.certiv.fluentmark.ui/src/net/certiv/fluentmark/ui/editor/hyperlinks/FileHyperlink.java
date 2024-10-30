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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import net.certiv.fluentmark.ui.Log;

public class FileHyperlink implements IHyperlink {
	
	private final IFile fileInWorkspace;
	private final IRegion linkTargetRegion;
	private final IFileStore fileOutsideWorkspace;
	
	public FileHyperlink(IFile markdownTargetFile, IRegion linkTargetRegion) {
		Assert.isNotNull(linkTargetRegion);
		Assert.isNotNull(markdownTargetFile);
		Assert.isTrue(markdownTargetFile.exists());
		
		this.fileInWorkspace = markdownTargetFile;
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
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null && !workbench.isClosing()) {
			IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
			if (workbenchWindow != null && !workbenchWindow.isClosing()) {
				IWorkbenchPage activePage = workbenchWindow.getActivePage();
				
				if (activePage != null) {
					try {
						if (fileInWorkspace != null) {
							IDE.openEditor(activePage, fileInWorkspace);
						} else {
							IDE.openEditorOnFileStore(activePage, fileOutsideWorkspace);
						}
					} catch (PartInitException e) {
						Log.error(String.format("Could not open file (path=%s) in default editor", fileInWorkspace.getLocation()), e);
					}
				}
			}
		}
	}

}
