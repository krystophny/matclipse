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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;


/**
 * Takes several lists of words which are connected with different tokens
 * and generates a rule which can be used to detect these words. 
 * @author Georg Huhs
 *
 */
class WordListRuleGenerator {
    
    /** is needed only for sorting the words */
    private ArrayList<String> wordList;
    
    /** keeps all words and their Tokens */
    private Hashtable<String, IToken> wordTable;
    
    
    public WordListRuleGenerator(){
        wordTable = new Hashtable<String, IToken>();
        wordList = new ArrayList<String>();
    }
    
    
    /**
     * add given wordlist and associate every word with the given token
     * @param words contains all words to add
     * @param token token to associate with the wordlist
     */
    public void addWordList(String[] words, IToken token){
        for (int i = 0; i < words.length; i++) {
            String currentWord = words[i];
            addWord(currentWord, token);
        }
    }

    
    private void addWord(String currentWord, IToken token) {
        wordTable.put(currentWord, token);
        wordList.add(currentWord);
    }
    
    
    public boolean addWordListFromFile(String filename, IToken token){
        boolean fileRead = false;
        try {
            File wordListFile = new File(filename);
            if (wordListFile.canRead()){
                FileReader wordListFileReader = new FileReader(wordListFile);
                BufferedReader fileReader = new BufferedReader(wordListFileReader);
                String currentWord = null;
                while((currentWord = fileReader.readLine()) != null){
                    if (currentWord.length() > 0){
                        addWord(currentWord, token);
                    }
                }
                fileRead = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileRead;
    }
    
    
    /**
     * generates an IPredicateRule that contains all added words and their tokens
     * @param detector detector to use
     * @param successToken
     * @param defaultToken
     * @return generated rule
     */
    public IPredicateRule generateRule(IWordDetector detector, 
            IToken successToken, IToken defaultToken){
        
        WordPredicateRule rule;
        rule = new WordPredicateRule(detector, successToken, defaultToken);
        Collections.sort((List<String>)wordList);
        for (int i = 0; i < wordList.size(); i++){
            String currentWord = wordList.get(i);
            rule.addWord(currentWord, wordTable.get(currentWord));
        }
        return rule;
    }

}
