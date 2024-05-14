package net.certiv.fluentmark.core.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class MarkdownExtensionsConverterIT extends AbstractConverterIT {
	
	@ParameterizedTest
	@EnumSource(names = { "FLEXMARK" })
	public void noImplicitHeaderIdsRenderedToHtml(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		
		String markdownFileContent = "# Heading without ID";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("header_without_id.md", markdownFileContent);
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertEquals("<h1>Heading without ID</h1>\n", result);
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void explicitHeaderIdsRenderedToHtml(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		
		String markdownFileContent = "# Heading with ID {#heading-id-1}";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("header_with_id.md", markdownFileContent);
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertEquals("<h1 id=\"heading-id-1\">Heading with ID</h1>\n", result);
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void imagesAndCaptionsCorrectlyRendered(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		
		String markdownFileContent = """
Here comes an image.

![FluentMark logo](Logo.png)
""";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("markdown_with_image.md", markdownFileContent);
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertTrue(result.matches("<p>Here comes an image.<\\/p>\\s*<figure>\\s*<img(.|\\s)*<figcaption.*>FluentMark logo<\\/figcaption>\\s*<\\/figure>\\s*"));
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void commentsCorrectlyRendered(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		
		String markdownFileContent = """
Use HTML-style comments.

<!-- Comments are looking exactly the same as in HTML. -->

Leave blank lines before and after comments for better tool compatibility.

<!-- Multi-line comments
     are looking exactly the same
     as in HTML. -->
""";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("comments.md", markdownFileContent);
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertTrue(result.matches("<p>Use HTML-style comments.<\\/p>\\s*"
				+ "<!-- Comments are looking exactly the same as in HTML. -->\\s*"
				+ "<p>Leave blank lines before and after comments\\s+for\\s+better\\s+tool\\s+compatibility.<\\/p>\\s*"
				+ "<!-- Multi-line comments\\n"
				+ "     are looking exactly the same\\n"
				+ "     as in HTML. -->\\s*"));
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void hiddenCommentsNotRendered(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		
		String markdownFileContent = """
Use HTML-style comments.

<!--- Hidden comments have one '-' symbol more than HTML comments, but are not rendered to HTML. --->

Leave blank lines before and after comments for better tool compatibility.

<!--- Multi-line comments
     are looking the same. --->
""";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("hidden_comments.md", markdownFileContent);
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertTrue(result.matches("<p>Use HTML-style comments.<\\/p>\\s*"
				+ "<p>Leave blank lines before and after comments\\s+for\\s+better\\s+tool\\s+compatibility.<\\/p>\\s*"));
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void strikeThroughCorrectlyRendered(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		
		String markdownFileContent = """
Text can be ~~striked through~~,
but that syntax is a Markdown extension
(i.e. not supported by all tools).
""";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("strike_through.md", markdownFileContent);
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertTrue(result.matches("<p>\\s*Text\\scan\\sbe\\s<del>striked\\sthrough<\\/del>,"
				+ "\\s*but\\sthat\\ssyntax\\sis\\sa\\sMarkdown\\sextension\\s*"
				+ "\\(i\\.e\\.\\snot\\ssupported\\sby\\sall\\stools\\)\\.\\s*<\\/p>\\s*"));
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void codeBlocksCorrectlyRendered(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		
		String markdownFileContent = """
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "age": 25
}
```
""";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("code_block.md", markdownFileContent);
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertTrue(result.matches("<pre( class=\"json\")*><code( class=\"json\")*>\\s*"
				+ "\\{\\n"
				+ "  &quot;firstName&quot;: &quot;John&quot;,\\n"
				+ "  &quot;lastName&quot;: &quot;Smith&quot;,\\n"
				+ "  &quot;age&quot;: 25\\n"
				+ "\\}\\s*"
				+ "<\\/code><\\/pre>\\s"));
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void tablesCorrectlyRendered(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		
		String markdownFileContent = """
| No. | Markdown dialect         | Default | Centered column |
|----:|:-------------------------|---------|:---------------:|
|  1  | CommonMark               |   x     |   A             |
|  2  | GitHub-flavored Markdown |   y     |   B             |
|  3  | GitLab-flavored Markdown |   z     |   C             |
| ... | ...                      |   ...   |   ...           |
""";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("simple_table.md", markdownFileContent);
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		assertTrue(result.matches("<table>\\s*"
				+ "<thead>\\s*"
				+ "<tr.*>\\s*"
				+ "<th (align=\"right\"|style=\"text-align: right;\")>No.<\\/th>\\s*"
				+ "<th (align=\"left\"|style=\"text-align: left;\")>Markdown dialect<\\/th>\\s*"
				+ "<th>Default<\\/th>\\s*"
				+ "<th (align=\"center\"|style=\"text-align: center;\")>Centered column<\\/th>\\s*"
				+ "<\\/tr>\\s*"
				+ "<\\/thead>\\s*"
				+ "<tbody>\\s*"
				+ "<tr.*>\\s*"
				+ "<td (align=\"right\"|style=\"text-align: right;\")>1<\\/td>\\s*"
				+ "<td (align=\"left\"|style=\"text-align: left;\")>CommonMark<\\/td>\\s*"
				+ "<td>x<\\/td>\\s*"
				+ "<td (align=\"center\"|style=\"text-align: center;\")>A<\\/td>\\s*"
				+ "<\\/tr>\\s*"
				+ "<tr.*>\\s*"
				+ "<td (align=\"right\"|style=\"text-align: right;\")>2<\\/td>\\s*"
				+ "<td (align=\"left\"|style=\"text-align: left;\")>GitHub-flavored Markdown<\\/td>\\s*"
				+ "<td>y<\\/td>\\s*"
				+ "<td (align=\"center\"|style=\"text-align: center;\")>B<\\/td>\\s*"
				+ "<\\/tr>\\s*"
				+ "<tr.*>\\s*"
				+ "<td (align=\"right\"|style=\"text-align: right;\")>3<\\/td>\\s*"
				+ "<td (align=\"left\"|style=\"text-align: left;\")>GitLab-flavored Markdown<\\/td>\\s*"
				+ "<td>z<\\/td>\\s*"
				+ "<td (align=\"center\"|style=\"text-align: center;\")>C<\\/td>\\s*"
				+ "<\\/tr>\\s*"
				+ "<tr.*>\\s*"
				+ "<td (align=\"right\"|style=\"text-align: right;\")>...<\\/td>\\s*"
				+ "<td (align=\"left\"|style=\"text-align: left;\")>...<\\/td>\\s*"
				+ "<td>...<\\/td>\\s*"
				+ "<td (align=\"center\"|style=\"text-align: center;\")>...<\\/td>\\s*"
				+ "<\\/tr>\\s*"
				+ "<\\/tbody>\\s*"
				+ "<\\/table>\\s"));
	}
	

}
