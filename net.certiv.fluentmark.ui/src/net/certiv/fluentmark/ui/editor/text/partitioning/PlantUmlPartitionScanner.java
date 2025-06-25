/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor.text.partitioning;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

import net.certiv.fluentmark.ui.editor.text.IScannerExt;

public class PlantUmlPartitionScanner extends RuleBasedPartitionScanner implements IScannerExt {

	public PlantUmlPartitionScanner() {
		super();
		
		IToken commentToken = new Token(PlantUmlPartitions.COMMENT);
		
		List<IPredicateRule> rules = new ArrayList<>();
		rules.add(new MultiLineRule("/'", "'/", commentToken, '\\', true));
		rules.add(new SingleLineRule("'", null, commentToken, '\\', true));
		
		IPredicateRule[] rule = new IPredicateRule[rules.size()];
		setPredicateRules(rules.toArray(rule));
	}

	@Override
	public IDocument getDocument() {
		return fDocument;
	}

	@Override
	public int getOffset() {
		return fOffset;
	}

	@Override
	public int getRangeEnd() {
		return fRangeEnd;
	}
	
}
