/*******************************************************************************
 * Copyright (c) 2016 - 2018 Certiv Analytics and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.gen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.certiv.dsl.core.log.Log;
import net.certiv.dsl.core.preferences.DslPrefsManager;
import net.certiv.dsl.core.util.Strings;
import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.preferences.Prefs;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.cucadiagram.dot.GraphvizUtils;

public class UmlGen {

	private static final Map<Integer, String> umlCache = new LinkedHashMap<>(20);

	private UmlGen() {}

	public static String uml2svg(List<String> lines) {
		return uml2svg(String.join(Strings.EOL, lines));
	}

	public static String uml2svg(String data) {

		// return cached value, if present
		int key = data.hashCode();
		String value = umlCache.get(key);
		if (value != null) return value;

		DslPrefsManager store = FluentCore.getDefault().getPrefsManager();
		String dotexe = store.getString(Prefs.EDITOR_DOT_PROGRAM);
		if (!dotexe.isEmpty()) {
			GraphvizUtils.setDotExecutable(dotexe);
		}

		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			SourceStringReader reader = new SourceStringReader(data);
			reader.generateImage(os, new FileFormatOption(FileFormat.SVG));
			value = new String(os.toByteArray(), Charset.forName("UTF-8"));
		} catch (IOException e) {
			Log.error(UmlGen.class, "Uml exception on" + Strings.EOL + data, e);
		}

		// update cache if valid value
		if (value != null && !value.trim().isEmpty()) {
			umlCache.put(key, value);
		} else {
			Log.error(UmlGen.class, "Uml created no output for" + Strings.EOL + data);
		}

		return value;
	}
}
