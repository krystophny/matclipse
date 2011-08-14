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
 * Action for indenting the whole document
 * @author Georg Huhs
 */
public class MatlabIndentAllAction extends MatlabAction implements
        IEditorActionDelegate {

    public MatlabIndentAllAction(String text) {
        super(text);
    }
    
    
    public MatlabIndentAllAction() {
        super("");
    }

    
    public void run(IAction action) {
        MatlabSelection selection = new MatlabSelection(getTextEditor(), false );
        IDocument document = selection.doc;
        int startLine = 0;
        int endLine   = document.getNumberOfLines() - 1;
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
