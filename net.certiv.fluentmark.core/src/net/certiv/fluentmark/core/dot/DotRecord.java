/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.dot;

import org.eclipse.core.resources.IResource;

import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

import net.certiv.fluentmark.core.dot.gen.DotParser;
import net.certiv.fluentmark.core.dot.gen.DotParser.GraphContext;

public class DotRecord {

		public IResource resource;
		public int documentOffset;     // start offset of parsed text
		public int length;
		public int documentLineNumber; // start line of parsed text
		public int tabWidth;
		
		public GraphContext tree;
		public DotParser parser;
		public CommonTokenStream ts;
		public CodePointCharStream cs;
		
		public DotRecord(IResource resource, int documentOffset, int length,
				int documentLineNumber, int tabWidth) {
			this.resource = resource;
			this.documentOffset = documentOffset;
			this.length = length;
			this.documentLineNumber = documentLineNumber;
			this.tabWidth = tabWidth;
		}
		
		public void dispose() {
			this.resource = null;
			this.tree = null;
			this.parser = null;
			this.ts = null;
			this.cs = null;
		}
}
