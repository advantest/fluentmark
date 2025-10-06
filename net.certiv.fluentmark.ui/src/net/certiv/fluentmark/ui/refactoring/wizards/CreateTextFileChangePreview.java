/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Solunar GmbH - Modifications for FluentMark Advantest Edition
 *******************************************************************************/
package net.certiv.fluentmark.ui.refactoring.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.IWorkbenchAdapter;

import net.certiv.fluentmark.core.markdown.partitions.MarkdownPartitioner;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.editor.FluentSimpleSourceViewerConfiguration;
import net.certiv.fluentmark.ui.editor.FluentSourceViewer;
import net.certiv.fluentmark.ui.editor.color.IColorManager;
import net.certiv.fluentmark.ui.refactoring.CreateTextFileChange;

// Implementation based on org.eclipse.jdt.internal.ui.refactoring.CreateTextFileChangePreviewViewer
public class CreateTextFileChangePreview implements IChangePreviewViewer {
	
	private CreateTextFileChangePreviewPane pane;
	private SourceViewer sourceViewer;
	private IPreferenceStore store;
	
	@Override
	public void createControl(Composite parent) {
		pane = new CreateTextFileChangePreviewPane(parent, SWT.BORDER | SWT.FLAT);
		Dialog.applyDialogFont(pane);
		
		store = FluentUI.getDefault().getCombinedPreferenceStore();

		sourceViewer = new FluentSourceViewer(pane, null, null, false,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION,
				store);
		pane.setContent(sourceViewer.getControl());
	}
	
	@Override
	public Control getControl() {
		return pane;
	}

	public void refresh() {
		sourceViewer.refresh();
	}
	
	@Override
	public void setInput(ChangePreviewViewerInput input) {
		Change change = input.getChange();
		
		if (change != null) {
			Object element = change.getModifiedElement();
			if (element instanceof IAdaptable) {
				IAdaptable adaptable= (IAdaptable) element;
				IWorkbenchAdapter workbenchAdapter = adaptable.getAdapter(IWorkbenchAdapter.class);
				if (workbenchAdapter != null) {
					pane.setImageDescriptor(workbenchAdapter.getImageDescriptor(element));
				} else {
					pane.setImageDescriptor(null);
				}
			} else {
				pane.setImageDescriptor(null);
			}
		}
		
		if (!(change instanceof CreateTextFileChange)) {
			sourceViewer.setInput(null);
			pane.setText(""); //$NON-NLS-1$
			return;
		}
		
		CreateTextFileChange textFileChange = (CreateTextFileChange) change;
		pane.setText(textFileChange.getName());
		IDocument document = new Document(textFileChange.getPreview());
		
		IFile file = textFileChange.getModifiedResource();
		String fileExtension = file != null ? file.getFileExtension() : "";
		
		if (file != null) {
			pane.setText(file.getName());
		}
		
		sourceViewer.unconfigure();
		
		if (fileExtension.equals("puml") || fileExtension.equals("md")) {
			MarkdownPartitioner.get().setupDocumentPartitioner(document);
			
			IColorManager colorManager = FluentUI.getDefault().getColorMgr();
			FluentSimpleSourceViewerConfiguration configuration = new FluentSimpleSourceViewerConfiguration(
					colorManager, store, null, MarkdownPartitioner.FLUENT_MARKDOWN_PARTITIONING, false);
			sourceViewer.configure(configuration);
			sourceViewer.getTextWidget().setOrientation(SWT.LEFT_TO_RIGHT);
		} else {
			sourceViewer.configure(new SourceViewerConfiguration());
			sourceViewer.getTextWidget().setOrientation(sourceViewer.getTextWidget().getParent().getOrientation());
		}
		
		sourceViewer.setInput(document);
	}
	
	private static class CreateTextFileChangePreviewPane extends ViewForm {
		
		private Image image;
		private ImageDescriptor imageDescriptor;
	
		public CreateTextFileChangePreviewPane(Composite parent, int style) {
			super(parent, style);
	
			marginWidth= 0;
			marginHeight= 0;
	
			CLabel label= new CLabel(this, SWT.NONE);
			setTopLeft(label);
			
			addDisposeListener(e -> {
				if (image != null) {
					image.dispose();
				}
			});
		}
		
		@Override
		public CLabel getTopLeft() {
			return (CLabel) super.getTopLeft();
		}
		
		public void setText(String label) {
			CLabel cl = getTopLeft();
			cl.setText(label);
			
			Image obsoleteImage = null;
			if (imageDescriptor != null) {
				obsoleteImage = image;
				image = imageDescriptor.createImage();
			} else {
				obsoleteImage = image;
				image = null;
			}
			
			setImage(image);
			
			if (obsoleteImage != null) {
				obsoleteImage.dispose();
			}
		}
	
		
		public void setImageDescriptor(ImageDescriptor imageDescriptor) {
			this.imageDescriptor= imageDescriptor;
		}
		
		public void setImage(Image image) {
			CLabel cl = getTopLeft();
			cl.setImage(image);
		}
	
	}
}
