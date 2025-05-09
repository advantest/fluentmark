/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.util;

import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Cmd {
	
	/**
	 * Execute a command in a subprocess
	 * 
	 * @param cmd command line argument array defining the command and options. The command must execute
	 *            as a standard filter: stdIn to stdOut.
	 * @param data input data
	 * @param text
	 * @return output data
	 */
	public static synchronized CmdResult process(String[] cmd, String base, String data) {
		return process(cmd, base, data, Strings.EOL);
	}

	/**
	 * Execute a command in a subprocess
	 * 
	 * @param cmd command line argument array defining the command and options. The command must execute
	 *            as a standard filter: stdIn to stdOut.
	 * @param data input data
	 * @param text
	 * @param preferredLineEnding the preferred line ending, usually OS-dependant.
	 * @return output data
	 */
	public static synchronized CmdResult process(String[] cmd, String base, String data, String preferredLineEnding) {
		if (!preferredLineEnding.equals("\n") && !preferredLineEnding.equals("\r\n")) {
			throw new IllegalStateException("Illegal line ending! Expected \\\\n or \\r\\n.");
		}
		
		final StringBuilder sb = new StringBuilder();
		final StringBuilder errSb = new StringBuilder();
		final ProcessBuilder pb = new ProcessBuilder(cmd);
		try {
			if (base != null) {
				pb.directory(new File(base));
			}
			pb.redirectErrorStream(false);
			Process process = pb.start();

			// prep for ouput from the process
			InputStreamReader in = new InputStreamReader(process.getInputStream());
			BufferedReader br = new BufferedReader(in);
			
			InputStreamReader errIn = new InputStreamReader(process.getErrorStream());
			BufferedReader errBr = new BufferedReader(errIn);

			// prep and feed input to the processs
			if (data != null) {
				OutputStreamWriter out = new OutputStreamWriter(process.getOutputStream());
				BufferedWriter bw = new BufferedWriter(out);
				bw.write(data);
				bw.close();
			}

			// read output from the process
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + preferredLineEnding);
			}
			
			while((line = errBr.readLine()) != null) {
				errSb.append(line + preferredLineEnding);
			}
			
			return new CmdResult(sb.toString(), errSb.toString());
		} catch (IOException e) {
			throw new RuntimeException("Cmd execution error: " + e.getMessage(), e);
		}
	}
	
	public static final class CmdResult {
		
		public final String stdOutput;
		public final String errOutput;
		
		CmdResult(String stdOutput, String errOutput) {
			this.stdOutput = stdOutput;
			this.errOutput = errOutput;
			
			if (stdOutput == null) {
				stdOutput = "";
			}
		}
		
		public boolean hasErrors() {
			return errOutput != null && !errOutput.isBlank();
		}
		
	}

	/**
	 * Parse a string into an array of command line arguments
	 */
	public static String[] parse(String command) {
		List<String> args = new ArrayList<>();
		StringBuilder qStr = new StringBuilder();
		boolean quoted = false;
		char ac[] = command.toCharArray();
		for (char c : ac) {
			if (quoted) {
				qStr.append(c);
				if (c == '"') {
					quoted = false;
				}
			} else if (Character.isWhitespace(c)) {
				if (qStr.length() != 0) {
					args.add(qStr.toString());
					qStr = new StringBuilder();
				}
			} else {
				qStr.append(c);
				if (c == '"') {
					quoted = true;
				}
			}
		}

		if (qStr.length() != 0) {
			args.add(qStr.toString());
		}
		String osName = System.getProperty("os.name");
		if (osName.equals("Windows 95")) {
			ArrayList<String> cmd = new ArrayList<>(args.size() + 2);
			cmd.add("command.com");
			cmd.add("/C");
			cmd.addAll(args);
			args = cmd;
		} else if (osName.startsWith("Windows")) {
			ArrayList<String> cmd = new ArrayList<>(args.size() + 2);
			cmd.add("cmd.exe");
			cmd.add("/C");
			cmd.addAll(args);
			args = cmd;
		}
		return args.toArray(new String[args.size()]);
	}
}
