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

import org.eclipse.jface.text.rules.IPartitionTokenScanner;

import net.certiv.fluentmark.core.extensionpoints.DocumentPartitionersManager;
import net.certiv.fluentmark.core.partitions.AbstractDocumentPartitioner;


public class PlantUmlPartitioner extends AbstractDocumentPartitioner {

	// unique partitioning type
	public final static String FLUENT_PLANTUML_PARTITIONING = "__fluent_plantuml_partitioning";

	// specialized partition content types
	public static final String COMMENT = "__comment";

	public static final String[] LEGAL_TYPES = new String[] { COMMENT };
	
	private static PlantUmlPartitioner INSTANCE = null;
	
	public static PlantUmlPartitioner get() {
		if (INSTANCE == null ) {
			INSTANCE = (PlantUmlPartitioner) DocumentPartitionersManager.getInstance()
					.getDocumentPartitioner(FLUENT_PLANTUML_PARTITIONING);
		}
		return INSTANCE;
	}
	
	@Override
	public String getSupportedPartitioning() {
		return FLUENT_PLANTUML_PARTITIONING;
	}

	@Override
	protected IPartitionTokenScanner createPartitionScanner() {
		return new PlantUmlPartitionScanner();
	}

	@Override
	public String[] getLegalContentTypes() {
		return LEGAL_TYPES;
	}

}
