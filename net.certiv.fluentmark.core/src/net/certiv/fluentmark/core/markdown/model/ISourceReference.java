/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.markdown.model;


public interface ISourceReference {

	/**
	 * Returns the source code associated with this element.
	 * 
	 * @return the source code, or <code>null</code> if this element has no associated source code
	 */
	String getContent();

	/**
	 * Returns the source range associated with this element.
	 * 
	 * @return the source range
	 */
	ISourceRange getSourceRange();

	/**
	 * Returns the content of this element with lines separated by EOLs. The content is not
	 * terminated by an EOL if <code>noTerm</code> is true. Otherwise, the content is terminated by
	 * an EOL.
	 */
	String getContent(boolean noTerm);
}
