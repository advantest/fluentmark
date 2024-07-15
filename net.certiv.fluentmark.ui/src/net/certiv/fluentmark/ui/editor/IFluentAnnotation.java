/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.editor;

import java.util.Iterator;

/** Represents markers and problems. */
public interface IFluentAnnotation {

	String PROBLEM = "fluentmark.problem"; //$NON-NLS-1$
	String TASK = "fluentmark.task"; //$NON-NLS-1$
	String ERROR = "fluentmark.error"; //$NON-NLS-1$
	String WARNING = "fluentmark.warning"; //$NON-NLS-1$
	String INFO = "fluentmark.info"; //$NON-NLS-1$

	String getType();

	boolean isPersistent();

	boolean isMarkedDeleted();

	String getText();

	/**
	 * Returns whether this annotation is overlaid.
	 *
	 * @return <code>true</code> if overlaid
	 */
	boolean hasOverlay();

	/**
	 * Returns the overlay of this annotation.
	 *
	 * @return the annotation's overlay
	 */
	IFluentAnnotation getOverlay();

	/**
	 * Returns an iterator for iterating over the annotation which are overlaid by this annotation.
	 *
	 * @return an iterator over the overlaid annotations
	 */
	Iterator<IFluentAnnotation> getOverlaidIterator();

	/**
	 * Adds the given annotation to the list of annotations which are overlaid by this annotations.
	 *
	 * @param annotation the problem annotation
	 */
	void addOverlaid(IFluentAnnotation annotation);

	/**
	 * Removes the given annotation from the list of annotations which are overlaid by this annotation.
	 *
	 * @param annotation the problem annotation
	 */
	void removeOverlaid(IFluentAnnotation annotation);

	/**
	 * Tells whether this annotation is a problem annotation.
	 *
	 * @return <code>true</code> if it is a problem annotation
	 */
	boolean isProblem();

	/**
	 * Returns the problem arguments or <code>null</code> if no problem arguments can be evaluated.
	 *
	 * @return returns the problem arguments or <code>null</code> if no problem arguments can be
	 *         evaluated.
	 */
	String[] getArguments();

	/**
	 * Returns the problem id or <code>-1</code> if no problem id can be evaluated.
	 *
	 * @return returns the problem id or <code>-1</code>
	 */
	int getId();

	/**
	 * Returns the marker type associated to this problem or <code>null<code> if no marker type can be
	 * evaluated. See also {@link org.eclipse.cdt.ui.text.IProblemLocation#getMarkerType()}.
	 *
	 * @return the type of the marker which would be associated to the problem or <code>null<code> if no
	 *         marker type can be evaluated.
	 */
	String getMarkerType();
}
