package net.certiv.fluentmark.ui.preferences.pages;

import static net.certiv.fluentmark.ui.preferences.Prefs.EDITOR_FLEXMARK_MATHJAX;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Composite;

import net.certiv.fluentmark.ui.preferences.AbstractOptionsBlock;

public class ConverterFlexmarkOps extends AbstractOptionsBlock {

	public ConverterFlexmarkOps(PrefPageConvert page, Composite parent, String title) {
		super(page, parent, title);
	}

	@Override
	protected void createControls(Composite comp) {
		addField(new BooleanFieldEditor(EDITOR_FLEXMARK_MATHJAX, "Enable Mathjax rendering", comp));
	}
	
	@Override
	public boolean validateSettings() {
		return true;
	}
	
}
