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
 *     2007-01-10
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor;


import java.util.ArrayList;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;


public class HelpBrowserPrefsPage extends AbstractMatlabenginePrefsPage {
    
    public static final String BROWSER_EDITOR   = "WEBBROWSER_EDITOR";
    public static final String BROWSER_VIEW     = "WEBBROWSER_VIEW";
    public static final String BROWSER_EXTERNAL = "WEBBROWSER_EXTERNAL";
    public static final String DEFAULT_BROWSER  = BROWSER_EDITOR;
    
    public static final String HTML_HELP_BASE          = "HTML_HELP_BASE";
    public static final String DEFAULT_HTML_HELP_BASE  = 
        "http://www.mathworks.com/access/helpdesk/help/";
    public static final String HTML_HELP_DEFPAGE          = "HTML_HELP_DEFPAGE";
    public static final String DEFAULT_HTML_HELP_DEFPAGE  = 
        "http://www.mathworks.com/access/helpdesk/help/techdoc/matlab.html";
    
    private static final int INDENT_H       = 20;
    private static final int INDENT_V_BIG   = 20;
    private static final int INDENT_V_SMALL = 10;
    
    
    public HelpBrowserPrefsPage(){
        super("Matlab help browser settings:");
    }
    
    
    protected OverlayPreferenceStore createOverlayStore() {
        
        ArrayList<OverlayPreferenceStore.OverlayKey> overlayKeys = 
            new ArrayList<OverlayPreferenceStore.OverlayKey>();

        // show linenumber options
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, BROWSER_EDITOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, BROWSER_VIEW));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, BROWSER_EXTERNAL));
        
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, HTML_HELP_BASE));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, HTML_HELP_DEFPAGE));
        
        OverlayPreferenceStore.OverlayKey[] keys= 
            new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
        overlayKeys.toArray(keys);
        return new OverlayPreferenceStore(getPreferenceStore(), keys);
    }

    
    protected Control createAppearancePage(Composite parent) {

        Composite appearanceComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout(); 
        layout.numColumns= 2;
        appearanceComposite.setLayout(layout);

        Group groupOutput = addGroup(appearanceComposite, "Web browser settings", INDENT_V_BIG, 2, 2);
        
        addRadioButtonField(groupOutput, 
                "Open Matlab help in:", 
                new String[][] { 
                    { "New editor",      BROWSER_EDITOR}, 
                    { "New view",        BROWSER_VIEW }, 
                    { "External editor", BROWSER_EXTERNAL} }, 
                INDENT_V_SMALL, 
                2, 
                INDENT_H);

        addTextField(appearanceComposite, "HTML help base URL", 
                HTML_HELP_BASE, Text.LIMIT, 0, false);
        addTextField(appearanceComposite, "Default help page URL", 
                HTML_HELP_DEFPAGE, Text.LIMIT, 0, false);
        
        return appearanceComposite;
    }

    
    /**
     * Sets default preference values
     */
    protected static void initializeDefaultPreferences(Preferences prefs) {
        
        prefs.setDefault(BROWSER_EDITOR,    false);
        prefs.setDefault(BROWSER_VIEW,      false);
        prefs.setDefault(BROWSER_EXTERNAL,  false);
        prefs.setDefault(DEFAULT_BROWSER,   true);
        
        prefs.setDefault(HTML_HELP_BASE, DEFAULT_HTML_HELP_BASE);
        prefs.setDefault(HTML_HELP_DEFPAGE, DEFAULT_HTML_HELP_DEFPAGE);
    }
}
