/*******************************************************************************
 * Copyright (c) 2006, 2011 Institute of Theoretical and Computational Physics (ITPCP), 
 * Graz University of Technology.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Georg Huhs, Winfried Kernbichler, David Camhy (ITPCP) - 
 *         initial API and implementation
 * Last changed: 
 *     2008-01-22
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor;


import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class Activator extends AbstractUIPlugin implements Preferences.IPropertyChangeListener  {

    //The shared instance.
    private static Activator plugin;
    
    public final static String CONFIGDIR = "config";
    public final static String BASEDIR   = "";
    
    
    public Activator() {
        plugin = this;
    }


    /**
     * This method is called upon plug-in activation
     */
    protected void initializeDefaultPluginPreferences() {
        MatlabenginePrefsPage.initializeDefaultPreferences(getPluginPreferences());
        MatlabengineExportPrefsPage.initializeDefaultPreferences(getPluginPreferences());
        HelpBrowserPrefsPage.initializeDefaultPreferences(getPluginPreferences());
    }
    
    
    public void start(BundleContext context) throws Exception {
        super.start(context);
        Preferences preferences = plugin.getPluginPreferences();
        preferences.addPropertyChangeListener(this);
    }
    
    
    public void propertyChange(Preferences.PropertyChangeEvent event) {
    }

    
    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
       try {
            Preferences preferences = plugin.getPluginPreferences();
            preferences.removePropertyChangeListener(this);
        } finally{
            super.stop(context);
        }
    }


    /**
     * Returns the shared instance.
     */
    public static Activator getDefault() {
        return plugin;
    }

    
    /**
     * Returns an image descriptor for the image file at the given
     * plugin relative path.
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipselabs.matclipse.meditor", path);
    }
    
    
    public static void log(int errorLevel, String message, Throwable e) {
            Status s = new Status(errorLevel, getPluginID(), errorLevel, message, e);
            getDefault().getLog().log(s);
    }

    
    public static void log(Throwable e) {
        log(IStatus.ERROR, e.getMessage() != null ? e.getMessage() : "No message gotten.", e);
    }

    
    public static String getPluginID() {
        return getDefault().getBundle().getSymbolicName();
    }
    
    /**
     * Returns an absolute path to the specified plugin-subdirectory.
     * @param dir Directory to find the path to. Needs to be a direct subdirectory of the
     * plugin directory. 
     * @return absolute path to the given directory
     * @throws IOException
     */
    public String getPluginDir(String dir) throws IOException{
        IPath relative = new Path(dir).addTrailingSeparator();
        Bundle bundle = Activator.getDefault().getBundle();
        
        URL bundleURL = FileLocator.find(bundle, relative, null);
        URL fileURL = FileLocator.toFileURL(bundleURL);
        return fileURL.getPath();
    }
    
    
    public static void beep(Exception e) {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay().beep();
        e.printStackTrace();
    }
    
    /**
     * Creates and displays an error dialog with as much debug information as possible. 
     * @param message Error message to display. 
     * @param error Cause of the error
     */
    public static void errorDialog(final String message, final Throwable error) {
        Display disp = Display.getDefault();
        disp.asyncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = getDefault().getWorkbench()
                        .getActiveWorkbenchWindow();
                Shell shell = window == null ? null : window.getShell();
                if (shell != null) {
                    MultiStatus status = new MultiStatus(Activator.getPluginID(), 
                            IStatus.ERROR, message, null);
                    if (error!=null){
                        status.add(new Status(
                            IStatus.ERROR, Activator.getPluginID(), 0, error.toString(), null));

                        Throwable cause = error;
                        Throwable initialCause = error;
                        String indent = "  ";
                        String indentSum = indent;
                        while ((cause = cause.getCause()) != null){
                            status.add(new Status(
                                IStatus.ERROR, 
                                Activator.getPluginID(), 
                                0, 
                                indentSum + "caused by " + cause.getMessage(), 
                                null));
                            indentSum += indent;
                            initialCause = cause;
                        }
                        
                        StackTraceElement[] stackTrace = initialCause.getStackTrace();
                        StringBuffer stackTraceString = new StringBuffer("Stack trace:");
                        for (StackTraceElement currentLine : stackTrace){
                            stackTraceString.append("\n    " + currentLine.toString());
                        }
                        status.add(new Status(IStatus.ERROR, Activator.getPluginID(), 0, 
                                stackTraceString.toString(), null));
                    } else { // there has to be at least one additional status to produce a working error dialog
                        String symbolicName = Activator.getDefault().getBundle().getSymbolicName();
                        status.add(new Status(IStatus.ERROR, Activator.getPluginID(), 0, 
                                        "Plugin ID: " + symbolicName, null));
                    }
                    ErrorDialog.openError(
                            shell, "Matlab editor error", "Error logged from Matclipse", status);
                }
            }
        });
     } 

    
    /**
     * Creates and displays a warning dialog. 
     * @param message Warning message to display. 
     * @param error Cause of the warning
     */
    public static void warningDialog(final String message, final Throwable warning) {
        Display disp = Display.getDefault();
        disp.asyncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = getDefault().getWorkbench()
                        .getActiveWorkbenchWindow();
                Shell shell = window == null ? null : window.getShell();
                if (shell != null) {

                    Status status = new Status(
                        IStatus.WARNING, Activator.getPluginID(), 0, message, warning);
                    ErrorDialog.openError(
                        shell, "Matlab editor warning", "Warning from Matclipse:", status);
                }
            }
        });
     } 
    
    /**
     * Generates a standard String for messages that say that a specific file wasn't found. 
     * @param file File that hasn't been found.
     * @param additionalMessage An additional message to show.
     * @return the generated String.
     */
    public static String fileNotFoundWarningString(String file, String additionalMessage){
        String message = "Wasn't able to find file: \n" + file;
        if (additionalMessage != null){
            message += "\n" + additionalMessage;
        }
        return message;
    }
}
