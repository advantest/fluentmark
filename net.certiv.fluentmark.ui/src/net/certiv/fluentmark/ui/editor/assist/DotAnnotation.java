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
