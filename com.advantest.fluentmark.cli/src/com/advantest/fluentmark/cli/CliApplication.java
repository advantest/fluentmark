package com.advantest.fluentmark.cli;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class CliApplication implements IApplication {
	
	public static final String PLUGIN_ID = "com.advantest.fluentmark.cli";
	
	private static IApplicationContext context;
	private static CliApplication application;
	
	public static CliApplication get() {
		return application;
	}

	@Override
	public Object start(IApplicationContext context) throws Exception {
		CliApplication.context = context;
		CliApplication.application = this;
		
		log("CLI app started.");
		
		String[] cliArguments = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		
		if (cliArguments != null && cliArguments.length > 0) {
			log("Received CLI arguments: " + String.join(", ", cliArguments));
		}
		
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		CliApplication.context = null;
		CliApplication.application = null;
		
		log("CLI app stopped.");
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
		ILog log = ILog.of(context.getBrandingBundle());
		log.log(new Status(type, PLUGIN_ID, IStatus.OK, msg, e));
	}

}
