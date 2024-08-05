/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.handlers;

public class ItalicHandler extends AbstractMarksHandler {

	private static final String[] ITALICS = { "*", "_" };

	@Override
	public String[] getMark() {
		return ITALICS;
	}

	@Override
	public boolean qualified(String mark) {
		if (ITALICS[0].equals(mark) || ITALICS[1].equals(mark)) return true;
		return false;
	}
}
