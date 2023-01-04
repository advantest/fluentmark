/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2022 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.views;

import org.eclipse.ui.ide.IDE;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;

import java.util.ArrayList;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.io.File;

import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.editor.FluentEditor;

public class FluentBrowserUrlListener implements LocationListener {

	private static final String FILE_EXTENSION_MARKDOWN = "md";
	private static final String FILE_EXTENSION_JAVA = "java";
	private static final String URL_SCHEME_ABOUT = "about";
	private static final String URL_SCHEME_FILE = "file";
	private static final String URL_SCHEME_SPECIFIC_PART_BLANK = "blank";
	private static final String URL_ABOUT_BLANK = URL_SCHEME_ABOUT + ":" + URL_SCHEME_SPECIFIC_PART_BLANK;
	
	private final FluentPreview preview;
	private final ViewJob viewJob;
	
	FluentBrowserUrlListener(FluentPreview preview, ViewJob viewJob) {
		this.preview = preview;
		this.viewJob = viewJob;
	}
	
	@Override
	public void changing(LocationEvent event) {
		String url = event.location;

		if (url == null || isRenderedPageUrl(url)) {
			// We're only opening the preview for a certain source file, we do not follow a link.
			// --> Nothing to do in this case.
			return;
		}

		URI targetUri = translateUrlToUri(url);
		if (targetUri == null) {
			// open URL in a separate browser if it is not  URI
			event.doit = false;
			openUrlInSeparateWebBrowser(url);
			return;
		}
		
		if (isRenderedPageUriWithAnchor(targetUri)) {
			// Do nothing if the anchor is on the page we're rendering / viewing.
			// Just open the link, which was fixed in a previous step
			return;
		}
		
		// open links to non-files in a separate web browser
		if (!hasFileScheme(targetUri)) {
			event.doit = false;
			openUriInSeparateWebBrowser(targetUri);
			return;
		}
		
		
		IFile fileInWorkspace = toWorkspaceRelativeFile(targetUri);
		
		// open non-Markdown files in default editor
		if (fileInWorkspace != null) {
			if (!hasMarkdownFileExtension(fileInWorkspace)) {
				event.doit = false;
				
				// Java file with link to a file member (method or field)?
				if (hasFileExtension(fileInWorkspace, FILE_EXTENSION_JAVA)
						&& isLinkToAnchor(targetUri)) {
					
					IEditorPart javaEditor = openJavaFileAndMemberInJavaEditor(fileInWorkspace, targetUri);
					if (javaEditor != null) {
						return;
					}
				}
				
				openFileInDefaultEditor(fileInWorkspace);
				return;
			}
		} else {
			URI targetFileUriWithoutAnchor = omitAnchor(targetUri);
			IFileStore fileOutsideWorkspace = EFS.getLocalFileSystem().getStore(targetFileUriWithoutAnchor);
			if (!hasMarkdownFileExtension(fileOutsideWorkspace)) {
				event.doit = false;
				openFileInDefaultEditor(fileOutsideWorkspace);
				return;
			}
		}
		
		
		// We definitely have a Markdown file
		
		
		if (isLinkToAnchor(targetUri)
				&& isLinkToFileAlreadyOpenInFluentmarkEditor(targetUri)) {
			// We're previewing the Markdown file currently open in editor
			// no reload / rendering necessary, just scroll to the anchor
			// call JavaScript function for scrolling to the anchor
			event.doit = false;
			this.viewJob.scrollTo(targetUri.getFragment());
			return;
		}
		
		// open Markdown file in our Fluentmark editor
		FluentEditor editor = null;
		if (fileInWorkspace != null) {
			editor = openFluentEditorWith(fileInWorkspace);
		} else {
			// file outside Eclipse workspace
			editor = openFluentEditorWith(omitAnchor(targetUri));
		}
		
		if (editor != null) {
			// update preview contents due to a new Markdown file in focus / opened in editor
			viewJob.load();
			// we do not abort the event, since the URL should be opened by our preview browser
			
			// remember the anchor to scroll to, before loading the new Markdown file
			// (and changing the URL to "about:blank", i.e. loosing the anchor in the URL)
			if (isLinkToAnchor(targetUri)) {
				viewJob.setAnchorForNextPageLoad(targetUri.getFragment());
			}
		} else {
			// abort preview refresh if we could not open the file in editor
			event.doit = false;
		}
	}

	@Override
	public void changed(LocationEvent event) {
		// do nothing (yet)
	}

	private boolean isRenderedPageUrl(String url) {
		if (url != null && url.equals(URL_ABOUT_BLANK)) {
			return true;
		}
		return false;
	}
	
	private boolean isRenderedPageUriWithAnchor(URI uri) {
		return (uri != null
			&& URL_SCHEME_ABOUT.equals(uri.getScheme())
			&& URL_SCHEME_SPECIFIC_PART_BLANK.equals(uri.getSchemeSpecificPart())
			&& isLinkToAnchor(uri));
	}
	
	private boolean hasFileScheme(URI uri) {
		return (uri != null
				&& uri.getScheme() != null
				&& URL_SCHEME_FILE.equals(uri.getScheme()));
	}
	
	private boolean hasFileExtension(IFile file, String fileExtension) {
		return (file != null && fileExtension != null
				&& file.getFileExtension() != null
				&& fileExtension.equalsIgnoreCase(file.getFileExtension()));
	}
	
	private boolean hasMarkdownFileExtension(IFile file) {
		return hasFileExtension(file, FILE_EXTENSION_MARKDOWN);
	}
	
	private boolean hasMarkdownFileExtension(IFileStore fileStore) {
		if (fileStore == null) {
			return false;
		}
		
		int lastPointIndex = fileStore.getName().lastIndexOf('.');
		if (lastPointIndex < 0) {
			return false;
		}
		
		String fileExtension = fileStore.getName().substring(lastPointIndex + 1);
		
		return FILE_EXTENSION_MARKDOWN.equalsIgnoreCase(fileExtension);
	}
	
	private URI translateUrlToUri(String url) {
		if (url == null || url.length() == 0) {
			return null;
		}
		
		try {
			return new URI (url);
		} catch (URISyntaxException e) {
			// ignore malformed URI (remember: URIs are always URLs, but URLs are not always URIs)
		}
		
		return null;
	}
	
	private IWebBrowser openUriInSeparateWebBrowser(URI uri) {
		try {
			return openUrlInSeparateWebBrowser(uri.toURL());
		} catch (MalformedURLException e) {
			Log.error(String.format("Could not open URI %s in web browser", uri), e );
		}
		
		return null;
	}
	
	private IWebBrowser openUrlInSeparateWebBrowser(String url) {
		try {
			return openUrlInSeparateWebBrowser(new URL(url));
		} catch (MalformedURLException e) {
			Log.error(String.format("Could not open URL %s in web browser", url), e );
		}
		
		return null;
	}
	
	private IWebBrowser openUrlInSeparateWebBrowser(URL url) {
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
	
	private URI omitAnchor(URI uri) {
		if (uri == null || uri.getFragment() == null) {
			return uri;
		}
		
		// remove trailing fragment & hashtag
		String uriString = uri.toString();
		int index = uriString.indexOf(uri.getRawFragment());
		uriString = uriString.substring(0, index - 1);
		
		return URI.create(uriString);
	}
	
	private IFile toWorkspaceRelativeFile(URI uri) {
		if (uri == null || uri.getPath() == null || !URL_SCHEME_FILE.equals(uri.getScheme())) {
			return null;
		}
		
		URI adaptedUri = omitAnchor(uri);
		
		File file = null;
		try {
			file = new File(adaptedUri);
		} catch (Exception e) {
			return null;
		}
		 
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		
		IFile[] filesFound = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(adaptedUri);
		if (filesFound.length == 1) {
			return filesFound[0];
		}
		
		return null;
	}
	
	private FluentEditor openFluentEditorWith(IFile file) {
		if (file == null) {
			return null;
		}
		
		IWorkbenchPage activePage = preview.getActivePage();
		if (activePage != null) {
			try {
				return (FluentEditor) IDE.openEditor(activePage, file, FluentEditor.ID);
			} catch (PartInitException e) {
				Log.error(String.format("Could not open file (path=%s) in FluentMark editor", file.getLocation()), e);
			}
		}
			
		return null;
	}
	
	private FluentEditor openFluentEditorWith(URI fileUri) {
		if (fileUri == null || fileUri.getPath() == null || !URL_SCHEME_FILE.equals(fileUri.getScheme())) {
			return null;
		}
		
		IWorkbenchPage activePage = preview.getActivePage();
		if (activePage != null) {
			try {
				return (FluentEditor) IDE.openEditor(activePage, fileUri, FluentEditor.ID, true);
			} catch (PartInitException e) {
				Log.error(String.format("Could not open file outside Eclipse workspace (URI=%s) in FluentMark editor", fileUri.toString()), e);
			}
		}
			
		return null;
	}
	
	private IEditorPart openFileInDefaultEditor(IFile file) {
		if (file == null) {
			return null;
		}
		
		IWorkbenchPage activePage = preview.getActivePage();
		if (activePage != null) {
			try {
				return IDE.openEditor(activePage, file);
			} catch (PartInitException e) {
				Log.error(String.format("Could not open file (path=%s) in default editor", file.getLocation()), e);
			}
		}
		
		return null;
	}
	
	private IEditorPart openFileInDefaultEditor(IFileStore fileStore) {
		if (fileStore == null
				|| !fileStore.fetchInfo().exists()
				|| fileStore.fetchInfo().isDirectory()) {
			return null;
		}
		
		IWorkbenchPage activePage = preview.getActivePage();
		if (activePage != null) {
			try {
				return IDE.openEditorOnFileStore(activePage, fileStore);
			} catch (PartInitException e) {
				Log.error(String.format("Could not open file outside Eclipse workspace (path=%s) in default editor", fileStore.getName()), e);
			}
		}
		
		return null;
	}
	
	private boolean isLinkToAnchor(URI targetUri) {
		// fragment is something like #some-section at the end of the URI, i.e. an anchor ID
		return (targetUri != null && targetUri.getFragment() != null);
	}
	
	private boolean isLinkToFileAlreadyOpenInFluentmarkEditor(URI targetUri) {
		if (targetUri == null || preview.getCurrentEditorInput() == null) {
			return false;
		}
		
		if (preview.getCurrentEditorInput() instanceof IURIEditorInput) {
			URI currentUri = ((IURIEditorInput) preview.getCurrentEditorInput()).getURI();
			String currentPath = currentUri.getPath();
			String targetPath = targetUri.getPath();
			return currentPath != null && targetPath != null && targetPath.equals(currentPath);
		} else if (preview.getCurrentEditorInput() instanceof IPathEditorInput) {
			IPath currentPath = ((IPathEditorInput) preview.getCurrentEditorInput()).getPath();
			String targetPath = targetUri.getPath();
			
			return (targetPath != null
					&& currentPath != null
					&& currentPath.toPortableString().equals(targetPath));
		}
		
		return false;
	}
	
	private IEditorPart openJavaFileAndMemberInJavaEditor(IFile javaFile, URI fileUriWithMemberReference) {
		IJavaElement element = JavaCore.create(javaFile);
		
		if (element == null || !(element instanceof ICompilationUnit) || fileUriWithMemberReference.getFragment() == null) {
			return null;
		}
		
		ICompilationUnit compilationUnit = (ICompilationUnit) element;
		
		// URI part following the '#'
		String memberReference = fileUriWithMemberReference.getFragment();
		
		IType primaryType = compilationUnit.findPrimaryType();
		
		if (primaryType == null) {
			return null;
		}
		
		if (memberReference.contains("(")) {
			// memberReference is in this case the method signature, e.g. "doSomething(String, boolean, int)"
			IMethod method = findMethod(primaryType, memberReference);
			
			if (method == null) {
				return null;
			}
			
			try {
				return JavaUI.openInEditor((IJavaElement) method);
			} catch (PartInitException | JavaModelException e) {
				Log.error(String.format("Could not open type and method %s in web browser", fileUriWithMemberReference), e );
			}
			
		} else {
			// memberReference is in this case the field name
			IField field = primaryType.getField(memberReference);
			
			if (!field.exists()) {
				return null;
			}
			
			try {
				return JavaUI.openInEditor((IJavaElement) field);
			} catch (PartInitException | JavaModelException e) {
				Log.error(String.format("Could not open type and field %s in web browser", fileUriWithMemberReference), e );
			}
		}
		
		return null;
	}
	
	private IMethod findMethod(IType parentType, String methodSignature) {
		String[] signatureParts = methodSignature.split("[\\(\\)]");
		String methodName = signatureParts[0];
		String methodParametersPart = signatureParts.length > 1 ? signatureParts[1] : "";
		//String[] methodParameters = methodParametersPart.split(",\\s*");
		String[] methodParameters = parseMethodParameters(methodParametersPart);
		int numParams = methodParameters.length;
		
		try {
			for (IMethod method: parentType.getMethods()) {
				if (method.getElementName().equals(methodName)
						&& method.getNumberOfParameters() == numParams) {
					
					String[] parameterTypes = method.getParameterTypes();
					boolean equalParameterTypes = true;
					for (int i = 0; equalParameterTypes && i < parameterTypes.length; i++) {
						String simpleType = Signature.getSignatureSimpleName(parameterTypes[i]);
						if (!methodParameters[i].equals(simpleType)) {
							equalParameterTypes = false;
						}
					}
					
					if (equalParameterTypes) {
						return method;
					}
				}
			}
		} catch (JavaModelException e) {
			Log.log(IStatus.WARNING, -1, "Could not access methods in type " + parentType.getFullyQualifiedName(), e);
		}
		
		return null;
	}
	
	/*
	 * Parses parameter type lists like
	 * (int, boolean, Character[], List<Map<K,V>>)
	 * 
	 * Such list are common in method signatures as the are used in Javadoc links, e.g.
	 * {@link ClassName#getSomething(int, boolean, Character[], List<Map<K,V>>)}
	 * 
	 * We use the same syntax in Markdown to point to a certain method in a Java class.
	 * 
	 * String.split(",") does not work with string like in the example above, since commas
	 * are also contained in parameter types like List<Map<K,V>>. Thus, we have to
	 * do a more complicated parsing of the parameter list.
	 */
	private String[] parseMethodParameters(String methodParametersList) {
		ArrayList<String> parameters = new ArrayList<>();
		int pos = 0;
		int begin = 0;
		int countTypeParamBrackets = 0;
		String param;
		
		while (pos < methodParametersList.length()) {
			char currentChar = methodParametersList.charAt(pos);
			
			if (currentChar == ','
					&& countTypeParamBrackets == 0) {
				if (pos == 0) {
					return null;
				}
				
				param = methodParametersList.substring(begin, pos);
				parameters.add(param);
				begin = pos + 1;
			} else if (currentChar == ' '
					&& countTypeParamBrackets == 0) {
				begin = pos +1;
			} else if (currentChar == '<') {
				countTypeParamBrackets++;
			} else if (currentChar == '>') {
				countTypeParamBrackets--;
			}
			
			if (countTypeParamBrackets < 0) {
				return null;
			}
			
			pos++;
		}
		
		if (begin < methodParametersList.length()) {
			param = methodParametersList.substring(begin, methodParametersList.length());
			parameters.add(param);
		}
		
		return parameters.toArray(new String[parameters.size()]);
	}
	
}