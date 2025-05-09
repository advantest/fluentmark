/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * Abstract preference page which is used to wrap a IPreferenceConfigurationBlock.
 *
 * @since 3.0
 */
public abstract class AbstractConfigurationBlockPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {

	private IPreferenceConfigurationBlock fConfigurationBlock;
	private OverlayPreferenceStore fOverlayStore;

	/**
	 * Creates a new preference page.
	 */
	public AbstractConfigurationBlockPreferencePage() {
		setDescription();
		setPreferenceStore();
		fOverlayStore = new OverlayPreferenceStore(getPreferenceStore(), new OverlayPreferenceStore.OverlayKey[] {});
		fConfigurationBlock = createConfigurationBlock(fOverlayStore);
	}

	protected abstract IPreferenceConfigurationBlock createConfigurationBlock(
			OverlayPreferenceStore overlayPreferenceStore);

	protected abstract String getHelpId();

	protected abstract void setDescription();

	protected abstract void setPreferenceStore();

	@Override
	public void init(IWorkbench workbench) {}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpId());
	}

	@Override
	protected Control createContents(Composite parent) {
		fOverlayStore.load();
		fOverlayStore.start();
		Control content = fConfigurationBlock.createControl(parent);
		initialize();
		Dialog.applyDialogFont(content);
		return content;
	}

	private void initialize() {
		fConfigurationBlock.initialize();
	}

	@Override
	public boolean performOk() {
		if (!fConfigurationBlock.canPerformOk()) return false;
		fConfigurationBlock.performOk();
		fOverlayStore.propagate(); // using writable chained store
		// FluentUI.flushInstanceScope();

		return true;
	}

	@Override
	public void performDefaults() {
		fOverlayStore.loadDefaults();
		fConfigurationBlock.performDefaults();
		super.performDefaults();
	}

	@Override
	public void dispose() {
		fConfigurationBlock.dispose();
		if (fOverlayStore != null) {
			fOverlayStore.stop();
			fOverlayStore = null;
		}
		super.dispose();
	}

	@Override
	public void applyData(Object data) {
		fConfigurationBlock.applyData(data);
	}
}
