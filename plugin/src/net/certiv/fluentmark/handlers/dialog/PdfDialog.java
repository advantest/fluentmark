package net.certiv.fluentmark.handlers.dialog;

import java.util.LinkedHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import net.certiv.fluentmark.FluentUI;
import net.certiv.fluentmark.handlers.ExportPdfHandler;
import net.certiv.fluentmark.preferences.Prefs;
import net.certiv.fluentmark.util.FileUtils;
import net.certiv.fluentmark.util.SwtUtil;

public class PdfDialog extends MessageDialog {

	private static final String Title = "PDF Generate";
	private static final String Msg = "Options";
	private static final String Default = "<default>";

	private IPreferenceStore store;
	private ExportPdfHandler handler;
	private IFile source;

	private Text txtTmpl;
	private Text txtSave;

	public PdfDialog(Shell parent, ExportPdfHandler handler, IFile source) {
		super(parent, Title, null, Msg, MessageDialog.NONE, 1, "Generate", "Cancel");
		this.store = FluentUI.getDefault().getCombinedPreferenceStore();
		this.handler = handler;
		this.source = source;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		Composite comp = SwtUtil.makeComposite(parent, 1, 3);

		// Template select ------------------------------

		SwtUtil.makeLabel(comp, "Template:", 1);

		txtTmpl = SwtUtil.makeText(comp, 1);
		txtTmpl.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				String pathname = txtTmpl.getText();
				pathname = pathname.equals(Default) ? "" : pathname;
				handler.setTemplate(pathname);
			}
		});
		txtTmpl.setText(findLast());

		Button bTmpl = SwtUtil.makeButton(comp, "Browse", 1);
		bTmpl.addSelectionListener(tmplListener);

		// Save destination select ----------------------

		SwtUtil.makeLabel(comp, "Save to:", 1);

		txtSave = SwtUtil.makeText(comp, 1);
		txtSave.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				handler.setDestination(txtSave.getText());
			}
		});
		IPath path = source.getLocation().removeFileExtension().addFileExtension("pdf");
		txtSave.setText(path.toString());

		Button bSave = SwtUtil.makeButton(comp, "Browse", 1);
		bSave.addSelectionListener(saveListener);

		// Options --------------------------------------

		// ----------------------------------------------

		return comp;
	}

	// get last template used for the source document
	// returns the pathname or the default ident
	private String findLast() {
		// key=document[pathname|name]; value=template pathname
		LinkedHashMap<String, String> map = FileUtils.getTemplateMap();
		String pathname = map.get(source.getFullPath().toString()); // by WS relative source file pathname
		if (invalid(pathname)) pathname = map.get(source.getName()); // by source filename
		return invalid(pathname) ? Default : pathname;
	}

	private boolean invalid(String pathname) {
		return pathname == null || pathname.isEmpty();
	}

	private SelectionListener tmplListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			String base = store.getString(Prefs.EDITOR_PANDOC_TEMPLATES).trim();
			base = base.endsWith("/") ? base : base + "/"; // default template base directory
			String name = ""; // internal default template name

			IPath tmplPath = new Path(findLast());
			switch (tmplPath.segmentCount()) {
				case 0:
					break;
				case 1:
					name = tmplPath.lastSegment();
					name = name.equals(Default) ? "" : name;
					break;
				default:
					base = tmplPath.removeLastSegments(1).addTrailingSeparator().toString();
					name = tmplPath.lastSegment();
			}

			FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
			dialog.setFilterNames(new String[] { "Tex Files", "All Files (*.*)" });
			dialog.setFilterExtensions(new String[] { "*.tex", "*.*" });
			dialog.setFilterPath(base);
			dialog.setFileName(name);

			String pathname = dialog.open();
			if (pathname != null) {
				if (invalid(pathname)) pathname = Default;
				IPath path = new Path(pathname); // normalize
				txtTmpl.setText(path.toString());
			}
		}
	};

	private SelectionListener saveListener = new SelectionAdapter() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			IPath path = source.getLocation().removeFileExtension().addFileExtension("pdf");
			String base = path.removeLastSegments(1).addTrailingSeparator().toString();
			String name = path.lastSegment();

			FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
			dialog.setFilterNames(new String[] { "PDF Files", "All Files (*.*)" });
			dialog.setFilterExtensions(new String[] { "*.pdf", "*.*" });
			dialog.setFilterPath(base);
			dialog.setFileName(name);

			String pathname = dialog.open();
			if (pathname != null) {
				path = new Path(pathname); // normalize
				txtSave.setText(path.toString());
			}
		}
	};
}
