package net.certiv.fluentmark.core.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class MarkdownExtensionsIConverterIT extends AbstractConverterIT {
	
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

}
