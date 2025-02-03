/*******************************************************************************
 * Copyright (c) 2017, 2018 Certiv Analytics. All rights reserved.
 * Use of this file is governed by the Eclipse Public License v1.0
 * that can be found in the LICENSE.txt file in the project root,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package net.certiv.fluentmark.core.dot;

import org.eclipse.core.resources.IResource;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import net.certiv.fluentmark.core.dot.gen.DotLexer;
import net.certiv.fluentmark.core.dot.gen.DotParser;

public class DotSourceParser {

	private final DotErrorStrategy errStrategy = new DotErrorStrategy();
	private final DotErrorListener errListener = new DotErrorListener();


	public DotRecord eval(String lineText, int lineNumberInDocument, int lineOffsetInDocument,
			int tabWidth, IResource documentResource,  DotProblemCollector collector) {
		
		DotRecord record = new DotRecord(documentResource, lineOffsetInDocument,
				lineText.length(), lineNumberInDocument, tabWidth);
		errListener.setup(record, collector); // FIXME
		
		record.cs = CharStreams.fromString(lineText);
		DotLexer lexer = new DotLexer(record.cs);
		lexer.addErrorListener(errListener);
		record.ts = new CommonTokenStream(lexer);
		record.parser = new DotParser(record.ts);
		record.parser.setErrorHandler(errStrategy);
		record.parser.removeErrorListeners();
		record.parser.addErrorListener(errListener);
		record.tree = record.parser.graph();
		
		VerifyVisitor.INST.check(record, collector); // FIXME
		
		return record;
	}
	
}
