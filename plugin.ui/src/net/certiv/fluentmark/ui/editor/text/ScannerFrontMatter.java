/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.editor.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.WhitespaceRule;

import net.certiv.dsl.core.preferences.IDslPrefsManager;
import net.certiv.dsl.ui.editor.scanners.AbstractBufferedRuleBasedScanner;
import net.certiv.fluentmark.core.preferences.Prefs;
import net.certiv.fluentmark.ui.editor.text.rules.FrontMatterRule;
import net.certiv.fluentmark.ui.editor.text.rules.WhitespaceDetector;

public class ScannerFrontMatter extends AbstractBufferedRuleBasedScanner {

	private String[] tokenProperties;

	public ScannerFrontMatter(IDslPrefsManager store) {
		super(store);
		initialize();
	}

	@Override
	protected String[] getTokenProperties() {
		if (tokenProperties == null) {
			tokenProperties = new String[] { Prefs.EDITOR_FRONTMATTER_COLOR };
		}
		return tokenProperties;
	}

	@Override
	protected List<IRule> createRules() {
		IToken matter = getToken(Prefs.EDITOR_FRONTMATTER_COLOR);

		List<IRule> rules = new ArrayList<>();
		rules.add(new FrontMatterRule("---", "---", matter, '\\'));
		rules.add(new WhitespaceRule(new WhitespaceDetector()));
		return rules;
	}
}
