/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.dialog.tables;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;

import java.util.ArrayList;
import java.util.List;

import net.certiv.fluentmark.core.markdown.PagePart;
import net.certiv.fluentmark.core.markdown.PageRoot;
import net.certiv.fluentmark.core.util.Strings;

public class TableModel {

	public static class Row {

		Integer num; // doc line, if already present #
		int row; // table row #
		String[] data;

		public Row(Integer num, int row, String[] data) {
			this.num = num;
			this.row = row;
			this.data = data;
		}

		public Row(int numCols) {
			data = new String[numCols];
			for (int idx = 0; idx < data.length; idx++) {
				data[idx] = "";
			}
		}

		public String toString() {
			return num + " [" + String.join(",", data) + "]";
		}
	}

	private PagePart part;
	List<Row> rows;
	int formatRow = -1; // row in model
	int numCols;
	int[] aligns;
	int[] colWidths;
	String[] headers;

	public TableModel() {
		rows = new ArrayList<>();
	}

	public boolean load(PagePart part) {
		this.part = part;
		PageRoot model = part.getPageModel();
		for (int idx = part.getBeginLine(), row = 0; idx <= part.getEndLine(); idx++) {
			String text = model.getText(idx);
			String[] cols = parseRow(text.substring(1));
			if (text.trim().contains("---")) {
				formatRow = row;
				aligns = getAligns(cols);
				numCols = cols.length;
			}
			rows.add(new Row(idx, row, cols));
			row++;
		}
		if (rows.isEmpty()) return false;

		headers = rows.get(0).data;
		calcColWidths();
		return true;
	}

	public void insertCol(int target) {
		numCols++;
		aligns = ArrayUtils.add(aligns, target, SWT.LEFT);
		for (Row row : rows) {
			if (row.row == formatRow) {
				row.data = ArrayUtils.add(row.data, target, ":---");
			} else {
				row.data = ArrayUtils.add(row.data, target, "");
			}
		}
		headers = rows.get(0).data;
		calcColWidths();
	}

	public void removeCol(int target) {
		numCols--;
		aligns = ArrayUtils.remove(aligns, target);
		for (Row row : rows) {
			row.data = ArrayUtils.remove(row.data, target);
		}
		headers = rows.get(0).data;
		calcColWidths();
	}

	public void addRow(int target) {
		if (target <= rows.size()) {
			rows.add(target, new Row(numCols));
		}
		int lastLine = part.getEndLine();
		for (int idx = 0, num = part.getBeginLine(); idx < rows.size(); idx++) {
			rows.get(idx).row = idx;
			if (num <= lastLine) {
				rows.get(idx).num = num;
			} else {
				rows.get(idx).num = null;
			}
			num++;
		}
	}

	public void removeRow(int target) {
		if (target > formatRow && target < rows.size()) {
			rows.remove(target);
		}
	}

	public int getFormatRow() {
		return formatRow;
	}

	public int getNumCols() {
		return numCols;
	}

	public int[] getAligns() {
		return aligns;
	}

	public List<Row> getRows() {
		return rows;
	}

	public Object[] getElements() {
		Row[] elements = new Row[rows.size() - 1];
		for (int idx = 0, cnt = 0; idx < rows.size(); idx++) {
			if (idx == formatRow) continue;
			elements[cnt] = rows.get(idx);
			cnt++;
		}
		return elements;
	}

	public String build() {
		calcColWidths();
		StringBuilder sb = new StringBuilder();
		for (int idx = 0; idx < rows.size(); idx++) {
			if (idx == formatRow) {
				addFormatRow(sb, rows.get(idx));
				continue;
			}
			addDataRow(sb, rows.get(idx));
		}
		return sb.toString();
	}

	private void calcColWidths() {
		colWidths = new int[numCols];
		for (int col = 0; col < numCols; col++) {
			colWidths[col] = aligns[col] == SWT.CENTER ? 5 : 4;
			for (Row row : rows) {
				if (row.row == formatRow) continue;
				colWidths[col] = Math.max(colWidths[col], row.data[col].length());
			}
		}
	}

	private void addFormatRow(StringBuilder sb, Row row) {
		sb.append("|");
		for (int col = 0; col < numCols; col++) {
			if (aligns[col] == SWT.LEFT || aligns[col] == SWT.CENTER) sb.append(":");

			int min = aligns[col] == SWT.CENTER ? 5 : 4;
			sb.append("---");
			sb.append(Strings.dup("-", colWidths[col] - min));

			if (aligns[col] == SWT.RIGHT || aligns[col] == SWT.CENTER) sb.append(":");
			sb.append("|");
		}
		
		if (row.num != null) {
			String existing = part.getPageModel().getText(row.num);
			int mark = existing.lastIndexOf("|");
			if (mark < existing.length() - 1) {
				sb.append(existing.substring(mark + 1));
			}
		}
		
		sb.append(part.getLineDelim());
	}

	private void addDataRow(StringBuilder sb, Row row) {
		sb.append("|");
		for (int col = 0; col < numCols; col++) {
			int colWidth = colWidths[col];
			int padLeft = 0;
			int padRight = colWidth - row.data[col].length();
			if (aligns[col] == SWT.CENTER) {
				padLeft = padRight / 2;
				padRight -= padLeft;
			} else if (aligns[col] == SWT.RIGHT) {
				padLeft = padRight;
				padRight = 0;
			}
			sb.append(Strings.dup(" ", padLeft));
			sb.append(row.data[col]);
			sb.append(Strings.dup(" ", padRight));
			sb.append("|");
		}
		
		if (row.num != null) {
			String existing = part.getPageModel().getText(row.num);
			int mark = existing.lastIndexOf("|");
			if (mark < existing.length() - 1) {
				sb.append(existing.substring(mark + 1));
			}
		}
		
		sb.append(part.getLineDelim());
	}

	private String[] parseRow(String text) {
		int end = text.lastIndexOf('|');
		text = text.substring(0, end);
		String[] cols = text.split("(?<!\\\\)\\|", -1);
		for (int idx = 0; idx < cols.length; idx++) {
			cols[idx] = cols[idx].trim();
		}
		return cols;
	}

	private int[] getAligns(String[] cols) {
		int[] aligns = new int[cols.length];
		for (int idx = 0; idx < cols.length; idx++) {
			aligns[idx] = characterize(cols[idx]);
		}
		return aligns;
	}

	private int characterize(String col) {
		col = col.trim();
		if (col.matches("\\:---+\\:")) return SWT.CENTER;
		if (col.matches("---+\\:")) return SWT.RIGHT;
		return SWT.LEFT;
	}
}
