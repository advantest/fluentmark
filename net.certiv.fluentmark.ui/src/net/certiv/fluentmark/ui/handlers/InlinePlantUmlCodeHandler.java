package net.certiv.fluentmark.ui.handlers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.handlers.HandlerUtil;

import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.refactoring.InlinePlantUmlCodeRefactoring;
import net.certiv.fluentmark.ui.refactoring.wizards.InlinePlantUmlCodeWizard;
import net.certiv.fluentmark.ui.util.EditorUtils;

// TODO avoid code duplication, see ReplaceSvgImagesWithPlantUmlImagesHandler
public class InlinePlantUmlCodeHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			
			InlinePlantUmlCodeRefactoring refactoring = null;
			String dialogTile = "Refactoring";
			
			if (selection instanceof ITextSelection) {
				// work-around, since HandlerUtil.getCurrentSelection(event) sometimes does not return valid offset values
				ISelection currentSelection = EditorUtils.getCurrentSelection();
				if (!(currentSelection instanceof ITextSelection)) {
					return null;
				}
				
				ITextSelection textSelection = (ITextSelection) currentSelection;
				
				FluentEditor fluentEditor = EditorUtils.getActiveFluentEditor();
				if (fluentEditor != null) {
					IFile markdownFile = fluentEditor.getEditorInputFile();
					if (markdownFile != null ) {
						dialogTile = "Inline PlantUML code";
						refactoring = new InlinePlantUmlCodeRefactoring(markdownFile, fluentEditor.getDocument(), textSelection);
					}
				}
			} else {
				IStructuredSelection structuredSelection = HandlerUtil.getCurrentStructuredSelection(event);
				
				if (structuredSelection != null && !structuredSelection.isEmpty()) {
					List<IResource> rootResources = structuredSelection.stream()
						.map(s -> getResourceForSelectedElement(s))
						.filter(Objects::nonNull)
						.collect(Collectors.toList());
					
					if (rootResources.isEmpty()) {
						MessageDialog dialog = new MessageDialog(
								HandlerUtil.getActiveShell(event), "Operation failed", null,
								"Could not perform refactoring operation. No directory or file found in the tree selection.",
								MessageDialog.ERROR, 0, "Close");
						dialog.open();
						return null;
					}
					
					dialogTile = "Inline PlantUML code from referenced *.puml files";
					refactoring = new InlinePlantUmlCodeRefactoring(rootResources);
				}
			}
			
			if (refactoring != null) {
				InlinePlantUmlCodeWizard wizard = new InlinePlantUmlCodeWizard(refactoring);
				RefactoringWizardOpenOperation refactoringOperation = new RefactoringWizardOpenOperation(wizard);
				
				refactoringOperation.run(HandlerUtil.getActiveShell(event), dialogTile);
			}
		} catch (InterruptedException e) {
			return null; // User action got cancelled
		}
		
		return null;
	}
	
	private IResource getResourceForSelectedElement(Object selectedElement) {
		if (selectedElement != null) {
			IAdapterManager adapterManager = Platform.getAdapterManager();
			if (adapterManager != null) {
				return adapterManager.getAdapter(selectedElement, IResource.class);
			}
		}
		return null;
	}
}
