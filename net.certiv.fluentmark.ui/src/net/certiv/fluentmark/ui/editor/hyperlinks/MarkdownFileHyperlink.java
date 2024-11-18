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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IRegion;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.util.EditorUtils;

public class MarkdownFileHyperlink extends FileHyperlink {
	
	private final String targetAnchor;
	
	public MarkdownFileHyperlink(IFile markdownTargetFile, IRegion linkTargetRegion, String anchor) {
		super (markdownTargetFile, linkTargetRegion);
		
		this.targetAnchor = anchor;
	}
	
	@Override
	public String getHyperlinkText() {
		if (targetAnchor != null) {
			return super.getHyperlinkText() + "#" + targetAnchor;
		}
		return super.getHyperlinkText();
	}
	
	@Override
	public void open() {
		FluentEditor editor = EditorUtils.openFileInFluentEditor(fileInWorkspace);
		
		if (editor != null && targetAnchor != null) {
			boolean success = editor.gotoAnchor(targetAnchor);
			
			if (!success) {
				FluentUI.log(IStatus.WARNING, String.format("Could not find anchor %s in file %s", targetAnchor, fileInWorkspace));
			}
		}
	}

}
