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

import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class PlantUmlPartitioningTools {
	
	private static PlantUmlPartitioningTools INSTANCE = null;
	
	private PlantUmlPartitionScanner partitionScanner;
	
	private PlantUmlPartitioningTools() {}
	
	public static PlantUmlPartitioningTools getTools() {
		if (INSTANCE == null) {
			INSTANCE = new PlantUmlPartitioningTools();
		}
		return INSTANCE;
	}
	
	private IPartitionTokenScanner getPartitionScanner() {
		if (partitionScanner == null) {
			partitionScanner = new PlantUmlPartitionScanner();
		}
		return partitionScanner;
	}
	
	public IDocumentPartitioner createDocumentPartitioner() {
		IPartitionTokenScanner scanner = getPartitionScanner();
		return new FastPartitioner(scanner, PlantUmlPartitions.getLegalContentTypes());
	}
}
