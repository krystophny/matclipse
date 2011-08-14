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
 *     2007-03-09
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.actions;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.editors.MatlabAutoEditStrategy;
import org.eclipselabs.matclipse.meditor.editors.MatlabSelection;


/**
 * Action for indenting a selected part of a document. If no text is selected, the current line
 * will be indented. 
 * @author Georg Huhs
 *
 */
public class MatlabIndentationAction extends MatlabAction implements IEditorActionDelegate{

    public MatlabIndentationAction(String text) {
        super(text);
    }

    
    public MatlabIndentationAction() {
        super("");
    }

    
    public void run(IAction action) {
        MatlabSelection selection = new MatlabSelection ( getTextEditor ( ), false );
        int startLine = selection.startLineIndex;
        int endLine   = selection.endLineIndex;
        IDocument document = selection.doc;
        try {
            MatlabAutoEditStrategy.getInstance().indentLines(document, startLine, endLine);
        } catch (BadLocationException e) {
            Activator.beep(e);
        }
    }

    
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);    
    }

}
