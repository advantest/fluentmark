/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.views;

import org.eclipse.ui.ide.IDE;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.widgets.Composite;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.io.File;

import net.certiv.fluentmark.FluentUI;
import net.certiv.fluentmark.Log;
import net.certiv.fluentmark.editor.FluentEditor;
import net.certiv.fluentmark.preferences.Prefs;
import net.certiv.fluentmark.util.PartListener;

public class FluentPreview extends ViewPart implements PartListener, ITextListener, IPropertyChangeListener, Prefs {

	public static final String ID = "net.certiv.fluentmark.views.FluentPreview";
	private static final String NO_CONTENT_TEXT = "No Markdown code to preview";

	private static FluentPreview viewpart;
	private Browser browser;
	private ViewJob viewjob;
	private IEditorInput currentEditorInput;

	public FluentPreview() {
		viewpart = this;
	}
	
	public String getHtmlViewContents() {
		if (viewjob != null) {
			return viewjob.getHtmlViewContents();
		}
		return null;
	}

	/** Callback to create and initialize the browser. */
	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setRedraw(true);
		browser.setJavascriptEnabled(true);
		browser.setText(NO_CONTENT_TEXT);

		currentEditorInput = this.getEditorInput();

		browser.addLocationListener(new FluentBrowserUrlListener(this));

		viewjob = new ViewJob(viewpart);
		getPreferenceStore().addPropertyChangeListener(this);
		getActivePage().addPartListener(this);
	}

	// -------------------------------------------------------------------------
	// view opened
	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof FluentEditor) {
			FluentEditor currentEditor = (FluentEditor) part;

			currentEditor.getViewer().addTextListener(this);

			// Load / re-create HTML file header (and base URL) if the opened Markdown file changes.
			// Otherwise, the relative references do not work anymore due to a wrong base URL
			// (e.g. set for another previously opened file from another directory).
			IEditorInput editorInput = currentEditor.getEditorInput();
			if (editorInput != null
					&& (currentEditorInput == null || !currentEditorInput.equals(editorInput))) {
				// it's not the same file as before, reload the HTML file header in our preview
				viewjob.load();
			}
			currentEditorInput = editorInput;
			
			viewjob.update();
		}
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		// a FluentEditor was closed and no other FluentEditor became active
		if (part instanceof FluentEditor
				&& (getActivePage().getActiveEditor() == null || !(getActivePage().getActiveEditor() instanceof FluentEditor) )) {
			currentEditorInput = null;
			browser.setText(NO_CONTENT_TEXT);
		}
	}

	// on content change in the editor's text viewer
	@Override
	public void textChanged(TextEvent event) {
		if (viewjob != null) viewjob.update();
	}

	// on property store change
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		switch (event.getProperty()) {
			case EDITOR_CSS_EXTERNAL:
			case EDITOR_CSS_BUILTIN:
				if (viewjob != null) viewjob.load();
		}
	}

	// -------------------------------------------------------------------------

	// called only by refresh view icon
	public void trigger() {
		viewjob.load();
		viewjob.update();
	}

	// called only by firebug view icon
	public void debug() {
		viewjob.load(true);
	}

	// -------------------------------------------------------------------------

	public Browser getBrowser() {
		return browser;
	}

	@Override
	public void setFocus() {
		if (browser != null) browser.setFocus();
	}

	protected IWorkbenchPage getActivePage() {
		return getSite().getWorkbenchWindow().getActivePage();
	}

	protected FluentEditor getEditor() {
		IEditorPart editor = getActivePage().getActiveEditor();
		if (editor != null && editor instanceof FluentEditor) {
			return (FluentEditor) editor;
		}
		return null;
	}

	protected ISourceViewer getSourceViewer() {
		FluentEditor editor = getEditor();
		if (editor == null) return null;
		return editor.getViewer();
	}
	
	protected IEditorInput getEditorInput() {
		FluentEditor editor = this.getEditor();
		if (editor != null) {
			return editor.getEditorInput();
		}
		return null;
	}

	protected IPreferenceStore getPreferenceStore() {
		return FluentUI.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(this);
		getActivePage().removePartListener(this);
		ITextViewer srcViewer = getSourceViewer();
		if (srcViewer != null) {
			srcViewer.removeTextListener(this);
		}

		if (viewjob != null) {
			viewjob.cancel();
			viewjob.dispose();
			viewjob = null;
		}
		
		if (browser != null && !browser.isDisposed()) {
			browser.dispose();
		}
		browser = null;
		
		super.dispose();
	}
	
	
	private static class FluentBrowserUrlListener implements LocationListener {
		
		private static final String FILE_EXTENSION_MARKDOWN = "md";
		private static final String URL_SCHEME_ABOUT = "about";
		private static final String URL_SCHEME_FILE = "file";
		private static final String URL_SCHEME_SPECIFIC_PART_BLANK = "blank";
		private static final String URL_ABOUT_BLANK = URL_SCHEME_ABOUT + ":" + URL_SCHEME_SPECIFIC_PART_BLANK;
		
		private final FluentPreview preview;
		
		FluentBrowserUrlListener(FluentPreview preview) {
			this.preview = preview;
		}
		
		@Override
		public void changing(LocationEvent event) {
			String url = event.location;

			if (url == null || isRenderedPageUrl(url)) {
				// We're only opening the preview for a certain source file, we do not follow a link.
				// --> Nothing to do in this case.
				return;
			}

			URI targetUri = translateUrlToUri(url);
			if (targetUri == null) {
				// open URL in a separate browser if it is not  URI
				event.doit = false;
				openUrlInSeparateWebBrowser(url);
				return;
			}
			
			if (isRenderedPageUriWithAnchor(targetUri)) {
				// Do nothing if the anchor is on the page we're rendering / viewing.
				// Just open the link, which was fixed in a previous step
				return;
			}
			
			// open links to non-files in a separate web browser
			if (!hasFileScheme(targetUri)) {
				event.doit = false;
				openUriInSeparateWebBrowser(targetUri);
				return;
			}
			
			
			IFile fileInWorkspace = toWorkspaceRelativeFile(targetUri);
			
			// open non-Markdown files in default editor
			if (fileInWorkspace != null) {
				if (!hasMarkdownFileExtension(fileInWorkspace)) {
					event.doit = false;
					openFileInDefaultEditor(fileInWorkspace);
					return;
				}
			} else {
				URI targetFileUriWithoutAnchor = omitAnchor(targetUri);
				IFileStore fileOutsideWorkspace = EFS.getLocalFileSystem().getStore(targetFileUriWithoutAnchor);
				if (!hasMarkdownFileExtension(fileOutsideWorkspace)) {
					event.doit = false;
					openFileInDefaultEditor(fileOutsideWorkspace);
					return;
				}
			}
			
			
			// We definitely have a Markdown file
			
			
			if (isLinkToAnchor(targetUri)
					&& isLinkToFileAlreadyOpenInFluentmarkEditor(targetUri)) {
				// We're previewing the Markdown file currently open in editor
				// no reload / rendering necessary, just scroll to the anchor
				// call JavaScript function for scrolling to the anchor
				event.doit = false;
				this.preview.viewjob.scrollTo(targetUri.getFragment());
				return;
			}
			
			// open Markdown file in our Fluentmark editor
			FluentEditor editor = null;
			if (fileInWorkspace != null) {
				editor = openFluentEditorWith(fileInWorkspace);
			} else {
				// file outside Eclipse workspace
				editor = openFluentEditorWith(omitAnchor(targetUri));
			}
			
			if (editor != null) {
				// update preview contents due to a new Markdown file in focus / opened in editor
				preview.viewjob.load();
				// we do not abort the event, since the URL should be opened by our preview browser
				
				// remember the anchor to scroll to, before loading the new Markdown file
				// (and changing the URL to "about:blank", i.e. loosing the anchor in the URL)
				if (isLinkToAnchor(targetUri)) {
					preview.viewjob.setAnchorForNextPageLoad(targetUri.getFragment());
				}
			} else {
				// abort preview refresh if we could not open the file in editor
				event.doit = false;
			}
			
		}

		@Override
		public void changed(LocationEvent event) {
			// do nothing (yet)
		}
		
		private boolean isRenderedPageUrl(String url) {
			if (url != null && url.equals(URL_ABOUT_BLANK)) {
				return true;
			}
			return false;
		}
		
		private boolean isRenderedPageUriWithAnchor(URI uri) {
			return (uri != null
				&& URL_SCHEME_ABOUT.equals(uri.getScheme())
				&& URL_SCHEME_SPECIFIC_PART_BLANK.equals(uri.getSchemeSpecificPart())
				&& isLinkToAnchor(uri));
		}
		
		private boolean hasFileScheme(URI uri) {
			return (uri != null
					&& uri.getScheme() != null
					&& URL_SCHEME_FILE.equals(uri.getScheme()));
		}
		
		private boolean hasMarkdownFileExtension(IFile file) {
			return (file != null
					&& file.getFileExtension() != null
					&& FILE_EXTENSION_MARKDOWN.equalsIgnoreCase(file.getFileExtension()));
		}
		
		private boolean hasMarkdownFileExtension(IFileStore fileStore) {
			if (fileStore == null) {
				return false;
			}
			
			int lastPointIndex = fileStore.getName().lastIndexOf('.');
			if (lastPointIndex < 0) {
				return false;
			}
			
			String fileExtension = fileStore.getName().substring(lastPointIndex + 1);
			
			return FILE_EXTENSION_MARKDOWN.equalsIgnoreCase(fileExtension);
		}
		
		private URI translateUrlToUri(String url) {
			if (url == null || url.length() == 0) {
				return null;
			}
			
			try {
				return new URI (url);
			} catch (URISyntaxException e) {
				// ignore malformed URI (remember: URIs are always URLs, but URLs are not always URIs)
			}
			
			return null;
		}
		
		private IWebBrowser openUriInSeparateWebBrowser(URI uri) {
			try {
				return openUrlInSeparateWebBrowser(uri.toURL());
			} catch (MalformedURLException e) {
				Log.error(String.format("Could not open URI %s in web browser", uri), e );
			}
			
			return null;
		}
		
		private IWebBrowser openUrlInSeparateWebBrowser(String url) {
			try {
				return openUrlInSeparateWebBrowser(new URL(url));
			} catch (MalformedURLException e) {
				Log.error(String.format("Could not open URL %s in web browser", url), e );
			}
			
			return null;
		}
		
		private IWebBrowser openUrlInSeparateWebBrowser(URL url) {
			try {
				// open a Browser (internal or external browser, depending on the user-specific Eclipse preferences)
				IWebBrowser webBrowser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
						IWorkbenchBrowserSupport.LOCATION_BAR
						| IWorkbenchBrowserSupport.NAVIGATION_BAR
						| IWorkbenchBrowserSupport.STATUS
						| IWorkbenchBrowserSupport.AS_VIEW,
						"com.advantest.fluentmark.browser.id",
						"FluentMark browser",
						"Browser instance used by Fluentmark to open any exernal link");
				webBrowser.openURL(url);
				return webBrowser;
			} catch (PartInitException e) {
				Log.error(String.format("Could not open URL %s in web browser", url), e );
			}
			
			return null;
		}
		
		private URI omitAnchor(URI uri) {
			if (uri == null || uri.getFragment() == null) {
				return uri;
			}
			
			// remove trailing fragment & hashtag
			String uriString = uri.toString();
			int index = uriString.indexOf(uri.getFragment());
			uriString = uriString.substring(0, index - 1);
			
			return URI.create(uriString);
		}
		
		private IFile toWorkspaceRelativeFile(URI uri) {
			if (uri == null || uri.getPath() == null || !URL_SCHEME_FILE.equals(uri.getScheme())) {
				return null;
			}
			
			URI adaptedUri = omitAnchor(uri);
			
			File file = null;
			try {
				file = new File(adaptedUri);
			} catch (Exception e) {
				return null;
			}
			 
			if (!file.exists() || !file.isFile()) {
				return null;
			}
			
			IFile[] filesFound = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(adaptedUri);
			if (filesFound.length == 1) {
				return filesFound[0];
			}
			
			return null;
		}
		
		private FluentEditor openFluentEditorWith(IFile file) {
			if (file == null) {
				return null;
			}
			
			IWorkbenchPage activePage = preview.getActivePage();
			if (activePage != null) {
				try {
					return (FluentEditor) IDE.openEditor(activePage, file, FluentEditor.ID);
				} catch (PartInitException e) {
					Log.error(String.format("Could not open file (path=%s) in FluentMark editor", file.getLocation()), e);
				}
			}
				
			return null;
		}
		
		private FluentEditor openFluentEditorWith(URI fileUri) {
			if (fileUri == null || fileUri.getPath() == null || !URL_SCHEME_FILE.equals(fileUri.getScheme())) {
				return null;
			}
			
			IWorkbenchPage activePage = preview.getActivePage();
			if (activePage != null) {
				try {
					return (FluentEditor) IDE.openEditor(activePage, fileUri, FluentEditor.ID, true);
				} catch (PartInitException e) {
					Log.error(String.format("Could not open file outside Eclipse workspace (URI=%s) in FluentMark editor", fileUri.toString()), e);
				}
			}
				
			return null;
		}
		
		private IEditorPart openFileInDefaultEditor(IFile file) {
			if (file == null) {
				return null;
			}
			
			IWorkbenchPage activePage = preview.getActivePage();
			if (activePage != null) {
				try {
					return IDE.openEditor(activePage, file);
				} catch (PartInitException e) {
					Log.error(String.format("Could not open file (path=%s) in default editor", file.getLocation()), e);
				}
			}
			
			return null;
		}
		
		private IEditorPart openFileInDefaultEditor(IFileStore fileStore) {
			if (fileStore == null
					|| !fileStore.fetchInfo().exists()
					|| fileStore.fetchInfo().isDirectory()) {
				return null;
			}
			
			IWorkbenchPage activePage = preview.getActivePage();
			if (activePage != null) {
				try {
					return IDE.openEditorOnFileStore(activePage, fileStore);
				} catch (PartInitException e) {
					Log.error(String.format("Could not open file outside Eclipse workspace (path=%s) in default editor", fileStore.getName()), e);
				}
			}
			
			return null;
		}
		
		private boolean isLinkToAnchor(URI targetUri) {
			// fragment is something like #some-section at the end of the URI, i.e. an anchor ID
			return (targetUri != null && targetUri.getFragment() != null);
		}
		
		private boolean isLinkToFileAlreadyOpenInFluentmarkEditor(URI targetUri) {
			if (targetUri == null || preview.currentEditorInput == null) {
				return false;
			}
			
			if (preview.currentEditorInput instanceof IURIEditorInput) {
				URI currentUri = ((IURIEditorInput) preview.currentEditorInput).getURI();
				String currentPath = currentUri.getPath();
				String targetPath = targetUri.getPath();
				return currentPath != null && targetPath != null && targetPath.equals(currentPath);
			} else if (preview.currentEditorInput instanceof IPathEditorInput) {
				IPath currentPath = ((IPathEditorInput) preview.currentEditorInput).getPath();
				String targetPath = targetUri.getPath();
				
				return (targetPath != null
						&& currentPath != null
						&& currentPath.toPortableString().equals(targetPath));
			}
			
			return false;
		}
	}
	
}
