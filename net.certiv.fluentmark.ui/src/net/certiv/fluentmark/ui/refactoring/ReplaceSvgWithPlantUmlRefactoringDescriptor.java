/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.refactoring;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class ReplaceSvgWithPlantUmlRefactoringDescriptor extends RefactoringDescriptor {

	protected ReplaceSvgWithPlantUmlRefactoringDescriptor(String id, String project, String description, String comment,
			int flags) {
		super(id, project, description, comment, flags);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status) throws CoreException {
		// TODO Auto-generated method stub
		return new ReplaceSvgWithPlantUmlRefactoring((IResource) null);
	}

}
