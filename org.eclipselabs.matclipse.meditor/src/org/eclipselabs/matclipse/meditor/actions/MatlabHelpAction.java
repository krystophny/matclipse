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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.HelpBrowserPrefsPage;
import org.eclipselabs.matclipse.meditor.editors.MatlabSelection;
import org.eclipselabs.matclipse.meditor.editors.partitioner.MatlabPartitionScanner;
import org.eclipselabs.matclipse.meditor.util.BrowserException;
import org.eclipselabs.matclipse.meditor.util.BrowserLauncherM;
import org.eclipselabs.matclipse.meditor.util.HelpExceptionFileReader;


/**
 * Provides context sensitive help.
 * If the cursor is placed at a Matlab-function this action opens a browser
 * with the help-page of this function. 
 * This class' behavior is configured by properties of the Help Browser Preference Page 
 * and two files:
 *     EXCEPTIONFILE    - Contains associations: Matlabfunction->helpfile
 *     HELPFILEFUNCFILE - Contains a list of all functions for which a 
 *                        helfile with a similar name exists
 * @author Georg Huhs
 *
 */
public class MatlabHelpAction extends MatlabAction implements IEditorActionDelegate{

    private final static String HTML_EXTENSION = ".html";
    private Vector<String> helpfileFunctions = null;

    /** name of the property file which contains information about the directories to use */
    private final static String EXCEPTIONFILE     = "helpfileexceptions.xml";
    private final static String HELPFILEFUNCFILE  = "help_funcs";

    private final static String HELP_HTML_BASE_DEF = 
        "http://itp.tugraz.at/matlab/";    
    private final static String REFERENCE_HTML_BASE_DEF = 
        HELP_HTML_BASE_DEF + "techdoc/ref/";
    
    private Hashtable<String, String> exceptionMap = null;
    private String helpHTMLBase = HELP_HTML_BASE_DEF;
    private String referenceHTMLBase = REFERENCE_HTML_BASE_DEF;
    
    private boolean initOk = false;
    private Exception initException;
    
    
    public MatlabHelpAction(String text) {
        super(text);
        init();
    }
    
    
    public MatlabHelpAction() {
        super("");
        init();
    }

    
    private void init(){
        // init directories
        String configFileDir;
        try {
            this.helpHTMLBase = Activator.getDefault().getPreferenceStore().
                getString(HelpBrowserPrefsPage.HTML_HELP_BASE);
            this.referenceHTMLBase = this.helpHTMLBase + "/techdoc/ref/";

            configFileDir = Activator.getDefault().getPluginDir(Activator.CONFIGDIR);
            
            // init helpfileindex
            this.helpfileFunctions = new Vector<String>();
            readFunctionFile(configFileDir + HELPFILEFUNCFILE);

            // init exception map
            this.exceptionMap = 
                HelpExceptionFileReader.readHelpFile(configFileDir, EXCEPTIONFILE);

            // if no file was read
            if (this.exceptionMap == null){
                this.exceptionMap = new Hashtable<String, String>();
                this.exceptionMap.put("+",  "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put(".+", "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put("-",  "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put(".-", "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put("*",  "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put(".*", "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put("/",  "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put("./", "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put("\\", "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put(".\\","techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put("^",  "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put(".^", "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put("'",  "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put(".'", "techdoc/matlab_prog/ch12_nd9.html");
                this.exceptionMap.put("<",  "techdoc/matlab_prog/ch12_n10.html");
                this.exceptionMap.put("<=", "techdoc/matlab_prog/ch12_n10.html");
                this.exceptionMap.put(">",  "techdoc/matlab_prog/ch12_n10.html");
                this.exceptionMap.put(">=", "techdoc/matlab_prog/ch12_n10.html");
                this.exceptionMap.put("==", "techdoc/matlab_prog/ch12_n10.html");
                this.exceptionMap.put("~=", "techdoc/matlab_prog/ch12_n10.html");
                this.exceptionMap.put("&",  "techdoc/matlab_prog/ch12_n11.html");
                this.exceptionMap.put("|",  "techdoc/matlab_prog/ch12_n11.html");
                this.exceptionMap.put("~",  "techdoc/matlab_prog/ch12_n11.html");
                this.exceptionMap.put("&&", "techdoc/matlab_prog/ch12_n11.html");
                this.exceptionMap.put("||", "techdoc/matlab_prog/ch12_n11.html");
                this.exceptionMap.put(":",  "techdoc/ref/colon.html");
            }
            this.initOk = true;
        } catch (Exception e) {
            this.initException = e;
        }
        
    }

    
    public void run(IAction action) {
        if (this.initOk){
            try {
                MatlabSelection selection = new MatlabSelection ( getTextEditor ( ), false );
                IDocument document = selection.doc;
                ITypedRegion partition = null;
                String urlToOpen = null;


                ITypedRegion partition1 = document.getPartition(selection.absoluteCursorOffset);
                int prevPartitionPos = Math.max(0, selection.absoluteCursorOffset-1);
                ITypedRegion partition2 = document.getPartition(prevPartitionPos);
                if (isSupportedPartition(partition1)){
                    partition = partition1;
                } else if (isSupportedPartition(partition2)){
                    partition = partition2;
                }

                if (partition != null){ 
                    int wordOffset = partition.getOffset();
                    int wordLength = partition.getLength();
                    String searchWord = document.get(wordOffset, wordLength);

                    String searchFilename = searchWord + HTML_EXTENSION;

                    if (this.helpfileFunctions.contains(searchWord)){
                        urlToOpen = this.referenceHTMLBase + searchFilename;
                    }
                    if (this.exceptionMap.containsKey(searchWord)){
                        urlToOpen = this.helpHTMLBase + this.exceptionMap.get(searchWord);
                    }
                } else {
                    urlToOpen = Activator.getDefault().getPreferenceStore().
                    getString(HelpBrowserPrefsPage.HTML_HELP_DEFPAGE);
                }
                if (urlToOpen != null){
                    launchBrowser(urlToOpen);
                }


            } catch (BadLocationException e) {
                Activator.errorDialog("Internal error with the document", e);
            } catch (BrowserException e) {
                Activator.errorDialog("Wasn't able to open Browser", e);
            }
        } else {
          Activator.errorDialog(
                  "Wasn't able to initialize help functionality", 
                  this.initException);
        }
    }


    private boolean isSupportedPartition(ITypedRegion partition) {
        return partition.getType().equals(MatlabPartitionScanner.MATLAB_FUNCTION)
            || partition.getType().equals(MatlabPartitionScanner.MATLAB_KEYWORD)
            || partition.getType().equals(MatlabPartitionScanner.MATLAB_OPERATOR);
    }
    

    public void readFunctionFile(String filename) throws IOException{
        File functionListFile = new File(filename);
        FileReader functionListFileReader = new FileReader(functionListFile);
        BufferedReader fileReader = new BufferedReader(functionListFileReader);
        String currentWord = null;
        while((currentWord = fileReader.readLine()) != null){
            if (currentWord.length() > 0){
                this.helpfileFunctions.add(currentWord);
            }
        }
    }

    
    private void launchBrowser(String urlToOpen) throws BrowserException{
        BrowserLauncherM.openURL(urlToOpen);
    }

    
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);
        
    }
}
