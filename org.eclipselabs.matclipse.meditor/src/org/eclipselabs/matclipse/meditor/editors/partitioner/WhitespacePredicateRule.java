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
 *     2008-01-28
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.editors.partitioner;


import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.WhitespaceRule;


/**
 * Implementation of a WhitespaceRule. Additionally it implements IPredicateRule, 
 * so that it can be used by a RuleBasedPartitionScanner 
 * => adapter class
 */
class WhitespacePredicateRule extends WhitespaceRule 
        implements IPredicateRule{
    
    private IToken fSuccessToken;
    
    
    public WhitespacePredicateRule(IWhitespaceDetector detector, IToken successToken) {
        super(detector);
        fSuccessToken= successToken;
    }
    
    
    /*
     * @see org.eclipse.jface.text.rules.IPredicateRule#
     * evaluate(ICharacterScanner, boolean)
     */
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        return this.evaluate(scanner);
    }

    
    public IToken evaluate(ICharacterScanner scanner) {
        IToken token = super.evaluate(scanner);
        if (token.isWhitespace()){
            token = fSuccessToken;
        }
        return token;
    }

    
    /*
     * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
     */
    public IToken getSuccessToken() {
        return fSuccessToken;
    }
    
}
