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

package org.eclipselabs.matclipse.meditor.util;


import java.io.FileReader;
import java.util.Hashtable;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Reads a XML file which contains a list of matlab-functions with their associated
 * HTML-help pages. 
 * @author osiris
 *
 */
public class HelpExceptionFileReader extends DefaultHandler {
    
    private final static String EXCEPTION_TAG  = "exception";
    private final static String WORD_ATTRIBUTE = "word";

    private Hashtable<String, String> exceptionMap = null;
    private String       currentWord = null;
    private StringBuffer currentURL  = null;
    

    public HelpExceptionFileReader(){
        super();
    }
    
    
    /**
     * Reads the specified file and returns it's content as a Hashtable, where 
     * the Matlab-function is used as key and the help page is given as value
     * @param directory Directory that contains the file to read
     * @param filename Name of the file to read
     * @return Hashtable which contains all specified functions and help pages
     * @throws Exception 
     */
    public static Hashtable<String, String> readHelpFile(String directory, String filename) 
            throws Exception{
        
        Hashtable<String, String> exceptionMap = null;
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            HelpExceptionFileReader handler = new HelpExceptionFileReader();
            reader.setContentHandler(handler);
            reader.setErrorHandler(handler);
            FileReader fileReader = new FileReader(directory + filename);
            InputSource inputSource = new InputSource(fileReader);
            inputSource.setSystemId(directory);
            reader.parse(inputSource);
            exceptionMap = handler.getExceptionMap();
        } catch (Exception e) {
            throw new Exception(e);
        }
        return exceptionMap;
    }
    
    
    public Hashtable<String, String> getExceptionMap(){
        return this.exceptionMap;
    }
    
    
    /**
     * @see org.xml.sax.helpers.Defaulthandler#startDocument()
     */
    public void startDocument (){
        this.exceptionMap = new Hashtable<String, String>();
    }
    
    
    /**
     * @see org.xml.sax.helpers.DefaultHandler
     * #startElement(java.lang.String, java.lang.String, 
     * java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement (String uri, String name,
            String qName, Attributes atts){

        if (name.equals(EXCEPTION_TAG)){
            this.currentURL  = new StringBuffer();
            this.currentWord = atts.getValue("", WORD_ATTRIBUTE);
        }
    }
    
    
    /**
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    public void characters (char ch[], int start, int length){
        if (this.currentURL != null){
            this.currentURL.append(ch, start, length);
        }
    }    
    
    
    /**
     * @see org.xml.sax.helpers.DefaultHandler
     * #endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement (String uri, String name, String qName){
        if (name.equals(EXCEPTION_TAG)){
            if (this.currentURL.length()>0 && this.currentWord != null){
                this.exceptionMap.put(this.currentWord, this.currentURL.toString());
            }
            this.currentWord = null;
            this.currentURL  = null;
        }
    }

    
    /**
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    public void endDocument (){
    }

}
