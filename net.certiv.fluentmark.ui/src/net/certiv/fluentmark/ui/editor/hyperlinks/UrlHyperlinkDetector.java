package net.certiv.fluentmark.ui.editor.hyperlinks;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.util.DocumentUtils;

public class UrlHyperlinkDetector extends URLHyperlinkDetector {

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || textViewer == null) {
			return null;
		}
		
		IDocument document = textViewer.getDocument();
		
		if (document == null) {
			return null;
		}
		
		IFile currentFile = DocumentUtils.findFileFor(document);
		if (currentFile != null && FileUtils.isMarkdownFile(currentFile)) {
			// ignore links in Markdown files, since we have another hyperlink detector registered for Markdown files
			return null;
		}
		
		IHyperlink[] result= super.detectHyperlinks(textViewer, region, canShowMultipleHyperlinks);
		if (result == null) {
			return null;
		}

		for (int i= 0; i < result.length; i++) {
			org.eclipse.jface.text.hyperlink.URLHyperlink hyperlink= (org.eclipse.jface.text.hyperlink.URLHyperlink)result[i];
			result[i]= new URLHyperlink(hyperlink.getHyperlinkRegion(), hyperlink.getURLString());
		}

		return result;
	}
	
}
