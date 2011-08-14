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
 *     2007-03-11
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.editors;


import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.browser.WebBrowserEditor;


public class PdfEditor extends WebBrowserEditor implements  ISelectionListener {

    public PdfEditor() {
        super();
    }

    
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        getSite().getPage().addSelectionListener((ISelectionListener) this);
    }

    
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        
    }
}
