package net.certiv.fluentmark.ui.preferences.pages;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.preferences.BaseFieldEditorPreferencePage;
import net.certiv.fluentmark.ui.preferences.Prefs;
import net.certiv.fluentmark.ui.preferences.editors.ProgramFieldEditor;
import net.certiv.fluentmark.ui.util.SwtUtil;
import com.advantest.MarkdownCoreInfo;

public class PrefPageGraphviz extends BaseFieldEditorPreferencePage implements Prefs {
	
	private static final String DOT = "dot";
	private static final String[] DOT_MSG = { "Invalid Dot executable",
			"Full pathname of the Dot executable [dot|dot.exe]", DOT };
	
	private ProgramFieldEditor dotExecutableEditor;
	private BooleanFieldEditor plantUmlEnabledEditor;
	private BooleanFieldEditor dotEnabledEditor;
	
	private final List<FieldEditor> editors = new ArrayList<>(2);
	
	public PrefPageGraphviz() {
		super(GRID);
		setDescription("PlantUML and Graphviz / DOT environment settings");
	}
	
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(FluentUI.getDefault().getPreferenceStore());
	}
	
	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		
		dotExecutableEditor = new ProgramFieldEditor(EDITOR_DOT_PROGRAM, "DOT executable:", parent, DOT_MSG);
		
		Composite bools = SwtUtil.makeComposite(parent, 2, 1);
		plantUmlEnabledEditor = new BooleanFieldEditor(EDITOR_UMLMODE_ENABLED, "Enable PlantUML diagram rendering", bools);
		dotEnabledEditor = new BooleanFieldEditor(EDITOR_DOTMODE_ENABLED, "Enable DOT diagram rendering", bools);
		
		editors.add(dotExecutableEditor);
		editors.add(plantUmlEnabledEditor);
		editors.add(dotEnabledEditor);
		
		addField(dotExecutableEditor);
		
		SwtUtil.addSpacer(parent, 3);
		
		addField(plantUmlEnabledEditor);
		addField(dotEnabledEditor);
		
		SwtUtil.addSpacer(parent, 3);
		
		StringBuilder builder = new StringBuilder();
		builder.append("PlantUML library version: ");
		builder.append(MarkdownCoreInfo.getPlantUmlVersion());
		builder.append('\n');
		builder.append("PlantUML security profile: ");
		builder.append(MarkdownCoreInfo.getPlantUmlSecurityProfile());
		builder.append('\n');
		builder.append("PlantUML URL white list (for security profile):\n\t");
		String whiteList = MarkdownCoreInfo.getPlantUmlUrlAllowList();
		if (whiteList.isBlank()) {
			whiteList = "-";
		}
		builder.append(whiteList.replace(";", "\t\n"));
		builder.append('\n');
		builder.append('\n');
		builder.append("Graphviz version: ");
		String graphvizVersion = MarkdownCoreInfo.getGraphvizVersion();
		if (graphvizVersion.isBlank()) {
			graphvizVersion = "unknown";
		}
		builder.append(MarkdownCoreInfo.getGraphvizVersion());
		
		Label label = new Label(parent, SWT.WRAP);
		label.setText(builder.toString());
		
		checkValid();
	}

	@Override
	protected void adjustSubLayout() {
	}
	
	private void checkValid() {
		boolean valid = dotEnabledEditor.getBooleanValue() ? checkPathExe(dotExecutableEditor.getStringValue(), DOT) : true;
		this.setValid(valid);
	}
	
	private boolean checkPathExe(String pathname, String target) {
		if (pathname.trim().isEmpty()) {
			this.setMessage("Missing pathname of " + target + " executable", IMessageProvider.ERROR);
			return false;
		}
		File file = new File(pathname);
		if (target != null) {
			if (!isValidExecutable(file.getPath(), target)) {
				this.setMessage("Invalid name of " + target + " executable", IMessageProvider.ERROR);
				return false;
			}
		}
		if (!file.isFile()) {
			this.setMessage("Invalid pathname of " + target + " executable", IMessageProvider.ERROR);
			return false;
		}
		return true;
	}
	
	private boolean isValidExecutable(String path, String name) {
		if (path == null || path.isEmpty()) return false;
		File file = new File(path);
		if (!(file.getName().equals(name) || file.getName().equals(name + ".exe"))) { //$NON-NLS-1$
			return false;
		}
		return file.isFile();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (!event.getNewValue().equals(event.getOldValue())) {
			checkValid();
		}
		super.propertyChange(event);
	}

	@Override
	public void dispose() {
		for (FieldEditor editor: editors) {
			editor.setPage(null);
			editor.setPropertyChangeListener(null);
			editor.setPreferenceStore(null);
		}
		editors.clear();
		getFieldEditorParent().dispose();
	}

}
