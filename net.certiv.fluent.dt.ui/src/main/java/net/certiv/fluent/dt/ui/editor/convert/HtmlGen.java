package net.certiv.fluent.dt.ui.editor.convert;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.osgi.framework.Bundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.IPathEditorInput;

import net.certiv.dsl.core.log.Log;
import net.certiv.dsl.core.util.Chars;
import net.certiv.dsl.core.util.FileUtils;
import net.certiv.dsl.core.util.Strings;
import net.certiv.fluent.dt.core.FluentCore;
import net.certiv.fluent.dt.core.preferences.Prefs;
import net.certiv.fluent.dt.core.preferences.ResourceDef;
import net.certiv.fluent.dt.ui.FluentUI;
import net.certiv.fluent.dt.ui.editor.FluentEditor;
import net.certiv.fluent.dt.ui.editor.Partitions;

/**
 * Generate Html files for:
 * <ul>
 * <li>Preview
 * <ul>
 * <li>Base html to be loaded to the browser
 * <li>Body to be updated to the base
 * </ul>
 * <li>Export
 * <ul>
 * <li>Full, self contanied Html document
 * <li>Minimal Html document
 * </ul>
 * </ul>
 */
public class HtmlGen implements ResourceDef {

	private FluentEditor editor;
	private Converter converter;
	private String pluginId;

	public HtmlGen(FluentEditor editor, Converter converter) {
		this.editor = editor;
		this.converter = converter;
		pluginId = FluentUI.getDefault().getPluginId();
	}

	/**
	 * Gets the current document content with a header as determined by item.
	 *
	 * @param item defines the intended use of the HTML: for export, for the
	 *            embedded view, or minimal.
	 */
	public String getHtml(Kind kind) {
		IPathEditorInput input = (IPathEditorInput) editor.getEditorInput();
		if (input == null) return "";

		IPath pathname = input.getPath();
		String basepath = pathname.removeLastSegments(1).addTrailingSeparator().toString();
		return build(kind, convert(basepath), basepath, pathname);
	}

	private String build(Kind kind, String content, String base, IPath pathname) {
		StringBuilder sb = new StringBuilder();
		switch (kind) {
			case EXPORT:
				sb.append("<html><head>" + Strings.EOL);
				sb.append(FileUtils.fromBundle(pluginId, "resources/html/meta.html") + Strings.EOL);
				sb.append(FileUtils.fromBundle(pluginId, "resources/html/highlight.html") + Strings.EOL);
				if (editor.useMathJax()) {
					sb.append(FileUtils.fromBundle(pluginId, "resources/html/mathjax.html") + Strings.EOL);
				}
				sb.append("<style media=\"screen\" type=\"text/css\">" + Strings.EOL);
				sb.append(getStyle(pathname) + Strings.EOL);
				sb.append("</style>" + Strings.EOL);
				sb.append("</head><body>" + Strings.EOL);
				sb.append(content + Strings.EOL);
				sb.append("</body></html>");
				break;

			case MIN:
				sb.append("<html><head>" + Strings.EOL);
				sb.append("</head><body>" + Strings.EOL);
				sb.append(content + Strings.EOL);
				sb.append("</body></html>");
				break;

			case VIEW:
				String preview = FileUtils.fromBundle(pluginId, "resources/html/preview.html");
				preview = preview.replaceFirst("%path%", base);
				sb.append(preview.replaceFirst("%styles%", getStyle(pathname)));
				break;

			case UPDATE:
				sb.append(content + Strings.EOL);
				break;
		}

		return sb.toString();
	}

	private String convert(String basepath) {
		IDocument doc = editor.getDocument();
		int beg = 0;
		int len = doc.getLength();

		ITypedRegion[] regions;
		try {
			regions = TextUtilities.computePartitioning(doc, Partitions.PARTITIONING, beg, len, false);
		} catch (BadLocationException e) {
			Log.error(this, "Failed to compute partitions." + beg);
			return "";
		}

		return converter.convert(basepath, doc, regions);
	}

	// path is the searchable base for the style to use; returns the content
	private String getStyle(IPath path) {
		try {
			URL url = findStyle(path);
			return FileUtils.readFromStream(url.openStream());
		} catch (Exception e) {
			Log.error(this, "Failed reading stylesheet", e);
		}
		return "";
	}

	private URL findStyle(IPath path) throws Exception {
		// 1) look for a file having the same name as the input file, beginning in the
		// current directory, parent directories, and the current project directory.
		IPath style = path.removeFileExtension().addFileExtension(CSS);
		URL pathUrl = find(style);
		if (pathUrl != null) return pathUrl;

		// 2) look for a file with the name 'markdown.css' in the same set of
		// directories
		style = path.removeLastSegments(1).append(MARKDOWN_CSS);
		pathUrl = find(style);
		if (pathUrl != null) return pathUrl;

		// 3) read the file identified by the pref key 'EDITOR_CSS_EXTERNAL' from the
		// filesystem
		IPreferenceStore store = FluentCore.getDefault().getPrefsManager();
		String customCss = store.getString(Prefs.EDITOR_CSS_EXTERNAL_DIR);
		if (!customCss.isEmpty()) {
			File file = new File(customCss);
			if (file.isFile() && file.getName().endsWith(Chars.DOT + CSS)) {
				return toURL(file);
			}
		}

		// 4) read the file identified by the pref key 'EDITOR_CSS_BUILTIN' from the
		// bundle
		String builtinCss = store.getString(Prefs.EDITOR_CSS_INTERNAL_DIR);
		if (!builtinCss.isEmpty()) {
			try {
				URI uri = new URI(builtinCss.replace(".css", ".min.css"));
				URL url = FileLocator.toFileURL(uri.toURL());
				File file = URIUtil.toFile(URIUtil.toURI(url));
				if (file.isFile()) return url;
			} catch (URISyntaxException e) {
				MessageDialog.openInformation(null, "Default CSS from bundle", builtinCss);
			}
		}

		// 5) read 'markdown.css' from the bundle
		Bundle bundle = Platform.getBundle(FluentUI.PLUGIN_ID);
		URL url = FileLocator.find(bundle, new Path(CSS + MARKDOWN_CSS), null);
		url = FileLocator.toFileURL(url);
		return url;
	}

	private URL find(IPath style) {
		String name = style.lastSegment();
		IPath base = style.removeLastSegments(1);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IContainer dir = root.getContainerForLocation(base);

		while (dir != null && dir.getType() != IResource.ROOT) {
			IResource member = dir.findMember(name);
			if (member != null) {
				File file = root.getLocation().append(member.getFullPath()).toFile();
				return toURL(file);
			}
			dir = dir.getParent();
		}
		return null;
	}

	private URL toURL(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {}
		return null;
	}
}
