/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.plantuml.parsing;

public interface PlantUmlConstants {
	
	String UML_START = "@startuml";
	String UML_END   = "@enduml";
	String UML_START_SALT = "@startsalt";
	String UML_END_SALT   = "@endsalt";
	String UML_START_YAML = "@startyaml";
	String UML_END_YAML   = "@endyaml";
	String UML_START_JSON = "@startjson";
	String UML_END_JSON   = "@endjson";
	String UML_START_MINDMAP = "@startmindmap";
	String UML_END_MINDMAP   = "@endmindmap";
	String UML_START_GANTT = "@startgantt";
	String UML_END_GANTT   = "@endgantt";
	String UML_START_WBS = "@startwbs";
	String UML_END_WBS   = "@endwbs";
	
	String DOT_START = "@startdot";
	String DOT_END   = "@enddot";
	
	String[] UML_START_STATEMENTS = {
			UML_START, UML_START_SALT, UML_START_YAML, UML_START_JSON,
			UML_START_MINDMAP, UML_START_GANTT, UML_START_WBS };

}
