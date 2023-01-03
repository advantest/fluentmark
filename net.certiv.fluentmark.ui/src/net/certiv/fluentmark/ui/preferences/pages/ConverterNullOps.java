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
