/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.handlers;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import net.certiv.fluentmark.ui.handlers.dialog.SourceCodeDialog;
import net.certiv.fluentmark.ui.views.FluentPreview;

public class ShowHtmlSourceCodeHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof FluentPreview) {
			FluentPreview view = (FluentPreview) part;
			String pageSourceCode = view.getBrowser().getText();
			String contents = null;
			
			if ("webkit".equals(view.getBrowser().getBrowserType())) {
				contents = view.getHtmlViewContents();
			}
			
			SourceCodeDialog dialog = new SourceCodeDialog(view.getSite().getShell(), "HTML Source Code", pageSourceCode, contents);
			dialog.open();
		}
		return null;
	}

}
