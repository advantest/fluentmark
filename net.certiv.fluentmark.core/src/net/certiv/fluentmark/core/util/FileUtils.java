/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.osgi.framework.Bundle;

import net.certiv.fluentmark.core.FluentCore;


public final class FileUtils {
	
	public static final String FILE_EXTENSION_MARKDOWN = "md";
	public static final String FILE_EXTENSION_PLANTUML = "puml";
	public static final String FILE_EXTENSION_SVG = "svg";
	public static final String FILE_EXTENSION_JAVA = "java";
	
	public static final String PROJECT_NATURE_JAVA = "org.eclipse.jdt.core.javanature";
	public static final String PROJECT_NATURE_C = "org.eclipse.cdt.core.cnature";
	public static final String PROJECT_NATURE_CPP = "org.eclipse.cdt.core.ccnature";
	
	private static final String operatingSystemName = System.getProperty("os.name");
	private static final OperatingSystem operatingSystem = getOs();
	
	private enum OperatingSystem {
		LinuxOrUnix,
		MacOs,
		Windows,
		Other
	}

	/**
	 * Creates a file resource handle for the file with the given workspace path. This method does not
	 * create the file resource; this is the responsibility of <code>createFile</code>.
	 *
	 * @param path the path of the file resource to create a handle for
	 * @return the new file resource handle
	 */
	public static IFile createFileHandle(IPath path) {
		if (path.isValidPath(path.toString()) && path.segmentCount() >= 2) {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		}
		return null;
	}

	public static BufferedReader getReader(File file) {
		try {
			return getReader(((new FileInputStream(file))));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedReader getReader(InputStream in) {
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(in, Strings.UTF_8);
			return new BufferedReader(reader);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String fromBundle(String path) throws IOException, URISyntaxException {
		return fromBundle(path, FluentCore.PLUGIN_ID);
	}

	/**
	 * Loads the content of a file from within a bundle. Returns null if not found.
	 * 
	 * @param path of the file within the bundle.
	 * @return null if not found.
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws  
	 */
	public static String fromBundle(String path, String bundleId) throws IOException, URISyntaxException {
		Bundle bundle = Platform.getBundle(bundleId);
		URL url = bundle.getEntry(path);
		if (url == null) return null;
		url = FileLocator.toFileURL(url);
		return read(URIUtil.toFile(URIUtil.toURI(url)));
	}

	public static String read(File file) throws RuntimeException {
		try {
			return read(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static String read(InputStream in) {
		return read(getReader(in));
	}

	public static String read(URL url) {
		try {
			return read(url.openStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String read(Reader r) {
		try (BufferedReader reader = (r instanceof BufferedReader) ? (BufferedReader) r : new BufferedReader(r);) {
			int bufSize = 8192;
			StringBuilder sb = new StringBuilder(bufSize);
			char cbuf[] = new char[bufSize];
			do {
				int chars = reader.read(cbuf);
				if (chars == -1) break;
				if (sb.length() == 0 && cbuf[0] == '\uFEFF') {
					sb.append(cbuf, 1, chars - 1);
				} else {
					sb.append(cbuf, 0, chars);
				}
			} while (true);
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedWriter getWriter(File file) {
		try {
			return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Strings.UTF_8));
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static BufferedWriter getWriter(OutputStream out) {
		OutputStreamWriter writer;
		try {
			writer = new OutputStreamWriter(out, Strings.UTF_8);
			return new BufferedWriter(writer);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static void write(File out, CharSequence page) {
		try (BufferedWriter writer = getWriter(new FileOutputStream(out))) {
			writer.append(page);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void close(Closeable io) {
		if (io == null) return;
		try {
			io.close();
		} catch (IOException e) {
			if (e.getMessage() != null && e.getMessage().contains("Closed")) {
				return;
			}
			e.printStackTrace();
		}
	}

	public static void delete(File file) {
		if (!file.exists()) return;
		if (file.delete()) return;
		System.gc();
		if (file.delete()) return;

		try {
			Thread.sleep(50L);
		} catch (InterruptedException interruptedexception) {}
		file.delete();
		if (!file.exists()) return;

		throw new RuntimeException(
				new IOException((new StringBuilder("Could not delete file ")).append(file).toString()));
	}
	
	public static String readTextFromFile(File file) {
		if (file != null && file.exists() && file.isFile()) {
			try {
				return Files.readString(
						Paths.get(file.getAbsolutePath()),
						StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(String.format("Could not read file %s", file.getAbsolutePath()), e);
			}
		}
		return null;
	}
	
	public static String readFileContents(IFile file) {
		try (InputStream fileInputStream = file.getContents()) {
			return new String(fileInputStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException | CoreException e) {
			throw new RuntimeException(String.format("Could not read file %s", file.getLocation()), e);
		}
	}
	
	public static boolean isInDocFolder(IResource resource) {
		String[] segments = resource.getLocation().segments();
		if (segments != null && segments.length > 0) {
			for (String segment: segments) {
				if (isDocFolder(segment)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static IFolder getParentDocFolder(IResource resource) {
		IResource currentResource = resource;
		while (currentResource != null && !isDocFolder(currentResource.getName())) {
			currentResource = currentResource.getParent();
		}
		
		if (currentResource instanceof IFolder && isDocFolder(currentResource.getName())) {
			return (IFolder) currentResource;
		}
		
		return null;
	}
	
	private static boolean isDocFolder(String pathSegment) {
		if (pathSegment != null
				&& (pathSegment.equals("doc") || pathSegment.startsWith("doc_"))) {
			return true;
		}
		return false;
	}
	
	public static boolean isMarkdownFile(IFile file) {
		return file != null
				&& FILE_EXTENSION_MARKDOWN.equals(file.getFileExtension());
	}
	
	public static boolean isPumlFile(IFile file) {
		return file != null
				&& FILE_EXTENSION_PLANTUML.equals(file.getFileExtension());
	}
	
	public static boolean isSvgFile(IFile file) {
		return file != null
				&& FILE_EXTENSION_SVG.equals(file.getFileExtension());
	}
	
	public static boolean isJavaFile(IFile file) {
		return file != null
				&& FILE_EXTENSION_JAVA.equals(file.getFileExtension());
	}
	
	public static boolean isAccessibleMarkdownFile(IFile file) {
		return isMarkdownFile(file) && file.isAccessible();
	}
	
	public static boolean isAccessiblePumlFile(IFile file) {
		return isPumlFile(file) && file.isAccessible();
	}
	
	public static boolean isAccessibleSvgFile(IFile file) {
		return isSvgFile(file) && file.isAccessible();
	}
	
	public static IPath resolveToAbsoluteResourcePath(String targetFilePath, IFile currentMarkdownFile) {
		IPath resourceRelativePath = new Path(targetFilePath);
		return toAbsolutePath(resourceRelativePath, currentMarkdownFile);
	}
	
	public static IPath toAbsolutePath(IPath resourceRelativePath, IResource currentResource) {
		IPath absolutePath;
		if (resourceRelativePath.equals(currentResource.getLocation())) {
			absolutePath = currentResource.getLocation();
		} else {
			absolutePath = currentResource.getLocation().removeLastSegments(1).append(resourceRelativePath);
		}
		return absolutePath;
	}
	
	public static List<IFile> findFilesForLocation(IPath fileLocation) {
		if (fileLocation == null) {
			return Collections.emptyList();
		}
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		File file = new File(fileLocation.makeAbsolute().toString());
		URI fileUri = file.toURI();
		
		IFile[] files = workspaceRoot.findFilesForLocationURI(fileUri);
		
		return Arrays.stream(files).collect(Collectors.toList());
	}
	
	public static List<IFile> findExistingFilesForLocation(IPath fileLocation) {
		return findFilesForLocation(fileLocation).stream()
				.filter(file -> file.exists())
				.collect(Collectors.toList());
	}
	
	private static OperatingSystem getOs() {
		String operatingSystemNameLowCase = operatingSystemName.toLowerCase(Locale.ENGLISH);
		
		if (operatingSystemNameLowCase.contains("mac")) {
			return OperatingSystem.MacOs;
		} else if (operatingSystemNameLowCase.contains("win")) {
			return OperatingSystem.Windows;
		} else if (operatingSystemNameLowCase.contains("nux")) {
			return OperatingSystem.LinuxOrUnix;
		} else {
			return OperatingSystem.Other;
		}
	}
	
	public static boolean isOsWindows() {
		return operatingSystem.equals(OperatingSystem.Windows);
	}
	
	public static boolean isOsLinuxOrUnix() {
		return operatingSystem.equals(OperatingSystem.LinuxOrUnix);
	}
	
	public static boolean isOsMacOs() {
		return operatingSystem.equals(OperatingSystem.MacOs);
	}

}
