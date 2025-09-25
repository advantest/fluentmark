/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.refactoring.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.DrillDownComposite;

import net.certiv.fluentmark.ui.refactoring.ExtractPlantUmlDiagramFileRefactoring;

public class ExtractPlantUmlCodeCreatePumlFileWizardPage extends UserInputWizardPage {
	
	private final ExtractPlantUmlDiagramFileRefactoring refactoring;
	
	private Text fileNameTextField;
	private TreeViewer directorySelectionTreeViewer;
	private IContainer selectedContainer = null;

	public ExtractPlantUmlCodeCreatePumlFileWizardPage(ExtractPlantUmlDiagramFileRefactoring refactoring) {
		super ("User Input Page");
		this.refactoring = refactoring;
		
		this.setTitle("Extract the selected PlantUML code block to a new diagram file");
		this.setDescription("Create a new file with file extension .puml,"
				+ " write the selected PlantUML code into that file,"
				+ "\nand replace the code block with a reference to the new file.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setFont(parent.getFont());
		
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Please choose a directory where to place the new PlantUML file (*.puml):");
		
		DrillDownComposite drillDown = new DrillDownComposite(composite, SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = 100;
		drillDown.setLayoutData(layoutData);
		
		directorySelectionTreeViewer = new TreeViewer(drillDown, SWT.NONE);
		drillDown.setChildTree(directorySelectionTreeViewer);
		
		ParentDirectoryTreeContentProvider contentProvider = new ParentDirectoryTreeContentProvider();
		directorySelectionTreeViewer.setContentProvider(contentProvider);
		directorySelectionTreeViewer.setLabelProvider(WorkbenchLabelProvider
				.getDecoratingWorkbenchLabelProvider());
		directorySelectionTreeViewer.setComparator(new ViewerComparator());
		directorySelectionTreeViewer.setUseHashlookup(true);
		directorySelectionTreeViewer.addSelectionChangedListener(event -> {
			IStructuredSelection selection = event.getStructuredSelection();
			selectedContainer = (IContainer) selection.getFirstElement();
		});
		
		directorySelectionTreeViewer.addDoubleClickListener(event -> {
			ISelection selection = event.getSelection();
			if (selection instanceof IStructuredSelection) {
				Object item = ((IStructuredSelection) selection)
						.getFirstElement();
				if (item == null) {
					return;
				}
				if (directorySelectionTreeViewer.getExpandedState(item)) {
					directorySelectionTreeViewer.collapseToLevel(item, 1);
				} else {
					directorySelectionTreeViewer.expandToLevel(item, 1);
				}
			}
		});
		
		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText("Please enter a name for the new PlantUML diagram file:");
		
		fileNameTextField = new Text(composite, SWT.BORDER);
		fileNameTextField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		fileNameTextField.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event event) {
				// TODO Auto-generated method stub
			}
		});
		fileNameTextField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// TODO set resource extension if missing
			}
		});
		fileNameTextField.setText("diagram.puml");
		
		// TODO select the current Markdown file's parent project instead?
		directorySelectionTreeViewer.setInput(ResourcesPlugin.getWorkspace());
		setSelectedContainer(refactoring.getSuggestedParentForNewPlantUmlFile());
		
		directorySelectionTreeViewer.getTree().setFocus();
		
		setControl(composite);
	}
	
	private void setSelectedContainer(IContainer container) {
		selectedContainer = container;

		// expand to and select the specified container
		List<IContainer> itemsToExpand = new ArrayList<>();
		IContainer parent = container.getParent();
		while (parent != null) {
			itemsToExpand.add(0, parent);
			parent = parent.getParent();
		}
		directorySelectionTreeViewer.setExpandedElements(itemsToExpand.toArray());
		directorySelectionTreeViewer.setSelection(new StructuredSelection(container), true);
	}
	
	private static class ParentDirectoryTreeContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IWorkspace) {
				IProject[] allProjects = ((IWorkspace) parentElement).getRoot().getProjects();

				ArrayList<IProject> accessibleProjects = new ArrayList<>();
				for (IProject project : allProjects) {
					if (project.isOpen()) {
						accessibleProjects.add(project);
					}
				}
				return accessibleProjects.toArray();
			} else if (parentElement instanceof IContainer) {
				IContainer container = (IContainer) parentElement;
				if (container.isAccessible()) {
					try {
						List<IResource> children = new ArrayList<>();
						for (IResource member : container.members()) {
							if (member.getType() != IResource.FILE) {
								children.add(member);
							}
						}
						return children.toArray();
					} catch (CoreException e) {
						// this should never happen because we call #isAccessible before invoking #members
					}
				}
			}
			return new Object[0];
		}
		
		@Override
		public Object[] getElements(Object element) {
			return getChildren(element);
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IResource) {
				return ((IResource) element).getParent();
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
		
	}

}
