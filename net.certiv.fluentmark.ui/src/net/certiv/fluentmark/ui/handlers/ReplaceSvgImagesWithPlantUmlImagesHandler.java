/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.handlers;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.handlers.HandlerUtil;

import net.certiv.fluentmark.ui.refactoring.ReplaceSvgWithPlantUmlRefactoring;
import net.certiv.fluentmark.ui.refactoring.wizards.ReplaceSvgWithPlantUmlWizard;

public class ReplaceSvgImagesWithPlantUmlImagesHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			// ISelection selection = HandlerUtil.getCurrentSelection(event);
			// TODO also handle text selections
			
			IStructuredSelection structuredSelection = HandlerUtil.getCurrentStructuredSelection(event);
			
			if (structuredSelection != null && !structuredSelection.isEmpty()) {
				List<IResource> rootResources = structuredSelection.stream()
					.filter(s -> s instanceof IResource)
					.map(s -> (IResource) s)
					.collect(Collectors.toList());
				
				if (rootResources.isEmpty()) {
					return null;
				}
				
				ReplaceSvgWithPlantUmlRefactoring refactoring = new ReplaceSvgWithPlantUmlRefactoring(rootResources);
				ReplaceSvgWithPlantUmlWizard wizard = new ReplaceSvgWithPlantUmlWizard(refactoring);
				RefactoringWizardOpenOperation refactoringOperation = new RefactoringWizardOpenOperation(wizard);
				
				refactoringOperation.run(HandlerUtil.getActiveShell(event), "Replace SVG images in Markdown with PlantUML images");
			}
		} catch (InterruptedException e) {
			return null; // User action got cancelled
		}
		
		return null;
	}

}
