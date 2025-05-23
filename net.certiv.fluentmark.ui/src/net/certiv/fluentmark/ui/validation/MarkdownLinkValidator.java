/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.markdown.MarkdownParsingTools;
import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.core.markdown.RegexMatch;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.editor.text.MarkdownPartioningTools;
import net.certiv.fluentmark.ui.markers.ITypedRegionMarkerCalculator;
import net.certiv.fluentmark.ui.markers.MarkerCalculator;


public class MarkdownLinkValidator extends AbstractLinkValidator implements ITypedRegionMarkerCalculator {
	
	private FilePathValidator filePathValidator;
	
	public MarkdownLinkValidator() {
		filePathValidator = new FilePathValidator();
	}
	
	@Override
	public void setupDocumentPartitioner(IDocument document, IFile file) {
		MarkdownPartioningTools.getTools().setupDocumentPartitioner(document);
	}
	
	@Override
	public ITypedRegion[] computePartitioning(IDocument document, IFile file) throws BadLocationException {
		return MarkdownPartioningTools.getTools().computePartitioning(document);
	}
	
	@Override
	public boolean isValidatorFor(IFile file) {
		return FileUtils.isMarkdownFile(file);
	}
	
	@Override
	public boolean isValidatorFor(ITypedRegion region, IFile file) {
		return IDocument.DEFAULT_CONTENT_TYPE.equals(region.getType())
				|| MarkdownPartitions.PLANTUML_INCLUDE.equals(region.getType());
	}

	@Override
	public void validateRegion(ITypedRegion region, IDocument document, IFile file) throws CoreException {
		String regionContent;
		try {
			regionContent = document.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			return;
		}

		MarkdownParsingTools.findLinksAndImages(regionContent)
			.forEach(match -> {
				try {
					validateLinkStatement(region, document, file, match, regionContent);
				} catch (Exception e) {
					FluentUI.log(IStatus.WARNING, String.format("Could not validate statement \"%s\".", match.matchedText), e);
				}
		});
		
		
		// check link reference definition targets
		MarkdownParsingTools.findLinkReferenceDefinitions(regionContent)
			.forEach(match -> {
				try {
					validateLinkReferenceDefinitionStatement(region, document, file, match);
				} catch (Exception e) {
					FluentUI.log(IStatus.WARNING, String.format("Could not validate statement \"%s\".", match.matchedText), e);
				}
		});
		
		// check link reference definition identifiers are unique
		Map<String,List<RegexMatch>> linkRefDefIds = new HashMap<>();
		MarkdownParsingTools.findLinkReferenceDefinitions(regionContent)
			.map(linkRefDefMatch -> linkRefDefMatch.subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_LABEL))
			.forEach(idMatch -> {
				List<RegexMatch> matches = linkRefDefIds.get(idMatch.matchedText);
				if (matches == null) {
					matches = new ArrayList<>(2);
					linkRefDefIds.put(idMatch.matchedText, matches);
				}
				matches.add(idMatch);
			});
		
		linkRefDefIds.keySet().stream()
			.filter(id -> linkRefDefIds.get(id).size() > 1)
			.forEach(id -> {
				String lines = linkRefDefIds.get(id).stream()
						.map(idMatch -> getLineForOffset(document,
								region.getOffset() + idMatch.startIndex))
						.map(line -> String.valueOf(line))
						.reduce("", (text1, text2) -> text1.isBlank() ? text2 : text1 + ", " + text2);
				
				for (RegexMatch idMatch : linkRefDefIds.get(id)) {
					int offset = region.getOffset() + idMatch.startIndex;
					int endOffset = region.getOffset() + idMatch.endIndex;
					int lineNumber = getLineForOffset(document, offset);
					
					try {
						MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
								"The link reference definition identifier \"" + id + "\" is not unique."
								+ " The same identifier is used in the following lines: " + lines,
								lineNumber,
								offset,
								endOffset);
					} catch (CoreException e) {
						FluentUI.log(IStatus.WARNING, "Could not create validation marker.", e);
					}
				}
			});
		
		Stream.concat(
				MarkdownParsingTools.findFullAndCollapsedReferenceLinks(regionContent),
				MarkdownParsingTools.findShortcutReferenceLinks(regionContent))
			.forEach(match -> {
				try {
					validateReferenceLinkLabel(region, document, file, match);
				} catch (Exception e) {
					FluentUI.log(IStatus.WARNING, String.format("Could not validate statement \"%s\".", match.matchedText), e);
				}
		});
	}
	
	protected void validateLinkStatement(ITypedRegion region, IDocument document, IFile file,
			RegexMatch linkStatementMatch, String regionContent) throws CoreException {
		
		RegexMatch targetMatch = linkStatementMatch.subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_TARGET);
		String linkTarget = targetMatch.matchedText;
		
		int linkTargetStartIndex = targetMatch.startIndex - linkStatementMatch.startIndex;
		
		if (linkTarget.contains("(")) {
			// In some cases the regex for links doesn't catch the last ')'. Thus we have to parse the text once again
			// Here is an example of such a case where the regex doesn't match correctly (last ')' is missing):
			// [a method in Java code](SomeClass.java#doSomething(String)
			// Changing the regex to match the ')' greedy instead of lazy would result in wrong matches
			// in lines where we have more than one link statement.
			// The greedy match would match the last ')' in the line.
			
			String linkTargetWithRest = regionContent.substring(linkStatementMatch.startIndex + linkTargetStartIndex);
			String[] lines = linkTargetWithRest.split("\\n");
			linkTargetWithRest = lines[0];
			
			// parse link statement in the current line
			int pos = 0;
			int roundBracketsNotClosedYet = 1;
			int endIndex = -1;
			while (pos < linkTargetWithRest.length() && endIndex < 0) {
				char currentChar = linkTargetWithRest.charAt(pos);
				
				if (currentChar == '(') {
					roundBracketsNotClosedYet++;
				} else if (currentChar == ')') {
					roundBracketsNotClosedYet--;
					if (roundBracketsNotClosedYet == 0) {
						endIndex = pos;
					}
				}
				
				pos++;
			}
			
			if (endIndex >= 0) {
				linkTarget = linkTargetWithRest.substring(0, endIndex);
			}
		}
		
		checkLinkTarget(linkTarget, linkTargetStartIndex, region, document, file, linkStatementMatch.startIndex);
	}
	
	protected void validateLinkReferenceDefinitionStatement(ITypedRegion region, IDocument document, IFile file,
			RegexMatch linkReferenceDefinitionStatementMatch) throws CoreException {
		
		RegexMatch targetMatch = linkReferenceDefinitionStatementMatch.subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_TARGET);
		String linkTarget = targetMatch.matchedText;
		
		int linkTargetStartIndex = targetMatch.startIndex - linkReferenceDefinitionStatementMatch.startIndex;
		
		checkLinkTarget(linkTarget, linkTargetStartIndex, region, document, file, linkReferenceDefinitionStatementMatch.startIndex);
	}
	
	protected void validateReferenceLinkLabel(ITypedRegion region, IDocument document, IFile file,
			RegexMatch referenceLinkMatch) throws CoreException {
		
		RegexMatch targetMatch = referenceLinkMatch.subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_TARGET);
		RegexMatch labelMatch = referenceLinkMatch.subMatches.get(MarkdownParsingTools.CAPTURING_GROUP_LABEL);
		
		// First, we assume a full reference link like [link text][linkLabel]
		// or a shortcut reference link like [linkLabel]
		String linkLabel = targetMatch.matchedText;
		
		// in some rare cases we have a collapsed reference link like [linkLabel][]
		boolean collapsedReferenceLink = false;
		if (linkLabel.isEmpty() && labelMatch != null && !labelMatch.matchedText.isBlank()) {
			linkLabel = labelMatch.matchedText;
			collapsedReferenceLink = true;
		}
		
		if (linkLabel.isBlank()) {
			int offset = region.getOffset() + targetMatch.startIndex;
			int endOffset = offset + linkLabel.length();
			
			// increase error marker region to include surrounding brackets, otherwise there is no character to underline
			if (linkLabel.length() == 0) {
				offset -= 1;
				endOffset += 1;
			}
			
			int lineNumber = getLineForOffset(document, offset);
			
			MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
					"The reference link label is empty. Please create a link reference definition like \"[ReferenceLinkLabel]: https://plantuml.com\""
					+ " and use that reference link label in your link, e.g. \"[your link text][ReferenceLinkLabel]\" or \"[ReferenceLinkLabel]\".",
					lineNumber,
					offset,
					endOffset);
			return;
		}
		
		String documentContent = document.get();
		
		Optional<RegexMatch> match = MarkdownParsingTools.findLinkReferenceDefinition(documentContent, linkLabel);
		if (match.isEmpty()) {
			if (collapsedReferenceLink) {
				// An empty full reference link looks like a collapsed reference link.
				// We have to adapt our marker message in that case.
				// full reference link: [Some text][RefID]
				// collapsed reference link: [RefID][]
				// empty full reference link: [Some text][]
				
				// In this case, we create a marker for the whole reference link statement
				int offset = region.getOffset() + referenceLinkMatch.startIndex;
				int endOffset = offset + referenceLinkMatch.matchedText.length();
				int lineNumber = getLineForOffset(document, offset);
				
				MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
						"There is either no link reference definition for the reference link label \"" + linkLabel + "\" (assuming this is a collapsed reference link like \"[ReferenceLinkLabel][]\")"
								+ " or the reference link label is empty  (assuming this is a full reference link like \"[Some text][ReferenceLinkLabel]\")."
								+ " Expected a link reference definition like \"[" + linkLabel + "]: https://plantuml.com\""
								+ " or a reference link \"[" + linkLabel + "][ReferenceLinkLabel]\" to an existing link reference definition.",
						lineNumber,
						offset,
						endOffset);
			} else {
				int offset = region.getOffset() + targetMatch.startIndex;
				int endOffset = offset + linkLabel.length();
				int lineNumber = getLineForOffset(document, offset);
				
				MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
						"There is no link reference definition for the reference link label \"" + linkLabel + "\". Expected a link reference definition like \"[ReferenceLinkLabel]: https://plantuml.com\"",
						lineNumber,
						offset,
						endOffset);
			}
		}
	}
	
	private void checkLinkTarget(String linkTarget, int linkTargetStartIndex,
			ITypedRegion region, IDocument document, IFile file,
			int linkStatementStartIndexInRegion) throws CoreException {
		
		UriDto uriDto = parseUri(linkTarget);
		
		// no path and no scheme?
		if (linkTarget == null
				|| (linkTarget.isBlank())) {
			
			int offset = region.getOffset() + linkStatementStartIndexInRegion + linkTargetStartIndex;
			int endOffset = linkTarget != null ? offset + linkTarget.length() : offset;
			int lineNumber = getLineForOffset(document, offset);
			
			if (linkTarget == null || linkTarget.length() == 0) {
				// extend character set to be marked, since we otherwise have not a single char
				offset -= 1;
				endOffset += 1;
			}
			
			MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
					"The target file path or URL is empty.",
					lineNumber,
					offset,
					endOffset);
			return;
		}
		
		// check file target (without URI scheme)
		if (uriDto.scheme == null) {
			int offset = region.getOffset() + linkStatementStartIndexInRegion + linkTargetStartIndex;
			int lineNumber = getLineForOffset(document, offset);
			int endOffset = offset + uriDto.path.length();
			
			filePathValidator.checkFilePathAndFragment(linkTarget, uriDto.path, uriDto.fragment, document, file, lineNumber, offset, endOffset);
		}
		
		// check http(s) targets
		if (uriDto.scheme != null
				&& (uriDto.scheme.equalsIgnoreCase("http") || uriDto.scheme.equalsIgnoreCase("https"))) {
			
			int offset = region.getOffset() + linkStatementStartIndexInRegion + linkTargetStartIndex;
			int lineNumber = getLineForOffset(document, offset);
			
			checkHttpUri(linkTarget, file, null, lineNumber, offset);
		}
	}
	
}
