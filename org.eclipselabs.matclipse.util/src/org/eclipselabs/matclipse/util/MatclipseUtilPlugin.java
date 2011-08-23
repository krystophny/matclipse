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
package org.eclipselabs.matclipse.util;



import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MatclipseUtilPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipselabs.matclipse.util";

	// The shared instance
	private static MatclipseUtilPlugin plugin;
	
	/**
	 * The constructor
	 */
	public MatclipseUtilPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public void errorDialog(final String message, final Throwable t) {
		Display disp = Display.getDefault();
		disp.asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = getDefault().getWorkbench()
				.getActiveWorkbenchWindow();
				Shell shell = window == null ? null : window.getShell();
				if (shell != null) {
					MultiStatus errorStatus=logStatus(message, t, IStatus.ERROR);
					logError(errorStatus);
					ErrorDialog.openError(
							shell, "Matclipse error", message, errorStatus );
				}
			}
		});
	}
	
	private MultiStatus logStatus(String message, final Throwable t, int code) {
		MultiStatus status = new MultiStatus(getPluginID(),
				code, message, t);
		String symbolicName = getDefault().getBundle().getSymbolicName();
		status.add(new Status(code, getPluginID(), 0,
				"Plugin ID: " + symbolicName, null));
		if (t!=null){
			StackTraceElement[] stackTrace = t.getStackTrace();
			StringBuffer stackTraceString = new StringBuffer("Stack trace:");
			for (StackTraceElement currentLine : stackTrace){
				stackTraceString.append("    \n" + currentLine.toString());
			}
			status.add(new Status(code, getPluginID(), 0,
					stackTraceString.toString(), null));
		}
		return status;
	}
	
	public void logError(final Status status) {
		getLog().log(status);
	}
	
	public void logError(final String message, final Throwable t) {
		getLog().log(logStatus(message, t, IStatus.ERROR));
	}
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MatclipseUtilPlugin getDefault() {
		return plugin;
	}
	public static String getPluginID() {
		return getDefault().getBundle().getSymbolicName();
	}
	
}
