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
 *     2008-01-23
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.util;


import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.HelpBrowserPrefsPage;


/**
 * Static class that provides functionality for launching webbrowsers in different ways.
 * @author Georg Huhs
 */
public class BrowserLauncherM {

    public static void openURL(String url) 
    throws BrowserException {

        boolean browserEditor = Activator.getDefault().getPreferenceStore().
            getBoolean(HelpBrowserPrefsPage.BROWSER_EDITOR);
        boolean browserView = Activator.getDefault().getPreferenceStore().
            getBoolean(HelpBrowserPrefsPage.BROWSER_VIEW);
//        boolean browserExternal = Activator.getDefault().getPreferenceStore().
//            getBoolean(HelpBrowserPrefsPage.BROWSER_EXTERNAL);
        if (browserEditor){
            openURLIntBrowserEdit(url);        
        } else if (browserView){
            openURLIntBrowserView(url);        
        } else {
            openURLExtBrowser(url);
        }
    }

    
    /**
     * Opens a URL in an external Browser
     * @param url URL to open
     * @throws BrowserException 
     */
    public static void openURLExtBrowser(String url) 
    throws BrowserException {

        try{
            String osName = System.getProperty("os.name");
            if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[] {String.class});
                openURL.invoke(null, new Object[] {url});
            }
            else if (osName.startsWith("Windows"))
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            else { //assume Unix or Linux
                String[] browsers = {
                        "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++)
                    if (Runtime.getRuntime().exec(
                            new String[] {"which", browsers[count]}).waitFor() == 0)
                        browser = browsers[count];
                if (browser == null)
                    throw new Exception("Wasn't able to find web browser");
                else
                    Runtime.getRuntime().exec(new String[] {browser, url});
            }
        } catch (Exception ex) {
            throw new BrowserException(ex.getLocalizedMessage());
        }
    }

    
    /**
     * Opens a URL in a new internal browser window as an editor.  
     * @param url URL to open
     * @throws MalformedURLException 
     * @throws PartInitException 
     */
    public static void openURLIntBrowserEdit(String url) 
    throws BrowserException{

        try {
        	WebBrowserEditorInput editorInput=new WebBrowserEditorInput(
                    new URL(url),
                    BrowserViewer.BUTTON_BAR);
        	try {
        	editorInput.setName(url.substring(url.lastIndexOf("/")+1));
        	} catch (Exception e) {
        		
        	}
            Activator.getDefault().getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage()
            .addSelectionListener(
                    (ISelectionListener) Activator.getDefault().getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage()
                    .openEditor(editorInput,
                    "org.eclipselabs.matclipse.meditor.editors.PdfEditor"));
        } catch (Exception ex) {
            throw new BrowserException(ex.getLocalizedMessage());
        }
    }

    
    /**
     * Opens a URL in a new internal browser window as a view.  
     * @param url URL to open
     * @throws PartInitException 
     * @throws MalformedURLException 
     */
    public static void openURLIntBrowserView(String url) 
    throws BrowserException{

        try {
            IWebBrowser browser;
            browser = Activator.getDefault()
            .getWorkbench().getBrowserSupport().createBrowser(
                    IWorkbenchBrowserSupport.AS_VIEW | BrowserViewer.BUTTON_BAR,
                    "Help Browser", "Help", "Help");
            browser.openURL(new URL(url));
        } catch (Exception ex) {
            throw new BrowserException(ex.getLocalizedMessage());
        }
    }

}
