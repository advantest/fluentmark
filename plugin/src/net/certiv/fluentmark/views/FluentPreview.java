/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.views;

import org.eclipse.ui.ide.IDE;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
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

import java.io.File;

import net.certiv.fluentmark.FluentUI;
import net.certiv.fluentmark.Log;
import net.certiv.fluentmark.editor.FluentEditor;
import net.certiv.fluentmark.preferences.Prefs;
import net.certiv.fluentmark.util.PartListener;

public class FluentPreview extends ViewPart implements PartListener, ITextListener, IPropertyChangeListener, Prefs {

	public static final String ID = "net.certiv.fluentmark.views.FluentPreview";

	private static FluentPreview viewpart;
	private Browser browser;
	private ViewJob viewjob;
	private IPath currentEditorInputPath;

	public FluentPreview() {
		viewpart = this;
	}

	/** Callback to create and initialize the browser. */
	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setRedraw(true);
		browser.setJavascriptEnabled(true);

		IPathEditorInput currentEditorInput = this.getEditorInput();
		this.currentEditorInputPath = (currentEditorInput != null ? currentEditorInput.getPath() : null);

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
			IPathEditorInput editorInput = (IPathEditorInput) currentEditor.getEditorInput();
			if (editorInput != null) {
				IPath editorInputPath = editorInput.getPath();
				if (currentEditorInputPath != null && !currentEditorInputPath.equals(editorInputPath)) {
					// it's not the same file as before, reload the HTML file header in our preview
					viewjob.load();
				}
				currentEditorInputPath = editorInputPath;
			} else {
				currentEditorInputPath = null;
			}

			viewjob.update();
		}
	}

	// view closed
	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof FluentEditor) {
			try { // exception when workbench close closes the editor
				getActivePage().hideView(viewpart);
			} catch (Exception e) {}
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
	
	protected IPathEditorInput getEditorInput() {
		FluentEditor editor = this.getEditor();
		if (editor != null
				&& editor.getEditorInput() != null) {
			return (IPathEditorInput) editor.getEditorInput();
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
		
		private FluentPreview preview;
		
		FluentBrowserUrlListener(FluentPreview preview) {
			this.preview = preview;
		}
		
		@Override
		public void changing(LocationEvent event) {
			String uri = event.location;

			if (uri != null && uri.equals("about:blank")) {
				// We're only opening the preview for a certain source file, we do not follow a
				// link.
				// --> Nothing to do in this case.
				return;
			}

			URI targetUri = null;
			try {
				targetUri = new URI (uri);
			} catch (URISyntaxException e) {
				// ignore malformed URI, do nothing
				return;
			}
			
			// if the link goes to a file in the Eclipse workspace, open it in an editor
			// (and update the preview in case of a markdown file)
			IFile fileInWorkspace = toWorkspaceRelativeFile(targetUri);
			if (fileInWorkspace != null) {
				
				if (fileInWorkspace.getFileExtension() != null
						&& "md".equals(fileInWorkspace.getFileExtension())) {
					// use our Fluentmark editor for markdown files
					FluentEditor editor = openFluentEditorWith(fileInWorkspace);
					if (editor != null) {
						// update preview contents due to a new Markdown file in focus / opened in editor
						preview.viewjob.load();
						
						// remember the anchor to scroll to, before loading the new Markdown file
						// (and changing the URL to "about:blank", i.e. loosing the anchor in the URL)
						if (targetUri.getFragment() != null) {
							preview.viewjob.setAnchorForNextPageLoad(targetUri.getFragment());
						}
					}
				} else {
					openFileInDefaultEditor(fileInWorkspace);
					
					// do not update the preview if it is not Markdown
					event.doit = false;
				}
				
				return;
			}
			
			// Adapt links to anchors (hashtags like #some-section)
			if (isLinkToAnchor(targetUri)) {

				String targetPath = targetUri.getPath();
				
				if (targetPath != null
						&& preview.currentEditorInputPath != null
						&& preview.currentEditorInputPath.toPortableString().equals(targetPath)) {

					// We're previewing the Markdown file currently open in editor, just fix the URI
					event.doit = false;
					
					String anchor = targetUri.getFragment();
					
					// call JavaScript function for scrolling to the anchor
					this.preview.viewjob.scrollTo(anchor);
					return;
				}
				
				// do nothing if the anchor is on the page we're viewing
				// Just open the link, which was fixed in a previous step
				return;
			}
			
			// In all other cases (not a Markdown file and not a link to an anchor on the same page)
			// open a Browser (internal or external browser, depending on the user-specific Eclipse preferences)
			event.doit = false;
			try {
				IWebBrowser webBrowser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
						IWorkbenchBrowserSupport.LOCATION_BAR
						| IWorkbenchBrowserSupport.NAVIGATION_BAR
						| IWorkbenchBrowserSupport.STATUS
						| IWorkbenchBrowserSupport.AS_VIEW,
						"com.advantest.fluentmark.browser.id",
						"FluentMark browser",
						"Browser instance used by Fluentmark to open any exernal link");
				webBrowser.openURL(targetUri.toURL());
			} catch (PartInitException | MalformedURLException e) {
				Log.error(String.format("Could not open URI %s in web browser", targetUri), e );
			}
		}

		@Override
		public void changed(LocationEvent event) {
			// do nothing (yet)
		}
		
		private IFile toWorkspaceRelativeFile(URI uri) {
			if (uri == null || uri.getPath() == null || !"file".equals(uri.getScheme())) {
				return null;
			}
			
			URI adaptedUri = uri;
			if (uri.getFragment() != null) {
				// remove trailing fragment & hashtag
				String uriString = uri.toString();
				int index = uriString.indexOf(uri.getFragment());
				uriString = uriString.substring(0, index - 1);
				
				adaptedUri = URI.create(uriString);
			}
			
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
		
		private boolean isLinkToAnchor(URI targetUri) {
			// fragment is something like #some-section at the end of the URI, i.e. an anchor ID
			if (targetUri == null
					|| targetUri.getFragment() == null) {
				return false;
			}
			
			return true;
		}
		
	}
	
}
