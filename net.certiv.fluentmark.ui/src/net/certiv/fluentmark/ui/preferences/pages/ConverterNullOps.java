/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.preferences.pages;

import org.eclipse.swt.widgets.Composite;

import net.certiv.fluentmark.ui.preferences.AbstractOptionsBlock;

public class ConverterNullOps extends AbstractOptionsBlock {

	public ConverterNullOps(PrefPageConvert page, Composite parent, String title) {
		super(page, parent, title);
	}

	@Override
	protected void createControls(Composite comp) {}

	@Override
	public boolean validateSettings() {
		return true;
	}
}
