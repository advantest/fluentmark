/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.preferences;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Adapts {@link org.eclipse.core.runtime.Preferences} to
 * {@link org.eclipse.jface.preference.IPreferenceStore}
 *
 * @since 3.0
 */
@SuppressWarnings("deprecation")
public class PreferencesAdapter implements IPreferenceStore {

	/**
	 * Property change listener. Listens for events of type
	 * {@link org.eclipse.core.runtime.Preferences.PropertyChangeEvent} and fires a
	 * {@link org.eclipse.jface.util.PropertyChangeEvent} on the adapter with arguments from the
	 * received event.
	 */
	private class PropertyChangeListener implements Preferences.IPropertyChangeListener {

		@Override
		public void propertyChange(Preferences.PropertyChangeEvent event) {
			firePropertyChangeEvent(event.getProperty(), event.getOldValue(), event.getNewValue());
		}
	}

	/** Listeners on the adapter */
	private ListenerList<IPropertyChangeListener> fListeners = new ListenerList<>(ListenerList.IDENTITY);

	/** Listener on the adapted OpenStylesheetsHandler */
	private PropertyChangeListener fListener = new PropertyChangeListener();

	/** Adapted OpenStylesheetsHandler */
	private Preferences fPreferences;

	/** True iff no events should be forwarded */
	private boolean fSilent;

	/**
	 * Initialize with empty OpenStylesheetsHandler.
	 */
	public PreferencesAdapter() {
		this(new Preferences());
	}

	/**
	 * Initialize with the given OpenStylesheetsHandler.
	 *
	 * @param preferences The preferences to wrap.
	 */
	public PreferencesAdapter(Preferences preferences) {
		fPreferences = preferences;
	}

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (fListeners.size() == 0) fPreferences.addPropertyChangeListener(fListener);
		fListeners.add(listener);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
		if (fListeners.size() == 0) fPreferences.removePropertyChangeListener(fListener);
	}

	@Override
	public boolean contains(String name) {
		return fPreferences.contains(name);
	}

	@Override
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		if (!fSilent) {
			final PropertyChangeEvent event = new PropertyChangeEvent(this, name, oldValue, newValue);
			for (final IPropertyChangeListener listener : fListeners) {
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						listener.propertyChange(event);
					}
				};

				if (Display.getCurrent() != null)
					runnable.run();
				else {
					// Post runnable into UI thread
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					Display display;
					if (shell != null)
						display = shell.getDisplay();
					else
						display = Display.getDefault();
					display.asyncExec(runnable);
				}
			}
		}
	}

	@Override
	public boolean getBoolean(String name) {
		return fPreferences.getBoolean(name);
	}

	@Override
	public boolean getDefaultBoolean(String name) {
		return fPreferences.getDefaultBoolean(name);
	}

	@Override
	public double getDefaultDouble(String name) {
		return fPreferences.getDefaultDouble(name);
	}

	@Override
	public float getDefaultFloat(String name) {
		return fPreferences.getDefaultFloat(name);
	}

	@Override
	public int getDefaultInt(String name) {
		return fPreferences.getDefaultInt(name);
	}

	@Override
	public long getDefaultLong(String name) {
		return fPreferences.getDefaultLong(name);
	}

	@Override
	public String getDefaultString(String name) {
		return fPreferences.getDefaultString(name);
	}

	@Override
	public double getDouble(String name) {
		return fPreferences.getDouble(name);
	}

	@Override
	public float getFloat(String name) {
		return fPreferences.getFloat(name);
	}

	@Override
	public int getInt(String name) {
		return fPreferences.getInt(name);
	}

	@Override
	public long getLong(String name) {
		return fPreferences.getLong(name);
	}

	@Override
	public String getString(String name) {
		return fPreferences.getString(name);
	}

	@Override
	public boolean isDefault(String name) {
		return fPreferences.isDefault(name);
	}

	@Override
	public boolean needsSaving() {
		return fPreferences.needsSaving();
	}

	@Override
	public void putValue(String name, String value) {
		try {
			fSilent = true;
			fPreferences.setValue(name, value);
		} finally {
			fSilent = false;
		}
	}

	@Override
	public void setDefault(String name, double value) {
		fPreferences.setDefault(name, value);
	}

	@Override
	public void setDefault(String name, float value) {
		fPreferences.setDefault(name, value);
	}

	@Override
	public void setDefault(String name, int value) {
		fPreferences.setDefault(name, value);
	}

	@Override
	public void setDefault(String name, long value) {
		fPreferences.setDefault(name, value);
	}

	@Override
	public void setDefault(String name, String defaultObject) {
		fPreferences.setDefault(name, defaultObject);
	}

	@Override
	public void setDefault(String name, boolean value) {
		fPreferences.setDefault(name, value);
	}

	@Override
	public void setToDefault(String name) {
		fPreferences.setToDefault(name);
	}

	@Override
	public void setValue(String name, double value) {
		fPreferences.setValue(name, value);
	}

	@Override
	public void setValue(String name, float value) {
		fPreferences.setValue(name, value);
	}

	@Override
	public void setValue(String name, int value) {
		fPreferences.setValue(name, value);
	}

	@Override
	public void setValue(String name, long value) {
		fPreferences.setValue(name, value);
	}

	@Override
	public void setValue(String name, String value) {
		fPreferences.setValue(name, value);
	}

	@Override
	public void setValue(String name, boolean value) {
		fPreferences.setValue(name, value);
	}
}
