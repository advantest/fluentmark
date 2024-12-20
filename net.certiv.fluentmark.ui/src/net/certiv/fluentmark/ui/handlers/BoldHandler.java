/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.handlers;

public class BoldHandler extends AbstractMarksHandler {

	private static final String[] BOLDS = { "**", "__" };

	@Override
	public String[] getMark() {
		return BOLDS;
	}

	@Override
	public boolean qualified(String mark) {
		if (BOLDS[0].equals(mark) || BOLDS[1].equals(mark)) return true;
		return false;
	}
}
