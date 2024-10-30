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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.Log;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;

import java.util.ArrayList;
import java.util.List;

public class EditorUtils {
	
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
	
	private static IWorkbenchPage getActiveWorkbenchPage() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null && !workbench.isClosing()) {
			IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
			if (workbenchWindow != null && !workbenchWindow.isClosing()) {
				return workbenchWindow.getActivePage();
			}
		}
		return null;
	}
	
	public static IEditorPart openFileInDefaultEditor(IFile file) {
		return openFileInDefaultEditor(file, getActiveWorkbenchPage());
	}
	
	public static IEditorPart openFileInDefaultEditor(IFile file, IWorkbenchPage activePage) {
		if (activePage == null) {
			FluentUI.log(IStatus.ERROR, String.format("Unable to open file since there is no active workbench page."));
		}
		
		if (file == null || !file.exists()) {
			FluentUI.log(IStatus.WARNING, String.format("Unable to open workspace file %s. File does not exists.", file));
			return null;
		}
		
		try {
			return IDE.openEditor(activePage, file);
		} catch (PartInitException e) {
			Log.error(String.format("Could not open file (path=%s) in default editor", file.getLocation()), e);
		}
		
		return null;
	}
	
	public static IEditorPart openFileInDefaultEditor(IFileStore fileStore) {
		return openFileInDefaultEditor(fileStore, getActiveWorkbenchPage());
	}
	
	public static IEditorPart openFileInDefaultEditor(IFileStore fileStore, IWorkbenchPage activePage) {
		if (activePage == null) {
			FluentUI.log(IStatus.ERROR, String.format("Unable to open file since there is no active workbench page."));
		}
		
		if (fileStore == null || activePage == null
				|| !fileStore.fetchInfo().exists()
				|| fileStore.fetchInfo().isDirectory()) {
			FluentUI.log(IStatus.WARNING, String.format("Unable to open workspace-external file %s. File does not exists or is a directory.", fileStore));
			return null;
		}
		
		try {
			return IDE.openEditorOnFileStore(activePage, fileStore);
		} catch (PartInitException e) {
			Log.error(String.format("Could not open file outside Eclipse workspace (path=%s) in default editor", fileStore.getName()), e);
		}
		
		return null;
	}
	
}
