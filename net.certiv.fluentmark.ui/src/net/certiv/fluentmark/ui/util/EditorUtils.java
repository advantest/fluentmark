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
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.ide.IDE;

import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.validation.JavaCodeMemberResolver;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.widgets.Display;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EditorUtils {
	
	private static final String URL_SCHEME_FILE = "file";
	
	private static final JavaCodeMemberResolver javaMemberResolver = new JavaCodeMemberResolver();
	
	
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
			return null;
		}
		
		if (file == null || !file.exists()) {
			FluentUI.log(IStatus.ERROR, String.format("Unable to open workspace file %s. File does not exist.", file));
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
			return null;
		}
		
		if (fileStore == null
				|| !fileStore.fetchInfo().exists()
				|| fileStore.fetchInfo().isDirectory()) {
			FluentUI.log(IStatus.ERROR, String.format("Unable to open workspace-external file %s. File does not exist or is a directory.", fileStore));
			return null;
		}
		
		try {
			return IDE.openEditorOnFileStore(activePage, fileStore);
		} catch (PartInitException e) {
			Log.error(String.format("Could not open file outside Eclipse workspace (path=%s) in default editor", fileStore.getName()), e);
		}
		
		return null;
	}
	
	public static FluentEditor openFileInFluentEditor(IFile file) {
		return openFileInFluentEditor(file, getActiveWorkbenchPage());
	}
	
	public static FluentEditor openFileInFluentEditor(IFile file, IWorkbenchPage activePage) {
		if (activePage == null) {
			FluentUI.log(IStatus.ERROR, String.format("Unable to open file since there is no active workbench page."));
			return null;
		}
		
		if (file == null || !file.exists()) {
			FluentUI.log(IStatus.ERROR, String.format("Unable to open workspace file %s. File does not exist.", file));
			return null;
		}
		
		try {
			return (FluentEditor) IDE.openEditor(activePage, file, FluentEditor.ID);
		} catch (PartInitException e) {
			Log.error(String.format("Could not open file (path=%s) in FluentMark editor", file.getLocation()), e);
		}
			
		return null;
	}
	
	public static FluentEditor openFileInFluentEditor(URI fileUri) {
		return openFileInFluentEditor(fileUri, getActiveWorkbenchPage());
	}
	
	public static FluentEditor openFileInFluentEditor(URI fileUri, IWorkbenchPage activePage) {
		if (activePage == null) {
			FluentUI.log(IStatus.ERROR, String.format("Unable to open file since there is no active workbench page."));
			return null;
		}
		
		if (fileUri == null || fileUri.getPath() == null || !URL_SCHEME_FILE.equals(fileUri.getScheme())) {
			FluentUI.log(IStatus.ERROR, String.format("Unable to open workspace file URI %s. URI or path are empty or illegal URI scheme.", fileUri));
			return null;
		}
		
		try {
			return (FluentEditor) IDE.openEditor(activePage, fileUri, FluentEditor.ID, true);
		} catch (PartInitException e) {
			Log.error(String.format("Could not open file (URI=%s) in FluentMark editor", fileUri.toString()), e);
		}
		
		return null;
	}
	
	public static IEditorPart openFileInJavaEditor(IFile javaFile, String memberReference) {
		if (memberReference == null) {
			throw new IllegalArgumentException();
		}
		
		if (javaFile == null || !javaFile.exists()) {
			FluentUI.log(IStatus.ERROR, String.format("Unable to open Java file %s. File does not exist.", javaFile));
			return null;
		}
		
		IMember member = javaMemberResolver.findJavaMember(javaFile, memberReference);
		
		if (member == null) {
			return null;
		}
		
		return openFileInJavaEditor(member);
	}
	
	public static IEditorPart openFileInJavaEditor(IMember javaElement) {
		if (javaElement == null) {
			FluentUI.log(IStatus.ERROR, "Unable to open Java member in Java file. No member given.");
			return null;
		}
		
		try {
			return JavaUI.openInEditor(javaElement);
		} catch (PartInitException | JavaModelException e) {
			Log.error(String.format("Could not open Java file %s with Java member '%s' in Java editor", javaElement.getResource(), javaElement), e );
		}
		
		return null;
	}
	
	public static IWebBrowser openUriInWebBrowser(URI uri) {
		try {
			return openUrlInWebBrowser(uri.toURL());
		} catch (MalformedURLException e) {
			Log.error(String.format("Could not open URI %s in web browser", uri), e );
		}
		
		return null;
	}
	
	public static IWebBrowser openUrlInWebBrowser(String url) {
		try {
			return openUrlInWebBrowser(new URL(url));
		} catch (MalformedURLException e) {
			Log.error(String.format("Could not open URL %s in web browser", url), e );
		}
		
		return null;
	}
	
	public static IWebBrowser openUrlInWebBrowser(URL url) {
		try {
			// open a Browser (internal or external browser, depending on the user-specific Eclipse preferences)
			IWebBrowser webBrowser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
					IWorkbenchBrowserSupport.LOCATION_BAR
					| IWorkbenchBrowserSupport.NAVIGATION_BAR
					| IWorkbenchBrowserSupport.STATUS
					| IWorkbenchBrowserSupport.AS_VIEW,
					"com.advantest.fluentmark.browser.id",
					"FluentMark browser",
					"Browser instance used by Fluentmark to open any exernal link");
			webBrowser.openURL(url);
			return webBrowser;
		} catch (PartInitException e) {
			Log.error(String.format("Could not open URL %s in web browser", url), e );
		}
		
		return null;
	}
	
}
