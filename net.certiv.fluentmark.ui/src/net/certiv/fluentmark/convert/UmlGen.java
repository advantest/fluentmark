/*******************************************************************************
 * Copyright (c) 2016 - 2018 Certiv Analytics and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.convert;

import java.util.List;
import java.util.Map;

import java.io.ByteArrayOutputStream;

import java.nio.charset.Charset;

import net.certiv.fluentmark.Log;
import net.certiv.fluentmark.core.util.LRUCache;
import net.certiv.fluentmark.core.util.Strings;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;

public class UmlGen {

	private static final Map<Integer, String> umlCache = new LRUCache<>(20);

	private IConfigurationProvider configurationProvider;

	public UmlGen(IConfigurationProvider configProvider) {
		this.configurationProvider = configProvider;
	}

	public String uml2svg(List<String> lines) {
		return uml2svg(String.join(Strings.EOL, lines));
	}

	public String uml2svg(String data) {

		// return cached value, if present
		int key = data.hashCode();
		String value = umlCache.get(key);
		if (value != null) return value;

		String dotexe = configurationProvider.getDotCommand();
		if (!dotexe.isEmpty()) {
			GraphvizUtils.setDotExecutable(dotexe);
		}

		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			SourceStringReader reader = new SourceStringReader(data);
			reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
			value = new String(os.toByteArray(), Charset.forName("UTF-8"));
		} catch (Exception e) {
			Log.error("PlantUML exception on" + Strings.EOL + data, e);
		}

		// update cache if valid value
		if (value != null && !value.trim().isEmpty()) {
			umlCache.put(key, value);
		} else {
			return "";
		}

		return value;
	}
}
