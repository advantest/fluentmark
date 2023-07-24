// Generated from D:/DevFiles/Eclipse/Tools/Editors/net.certiv.fluent.dt/net.certiv.fluent.dt.core/src/main/java/net/certiv/fluent/dt/core/lang/md/MdParser.g4 by ANTLR 4.12.0

	package net.certiv.fluent.dt.core.lang.md.gen;
	import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link MdParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface MdParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link MdParser#page}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPage(MdParser.PageContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#yamlBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYamlBlock(MdParser.YamlBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#dotBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDotBlock(MdParser.DotBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#mathBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathBlock(MdParser.MathBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#texBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTexBlock(MdParser.TexBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#umlBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUmlBlock(MdParser.UmlBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#htmlBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHtmlBlock(MdParser.HtmlBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#codeBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCodeBlock(MdParser.CodeBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#style}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStyle(MdParser.StyleContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#header}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHeader(MdParser.HeaderContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#hrule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHrule(MdParser.HruleContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#table}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable(MdParser.TableContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#tableRow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableRow(MdParser.TableRowContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList(MdParser.ListContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#listItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitListItem(MdParser.ListItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefinition(MdParser.DefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#defineItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefineItem(MdParser.DefineItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#link}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLink(MdParser.LinkContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#imgLink}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImgLink(MdParser.ImgLinkContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#fnLink}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFnLink(MdParser.FnLinkContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#alt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlt(MdParser.AltContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#url}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUrl(MdParser.UrlContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#paragraph}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParagraph(MdParser.ParagraphContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#line}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLine(MdParser.LineContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWord(MdParser.WordContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#l_line}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitL_line(MdParser.L_lineContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#l_word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitL_word(MdParser.L_wordContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#nl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNl(MdParser.NlContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#attrLeft}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrLeft(MdParser.AttrLeftContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#attrRight}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAttrRight(MdParser.AttrRightContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#comment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComment(MdParser.CommentContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#lineBlank}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLineBlank(MdParser.LineBlankContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#codeBlank}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCodeBlank(MdParser.CodeBlankContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#quoteBlank}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuoteBlank(MdParser.QuoteBlankContext ctx);
	/**
	 * Visit a parse tree produced by {@link MdParser#lineBreak}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLineBreak(MdParser.LineBreakContext ctx);
}