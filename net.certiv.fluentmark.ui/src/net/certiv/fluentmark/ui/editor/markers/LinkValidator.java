/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.markers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.List;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;

import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import net.certiv.fluentmark.core.convert.Partitions;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.extensionpoints.UriValidatorsManager;
import net.certiv.fluentmark.ui.util.JavaCodeMemberResolver;


public class LinkValidator implements ITypedRegionValidator {
	
	private static final String FILE_EXTENSION_MARKDOWN = "md";
	private static final String FILE_EXTENSION_JAVA = "java";
	
	// pattern for images and links, e.g. ![](../image.png) or [some text](https://www.advantext.com)
	// search non-greedy ("?" parameter) for "]" and ")" brackets, otherwise we match the last ")" in the following example
	// (link to [Topic Y](#topic-y))
	private static final String REGEX_LINK_PREFIX = "(!){0,1}\\[.*?\\]\\(";
	private static final String REGEX_LINK = REGEX_LINK_PREFIX + ".*?\\)";
	
	// pattern for link reference definitions, like [label]: https://www.plantuml.com "title"
	private static final String REGEX_LINK_REF_DEF_PREFIX = "\\[.*?\\]:( |\\t|\\n)+( |\\t)*";
	private static final String REGEX_LINK_REF_DEFINITION = REGEX_LINK_REF_DEF_PREFIX + "\\S+";
	
	// pattern for headings with anchors, e.g. #### 1.2 Section {#topic-x}
	private static final String REGEX_HEADING_ANCHOR_PREFIX = "#{1,6}( |\\t)+.*\\{#";
	private static final String REGEX_HEADING_WITH_ANCHOR = REGEX_HEADING_ANCHOR_PREFIX + "\\S+\\}";
	
	private final Pattern LINK_PATTERN;
	private final Pattern LINK_PREFIX_PATTERN;
	private final Pattern LINK_REF_DEF_PATTERN_PREFIX;
	private final Pattern LINK_REF_DEF_PATTERN;
	private final Pattern HEADING_WITH_ANCHOR_PREFIX_PATTERN;
	private final Pattern HEADING_WITH_ANCHOR_PATTERN;
	
	private JavaCodeMemberResolver javaMemberResolver;
	
	private HttpClient httpClient;
	
	
	public LinkValidator() {
		LINK_PATTERN = Pattern.compile(REGEX_LINK);
		LINK_PREFIX_PATTERN = Pattern.compile(REGEX_LINK_PREFIX);
		LINK_REF_DEF_PATTERN_PREFIX = Pattern.compile(REGEX_LINK_REF_DEF_PREFIX);
		LINK_REF_DEF_PATTERN = Pattern.compile(REGEX_LINK_REF_DEFINITION);
		HEADING_WITH_ANCHOR_PREFIX_PATTERN = Pattern.compile(REGEX_HEADING_ANCHOR_PREFIX);
		HEADING_WITH_ANCHOR_PATTERN = Pattern.compile(REGEX_HEADING_WITH_ANCHOR);
		
		javaMemberResolver = new JavaCodeMemberResolver();
	}
	

	@Override
	public boolean isValidatorFor(ITypedRegion region, IDocument document) {
		return IDocument.DEFAULT_CONTENT_TYPE.equals(region.getType())
				|| Partitions.PLANTUML_INCLUDE.equals(region.getType());
	}

	@Override
	public void validateRegion(ITypedRegion region, IDocument document, IResource resource) throws CoreException {
		String regionContent;
		try {
			regionContent = document.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			return;
		}

		Matcher linkMatcher = LINK_PATTERN.matcher(regionContent);
		boolean found = linkMatcher.find();
		
		// go through all the link statements in this region and check each of them
		while (found) {
			String currentLinkMatch = linkMatcher.group();
			int startIndex = linkMatcher.start();
			
			validateLinkStatement(region, document, resource, currentLinkMatch, startIndex, regionContent);
			
			found = linkMatcher.find();
		}
		
		Matcher linkRefDefMatcher = LINK_REF_DEF_PATTERN.matcher(regionContent);
		found = linkRefDefMatcher.find();
		
		while (found) {
			String currentLinkReferenceDefinition = linkRefDefMatcher.group();
			int startIndex = linkRefDefMatcher.start();
			
			validateLinkReferenceDefinitionStatement(region, document, resource, currentLinkReferenceDefinition, startIndex);
			
			found = linkRefDefMatcher.find();
		}
	}


	private void validateLinkStatement(ITypedRegion region, IDocument document, IResource resource,
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
		
		checkLinkTarget(linkTarget, linkTargetStartIndex, region, document, resource, linkStatementStartIndexInRegion);
	}
	
	private void validateLinkReferenceDefinitionStatement(ITypedRegion region, IDocument document, IResource resource,
			String linkRefDefStatement, int linkStatementStartIndexInRegion) throws CoreException {
		Matcher prefixMatcher = LINK_REF_DEF_PATTERN_PREFIX.matcher(linkRefDefStatement);
		boolean foundPrefix = prefixMatcher.find();
		Assert.isTrue(foundPrefix);
		int linkTargetStartIndex = prefixMatcher.end();
		
		String linkTarget = linkRefDefStatement.substring(linkTargetStartIndex, linkRefDefStatement.length());
		
		checkLinkTarget(linkTarget, linkTargetStartIndex, region, document, resource, linkStatementStartIndexInRegion);
	}
	
	private void checkLinkTarget(String linkTarget, int linkTargetStartIndex,
			ITypedRegion region, IDocument document, IResource resource,
			int linkStatementStartIndexInRegion) throws CoreException {
		
		String path = null;
		String scheme = null;
		String fragment = null;
		
		URI uri;
		try {
			uri = URI.create(linkTarget);
			
			scheme = uri.getScheme();
			fragment = uri.getFragment();
			path = uri.getPath();
		} catch (IllegalArgumentException e) {
			// we seem not to have a standard-compliant URI, try parsing it ourselves
			int indexOfColon = linkTarget.indexOf(':');
			int indexOfHashtag = linkTarget.indexOf('#');
			
			path = linkTarget;
			
			if (indexOfHashtag > -1) {
				fragment = linkTarget.substring(indexOfHashtag);
				path = linkTarget.substring(0, indexOfHashtag);
			}
			
			if (indexOfColon > -1) {
				scheme = linkTarget.substring(0, indexOfColon);
				if (indexOfColon + 1 < path.length()) {
					path = path.substring(indexOfColon + 1);
				}
			}
		}
		
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
			
			MarkerCalculator.createMarkdownMarker(resource, IMarker.SEVERITY_WARNING,
					"The target file path or URL is empty.",
					lineNumber,
					offset,
					endOffset);
			return;
		}
		
		// check file target (without URI scheme)
		if (scheme == null) {
			
			// in case of fragments we omit the path of the current file in Markdown -> we assume that path now
			if ((path == null || path.isBlank())
					&& fragment != null && !fragment.isBlank()) {
				path = resource.getLocation().toString();
			}
			
			IPath resourceRelativePath = new Path(path);
			
			int offset = region.getOffset() + linkStatementStartIndexInRegion + linkTargetStartIndex;
			int lineNumber = getLineForOffset(document, offset);
			int endOffset = offset + path.length();
			
			IMarker problemMarker = checkFileExists(resourceRelativePath, resource, lineNumber, offset, endOffset);
			
			// check fragment if file exists
			if (problemMarker == null && fragment != null) {
				
				// adapt positions to the fragment only
				int indexOfHashTag = linkTarget.indexOf('#');
				offset += indexOfHashTag;
				endOffset = offset + fragment.length() + 1;
				lineNumber = getLineForOffset(document, offset);
				
				
				if (resourceRelativePath.equals(resource.getLocation())) {
					// are we looking for sections in current Markdown file
					
					checkSectionAnchorExists(fragment, document, resource, lineNumber, offset, endOffset);
					
				} else if (FILE_EXTENSION_MARKDOWN.equalsIgnoreCase(resourceRelativePath.getFileExtension())) {
					// we're looking for sections in another Markdown file
					
					checkSectionAnchorExists(resourceRelativePath, fragment, resource, lineNumber, offset, endOffset);
					
				} else if (FILE_EXTENSION_JAVA.equalsIgnoreCase(resourceRelativePath.getFileExtension())) {
					// we're looking for members in a Java file, e.g. a method or a field
					
					checkJavaMemberExists(resourceRelativePath, fragment, resource, lineNumber, offset, endOffset);
				}
			}
		}
		
		// check http(s) targets
		if (scheme != null
				&& (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
			
			int offset = region.getOffset() + linkStatementStartIndexInRegion + linkTargetStartIndex;
			int lineNumber = getLineForOffset(document, offset);
			
			checkHttpUri(linkTarget, resource, lineNumber, offset);
		}
	}
	
	private int getLineForOffset(IDocument document, int offset) {
		try {
			return document.getLineOfOffset(offset) + 1;
		} catch (BadLocationException e) {
			return -1;
		}
	}
	
	private IMarker checkFileExists(IPath resourceRelativePath, IResource resource, int lineNumber, int offset, int endOffset) throws CoreException {
		// try resolving the file
		IPath absolutePath = toAbsolutePath(resourceRelativePath, resource);
		File targetFile = absolutePath.toFile();
		
		if (!targetFile.exists()) {
			return MarkerCalculator.createMarkdownMarker(resource, IMarker.SEVERITY_WARNING,
					String.format("The referenced file '%s' does not exist. Full path: %s", resourceRelativePath.toString(), targetFile.getAbsolutePath()),
					lineNumber,
					offset,
					endOffset);
		}
		
		if (!targetFile.isFile()) {
			return MarkerCalculator.createMarkdownMarker(resource, IMarker.SEVERITY_WARNING,
					String.format("The referenced file '%s' is actually not a file (it seems to be a directory). Full path: %s",
							resourceRelativePath.toString(), targetFile.getAbsolutePath()),
					lineNumber,
					offset,
					endOffset);
		}
		
		return null;
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
	
	private HttpClient getHttpClient() {
		if (this.httpClient == null) {
			this.httpClient = HttpClient.newBuilder()
					  .version(Version.HTTP_2)
					  .followRedirects(Redirect.NEVER)
					  .build();
		}
		return this.httpClient;
	}
	
	private IMarker checkHttpUri(String uriText, IResource resource, int lineNumber, int offset) throws CoreException {
		if (uriText == null) {
			return null;
		}
		
		// run all the URI validators from extensions, first
		List<IUriValidator> uriValidators = UriValidatorsManager.getInstance().getUriValidators();
		for (IUriValidator uriValidator : uriValidators) {
			if (uriValidator.isResponsibleFor(uriText)) {
				final List<IMarker> markers = new ArrayList<>(1);
				
				ISafeRunnable runnable = new ISafeRunnable() {
		            @Override
		            public void handleException(Throwable e) {
		            	Exception ex;
		            	if (e instanceof Exception) {
		            		ex = (Exception) e;
		            	} else {
		            		ex = new RuntimeException(e);
		            	}
		            	FluentUI.log(IStatus.WARNING, String.format("Could not run URI validator \"%s\".", uriValidator.getClass().getName()), ex);
		            }

		            @Override
		            public void run() throws Exception {
		            	if (uriValidator.isResponsibleFor(uriText)) {
		            		markers.add(uriValidator.checkUri(uriText, resource, lineNumber, offset, getHttpClient()));
		            	}
		            }
		        };
		        SafeRunner.run(runnable);
		        
		        if (markers.size() > 0) {
		        	return markers.get(0);
		        }
		        
		        // do not run other validations if we found a responsible validator from extensions
		        return null;
			}
		}
		
		
		if (!uriText.toLowerCase().startsWith("http://")
			&& !uriText.toLowerCase().startsWith("https://")) {
			return MarkerCalculator.createMarkdownMarker(resource, IMarker.SEVERITY_WARNING,
					String.format("The referenced web address '%s' seems not to be a valid HTTP web address. It has to start with https:// or http://", uriText),
					lineNumber,
					offset,
					offset + uriText.length());
		}
		
		URI uri = null;
		try {
			uri = new URI(uriText);
		} catch (URISyntaxException e) {
			return MarkerCalculator.createMarkdownMarker(resource, IMarker.SEVERITY_WARNING,
					String.format("The referenced web address '%s' seems not to be a valid HTTP web address. " + e.getMessage(), uriText),
					lineNumber,
					offset,
					offset + uriText.length());
		}
		
		
		HttpClient client = getHttpClient();
		
		// we only need HTTP HEAD, no page content, just reachability
		HttpRequest headRequest = HttpRequest.newBuilder()
			      .method("HEAD", HttpRequest.BodyPublishers.noBody())    
			      .uri(uri)
			      .timeout(Duration.ofSeconds(2))
			      .build();
		
		int statusCode = -1;
		String errorMessage = null;
		try {
			statusCode = client.send(headRequest, BodyHandlers.discarding()).statusCode(); 
		} catch (IOException | InterruptedException e) {
			errorMessage = e.getMessage();
			statusCode = -404;
		}
		
		if (statusCode >= 400) {
			return MarkerCalculator.createMarkdownMarker(resource, IMarker.SEVERITY_WARNING,
					String.format("The referenced web address '%s' is not reachable (HTTP status code %s).", uriText, statusCode),
					lineNumber,
					offset,
					offset + uriText.length());
		} else if (statusCode == -404) {
			return MarkerCalculator.createMarkdownMarker(resource, IMarker.SEVERITY_WARNING,
					String.format("The referenced web address '%s' seems not to exist. (Error message: %s)", uriText, errorMessage),
					lineNumber,
					offset,
					offset + uriText.length());
		}
		
		return null;
	}
	
	private String removeQueryParametersFromUrl(String urlText) {
		if (urlText.contains("?")) {
			String[] urlParts = urlText.split("\\?");
			if (urlParts != null && urlParts.length >= 0) {
				return urlParts[0];
			}
			
		}
		return urlText;
	}
	
	private String removeAnchorFromUrl(String urlText) {
		if (urlText.contains("#")) {
			String[] urlParts = urlText.split("#");
			if (urlParts != null && urlParts.length >= 0) {
				return urlParts[0];
			}
			
		}
		return urlText;
	}
	
	private String extractLastUrlSegment(String urlText) {
		if (urlText.contains("/")) {
			String[] urlParts = urlText.split("/");
			if (urlParts != null && urlParts.length >= 0) {
				return urlParts[urlParts.length - 1];
			}
		}
		return urlText;
	}
	
	private IMarker checkSectionAnchorExists(String sectionAnchor, IDocument currentDocument, IResource currentResource, int lineNumber, int offset, int endOffset) throws CoreException {
		String markdownFileContent = currentDocument.get();
		
		return checkSectionAnchorExists(sectionAnchor, markdownFileContent, currentResource, lineNumber, offset, endOffset);
	}
	
	private IMarker checkSectionAnchorExists(IPath targetFileWithAnchor, String sectionAnchor, IResource currentResource, int lineNumber, int offset, int endOffset) throws CoreException {
		IPath absolutePath = toAbsolutePath(targetFileWithAnchor, currentResource);
		File file = absolutePath.toFile();
        String mdFileContent = readTextFromFile(file);
        
        if (mdFileContent != null) {
        	return checkSectionAnchorExists(sectionAnchor, mdFileContent, currentResource, lineNumber, offset, endOffset);
        }
		
		return null;
	}
	
	// TODO Avoid code duplication here and in Converter#readTextFromFile(File)
	private String readTextFromFile(File file) {
		if (file != null && file.exists() && file.isFile()) {
			try {
				return Files.readString(
						Paths.get(file.getAbsolutePath()),
						StandardCharsets.UTF_8);
			} catch (IOException e) {
				FluentUI.log(IStatus.WARNING, String.format("Could not read Markdown file '%s'", file.getAbsolutePath()), e);
				return null;
			}
		}
		return null;
	}
	
	private IMarker checkSectionAnchorExists(String sectionAnchor, String markdownFileContent, IResource currentResource, int lineNumber, int offset, int endOffset) throws CoreException {
		Matcher headingMatcher = HEADING_WITH_ANCHOR_PATTERN.matcher(markdownFileContent);
		boolean found = headingMatcher.find();
		
		// go through all the headings in the document and check their anchors
		while (found) {
			String currentHeadingMatch = headingMatcher.group();
			
			Matcher prefixMatcher = HEADING_WITH_ANCHOR_PREFIX_PATTERN.matcher(currentHeadingMatch);
			boolean foundPrefix = prefixMatcher.find();
			Assert.isTrue(foundPrefix);
			int indexBeginAnchor = prefixMatcher.end();
			
			// cut off heading and "{#" and "}" parts to extract the anchor in between
			String currentAnchor = currentHeadingMatch.substring(indexBeginAnchor, currentHeadingMatch.length() - 1);
			
			if (sectionAnchor.equals(currentAnchor)) {
				// we found the target, no need to create markers
				return null;
			}
			
			found = headingMatcher.find();
		}
		
		// we didn't find any target anchor ==> create a marker
		return MarkerCalculator.createMarkdownMarker(currentResource, IMarker.SEVERITY_WARNING,
				String.format("There is no section with the given anchor '%s' in this Markdown document '%s'.", sectionAnchor, currentResource.getLocation().toString()),
				lineNumber,
				offset,
				endOffset);
	}
	
	private IMarker checkJavaMemberExists(IPath targetJavaFile, String memberReference, IResource currentResource, int lineNumber, int offset, int endOffset) throws CoreException {
		
		IPath absolutePath = toAbsolutePath(targetJavaFile, currentResource);
		File targetFile = absolutePath.toFile();
		 
		if (!targetFile.exists() || !targetFile.isFile()) {
			return null;
		}
		
		IFile[] filesFound = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(targetFile.toURI());
		if (filesFound.length != 1) {
			return null;
		}
		
		IMember member = this.javaMemberResolver.findJavaMember(filesFound[0], memberReference);
		
		if (member == null || !member.exists()) {
			// we didn't find the referenced class member ==> create a problem marker
			return MarkerCalculator.createMarkdownMarker(currentResource, IMarker.SEVERITY_WARNING,
					String.format("There is no class member (field or method) corresponding to the given anchor '%s' in the Java file '%s'.", memberReference, absolutePath.toString()),
					lineNumber,
					offset,
					endOffset);
		}
		
		return null;
	}

}
