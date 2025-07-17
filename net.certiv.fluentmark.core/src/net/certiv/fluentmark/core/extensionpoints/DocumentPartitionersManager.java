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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.partitions.IDocumentPartitioner;

public class DocumentPartitionersManager {
	
	private static final String EXTENSION_POINT_ID_DOCUMENT_PARTITIONERS = "net.certiv.fluentmark.core.partitioners";

	private static DocumentPartitionersManager INSTANCE = null;
	
	public static DocumentPartitionersManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DocumentPartitionersManager();
		}
		return INSTANCE;
	}
	
	private final List<IDocumentPartitioner> partitioners = new ArrayList<>();
	
	private DocumentPartitionersManager() {
		this.init();
	}
	
	public List<IDocumentPartitioner> getDocumentPartitionerss() {
		return Collections.unmodifiableList(partitioners);
	}
	
	private void init() {
		IExtensionPoint partitionersExtensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID_DOCUMENT_PARTITIONERS);
		IExtension[] partitionerExtensions = partitionersExtensionPoint.getExtensions();
		for (IExtension partitionerExtension: partitionerExtensions) {
			IConfigurationElement[] configElements = partitionerExtension.getConfigurationElements();
			
			try {
				for (IConfigurationElement configElement : configElements) {
					Object obj = configElement.createExecutableExtension("class");
					if (obj instanceof IDocumentPartitioner) {
						partitioners.add((IDocumentPartitioner) obj);
					}
				}
			} catch (CoreException e) {
				FluentCore.log(IStatus.ERROR, "Could not load IDocumentPartitioner from extension", e);
			}
		}
	}
}
