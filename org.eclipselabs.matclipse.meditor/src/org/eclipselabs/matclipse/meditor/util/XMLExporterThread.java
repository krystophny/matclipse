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
 *     2008-01-14
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.util;


import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;

import org.eclipselabs.matclipse.meditor.editors.IWorkerThreadListener;


/**
 * An interface to XMLExporter. Meant to be used by an editor. 
 * This class can be fed with a document and all export preferences needed and it 
 * does the export operations. Either a direct use or processing in an extra thread is possible. 
 * If the threaded version is used, also listeners for the thread can be specified. They will be 
 * notified if an exception occurred and when the work is finished. 
 * @author Georg Huhs
 *
 */
public class XMLExporterThread implements Runnable {
    
    private IDocument document;
    private Properties metadata;
    private Properties exportProperties;
    private IPath XMLFilePath;
    
    private Vector<IWorkerThreadListener> listeners = 
        new Vector<IWorkerThreadListener>();
    private Object listenerLock = new Object();
    private HashMap<String, IPath> generatedFiles;

    public final static String EXPORT_EXCEPTION_MSG = 
        "Error during export operations, detailed information attached";

    
    /**
     * Only constructor. Here all export-data needs to be specified
     * @param document Document to export. 
     * @param metadata Metadata as Properties
     * @param exportProperties Exportproperties as Properties. 
     * @param XMLFilePath Path to the XML file to generate.
     */
    public XMLExporterThread(IDocument document, Properties metadata, 
            Properties exportProperties, IPath XMLFilePath){

        this.document         = document;
        this.metadata         = metadata;
        this.exportProperties = exportProperties;
        this.XMLFilePath      = XMLFilePath;
    }

    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            doExportOperations();
        } catch (Exception e) {
            notifyListenersException(EXPORT_EXCEPTION_MSG, e);
        } finally {
            notifyListenersFinished();
        }
    }

    
    /**
     * Does all operations based on the data in the export properties. 
     * @throws Exception
     */
    public void doExportOperations() throws Exception {
        XMLExporter exporter = new XMLExporter();
        exporter.export(this.document, null, this.metadata, this.XMLFilePath);
        
        boolean xmlOutput = Boolean.parseBoolean(
                this.exportProperties.getProperty(XMLExporter.OUTPUT_XML_KEY));
        boolean htmlOutput = Boolean.parseBoolean(
                this.exportProperties.getProperty(XMLExporter.OUTPUT_HTML_KEY));
        boolean latexOutput = Boolean.parseBoolean(
                this.exportProperties.getProperty(XMLExporter.OUTPUT_LATEX_KEY));
        boolean pdfOutput = Boolean.parseBoolean(
                this.exportProperties.getProperty(XMLExporter.OUTPUT_PDF_KEY));
        boolean coreonly = Boolean.parseBoolean(
                this.exportProperties.getProperty(XMLExporter.OUTPUT_COREONLY_KEY));

        // some tests if the specified options make sense
        if (latexOutput && coreonly){
            pdfOutput = false;
        }
        if (!latexOutput){
            exportProperties.setProperty(
                    XMLExporter.OUTPUT_COREONLY_KEY, Boolean.toString(false));
        }
        String[] xsltFiles = XMLExporter.extractXsltFiles(this.exportProperties);

        if (htmlOutput){
            String filetitle = this.metadata.getProperty(XMLExporter.FILETITLE_KEY);
            exporter.transformToHTML(filetitle, xsltFiles[0]);
        } 
        if (latexOutput || pdfOutput){
            exporter.transformToLatexAndPdf(latexOutput, pdfOutput, xsltFiles[1]); 
        }
        if (!xmlOutput){
            exporter.deleteXMLFile();
        }
        this.generatedFiles = exporter.getGeneratedFiles();

        // if an Exception occurred that didn't stop the exporter. (Non fatal exceptions.)
        Exception e = exporter.fetchException();
        if (e != null){
            throw e;
        }
    }

    
    public HashMap<String, IPath> getGeneratedFiles(){
        return this.generatedFiles;
    }

    
    public boolean addWorkerThreadListener(IWorkerThreadListener listener){
        boolean added = false;
        synchronized (listenerLock) {
            if (!listeners.contains(listener)){
                listeners.add(listener);
                added = true;
            }
        }
        return added;
    }
    
    
    public boolean removeWorkerThreadListener(IWorkerThreadListener listener){
        synchronized (listenerLock) {
            return listeners.remove(listener);
        }
    }

    
    private void notifyListenersException(String message, Exception e){
        synchronized (listenerLock) {
            for (IWorkerThreadListener listener : listeners) {
                listener.exceptionOccured(message, e);
            }
        }
    }

    
    private void notifyListenersFinished(){
        synchronized (listenerLock) {
            for (IWorkerThreadListener listener : listeners) {
                listener.workFinished(this.generatedFiles);
            }
        }
    }

}
