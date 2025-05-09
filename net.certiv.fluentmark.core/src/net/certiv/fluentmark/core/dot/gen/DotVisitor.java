// Generated from D:/DevFiles/Eclipse/Fluentmark/net.certiv.fluentmark/plugin/src/net/certiv/fluentmark/dot/Dot.g4 by ANTLR 4.7.1

	package net.certiv.fluentmark.core.dot.gen;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DotParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DotVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DotParser#graph}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGraph(DotParser.GraphContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#stmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStmt(DotParser.StmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#nodeStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNodeStmt(DotParser.NodeStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#edgeStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEdgeStmt(DotParser.EdgeStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#attrStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrStmt(DotParser.AttrStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#attrList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrList(DotParser.AttrListContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#attribute}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttribute(DotParser.AttributeContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#subgraph}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubgraph(DotParser.SubgraphContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#port}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPort(DotParser.PortContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#edgeRhs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEdgeRhs(DotParser.EdgeRhsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DotParser#nodeId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNodeId(DotParser.NodeIdContext ctx);
}