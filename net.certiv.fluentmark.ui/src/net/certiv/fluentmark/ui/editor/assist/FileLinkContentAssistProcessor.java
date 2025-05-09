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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import java.io.File;

import net.certiv.fluentmark.ui.FluentImages;
import net.certiv.fluentmark.ui.FluentUI;

public class FileLinkContentAssistProcessor implements IContentAssistProcessor {

	private static final char[] COMPLETION_PROPOSAL_AUTO_ACTIVATION_CHARS = { '/' };
	private static final ICompletionProposal[] NO_PROPOSALS = new ICompletionProposal[0];
	
	private final ITextEditor editor;
	
	public FileLinkContentAssistProcessor(ITextEditor editor) {
		this.editor = editor;
	}
	
	/**
	 * We compute completion proposals for links, image references, and link reference definitions.
	 * 
	 * 
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument document = viewer.getDocument();
		
		IFile currentEditorsMarkdownFile = getCurrentEditorsMarkdownFile();
		if (currentEditorsMarkdownFile == null) {
			return NO_PROPOSALS;
		}
		
		try {
			int currentLine = document.getLineOfOffset(offset);
			int lineOffset = document.getLineOffset(currentLine);
			int lineLength = document.getLineLength(currentLine);
			
			String lineLeftFromCursor = lineLength > 0 ? document.get(lineOffset, offset - lineOffset) : "";
			String lineRightFromCursor = lineLength > 0 ? document.get(offset, lineOffset + lineLength - offset) : "";
			
			ArrayList<ICompletionProposal> proposals = new ArrayList<>();
			
			addProposalsForLinksAndImages(proposals, currentEditorsMarkdownFile,
					offset, currentLine, lineOffset, lineLength, lineLeftFromCursor, lineRightFromCursor);
			
			addProposalsForLinkReferenceDefinitions(proposals, currentEditorsMarkdownFile, document,
					offset, currentLine, lineOffset, lineLength, lineLeftFromCursor, lineRightFromCursor);
			
			return proposals.toArray(new ICompletionProposal[proposals.size()]);
		} catch (BadLocationException e) {
			FluentUI.log(IStatus.ERROR, "Failed reading document for code assist proposals.", e);
			return NO_PROPOSALS;
		}
	}
	
	private void addProposalsForLinksAndImages(List<ICompletionProposal> proposals, IFile currentEditorsMarkdownFile,
			int offset, int currentLine, int lineOffset, int lineLength, String lineLeftFromCursor, String lineRightFromCursor) {
		/*
		 *  Try to detect if we're in a statement like
		 *  [title](file reference path)
		 *  or
		 *  ![title](file reference path)
		 *  where the cursor is between the "(" and ")" brackets.
		 */
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
			return;
		}
		
		// TODO only propose image files in case we're in an image reference?
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
		
		addFilePathProposals(proposals, currentEditorsMarkdownFile,
				offset, linkTextLeftFromCursor, linkTextRightFromCursor);
	}
	
	private void addProposalsForLinkReferenceDefinitions(List<ICompletionProposal> proposals, IFile currentEditorsMarkdownFile, IDocument document,
			int offset, int currentLine, int lineOffset, int lineLength, String lineLeftFromCursor, String lineRightFromCursor) {
		/*
		 *  Try to detect if we're in a statement like
		 *  [important]: ../../src/main/java/com/advantest/module/SomeClass.java "Important File"
		 *  or
		 *     [important]:
		 *        ../../src/main/java/com/advantest/module/SomeClass.java
		 *            'Some Important File Description
		 *            spanning over
		 *            multiple lines'
		 *  where the cursor is between the ":" and the optional title.
		 *  See https://spec.commonmark.org/0.30/#link-reference-definitions
		 */
		
		int indexOfColon = lineLeftFromCursor.lastIndexOf(':');
		int lineOfColon = -1;
		
		if (indexOfColon < 0 && currentLine - 1 >= 0) {
			int prevLineNumber = currentLine - 1;
			String previousLine;
			try {
				int prevLineOffset = document.getLineOffset(prevLineNumber);
				int prevLineLength =  document.getLineLength(prevLineNumber);
				previousLine = document.get(prevLineOffset, prevLineLength);
			} catch (BadLocationException e) {
				FluentUI.log(IStatus.ERROR, "Failed reading document for code assist proposals.", e);
				return;
			}
			
			// abort if the previous line does not only contain the link label (then it's not a link reference definition)
			if (!previousLine.matches(" {0,3}\\[.*\\]:\\s*\\n")) {
				return;
			}
			
			indexOfColon = previousLine.lastIndexOf(':');
			if (indexOfColon >= 0) {
				lineOfColon = prevLineNumber;
			}
		} else {
			lineOfColon = currentLine;
			
			// abort if we don't find the mandatory link label (then it's not a link reference definition)
			String textLeftFromColon = lineLeftFromCursor.substring(0, indexOfColon);
			if (!textLeftFromColon.matches(" {0,3}\\[.*\\]")) {
				return;
			}
		}
		
		String linkTextLeftFromCursor = "";
		if (lineOfColon == currentLine) {
			linkTextLeftFromCursor = indexOfColon + 1 <= offset ? lineLeftFromCursor.substring(indexOfColon + 1) : "";
			linkTextLeftFromCursor = linkTextLeftFromCursor.trim();
		} else {
			linkTextLeftFromCursor = lineLeftFromCursor.trim();
		}
		
		// abort if the remainder of the line doesn't look like being a link reference definition
		if (!lineRightFromCursor.matches(".*( \\t)*('\")*\\n*")) {
			return;
		}
		
		int indexOfTitleBegin = lineRightFromCursor.indexOf('\'');
		int indexOfTitleBegin2 = lineRightFromCursor.indexOf('"');
		if (indexOfTitleBegin2 >= 0 && (indexOfTitleBegin == -1 || indexOfTitleBegin > indexOfTitleBegin2)) {
			indexOfTitleBegin = indexOfTitleBegin2;
		}
		
		String linkTextRightFromCursor = indexOfTitleBegin >= 0 ? lineRightFromCursor.substring(0, indexOfTitleBegin) : lineRightFromCursor;
		linkTextRightFromCursor = linkTextRightFromCursor.trim();
		
		addFilePathProposals(proposals, currentEditorsMarkdownFile,
				offset, linkTextLeftFromCursor, linkTextRightFromCursor);
	}
	
	private void addFilePathProposals(List<ICompletionProposal> proposals, IFile currentEditorsMarkdownFile,
			int offset, String linkTextLeftFromCursor, String linkTextRightFromCursor) {
		
		// no proposals in case of web addresses
		if (linkTextLeftFromCursor.startsWith("http://")
				|| linkTextLeftFromCursor.startsWith("HTTP://")
				|| linkTextLeftFromCursor.startsWith("https://")
				|| linkTextLeftFromCursor.startsWith("HTTPS://")) {
			return;
		}
		
		String linkTextLeftFromCursorWithoutLastPartialSegment = linkTextLeftFromCursor;
		String lastPathSegmentPart = "";
		int indexOfLastSlash = linkTextLeftFromCursor.lastIndexOf('/');
		if (indexOfLastSlash >= 0) {
			linkTextLeftFromCursorWithoutLastPartialSegment = linkTextLeftFromCursor.substring(0, indexOfLastSlash);
			if (indexOfLastSlash + 1 < linkTextLeftFromCursor.length()) {
				lastPathSegmentPart = linkTextLeftFromCursor.substring(indexOfLastSlash + 1);
			}
		}
		
		IPath currentResourcesAbsolutePath = readCurrentResourcePath(
				currentEditorsMarkdownFile, linkTextLeftFromCursorWithoutLastPartialSegment);
		
		// no proposals if the path is invalid
		File currentFile = currentResourcesAbsolutePath.toFile();
		if (!currentFile.exists()) {
			return;
		}
		
		File currentDir =  null;
		if (currentResourcesAbsolutePath.equals(currentEditorsMarkdownFile.getLocation())) {
			currentDir = currentFile.getParentFile();
		} else {
			if (currentFile.isDirectory()) {
				currentDir = currentFile;
				// propose contained files in the next steps
			} else if (currentFile.isFile()) {
				// we have already a complete file path, no other proposals
				return;
			}
		}
		
		if (currentDir == null) {
			return;
		}
		
		proposals.add(new FilePathCompletionProposalWithDialog(editor,
				offset - linkTextLeftFromCursor.length(), linkTextLeftFromCursor.length() + linkTextRightFromCursor.length()));
		
		
		List<File> files = Arrays.asList(currentDir.listFiles());
		files.sort(new Comparator<File>() {

			@Override
			public int compare(File file1, File file2) {
				boolean file1IsFile = file1.isFile();
				boolean file2IsFile = file2.isFile();
				
				if (file1IsFile && !file2IsFile) {
					return -1;
				} else if (file2IsFile && !file1IsFile) {
					return 1;
				}
				
				return file1.getName().compareTo(file2.getName());
			}
			
		});
		
		int replacementLengthLeftFromCursor = linkTextLeftFromCursor.length() - linkTextLeftFromCursorWithoutLastPartialSegment.length();
		int newOffset = offset - replacementLengthLeftFromCursor;
		
		for (File fileInDir : files) {
			// skip the file we have already open in editor
			if (fileInDir.equals(currentEditorsMarkdownFile.getLocation().toFile())) {
				continue;
			}
			
			// skip files that do not match the given file name prefix
			if (lastPathSegmentPart.length() > 0
					&& !fileInDir.getName().startsWith(lastPathSegmentPart)) {
				continue;
			}
			
			String filePathSegment = fileInDir.getName();
			if (!linkTextLeftFromCursorWithoutLastPartialSegment.isBlank()) {
				filePathSegment = '/' + filePathSegment;
			}
			
			
			Image img = null;
			if (fileInDir.isDirectory()) {
				img = FluentUI.getDefault().getImageProvider().get(FluentImages.DESC_OBJ_FOLDER); 
			} else {
				img = FluentUI.getDefault().getImageProvider().get(FluentImages.DESC_OBJ_FILE);
			}
			
			proposals.add(new CompletionProposal(filePathSegment, newOffset, replacementLengthLeftFromCursor + linkTextRightFromCursor.length(), filePathSegment.length(), img, null, null, null));
		}
		
		String parentDir = currentDir.getParent();
		if (parentDir != null) {
			String toParentPathSegment = "..";
			if (!linkTextLeftFromCursor.isBlank()
					&& !linkTextLeftFromCursor.endsWith("/")) {
				toParentPathSegment = '/' + toParentPathSegment;
			}
			
			Image img = FluentUI.getDefault().getImageProvider().get(FluentImages.DESC_OBJ_FOLDER_UP); 
			proposals.add(new CompletionProposal(toParentPathSegment, offset, linkTextRightFromCursor.length(), toParentPathSegment.length(), img, null, null, null));
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
