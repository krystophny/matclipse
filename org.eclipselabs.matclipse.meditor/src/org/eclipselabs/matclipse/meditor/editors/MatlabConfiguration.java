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
 *     2007-03-09
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.editors;

 
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.RGB;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.MatlabenginePrefsPage;
import org.eclipselabs.matclipse.meditor.editors.partitioner.MatlabPartitionScanner;
import org.eclipselabs.matclipse.meditor.util.ColorManager;


public class MatlabConfiguration extends SourceViewerConfiguration {
    private MatlabDoubleClickStrategy doubleClickStrategy;
    private ColorManager colorManager;
    private String[] indentPrefixes = { "    ", "\t", ""};
    private PresentationReconciler reconciler;

    
    public MatlabConfiguration(ColorManager colorManager) {
        this.colorManager = colorManager;
    }
    
    
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return MatlabPartitionScanner.getConfiguredContentTypes(); 
    }
    
    
    public int getTabWidth(ISourceViewer sourceViewer) {
        return Activator.getDefault().getPluginPreferences().getInt(
                MatlabenginePrefsPage.TAB_WIDTH);
    }
    
    
    public IUndoManager getUndoManager(ISourceViewer sourceViewer) {
        return new TextViewerUndoManager(100);
    }

    
    public ITextDoubleClickStrategy getDoubleClickStrategy(
        ISourceViewer sourceViewer,
        String contentType) {
        if (doubleClickStrategy == null)
            doubleClickStrategy = new MatlabDoubleClickStrategy();
        return doubleClickStrategy;
    }
    
    
    public void resetIndentPrefixes() {
        Preferences prefs = Activator.getDefault().getPluginPreferences();
        int tabWidth = prefs.getInt(MatlabenginePrefsPage.TAB_WIDTH);
        StringBuffer spaces = new StringBuffer(8);
        for (int i = 0; i < tabWidth; i++)
            spaces.append(" ");

        boolean spacesFirst = prefs.getBoolean(MatlabenginePrefsPage.SUBSTITUTE_TABS); 
        if (spacesFirst) {
            this.indentPrefixes[0] = spaces.toString();
            this.indentPrefixes[1] = "\t";
        }
        else {
            this.indentPrefixes[0] = "\t";
            this.indentPrefixes[1] = spaces.toString();
        }
    }

    
    public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, 
            String contentType) {
        IAutoEditStrategy[] strategies = new IAutoEditStrategy[1];
        MatlabAutoEditStrategy autoEditStrategy = MatlabAutoEditStrategy.getInstance(); 
        autoEditStrategy.setSourceViewer(sourceViewer);
        strategies[0] = autoEditStrategy;
        return strategies;
    }
    
    
    public String[] getIndentPrefixes(
            ISourceViewer sourceViewer,
            String contentType) {
        resetIndentPrefixes();
        sourceViewer.setIndentPrefixes(this.indentPrefixes, contentType);
        return this.indentPrefixes;
    }
    
    
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        if (reconciler == null)
        {
            reconciler = new PresentationReconciler();
            initReconciler();            
        }
        return reconciler;
    }

    
    private void initReconciler() {
        Preferences prefs = Activator.getDefault().getPluginPreferences();
        
        setDamagerRepairer(prefs, 
                MatlabenginePrefsPage.COMMENT_COLOR, 
                MatlabPartitionScanner.MATLAB_COMMENT);
        setDamagerRepairer(prefs, 
                MatlabenginePrefsPage.COMMENT_COLOR, 
                MatlabPartitionScanner.MATLAB_CONTINUATION);
        setDamagerRepairer(prefs, 
                MatlabenginePrefsPage.STRING_COLOR, 
                MatlabPartitionScanner.MATLAB_SINGLELINE_STRING);
        setDamagerRepairer(prefs, 
                MatlabenginePrefsPage.KEYWORD_COLOR, 
                MatlabPartitionScanner.MATLAB_KEYWORD);
        setDamagerRepairer(prefs, 
                MatlabenginePrefsPage.KEYWORD_COLOR, 
                MatlabPartitionScanner.MATLAB_OPERATOR);
        setDamagerRepairer(prefs, 
                MatlabenginePrefsPage.FUNCTION_COLOR, 
                MatlabPartitionScanner.MATLAB_FUNCTION);
        setDamagerRepairer(prefs, 
                MatlabenginePrefsPage.TOOLBOX_COLOR,     
                MatlabPartitionScanner.TOOLBOX_FUNCTION);
        setDamagerRepairer(prefs, 
                MatlabenginePrefsPage.NUMBER_COLOR, 
                MatlabPartitionScanner.MATLAB_NUMBER);
        setDamagerRepairer(prefs, 
                MatlabenginePrefsPage.CODE_COLOR, 
                MatlabPartitionScanner.MATLAB_CODE);
        setDamagerRepairer(prefs, 
                MatlabenginePrefsPage.CODE_COLOR, 
                IDocument.DEFAULT_CONTENT_TYPE);

    }

    
    private void setDamagerRepairer(Preferences prefs, String color, String token) {
        TextAttribute attribute  = generateTextAttribute(prefs, color); 
        NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(attribute);
        reconciler.setDamager(ndr, token);
        reconciler.setRepairer(ndr, token);
    }
    
    
    public void updateSyntaxColor(String name)
    {
        if (reconciler != null) {
            Preferences prefs = Activator.getDefault().getPluginPreferences();
            Vector<String> tokens = new Vector<String>(3,1);

            // get changed property
            if (name.equals(MatlabenginePrefsPage.COMMENT_COLOR)){
                tokens.add(MatlabPartitionScanner.MATLAB_COMMENT);
                tokens.add(MatlabPartitionScanner.MATLAB_CONTINUATION);
            } else if (name.equals(MatlabenginePrefsPage.CODE_COLOR)){
                tokens.add(MatlabPartitionScanner.MATLAB_CODE);
                tokens.add(IDocument.DEFAULT_CONTENT_TYPE);
            } else if (name.equals(MatlabenginePrefsPage.NUMBER_COLOR)){
                tokens.add(MatlabPartitionScanner.MATLAB_NUMBER);
            } else if (name.equals(MatlabenginePrefsPage.KEYWORD_COLOR)){
                tokens.add(MatlabPartitionScanner.MATLAB_KEYWORD);
            } else if (name.equals(MatlabenginePrefsPage.FUNCTION_COLOR)){
                tokens.add(MatlabPartitionScanner.MATLAB_FUNCTION);
            } else if (name.equals(MatlabenginePrefsPage.TOOLBOX_COLOR)){ 
                tokens.add(MatlabPartitionScanner.TOOLBOX_FUNCTION);
           } else if (name.equals(MatlabenginePrefsPage.STRING_COLOR)){
                tokens.add(MatlabPartitionScanner.MATLAB_SINGLELINE_STRING);
            }
                        
            // change presentation
            for (Iterator<String> iter = tokens.iterator(); iter.hasNext();) {
                String currentToken = iter.next();                
                NonRuleBasedDamagerRepairer ndr = 
                    (NonRuleBasedDamagerRepairer)reconciler.getRepairer(currentToken);
                TextAttribute attribute = generateTextAttribute(prefs, name); 
                ndr.setTextAttribute(attribute);
            }
        }
    }

    
    private TextAttribute generateTextAttribute(Preferences prefs, String color) {
        String colorname = prefs.getString(color);
        RGB rgb = StringConverter.asRGB(colorname, new RGB(0,0,0));
        TextAttribute attribute  = new TextAttribute(colorManager.getColor(rgb));
        return attribute;
    }

}
