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

package org.eclipselabs.matclipse.meditor.editors.partitioner;


import org.eclipse.jface.text.rules.IWhitespaceDetector;


class WhitespaceDetector implements IWhitespaceDetector {

    /** contains all chars that are defined as whitespace */
    private char[] whitespaceList;
    
    
    /**
     * constructor for WhitespaceDetector
     * @param whitespaces defines which chars are whitespaces
     */
    WhitespaceDetector(char[] whitespaces){
        whitespaceList = whitespaces;
    }
    
    
    /**
     * @see org.eclipse.jface.text.rules.IWhitespaceDetector#isWhitespace(char)
     */
    public boolean isWhitespace(char c) {
        boolean charFound = false;
        for (int i = 0; i < whitespaceList.length; i++) {
            if (c == whitespaceList[i]){
                charFound = true;
                break;
            }
        }
        return charFound;
    }

}
