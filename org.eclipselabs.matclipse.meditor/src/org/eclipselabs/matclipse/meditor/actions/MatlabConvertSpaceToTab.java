/*******************************************************************************
 * Copyright (c) 2004, 2011 Parhaum Toofanian (Pydev team) and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Parhaum Toofanian - initial API and implementation
 *     Georg Huhs, Winfried Kernbichler, David Camhy (ITPCP) - 
 *         some changes for using in Meditor
 * Last changed: 
 *     2008-01-22
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.actions;


import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.text.BadLocationException;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.MatlabenginePrefsPage;
import org.eclipselabs.matclipse.meditor.editors.MatlabSelection;


/**
 * Converts tab-width spacing to tab characters in selection or entire document, if nothing
 * selected.
 * 
 * @author Parhaum Toofanian
 */
public class MatlabConvertSpaceToTab extends MatlabAction 
{
    public MatlabConvertSpaceToTab(String label) {
        super(label);
    }
    /**
     * Grabs the selection information and performs the action.
     */
    public void run () {
        // Select from text editor
        MatlabSelection selection = new MatlabSelection ( getTextEditor ( ), true );
        // Perform the action
        perform(selection);

        // Put cursor at the first area of the selection
        getTextEditor ( ).selectAndReveal ( selection.getCursorOffset ( ), 0 );
    }

    
    /**
     * Performs the action with a given MatlabSelection
     * 
     * @param selection Given MatlabSelection
     * @return boolean The success or failure of the action
     */
    public static boolean perform (MatlabSelection selection ) {
        // What we'll be replacing the selected text with
        StringBuffer strbuf = new StringBuffer ( );
        
        // If they selected a partial line, count it as a full one
        selection.selectCompleteLines ( );
            
        int i;
    
        try {
            // For each line, strip their whitespace
            for ( i = selection.startLineIndex; i <= selection.endLineIndex; i++ ) {
                String line = selection.doc.get ( 
                        selection.doc.getLineInformation ( i ).getOffset ( ), 
                        selection.doc.getLineInformation ( i ).getLength ( ) );
                strbuf.append ( line.replaceAll ( getTabSpace ( ), "\t" ) + 
                        ( i < selection.endLineIndex ? selection.endLineDelim : "" ) );
            }

            // If all goes well, replace the text with the modified information    
            if ( strbuf.toString ( ) != null ) {
                selection.doc.replace ( selection.startLine.getOffset ( ), 
                        selection.selLength, strbuf.toString ( ) );
                return true;
            }
        } catch ( BadLocationException e ) {
            Activator.beep( e );
        }    
            
        // In event of problems, return false
        return false;        
    }
    
    
    /**
     * Currently returns a String filled with spaces and with length = Tab Width.  
     * 
     * @return String filled with Tab Width spaces
     */
    protected static String getTabSpace ( ) {
        StringBuffer sbuf = new StringBuffer ( );
        for ( int i = 0; i < getTabWidth ( ); i++ ) {
            sbuf.append ( " " );
        }
        return sbuf.toString ( );
    }        
    
    
    /**
     * Currently returns an int of the Preferences' Tab Width.  
     * 
     * @return Tab width in preferences
     */
    protected static int getTabWidth ( ) {
        Preferences prefs = Activator.getDefault().getPluginPreferences();
        return prefs.getInt(MatlabenginePrefsPage.TAB_WIDTH);
    }
}
