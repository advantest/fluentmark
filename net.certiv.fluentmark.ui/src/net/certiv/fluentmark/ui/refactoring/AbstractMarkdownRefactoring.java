package net.certiv.fluentmark.ui.refactoring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import com.advantest.markdown.MarkdownParserAndHtmlRenderer;

public abstract class AbstractMarkdownRefactoring extends Refactoring {
	
	protected final Set<IResource> rootResources = new HashSet<>();
	protected IDocument markdownFileDocument;
	protected ITextSelection textSelection;
	protected final MarkdownParserAndHtmlRenderer markdownParser = new MarkdownParserAndHtmlRenderer();
	
	public AbstractMarkdownRefactoring(IFile markdownFile, IDocument document, ITextSelection textSelection) {
		this.rootResources.add(markdownFile);
		this.markdownFileDocument = document;
		this.textSelection = textSelection;
	}
	
	public AbstractMarkdownRefactoring(List<IResource> rootResources) {
		this.rootResources.addAll(rootResources);
	}
	
	public boolean hasTextSelection() {
		return textSelection != null;
	}
	
	public Set<IResource> getRootResources() {
		return this.rootResources;
	}
	
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		for (IResource rootResource: rootResources) {
			if (rootResource == null || !rootResource.exists() || !rootResource.isAccessible()) {
				return RefactoringStatus.createFatalErrorStatus("Refactoring not applicable to the given resource."
						+ " The following resource is not accessible. Resource: " + rootResource);
			}
		}
		
		return new RefactoringStatus(); // ok status -> go to preview page, no error page
	}

}
