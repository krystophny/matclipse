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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

import org.eclipselabs.matclipse.meditor.Activator;


/**
 * Class for (rule based) partitioning of a Matlab-file. 
 */
public class MatlabPartitionScanner extends RuleBasedPartitionScanner {
    public final static String MATLAB_COMMENT           = "__matlab_comment";
    public final static String MATLAB_SINGLELINE_STRING = "__matlab_singleline_string";
    public final static String MATLAB_KEYWORD           = "__matlab_keyword";
    public final static String MATLAB_FUNCTION          = "__matlab_function";
    public final static String TOOLBOX_FUNCTION         = "__toolbox_function";
    public final static String MATLAB_OPERATOR          = "__matlab_operator";
    public final static String MATLAB_CODE              = "__matlab_code";
    public final static String MATLAB_NUMBER            = "__matlab_number";
    public final static String MATLAB_WHITESPACE        = "__matlab_whitespace";
    public final static String MATLAB_NEWLINE           = "__matlab_newline";
    public final static String MATLAB_CONTINUATION      = "__matlab_continuation";
    
    private final static String MATLAB_FUNCS_FILE       = "matlab_funcs";
    private final static String TOOLBOX_FUNCS_FILE      = "toolbox_funcs";

    private String configFileDir = "";

    public MatlabPartitionScanner() {
        try {
            this.configFileDir = Activator.getDefault().getPluginDir(Activator.CONFIGDIR);        
            init();
        } catch (IOException e) {
            Activator.errorDialog("Partition scanner init failed, " +
                    "the editor can't provide it's full functionality", e);
        }
    }

    
    public MatlabPartitionScanner(String configFileDir) {
        this.configFileDir = configFileDir;
        init();
    }
    
    
    public static String[] getConfiguredContentTypes() {
        return new String[] {
            IDocument.DEFAULT_CONTENT_TYPE,
            MATLAB_COMMENT,
            MATLAB_SINGLELINE_STRING,
            MATLAB_KEYWORD, 
            MATLAB_FUNCTION, 
            TOOLBOX_FUNCTION, 
            MATLAB_OPERATOR,
            MATLAB_CODE, 
            MATLAB_NUMBER, 
            MATLAB_WHITESPACE, 
            MATLAB_NEWLINE, 
            MATLAB_CONTINUATION
        };
    }
    
    
    private void init(){
        List<IPredicateRule> rules = new ArrayList<IPredicateRule>();
        
        addStringRules(rules);
        addCommentRules(rules);
        addOperatorRules(rules);
        addSpecialWordsRules(rules); // Keywords, Matlab-, and toolbox-functions
        addNumberRules(rules);
        addWhitespaceRules(rules);
        addNewlineRules(rules);
        addContinuationRules(rules);

        IPredicateRule[] result = new IPredicateRule[rules.size()];
        rules.toArray(result);
        setPredicateRules(result);
    }


    private void addContinuationRules(List<IPredicateRule> rules) {
        IToken continuationToken = new Token(MATLAB_CONTINUATION);
        rules.add(new MatlabEndOfLineRule("...", continuationToken));
    }


    private void addNewlineRules(List<IPredicateRule> rules) {
        IToken newlineToken      = new Token(MATLAB_NEWLINE);
        char[] newline = new char[]{'\n'};
        rules.add(new WhitespacePredicateRule(new WhitespaceDetector(newline), 
                newlineToken));
    }


    private void addWhitespaceRules(List<IPredicateRule> rules) {
        IToken whitespaceToken = new Token(MATLAB_WHITESPACE);
        char[] whitespaces = new char[]{' ', '\t'};
        rules.add(new WhitespacePredicateRule(new WhitespaceDetector(whitespaces), 
                whitespaceToken));
    }


    private void addNumberRules(List<IPredicateRule> rules) {
        IToken numberToken = new Token(MATLAB_NUMBER);
        
        // many different rules to handle different regex, 
        // some with and others without lookahead
        // sequence is important!
        RegExRule numberRule1 = new RegExRule(numberToken, 
                Pattern.compile("(\\d+\\.|\\d*\\.\\d+|\\d+)([eEdD][\\-\\+]?\\d+)"), 
                Pattern.compile(""), 
                true);
        rules.add(numberRule1);
        
        RegExRule numberRule2 = new RegExRule(numberToken, 
                Pattern.compile("\\d*\\.\\d+"), 
                Pattern.compile(""), 
                true);
        rules.add(numberRule2);

        RegExRule numberRule3 = new RegExRule(numberToken, 
                Pattern.compile("\\d+\\."), 
                Pattern.compile(""), 
                false);
        numberRule3.setLookahead(Pattern.compile("[^\\+\\-\\*/\\\\\\^']"), 1);
        rules.add(numberRule3);

        RegExRule numberRule4 = new RegExRule(numberToken, 
                Pattern.compile("\\d+"), 
                Pattern.compile(""), 
                true);
        rules.add(numberRule4);
    }


    private void addSpecialWordsRules(List<IPredicateRule> rules) {
        IToken defaultToken = new Token(MATLAB_CODE);
        IToken keywordToken = new Token(MATLAB_KEYWORD);
        IToken matlabFunctionToken = new Token(MATLAB_FUNCTION);
        IToken toolboxFunctionToken = new Token(TOOLBOX_FUNCTION);
        WordListRuleGenerator wordListRuleGenerator = new WordListRuleGenerator();
                
        String toolboxFuncsFilePath = configFileDir + TOOLBOX_FUNCS_FILE;
        if (!wordListRuleGenerator.addWordListFromFile(
                toolboxFuncsFilePath, toolboxFunctionToken)){
            
            Activator.warningDialog(
                    Activator.fileNotFoundWarningString(
                            toolboxFuncsFilePath, "No toolbox functions found."), null);
        }

        String matlabFuncsFilePath = configFileDir + MATLAB_FUNCS_FILE;
        if (!wordListRuleGenerator.addWordListFromFile(
                matlabFuncsFilePath, matlabFunctionToken)){
            
            wordListRuleGenerator.addWordList(
                    GreatKeywordDetector.functions, matlabFunctionToken);
            Activator.warningDialog(
                    Activator.fileNotFoundWarningString(
                            matlabFuncsFilePath, "Using standard function set."), null);
        }
        
        wordListRuleGenerator.addWordList(GreatKeywordDetector.keywords, keywordToken);

        IPredicateRule wordListRule = 
            wordListRuleGenerator.generateRule(new GreatKeywordDetector(), 
                    keywordToken, defaultToken);
        rules.add(wordListRule);
    }


    private void addOperatorRules(List<IPredicateRule> rules) {
        IToken operatorToken = new Token(MATLAB_OPERATOR);
        String operatorRegex = MatlabOperatorList.getRegexString();
        rules.add(new RegExRule(operatorToken, 
                   Pattern.compile(operatorRegex), 
                   Pattern.compile(""), 
                   true));
        // deal with ' operator
        RegExRule opRule1 = new RegExRule(operatorToken, 
                Pattern.compile("\\.?'"), 
                Pattern.compile(""), 
                false);
        opRule1.setLookback(Pattern.compile("[\\d\\w\\]\\)\\}]"), 1);
        rules.add(opRule1);
    }


    private void addCommentRules(List<IPredicateRule> rules) {
        IToken comment = new Token(MATLAB_COMMENT);
        rules.add(new MatlabEndOfLineRule("%", comment));
    }


    private void addStringRules(List<IPredicateRule> rules) {
        IToken singleLineString = new Token(MATLAB_SINGLELINE_STRING);
        // deal with "....." strings
        rules.add(new SingleLineRule("\"", "\"", singleLineString, '\\'));
        // deal with '.....' strings
        RegExRule singleLineStringRule = new RegExRule(singleLineString, 
                Pattern.compile("'.*'"), 
                Pattern.compile(""), 
                false);
        singleLineStringRule.setLookback(Pattern.compile("[^\\d\\w\\]\\)\\}\\.]"), 1);
        rules.add(singleLineStringRule);
        // special rules to legalize things like: case'a'
        String[] legalLeadingKeywords = new String[]{
                "case", "elseif", "if", "while"};
        for (String currentKeyword:legalLeadingKeywords){
            singleLineStringRule = new RegExRule(singleLineString, 
                    Pattern.compile("'.*'"), 
                    Pattern.compile(""), 
                    false);
            singleLineStringRule.setLookback(Pattern.compile(
                    currentKeyword), currentKeyword.length());
            rules.add(singleLineStringRule);
        }
    }
    
    
    private static class MatlabOperatorList {
        private final static String[] DOT_OPERATORS = {
            "+", "-", "*", "/", "\\", "^"
        }; // IMPORTANT: without ' and .' operators

// uncomment this for a more efficient regex
/*        private final static String[] LTGT_OPERATORS = {
            "<", ">"
        };
*/
        private final static String[] OPERATORS = {
            ":", 
            "<", "<=", ">", ">=", "~=", "==", 
            "&", "|", "~", 
            "&&", "||"
        };

        private final static char[] ESCAPELIST= new char []{
            '\\', '.', '*', '^', '+', '-', '|' 
        };
        
        private final static String OR_PLACEHOLDER = "###";
        private final static String OR_FINALLY     = "|";
        
        public static String getRegexString(){
            String regex = "";
            String dotOps = "";
            for (String currentOperator : DOT_OPERATORS){
                dotOps += OR_PLACEHOLDER + currentOperator;
            }
            dotOps = dotOps.substring(OR_PLACEHOLDER.length());
            regex += ".?(" + dotOps + ")";

            // it's possible to write this regex more compact: \.?[\+\-\*/\\\^]
            // but this construct can be used for operators with more than one chars 
            // (in addition to the dot)
            
// uncomment this for a more efficient regex
/*            for (String currentOperator : LTGT_OPERATORS){
                regex += OR_PLACEHOLDER + "(" + currentOperator + "=?)";
            }
*/            
            for (String currentOperator : OPERATORS){
                    regex += OR_PLACEHOLDER + currentOperator;
            }
            regex = escapeChars(regex);
            regex = regex.replace(OR_PLACEHOLDER, OR_FINALLY);
            return regex;
        }
        
        private static String escapeChars(String input){
            String escapedString = input;
            for (char repChar : ESCAPELIST){
                String repString = Character.toString(repChar);
                    escapedString = escapedString.replace(repString, "\\"+repString);
            }
            return escapedString;
        }
    }
    
}
