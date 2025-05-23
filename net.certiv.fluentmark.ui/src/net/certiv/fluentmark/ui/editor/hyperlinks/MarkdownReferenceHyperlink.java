/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.hyperlinks;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.IRegion;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.util.EditorUtils;

public class MarkdownReferenceHyperlink extends FileHyperlink {
	
	private final String targetReferenceDefinitionName;
	
	public MarkdownReferenceHyperlink(IFile markdownTargetFile, IRegion linkTargetRegion, String referenceDefinitionName) {
		super (markdownTargetFile, linkTargetRegion);
		
		this.targetReferenceDefinitionName = referenceDefinitionName;
	}
	
	@Override
	public String getHyperlinkText() {
		return "Jump to link reference definition " + targetReferenceDefinitionName;
	}
	
	@Override
	public void open() {
		// The file should already be opened in FluentEditor. The following just returns the editor.
		FluentEditor editor = EditorUtils.openFileInFluentEditor(fileInWorkspace);
		
		if (editor != null && targetReferenceDefinitionName != null) {
			boolean success = editor.gotoLinkReferenceDefinition(targetReferenceDefinitionName);
			
			if (!success) {
				FluentUI.log(IStatus.WARNING, String.format("Could not find link reference definition \"%s\" in file %s", targetReferenceDefinitionName, fileInWorkspace));
			}
		}
	}

}
