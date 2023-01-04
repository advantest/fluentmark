/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.views;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

import net.certiv.fluentmark.core.convert.IConfigurationProvider;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.preferences.Prefs;
import net.certiv.fluentmark.ui.util.PartListener;

public class FluentPreview extends ViewPart implements PartListener, ITextListener, IPropertyChangeListener, Prefs {

	public static final String ID = FluentUI.PLUGIN_ID + ".views.FluentPreview";
	private static final String NO_CONTENT_TEXT = "No Markdown code to preview";

	private static FluentPreview viewpart;
	private Browser browser;
	private ViewJob viewjob;
	private FluentEditor latestActiveEditor;
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

		latestActiveEditor = this.getActiveFluentEditor();
		currentEditorInput = this.getActiveEditorInput();

		viewjob = new ViewJob(viewpart);
		
		browser.addLocationListener(new FluentBrowserUrlListener(this, viewjob));
		
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
			latestActiveEditor = currentEditor;
			currentEditorInput = editorInput;
			
			viewjob.update();
		}
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		// a FluentEditor was closed and no other FluentEditor became active
		if (part instanceof FluentEditor
				&& (getActivePage().getActiveEditor() == null || !(getActivePage().getActiveEditor() instanceof FluentEditor) )) {
			latestActiveEditor = null;
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
			case IConfigurationProvider.EDITOR_CSS_EXTERNAL:
			case IConfigurationProvider.EDITOR_CSS_BUILTIN:
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

	protected FluentEditor getActiveFluentEditor() {
		IEditorPart editor = getActivePage().getActiveEditor();
		if (editor != null && editor instanceof FluentEditor) {
			return (FluentEditor) editor;
		}
		return null;
	}

	protected ISourceViewer getActiveSourceViewer() {
		FluentEditor editor = getActiveFluentEditor();
		if (editor == null) return null;
		return editor.getViewer();
	}
	
	protected IEditorInput getActiveEditorInput() {
		FluentEditor editor = this.getActiveFluentEditor();
		if (editor != null) {
			return editor.getEditorInput();
		}
		return null;
	}
	
	IEditorInput getCurrentEditorInput() {
		return this.currentEditorInput;
	}
	
	FluentEditor getLatestActiveFluentEditor() {
		return this.latestActiveEditor;
	}

	protected IPreferenceStore getPreferenceStore() {
		return FluentUI.getDefault().getPreferenceStore();
	}

	@Override
	public void dispose() {
		getPreferenceStore().removePropertyChangeListener(this);
		getActivePage().removePartListener(this);
		ITextViewer srcViewer = getActiveSourceViewer();
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
		
		this.latestActiveEditor = null;
		this.currentEditorInput = null;
		
		super.dispose();
	}
	
}
