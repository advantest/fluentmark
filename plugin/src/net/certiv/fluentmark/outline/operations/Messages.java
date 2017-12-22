/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.outline.operations;

import org.eclipse.osgi.util.NLS;

class Messages extends NLS {

	private static final String BUNDLE_NAME = "net.certiv.fluentmark.outline.operations"; //$NON-NLS-1$

	public static String MoveSectionsCommand_invalidTargetLocation_self;

	private Messages() {}

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
