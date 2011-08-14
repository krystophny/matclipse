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

package org.eclipselabs.matclipse.meditor.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.editors.MatlabEditor;


public abstract class MatlabAction extends Action {

    // Always points to the current editor
    protected IEditorPart targetEditor;

    
    public MatlabAction(String label) {
        super(label);
    }
    

    public void setEditor(IEditorPart targetEditor) {
        this.targetEditor = targetEditor;
    }
    
    
    /**
     * This is an IEditorActionDelegate override
     */
    public void setActiveEditor(IEditorPart targetEditor) {
        setEditor(targetEditor);
    }

    
    /**
     * Activate action  (if we are getting text)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof TextSelection) {
            action.setEnabled(true);
            return;
        }
        action.setEnabled( targetEditor instanceof ITextEditor);
    }

    
    /**
     * This method returns the delimiter for the document
     * @param doc
     * @param startLineIndex
     * @return  delimiter for the document (\n|\r\|r\n)
     * @throws BadLocationException
     */
    public static String getDelimiter(IDocument doc, int startLineIndex)
        throws BadLocationException {
        String endLineDelim = doc.getLineDelimiter(startLineIndex);
        if (endLineDelim == null) {
            endLineDelim = doc.getLegalLineDelimiters()[0];
        }
        return endLineDelim;
    }

    
    protected ITextEditor getTextEditor() {
        if (targetEditor instanceof ITextEditor) {
            return (ITextEditor) targetEditor;
        } else {
            throw new RuntimeException("Expecting text editor. Found:"
                    +targetEditor.getClass().getName());
        }
    }

    
    protected MatlabEditor getMatlabEditor() {
        if (targetEditor instanceof MatlabEditor) {
            return (MatlabEditor) targetEditor;
        } else {
            throw new RuntimeException("Expecting PyEdit editor. Found:"
                    +targetEditor.getClass().getName());
        }
    }
    

    protected void setCaretPosition(int pos) throws BadLocationException {
        getTextEditor().selectAndReveal(pos, 0);
    }

    
    /**
     * Returns the position of the first non whitespace char in the current line.
     * @param doc Document to search in
     * @param cursorOffset Current cursor position
     * @return position of the first character of the current line 
     * (returned as an absolute offset)
     * @throws BadLocationException
     */
    public static int getFirstCharPosition(IDocument doc, int cursorOffset)
        throws BadLocationException {
        IRegion region;
        region = doc.getLineInformationOfOffset(cursorOffset);
        int offset = region.getOffset();
        return offset + getFirstCharRelativePosition(doc, cursorOffset);
    }

    
    /**
     * Returns the position of the first non whitespace char in the current line.
     * @param doc Document to search in
     * @param cursorOffset Current cursor position
     * @return position of the first character of the current line 
     * (returned relative to the current cursor position)
     * @throws BadLocationException
     */
    public static int getFirstCharRelativePosition(IDocument doc, int cursorOffset) 
            throws BadLocationException {
        IRegion region;
        region = doc.getLineInformationOfOffset(cursorOffset);
        return getFirstCharRelativePosition(doc, region);
    }

    
    /**
     * Returns the position of the first non whitespace char in the current line.
     * @param doc Document to search in
     * @param line Current cursor position's line number
     * @return position of the first character of the current line 
     * (returned relative to the current cursor position)
     * @throws BadLocationException
     */
    public static int getFirstCharRelativeLinePosition(IDocument doc, int line) 
            throws BadLocationException {
        IRegion region;
        region = doc.getLineInformation(line);
        return getFirstCharRelativePosition(doc, region);
    }

    /**
     * Returns the position of the first non whitespace char in the specified region.
     * @param doc Document to search in
     * @param region Region to search in
     * @return position of the first character of the current line 
     * (returned relative to the region's start)
     * @throws BadLocationException
     */
    public static int getFirstCharRelativePosition(IDocument doc, IRegion region) 
            throws BadLocationException {
        int offset = region.getOffset();
        String src = doc.get(offset, region.getLength());

        int i = 0;
        boolean broke = false;
        while (i < src.length()) {
            if (Character.isWhitespace(src.charAt(i)) == false && src.charAt(i) != '\t') {
                i++;
                broke = true;
                break;
            }
            i++;
        }
        if (!broke){
            i++;
        }
        return (i - 1);
    }

    
    /**
     * Returns the position of the last non whitespace char in the current line.
     * @param doc Document to search in
     * @param cursorOffset Current cursor position
     * @return position of the last character of the line (returned as an absolute offset)
     * @throws BadLocationException
     */
    protected int getLastCharPosition(IDocument doc, int cursorOffset)
        throws BadLocationException {
        IRegion region;
        region = doc.getLineInformationOfOffset(cursorOffset);
        int offset = region.getOffset();
        String src = doc.get(offset, region.getLength());

        int i = src.length();
        boolean broke = false;
        while (i > 0 ) {
            i--;
            //we have to break if we find a character that is not a whitespace or a tab.
            if (   Character.isWhitespace(src.charAt(i)) == false && src.charAt(i) != '\t'  ) {
                broke = true;
                break;
            }
        }
        if (!broke){
            i--;
        }
        return (offset + i);
    }

    
    /**
     * Goes to first char of the line.
     * @param doc Base document
     * @param cursorOffset Current cursor position
     */
    protected void gotoFirstChar(IDocument doc, int cursorOffset) {
        try {
            IRegion region = doc.getLineInformationOfOffset(cursorOffset);
            int offset = region.getOffset();
            setCaretPosition(offset);
        } catch (BadLocationException e) {
            Activator.beep(e);
        }
    }

    
    /**
     * Goes to the first visible char.
     * @param doc Base document
     * @param cursorOffset Current cursor position
     */
    protected void gotoFirstVisibleChar(IDocument doc, int cursorOffset) {
        try {
            setCaretPosition(getFirstCharPosition(doc, cursorOffset));
        } catch (BadLocationException e) {
            Activator.beep(e);
        }
    }

    
    /**
     * Tells if the cursor position is at the first visible char.
     * @param doc Base document
     * @param cursorOffset Current cursor position
     * @return true if cursor is placed at the first visible char, false otherwise
     */
    protected boolean isAtFirstVisibleChar(IDocument doc, int cursorOffset) {
        try {
            return getFirstCharPosition(doc, cursorOffset) == cursorOffset;
        } catch (BadLocationException e) {
            return false;
        }
    }

    
    //================================================================
    // HELPER FOR DEBBUGING... 
    //================================================================

    protected void print(Object o) {
        System.out.println(o);
    }

    protected void print(boolean b) {
        System.out.println(b);
    }
    
}
