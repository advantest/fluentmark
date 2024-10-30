/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2024 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.util;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.core.resources.IFile;

import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.List;

public class EditorsUtils {
	
	public static <T extends IEditorPart> T findDirtyEditorFor(Class<T> editorType, IFile file) {
		if (file == null) {
			return null;
		}
		
		List<T> dirtyEditors = getDirtyEditors(editorType);
		
		for (T editor: dirtyEditors) {
			IEditorInput editorInput = editor.getEditorInput();
			IFile inputFile = editorInput.getAdapter(IFile.class);
			
			if (inputFile != null && inputFile.equals(file)) {
				return editor;
			}
		}
		
		return null;
	}

	private static <T extends IEditorPart> List<T> getDirtyEditors(Class<T> editorType) {
		if (editorType == null) {
			throw new IllegalArgumentException();
		}
		
		final List<T> dirtyEditorsFound = new ArrayList<>();
		
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		
		if (display == null) {
			return dirtyEditorsFound;
		}
		
		display.syncExec(new Runnable() {

			@Override
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				if (workbench != null && !workbench.isClosing()) {
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					if (window != null && !window.isClosing()) {
						IWorkbenchPage page = window.getActivePage();
						if (page != null) {
							IEditorPart[] dirtyEditors = page.getDirtyEditors();
							for (IEditorPart editor : dirtyEditors) {
								if (editorType.isInstance(editor)) {
									T dirtyEditor = editorType.cast(editor);
									dirtyEditorsFound.add(dirtyEditor);
								}
							}
						}
					}
				}
			}
			
		});
		
		return dirtyEditorsFound;
	}
	
}
