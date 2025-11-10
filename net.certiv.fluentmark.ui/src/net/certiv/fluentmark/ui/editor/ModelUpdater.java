/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.editor;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.text.DocumentEvent;

import net.certiv.fluentmark.core.markdown.model.PageRoot;
import net.certiv.fluentmark.core.markdown.model.UpdateJob;

public class ModelUpdater implements IDocumentChangedListener {
	
	private final PageRoot pageRoot;
	private final IResource resource;
	private final UpdateJob modelUpdateJob = new UpdateJob("Model updater");
	
	public ModelUpdater(PageRoot pageRoot, IResource resource) {
		this.pageRoot = pageRoot;
		this.resource = resource;
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		modelUpdateJob.trigger(this.pageRoot, this.resource, event.getDocument());
	}

}
