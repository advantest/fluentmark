package net.certiv.fluent.dt.ui.preferences.tabs;

import static net.certiv.fluent.dt.core.preferences.Prefs.EDITOR_EXTERNAL_COMMAND;
import static net.certiv.fluent.dt.core.preferences.Prefs.EDITOR_MD_CONVERTER;
import static net.certiv.fluent.dt.core.preferences.Prefs.KEY_EXTERNAL;

import org.eclipse.swt.widgets.Composite;

import net.certiv.dsl.core.preferences.PrefsDeltaManager;
import net.certiv.dsl.ui.preferences.blocks.AbstractConfigBlock.FType;
import net.certiv.dsl.ui.preferences.tabs.AbstractTab;
import net.certiv.dsl.ui.util.SWTFactory;
import net.certiv.fluent.dt.ui.preferences.blocks.ConvertersConfigBlock;

public class ExternalTab extends AbstractTab {

	private PrefsDeltaManager delta;

	public ExternalTab(ConvertersConfigBlock block, Composite parent, PrefsDeltaManager delta, String title) {
		super(block, parent, title);
		this.delta = delta;
	}

	@Override
	protected void createControls(Composite comp) {
		Composite loc = SWTFactory.createGroupComposite(comp, 1, 3, "Program");
		block.addTextField(loc, "Command line", EDITOR_EXTERNAL_COMMAND, 0, 0, FType.STRING);
	}

	@Override
	public boolean validateSettings() {
		return KEY_EXTERNAL.equals(delta.getString(EDITOR_MD_CONVERTER));
	}
}
