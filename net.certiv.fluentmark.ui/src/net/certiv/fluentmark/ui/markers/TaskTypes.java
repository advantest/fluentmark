/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.markers;

import net.certiv.fluentmark.ui.FluentUI;

public interface TaskTypes {

	String PREFIX = FluentUI.PLUGIN_ID + ".validation.task.";
	String MARKDOWN_TASK = PREFIX + "markdown";
	String PLANTUML_TASK = PREFIX + "plantuml";
	
}
