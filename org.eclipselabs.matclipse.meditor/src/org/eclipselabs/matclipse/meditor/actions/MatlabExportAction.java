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

package org.eclipselabs.matclipse.meditor.actions;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.MatlabengineExportPrefsPage;
import org.eclipselabs.matclipse.meditor.dialogs.ExportDialog;
import org.eclipselabs.matclipse.meditor.editors.IWorkerThreadListener;
import org.eclipselabs.matclipse.meditor.editors.MatlabEditor;
import org.eclipselabs.matclipse.meditor.editors.MatlabSelection;
import org.eclipselabs.matclipse.meditor.util.BrowserLauncherM;
import org.eclipselabs.matclipse.meditor.util.XMLExporter;
import org.eclipselabs.matclipse.meditor.util.XMLExporterThread;


/**
 * Exports the editor's content as file. Various formats are possible. 
 * @author Georg Huhs
 */
public class MatlabExportAction 
        extends MatlabAction implements IEditorActionDelegate, IWorkerThreadListener{

    public MatlabExportAction(String text) {
        super(text);
    }
     

    public MatlabExportAction() {
        super("");
    }

    
    /**
     * 
     */
    public void run(IAction action) {

        MatlabEditor editor = (MatlabEditor)getTextEditor();
        Preferences exportPreferences = MatlabengineExportPrefsPage.getPreferences();

        String matlabFileName = editor.getEditorInput().getName();
        
        IPath inputFileDir    = editor.getInputFilePath().removeLastSegments(1);
        String exportDialogPropertiesFileName = 
            "." + new Path(matlabFileName).removeFileExtension().toOSString() + 
            "_expProperties.xml";
        IPath exportDialogPropertiesFilePath = inputFileDir.addTrailingSeparator().
                append(exportDialogPropertiesFileName);

        int exportDlgCode = Dialog.OK;
        Properties exportDialogProperties = null;
        if (exportPreferences.getBoolean(MatlabengineExportPrefsPage.SHOW_DIALOG)){
            ExportDialog exportDataDialog = 
                new ExportDialog(editor.getSite().getShell());
            File exportDialogPropertiesFile = exportDialogPropertiesFilePath.toFile();
            if (exportDialogPropertiesFile.exists()){
                Properties oldExportDialogProperties = new Properties();
                try {
                    oldExportDialogProperties.loadFromXML(
                            new FileInputStream(exportDialogPropertiesFile));
                    exportDataDialog.setExportProperties(oldExportDialogProperties);
                } catch (IOException e) {
                    Activator.warningDialog(
                            "Wasn't able to load existing export properties for this file.", e);
                }
            }
            exportDlgCode = exportDataDialog.open();
            exportDialogProperties = exportDataDialog.getExportProperties();
        }

        if (exportDlgCode == Dialog.OK){
            editor.getStatusLineManager().setMessage("Doing export operations");

            // save export properties for this file
            try {
                exportDialogProperties.storeToXML(
                        new FileOutputStream(exportDialogPropertiesFilePath.toFile()), "");
            } catch (IOException e) {
                Activator.warningDialog(
                        "Wasn't able to save export properties for this file.", e);
            }
            
            // get output path
            IPath outputDir = Path.fromOSString(".");
            if (exportPreferences.getBoolean(MatlabengineExportPrefsPage.OUT_USE_INPUTDIR)){
                outputDir = inputFileDir;
            } else {
                outputDir = Path.fromOSString(exportPreferences.getString(
                        MatlabengineExportPrefsPage.OUTPUT_DIRECTORY)).addTrailingSeparator();
            }
            IPath fileBasePath = outputDir.append(matlabFileName).removeFileExtension();

            MatlabSelection selection = new MatlabSelection (editor, false );
            IDocument document = selection.doc;
            IPath XMLFilePath = fileBasePath.addFileExtension("xml");

            Properties metadata = MatlabengineExportPrefsPage.getMetadataAsProperties();
            metadata.setProperty(XMLExporter.FILETITLE_KEY, matlabFileName);

            Properties exportProperties = MatlabengineExportPrefsPage.getExportProperties();

            XMLExporterThread exportThread = 
                new XMLExporterThread(document, metadata, exportProperties, XMLFilePath);
            exportThread.addWorkerThreadListener(this);
            Display.getDefault().asyncExec(exportThread);
        }      
    }

    
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        super.setActiveEditor(targetEditor);
    }

    
    /**
     * If the created files should be shown, this function is called 
     * after the export has finished. A new project is created; all given files are put 
     * into it and are shown. 
     * @param generatedFiles contains all files that have been created
     */
    public void workFinished(HashMap<String, IPath> generatedFiles) {
    	
        MatlabEditor editor = (MatlabEditor)getTextEditor();
        editor.getStatusLineManager().setMessage(null);
        try {
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(-1, new NullProgressMonitor());
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        // show generated files
        if (MatlabengineExportPrefsPage.getPreferences().getBoolean(
                MatlabengineExportPrefsPage.SHOW_OUTPUT)){
            
            Set<String> filetypes = generatedFiles.keySet();
            try {

                // Create and open project for exported files
                IWorkspace ws = ResourcesPlugin.getWorkspace();
                IProject project = ws.getRoot().getProject("Exported files");
                if (!project.exists())
                   project.create(null);
                if (!project.isOpen())
                   project.open(null);
                  
                // empty project
                IResource[] oldExpFiles = project.members();
                for (IResource oldExpFile:oldExpFiles){
                    oldExpFile.delete(IResource.NEVER_DELETE_PROJECT_CONTENT, null);
                }
                
                for(String filetype:filetypes){
                    String filePath = generatedFiles.get(filetype).toOSString();

                    // import file to project
                    IPath location = new Path(filePath);
                    IFile file = project.getFile(location.lastSegment());
                    file.createLink(location, IResource.REPLACE, null);

                    if (filetype.equals(XMLExporter.OUTPUT_HTML_KEY) ||
                            filetype.equals(XMLExporter.OUTPUT_PDF_KEY)){
                        
                        BrowserLauncherM.openURLIntBrowserEdit("file://" + filePath);
                        
                    } else if (filetype.equals(XMLExporter.OUTPUT_LATEX_KEY) ||
                            filetype.equals(XMLExporter.OUTPUT_XML_KEY)){
                        
                        IWorkbench workbench = Activator.getDefault().getWorkbench();
                        IWorkbenchPage page = 
                            workbench.getActiveWorkbenchWindow().getActivePage();
  
                        IEditorDescriptor eDescriptor = 
                            workbench.getEditorRegistry().getDefaultEditor(filePath);

                        if (page != null){
                            if (eDescriptor != null){
                                page.openEditor(
                                        new FileEditorInput(file), 
                                        eDescriptor.getId());
                            } else {
                                String fileName = generatedFiles.get(filetype).lastSegment();
                                Activator.errorDialog(
                                        "No editor for file " + fileName + " found.", null);
                            }
                        }
                        
                    } else if (filetype.equals(XMLExporter.OUTPUT_PDF_KEY)){
                        BrowserLauncherM.openURLExtBrowser("file://" + filePath);
                    }
                }
            } catch (Exception e) {
                Activator.errorDialog("Wasn't able to show generated files.", e);
            }
        }
    }

    
    public void exceptionOccured(String message, Exception e) {
        Activator.errorDialog(message, e);
    }



}
