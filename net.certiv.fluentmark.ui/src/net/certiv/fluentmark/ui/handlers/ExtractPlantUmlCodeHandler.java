package net.certiv.fluentmark.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ui.handlers.HandlerUtil;

import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.refactoring.ExtractPlantUmlDiagramFileRefactoring;
import net.certiv.fluentmark.ui.refactoring.wizards.ExtractPlantUmlCodeWizard;
import net.certiv.fluentmark.ui.util.EditorUtils;

//TODO avoid code duplication, see InlinePlantUmlCodeHandler
public class ExtractPlantUmlCodeHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			ISelection selection = HandlerUtil.getCurrentSelection(event);
			
			ExtractPlantUmlDiagramFileRefactoring refactoring = null;
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
						dialogTile = "Extract PlantUML code";
						refactoring = new ExtractPlantUmlDiagramFileRefactoring(markdownFile, fluentEditor.getDocument(), textSelection);
					}
				}
			}
			
			if (refactoring != null) {
				ExtractPlantUmlCodeWizard wizard = new ExtractPlantUmlCodeWizard(refactoring);
				RefactoringWizardOpenOperation refactoringOperation = new RefactoringWizardOpenOperation(wizard);
				
				refactoringOperation.run(HandlerUtil.getActiveShell(event), dialogTile);
			}
		} catch (InterruptedException e) {
			return null; // User action got cancelled
		}
		
		return null;
	}
	
}
