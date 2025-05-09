/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
/**
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package net.certiv.fluentmark.ui;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.HyperlinkDetectorDescriptor;
import org.eclipse.ui.texteditor.HyperlinkDetectorRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import net.certiv.fluentmark.core.convert.Converter;
import net.certiv.fluentmark.ui.editor.ConfigurationProvider;
import net.certiv.fluentmark.ui.editor.color.ColorManager;
import net.certiv.fluentmark.ui.editor.color.IColorManager;
import net.certiv.fluentmark.ui.preferences.Prefs;

/**
 * The activator class controls the plug-in life cycle
 */
public class FluentUI extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.certiv.fluentmark.ui"; //$NON-NLS-1$

	// The shared instance
	private static FluentUI plugin;

	private IPreferenceStore combinedStore;
	private FluentImages fluentImages;
	private FormToolkit dialogsFormToolkit;
	private ColorManager colorManager;
	private Converter converter;

	public FluentUI() {
		super();
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		fluentImages = new FluentImages(context.getBundle(), this);
		
		converter = new Converter(new ConfigurationProvider());
		
		// Switch off the default URLHyperlinkDetector that does not correctly detect Hyperlinks in Markdown files,
		// see javadoc in org.eclipse.ui.texteditor.HyperlinkDetectorRegistry#HyperlinkDetectorRegistry(IPreferenceStore)
		// We replace this URLHyperlinkDetector with our own that is not applied to Markdown files.
		HyperlinkDetectorRegistry registry = EditorsUI.getHyperlinkDetectorRegistry();
		IPreferenceStore editorsUiPrefStore = EditorsUI.getPreferenceStore();
		for (HyperlinkDetectorDescriptor descriptor : registry.getHyperlinkDetectorDescriptors()) {
			if ("org.eclipse.ui.internal.editors.text.URLHyperlinkDetector".equals(descriptor.getId())) {
				editorsUiPrefStore.setValue(descriptor.getId(), true);
			}
		}

		// ISaveParticipant saveParticipant = new MyWorkspaceSaveParticipant();
		// ISavedState lastState = ResourcesPlugin.getWorkspace().addSaveParticipant(PLUGIN_ID,
		// saveParticipant);
		// if (lastState == null) return;
		//
		// IPath location = lastState.lookup(new Path("save"));
		// if (location == null) return;
		//
		// // the plugin instance should read any important state from the file.
		// File f = getStateLocation().append(location).toFile();
		// readStateFrom(f);

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 */
	public static FluentUI getDefault() {
		return plugin;
	}
	
	public Converter getConverter() {
		return converter;
	}

	public static void log(String msg) {
		log(IStatus.INFO, msg, null);
	}

	public static void log(int type, String msg) {
		log(type, msg, null);
	}

	public static void log(int type, String msg, Exception e) {
		plugin.getLog().log(new Status(type, PLUGIN_ID, IStatus.OK, msg, e));
	}

	public IColorManager getColorMgr() {
		if (colorManager == null) {
			colorManager = new ColorManager(true);
		}
		return colorManager;
	}

	/**
	 * Returns a chained preference store representing the combined values of the FluentUI, EditorsUI,
	 * and PlatformUI stores.
	 */
	public IPreferenceStore getCombinedPreferenceStore() {
		if (combinedStore == null) {
			List<IPreferenceStore> stores = new ArrayList<>();
			stores.add(getPreferenceStore()); // FluentUI store
			stores.add(EditorsUI.getPreferenceStore());
			stores.add(PlatformUI.getPreferenceStore());
			combinedStore = new WritableChainedPreferenceStore(stores.toArray(new IPreferenceStore[stores.size()]));
		}
		return combinedStore;
	}

	public FormToolkit getDialogsFormToolkit() {
		if (dialogsFormToolkit == null) {
			FormColors colors = new FormColors(Display.getCurrent());
			colors.setBackground(null);
			colors.setForeground(null);
			dialogsFormToolkit = new FormToolkit(colors);
		}
		return dialogsFormToolkit;
	}

	/** Returns the image provider */
	public FluentImages getImageProvider() {
		return fluentImages;
	}

	public static Image getImage(String key) {
		return plugin.fluentImages.get(key);
	}

	public static ImageDescriptor getDescriptor(String key) {
		return plugin.fluentImages.getDescriptor(key);
	}

	/**
	 * Returns the content assist additional info focus affordance string.
	 *
	 * @return the affordance string which is <code>null</code> if the preference is disabled
	 * @see EditorsUI#getTooltipAffordanceString()
	 * @since 3.4
	 */
	public static String getAdditionalInfoAffordanceString() {
		if (EditorsUI.getPreferenceStore().getBoolean(Prefs.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE)) {
			return "Press 'Tab' from proposal table or click for focus"; //$NON-NLS-1$
		}
		return null;
	}

	public IStatus createStatus(int statusCode, Throwable exception) {
		return createStatus(null, statusCode, exception);
	}

	public IStatus createStatus(String message, int statusCode, Throwable exception) {
		if (message == null && exception != null) {
			message = exception.getClass().getName() + ": " + exception.getMessage(); //$NON-NLS-1$
		}
		Status status = new Status(statusCode, PLUGIN_ID, statusCode, message, exception);
		return status;
	}

	/**
	 * Returns the workspace root default charset encoding.
	 *
	 * @return the name of the default charset encoding for workspace root.
	 * @see IContainer#getDefaultCharset()
	 * @see ResourcesPlugin#getEncoding()
	 */
	public static String getEncoding() {
		try {
			return ResourcesPlugin.getWorkspace().getRoot().getDefaultCharset();
		} catch (IllegalStateException e) {
			return System.getProperty("file.encoding"); //$NON-NLS-1$
		} catch (CoreException e) {
			return ResourcesPlugin.getEncoding();
		}
	}

	/**
	 * Flushes the instance scope of this plug-in.
	 */
	public static void flushInstanceScope() {
		try {
			InstanceScope.INSTANCE.getNode(PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
			Log.error(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static LinkedHashMap<String, String> getTemplateMap() {
		LinkedHashMap<String, String> map = null;
		File file = getTemplateStateFile();
		if (file != null && file.isFile()) {
			try (XMLDecoder coder = new XMLDecoder(new BufferedInputStream(new FileInputStream(file)));) {
				map = (LinkedHashMap<String, String>) coder.readObject();
			} catch (FileNotFoundException e) {}
		}
		if (map == null) {
			map = new LinkedHashMap<>();
		}
		return map;
	}

	public static boolean putTemplateMap(LinkedHashMap<String, String> map) {
		File file = getTemplateStateFile();
		try (XMLEncoder coder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));) {
			coder.writeObject(map);
			return true;
		} catch (FileNotFoundException e) {
			return false;
		}
	}

	private static File getTemplateStateFile() {
		return FluentUI.getDefault().getStateLocation().append("TemplateMap.xml").toFile();
	}
	
}
