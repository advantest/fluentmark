/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class FluentCore implements BundleActivator {

	public static final String PLUGIN_ID = "net.certiv.fluentmark.core";
	
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		FluentCore.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		FluentCore.context = null;
	}

}
