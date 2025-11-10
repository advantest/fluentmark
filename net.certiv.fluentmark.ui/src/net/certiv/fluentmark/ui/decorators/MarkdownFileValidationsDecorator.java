/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.decorators;

import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.builders.MarkerCalculatingFileValidationBuilder;


public class MarkdownFileValidationsDecorator implements ILightweightLabelDecorator {
	
	public static final String DECORATOR_ID = "com.advantest.fluentmark.extensions.validations.decorator";
	
	private static final Optional<ImageDescriptor> OVERLAY_ICON = ResourceLocator.imageDescriptorFromBundle(
			FluentUI.PLUGIN_ID,
			"icons/obj16/fluentmark-decorator.png");

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {
		// no resources to dispose
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (!(element instanceof IProject)) {
			return;
		}
		
		IProject project = (IProject) element;
		if (MarkerCalculatingFileValidationBuilder.hasBuilder(project)) {
			OVERLAY_ICON.ifPresent(img -> decoration.addOverlay(img, IDecoration.TOP_LEFT));
		}
	}
	
}
