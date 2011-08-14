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

package org.eclipselabs.matclipse.meditor.editors;


import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.MatlabenginePrefsPage;
import org.eclipselabs.matclipse.meditor.util.ColorManager;


public class MatlabEditor extends MatlabEditProjection  {
    MatlabConfiguration editConfiguration;
    private ColorManager colorManager;
    static public String ACTION_OPEN = "OpenEditor";

    static public String EDITOR_ID = "org.eclipselabs.matclipse.meditor.editors.MatlabEditor";
    private Preferences.IPropertyChangeListener prefListener;

    
    public MatlabEditor() {
        super();
        colorManager = new ColorManager();
        editConfiguration=new MatlabConfiguration(colorManager);
        setSourceViewerConfiguration(editConfiguration);
        setDocumentProvider(new MatlabDocumentProvider());
        setRangeIndicator(new DefaultRangeIndicator()); 
        
        setEditorContextMenuId("org.eclipselabs.matclipse.meditor.editors.MatlabEditor.contextmenu");
    }
    
    
    public void dispose() {
        colorManager.dispose();
        super.dispose();
    }
    
    
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        super.init(site, input);
        
        prefListener = new Preferences.IPropertyChangeListener() {
            public void propertyChange(Preferences.PropertyChangeEvent event) {
                String property = event.getProperty();

                if (property.equals(MatlabenginePrefsPage.TAB_WIDTH)
                        ||property.equals(MatlabenginePrefsPage.SUBSTITUTE_TABS)) {
                    ISourceViewer sourceViewer = getSourceViewer();
                    if (sourceViewer == null)
                        return;
                    sourceViewer.getTextWidget().setTabs(
                            Activator.getDefault().getPluginPreferences().getInt(
                                    MatlabenginePrefsPage.TAB_WIDTH));
                    MatlabAutoEditStrategy editStrategy = (MatlabAutoEditStrategy)
                        editConfiguration.getAutoEditStrategies(sourceViewer, "")[0];
                    editStrategy.setupStrategy();
                } else if (
                        property.equals(MatlabenginePrefsPage.CODE_COLOR) ||
                        property.equals(MatlabenginePrefsPage.NUMBER_COLOR) ||
                        property.equals(MatlabenginePrefsPage.KEYWORD_COLOR) ||
                        property.equals(MatlabenginePrefsPage.FUNCTION_COLOR) ||
                        property.equals(MatlabenginePrefsPage.COMMENT_COLOR) ||
                        property.equals(MatlabenginePrefsPage.STRING_COLOR) ) {

                    editConfiguration.updateSyntaxColor(property);
                    getSourceViewer().invalidateTextPresentation();
                } 
            }
        };
        MatlabenginePrefsPage.getPreferences().addPropertyChangeListener(prefListener);
    }
    
    
    public IPath getInputFilePath() {
        IPath path = null;
        IEditorInput input = getEditorInput();
        if (input instanceof FileEditorInput){
            FileEditorInput feInput = (FileEditorInput)input;
            path = feInput.getPath();
        }
        return path;
    }

    
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] { 
                "org.eclipselabs.matclipse.meditor.matlabEditorScope" });
    }

    
    public void setSelection(int offset, int length) {
        ISourceViewer sourceViewer = getSourceViewer();
        sourceViewer.setSelectedRange(offset, length);
        sourceViewer.revealRange(offset, length); 
    }
    
    
    protected void initializeEditor(){
        super.initializeEditor();        
        this.setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }
    
    
    public IStatusLineManager getStatusLineManager(){
        IStatusLineManager manager = null;
        IEditorActionBarContributor contributor = 
            getEditorSite().getActionBarContributor();
        
        if (contributor instanceof EditorActionBarContributor){ 
            manager =  ((EditorActionBarContributor) contributor).
            getActionBars().getStatusLineManager();
        }
        
        return manager;
    }

}
