/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.assist;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import java.util.ArrayList;

import java.io.File;

import net.certiv.fluentmark.ui.FluentUI;

public class FileLinkContentAssistProcessor implements IContentAssistProcessor {

	private static final char[] COMPLETION_PROPOSAL_AUTO_ACTIVATION_CHARS = { '/' };
	private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];
	
	private ITextEditor editor;
	
	public FileLinkContentAssistProcessor(ITextEditor editor) {
		this.editor = editor;
	}
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument document = viewer.getDocument();
		try {
			int currentLine = document.getLineOfOffset(offset);
			int lineOffset = document.getLineOffset(currentLine);
			int lineLength = document.getLineLength(currentLine);
			// TODO check document length
			String lineLeftFromCursor = lineLength > 0 ? document.get(lineOffset, offset - lineOffset) : "";
			String lineRightFromCursor = lineLength > 0 ? document.get(offset, lineOffset + lineLength - offset - 1) : "";
			
			int indexOfClosingRoundBracket = lineRightFromCursor.indexOf(')');
			int indexOfOpeningRoundBracket = lineLeftFromCursor.lastIndexOf('(');
			int indexOfClosingSquareBracket = -1;
			if (indexOfOpeningRoundBracket > -1
					&& indexOfOpeningRoundBracket - 1 >= 0
					&& lineLeftFromCursor.charAt(indexOfOpeningRoundBracket - 1) == ']') {
				indexOfClosingSquareBracket = indexOfOpeningRoundBracket - 1;
			}
			int indexOfOpeningSquareBracket = -1;
			if (indexOfClosingSquareBracket > -1) {
				indexOfOpeningSquareBracket = lineLeftFromCursor.substring(0, indexOfClosingSquareBracket).lastIndexOf('[');
			}
			
			// we're not in a link declaration? ==> we have no proposals
			if (indexOfClosingRoundBracket < 0 || indexOfOpeningRoundBracket < 0
					|| indexOfClosingSquareBracket < 0 || indexOfOpeningSquareBracket < 0) {
				return NO_PROPOSALS;
			}
			
			int indexOfExclamationMark = -1;
			if (indexOfOpeningSquareBracket > -1
					&& indexOfOpeningSquareBracket - 1 >= 0
					&& lineLeftFromCursor.charAt(indexOfOpeningSquareBracket - 1) == '!') {
				indexOfExclamationMark = indexOfOpeningSquareBracket - 1;
			}
			
			String linkTextLeftFromCursor = "";
			if (indexOfOpeningRoundBracket + 1 < lineLeftFromCursor.length()) {
				linkTextLeftFromCursor = lineLeftFromCursor.substring(indexOfOpeningRoundBracket + 1); 
			}
			
			String linkTextRightFromCursor = lineRightFromCursor.substring(0, indexOfClosingRoundBracket);
			
			// no proposals in case of web addresses
			if (linkTextLeftFromCursor.startsWith("http://")
					|| linkTextLeftFromCursor.startsWith("HTTP://")
					|| linkTextLeftFromCursor.startsWith("https://")
					|| linkTextLeftFromCursor.startsWith("HTTPS://")) {
				return NO_PROPOSALS;
			}
			
			IFile currentEditorsMarkdownFile = getCurrentEditorsMarkdownFile();
			if (currentEditorsMarkdownFile == null) {
				return NO_PROPOSALS;
			}
			
			IPath currentResourcesAbsolutePath = readCurrentResourcePath(
					currentEditorsMarkdownFile, linkTextLeftFromCursor);
			
			// no proposals if the path is invalid
			File currentFile = currentResourcesAbsolutePath.toFile();
			if (!currentFile.exists()) {
				return NO_PROPOSALS;
			}
			
			ArrayList<ICompletionProposal> proposals = new ArrayList<>();
			
			File currentDir =  null;
			if (currentResourcesAbsolutePath.equals(currentEditorsMarkdownFile.getLocation())) {
				currentDir = currentFile.getParentFile();
			} else {
				if (currentFile.isDirectory()) {
					currentDir = currentFile;
					// propose contained files in the next steps
				} else if (currentFile.isFile()) {
					// we have already a complete file path, no other proposals
					return NO_PROPOSALS;
				}
			}
			
			if (currentDir != null) {
				File[] files = currentDir.listFiles();
				for (File fileInDir : files) {
					if (fileInDir.equals(currentEditorsMarkdownFile.getLocation().toFile())) {
						continue;
					}
					String filePathSegment = fileInDir.getName();
					if (!linkTextLeftFromCursor.isBlank()
							&& !linkTextLeftFromCursor.endsWith("/")) {
						filePathSegment = '/' + filePathSegment;
					}
					proposals.add(new CompletionProposal(filePathSegment, offset, linkTextRightFromCursor.length(), filePathSegment.length()));
				}
				
				String parentDir = currentDir.getParent();
				if (parentDir != null) {
					String toParentPathSegment = "..";
					if (!linkTextLeftFromCursor.isBlank()
							&& !linkTextLeftFromCursor.endsWith("/")) {
						toParentPathSegment = '/' + toParentPathSegment;
					}
					proposals.add(new CompletionProposal(toParentPathSegment, offset, linkTextRightFromCursor.length(), toParentPathSegment.length()));
				}
			}
			
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (BadLocationException e) {
			FluentUI.log(IStatus.ERROR, "Failed reading document for code assist proposals.", e);
			return NO_PROPOSALS;
		}
	}
	
	private IFile getCurrentEditorsMarkdownFile() {
		if (this.editor != null) {
			IEditorInput editorInput = this.editor.getEditorInput();
			if (editorInput != null) {
				return editorInput.getAdapter(IFile.class);
			}
		}
		return null;
	}
	
	private IPath readCurrentResourcePath(IFile currentlyOpenMarkdownFile, String currentFileLinkText) {
		if (currentFileLinkText.isBlank()) {
			return currentlyOpenMarkdownFile.getLocation();
		}
		
		IPath resourceRelativePath = new Path(currentFileLinkText);
		return toAbsolutePath(resourceRelativePath, currentlyOpenMarkdownFile);
	}
	
	private IPath toAbsolutePath(IPath resourceRelativePath, IResource currentResource) {
		IPath absolutePath;
		if (resourceRelativePath.equals(currentResource.getLocation())) {
			absolutePath = currentResource.getLocation();
		} else {
			absolutePath = currentResource.getLocation().removeLastSegments(1).append(resourceRelativePath);
		}
		return absolutePath;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return COMPLETION_PROPOSAL_AUTO_ACTIVATION_CHARS;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}
