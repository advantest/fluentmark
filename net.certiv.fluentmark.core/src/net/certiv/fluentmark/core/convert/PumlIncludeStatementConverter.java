/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.convert;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.certiv.fluentmark.core.markdown.Lines;

public class PumlIncludeStatementConverter {
	
	String readCaptionFrom(String pumlFileInclusionStatement) {
		// we get something like ![The image's caption](path/to/some/file.puml)
		// and want to extract the caption text
		Assert.isTrue(pumlFileInclusionStatement != null && pumlFileInclusionStatement.startsWith("!["));
		
		String caption = pumlFileInclusionStatement.substring(2);
		
		int indexOfLastCaptionCharacter = caption.indexOf(']');
		return caption.substring(0, indexOfLastCaptionCharacter);
	}
	
	public String readPumlFilePathText(String pumlFileInclusionStatement) {
		// we get something like ![any text](path/to/some/file.puml)
		// and want to extract the path to the puml file
		Pattern p = Pattern.compile("!\\[.+\\]\\("); // compare Lines.PATTERN_PLANTUML_INCLUDE
        Matcher m = p.matcher(pumlFileInclusionStatement);
        
        m.find();
        int indexOfFirstPathCharacter = m.end();
        String pumlFilePath = pumlFileInclusionStatement.substring(indexOfFirstPathCharacter);
        
        int indexOfFirstCharacterAfterPath = pumlFilePath.lastIndexOf(')');
        pumlFilePath = pumlFilePath.substring(0, indexOfFirstCharacterAfterPath);
        
        return pumlFilePath;
	}
	
	public IPath readPumlFilePath(String pumlFileInclusionStatement) {
        return new Path(readPumlFilePathText(pumlFileInclusionStatement));
	}
	
	String getRemainderOfThePumlIncludeLine(String pumlIncludeLine) {
		if (pumlIncludeLine == null) {
			return null;
		}
		
		String remainingLine = "";
        int endOfPattern = findEndOfPumlFileInclusionStatement(pumlIncludeLine);
        if (endOfPattern < pumlIncludeLine.length()) {
        	remainingLine = pumlIncludeLine.substring(endOfPattern);
        }
        return remainingLine;
	}
	
	private int findEndOfPumlFileInclusionStatement(String text) {
		Pattern p = Pattern.compile(Lines.PATTERN_PLANTUML_INCLUDE);
        Matcher m = p.matcher(text);
        m.find();
        return m.end();
	}
	
	public IPath toAbsolutePumlFilePath(IPath currentMarkdownFilePath, IPath relativePumlFilePath) {
		// remove file name, so that we get the current folder's path, then append the relative path of the target puml file
        IPath absolutePumlFilePath = currentMarkdownFilePath.removeLastSegments(1).append(relativePumlFilePath);
        return absolutePumlFilePath;
	}

}
