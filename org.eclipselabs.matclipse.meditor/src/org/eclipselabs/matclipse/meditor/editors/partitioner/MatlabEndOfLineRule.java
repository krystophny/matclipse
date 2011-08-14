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


import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;


/**
 * A little modification of EndOfLineRule that was necessary because the original
 * rule also catches the \n at the end of a line. But this \n should be recognized as
 * a Matlab-Newline partition, which is made possible by this class. 
 * @author Georg Huhs
 */
class MatlabEndOfLineRule extends EndOfLineRule {

    public MatlabEndOfLineRule(String startSequence, IToken token) {
        super(startSequence, token);
    }


    /**
     * @see org.eclipse.jface.text.rules.PatternRule
     * #evaluate(org.eclipse.jface.text.rules.ICharacterScanner, boolean)
     */
    @Override
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        IToken token = super.evaluate(scanner, resume);
        if (token.equals(this.fToken)){
            scanner.unread(); // rewind scanner to catch \n ( -> as a newline-partition!)
        }
        return token;
    }
    
}
