/*******************************************************************************
 * Copyright (c) 2006, 2011 Graz University of Technology,
 * Institute of Theoretical and Computational Physics (ITPCP) 
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Camhy, Winfried Kernbichler, Georg Huhs (ITPCP) - 
 *        initial API and implementation
 *     Christopher Albert (ITPCP) - refactoring
 *******************************************************************************/
package org.eclipselabs.matclipse.mconsole;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.IPreferenceConstants;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.ThemeWrapper;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabDataParent;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabInterpreter;
import org.eclipselabs.matclipse.mconsole.matlab.impl.OctaveInterpreter;
import org.eclipselabs.matclipse.mconsole.matlab.impl.UnixMatlabInterpreter;
import org.eclipselabs.matclipse.mconsole.views.CommandHistoryEntry;
import org.eclipselabs.matclipse.mconsole.views.MatlabConsoleDebugView;
import org.eclipselabs.matclipse.util.MatclipseUtilPlugin;
import org.osgi.framework.BundleContext;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The activator class controls the plug-in life cycle
 */
public class MConsolePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipselabs.matclipse.mconsole";
	// This is the ID from your extension point
	private static final String INTERPRETER_ID = "org.eclipselabs.matclipse.mconsole.matlabInterpreter";
	private static String system;
	// The shared instance
	private static MConsolePlugin plugin;
	private MatlabInterpreter matlab;
	private boolean matlabInitialized = false;
	private MatlabDataParent TestResults;
	private Vector<CommandHistoryEntry> commandHistory;
	private final XStream xstream = new XStream(new DomDriver());

	/**
	 * The constructor
	 */
	public MConsolePlugin() {
		system = System.getProperty("os.name").toLowerCase();
		xstream.alias("commandHistoryEntry", CommandHistoryEntry.class);
		this.commandHistory = new Vector<CommandHistoryEntry>();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		if (MConsolePlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceConstants.P_REMEMBERCOMMANDHISTORY)) {
			try {
				commandHistory = (Vector<CommandHistoryEntry>) xstream
						.fromXML(getPreferenceStore().getString(
								IPreferenceConstants.P_COMMANDHISTORY));
			} catch (Exception e) {
			}

			Date itIsNow = new Date(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			String formattedDateString = sdf.format(itIsNow);
			addCommandHistoryEntry(new CommandHistoryEntry("%--"
					+ formattedDateString + "--%", true));
		}
		startMatlab();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;

		try {
			matlab.stop();
			getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage()
					.hideView(MatlabConsoleDebugView.getDefault());
		} catch (Exception e) {

		}
		super.stop(context);
	}

	public MatlabDataParent getTestResults() {
		// System.out.println("Here="+TestResults);
		return TestResults;
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static MConsolePlugin getDefault() {
		return plugin;
	}

	public void startMatlab() {
		if (matlab == null) {
			String interpreter = getPreferenceStore().getString(
					IPreferenceConstants.P_MATLABINTERPRETER);
			if (interpreter.equals("octave"))
				matlab = new OctaveInterpreter();
			else if (getSystem().contains("windows")) {
				
				loadInterpreterExtension();
				System.out.println("WINDOWS");
			}
			else
				matlab = new UnixMatlabInterpreter();

		}
		try {
			matlab.start();
		} catch (CoreException e) {
			MatclipseUtilPlugin.getDefault().errorDialog(
					"Problem starting Matlab", e);
		}
	}

	private void loadInterpreterExtension() {
		try {
			IConfigurationElement[] config = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(INTERPRETER_ID);
			for (IConfigurationElement e : config) {
				if (Platform.inDebugMode())
					System.out.println("Loading MatlabInterpreter");
				final Object o = e.createExecutableExtension("class");
				if (o instanceof MatlabInterpreter) {
					ISafeRunnable runnable = new ISafeRunnable() {
						public void handleException(Throwable exception) {
							getLog().log(
									new Status(
											IStatus.ERROR,
											PLUGIN_ID,
											IStatus.OK,
											"Exception loading MatlabInterpreter",
											exception));
						}

						public void run() throws Exception {
							matlab = (MatlabInterpreter) o;
						}
					};
					SafeRunner.run(runnable);
				}
			}
		} catch (CoreException ex) {
			MatclipseUtilPlugin.getDefault().errorDialog(
					"Problem starting Matlab", ex);
		}
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static String getSystem() {
		return system;
	}

	public MatlabInterpreter getMatlab() {
		return matlab;
	}

	public static String getMatlabLibPath() {

		URL bundleURL = getDefault().getBundle().getEntry("m-files");
		URL fileURL;
		try {
			URL asLocalURL2 = FileLocator.toFileURL(bundleURL);
			fileURL = asLocalURL2;
			String f = fileURL.getPath();
			if (getSystem().contains("windows"))
				f = f.substring(1);
			return f;
		} catch (IOException e) {
			return "";
		}
	}

	public static String getPluginID() {
		return getDefault().getBundle().getSymbolicName();
	}

	public Vector<CommandHistoryEntry> getCommandHistory() {
		return commandHistory;
	}

	public void updatePreferences() {
		String xml = xstream.toXML(commandHistory);
		if (MConsolePlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceConstants.P_REMEMBERCOMMANDHISTORY)) {
			MConsolePlugin.getDefault().getPreferenceStore()
					.setValue(IPreferenceConstants.P_COMMANDHISTORY, xml);
		}
	}

	public void addCommandHistoryEntry(CommandHistoryEntry entry) {
		commandHistory.add(entry);
		updatePreferences();
	}

	public void deleteCommandHistoryEntry(CommandHistoryEntry entry) {
		try {
			commandHistory.remove(entry);
			updatePreferences();
		} catch (Exception e) {

		}

	}

	public void setCommandHistory(Vector<CommandHistoryEntry> commandHistory) {
		this.commandHistory = commandHistory;
		updatePreferences();
	}

	public void waitForAutoBuild() {

		boolean wasInterrupted = false;
		do {
			try {
				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
						null);
				wasInterrupted = false;
			} catch (OperationCanceledException e) {
				throw (e);
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		} while (wasInterrupted);

	}

	public boolean isMatlabInitialized() {
		return matlabInitialized;
	}

	public void setMatlabInitialized(boolean matlabInitialized) {
		this.matlabInitialized = matlabInitialized;
	}

	public ThemeWrapper getCurrentTheme() {
		return new ThemeWrapper(MConsolePlugin.getDefault().getWorkbench()
				.getThemeManager().getCurrentTheme());
	}

	public void setTestResults(MatlabDataParent testResults) {
		TestResults = testResults;
	}

}
