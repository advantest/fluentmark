/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.assist;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import net.certiv.fluentmark.ui.FluentImages;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.dialog.FindFileDialog;

public class FilePathCompletionProposalWithDialog implements ICompletionProposal {
	
	private final ITextEditor editor;
	private final int offset;
	private final int replacementLength;
	private final FileLinkCreationType creationType;
	
	private String replacementText;
	
	public enum FileLinkCreationType {
		PATH_ONLY,
		FILE_LINK,
		IMAGE
	}
	
	public FilePathCompletionProposalWithDialog(ITextEditor editor, int offset, int replacementLength, FileLinkCreationType creationType) {
		this.editor = editor;
		this.offset = offset;
		this.replacementLength = replacementLength;
		this.creationType = creationType;
	}

	@Override
	public void apply(IDocument document) {
		if (this.editor == null
				|| this.editor.getSite() == null
				|| this.editor.getSite().getShell() == null) {
			return;
		}
		
		IEditorInput input = this.editor.getEditorInput();
		if (input == null) {
			return;
		}
		
		IFile fileInEditor = input.getAdapter(IFile.class);
		if (fileInEditor == null) {
			return;
		}
		
		IPath openFileAbsolutePath = fileInEditor.getLocation();
		
		final Shell parentShell = this.editor.getSite().getShell();
		final IContainer workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		final FindFileDialog dialog = new FindFileDialog(parentShell, workspaceRoot);
		final int resultCode = dialog.open();
		
		if (resultCode != Window.OK) {
			return;
		}

		IFile[] selectedFiles = dialog.getResult();
		
		if (selectedFiles == null || selectedFiles.length != 1) {
			return;
		}
		
		IFile selectedFile = selectedFiles[0];
		IPath absoluteFilePath = selectedFile.getLocation();
		
		IPath relativePathToTargetFile = absoluteFilePath.makeRelativeTo(openFileAbsolutePath.removeLastSegments(1));
		
		String pathText = relativePathToTargetFile.toString();
		
		switch (creationType) {
			case PATH_ONLY -> replacementText = pathText;
			case FILE_LINK -> replacementText =  "[](" + pathText + ")";
			case IMAGE     -> replacementText = "![](" + pathText + ")";
		
			default -> {
				replacementText = pathText;
				FluentUI.log(IStatus.ERROR, "Unexpected value: " + creationType);
			}
		}
		
		try {
			document.replace(offset, replacementLength, replacementText);
		} catch (BadLocationException e) {
			FluentUI.log(IStatus.ERROR, "Could not insert proposed path into document.", e);
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		if (replacementText == null) {
			return null;
		}
		
		switch (creationType) {
			case PATH_ONLY:
				// place cursor after inserted path
				return new Point(offset + replacementText.length(), 0);
			case FILE_LINK:
				// place cursor in the square brackets: [here](...)
				return new Point(offset + 1, 0);
			case IMAGE:
				// place cursor in the square brackets: ![here](...)
				return new Point(offset + 2, 0);
		
			default:
				FluentUI.log(IStatus.ERROR, "Unexpected value: " + creationType);
				return null;
		}
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public String getDisplayString() {
		String displayString;
		switch (creationType) {
			case PATH_ONLY -> displayString = "Select file...";
			case FILE_LINK -> displayString = "Create file link...";
			case IMAGE     -> displayString = "Create image...";
		
			default -> {
				displayString = "Select file...";
				FluentUI.log(IStatus.ERROR, "Unexpected value: " + creationType);
			}
		}
		
		return displayString;
	}

	@Override
	public Image getImage() {
		return FluentUI.getDefault().getImageProvider().get(FluentImages.DESC_OBJ_LIST);
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

}
