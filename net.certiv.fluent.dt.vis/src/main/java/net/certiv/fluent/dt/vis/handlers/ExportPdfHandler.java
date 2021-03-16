/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluent.dt.vis.handlers;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import net.certiv.dsl.core.model.ICodeUnit;
import net.certiv.dsl.core.util.Resources;
import net.certiv.fluent.dt.ui.FluentUI;
import net.certiv.fluent.dt.ui.editor.FluentEditor;
import net.certiv.fluent.dt.vis.convert.PdfGen;

public class ExportPdfHandler extends AbstractHandler {

	private String template;
	private String destination;

	public ExportPdfHandler() {
		super();
	}

	public void setTemplate(String pathname) {
		this.template = pathname;
	}

	public void setDestination(String pathname) {
		this.destination = pathname;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart active = HandlerUtil.getActiveEditor(event);
		if (active instanceof FluentEditor) {
			FluentEditor editor = (FluentEditor) active;
			Shell shell = HandlerUtil.getActiveShell(event);

			ICodeUnit unit = editor.getInputDslElement();
			if (unit != null) {
				IFile file = unit.getFile();

				ExportPdfDialog dialog = new ExportPdfDialog(shell, this, file);
				if (dialog.open() == 0) { // 0: generate; 1: cancel; -1: close
					PdfGen.save(unit, template, destination);

					IPath location = FluentUI.getDefault().getStateLocation();
					Map<String, String> map = Resources.getTemplateMap(location);
					map.put(file.getFullPath().toString(), template); // by WS relative file pathname
					map.put(file.getName(), template); // by file filename
					Resources.putTemplateMap(location, map);
				}
			}
		}
		return null;
	}
}
