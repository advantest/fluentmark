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

public class PlantUmlParsingTools {
	
	public static int getNumberOfDiagrams(String plantUmlCode) {
		if (plantUmlCode == null || plantUmlCode.isBlank()) {
			return 0;
		}
		
		int numberOfDiagrams = 0;
		int indexOfStart = plantUmlCode.indexOf(PlantUmlConstants.PREFIX_START);
		
		while (indexOfStart >= 0 && indexOfStart + 1 < plantUmlCode.length()) {
			numberOfDiagrams++;
			indexOfStart = plantUmlCode.indexOf(PlantUmlConstants.PREFIX_START, indexOfStart + 1);
		}
		
		return numberOfDiagrams;
	}

}
