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
 *      2008-01-22
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.actions;


import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.MatlabengineExportPrefsPage;
import org.eclipselabs.matclipse.meditor.editors.MatlabEditor;
import org.eclipselabs.matclipse.meditor.editors.MatlabSelection;
import org.eclipselabs.matclipse.meditor.util.XMLExporter;
import org.eclipselabs.matclipse.meditor.util.XMLExporterThread;


/**
 * Exports the editors content as file. Various formats are possible. 
 * @author Georg Huhs
 */
public class MatlabExportAsAction extends MatlabAction implements IEditorActionDelegate{

    private final static String PREF_LAST_PATH      = "__LAST_PATH";
    private final static String PREF_LAST_EXTENSION = "__LAST_EXTENSION";

    
    public MatlabExportAsAction(String text) {
        super(text);
    }
    
    
    public MatlabExportAsAction() {
        super("");
    }

    
    public void run(IAction action) {
        try {
            MatlabEditor editor = (MatlabEditor)getTextEditor();
            String matlabFileName = editor.getEditorInput().getName();

            // ask user for filename:
            FileDialog saveDialog = new FileDialog(editor.getSite().getShell(), SWT.SAVE);
            saveDialog.setFilterExtensions(new String[]{
                    "*.xml", "*.html", "*.tex", "*.pdf", "*.*"});
            saveDialog.setFilterNames(new String[]{
                    "XML files", "HTML files", "LaTeX source files", "PDF files", "all files"});
            saveDialog.setText("Export");
            // start from the directory last used
            IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
            String lastPath      = preferenceStore.getString(PREF_LAST_PATH);
            String lastExtension = preferenceStore.getString(PREF_LAST_EXTENSION);
            if (!lastPath.equals("")){
                saveDialog.setFilterPath(lastPath);
            }
            // suggest a filename similar to the matlab-file's name
            String extension = "xml";
            if (!lastExtension.equals("")){
                extension = lastExtension;
            }
            String suggestedFileName = matlabFileName.replace(".m", "."+extension);
            saveDialog.setFileName(suggestedFileName);

            // show dialog to ask user for filename
            String targetFileName = saveDialog.open();
            if (targetFileName != null){
                IPath targetFilePath = Path.fromOSString(targetFileName);
                String targetFileExtension = targetFilePath.getFileExtension();

                boolean xmlOutput   = targetFileExtension.compareToIgnoreCase("xml")  == 0;
                boolean htmlOutput  = targetFileExtension.compareToIgnoreCase("html") == 0;
                boolean latexOutput = targetFileExtension.compareToIgnoreCase("tex")  == 0;
                boolean pdfOutput   = targetFileExtension.compareToIgnoreCase("pdf")  == 0;
                boolean someOutput  = xmlOutput || htmlOutput || latexOutput || pdfOutput;

                // export
                if (!targetFilePath.isEmpty() && someOutput){
                    // save used directory
                    String outputDir = saveDialog.getFilterPath();
                    preferenceStore.setValue(PREF_LAST_PATH, outputDir);
                    preferenceStore.setValue(PREF_LAST_EXTENSION, targetFileExtension);
                    MatlabSelection selection = new MatlabSelection (editor, false );
                    IDocument document = selection.doc;
                    IPath XMLFilePath = 
                        targetFilePath.removeFileExtension().addFileExtension("xml");

                    Properties metadata = MatlabengineExportPrefsPage.getMetadataAsProperties();
                    metadata.setProperty(XMLExporter.FILETITLE_KEY, matlabFileName);

                    Properties exportProperties = 
                        MatlabengineExportPrefsPage.getExportProperties();

                    exportProperties.setProperty(
                            XMLExporter.OUTPUT_XML_KEY,   Boolean.toString(xmlOutput));
                    exportProperties.setProperty(
                            XMLExporter.OUTPUT_HTML_KEY,  Boolean.toString(htmlOutput));
                    exportProperties.setProperty(
                            XMLExporter.OUTPUT_LATEX_KEY, Boolean.toString(latexOutput));
                    exportProperties.setProperty(
                            XMLExporter.OUTPUT_PDF_KEY,   Boolean.toString(pdfOutput));

                    XMLExporterThread exporter = new XMLExporterThread(
                            document, metadata, exportProperties, XMLFilePath);

                    exporter.doExportOperations();
                } else {
                    Activator.errorDialog(XMLExporterThread.EXPORT_EXCEPTION_MSG, 
                            new Exception("No target file or no supported filetype specified"));
                }
            }
        } catch (Exception e) {
            Activator.errorDialog(XMLExporterThread.EXPORT_EXCEPTION_MSG, e);
        }
    }

    
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);        
    }

}
