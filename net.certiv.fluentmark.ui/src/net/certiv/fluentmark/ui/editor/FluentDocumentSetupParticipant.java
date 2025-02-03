/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.editor;

import org.eclipse.core.runtime.IPath;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.IDocumentSetupParticipantExtension;
import org.eclipse.core.filebuffers.LocationKind;

import org.eclipse.jface.text.IDocument;

import net.certiv.fluentmark.core.markdown.MarkdownPartitions;
import net.certiv.fluentmark.core.util.FluentPartitioningTools;
import net.certiv.fluentmark.ui.editor.text.MarkdownPartioningTools;

/**
 * Reacts to IDocument changes and allows for calculating partitions or validating and creating (problem) markers, for example. 
 */
public class FluentDocumentSetupParticipant implements IDocumentSetupParticipant, IDocumentSetupParticipantExtension {

	public void setup(IDocument document) {
		setup(document, null, null);
	}

	public void setup(IDocument document, IPath location, LocationKind locationKind) {
		FluentPartitioningTools.setupDocumentPartitioner(
				document,
				MarkdownPartioningTools.getTools().createDocumentPartitioner(),
				MarkdownPartitions.FLUENT_MARKDOWN_PARTITIONING);
	}
	
}
