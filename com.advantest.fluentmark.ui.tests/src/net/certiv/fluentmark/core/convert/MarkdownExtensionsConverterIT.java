package net.certiv.fluentmark.core.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

}
