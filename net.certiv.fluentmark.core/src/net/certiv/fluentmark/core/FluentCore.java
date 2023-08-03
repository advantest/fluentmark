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
