/*******************************************************************************
 * Copyright (c) 2016 - 2018 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

import net.certiv.fluentmark.core.markdown.DiagramConstants;
import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.ui.editor.text.IScannerExt;
import net.certiv.fluentmark.ui.editor.text.rules.DotCodeRule;
import net.certiv.fluentmark.ui.editor.text.rules.FrontMatterRule;
import net.certiv.fluentmark.ui.editor.text.rules.HtmlCodeRule;
import net.certiv.fluentmark.ui.editor.text.rules.IndentedCodeRule;
import net.certiv.fluentmark.ui.editor.text.rules.MatchRule;
import net.certiv.fluentmark.ui.editor.text.rules.PumlFileInclusionRule;

public class MarkdownPartitionScanner extends RuleBasedPartitionScanner implements IScannerExt {

	public MarkdownPartitionScanner() {
		super();

		IToken matter = new Token(MarkdownPartitions.FRONT_MATTER);
		IToken comment = new Token(MarkdownPartitions.COMMENT);
		IToken codeblock = new Token(MarkdownPartitions.CODEBLOCK);
		IToken htmlblock = new Token(MarkdownPartitions.HTMLBLOCK);
		IToken dotblock = new Token(MarkdownPartitions.DOTBLOCK);
		IToken umlblock = new Token(MarkdownPartitions.UMLBLOCK);
		IToken mathblock = new Token(MarkdownPartitions.MATHBLOCK);
		Token plantUmlInclude = new Token(MarkdownPartitions.PLANTUML_INCLUDE);
		
		// TODO Find another way to set token color, maybe in a second parsing step
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
