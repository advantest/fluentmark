/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui;

import org.eclipse.core.runtime.NullProgressMonitor;

import net.certiv.fluentmark.ui.editor.ICancelable;
import net.certiv.fluentmark.ui.editor.ICanceler;

/**
 * A progress monitor accepting a <code>ICancelable</code> object to receive the cancel request.
 * 
 * @since 5.0
 */
public class ProgressMonitorAndCanceler extends NullProgressMonitor implements ICanceler {

	private ICancelable fCancelable;

	public void setCancelable(ICancelable cancelable) {
		fCancelable = cancelable;
		checkCanceled();
	}

	@Override
	public void setCanceled(boolean canceled) {
		super.setCanceled(canceled);
		checkCanceled();
	}

	private void checkCanceled() {
		if (fCancelable != null && isCanceled()) {
			fCancelable.cancel();
			fCancelable = null;
		}
	}

}
