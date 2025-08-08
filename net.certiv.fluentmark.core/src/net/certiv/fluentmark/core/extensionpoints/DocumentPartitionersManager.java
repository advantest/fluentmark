/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.extensionpoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.partitions.IFluentDocumentPartitioner;
import net.certiv.fluentmark.core.util.ExtensionsUtil;

public class DocumentPartitionersManager {
	
	private static final String EXTENSION_POINT_ID_DOCUMENT_PARTITIONERS = FluentCore.PLUGIN_ID + ".partitioner";

	private static DocumentPartitionersManager INSTANCE = null;
	
	public static DocumentPartitionersManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DocumentPartitionersManager();
		}
		return INSTANCE;
	}
	
	private final List<IFluentDocumentPartitioner> partitioners = new ArrayList<>();
	
	private DocumentPartitionersManager() {
		this.init();
	}
	
	public List<IFluentDocumentPartitioner> getDocumentPartitioners() {
		return Collections.unmodifiableList(partitioners);
	}
	
	public IFluentDocumentPartitioner getDocumentPartitioner(String partitioning) {
		if (partitioning == null) {
			throw new IllegalArgumentException();
		}
		
		return partitioners.stream()
			.filter(partitioner -> partitioning.equals(partitioner.getSupportedPartitioning()))
			.findFirst()
			.orElse(null);
	}
	
	private void init() {
		partitioners.addAll(
				ExtensionsUtil.createExecutableExtensionsFor(
						EXTENSION_POINT_ID_DOCUMENT_PARTITIONERS, 
						IFluentDocumentPartitioner.class));
	}
}
