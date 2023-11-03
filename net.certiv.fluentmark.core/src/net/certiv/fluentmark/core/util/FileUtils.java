/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;

import org.osgi.framework.Bundle;

import java.net.URISyntaxException;
import java.net.URL;

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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class FileUtils {
	
	public static final String FILE_EXTENSION_MARKDOWN = "md";
	public static final String FILE_EXTENSION_PLANTUML = "puml";
	public static final String FILE_EXTENSION_SVG = "svg";
	public static final String FILE_EXTENSION_JAVA = "java";

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

	/**
	 * Loads the content of a file from within a bundle. Returns null if not found.
	 * 
	 * @param pathname of the file within the bundle.
	 * @return null if not found.
	 * @throws IOException 
	 * @throws URISyntaxException 
	 * @throws  
	 */
	public static String fromBundle(String pathname, String bundleId) throws IOException, URISyntaxException {
		Bundle bundle = Platform.getBundle(bundleId);
		URL url = bundle.getEntry(pathname);
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
	
	public static boolean isInDocFolder(IResource resource) {
		String[] segments = resource.getFullPath().segments();
		if (segments != null && segments.length > 0) {
			for (String segment: segments) {
				if ("doc".equals(segment)
						|| segment.startsWith("doc_")) {
					return true;
				}
			}
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

}
