package net.certiv.fluentmark.ui.refactoring;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import net.certiv.fluentmark.core.util.FileUtils;

public class InlinePlantUmlCodeRefactoring extends AbstractMarkdownRefactoring {
	
	private final static String MSG_INLINE_CODE = "Replace *.puml references (images) with in-lined PlantUML code blocks in Markdown files";
	private final static String MSG_AND_DELETE_PUMLS = " and remove the no longer referenced *.puml files";
	
	private boolean deleteObsoletePlantUmlFiles = true;
	private boolean useFencedCodeBlocks = true;
	
	/*
	 * Questions to clarify:
	 * - use current text selection or a set of directories, projects, and Markdown /PlantUML files as starting point?
	 * - current puml reference only or all uses of referenced puml file?
	 * - also delete no longer referenced puml files?
	 * - plain PlantUML code blocks or fenced PlantUML code blocks?
	 */
	
	public InlinePlantUmlCodeRefactoring(IFile markdownFile, IDocument document, ITextSelection textSelection) {
		super(markdownFile, document, textSelection);
	}
	
	public InlinePlantUmlCodeRefactoring(List<IResource> rootResources) {
		super(rootResources);
	}
	
	@Override
	public String getName() {
		return MSG_INLINE_CODE + MSG_AND_DELETE_PUMLS;
	}
	
	public void setDeletePumlFiles(boolean deleteObsoletePlantUmlFiles) {
		this.deleteObsoletePlantUmlFiles = deleteObsoletePlantUmlFiles;
	}
	
	public void setUseFencedCodeBlocks(boolean useFencedCodeBlocks) {
		this.useFencedCodeBlocks = useFencedCodeBlocks;
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		
		for (IResource rootResource: rootResources) {
			if (deleteObsoletePlantUmlFiles && !(rootResource instanceof IProject)) {
				IFolder parentDocFolder = FileUtils.getParentDocFolder(rootResource);
				if (parentDocFolder != null && !rootResource.equals(parentDocFolder)) {
					return RefactoringStatus.createWarningStatus("There might be Markdown files in other folders"
							+ " of your documentation that point to *.puml files that you are going to delete."
							+ " Avoid that by selecting your selected resource's (" + rootResource.getFullPath() + ") parent project or documentation folder "
							+ parentDocFolder.getFullPath().toString());
				}
			}
		}
		
		return new RefactoringStatus(); // ok status -> go to preview page, no error page
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		// TODO Auto-generated method stub
		return null;
	}

}
