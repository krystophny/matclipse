/*******************************************************************************
 * Copyright (c) 2004, 2011 fabioz (Pydev team) and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     fabioz - initial API and implementation
 *     Georg Huhs, Winfried Kernbichler, David Camhy (ITPCP) - 
 *         some changes for using in Meditor
 * Last changed: 
 *     2008-01-22
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.editors.MatlabSelection;


public class MatlabCommentAction extends MatlabAction implements IEditorActionDelegate {
    public MatlabCommentAction(String label) {
        super(label);
    }
    
    public MatlabCommentAction() {
        super("");
    }
    
    
    /**
     * Grabs the selection information and performs the action.
     */
    public void run(IAction action) {
        try {
            // Select from text editor
            MatlabSelection selection = new MatlabSelection(getTextEditor(), false);
            // Perform the action
            
            perform (selection);

            // Put cursor at the first area of the selection
            getTextEditor().selectAndReveal(selection.endLine.getOffset(), 0);
        } catch (Exception e) {
            Activator.beep(e);
        }        
    }

    /**
     * Performs the action with a given MatlabSelection
     * 
     * @param selection Given MatlabSelection
     * @return boolean The success or failure of the action
     */
    public static boolean perform (MatlabSelection selection) {
        boolean success = false;
        
        // What we'll be replacing the selected text with
        StringBuffer strbuf = new StringBuffer();
        
        // If they selected a partial line, count it as a full one
        selection.selectCompleteLines();

        int i;
        try {
            // For each line, comment them out
            for ( i = selection.startLineIndex; i < selection.endLineIndex; i++ ) {
                strbuf.append ( "%" + selection.getLine ( i ) + selection.endLineDelim );
            }
            // Last line shouldn't add the delimiter
            strbuf.append ("%" + selection.getLine(i));

            // Replace the text with the modified information
            selection.doc.replace(selection.startLine.getOffset(), 
                    selection.selLength, strbuf.toString());
            success = true;
        } catch (Exception e) {
            Activator.beep(e);
        }

        // In event of problems, return false
        return success;
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);
    }
}
