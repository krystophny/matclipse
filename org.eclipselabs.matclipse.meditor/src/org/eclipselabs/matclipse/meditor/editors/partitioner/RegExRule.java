/*******************************************************************************
 * Copyright (c) 2005, 2011 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     mbaumbach (RadRails.org) - initial API and implementation
 *     Georg Huhs, Winfried Kernbichler, David Camhy (ITPCP) - 
 *         extended to handle lookaheads and lookbacks
 * Last changed: 
 *     2008-02-01
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.editors.partitioner;


import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;


/**
 * Defines a Regular-Expression rule to be used by a partitionscanner. 
 * This class searches for regexes only in one line!
 * Since lookbacks and lookaheads can't be used directly by writing them 
 * into the regex they are special treated by this class. 
 *
 * @author  basic version was taken from RadRails.org, author: mbaumbach
 * originally published under CPL v1.0, see: http://www.radrails.org/legal/cpl-v10.html 
 * republished under EPL v1.0 by courtesy of mbaumbach. 
 * extended by Georg Huhs (ITPCP)
 */
class RegExRule implements IPredicateRule {
    
    private IToken  token;
    private Pattern pattern;
    private Pattern completePattern;
    private Pattern lookbackPattern;
    private int     lookbackLen;
    private Pattern lookaheadPattern;
    private int     lookaheadMaxLen;
    private boolean greedy = false;
    
    
    /**
     * Constructs a new simple RegExRule.
     * @param token Token to return in case of success
     * @param pattern Pattern to search for
     * @param completePattern Complete-pattern if resuming is used
     * @param greedy Specifies if greedy behaviour should be used
     */
    public RegExRule(IToken token, Pattern pattern, Pattern completePattern, boolean greedy) {
        this.token            = token;
        this.pattern          = pattern;
        this.completePattern  = completePattern;
        this.lookbackPattern  = null;
        this.lookbackLen      = 0;
        this.lookaheadPattern = null;
        this.lookaheadMaxLen  = 0;
        this.greedy           = greedy;
    }

    
    /**
     * Sets the pattern for the lookback and activates its examination. 
     * The sequence's length needs to be known!
     * @param lookbackPattern Pattern to search for
     * @param lookbackLen Length of the lookback sequence to check
     */
    public void setLookback(Pattern lookbackPattern, int lookbackLen){
        this.lookbackPattern = lookbackPattern;
        if (lookbackPattern != null){
            this.lookbackLen = lookbackLen;
        } else {
            this.lookbackLen = 0;
        }
    }

    
    /**
     * Sets the pattern for the lookahead and activates its examination. 
     * Here only the maximum length of the sequence needs to be specified.
     * If a EOL arises before the maximum length is reached, the examination will stop.
     * @param lookaheadPattern Pattern to search for
     * @param lookaheadMaxLen Maximum length of the lookahead sequence to examine. 
     */
    public void setLookahead(Pattern lookaheadPattern, int lookaheadMaxLen){
        this.lookaheadPattern = lookaheadPattern;
        if (lookaheadPattern != null){
            this.lookaheadMaxLen = lookaheadMaxLen;
        } else {
            this.lookaheadMaxLen = 0;
        }
    }

    
    public IToken getSuccessToken() {
        return this.token;
    }
    
    
    /** 
     * @see org.eclipse.jface.text.rules.IRule
     * #evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner) {
        return this.evaluate(scanner, false);
    }
    
    
    /**
     * @see org.eclipse.jface.text.rules.IRule
     * #evaluate(org.eclipse.jface.text.rules.ICharacterScanner, boolean)
     */
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        
        Pattern pattern     = null;
        boolean lookbackOK  = false; 
        boolean matchOK     = false; 
        boolean lookaheadOK = false; 

        if(resume) {
            pattern = this.completePattern;
        }
        else {
            pattern = this.pattern;
        }
        IToken retToken = Token.UNDEFINED;
        StringBuffer buffer = new StringBuffer();
        
        // do the lookback - part 
        lookbackOK = true; 
        int col = scanner.getColumn();
        if (this.lookbackPattern != null && this.lookbackLen <= col){
            lookbackOK = doLookback(scanner);
        }
        
        // search for the expression, rewind scanner if no match was found
        if (lookbackOK){
            while(readToBuffer(scanner, buffer) && !pattern.matcher(buffer).matches()) {}
            
            if ( pattern.matcher(buffer).matches() ) {
                matchOK = true;
                
                // greedy part: read the rest of the line, add as much as 
                // possible to the buffer and rewind the scanner correctly
                if (greedy){
                    finishReadingGreedily(scanner, buffer, pattern);
                }
                
                // do the lookahead: check up to lookaheadMaxLen chars and rewind scanner
                // greedy behavior isn't needed here
                if (this.lookaheadPattern != null && this.lookaheadMaxLen > 0){
                    lookaheadOK = doLookahead(scanner);
                    if (!lookaheadOK){
                        rewindScanner(scanner, buffer.length());
                    }
                } else {
                    lookaheadOK = true;
                }
                
            } else {
                rewindScanner(scanner, buffer.length());
            }
        }
        if (lookbackOK && matchOK && lookaheadOK){
            retToken = this.token;
        }
        return retToken;
    }


    private void rewindScanner(ICharacterScanner scanner, int rewindLength) {
        for(int i = 0; i < rewindLength; i++) {
            scanner.unread();
        }
    }
    
    
    /**
     * Reads a single char from the scanner and puts it into the buffer, but only if 
     * the char is not an EOF or a newline. It also updates the scanner position 
     * such that it stands always after the last char that has been written 
     * into the buffer.
     * @param scanner Scanner to read from  
     * @param buffer Buffer to write to
     * @return true if a char has been written. False indicates that the end of the file
     * or the end of a line have been reached. 
     */
    private boolean readToBuffer(ICharacterScanner scanner, StringBuffer buffer){
        boolean charRead = false;
        
        int c = scanner.read();
        if (c != ICharacterScanner.EOF && c != '\n'){
            buffer.append((char) c);
            charRead = true;
        } else {
            scanner.unread();
        }
        
        return charRead;
    }


    /**
     * Finds the longest expression that matches the pattern. The result is written to 
     * the buffer and the scanner's position is set behind the last char 
     * that has been written into the buffer. 
     * @param scanner Scanner to read from  
     * @param buffer Buffer to write to
     * @param pattern Pattern to find
     */
    private void finishReadingGreedily(ICharacterScanner scanner, 
            StringBuffer buffer, Pattern pattern) {
        
        int notMatching = 0;
        while (readToBuffer(scanner, buffer)){
            if (pattern.matcher(buffer).matches()){
                notMatching = 0;
            } else {
                notMatching++;
            }
        }
        rewindScanner(scanner, notMatching);
    }


    /**
     * Checks if the lookback-part is satisfied. 
     * @param scanner Scanner to read from
     * @return true if the lookback-part is satisfied
     */
    private boolean doLookback(ICharacterScanner scanner) {
        
        boolean lookbackOK  = true; 
        StringBuffer lookbackBuffer = new StringBuffer();

        rewindScanner(scanner, this.lookbackLen);
        for( int i = 0; i < this.lookbackLen; i++ ) {
            lookbackBuffer.append((char)scanner.read());
        }
        if (!this.lookbackPattern.matcher(lookbackBuffer).matches()){
            lookbackOK = false;
        }
        return lookbackOK;
    }

    
    /**
     * Checks if the lookahead-part is satisfied. Does not change the position of the 
     * scanner. 
     * @param scanner Scanner to read from
     * @return true if the lookahead-part is satisfied
     */
    private boolean doLookahead(ICharacterScanner scanner) {
        
        boolean lookaheadOK = false; 
        StringBuffer lookaheadBuffer = new StringBuffer();

        while(readToBuffer(scanner, lookaheadBuffer) && 
                lookaheadBuffer.length() <= this.lookaheadMaxLen) {

            if (this.lookaheadPattern.matcher(lookaheadBuffer).matches()){
                lookaheadOK = true;
                break;
            }
        }
        rewindScanner(scanner, lookaheadBuffer.length());
        return lookaheadOK;
    }
    
} 
