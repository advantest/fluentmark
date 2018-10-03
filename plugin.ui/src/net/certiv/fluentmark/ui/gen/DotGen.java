/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.gen;

import java.util.List;
import java.util.Map;

import net.certiv.dsl.core.preferences.DslPrefsManager;
import net.certiv.dsl.core.util.Log;
import net.certiv.dsl.core.util.Strings;
import net.certiv.dsl.core.util.exec.Cmd;
import net.certiv.dsl.core.util.stores.LRUCache;
import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.preferences.Prefs;

public class DotGen {

	private static final String[] DOTOPS = new String[] { "", "-Tsvg" };
	private static final Map<Integer, String> dotCache = new LRUCache<>(20);

	private DotGen() {}

	public static String runDot(List<String> lines) {
		return runDot(String.join(Strings.EOL, lines));
	}

	public static String runDot(String data) {
		DslPrefsManager store = FluentCore.getDefault().getPrefsManager();
		String cmd = store.getString(Prefs.EDITOR_DOT_PROGRAM);
		if (data.trim().isEmpty() || cmd.trim().isEmpty()) return "";

		// return cached value, if present
		int key = data.hashCode();
		String value = dotCache.get(key);
		if (value != null) return value;

		// generate a new value by executing dot
		String[] args = DOTOPS;
		args[0] = cmd;

		StringBuilder out = new StringBuilder();
		out.append("<div class=\"dot\">");
		out.append(Cmd.process(args, null, data).replaceAll("\\R", "").replaceFirst("\\<\\!DOC.+?\\>", ""));
		out.append("</div>" + Strings.EOL);
		value = out.toString();

		// update cache if valid value
		if (value != null && !value.trim().isEmpty()) {
			dotCache.put(key, value);
		} else {
			Log.error(DotGen.class, "Dot created no output for" + Strings.EOL + data);
		}

		return value;
	}
}
