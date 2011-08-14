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
 *     2008-01-29
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.editors;


import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.MatlabenginePrefsPage;
import org.eclipselabs.matclipse.meditor.editors.partitioner.MatlabPartitionScanner;


/**
 * Implementation of an edit strategy for the Matlab editor
 * @author Georg Huhs
 */
public class MatlabAutoEditStrategy implements IAutoEditStrategy {

    /** keeps the only instance of this singleton class */
    private static final MatlabAutoEditStrategy INSTANCE = new MatlabAutoEditStrategy();
    
    /**  should tab be converted to spaces? */
    private boolean useSpaces = false;   
    private int     tabWidth  = 0;
    
    /** keeps all information which indentations are forced by which keywords */
    private Hashtable<String, IndentInfo> keywordIndentInfo = null;       
    /** contains all chars which are treaded as whitespace */
    private Vector<String>    whitespaceList    = null;
    /** defines the indentation step size of continuation lines */
    private final int continuationIndentStepSize = 2;
    
    private ISourceViewer sourceViewer = null;
    
    private static final int THIS_LINE = 1;
    private static final int NEXT_LINE = 2;
    private static final int UNDEFINED = -1;

    
    private MatlabAutoEditStrategy(){
        setupStrategy();
        initKeywords();
        initWhitespaces();
    }
    
    
    /**
     * this function is the only way to get a MatlabAutoEditStrategy object
     * (singleton class)
     * @return the only instance of this class
     */
    public static MatlabAutoEditStrategy getInstance(){
        return INSTANCE;
    }

    
    public void setSourceViewer(ISourceViewer sourceViewer){
        this.sourceViewer = sourceViewer;
    }

    
    public void setupStrategy(){
        Preferences matlabPreferences = MatlabenginePrefsPage.getPreferences();
        this.useSpaces  = matlabPreferences.getBoolean(MatlabenginePrefsPage.SUBSTITUTE_TABS);   
        this.tabWidth   = matlabPreferences.getInt(MatlabenginePrefsPage.TAB_WIDTH);        
    }
    
    
    /**
     * indents lines in a given range 
     * @param document
     * @param startLine first line to indent
     * @param endLine last line to indent
     * @throws BadLocationException 
     */    
    public void indentLines(IDocument document, int startLine, int endLine) 
            throws BadLocationException{
            
        int[] indentInfo = new int[] {UNDEFINED, UNDEFINED}; 

        for (int lineIndex = startLine; lineIndex <= endLine; lineIndex++){
            if (!isWhitespaceLine(document, lineIndex)){
                indentInfo = indentLine(document, lineIndex, indentInfo);
            }
        }
    }
    
    
    /**
     * @see org.eclipse.jface.text.IAutoEditStrategy#
     * customizeDocumentCommand(IDocument document, DocumentCommand command)
     */
    public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
        try {
            LineInfo currentLineInfo;
            {
                int currentLineNumber = document.getLineOfOffset(command.offset);
                int currentLineOffset = document.getLineOffset(currentLineNumber);
                int currentLineLength = document.getLineLength(currentLineNumber);
                currentLineInfo = 
                    new LineInfo(currentLineNumber, currentLineOffset, currentLineLength);
            }

            // if some text will be deleted
            if (command.length > 0 
                    // or a longer text is inserted
                    || command.text.length() > 1){
                
                    processTextReplace(document, currentLineInfo, command);
            }
            
            // correct indentation of current line
            // only if last typed word is a keyword
            if (this.whitespaceList.contains(command.text)){ // whitespace
                
                processWhitespace(document, currentLineInfo, command);
            }

            // start new line with correct indentation
            if (command.text.equals("\n")){
                processNewline(document, currentLineInfo, command);
            } 
            
            // handle tab
            if (command.text.equals("\t")){
                // Should never happen, because TAB is assigned 
                // to org.eclipselabs.matclipse.actions.indentationaction
                // This code is still left here for the event of an action malfunction
                processTab(document, currentLineInfo, command);                
            }
            
            // to ensure that the text is shown correctly colored
            if (sourceViewer != null){
                sourceViewer.invalidateTextPresentation();
            }
        } catch (BadLocationException e) {
            Activator.beep(e);
        }
    }


    /**
     * Is called when a tab has been pressed. A tab indents the current line correctly. 
     * Should never happen, because TAB is assigned 
     * to org.eclipselabs.matclipse.actions.indentationaction
     * This code is still left here for the event of an action malfunction
     * @param document Document to work on
     * @param lineInfo Information about the current line
     * @param command The entered command
     * @throws BadLocationException
     */
    private void processTab(IDocument document, LineInfo lineInfo, 
            DocumentCommand command) 
            throws BadLocationException {
                        
        indentLine(document, lineInfo.lineNumber);
        command.text = "";
        command.offset = 
            lineInfo.lineOffset + getIndentStringLength(document, lineInfo.lineNumber);

    }


    /**
     * Called when a LF has been entered. The new line is indented correctly.
     * @param document Document to work on
     * @param lineInfo Information about the line preceding the current one
     * @param command The entered command
     * @throws BadLocationException
     */
    private void processNewline(IDocument document, LineInfo lineInfoPrev, 
            DocumentCommand command)
            throws BadLocationException {

        int indentNew  = 0;
        int[] indentInfo = new int[] {UNDEFINED, UNDEFINED};
        
        // set the \n in the document where it was typed
        document.replace(command.offset, 0, "\n");
        // indent finished line
        indentInfo = indentLine(document, lineInfoPrev.lineNumber, indentInfo);
        int lineNumberNext = lineInfoPrev.lineNumber + 1;

        // enter was pressed at end of line
        if (command.offset == lineInfoPrev.lineOffset + lineInfoPrev.lineLength - 1){
            // generate indentation string for new line
            indentNew = indentInfo[0] + indentInfo[1];
            String indentStringNew = generateIndentString(indentNew);
            command.text   = indentStringNew;
        } else { // enter was pressed in the middle of a line
            // also indent next line
            indentLine(document, lineNumberNext, indentInfo);
            // and set cursor to the end of the leading whitespace in the next line
            command.text = "";
        }
        command.offset = document.getLineOffset(lineNumberNext) + 
            getIndentStringLength(document, lineNumberNext);
    }


    /**
     * Called when any kind of whitespace is entered. This causes a recomputation of the 
     * document partitioning and depending on the result eventually a new indentation. 
     * @param document Document to work on
     * @param lineInfo Information about the current line
     * @param command The entered command
     * @throws BadLocationException
     */
    private void processWhitespace(IDocument document, LineInfo lineInfo, 
            DocumentCommand command)
            throws BadLocationException {

        // only if current position isn't the beginning of a new line
        if (lineInfo.lineOffset < command.offset)
        {
            document.replace(command.offset, 0, command.text);
            document.computePartitioning(lineInfo.lineOffset, 
                    lineInfo.lineLength + command.text.length());
            ITypedRegion lastPartition = document.getPartition(command.offset-1);
            String lastPartitionTypeThis = lastPartition.getType();
            if (lastPartitionTypeThis.equals(MatlabPartitionScanner.MATLAB_KEYWORD)){
                int oldIndentStringLength = 
                    getIndentStringLength(document, lineInfo.lineNumber);
                indentLine(document, lineInfo.lineNumber);
                int newIndentStringLength = 
                    getIndentStringLength(document, lineInfo.lineNumber);
                int indentStringLenDiff = newIndentStringLength - oldIndentStringLength;
                command.offset += indentStringLenDiff;
            }
            command.offset += command.text.length();
            command.text = "";
        }
    }


    /**
     * Replaces text. Called when some text is deleted.
     * @param document Document to work on
     * @param lineInfo Information about the current line
     * @param command The entered command
     * @throws BadLocationException
     */
    private void processTextReplace(IDocument document, LineInfo lineInfo, 
            DocumentCommand command)
            throws BadLocationException {
        
        // do the operation NOW
        performDocumentCommand(document, command);
        lineInfo.lineLength = document.getLineLength(lineInfo.lineNumber);
        // compute new partitioning for this line
        document.computePartitioning(lineInfo.lineOffset, lineInfo.lineLength);
        int numInsertedLines = command.text.split("\n").length -1 ;
        int lastInsertedLine = lineInfo.lineNumber + numInsertedLines;
        
        int indentStringLengthOld = getIndentStringLength(document, lineInfo.lineNumber);
        indentLines(document, lineInfo.lineNumber, lastInsertedLine);
        if (numInsertedLines > 0){
            // set cursor to the end of the inserted text
            command.offset = document.getLineOffset(lastInsertedLine)
                    + document.getLineLength(lastInsertedLine) - 1;
        } else {
            int indentStringLengthNew = 
                getIndentStringLength(document, lineInfo.lineNumber);
            int indentCorrection = indentStringLengthNew - indentStringLengthOld;
            // if nothing has been inserted
            if (command.text.length() == 0 && indentCorrection < 0){
                indentCorrection = 0;
            }
            command.offset += indentCorrection;
        }
    }

    
    /**
     * Executes the text replace specified by command instantaneously and clears command.
     * @param document Document to work on
     * @param command The entered command
     * @throws BadLocationException
     */
    private void performDocumentCommand(IDocument document, DocumentCommand command) 
            throws BadLocationException {
        
        document.replace(command.offset, command.length, command.text);
        if (command.text.length() > 0){
            command.offset += command.text.length();
        }
        command.length = 0;
        command.text   = "";
    }

    
    /**
     * Tells if the given line a continued one (ends with a continuation) is
     * @param document Document to work on
     * @param lineNumber line to check
     * @return true if the given line is continued
     * @throws BadLocationException
     */
    private boolean isContinuedLine(IDocument document, int lineNumber) 
            throws BadLocationException {
        
        boolean isContinuedLine = false;
        int lastPartitionOffset = document.getLineOffset(lineNumber) 
                + document.getLineLength(lineNumber);
        
        if (document.getPartition(lastPartitionOffset-1).getType().equals(
                MatlabPartitionScanner.MATLAB_CONTINUATION)){
            isContinuedLine = true;
        }
        return isContinuedLine;
    }

    
    private boolean isWhitespacePartition(ITypedRegion partition) {
        return partition.getType().equals(MatlabPartitionScanner.MATLAB_WHITESPACE);
    }
    
    
    /**
     * Tells if the whole line consists of whitespace
     * @param document Document to work on
     * @param lineNumber number of the line to check
     * @return true if the whole line consists only of whitespace
     * @throws BadLocationException
     */
    private boolean isWhitespaceLine(IDocument document, int lineNumber) 
            throws BadLocationException{
        
        ITypedRegion[] linePartitions = getLinePartitions(document, lineNumber);
        
        return ((linePartitions.length == 1 && ( isWhitespacePartition(linePartitions[0])
                || linePartitions[0].getType().equals(
                        MatlabPartitionScanner.MATLAB_NEWLINE))));
    }

    /**
     * Tells if the whole line is a comment
     * @param document Document to work on
     * @param lineNumber number of the line to check
     * @return true if the whole line is a comment
     * @throws BadLocationException
     */
    private boolean isCommentLine(IDocument document, int lineNumber) 
            throws BadLocationException{

        return (document.getPartition(document.getLineOffset(lineNumber))
                .getType().equals(MatlabPartitionScanner.MATLAB_COMMENT));
    }

    
    private int getIndentStringLength(IDocument document, int lineNumber) 
            throws BadLocationException{
        
        int length = 0;
        ITypedRegion firstPartition = document.getPartition(
                document.getLineOffset(lineNumber));
        if (isWhitespacePartition(firstPartition)){
            length = firstPartition.getLength();
        }
        return length;
    }

    
    /**
     * computes the correct indentation for the given line 
     * and corrects its indentation by exchanging leading whitespace
     * @param document Document to work on
     * @param lineNumber line to indent
     * @throws BadLocationException
     */
    private void indentLine(IDocument document, int lineNumber) 
            throws BadLocationException {
        
        indentLine(document, lineNumber, null);
    }
    
    /**
     * computes correct indentation for the given line 
     * and corrects its indentation by exchanging leading whitespace.
     * if an indentInfo is given a faster algorithm is used
     * @param document Document to work on
     * @param lineNumber line to indent
     * @param indentInfo array that holds the indentation information of the previous line<br>
     *                   indentInfo[0] is its indentation length<br>
     *                   indentInfo[1] is its indentation correction for this line<br>
     *                   if <code>indentInfo == null</code> the same will be returned, 
     *                   but if one of it's elements is <code>UNDEFINED</code>
     *                   the correct information will be returned
     * @return array that holds the indentation information of this line<br>
     *                   indentInfo[0] indentation length of this line<br>
     *                   indentInfo[1] indentation correction for next line<br>
     *                   may be <code>null</code> if indentInfo was <code>null</code>
     * @throws BadLocationException
     */
    private int[] indentLine(IDocument document, int lineNumber, int[] indentInfo) 
            throws BadLocationException {
        
        // get indentation of previous line
        // also get correction for this line caused by keywords in previous line
        int lineNumberPrev = lineNumber;
        int indentLengthPrev = 0;
        int indentCorrection = 0;
        
        boolean indentInfoProvided = false;
        
        if (indentInfo != null){
            if (indentInfo[0] != UNDEFINED && indentInfo[1] != UNDEFINED){
                indentInfoProvided = true;
            }
        }
        
        if (!indentInfoProvided){
            // search for nearest previous line which contains not only whitespace
            while (lineNumberPrev > 0){
                lineNumberPrev--;
                
                if (!isWhitespaceLine(document, lineNumberPrev) 
                        && !isCommentLine(document, lineNumberPrev)){
                    indentLengthPrev = getIndentLength(document, lineNumberPrev);
                    indentCorrection = calcIndentCorrection(document, lineNumberPrev, NEXT_LINE);
                    break;
                } 
            }
        } else {
            indentLengthPrev = indentInfo[0];
            indentCorrection = indentInfo[1];
        }
        

        // collect data for indentation and generate indentation string
        int lineOffset = document.getLineOffset(lineNumber);
        int indentLengthNew=0;
        //TODO Georg mitteilen
        try {
        indentLengthNew = indentLengthPrev + indentCorrection 
            + calcIndentCorrection(document, lineNumber, THIS_LINE);
        } catch (org.eclipse.jface.text.BadLocationException e) {
        	
        }
        String indentString = generateIndentString(indentLengthNew);
        
        // calculate number of chars of old indentation
        int indentStringLengthOld = getIndentStringLength(document, lineNumber);
        
        // correct contents (replace old indentation by correct indentation string)
        document.replace(lineOffset, indentStringLengthOld, indentString);
        //TODO Georg mitteilen
        if (indentInfo != null){
            indentInfo[0] = Math.max(indentLengthNew, 0);
            try {
            	indentInfo[1] = calcIndentCorrection(document, lineNumber, NEXT_LINE);
            } catch (org.eclipse.jface.text.BadLocationException e) {
            	
            }
        }
        
        return indentInfo;
    }
    
    
    /**
     * calculates a relative indentation length  
     * depending on the content of a given line
     * for a relatively specified line
     * @param document Document to work on
     * @param lineNumber line which content is examined
     * @param lineSelect tells for which line the correction should be calculated
     *                   possible values are THIS_LINE or NEXT_LINE
     * @return calculated length of indentation-correction, may be negative, 
     *         length equals number of spaces if spaces are used for indentation
     * @throws BadLocationException
     */
    private int calcIndentCorrection(IDocument document, int lineNumber, int lineSelect) 
            throws BadLocationException {
        
        int indentCorrection = 0;

        // if this is a continued line
        if (isContinuedLine(document, lineNumber)){
            if (lineSelect == NEXT_LINE){
                
                indentCorrection += continuationIndentStepSize * tabWidth; 
                
                // if this line is not the first continuation
                try {
                    if (isContinuedLine(document, lineNumber - 1)){
                        indentCorrection -= continuationIndentStepSize * tabWidth; 
                    }
                } catch (BadLocationException e) {}
            }
        } else { 
            // correction for the usual case (no continued line)
            indentCorrection = calcIndentCorrectionSingleline(document, 
                    lineNumber, lineSelect);    

            int continuedLinePointer = lineNumber;
            // if this is the first line after the end of a continued one
            try {
                if (isContinuedLine(document, lineNumber - 1)){
                    indentCorrection = 0;
                    if (lineSelect == NEXT_LINE){
                        // collect all indentation information of the whole continued line
                        while (continuedLinePointer >= 1 && 
                                isContinuedLine(document, continuedLinePointer-1)){
                            continuedLinePointer--;
                            indentCorrection += calcIndentCorrectionSingleline(document, 
                                    continuedLinePointer, NEXT_LINE);
                        }
                        indentCorrection -= continuationIndentStepSize * tabWidth;
                    }
                }
            } catch (BadLocationException e) {}            
        }
        return indentCorrection;
    }
        
        
    private int calcIndentCorrectionSingleline(
            IDocument document, int lineNumber, int lineSelect) 
            throws BadLocationException {
        
        int indentCorrection = 0;
        int openCloseCount = 0;
        int middleCount = 0;  // keywords like else, case, ...
        int keywordCount = 0;
        ITypedRegion[] partitions = getLinePartitions(document, lineNumber);
        
//      search partitions for keywords
        for (int partitionIndex = 0; partitionIndex < partitions.length; partitionIndex++) {
            ITypedRegion currentPartition = partitions[partitionIndex]; 
            String partitionType = currentPartition.getType(); 
            if (partitionType.equals(MatlabPartitionScanner.MATLAB_KEYWORD)){
                int keywordOffset = currentPartition.getOffset();
                int keywordLength = currentPartition.getLength();
                String keyword = document.get(keywordOffset, keywordLength);

                boolean ignore = false;
                if (keyword.equals("end") && keywordOffset>0){
                    int lineOffset = document.getLineOffset(lineNumber);
                    String lineTillKeyword = document.get(lineOffset, keywordOffset-lineOffset);
                    // ignore 'end' as array index
                    if (Pattern.matches(".*[:\\(\\{]\\s*", lineTillKeyword) ||
                            Pattern.matches(".*\\[[^\\]]*", lineTillKeyword) ){
                        ignore = true;
                    }
                }
                
                if (!ignore){
                    if (this.keywordIndentInfo.containsKey(keyword)){ 
                        // Indentrules for keyword found
                        keywordCount++;
                        IndentInfo keywordIndentInfo;
                        keywordIndentInfo = this.keywordIndentInfo.get(keyword);
                        
                        if (keywordIndentInfo.openClose == 0){
                            middleCount++;
                        } else {
                            openCloseCount += keywordIndentInfo.openClose;
                        }
                    }
                }
            }
        }
        if (lineSelect == THIS_LINE){
            if (keywordCount == 1 && middleCount > 0){
                openCloseCount--;
            }
            indentCorrection =  Math.min(openCloseCount, 0) * this.tabWidth;
        } else if (lineSelect == NEXT_LINE){
            if (keywordCount == 1 && middleCount > 0){
                openCloseCount++;
            }
            indentCorrection =  Math.max(openCloseCount, 0) * this.tabWidth;
        }
        return indentCorrection;
    }


    /**
     * generates a string that can be used for an indentation of the given length
     * it will contain tabs and spaces, depending on the preferences
     * @param indentLength indentation length, equals number of spaces if spaces are used
     * @return generated string
     */
    private String generateIndentString(int indentLength) {
        StringBuffer indentString = new StringBuffer();
        if (this.useSpaces){
            for (int i=0; i<indentLength; i++){
                indentString.append(' ');
            }
        } else {
        	int tw = this.tabWidth;
        	if(tw == 0)
        		tw = 4;
            for (int i=0; i<indentLength/tw; i++){
                indentString.append('\t');
            }
            for (int i=0; i<indentLength%tw; i++){
                indentString.append(' ');
            }
        }
        return indentString.toString();
    }
    
    
    /**
     * calculates the current indentation of the given line
     * @param document Document to work on
     * @param lineNumber line which indentation is guessed
     * @return exact length of indentation, 
     *         equals number of spaces needed for this indentation
     * @throws BadLocationException
     */
    private int getIndentLength(IDocument document, int lineNumber) 
    throws BadLocationException {
        
        int indentLength = 0;
        ITypedRegion[] linePartitions = getLinePartitions(document, lineNumber);
        ITypedRegion firstPartition   = linePartitions[0];
        
        if (isWhitespacePartition(firstPartition)){
            int tabCount = 0;
            int spaceCount = 0;
            int whitespaceStart = firstPartition.getOffset();
            int whitespaceEnd   = whitespaceStart + firstPartition.getLength();
            
            for (int searchOffset = whitespaceStart; searchOffset < whitespaceEnd; 
                    searchOffset++){
                char currentChar = document.getChar(searchOffset);
                if (currentChar == ' '){
                    spaceCount++;
                } else if (currentChar == '\t') {
                    tabCount++;
                }
            }
            indentLength = tabCount * this.tabWidth;
            indentLength += spaceCount;
        }
        return indentLength;
    }
    
    
    private ITypedRegion[] getLinePartitions(IDocument document, int lineNumber) 
    throws BadLocationException {
        
        IRegion lineInformation = document.getLineInformation(lineNumber);
        int lineOffset = lineInformation.getOffset();
        int lineLength = lineInformation.getLength();
        ITypedRegion[] partitions = document.computePartitioning(lineOffset, lineLength);
        return partitions;
    }

    
    private void initKeywords(){
        try {
            this.keywordIndentInfo = new Hashtable<String, IndentInfo>(11,(float)1.0);

            this.keywordIndentInfo.put("case",      new IndentInfo(0));
            this.keywordIndentInfo.put("catch",     new IndentInfo(0));
            this.keywordIndentInfo.put("else",      new IndentInfo(0));
            this.keywordIndentInfo.put("elseif",    new IndentInfo(0));
            this.keywordIndentInfo.put("end",       new IndentInfo(-1));
            this.keywordIndentInfo.put("for",       new IndentInfo(1));
            this.keywordIndentInfo.put("if",        new IndentInfo(1));
            this.keywordIndentInfo.put("otherwise", new IndentInfo(0));
            this.keywordIndentInfo.put("switch",    new IndentInfo(1));
            this.keywordIndentInfo.put("try",       new IndentInfo(1));
            this.keywordIndentInfo.put("while",     new IndentInfo(1));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
    
    
    private void initWhitespaces(){
        this.whitespaceList = new Vector<String>(4,1);
        this.whitespaceList.add(" ");
//        this.whitespaceList.add("\t");
        this.whitespaceList.add(";");
        this.whitespaceList.add(",");
    }
    

    /**
     * Replaces tabs if needed by indent string or just a space depending of the
     * tab location
     * 
     */
/*    protected String convertTabs(
        IDocument document, int length, String text, int offset, 
        String indentString) throws BadLocationException 
    {
        // only interresting if it contains a tab (also if it is a tab only)
        if (text.indexOf("\t") >= 0) {
            // get some text infos
            int lineStart = 
                document.getLineInformationOfOffset(offset).getOffset();
            String line = document.get(lineStart, offset - lineStart);
            // only a single tab?
            if (text.equals("\t")) {
                deleteWhitespaceAfter(document, offset);
                if (isWhitespace(line))
                    text = indentString;
                else
                    text = indentString;
                // contains a char (pasted text)
            } else {
                byte[] byteLine = text.getBytes();
                StringBuffer newText = new StringBuffer();
                for (int count = 0; count < byteLine.length; count++) {
                    if (byteLine[count] == '\t')
                        newText.append(indentString);
                        // if it is not a tab add the char
                    else
                        newText.append((char) byteLine[count]);
                }
                text = newText.toString();
            }
        }
        return text;
    }
*/    
    
    
    /**
     * Structure for describing indentation rules.
     * This class is primary thought to be used in combination with keywords.
     * @author Georg Huhs
     */
    private class IndentInfo{
        /** is this an opening (positive) or closing (negative) command, or none of both (0) */
        public int openClose;

        public static final int MIN_STEP = -1;
        public static final int MAX_STEP =  1;
        
        /**
         * @param openClose is this an opening or closing command.
         * positive values for opening, negative values for closing, 
         * 0 if neither opening nor closing
         * @throws IllegalArgumentException
         */
        IndentInfo(int openClose) throws IllegalArgumentException{
                          if (checkArg(openClose)){
                this.openClose = openClose;
            } else {
                IllegalArgumentException ex = 
                    new IllegalArgumentException("Indent stepsize must be between " 
                            + MIN_STEP + " and " + MIN_STEP);
                throw(ex);
            }
        }
        
        private boolean checkArg(int arg){
            return (arg >= MIN_STEP && arg <= MAX_STEP);
        }
    }
    
    
    /**
     * Structure that holds some basic information about a line. 
     * @author Georg Huhs
     */
    private class LineInfo{
        
        public int lineNumber;
        public int lineOffset;
        public int lineLength;


        LineInfo(int lineNumber, int lineOffset, int lineLength){
            this.lineNumber = lineNumber;
            this.lineOffset = lineOffset;
            this.lineLength = lineLength;
        }
    }
        
}
