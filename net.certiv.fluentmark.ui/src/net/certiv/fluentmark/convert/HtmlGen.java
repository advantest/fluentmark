package net.certiv.fluentmark.convert;

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
import org.osgi.framework.Bundle;

import java.util.List;
import java.util.Map;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.io.File;
import java.io.IOException;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.core.util.Strings;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.Log;

/**
 * Generate Html files for:
 * <ul>
 *   <li>Preview
 *   <ul>
 *     <li>Base html to be loaded to the browser
 *     <li>Body to be updated to the base
 *   </ul>
 *   <li>Export
 *   <ul>
 *     <li>Full, self contanied Html document
 *     <li>Minimal Html document
 *   </ul>
 * </ul>
 */
public class HtmlGen {

	private Converter converter;
	private IConfigurationProvider configurationProvider;

	public HtmlGen(Converter converter, IConfigurationProvider configProvider) {
		this.converter = converter;
		this.configurationProvider = configProvider;
	}
	
	/**
	 * Gets the current document content with a header as determined by kind.
	 *
	 * @param kind defines the intended use of the HTML: for export, for the embedded view, or minimal.
	 */
	public String buildHtml(IPath filePath, String basepath, List<String> regionTexts, Map<Integer,String> regionTypes, Kind kind) {
		String text = converter.convert(filePath, basepath, regionTexts, regionTypes, kind);
		try {
			return build(kind, text, basepath, filePath);
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private String build(Kind kind, String content, String base, IPath filePath) throws IOException, URISyntaxException {
		StringBuilder sb = new StringBuilder();
		switch (kind) {
			case EXPORT:
				sb.append("<html><head>" + Strings.EOL);
				sb.append(FileUtils.fromBundle("resources/html/meta.html", FluentUI.PLUGIN_ID) + Strings.EOL);
				sb.append(FileUtils.fromBundle("resources/html/highlight.html", FluentUI.PLUGIN_ID) + Strings.EOL);
				if (configurationProvider.useMathJax()) {
					sb.append(FileUtils.fromBundle("resources/html/mathjax.html", FluentUI.PLUGIN_ID) + Strings.EOL);
				}
				sb.append("<style media=\"screen\" type=\"text/css\">" + Strings.EOL);
				sb.append(getStyle(filePath) + Strings.EOL);
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
				String preview = FileUtils.fromBundle("resources/html/preview.html", FluentUI.PLUGIN_ID);
				preview = preview.replaceFirst("%path%", filePath.toString());
				sb.append(preview.replaceFirst("%styles%", getStyle(filePath)));
				break;

			case UPDATE:
				sb.append(content + Strings.EOL);
				break;
		}

		return sb.toString();
	}

	// path is the searchable base for the style to use; returns the content
	private String getStyle(IPath path) {
		try {
			URL url = findStyle(path);
			return FileUtils.read(url);
		} catch (Exception e) {
			Log.error("Failed reading stylesheet", e);
		}
		return "";
	}

	private URL findStyle(IPath path) throws Exception {
		// 1) look for a file having the same name as the input file, beginning in the
		// current directory, parent directories, and the current project directory.
		IPath style = path.removeFileExtension().addFileExtension(IConfigurationProvider.CSS);
		URL pathUrl = find(style);
		if (pathUrl != null) return pathUrl;

		// 2) look for a file with the name 'advantest.css' in the same set of directories
		style = path.removeLastSegments(1).append(IConfigurationProvider.CSS_DEFAULT);
		pathUrl = find(style);
		if (pathUrl != null) return pathUrl;

		// 3) read the file identified by the pref key 'EDITOR_CSS_EXTERNAL' from the filesystem
		String customCss = configurationProvider.getCustomCssSettingsFile();
		if (!customCss.isEmpty()) {
			File file = new File(customCss);
			if (file.isFile() && file.getName().endsWith("." + IConfigurationProvider.CSS)) {
				return toURL(file);
			}
		}

		// 4) read the file identified by the pref key 'EDITOR_CSS_BUILTIN' from the bundle
		String builtinCss = configurationProvider.getBuiltinCssSettingsFile();
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

		// 5) read 'advantest.css' from the bundle
		Bundle bundle = Platform.getBundle(FluentUI.PLUGIN_ID);
		URL url = FileLocator.find(bundle, new Path(IConfigurationProvider.CSS_RESOURCE_DIR + IConfigurationProvider.CSS_DEFAULT), null);
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
