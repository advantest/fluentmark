/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.plantuml.partitions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.partitions.FluentPartitioningTools;
import net.certiv.fluentmark.core.partitions.IFluentDocumentPartitioner;


public class PlantUmlPartitioner implements IFluentDocumentPartitioner {

	@Override
	public String getSupportedPartitioning() {
		return PlantUmlPartitions.FLUENT_PLANTUML_PARTITIONING;
	}

	@Override
	public void setupDocumentPartitioner(IDocument document, IFile file) {
		if (document == null) {
			throw new IllegalArgumentException();
		}
		
		org.eclipse.jface.text.IDocumentPartitioner partitioner = FluentPartitioningTools.getDocumentPartitioner(document, PlantUmlPartitions.FLUENT_PLANTUML_PARTITIONING);
		if (partitioner == null) {
			partitioner = PlantUmlPartitioningTools.getTools().createDocumentPartitioner();
			FluentPartitioningTools.setupDocumentPartitioner(document, partitioner, PlantUmlPartitions.FLUENT_PLANTUML_PARTITIONING);
		}
	}

	@Override
	public ITypedRegion[] computePartitioning(IDocument document, IFile file) {
		return PlantUmlPartitions.computePartitions(document);
	}
	
}
