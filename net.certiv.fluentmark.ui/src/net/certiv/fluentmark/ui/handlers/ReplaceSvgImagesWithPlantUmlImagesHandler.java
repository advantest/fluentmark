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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.handlers.HandlerUtil;

import net.certiv.fluentmark.ui.refactoring.ReplaceSvgWithPlantUmlRefactoring;
import net.certiv.fluentmark.ui.refactoring.wizards.ReplaceSvgWithPlantUmlWizard;

public class ReplaceSvgImagesWithPlantUmlImagesHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			IResource rootResource = null;
			
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			IStructuredSelection structuredSelection = HandlerUtil.getCurrentStructuredSelection(event);
			
			if (structuredSelection != null && !structuredSelection.isEmpty()) {
				// TODO handle all selected elements
				if (structuredSelection.getFirstElement() instanceof IResource) {
					rootResource = (IResource) structuredSelection.getFirstElement();
				}
			}
			
			ReplaceSvgWithPlantUmlRefactoring refactoring = new ReplaceSvgWithPlantUmlRefactoring(rootResource);
			ReplaceSvgWithPlantUmlWizard wizard = new ReplaceSvgWithPlantUmlWizard(refactoring);
			RefactoringWizardOpenOperation refactoringOperation = new RefactoringWizardOpenOperation(wizard);
			
			int result = refactoringOperation.run(HandlerUtil.getActiveShell(event), "Replace SVG images in Markdown with PlantUML images");
			
			RefactoringStatus status = refactoringOperation.getInitialConditionCheckingStatus();
			if (result == IDialogConstants.CANCEL_ID || result == RefactoringWizardOpenOperation.INITIAL_CONDITION_CHECKING_FAILED) {
				// TODO Do we need to re-cover here and start a re-build?
			}
		} catch (InterruptedException e) {
			return null; // User action got cancelled
		}
		
		return null;
	}

}
