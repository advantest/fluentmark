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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.markdown.MarkdownParsingTools;
import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.editor.text.MarkdownPartioningTools;
import net.certiv.fluentmark.ui.markers.ITypedRegionMarkerCalculator;
import net.certiv.fluentmark.ui.markers.MarkerCalculator;


public class MarkdownLinkValidator extends AbstractLinkValidator implements ITypedRegionMarkerCalculator {
	
	private final Pattern LINK_PATTERN;
	private final Pattern LINK_PREFIX_PATTERN;
	private final Pattern LINK_REF_DEF_PATTERN_PREFIX;
	private final Pattern LINK_REF_DEF_PATTERN;
	private final Pattern REF_LINK_PEFIX_PATTERN;
	private final Pattern REF_LINK_FULL_PATTERN;
	private final Pattern REF_LINK_SHORT_PATTERN;
	
	private FilePathValidator filePathValidator;
	
	public MarkdownLinkValidator() {
		LINK_PATTERN = Pattern.compile(MarkdownParsingTools.REGEX_LINK);
		LINK_PREFIX_PATTERN = Pattern.compile(MarkdownParsingTools.REGEX_LINK_PREFIX);
		LINK_REF_DEF_PATTERN_PREFIX = Pattern.compile(MarkdownParsingTools.REGEX_LINK_REF_DEF_PREFIX);
		LINK_REF_DEF_PATTERN = Pattern.compile(MarkdownParsingTools.REGEX_LINK_REF_DEFINITION);
		REF_LINK_PEFIX_PATTERN = Pattern.compile(MarkdownParsingTools.REGEX_REF_LINK_FULL_OR_COLLAPSED_PREFIX);
		REF_LINK_FULL_PATTERN = Pattern.compile(MarkdownParsingTools.REGEX_REF_LINK_FULL_OR_COLLAPSED);
		REF_LINK_SHORT_PATTERN = Pattern.compile(MarkdownParsingTools.REGEX_REF_LINK_SHORTCUT);
		
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

		MarkdownParsingTools.findMatches(regionContent, LINK_PATTERN)
			.forEach(match -> {
				try {
					validateLinkStatement(region, document, file, match.matchedText, match.startIndex, regionContent);
				} catch (Exception e) {
					FluentUI.log(IStatus.WARNING, String.format("Could not validate statement \"%s\".", match.matchedText), e);
				}
		});
		
		
		MarkdownParsingTools.findMatches(regionContent, LINK_REF_DEF_PATTERN)
			.forEach(match -> {
				try {
					validateLinkReferenceDefinitionStatement(region, document, file, match.matchedText, match.startIndex);
				} catch (Exception e) {
					FluentUI.log(IStatus.WARNING, String.format("Could not validate statement \"%s\".", match.matchedText), e);
				}
		});
		
		MarkdownParsingTools.findMatches(regionContent, REF_LINK_FULL_PATTERN)
			.forEach(match -> {
				try {
					Matcher prefixMatcher = REF_LINK_PEFIX_PATTERN.matcher(match.matchedText);
					boolean foundPrefix = prefixMatcher.find();
					Assert.isTrue(foundPrefix);
					int secondTextStartIndex = prefixMatcher.end();
					
					String linkLabel = match.matchedText.substring(secondTextStartIndex, match.matchedText.length() - 1);
					if (linkLabel.length() > 0) {
						// we have a full reference link like [link text][linkLabel]
					} else {
						// we have a collapsed reference link like [linkLabel][]
						linkLabel = match.matchedText.substring(1, secondTextStartIndex - 2);
					}
					validateReferenceLinkLabel(region, document, file, match.matchedText, linkLabel, match.startIndex);
				} catch (Exception e) {
					FluentUI.log(IStatus.WARNING, String.format("Could not validate statement \"%s\".", match.matchedText), e);
				}
		});
		
		MarkdownParsingTools.findMatches(regionContent, REF_LINK_SHORT_PATTERN)
			.forEach(match -> {
				try {
					String linkLabel = match.matchedText.substring(1, match.matchedText.length() - 1);
					
					validateReferenceLinkLabel(region, document, file, match.matchedText, linkLabel, match.startIndex);
				} catch (Exception e) {
					FluentUI.log(IStatus.WARNING, String.format("Could not validate statement \"%s\".", match.matchedText), e);
				}
		});
	}
	
	protected void validateLinkStatement(ITypedRegion region, IDocument document, IFile file,
			String linkStatement, int linkStatementStartIndexInRegion, String regionContent) throws CoreException {
		
		Matcher prefixMatcher = LINK_PREFIX_PATTERN.matcher(linkStatement);
		boolean foundPrefix = prefixMatcher.find();
		Assert.isTrue(foundPrefix);
		int linkTargetStartIndex = prefixMatcher.end();
		
		String linkTarget = linkStatement.substring(linkTargetStartIndex, linkStatement.length() - 1);
		
		if (linkTarget.contains("(")) {
			// In some cases the regex for links doesn't catch the last ')'. Thus we have to parse the text once again
			// Here is an example of such a case where the regex doesn't match correctly (last ')' is missing):
			// [a method in Java code](SomeClass.java#doSomething(String)
			// Changing the regex to match the ')' greedy instead of lazy would result in wrong matches
			// in lines where we have more than one link statement.
			// The greedy match would match the last ')' in the line.
			
			String linkTargetWithRest = regionContent.substring(linkStatementStartIndexInRegion + linkTargetStartIndex);
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
		
		checkLinkTarget(linkTarget, linkTargetStartIndex, region, document, file, linkStatementStartIndexInRegion);
	}
	
	protected void validateLinkReferenceDefinitionStatement(ITypedRegion region, IDocument document, IFile file,
			String linkRefDefStatement, int linkStatementStartIndexInRegion) throws CoreException {
		Matcher prefixMatcher = LINK_REF_DEF_PATTERN_PREFIX.matcher(linkRefDefStatement);
		boolean foundPrefix = prefixMatcher.find();
		Assert.isTrue(foundPrefix);
		int linkTargetStartIndex = prefixMatcher.end();
		
		String linkTarget = linkRefDefStatement.substring(linkTargetStartIndex, linkRefDefStatement.length());
		
		checkLinkTarget(linkTarget, linkTargetStartIndex, region, document, file, linkStatementStartIndexInRegion);
	}
	
	protected void validateReferenceLinkLabel(ITypedRegion region, IDocument document, IFile file,
			String referenceLinkStatement, String linkLabel, int referenceLinkStatementStartIndexInRegion) throws CoreException {
		
		if (linkLabel.isBlank()) {
			int startIndexInRefLink = referenceLinkStatement.lastIndexOf(linkLabel);
			int offset = region.getOffset() + referenceLinkStatementStartIndexInRegion + startIndexInRefLink;
			int endOffset = offset + linkLabel.length();
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
		
		String linkRefDefinitionForLabelRegex = MarkdownParsingTools.REGEX_LINK_REF_DEF_OPENING_BRACKET
				+ Pattern.quote(linkLabel)
				+ MarkdownParsingTools.REGEX_LINK_REF_DEF_PART
				+ MarkdownParsingTools.REGEX_LINK_REF_DEF_SUFFIX;
		Pattern linkRefDefinitionForLabelPattern = Pattern.compile(linkRefDefinitionForLabelRegex);
		Matcher linkReferenceDefinitionForLabelMatcher = linkRefDefinitionForLabelPattern.matcher(documentContent);
		boolean foundLinkReferenceDefinition = linkReferenceDefinitionForLabelMatcher.find();
		
		if (!foundLinkReferenceDefinition) {
			int startIndexInRefLink = referenceLinkStatement.lastIndexOf(linkLabel);
			int offset = region.getOffset() + referenceLinkStatementStartIndexInRegion + startIndexInRefLink;
			int endOffset = offset + linkLabel.length();
			int lineNumber = getLineForOffset(document, offset);
			
			MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
					"There is no link reference definition for the reference link label \"" + linkLabel + "\". Expected a link reference definition like \"[ReferenceLinkLabel]: https://plantuml.com\"",
					lineNumber,
					offset,
					endOffset);
			return;
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
