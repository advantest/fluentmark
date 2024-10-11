/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.core.util.FluentPartitioningTools;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.editor.text.MarkdownPartioningTools;
import net.certiv.fluentmark.ui.markers.ITypedRegionMarkerCalculator;
import net.certiv.fluentmark.ui.markers.MarkerCalculator;


public class MarkdownLinkValidator extends AbstractLinkValidator implements ITypedRegionMarkerCalculator {
	
	// pattern for images and links, e.g. ![](../image.png) or [some text](https://www.advantext.com)
	// search non-greedy ("?" parameter) for "]" and ")" brackets, otherwise we match the last ")" in the following example
	// (link to [Topic Y](#topic-y))
	private static final String REGEX_LINK_PREFIX = "(!){0,1}\\[.*?\\]\\(";
	private static final String REGEX_LINK = REGEX_LINK_PREFIX + ".*?\\)";
	
	// pattern for link reference definitions, like [label]: https://www.plantuml.com "title",
	// but excludes footnote definitions like [^label]: Some text
	private static final String REGEX_LINK_REF_DEF_PREFIX = "\\[[^^\\n]+?\\]:( |\\t|\\n)+( |\\t)*";
	private static final String REGEX_LINK_REF_DEFINITION = REGEX_LINK_REF_DEF_PREFIX + "\\S+";
	
	// patterns for reference links like the following three variants specified in CommonMark: https://spec.commonmark.org/0.31.2/#reference-link
	// * full reference link:      [Markdown specification][CommonMark]
	// * collapsed reference link: [CommonMark][]
	// * shortcut reference link:  [CommonMark]
	private static final String REGEX_REF_LINK_FULL_OR_COLLAPSED_PREFIX = "\\[[^\\]]*?\\]\\[";
	private static final String REGEX_REF_LINK_FULL_OR_COLLAPSED = REGEX_REF_LINK_FULL_OR_COLLAPSED_PREFIX + "[^\\]]*?\\]";
	private static final String REGEX_REF_LINK_SHORTCUT = "(?<!\\]|\\\\)(\\[[^\\]]*?\\])(?!(\\[|\\(|:))";
	
	private final Pattern LINK_PATTERN;
	private final Pattern LINK_PREFIX_PATTERN;
	private final Pattern LINK_REF_DEF_PATTERN_PREFIX;
	private final Pattern LINK_REF_DEF_PATTERN;
	private final Pattern REF_LINK_PEFIX_PATTERN;
	private final Pattern REF_LINK_FULL_PATTERN;
	private final Pattern REF_LINK_SHORT_PATTERN;
	
	private FilePathValidator filePathValidator;
	
	public MarkdownLinkValidator() {
		LINK_PATTERN = Pattern.compile(REGEX_LINK);
		LINK_PREFIX_PATTERN = Pattern.compile(REGEX_LINK_PREFIX);
		LINK_REF_DEF_PATTERN_PREFIX = Pattern.compile(REGEX_LINK_REF_DEF_PREFIX);
		LINK_REF_DEF_PATTERN = Pattern.compile(REGEX_LINK_REF_DEFINITION);
		REF_LINK_PEFIX_PATTERN = Pattern.compile(REGEX_REF_LINK_FULL_OR_COLLAPSED_PREFIX);
		REF_LINK_FULL_PATTERN = Pattern.compile(REGEX_REF_LINK_FULL_OR_COLLAPSED);
		REF_LINK_SHORT_PATTERN = Pattern.compile(REGEX_REF_LINK_SHORTCUT);
		
		filePathValidator = new FilePathValidator();
	}
	
	
	@Override
	public void setupDocumentPartitioner(IDocument document, IFile file) {
		if (document == null || file == null) {
			throw new IllegalArgumentException();
		}
		
		IDocumentPartitioner partitioner = FluentPartitioningTools.getDocumentPartitioner(document, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
		
		if (partitioner == null) {
			partitioner = MarkdownPartioningTools.getTools().createDocumentPartitioner();
			FluentPartitioningTools.setupDocumentPartitioner(document, partitioner, MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
		}
	}
	
	@Override
	public ITypedRegion[] computePartitioning(IDocument document, IFile file) throws BadLocationException {
		return MarkdownPartitions.computePartitions(document);
	}
	
	@Override
	public boolean isValidatorFor(IFile file) {
		if (!FileUtils.FILE_EXTENSION_MARKDOWN.equalsIgnoreCase(file.getFileExtension())) {
			return false;
		}
		return true;
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

		findMatches(regionContent, LINK_PATTERN)
			.forEach(match -> {
				try {
					validateLinkStatement(region, document, file, match.matchedText, match.startIndex, regionContent);
				} catch (Exception e) {
					FluentUI.log(IStatus.WARNING, String.format("Could not validate statement \"%s\".", match.matchedText), e);
				}
		});
		
		
		findMatches(regionContent, LINK_REF_DEF_PATTERN)
			.forEach(match -> {
				try {
					validateLinkReferenceDefinitionStatement(region, document, file, match.matchedText, match.startIndex);
				} catch (Exception e) {
					FluentUI.log(IStatus.WARNING, String.format("Could not validate statement \"%s\".", match.matchedText), e);
				}
		});
		
		findMatches(regionContent, REF_LINK_FULL_PATTERN)
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
		
		findMatches(regionContent, REF_LINK_SHORT_PATTERN)
			.forEach(match -> {
				try {
					String linkLabel = match.matchedText.substring(1, match.matchedText.length() - 1);
					
					validateReferenceLinkLabel(region, document, file, match.matchedText, linkLabel, match.startIndex);
				} catch (Exception e) {
					FluentUI.log(IStatus.WARNING, String.format("Could not validate statement \"%s\".", match.matchedText), e);
				}
		});
	}
	
	protected Stream<Match> findMatches(String textToCheck, Pattern patternToFind) {
		List<Match> matches = new ArrayList<>();
		
		Matcher textMatcher = patternToFind.matcher(textToCheck);
		boolean found = textMatcher.find();
		
		while (found) {
			String currentTextMatch = textMatcher.group();
			int startIndex = textMatcher.start();
			
			matches.add(new Match(currentTextMatch, startIndex));
			
			found = textMatcher.find();
		}
		
		return matches.stream();
	}
	
	private class Match {
		public final String matchedText;
		public final int startIndex;
		
		public Match(String matchedText, int startIndex) {
			this.matchedText = matchedText;
			this.startIndex = startIndex;
		}
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
		
		String documentContent = document.get();
		
		// see REGEX_LINK_REF_DEFINITION
		String linkRefDefinitionForLabelRegex = "\\[" + Pattern.quote(linkLabel) + "\\]:( |\\t|\\n)+( |\\t)*\\S+";
		Pattern linkRefDefinitionForLabelPattern = Pattern.compile(linkRefDefinitionForLabelRegex);
		Matcher linkReferenceDefinitionForLabelMatcher = linkRefDefinitionForLabelPattern.matcher(documentContent);
		boolean foundLinkReferenceDefinition = linkReferenceDefinitionForLabelMatcher.find();
		
		if (!foundLinkReferenceDefinition) {
			int startIndexInRefLink = referenceLinkStatement.lastIndexOf(linkLabel);
			int offset = region.getOffset() + referenceLinkStatementStartIndexInRegion + startIndexInRefLink;
			int endOffset = offset + linkLabel.length();
			int lineNumber = getLineForOffset(document, offset);
			
			MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_ERROR,
					"There is no link reference definition for the reference link label \"" + linkLabel + "\". Expected a link reference definition like \"[ReferenceLinkLabel]: https://plantuml.com\")",
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
			
			MarkerCalculator.createDocumentationProblemMarker(file, IMarker.SEVERITY_WARNING,
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
