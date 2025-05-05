/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.spell;

import org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.Log;
import net.certiv.spellchecker.TextSpellingEngine;
import net.certiv.spellchecker.engine.ISpellChecker;

public class SpellingEngine extends TextSpellingEngine {

	public static final String ID = "net.certiv.fluentmark.spell.SpellingEngine";

	public SpellingEngine() {
		super(FluentUI.getDefault().getPreferenceStore());
	}

	/**
	 * Check the given document regions only for those regions that are in the default document
	 * partition.
	 */
	@Override
	protected void check(IDocument doc, IRegion[] regions, ISpellChecker checker, ISpellingProblemCollector collector,
			IProgressMonitor monitor) {

		Filter filter = new Filter();
		try {
			regions = filter.exec(doc, regions);
		} catch (BadLocationException | BadPartitioningException e) {
			Log.log(IStatus.WARNING, IStatus.ERROR, "Fluent Mark Spelling Engine: Failed to filter doc partitions.", e);
		}

		super.check(doc, regions, checker, collector, monitor);
	}
}
