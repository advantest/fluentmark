/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.core.markdown.model;

import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import java.util.ArrayList;
import java.util.List;

public class UpdateJob extends Job {

	private static final long DELAY = 1000L;
	private final List<Task> queue = new ArrayList<>();

	public UpdateJob(String name) {
		super(name);
	}

	public void trigger(PageRoot root, IResource res, String text) {
		synchronized (queue) {
			queue.add(new Task(root, res, text));
			schedule(DELAY);
		}
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Task task;
		synchronized (queue) {
			if (queue.size() <= 0) {
				return Status.CANCEL_STATUS;
			}
			task = queue.get(queue.size() - 1);
			queue.clear();
		}

		try {
			task.root.updateModel(task.res, task.text);
		} catch (CoreException e) {
			return e.getStatus();
		}
		
		return Status.OK_STATUS;
	}

	private class Task {
		PageRoot root;
		IResource res;
		String text;

		Task(PageRoot root, IResource res, String text) {
			this.root = root;
			this.res = res;
			this.text = text;
		}
	}

}
