/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2026 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class UrlUtilsTest {
	
	@ParameterizedTest
	@CsvSource(delimiter = ';', value = {
			"'';''",
			"someName478623;someName478623",
			"method();method\\(\\)",
			"foo(int,boolean,String);foo\\(int,boolean,String\\)",
			"bar(Charachter[]);bar\\(Charachter\\[\\]\\)",
			"doSomething(int,boolean,Character[],List<Map<K,V>>);doSomething\\(int,boolean,Character\\[\\],List\\<Map\\<K,V\\>\\>\\)",
			"../../path/to/ImportantClassOrInterface.java#doSomething(int,boolean,Character[],List<Map<K,V>>);../../path/to/ImportantClassOrInterface.java#doSomething\\(int,boolean,Character\\[\\],List\\<Map\\<K,V\\>\\>\\)"
	})
	public void test(String unescapedUrl, String escapedUrl) {
		String actualEscapedUrl = UrlUtils.escapeBracketsInMethodReference(unescapedUrl);
		String actualUnescapedUrl = UrlUtils.unescapeBracketsInMethodReference(escapedUrl);
		String actualEscapendThanUnescapedUrl = UrlUtils.unescapeBracketsInMethodReference(actualEscapedUrl);
		
		assertEquals(escapedUrl, actualEscapedUrl);
		assertEquals(unescapedUrl, actualUnescapedUrl);
		assertEquals(unescapedUrl, actualEscapendThanUnescapedUrl);
	}

}
