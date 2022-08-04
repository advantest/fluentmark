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
import org.eclipse.ui.part.ViewPart;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

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

		FluentEditor editor = this.getEditor();
		if (editor != null && editor.getEditorInput() != null) {
			this.currentEditorInputPath = ((IPathEditorInput) editor.getEditorInput()).getPath();
		} else {
			currentEditorInputPath = null;
		}

		browser.addLocationListener(new LocationListener() {

			@Override
			public void changing(LocationEvent event) {
				String urlText = event.location;

				if (urlText != null && urlText.equals("about:blank")) {
					// We're only opening the preview for a certain source file, we do not follow a
					// link.
					// --> Nothing to do in this case.
					return;
				}

				// If the link goes to a markdown file, open it in an editor.
				// That will cause this viewer to update its contents to the rendered version of
				// the target file.
				File targetFile = null;
				try {
					URL targetUrl = new URL(urlText);

					if (targetUrl.getPath().endsWith(".md")) {
						targetFile = new File(targetUrl.toURI());
					} else if (targetUrl.getRef() != null) {
						String targetPath = targetUrl.getPath();
						if (targetPath.endsWith("/")) {
							targetPath = targetPath.substring(0, targetPath.length() - 1);
						}
						if (currentEditorInputPath != null) {
							IPath currentParentPath = currentEditorInputPath.removeLastSegments(1);
							if (currentParentPath.toPortableString().equals(targetPath)) {
								// do not load the corrupt URL (made of parent-folder-path/#target-reference)
								event.doit = false;
								return;
								// browser.setUrl( "about:blank#" + targetUrl.getRef() );
							}
						}
					}
				} catch (MalformedURLException | URISyntaxException e1) {
					return;
				}

				if (targetFile != null && targetFile.exists() && targetFile.isFile() && urlText.endsWith(".md")) {

					String absoluteWorkspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
					String targetFilePath = targetFile.getPath();
					if (targetFilePath.startsWith(absoluteWorkspacePath)) {
						// ok, the target file is in our Eclipse workspace
						String workspaceRelativPath = targetFilePath.substring(absoluteWorkspacePath.length());

						IPath path = new Path(workspaceRelativPath);
						IFile fileToOpen = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

						IWorkbenchPage activePage = FluentPreview.this.getActivePage();
						try {
							FluentEditor editor = (FluentEditor) IDE.openEditor(activePage, fileToOpen,
									FluentEditor.ID);
							if (editor != null) {
								viewjob.load();
							}
						} catch (PartInitException e) {
							Log.error(String.format("Could not open FluentMark editor for path $s", path.toString()), e);
						}
					}
				}

			}

			@Override
			public void changed(LocationEvent event) {
			}

		});

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

      currentEditor.getViewer().addTextListener( this );

      // load / create HTML file header (and base URL) if the opened Markdown file changes
      // otherwise, the relative references do not work anymore due to a wrong base URL
      IPathEditorInput editorInput = (IPathEditorInput) currentEditor.getEditorInput();
      if( editorInput != null ) {
        IPath editorInputPath = editorInput.getPath();
        if( currentEditorInputPath != null && !currentEditorInputPath.equals( editorInputPath ) ) {
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
		browser = null;
		super.dispose();
	}
}
