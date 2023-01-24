package net.certiv.fluentmark.core.dot;

import org.eclipse.core.resources.IMarker;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.certiv.fluentmark.core.dot.AttrMap.Props;
import net.certiv.fluentmark.core.dot.gen.DotLexer;
import net.certiv.fluentmark.core.dot.gen.DotParser.AttrListContext;
import net.certiv.fluentmark.core.dot.gen.DotParser.AttrStmtContext;
import net.certiv.fluentmark.core.dot.gen.DotParser.AttributeContext;
import net.certiv.fluentmark.core.dot.gen.DotParser.EdgeStmtContext;
import net.certiv.fluentmark.core.dot.gen.DotParser.NodeStmtContext;
import net.certiv.fluentmark.core.dot.gen.DotParser.StmtContext;
import net.certiv.fluentmark.core.dot.gen.DotParserBaseListener;
import net.certiv.fluentmark.core.marker.DotProblem;

public class VerifyVisitor {

	public static final VerifyVisitor INST = new VerifyVisitor();
	private static final ContextVisitor visitor = INST.new ContextVisitor();

	private DotRecord record;
	private DotProblemCollector collector;

	/**
	 * Verifies the semantic and attribute value correctness of the DOT spec described by the given
	 * record. Issues are reported to the given collector.
	 */
	public void check(DotRecord record, DotProblemCollector collector) {
		this.record = record;
		this.collector = collector;
		ParseTreeWalker.DEFAULT.walk(visitor, record.tree);
	}

	private void report(int severity, Kind kind, Token token, String cause) {
		DotProblem problem = new DotProblem(severity, cause, token, record);
		collector.accept(problem);
	}

	private class ContextVisitor extends DotParserBaseListener {

		@Override
		public void enterStmt(StmtContext ctx) {}

		@Override
		public void enterNodeStmt(NodeStmtContext ctx) {}

		@Override
		public void enterEdgeStmt(EdgeStmtContext ctx) {}

		@Override
		public void enterAttrStmt(AttrStmtContext ctx) {}

		@Override
		public void enterAttrList(AttrListContext ctx) {
			for (AttributeContext attribute : ctx.attribute()) {
				String id = attribute.id().getText();
				Props props = AttrMap.get(id);
				if (props.name.equals(Attr.INVALID)) {
					String cause = "Invalid name '" + id + "' at %s:%s";
					report(IMarker.SEVERITY_ERROR, Kind.ATTRIBUTE, attribute.id().name, cause);
					continue;
				}

				Token token = attribute.value().getStart();
				String value = attribute.value().getText();
				String cause = null;
				switch (props.type) {
					case LIST:
						if (!AttrMap.in(props.values, value)) {
							cause = "Invalid value '" + value + "' at %s:%s";
						}
						break;

					case COLOR:
						boolean rgb = token.getType() == DotLexer.RGB;
						boolean hsv = token.getType() == DotLexer.HSV;
						String color = DotColors.get("x11", value); // TODO: handle colorScheme
						if (!rgb && !hsv && color == null) {
							cause = "Color [name|RGB|RGBA|HSV] value required as '" + value + "' at %s:%s";
						}
						break;

					case COLORS:
						rgb = token.getType() == DotLexer.RGB;
						hsv = token.getType() == DotLexer.HSV;
						color = DotColors.get("x11", value);
						if (!rgb && !hsv && color == null && attribute.value().COLORS() == null) {
							cause = "Color or Color list required as '" + value + "' at %s:%s";
						}
						break;

					case NUMBER:
						if (attribute.value().NUMBER() == null) {
							cause = "Numeric value required as '" + value + "' at %s:%s";
						}
						break;

					case POINT:
						if (attribute.value().POINT() == null) {
							cause = "Point required as '" + value + "' at %s:%s";
						}
						break;

					case RECT:
						if (attribute.value().RECT() == null) {
							cause = "Rectangle required as '" + value + "' at %s:%s";
						}
						break;

					case SPLINE:
						if (attribute.value().SPLINE() == null || attribute.value().POINT() == null) {
							cause = "Spline or Point required as '" + value + "' at %s:%s";
						}
						break;

					case STRING:
						if (token.getType() != DotLexer.STRING && token.getType() != DotLexer.HTML) {
							cause = "String or Html value required as '" + value + "' at %s:%s";
						}
						break;

					default:
						break;
				}

				// emit value problem
				if (cause != null) report(IMarker.SEVERITY_ERROR, Kind.ATTRIBUTE, token, cause);
			}
		}
	}

	private enum Kind {
		SEMANTIC,
		ATTRIBUTE;
	}
}
