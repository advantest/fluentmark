/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.editor.assist;

import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;

import net.certiv.fluentmark.core.marker.DotProblem;

public class DotAnnotation extends SimpleMarkerAnnotation implements IQuickFixableAnnotation {

	private DotProblem problem;

	public DotAnnotation(DotProblem problem) {
		super(problem.getMarker());
		this.problem = problem;
	}

	public DotProblem getProblem() {
		return problem;
	}

	public void deleteMarker() {
		problem.deleteMarker();
	}

	@Override
	public boolean isQuickFixable() {
		return false;
	}

	@Override
	public boolean isQuickFixableStateSet() {
		return true;
	}

	@Override
	public void setQuickFixable(boolean state) {}
}
