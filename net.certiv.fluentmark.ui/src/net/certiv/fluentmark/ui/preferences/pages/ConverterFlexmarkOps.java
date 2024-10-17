package net.certiv.fluentmark.ui.preferences.pages;

import static net.certiv.fluentmark.ui.preferences.Prefs.EDITOR_FLEXMARK_MATHJAX;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.advantest.MarkdownCoreInfo;

import net.certiv.fluentmark.ui.preferences.AbstractOptionsBlock;
import net.certiv.fluentmark.ui.util.SwtUtil;

public class ConverterFlexmarkOps extends AbstractOptionsBlock {

	public ConverterFlexmarkOps(PrefPageConvert page, Composite parent, String title) {
		super(page, parent, title);
	}

	@Override
	protected void createControls(Composite comp) {
		Label label = new Label(comp, SWT.NONE);
		label.setText("Version: " + MarkdownCoreInfo.getFlexmarkVersion());
		
		SwtUtil.addSpacer(comp, 3);
		
		addField(new BooleanFieldEditor(EDITOR_FLEXMARK_MATHJAX, "Enable Mathjax rendering", comp));
	}
	
	@Override
	public boolean validateSettings() {
		return true;
	}
	
}
