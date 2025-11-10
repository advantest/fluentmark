/*******************************************************************************
 * Copyright (c) 2016 - 2018 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.markdown.partitions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

import net.certiv.fluentmark.core.dot.DotConstants;
import net.certiv.fluentmark.core.markdown.scanner.IScannerExt;
import net.certiv.fluentmark.core.markdown.scanner.rules.CodeSpanRule;
import net.certiv.fluentmark.core.markdown.scanner.rules.DotCodeRule;
import net.certiv.fluentmark.core.markdown.scanner.rules.FrontMatterRule;
import net.certiv.fluentmark.core.markdown.scanner.rules.HtmlCodeRule;
import net.certiv.fluentmark.core.markdown.scanner.rules.IndentedCodeRule;
import net.certiv.fluentmark.core.markdown.scanner.rules.MatchRule;
import net.certiv.fluentmark.core.markdown.scanner.rules.PumlFileInclusionRule;
import net.certiv.fluentmark.core.plantuml.parsing.PlantUmlConstants;

public class MarkdownPartitionScanner extends RuleBasedPartitionScanner implements IScannerExt {

	public MarkdownPartitionScanner() {
		this(4);
	}
	
	public MarkdownPartitionScanner(int tabWidth) {
		super();

		IToken matter = new Token(MarkdownPartitioner.FRONT_MATTER);
		IToken comment = new Token(MarkdownPartitioner.COMMENT);
		IToken codeblock = new Token(MarkdownPartitioner.CODEBLOCK);
		IToken codespan = new Token(MarkdownPartitioner.CODESPAN);
		IToken htmlblock = new Token(MarkdownPartitioner.HTMLBLOCK);
		IToken dotblock = new Token(MarkdownPartitioner.DOTBLOCK);
		IToken umlblock = new Token(MarkdownPartitioner.UMLBLOCK);
		IToken mathblock = new Token(MarkdownPartitioner.MATHBLOCK);
		
		// TODO Get rid of the PlantUML inclusion statement type region in the partition scanner
		Token plantUmlInclude = new Token(MarkdownPartitioner.PLANTUML_INCLUDE);
		
		// TODO Find another way to set token color.
		// That should work as soon as we get rid of PumlFileInclusionRule,
		// then the ScannerMarkup and its LinkRule will be responsible for the syntax highlighting.
		// The following code doesn't work, since FastPartinioner expects String as token data,
		// but link color and style are set via TextAttribute as token data.
		
//		IPreferenceStore store = FluentUI.getDefault().getPreferenceStore();
//		IColorManager colorManager = FluentUI.getDefault().getColorMgr();
//		PreferenceConverter.getColor(store, Prefs.EDITOR_LINK_COLOR);
//		Color color = colorManager.getColor(Prefs.EDITOR_LINK_COLOR);
//		// TODO also use the same style for PlantUML include statements as for links
//		// see net.certiv.fluentmark.ui.editor.text.AbstractBufferedRuleBasedScanner#createTextAttribute(...)
//		TextAttribute attribute = new TextAttribute(color, null, SWT.NORMAL);
//		plantUmlInclude.setData(attribute);

		List<IPredicateRule> rules = new ArrayList<>();

		rules.add(new FrontMatterRule("---", "---", matter, '\\'));
		rules.add(new MultiLineRule("<!--", "-->", comment, '\\', false));
		rules.add(new MultiLineRule("$$", "$$", mathblock, '\\', false));
		rules.add(new MatchRule("\\$\\S", "\\S\\$\\D", mathblock, '\\', true, true));
		
		rules.add(new PumlFileInclusionRule(plantUmlInclude));
		
		rules.add(new HtmlCodeRule(htmlblock));
		rules.add(new DotCodeRule(dotblock));
		rules.add(new MultiLineRule(DotConstants.DOT_START, DotConstants.DOT_END, umlblock, '\\', false));
		rules.add(new MultiLineRule(PlantUmlConstants.UML_START, PlantUmlConstants.UML_END, umlblock, '\\', false));
		rules.add(new MultiLineRule(PlantUmlConstants.UML_START_SALT, PlantUmlConstants.UML_END_SALT, umlblock, '\\', false));
		rules.add(new MultiLineRule(PlantUmlConstants.UML_START_YAML, PlantUmlConstants.UML_END_YAML, umlblock, '\\', false));
		rules.add(new MultiLineRule(PlantUmlConstants.UML_START_JSON, PlantUmlConstants.UML_END_JSON, umlblock, '\\', false));
		rules.add(new MultiLineRule(PlantUmlConstants.UML_START_MINDMAP, PlantUmlConstants.UML_END_MINDMAP, umlblock, '\\', false));
		rules.add(new MultiLineRule(PlantUmlConstants.UML_START_GANTT, PlantUmlConstants.UML_END_GANTT, umlblock, '\\', false));
		rules.add(new MultiLineRule(PlantUmlConstants.UML_START_WBS, PlantUmlConstants.UML_END_WBS, umlblock, '\\', false));
		rules.add(new MultiLineRule("~~~", "~~~", codeblock, '\\', false));
		rules.add(new MultiLineRule("```", "```", codeblock, '\\', false));
		rules.add(new CodeSpanRule(codespan));
		rules.add(new IndentedCodeRule(codeblock, tabWidth));

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
