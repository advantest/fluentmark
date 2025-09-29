/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.plantuml.parsing;

import net.certiv.fluentmark.core.util.FlexmarkUtil;

/**
 * @see FlexmarkUtil
 */
public class PlantUmlParsingTools {
	
	public static int getNumberOfDiagrams(String plantUmlCode) {
		if (plantUmlCode == null || plantUmlCode.isBlank()) {
			return 0;
		}
		
		int numberOfDiagrams = 0;
		int indexOfStart = plantUmlCode.indexOf(PlantUmlConstants.PREFIX_START);
		int indexOfEnd = -1;
		
		while (indexOfStart >= 0 && indexOfStart + 1 < plantUmlCode.length()) {
			indexOfEnd = plantUmlCode.indexOf(PlantUmlConstants.PREFIX_END, indexOfStart + PlantUmlConstants.PREFIX_START.length());
			if (indexOfEnd >= 0) {
				numberOfDiagrams++;
				
				indexOfStart = plantUmlCode.indexOf(PlantUmlConstants.PREFIX_START, indexOfEnd + PlantUmlConstants.PREFIX_END.length());
			} else {
				break;
			}
			
		}
		
		return numberOfDiagrams;
	}

}
