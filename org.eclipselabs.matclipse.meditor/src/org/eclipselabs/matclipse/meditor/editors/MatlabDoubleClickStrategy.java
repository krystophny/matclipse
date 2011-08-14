/*******************************************************************************
 * Copyright (c) 2004, 2011 atotic, fabioz (Pydev team) and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     atotic, fabioz - initial API and implementation
 *     Georg Huhs, Winfried Kernbichler, David Camhy (ITPCP) - 
 *         adapted to Matlab for using with Meditor
 * Last changed: 
 *     2008-01-30
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.editors;


import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

import org.eclipselabs.matclipse.meditor.Activator;


public class MatlabDoubleClickStrategy implements ITextDoubleClickStrategy {

    protected ITextViewer textViewer;


    /** 
     * @see org.eclipse.jface.text.ITextDoubleClickStrategy#
     * doubleClicked(org.eclipse.jface.text.ITextViewer)
     */
    public void doubleClicked(ITextViewer viewer) {
        int caretPos = viewer.getSelectedRange().x;

        if (caretPos < 0)
            return;

        this.textViewer = viewer;
        selectWord(caretPos);
    }
    
    
    protected void selectWord(int caretPos) {

        IDocument doc = textViewer.getDocument();
        int startPos, endPos;

        try {
            int pos = caretPos;
            char c;

            while (pos >= 0) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c))
                    break;
                --pos;
            }

            startPos = pos;
            pos = caretPos;
            int length = doc.getLength();

            while (pos < length) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c))
                    break;
                ++pos;
            }

            endPos = pos;
            selectRange(startPos, endPos);
        } catch (BadLocationException e) {
            Activator.beep(e);
        }
    }

    
    private void selectRange(int startPos, int endPos) {
        int offset = startPos + 1;
        int length = endPos - offset;
        textViewer.setSelectedRange(offset, length);
    }
    
}
