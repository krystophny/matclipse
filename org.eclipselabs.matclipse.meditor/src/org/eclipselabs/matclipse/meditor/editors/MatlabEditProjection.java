/*******************************************************************************
 * Copyright (c) 2004, 2011 Fabio Zadrozny (Pydev team) and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Fabio Zadrozny - initial API and implementation
 *     Georg Huhs, Winfried Kernbichler, David Camhy (ITPCP) - 
 *         some changes for using in Meditor
 * Last changed: 
 *     2008-01-23
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.editors;


import org.eclipse.jdt.internal.ui.text.JavaPairMatcher;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import org.eclipselabs.matclipse.meditor.MatlabenginePrefsPage;


public abstract class MatlabEditProjection extends TextEditor  {

    private ProjectionSupport fProjectionSupport;
    protected final static char[] BRACKETS= { '{', '}', '(', ')', '[', ']' };
    protected JavaPairMatcher fBracketMatcher= new JavaPairMatcher(BRACKETS);

    
    /**
     * Returns the source viewer decoration support.
     * 
     * @param viewer the viewer for which to return a decoration support
     * @return the source viewer decoration support
     */
    protected ISourceViewer createSourceViewer(Composite parent,
            IVerticalRuler ruler, int styles) {
        ProjectionViewer viewer = 
            new ProjectionViewer(parent, ruler, getOverviewRuler(), true, styles);
        getSourceViewerDecorationSupport(viewer);
        return viewer;
        
    }

    
    protected SourceViewerDecorationSupport getSourceViewerDecorationSupport(
            ISourceViewer viewer) {
        
        if (fSourceViewerDecorationSupport == null) {
            fSourceViewerDecorationSupport= 
                new SourceViewerDecorationSupport(viewer, getOverviewRuler(), 
                        getAnnotationAccess(), getSharedColors());
            configureSourceViewerDecorationSupport(fSourceViewerDecorationSupport);
        }
        return fSourceViewerDecorationSupport;
    }

    
    protected void configureSourceViewerDecorationSupport(
            SourceViewerDecorationSupport support) {
        
        support.setCharacterPairMatcher(fBracketMatcher);
        support.setMatchingCharacterPainterPreferenceKeys(
                MatlabenginePrefsPage.USE_MATCHING_BRACKETS, 
                MatlabenginePrefsPage.MATCHING_BRACKETS_COLOR);
        
        super.configureSourceViewerDecorationSupport(support);
    }
    
    
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        ProjectionViewer viewer = (ProjectionViewer) getSourceViewer();
        fProjectionSupport = new ProjectionSupport(viewer, 
                getAnnotationAccess(), getSharedColors());
        fProjectionSupport
                .addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error");
        fProjectionSupport
                .addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning");
        fProjectionSupport.install();
        viewer.doOperation(ProjectionViewer.TOGGLE);
    }


    public static boolean isFoldingEnabled() {
        return false;
//        return MatlabenginePrefsPage.getPreferences().getBoolean(
//                MatlabenginePrefsPage.USE_CODE_FOLDING);
    }

    
    public Object getAdapter(Class required) {
        if (fProjectionSupport != null) {
            Object adapter = fProjectionSupport.getAdapter(getSourceViewer(),
                    required);
            if (adapter != null)
                return adapter;
        }

        return super.getAdapter(required);
    }

    
    /**
     * Sets the given message as error message to this editor's status line.
     * 
     * @param msg message to be set
     */
    public void setStatusLineErrorMessage(String msg) {
        IEditorStatusLine statusLine= (IEditorStatusLine) 
            getAdapter(IEditorStatusLine.class);
        if (statusLine != null)
            statusLine.setMessage(true, msg, null);    
    }
    
}
