/*******************************************************************************
 * Copyright (c) 2004, 2011 2006 fabioz (Pydev team) and others
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


public class MatlabUncommentAction extends MatlabAction implements IEditorActionDelegate {

    public MatlabUncommentAction(String label) {
        super(label);
    }

    
    public MatlabUncommentAction() {
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
            perform(selection);

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
    public static boolean perform(MatlabSelection selection) {
        // What we'll be replacing the selected text with
        StringBuffer strbuf = new StringBuffer();

        // If they selected a partial line, count it as a full one
        selection.selectCompleteLines();

        int i;
        try {
            // For each line, comment them out
            for (i = selection.startLineIndex; i <= selection.endLineIndex; i++) {
                String l = selection.getLine(i);
                
                if (l.trim().startsWith("%")){ 
                        //we may want to remove comment that are not really in the beginning...
                    strbuf.append(
                            l.replaceFirst("%", "") 
                            + (i < selection.endLineIndex ? selection.endLineDelim : ""));
                }else{
                    strbuf.append(
                            l + (i < selection.endLineIndex ? selection.endLineDelim : ""));
                }
            }

            // Replace the text with the modified information
            selection.doc.replace(
                    selection.startLine.getOffset(), 
                    selection.selLength, 
                    strbuf.toString());
            return true;
        } catch (Exception e) {
            Activator.beep(e);
        }

        // In event of problems, return false
        return false;
    }

    
    /**
     * Same as comment, but remove the first char.
     */
    protected String replaceStr(String str, String endLineDelim) {
        str = str.replaceAll(endLineDelim + "%", endLineDelim);
        if (str.startsWith("%")) {
            str = str.substring(1);
        }
        return str;
    }

    
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);    
    }

}
