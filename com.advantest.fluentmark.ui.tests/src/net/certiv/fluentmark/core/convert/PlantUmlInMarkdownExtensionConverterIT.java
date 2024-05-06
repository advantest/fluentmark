/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.core.convert;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.text.IDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class PlantUmlInMarkdownExtensionConverterIT extends AbstractConverterIT {
	
	@Test
	public void test() throws IOException {
		// given
		String srcMarkdownFile = "resources/feature-overview.md";
		String documentContent = readFileContentFrom(srcMarkdownFile);
		IDocument document = prepareDocument(documentContent);
		File testMarkdownFile = copyFileFromResourceToTempFolder(srcMarkdownFile, "feature-overview.md");
		
		// when
		String result = convert(testMarkdownFile, document);
		
		// then
		assertNotNull(result);
		assertFalse(result.isBlank());
		
		// TODO check result contents
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void includeNonExistingPumlFileDoesntThrowException(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		String markdownFileContent = "# Test\n\n![alt text](../diagrams/none.puml) ";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("include_missing_puml_file.md", markdownFileContent);
		
		String result = null;
		try {
			result = convert(markdownFile, document);
		} catch (Exception e) {
			fail("Converter is not expected to throw exceptions.", e);
		}
		
		
		assertNotNull(result);
		assertTrue(result.contains("<span style=\"color:red\">PlantUML file"));
		assertTrue(result.contains("does not exist.</span>"));
	}
	
	@ParameterizedTest
	@EnumSource(names = { "PANDOC", "FLEXMARK" })
	public void includedPlantUmlImagesRenderedToSvg(ConverterType converterType) throws Exception {
		configProvider.setConverterType(converterType);
		String markdownFileContent = "![Some diagram](classes.puml) ";
		IDocument document = prepareDocument(markdownFileContent);
		File markdownFile = createFileWithContent("include_puml_file.md", markdownFileContent);
		copyFileFromResourceToTempFolder("resources/PlantUML/classes.puml", "classes.puml");
		
		String result = convert(markdownFile, document);
		
		assertNotNull(result);
		// TODO check <figcaption aria-hidden="true">Some diagram</figcaption>
		assertTrue(result.matches("<figure>\\s*<svg(.|\\s)*<\\/svg>(.|\\s)*<\\/figure>\\s*"));
	}
	
	// TODO check PlantUML code blocks in Markdown, too
	
	/*
	 * 
<figure>
  <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" contentStyleType="text/css" height="170px" preserveAspectRatio="none" style="width:241px;height:170px;background:#FFFFFF;" version="1.1" viewBox="0 0 241 170" width="241px" zoomAndPan="magnify">
    <defs/>
    <g>
      <!--class List-->
      <g id="elem_List">
        <rect codeLine="2" fill="#F1F1F1" height="48" id="List" rx="2.5" ry="2.5" style="stroke:#181818;stroke-width:0.5;" width="55" x="90.5" y="7"/>
        <ellipse cx="105.5" cy="23" fill="#B4A7E5" rx="11" ry="11" style="stroke:#181818;stroke-width:1.0;"/>
        <path d="M101.9277,19.2651 L101.9277,17.1069 L109.3071,17.1069 L109.3071,19.2651 L106.8418,19.2651 L106.8418,27.3418 L109.3071,27.3418 L109.3071,29.5 L101.9277,29.5 L101.9277,27.3418 L104.3931,27.3418 L104.3931,19.2651 Z " fill="#000000"/>
        <text fill="#000000" font-family="sans-serif" font-size="14" font-style="italic" lengthAdjust="spacing" textLength="23" x="119.5" y="28.291">List</text>
        <line style="stroke:#181818;stroke-width:0.5;" x1="91.5" x2="144.5" y1="39" y2="39"/>
        <line style="stroke:#181818;stroke-width:0.5;" x1="91.5" x2="144.5" y1="47" y2="47"/>
      </g>
      <!--class ArrayList-->
      <g id="elem_ArrayList">
        <rect codeLine="4" fill="#F1F1F1" height="48" id="ArrayList" rx="2.5" ry="2.5" style="stroke:#181818;stroke-width:0.5;" width="92" x="7" y="115"/>
        <ellipse cx="22" cy="131" fill="#ADD1B2" rx="11" ry="11" style="stroke:#181818;stroke-width:1.0;"/>
        <path d="M24.4731,137.1431 Q23.8921,137.4419 23.2529,137.5913 Q22.6138,137.7407 21.9082,137.7407 Q19.4014,137.7407 18.0815,136.0889 Q16.7617,134.437 16.7617,131.3159 Q16.7617,128.1865 18.0815,126.5347 Q19.4014,124.8828 21.9082,124.8828 Q22.6138,124.8828 23.2612,125.0322 Q23.9087,125.1816 24.4731,125.4805 L24.4731,128.2031 Q23.8423,127.6221 23.2488,127.3523 Q22.6553,127.0825 22.0244,127.0825 Q20.6797,127.0825 19.9949,128.1492 Q19.3101,129.2158 19.3101,131.3159 Q19.3101,133.4077 19.9949,134.4744 Q20.6797,135.541 22.0244,135.541 Q22.6553,135.541 23.2488,135.2712 Q23.8423,135.0015 24.4731,134.4204 Z " fill="#000000"/>
        <text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="60" x="36" y="136.291">ArrayList</text>
        <line style="stroke:#181818;stroke-width:0.5;" x1="8" x2="98" y1="147" y2="147"/>
        <line style="stroke:#181818;stroke-width:0.5;" x1="8" x2="98" y1="155" y2="155"/>
      </g>
      <!--class LinkedList-->
      <g id="elem_LinkedList">
        <rect codeLine="5" fill="#F1F1F1" height="48" id="LinkedList" rx="2.5" ry="2.5" style="stroke:#181818;stroke-width:0.5;" width="100" x="134" y="115"/>
        <ellipse cx="149" cy="131" fill="#ADD1B2" rx="11" ry="11" style="stroke:#181818;stroke-width:1.0;"/>
        <path d="M151.4731,137.1431 Q150.8921,137.4419 150.2529,137.5913 Q149.6138,137.7407 148.9082,137.7407 Q146.4014,137.7407 145.0815,136.0889 Q143.7617,134.437 143.7617,131.3159 Q143.7617,128.1865 145.0815,126.5347 Q146.4014,124.8828 148.9082,124.8828 Q149.6138,124.8828 150.2612,125.0322 Q150.9087,125.1816 151.4731,125.4805 L151.4731,128.2031 Q150.8423,127.6221 150.2488,127.3523 Q149.6553,127.0825 149.0244,127.0825 Q147.6797,127.0825 146.9949,128.1492 Q146.3101,129.2158 146.3101,131.3159 Q146.3101,133.4077 146.9949,134.4744 Q147.6797,135.541 149.0244,135.541 Q149.6553,135.541 150.2488,135.2712 Q150.8423,135.0015 151.4731,134.4204 Z " fill="#000000"/>
        <text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="68" x="163" y="136.291">LinkedList</text>
        <line style="stroke:#181818;stroke-width:0.5;" x1="135" x2="233" y1="147" y2="147"/>
        <line style="stroke:#181818;stroke-width:0.5;" x1="135" x2="233" y1="155" y2="155"/>
      </g>
      <!--reverse link List to ArrayList-->
      <g id="link_List_ArrayList">
        <path codeLine="7" d="M94.3314,70.6053 C83.4914,88.2853 78.06,97.13 67.23,114.79 " fill="none" id="List-backto-ArrayList" style="stroke:#181818;stroke-width:1.0;stroke-dasharray:7.0,7.0;"/>
        <polygon fill="none" points="103.74,55.26,89.2163,67.4691,99.4465,73.7415,103.74,55.26" style="stroke:#181818;stroke-width:1.0;"/>
      </g>
      <!--reverse link List to LinkedList-->
      <g id="link_List_LinkedList">
        <path codeLine="8" d="M141.9951,70.5395 C153.0051,88.2195 158.55,97.13 169.55,114.79 " fill="none" id="List-backto-LinkedList" style="stroke:#181818;stroke-width:1.0;stroke-dasharray:7.0,7.0;"/>
        <polygon fill="none" points="132.48,55.26,136.9019,73.7112,147.0883,67.3678,132.48,55.26" style="stroke:#181818;stroke-width:1.0;"/>
      </g>
      <!--SRC=[oymhIIrAIqnELV39B2xXuahEIImkLd0iAagi10j0-3wPUTcfAH0L85B1faPF3qaLGWn99G00]-->
    </g>
  </svg>
</figure>

	 * 
	 * 
<figure>
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" contentStyleType="text/css" height="170px" preserveAspectRatio="none" style="width:241px;height:170px;background:#FFFFFF;" version="1.1" viewBox="0 0 241 170" width="241px" zoomAndPan="magnify"><defs/><g><!--class List--><g id="elem_List"><rect codeLine="2" fill="#F1F1F1" height="48" id="List" rx="2.5" ry="2.5" style="stroke:#181818;stroke-width:0.5;" width="55" x="90.5" y="7"/><ellipse cx="105.5" cy="23" fill="#B4A7E5" rx="11" ry="11" style="stroke:#181818;stroke-width:1.0;"/><path d="M101.9277,19.2651 L101.9277,17.1069 L109.3071,17.1069 L109.3071,19.2651 L106.8418,19.2651 L106.8418,27.3418 L109.3071,27.3418 L109.3071,29.5 L101.9277,29.5 L101.9277,27.3418 L104.3931,27.3418 L104.3931,19.2651 Z " fill="#000000"/><text fill="#000000" font-family="sans-serif" font-size="14" font-style="italic" lengthAdjust="spacing" textLength="23" x="119.5" y="28.291">List</text><line style="stroke:#181818;stroke-width:0.5;" x1="91.5" x2="144.5" y1="39" y2="39"/><line style="stroke:#181818;stroke-width:0.5;" x1="91.5" x2="144.5" y1="47" y2="47"/></g><!--class ArrayList--><g id="elem_ArrayList"><rect codeLine="4" fill="#F1F1F1" height="48" id="ArrayList" rx="2.5" ry="2.5" style="stroke:#181818;stroke-width:0.5;" width="92" x="7" y="115"/><ellipse cx="22" cy="131" fill="#ADD1B2" rx="11" ry="11" style="stroke:#181818;stroke-width:1.0;"/><path d="M24.4731,137.1431 Q23.8921,137.4419 23.2529,137.5913 Q22.6138,137.7407 21.9082,137.7407 Q19.4014,137.7407 18.0815,136.0889 Q16.7617,134.437 16.7617,131.3159 Q16.7617,128.1865 18.0815,126.5347 Q19.4014,124.8828 21.9082,124.8828 Q22.6138,124.8828 23.2612,125.0322 Q23.9087,125.1816 24.4731,125.4805 L24.4731,128.2031 Q23.8423,127.6221 23.2488,127.3523 Q22.6553,127.0825 22.0244,127.0825 Q20.6797,127.0825 19.9949,128.1492 Q19.3101,129.2158 19.3101,131.3159 Q19.3101,133.4077 19.9949,134.4744 Q20.6797,135.541 22.0244,135.541 Q22.6553,135.541 23.2488,135.2712 Q23.8423,135.0015 24.4731,134.4204 Z " fill="#000000"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="60" x="36" y="136.291">ArrayList</text><line style="stroke:#181818;stroke-width:0.5;" x1="8" x2="98" y1="147" y2="147"/><line style="stroke:#181818;stroke-width:0.5;" x1="8" x2="98" y1="155" y2="155"/></g><!--class LinkedList--><g id="elem_LinkedList"><rect codeLine="5" fill="#F1F1F1" height="48" id="LinkedList" rx="2.5" ry="2.5" style="stroke:#181818;stroke-width:0.5;" width="100" x="134" y="115"/><ellipse cx="149" cy="131" fill="#ADD1B2" rx="11" ry="11" style="stroke:#181818;stroke-width:1.0;"/><path d="M151.4731,137.1431 Q150.8921,137.4419 150.2529,137.5913 Q149.6138,137.7407 148.9082,137.7407 Q146.4014,137.7407 145.0815,136.0889 Q143.7617,134.437 143.7617,131.3159 Q143.7617,128.1865 145.0815,126.5347 Q146.4014,124.8828 148.9082,124.8828 Q149.6138,124.8828 150.2612,125.0322 Q150.9087,125.1816 151.4731,125.4805 L151.4731,128.2031 Q150.8423,127.6221 150.2488,127.3523 Q149.6553,127.0825 149.0244,127.0825 Q147.6797,127.0825 146.9949,128.1492 Q146.3101,129.2158 146.3101,131.3159 Q146.3101,133.4077 146.9949,134.4744 Q147.6797,135.541 149.0244,135.541 Q149.6553,135.541 150.2488,135.2712 Q150.8423,135.0015 151.4731,134.4204 Z " fill="#000000"/><text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacing" textLength="68" x="163" y="136.291">LinkedList</text><line style="stroke:#181818;stroke-width:0.5;" x1="135" x2="233" y1="147" y2="147"/><line style="stroke:#181818;stroke-width:0.5;" x1="135" x2="233" y1="155" y2="155"/></g><!--reverse link List to ArrayList--><g id="link_List_ArrayList"><path codeLine="7" d="M94.3314,70.6053 C83.4914,88.2853 78.06,97.13 67.23,114.79 " fill="none" id="List-backto-ArrayList" style="stroke:#181818;stroke-width:1.0;stroke-dasharray:7.0,7.0;"/><polygon fill="none" points="103.74,55.26,89.2163,67.4691,99.4465,73.7415,103.74,55.26" style="stroke:#181818;stroke-width:1.0;"/></g><!--reverse link List to LinkedList--><g id="link_List_LinkedList"><path codeLine="8" d="M141.9951,70.5395 C153.0051,88.2195 158.55,97.13 169.55,114.79 " fill="none" id="List-backto-LinkedList" style="stroke:#181818;stroke-width:1.0;stroke-dasharray:7.0,7.0;"/><polygon fill="none" points="132.48,55.26,136.9019,73.7112,147.0883,67.3678,132.48,55.26" style="stroke:#181818;stroke-width:1.0;"/></g><!--SRC=[oymhIIrAIqnELV39B2xXuahEIImkLd0iAagi10j0-3wPUTcfAH0L85B1faPF3qaLGWn99G00]--></g></svg>
<figcaption aria-hidden="true">Some diagram</figcaption>
</figure>
	 * 
	 */

}
