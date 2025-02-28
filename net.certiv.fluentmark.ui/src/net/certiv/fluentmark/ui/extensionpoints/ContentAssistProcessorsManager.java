package net.certiv.fluentmark.ui.extensionpoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;

import net.certiv.fluentmark.ui.FluentUI;

public class ContentAssistProcessorsManager {
	
	private static final String EXTENSION_POINT_ID_URI_VALIDATOR = "net.certiv.fluentmark.ui.content.assist.processor";
	
	private static ContentAssistProcessorsManager INSTANCE = null;
	
	private final List<IContentAssistProcessor> additionalContentAssistProcessors = new ArrayList<>();
	
	public static ContentAssistProcessorsManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ContentAssistProcessorsManager();
		}
		return INSTANCE;
	}
	
	private ContentAssistProcessorsManager() {
		this.init();
	}
	
	private void init() {
		IExtensionPoint contentAssistProcessorExtensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID_URI_VALIDATOR);
		IExtension[] contentAssistProcessorExtensions = contentAssistProcessorExtensionPoint.getExtensions();
		for (IExtension contentAssistProcessorExtension: contentAssistProcessorExtensions) {
			IConfigurationElement[] configElements = contentAssistProcessorExtension.getConfigurationElements();
			
			try {
				for (IConfigurationElement configElement : configElements) {
					Object obj = configElement.createExecutableExtension("class");
					if (obj instanceof IContentAssistProcessor) {
						additionalContentAssistProcessors.add((IContentAssistProcessor) obj);
					}
				}
			} catch (CoreException e) {
				FluentUI.log(IStatus.ERROR, "Could not load IContentAssistProcessor from extension", e);
			}
		}
	}
	
	public List<IContentAssistProcessor> getAdditionalContentAssistProcessors() {
		return Collections.unmodifiableList(additionalContentAssistProcessors);
	}

}
