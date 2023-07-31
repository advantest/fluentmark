/*******************************************************************************
 * Copyright (c) 2016 - 2018 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

import java.util.ArrayList;
import java.util.List;

import net.certiv.fluentmark.core.convert.Partitions;
import net.certiv.fluentmark.core.markdown.DiagramConstants;
import net.certiv.fluentmark.ui.editor.text.IScannerExt;
import net.certiv.fluentmark.ui.editor.text.rules.DotCodeRule;
import net.certiv.fluentmark.ui.editor.text.rules.FrontMatterRule;
import net.certiv.fluentmark.ui.editor.text.rules.HtmlCodeRule;
import net.certiv.fluentmark.ui.editor.text.rules.IndentedCodeRule;
import net.certiv.fluentmark.ui.editor.text.rules.MatchRule;
import net.certiv.fluentmark.ui.editor.text.rules.PumlFileInclusionRule;

public class PartitionScanner extends RuleBasedPartitionScanner implements IScannerExt {

	public PartitionScanner() {
		super();

		IToken matter = new Token(Partitions.FRONT_MATTER);
		IToken comment = new Token(Partitions.COMMENT);
		IToken codeblock = new Token(Partitions.CODEBLOCK);
		IToken htmlblock = new Token(Partitions.HTMLBLOCK);
		IToken dotblock = new Token(Partitions.DOTBLOCK);
		IToken umlblock = new Token(Partitions.UMLBLOCK);
		IToken mathblock = new Token(Partitions.MATHBLOCK);
		IToken plantUmlInclude = new Token(Partitions.PLANTUML_INCLUDE);

		List<IRule> rules = new ArrayList<>();

		rules.add(new FrontMatterRule("---", "---", matter, '\\'));
		rules.add(new MultiLineRule("<!--", "-->", comment, '\\', false));
		rules.add(new MultiLineRule("$$", "$$", mathblock, '\\', false));
		rules.add(new MatchRule("\\$\\S", "\\S\\$\\D", mathblock, '\\', true, true));
		rules.add(new PumlFileInclusionRule(plantUmlInclude));
		rules.add(new HtmlCodeRule(htmlblock));
		rules.add(new DotCodeRule(dotblock));
		rules.add(new MultiLineRule(DiagramConstants.UML_START, DiagramConstants.UML_END, umlblock, '\\', false));
		rules.add(new MultiLineRule(DiagramConstants.DOT_START, DiagramConstants.DOT_END, umlblock, '\\', false));
		rules.add(new MultiLineRule(DiagramConstants.UML_START_SALT, DiagramConstants.UML_END_SALT, umlblock, '\\', false));
		rules.add(new MultiLineRule(DiagramConstants.UML_START_YAML, DiagramConstants.UML_END_YAML, umlblock, '\\', false));
		rules.add(new MultiLineRule(DiagramConstants.UML_START_JSON, DiagramConstants.UML_END_JSON, umlblock, '\\', false));
		rules.add(new MultiLineRule(DiagramConstants.UML_START_MINDMAP, DiagramConstants.UML_END_MINDMAP, umlblock, '\\', false));
		rules.add(new MultiLineRule(DiagramConstants.UML_START_GANTT, DiagramConstants.UML_END_GANTT, umlblock, '\\', false));
		rules.add(new MultiLineRule(DiagramConstants.UML_START_WBS, DiagramConstants.UML_END_WBS, umlblock, '\\', false));
		rules.add(new MultiLineRule("~~~", "~~~", codeblock, '\\', false));
		rules.add(new MultiLineRule("```", "```", codeblock, '\\', false));
		rules.add(new IndentedCodeRule(codeblock));

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
