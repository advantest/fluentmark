/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.editor;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.swt.graphics.Point;

import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import org.eclipse.ui.ide.ResourceUtil;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;

import javax.swing.event.EventListenerList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import java.net.URI;

import net.certiv.fluentmark.FluentUI;
import net.certiv.fluentmark.Log;
import net.certiv.fluentmark.convert.Converter;
import net.certiv.fluentmark.convert.HtmlGen;
import net.certiv.fluentmark.convert.IConfigurationProvider;
import net.certiv.fluentmark.convert.Kind;
import net.certiv.fluentmark.core.dot.DotRecord;
import net.certiv.fluentmark.core.markdown.IOffsetProvider;
import net.certiv.fluentmark.core.markdown.ISourceRange;
import net.certiv.fluentmark.core.markdown.ISourceReference;
import net.certiv.fluentmark.core.markdown.PagePart;
import net.certiv.fluentmark.core.markdown.PageRoot;
import net.certiv.fluentmark.core.util.LRUCache;
import net.certiv.fluentmark.core.util.Strings;
import net.certiv.fluentmark.editor.color.IColorManager;
import net.certiv.fluentmark.editor.folding.FoldingStructureProvider;
import net.certiv.fluentmark.editor.folding.IFoldingStructureProvider;
import net.certiv.fluentmark.editor.text.SmartBackspaceManager;
import net.certiv.fluentmark.outline.FluentOutlinePage;
import net.certiv.fluentmark.outline.operations.AbstractDocumentCommand;
import net.certiv.fluentmark.outline.operations.CommandManager;
import net.certiv.fluentmark.preferences.Prefs;

/**
 * Text editor with markdown support.
 */
public class FluentEditor extends TextEditor
		implements CommandManager, IShowInTarget, IShowInSource, IReconcilingListener, IOffsetProvider {

	public static final String ID = "net.certiv.fluentmark.editor.FluentEditor";
	
	private static final int POST_CHANGE_EVENT = IResourceChangeEvent.POST_CHANGE;

	private FluentSourceViewer viewer;
	private FluentOutlinePage outlinePage;
	private FluentTextTools tools;
	private IColorManager colorManager;
	private Converter converter;
	private PageRoot pageModel;
	private IFoldingStructureProvider projectionProvider;
	private ProjectionSupport projectionSupport;
	private IDocumentListener docListener;
	private EventListenerList docListenerList;
	private IPropertyChangeListener prefChangeListener;
	// private SemanticHighlightingManager semanticManager;

	private final LRUCache<IRegion, DotRecord> parseRecords = new LRUCache<>(25);

	private boolean pageDirty = true;

	private ListenerList<IReconcilingListener> reconcilingListeners;
	private EditorSelectionChangedListener editorSelectionChangedListener;
	private PageModelUpdater pageModelUpdater;
	private boolean disableSelResponse;
	private HtmlGen htmlGen;
	private ModelUpdater modelUpdater;
	private IConfigurationProvider configProvider;

	public FluentEditor() {
		super();
		this.configProvider = new ConfigurationProvider();
	}
	
	// Updates the DslOutline pageModel selection and this editor's range indicator.
	private class EditorSelectionChangedListener extends AbstractSelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			FluentEditor.this.selectionChanged();
		}
	}
	
	private class PageModelUpdater implements IResourceChangeListener {
		
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getSource() instanceof IWorkspace) {
				switch (event.getType()) {
					case POST_CHANGE_EVENT:
						try {
							if (event.getDelta() != null
									&& FluentEditor.this.isActiveOn(event.getResource())) {
								// update page model if page dirty
								FluentEditor.this.getPageModel();
							}
						} catch (Exception e) {
							Log.error("Failed handing post_change of resource", e);
						}
						break;
				}
			}
		}
		
	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { "org.eclipse.ui.textEditorScope", "net.certiv.fluentmark.ui.editorScope" });
	}

	@Override
	protected boolean isTabsToSpacesConversionEnabled() {
		return true;
	}

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		createListeners();
		initEditorPreferenceStore();
		colorManager = FluentUI.getDefault().getColorMgr();
		tools = FluentUI.getDefault().getTextTools();
		SourceViewerConfiguration config = tools.createSourceViewerConfiguraton(getPreferenceStore(), this);
		setSourceViewerConfiguration(config);
		setDocumentProvider(getDocumentProvider());
		int tabWidth = FluentUI.getDefault().getPreferenceStore().getInt(Prefs.EDITOR_TAB_WIDTH);
		pageModel = new PageRoot(this, getLineDelimiter(), tabWidth);
		converter = new Converter(this.configProvider);
		htmlGen = new HtmlGen(converter, this.configProvider);
		
		pageModelUpdater = new PageModelUpdater();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(pageModelUpdater, POST_CHANGE_EVENT);
		modelUpdater = new ModelUpdater(pageModel, getResource());
		this.addDocChangeListener(modelUpdater);
	}

	private void createListeners() {
		reconcilingListeners = new ListenerList<>(ListenerList.IDENTITY);
		docListenerList = new EventListenerList();
		docListener = new IDocumentListener() {

			@Override
			public void documentChanged(DocumentEvent event) {
				pageDirty = true;
				fireDocumentChanged(event);
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) {}
		};

		prefChangeListener = new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(Prefs.EDITOR_WORD_WRAP)) {
					getViewer().getTextWidget().setWordWrap(isWordWrap());
				}
			}
		};
	}

	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		if (getDocument() != null) getDocument().removeDocumentListener(docListener);
		setPreferenceStore(getPrefsStore());
		super.doSetInput(input);
		IDocument doc = getDocumentProvider().getDocument(input);

		// check and correct line endings
		String text = doc.get();
		int hash = text.hashCode();
		text = Strings.normalize(text);
		if (hash != text.hashCode()) doc.set(text);

		connectPartitioningToElement(input, doc);

		FluentSourceViewer sourceViewer = (FluentSourceViewer) getSourceViewer();
		if (sourceViewer != null && sourceViewer.getReconciler() == null) {
			IReconciler reconciler = getSourceViewerConfiguration().getReconciler(sourceViewer);
			if (reconciler != null) {
				reconciler.install(sourceViewer);
				sourceViewer.setReconciler(reconciler);
			}
		}

		// Attach listener to new doc
		doc.addDocumentListener(docListener);
		installSemanticHighlighting();

		// Initialize code folding
		if (projectionProvider != null) {
			projectionProvider.initialize();
		}
	}

	private void connectPartitioningToElement(IEditorInput input, IDocument document) {
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension = (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(Partitions.PARTITIONING) == null) {
				FluentDocumentSetupParticipant participant = new FluentDocumentSetupParticipant(tools);
				participant.setup(document);
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor progressMonitor) {
		IDocument doc = getDocumentProvider().getDocument(getEditorInput());
		if (doc != null) {
			IDocumentUndoManager undoMgr = DocumentUndoManagerRegistry.getDocumentUndoManager(doc);
			if (undoMgr != null) undoMgr.commit();
		}
		super.doSave(progressMonitor);
	}

	/**
	 * Returns the editor's source viewer. May return null before the editor's part has been created and
	 * after disposal.
	 */
	public ISourceViewer getViewer() {
		return getSourceViewer();
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(composite);

		Composite editorComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editorComposite);
		editorComposite.setLayout(new FillLayout(SWT.VERTICAL));

		viewer = new FluentSourceViewer(editorComposite, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles,
				getPreferenceStore());
		if (isFoldingEnabled() && !getPreferenceStore().getBoolean(Prefs.EDITOR_SHOW_SEGMENTS)) {
			viewer.prepareDelayedProjection();
		}

		projectionSupport = new ProjectionSupport(viewer, getAnnotationAccess(), getSharedColors());
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); // $NON-NLS-1$
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); // $NON-NLS-1$
		projectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.info"); // $NON-NLS-1$
		projectionSupport.addSummarizableAnnotationType("org.eclipse.search.results"); // $NON-NLS-1$
		projectionSupport.setHoverControlCreator(new IInformationControlCreator() {

			@Override
			public IInformationControl createInformationControl(Shell shell) {
				String statusFieldText = EditorsUI.getTooltipAffordanceString();
				return new FluentSourceViewerInfoControl(shell, false, getOrientation(), statusFieldText);
			}
		});
		projectionSupport.install();

		projectionProvider = new FoldingStructureProvider();
		if (projectionProvider != null) {
			projectionProvider.install(this, viewer, getPreferenceStore());
		}

		if (isFoldingEnabled()) {
			viewer.doOperation(ProjectionViewer.TOGGLE);
		}

		getSourceViewerDecorationSupport(viewer);
		viewer.getTextWidget().setWordWrap(isWordWrap());
		return viewer;
	}

	private boolean isWordWrap() {
		return getPreferenceStore().getBoolean(Prefs.EDITOR_WORD_WRAP);
	}

	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(pageModelUpdater);
		if (modelUpdater != null) {
			removeDocChangeListener(modelUpdater);
		}
		removePreferenceStoreListener();
		uninstallSemanticHighlighting();
		parseRecords.clear();
		colorManager.dispose();
		colorManager = null;
		super.dispose();
	}

	@Override
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		SourceViewerConfiguration configuration = getSourceViewerConfiguration();
		if (configuration instanceof FluentSourceViewerConfiguration) {
			return ((FluentSourceViewerConfiguration) configuration).affectsTextPresentation(event);
		}
		return false;
	}

	@Override
	protected void performRevert() {
		ProjectionViewer projectionViewer = (ProjectionViewer) getSourceViewer();
		projectionViewer.setRedraw(false);
		try {

			boolean projectionMode = projectionViewer.isProjectionMode();
			if (projectionMode) {
				projectionViewer.disableProjection();
				if (projectionProvider != null) {
					projectionProvider.uninstall();
				}
			}

			super.performRevert();

			if (projectionMode) {
				if (projectionProvider != null) {
					projectionProvider.install(this, projectionViewer, getPreferenceStore());
				}
				projectionViewer.enableProjection();
			}

		} finally {
			projectionViewer.setRedraw(true);
		}
	}

	/**
	 * Initializes the preference store for this editor. The constucted store represents the combined
	 * values of the FluentUI, EditorsUI, and PlatformUI stores.
	 */
	private void initEditorPreferenceStore() {
		IPreferenceStore store = FluentUI.getDefault().getCombinedPreferenceStore();
		store.addPropertyChangeListener(prefChangeListener);
		setPreferenceStore(store);
	}

	private void removePreferenceStoreListener() {
		if (getPreferenceStore() != null) {
			getPreferenceStore().removePropertyChangeListener(prefChangeListener);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		editorSelectionChangedListener = new EditorSelectionChangedListener();
		editorSelectionChangedListener.install(getSelectionProvider());

		if (isMarkingOccurrences()) {
			installOccurrencesFinder(false);
		}
	}

	private FluentOutlinePage createOutlinePage() {
		final FluentOutlinePage page = new FluentOutlinePage(this, getPreferenceStore());
		setOutlinePageInput(page, getEditorInput());
		return page;
	}

	private boolean isOutlinePageValid() {
		return outlinePage != null && outlinePage.getControl() != null && !outlinePage.getControl().isDisposed();
	}

	/**
	 * Informs the editor that its outliner has been closed.
	 */
	public void outlinePageClosed() {
		if (outlinePage != null) {
			outlinePage = null;
			resetHighlightRange();
		}
	}

	private void setOutlinePageInput(FluentOutlinePage page, IEditorInput input) {
		if (page == null) return;
		PageRoot model = PageRoot.MODEL;
		if (model != null) {
			page.setInput(model);
		} else {
			page.setInput(null);
		}
	}

	/**
	 * Handles a property change event describing a change of the editor's preference delta and updates
	 * the preference related editor properties.
	 *
	 * @param event the property change event
	 */
	@Override
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		String property = event.getProperty();
		try {
			SourceViewerConfiguration config = getSourceViewerConfiguration();
			if (config != null) ((FluentSourceViewerConfiguration) config).handlePropertyChangeEvent(event);

			if (Prefs.EDITOR_TAB_WIDTH.equals(property)) {
				StyledText textWidget = getViewer().getTextWidget();
				int tabWidth = getSourceViewerConfiguration().getTabWidth(getViewer());
				if (textWidget.getTabs() != tabWidth) {
					textWidget.setTabs(tabWidth);
				}
				uninstallTabsToSpacesConverter();
				if (isTabsToSpacesConversionEnabled()) {
					installTabsToSpacesConverter();
				} else {
					updateIndentPrefixes();
				}
				return;
			}
		} finally {
			super.handlePreferenceStoreChanged(event);
		}
	}

	/**
	 * Determines if folding is enabled.
	 *
	 * @return <code>true</code> if folding is enabled, <code>false</code> otherwise.
	 */
	boolean isFoldingEnabled() {
		IPreferenceStore store = getPreferenceStore();
		return store.getBoolean(Prefs.FOLDING_FRONTMATTER_ENABLED)
				|| store.getBoolean(Prefs.FOLDING_HIDDEN_COMMENTS_ENABLED)
				|| store.getBoolean(Prefs.FOLDING_CODEBLOCKS_ENABLED);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public <T> T getAdapter(Class<T> target) {
		if (IContentOutlinePage.class.equals(target)) {
			if (!isOutlinePageValid()) {
				outlinePage = createOutlinePage();
			}
			return (T) outlinePage;
		}
		if (SmartBackspaceManager.class.equals(target)) {
			if (getSourceViewer() instanceof FluentSourceViewer) {
				return (T) ((FluentSourceViewer) getSourceViewer()).getBackspaceManager();
			}
		}
		if (PagePart.class.equals(target)) {
			return (T) getPageModel();
		}
		if (ProjectionAnnotationModel.class.equals(target)) {
			if (projectionSupport != null) {
				Object adapter = projectionSupport.getAdapter(getSourceViewer(), target);
				if (adapter != null) return (T) adapter;
			}
		}
		if (target == IFoldingStructureProvider.class) {
			return (T) projectionProvider;
		}
		return super.getAdapter(target);
	}

	@Override
	public IEditorInput getEditorInput() {
		IEditorInput input = super.getEditorInput();
		if (input instanceof IPathEditorInput
				|| input instanceof IURIEditorInput) {
			return input;
		}
		
		input = getAdapter(IPathEditorInput.class);
		if (input == null) {
			input = getAdapter(IURIEditorInput.class);
		}
		return input;
	}

	public IDocument getDocument() {
		IEditorInput input = getEditorInput();
		IDocumentProvider provider = getDocumentProvider();
		return provider == null ? null : provider.getDocument(input);
	}

	public FluentEditor ensureLastLineBlank() {
		IDocument doc = getDocument();
		String text = doc.get();
		if (!text.endsWith(Strings.EOL)) {
			doc.set(text + Strings.EOL);
		}
		return this;
	}

	public String getLineDelimiter() {
		return getLineDelimiter(getDocument());
	}

	public String getLineDelimiter(IDocument doc) {
		try {
			if (doc != null) return doc.getLineDelimiter(0);
		} catch (BadLocationException e) {}

		// workspace preference
		IScopeContext[] scopeContext = new IScopeContext[] { InstanceScope.INSTANCE };
		return Platform.getPreferencesService().getString(Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR,
				Strings.EOL, scopeContext);
	}

	public PageRoot getPageModel() {
		if (pageDirty) updatePageModel();
		return pageModel;
	}

	public boolean isPageModelDirty() {
		return pageDirty;
	}

	public PageRoot getPageModel(boolean forceUpdate) {
		if (forceUpdate) pageDirty = true;
		return getPageModel();
	}

	private void updatePageModel() {
		String text = getText();
		if (text == null) text = "";
		IResource resource = ResourceUtil.getResource(getEditorInput());
		pageDirty = false;
		try {
			pageModel.updateModel(resource, text);
		} catch (CoreException e) {
			FluentUI.log(IStatus.ERROR, "Failed updating model", e);
		}
	}

	/** Make the store visible outside of the editor */
	public IPreferenceStore getPrefsStore() {
		return getPreferenceStore();
	}

	/** Gets the text of the current document, or null. */
	public String getText() {
		IDocument doc = getDocument();
		return doc == null ? null : doc.get();
	}
	
	public boolean useMathJax() {
		return this.configProvider.useMathJax();
	}

	/** Returns the Html content. */
	public String getHtml(Kind kind) {
		IEditorInput input = this.getEditorInput();
		if (input == null) return "";
		
		String basepath = null;
		IPath filePath = null;
		if (input instanceof IPathEditorInput) {
			filePath = ((IPathEditorInput) input).getPath();
			basepath = filePath.removeLastSegments(1).addTrailingSeparator().toString();
		} else if (input instanceof IURIEditorInput) {
			URI uri = ((IURIEditorInput) input).getURI();
			basepath = uri.getPath();
			basepath = basepath.substring(0, basepath.lastIndexOf('/') + 1);
			filePath = new Path(uri.getPath());
		}
		
		return htmlGen.getHtml(getDocument(), filePath, basepath, kind);
	}

	/**
	 * React to change selection event in the editor and outline!
	 */
	public void selectionChanged() {
		if (disableSelResponse) return;

		if (getSelectionProvider() == null) return;
		ISourceReference element = computeHighlightRange();
		if (element != null) {
			disableSelResponse = true;
			try {
				if (isOutlinePageValid()) outlinePage.select(element);
				setSelection(element.getSourceRange(), false);
			} finally {
				disableSelResponse = false;
			}
		}
		updateStatusLine();
	}

	/**
	 * Sets the current editor selection to the source range. Optionally sets the current editor
	 * position.
	 *
	 * @param range the source range to be shown in the editor, can be null.
	 * @param moveCursor if true the editor is scrolled to show the range.
	 */
	private void setSelection(ISourceRange range, boolean moveCursor) {
		if (range == null) return;

		try {
			int start = range.getOffset();
			int length = range.getLength();
			setHighlightRange(start, length, moveCursor);

			if (moveCursor) {
				if (start > -1 && getSourceViewer() != null) {
					getSourceViewer().revealRange(start, length);
					// getSourceViewer().setSelectedRange(start, length);
				}
				updateStatusField(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
			}
			return;
		} catch (IllegalArgumentException e) {}

		if (moveCursor) resetHighlightRange();
	}

	public void revealPart(PagePart part) {
		reveal(part.getSourceRange().getOffset(), part.getSourceRange().getLength());
		// TODO: scroll browser
	}

	/**
	 * Reveals the specified ranges in this text editor.
	 *
	 * @param offset the offset of the revealed range
	 * @param len the length of the revealed range
	 */
	protected void reveal(int offset, int len) {
		if (viewer == null) return;

		ISelection sel = getSelectionProvider().getSelection();
		if (sel instanceof ITextSelection) {
			ITextSelection tsel = (ITextSelection) sel;
			if (tsel.getOffset() != 0 || tsel.getLength() != 0) markInNavigationHistory();
		}

		StyledText widget = viewer.getTextWidget();
		widget.setRedraw(false);
		setHighlightRange(offset, len, false);
		viewer.revealRange(offset, len);
		markInNavigationHistory();
		widget.setRedraw(true);
	}

	protected void updateStatusLine() {
		ITextSelection selection = (ITextSelection) getSelectionProvider().getSelection();
		Annotation annotation = getAnnotation(selection.getOffset(), selection.getLength());
		String message = null;
		if (annotation != null) {
			updateMarkerViews(annotation);
			if (annotation instanceof IFluentAnnotation && ((IFluentAnnotation) annotation).isProblem()) {
				message = annotation.getText();
			}
		}
		setStatusLineErrorMessage(null);
		setStatusLineMessage(message);
	}

	/**
	 * Computes and returns the source reference that includes the caret and serves as provider for the
	 * outline pageModel selection and the editor range indication.
	 *
	 * @return the computed source reference
	 */
	public ISourceReference computeHighlightRange() {
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer == null) return null;

		StyledText styledText = sourceViewer.getTextWidget();
		if (styledText == null) return null;

		int caret = 0;
		if (sourceViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension = (ITextViewerExtension5) sourceViewer;
			caret = extension.widgetOffset2ModelOffset(styledText.getSelection().x);
		} else {
			int offset = sourceViewer.getVisibleRegion().getOffset();
			caret = offset + styledText.getSelection().x;
		}

		PagePart part = getPagePartAt(caret, false);
		return part;
	}

	private PagePart getMatchingPagePart() {
		Point selectedRange = getSourceViewer().getSelectedRange();
		if (selectedRange != null) {
			return getPagePartAt(selectedRange.x, false);
		}
		return null;
	}

	/**
	 * Returns the most narrow element including the given offset. If <code>reconcile</code> is
	 * <code>true</code> the editor's input element is reconciled in advance. If it is
	 * <code>false</code> this method only returns a result if the editor's input element does not need
	 * to be reconciled.
	 *
	 * @param offset the offset included by the retrieved element
	 * @param reconcile <code>true</code> if should be reconciled
	 * @return the most narrow element which includes the given offset
	 */
	private PagePart getPagePartAt(int offset, boolean reconcile) {
		return getPageModel().partAtOffset(offset);
	}

	final AtomicInteger atom = new AtomicInteger();

	public int getCursorOffset() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				atom.set(getSourceViewer().getTextWidget().getCaretOffset());
			}
		});
		return atom.get();
	}

	public void setCursorOffset(final int offset) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				getSourceViewer().getTextWidget().setCaretOffset(offset);
			}
		});
	}

	/**
	 * Returns the annotation overlapping with the given range or <code>null</code>.
	 *
	 * @param offset the region offset
	 * @param length the region length
	 * @return the found annotation or <code>null</code>
	 */
	private Annotation getAnnotation(int offset, int length) {
		IAnnotationModel model = getDocumentProvider().getAnnotationModel(getEditorInput());
		if (model == null) return null;

		Iterator<Annotation> e = new AnnotationIterator(model, true, false);
		while (e.hasNext()) {
			Annotation a = e.next();
			Position p = model.getPosition(a);
			if (p != null && p.overlapsWith(offset, length)) return a;
		}
		return null;
	}

	public boolean isActiveOn(IResource resource) {
		IFile current = getResource();
		if (current != null && current.equals(resource)) return true;
		return false;
	}

	private IFile getResource() {
		return ResourceUtil.getFile(getEditorInput());
	}

	void updateTaskTags(IRegion region) {
		boolean useTags = getPreferenceStore().getBoolean(Prefs.EDITOR_TASK_TAGS);
		if (!useTags) return;

		String tagString = getPreferenceStore().getString(Prefs.EDITOR_TASK_TAGS_DEFINED);
		List<String> tags = new ArrayList<>();
		for (String tag : tagString.split(",")) {
			tags.add(tag.trim());
		}

		IFile markFile = getResource();
		IMarker[] taskMarkers;
		try {
			taskMarkers = markFile.findMarkers(IMarker.TASK, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			return;
		}

		List<IMarker> markers = new ArrayList<>(Arrays.asList(taskMarkers));
		getPageModel().markTaggedLines(markFile, tags, markers);

	}

	protected void installSemanticHighlighting() {
		// if (semanticManager == null) {
		// semanticManager = new SemanticHighlightingManager();
		// semanticManager.install(this, (FluentSourceViewer) getSourceViewer(), colorManager,
		// getPreferenceStore());
		// }
	}

	private void uninstallSemanticHighlighting() {
		// if (semanticManager != null) {
		// semanticManager.uninstall();
		// semanticManager = null;
		// }
	}

	public boolean isMarkingOccurrences() {
		return false;
	}

	public void installOccurrencesFinder(boolean b) {}

	public void uninstallOccurrencesFinder() {}

	public DotRecord getParseRecord(ITypedRegion region) {
		return parseRecords.get(region);
	}

	public void setParseRecord(DotRecord record) {
		IRegion region = new Region(record.documentOffset, record.length);
		parseRecords.put(region, record);
	}

	@Override
	public void reconciled() {
		Object[] listeners = reconcilingListeners.getListeners();
		for (Object listener : listeners) {
			((IReconcilingListener) listener).reconciled();
		}
	}

	/**
	 * Adds the given listener. Has no effect if an identical listener was not already registered.
	 *
	 * @param listener The reconcile listener to be added
	 */
	public final void addReconcileListener(IReconcilingListener listener) {
		reconcilingListeners.add(listener);
	}

	/**
	 * Removes the given listener. Has no effect if an identical listener was not already registered.
	 *
	 * @param listener the reconcile listener to be removed
	 */
	public final void removeReconcileListener(IReconcilingListener listener) {
		reconcilingListeners.remove(listener);
	}

	protected IWorkbenchPart getActivePart() {
		IWorkbenchWindow window = getSite().getWorkbenchWindow();
		IPartService service = window.getPartService();
		IWorkbenchPart part = service.getActivePart();
		return part;
	}

	@Override
	public ShowInContext getShowInContext() {
		PagePart part = getMatchingPagePart();
		StructuredSelection sel;
		if (part == null) {
			sel = new StructuredSelection();
		} else {
			sel = new StructuredSelection(part);
		}
		return new ShowInContext(getEditorInput(), sel);
	}

	@Override
	public boolean show(ShowInContext context) {
		ISelection selection = context.getSelection();
		if (selection instanceof IStructuredSelection) {
			for (Object element : ((IStructuredSelection) selection).toArray()) {
				if (element instanceof PagePart) {
					PagePart item = (PagePart) element;
					revealPart(item);
					if (isOutlinePageValid()) {
						outlinePage.setSelection(selection);
					}
					return true;
				}
			}
		} else if (selection instanceof ITextSelection) {
			ITextSelection textSel = (ITextSelection) selection;
			selectAndReveal(textSel.getOffset(), textSel.getLength());
			return true;
		}
		return false;
	}

	public void addDocChangeListener(IDocumentChangedListener listener) {
		docListenerList.add(IDocumentChangedListener.class, listener);
	}

	public void removeDocChangeListener(IDocumentChangedListener listener) {
		docListenerList.remove(IDocumentChangedListener.class, listener);
	}

	protected void fireDocumentChanged(DocumentEvent event) {
		IDocumentChangedListener[] listeners;
		synchronized (docListenerList) {
			Object[] src = docListenerList.getListeners(IDocumentChangedListener.class);
			int len = docListenerList.getListenerCount();
			listeners = new IDocumentChangedListener[len];
			System.arraycopy(src, 0, listeners, 0, len);
		}
		for (IDocumentChangedListener listener : listeners) {
			listener.documentChanged(event);
		}
	}

	@Override
	public void perform(AbstractDocumentCommand command) throws CoreException {
		disableSelResponse = true;
		try {
			command.execute(((ITextViewerExtension6) getViewer()).getUndoManager(), getViewer().getDocument());
		} finally {
			disableSelResponse = false;
		}
		selectionChanged();
	}
}
