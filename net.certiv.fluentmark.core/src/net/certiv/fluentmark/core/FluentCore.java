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

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class FluentCore implements BundleActivator {

	public static final String PLUGIN_ID = "net.certiv.fluentmark.core";
	
	private static FluentCore bundle;
	
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		FluentCore.context = bundleContext;
		FluentCore.bundle = this;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		FluentCore.context = null;
		FluentCore.bundle = null;
	}
	
	public static FluentCore getDefault() {
		return bundle;
	}
	
	public static void log(String msg) {
		log(IStatus.INFO, msg, null);
	}

	public static void log(int type, String msg) {
		log(type, msg, null);
	}

	public static void log(int type, String msg, Exception e) {
		if (context == null) {
			return;
		}
		ILog log = ILog.of(context.getBundle());
		log.log(new Status(type, PLUGIN_ID, IStatus.OK, msg, e));
	}

}
