/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.extensionpoints;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.builders.IMarkerCalculationResourcesVisitor;

/**
 * 
 */
public class MarkerCalculationBuildersManager {
	
	private static final String EXTENSION_POINT_ID_FLUENT_BUILDER = FluentUI.PLUGIN_ID + ".marker.builders";

	private static MarkerCalculationBuildersManager INSTANCE = null;
	
	public static MarkerCalculationBuildersManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MarkerCalculationBuildersManager();
		}
		return INSTANCE;
	}
	
	private final Map<String,IMarkerCalculationResourcesVisitor> builderIdToVisitorMap = new HashMap<>();
	private final Map<String,Set<String>> builderIdToMarkerIdsMap = new HashMap<>();
	private final Map<String,Set<String>> builderIdToNatureIdsMap = new HashMap<>();
	
	private MarkerCalculationBuildersManager() {
		this.init();
	}
	
	private void init() {
		IExtensionPoint builderExtensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID_FLUENT_BUILDER);
		IExtension[] builderExtensions = builderExtensionPoint.getExtensions();
		for (IExtension builderExtension: builderExtensions) {
			IConfigurationElement[] configElements = builderExtension.getConfigurationElements();
			
			try {
				for (IConfigurationElement builderConfigElement : configElements) {
					String builderId = builderConfigElement.getAttribute("id");
					Object obj = builderConfigElement.createExecutableExtension("visitorClass");
					if (obj instanceof IMarkerCalculationResourcesVisitor) {
						builderIdToVisitorMap.put(builderId, (IMarkerCalculationResourcesVisitor) obj);
						
						for (IConfigurationElement markerConfigElement : builderConfigElement.getChildren("marker")) {
							String markerId = markerConfigElement.getAttribute("id");
							
							Set<String> markerIds = builderIdToMarkerIdsMap.get(builderId);
							if (markerIds == null) {
								markerIds = new HashSet<>();
								builderIdToMarkerIdsMap.put(builderId, markerIds);
							}
							markerIds.add(markerId);
						}
						
						for (IConfigurationElement natureConfigElement : builderConfigElement.getChildren("projectNature")) {
							String natureId = natureConfigElement.getAttribute("id");
							
							Set<String> natureIds = builderIdToNatureIdsMap.get(builderId);
							if (natureIds == null) {
								natureIds = new HashSet<>();
								builderIdToNatureIdsMap.put(builderId, natureIds);
							}
							natureIds.add(natureId);
						}
					}
				}
			} catch (CoreException e) {
				FluentUI.log(IStatus.ERROR, "Could not load ITypedRegionMarkerCalculator extension", e);
			}
		}
	}
	
	public Set<String> getMarkerCalculationBuilderIdsFromExtensions() {
		return Collections.unmodifiableSet(builderIdToVisitorMap.keySet());
	}
	
	public Map<String,IMarkerCalculationResourcesVisitor> getAllMarkerCalculationResourceVisitors() {
		return Collections.unmodifiableMap(builderIdToVisitorMap);
	}
	
	public Set<String> getMarkersForBuilder(String builderId) {
		Set<String> markerIds = builderIdToMarkerIdsMap.get(builderId);
		if (markerIds != null) {
			return Collections.unmodifiableSet(markerIds);
		}
		return Collections.emptySet();
	}
	
	public IMarkerCalculationResourcesVisitor getMarkerCalculationResourcesVisitorForBuilder(String builderId) {
		return builderIdToVisitorMap.get(builderId);
	}
	
	public Set<String> getProjectNaturesForBuilder(String builderId) {
		Set<String> natureIds = builderIdToNatureIdsMap.get(builderId);
		if (natureIds != null) {
			return Collections.unmodifiableSet(natureIds);
		}
		return Collections.emptySet();
	}
}
