/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.plantuml.partitions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import net.certiv.fluentmark.core.partitions.FluentPartitioningTools;

public class PlantUmlPartitions {
	
		// unique partitioning type
		public final static String FLUENT_PLANTUML_PARTITIONING = "__fluent_plantuml_partitioning";

		// specialized partition content types
		public static final String COMMENT = "__comment";

		public static final String[] LEGAL_TYPES = new String[] { COMMENT };
		
		private static PlantUmlPartitions INSTANCE = null;
		
		private PlantUmlPartitionScanner partitionScanner;
		
		private PlantUmlPartitions() {}
		
		public static PlantUmlPartitions get() {
			if (INSTANCE == null) {
				INSTANCE = new PlantUmlPartitions();
			}
			return INSTANCE;
		}

		public static String[] getLegalContentTypes() {
			return LEGAL_TYPES;
		}
		
		public ITypedRegion[] computePartitions(IDocument document) {
			return FluentPartitioningTools.computePartitions(document, FLUENT_PLANTUML_PARTITIONING);
		}
		
		private IPartitionTokenScanner getPartitionScanner() {
			if (partitionScanner == null) {
				partitionScanner = new PlantUmlPartitionScanner();
			}
			return partitionScanner;
		}
		
		public IDocumentPartitioner createDocumentPartitioner() {
			IPartitionTokenScanner scanner = getPartitionScanner();
			return new FastPartitioner(scanner, getLegalContentTypes());
		}
		
		public void setupDocumentPartitioner(IDocument document) {
			if (document == null) {
				throw new IllegalArgumentException();
			}
			
			IDocumentPartitioner partitioner = FluentPartitioningTools.getDocumentPartitioner(document, FLUENT_PLANTUML_PARTITIONING);
			
			if (partitioner == null) {
				partitioner = createDocumentPartitioner();
				FluentPartitioningTools.setupDocumentPartitioner(document, partitioner, FLUENT_PLANTUML_PARTITIONING);
			}
		}
}
