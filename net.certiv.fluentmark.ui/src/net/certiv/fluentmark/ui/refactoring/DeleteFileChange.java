/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2025 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.refactoring;

import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.resource.DeleteResourceChange;
import org.eclipse.ltk.internal.core.refactoring.BasicElementLabels;
import org.eclipse.ltk.internal.core.refactoring.Messages;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;
import org.eclipse.ltk.internal.core.refactoring.resource.UndoDeleteResourceChange;
import org.eclipse.ltk.internal.core.refactoring.resource.undostates.FileUndoState;

import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.ui.FluentUI;

public class DeleteFileChange extends DeleteResourceChange {
	
	private final IPath filePath;
	private final boolean forceOutOfSync;

	public DeleteFileChange(IPath resourcePath, boolean forceOutOfSync) {
		super(resourcePath, forceOutOfSync, false);
		this.filePath = resourcePath;
		this.forceOutOfSync = forceOutOfSync;
	}
	
	public IPath getResourcePath() {
		return filePath;
	}
	
	@Override
	protected IFile getModifiedResource() {
		return getModifiedFile();
	}

	protected IFile getModifiedFile() {
		if (filePath != null && filePath.isAbsolute()) {
			List<IFile> filesFound = FileUtils.findFilesForLocation(filePath);
			if (!filesFound.isEmpty() && filesFound.size() == 1) {
				return filesFound.getFirst();
			}
		}
		
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(filePath);
		if (resource instanceof IFile) {
			return (IFile) resource;
		}
		
		return null;
	}
	
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		SubMonitor subMonitor= SubMonitor.convert(pm, RefactoringCoreMessages.DeleteResourceChange_deleting, 10);

		IFile file = getModifiedFile();
		if (file == null || !file.exists()) {
			String message= Messages.format(RefactoringCoreMessages.DeleteResourceChange_error_resource_not_exists, BasicElementLabels.getPathLabel(filePath.makeRelative(), false));
			throw new CoreException(new Status(IStatus.ERROR, FluentUI.PLUGIN_ID, message));
		}

		// make sure all files inside the resource are saved so restoring works
		try {
			saveFileIfNeeded(file, new NullProgressMonitor());
		} catch (CoreException e) {
			// ignore
		}
		
		FileUndoState desc = new FileUndoState(file);
		int updateFlags;
		if (forceOutOfSync) {
			updateFlags= IResource.KEEP_HISTORY | IResource.FORCE;
		} else {
			updateFlags= IResource.KEEP_HISTORY;
		}
		file.delete(updateFlags, subMonitor.newChild(5));
		desc.recordStateFromHistory(file, subMonitor.newChild(5));
		
		return new UndoDeleteResourceChange(desc);
	}
	
	private static void saveFileIfNeeded(IFile file, IProgressMonitor pm) throws CoreException {
		ITextFileBuffer buffer= FileBuffers.getTextFileBufferManager().getTextFileBuffer(file.getFullPath(), LocationKind.IFILE);
		SubMonitor subMonitor= SubMonitor.convert(pm, 2);
		if (buffer != null && buffer.isDirty() && buffer.isStateValidated() && buffer.isSynchronized()) {
			buffer.commit(subMonitor.newChild(1), false);
			file.refreshLocal(IResource.DEPTH_ONE, subMonitor.newChild(1));
			buffer.commit(subMonitor.newChild(1), false);
			file.refreshLocal(IResource.DEPTH_ONE, subMonitor.newChild(1));
		} else {
			subMonitor.worked(2);
		}
	}
}
