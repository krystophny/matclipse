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

package org.eclipselabs.matclipse.meditor.dialogs;


import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * This class an implementation of the SelectionListener interface. It provides functionality 
 * for listening to a "Browse..." button. If the button is pressed, a dialog for selecting a file
 * or directory is opened and the path to the selected file/directory will be written in a 
 * specified textfield. 
 * @author Georg Huhs
 */
public class FileDialogListener implements SelectionListener {
    
    private Text textfield;
    private boolean isDir;
    private Shell shell;
    private String [] filterExtensions;
    private String [] filterNames;
    
    
    /**
     * Only constructor. 
     * @param textfield Textfield to write the selected File path into. 
     * @param isDir Specifies if a directory or a file shall be searched for. 
     * @param shell Parent shell for the dialog
     */
    public FileDialogListener(Text textfield, boolean isDir, Shell shell){
        this.textfield = textfield;
        this.isDir     = isDir;
        this.shell     = shell;
        this.filterExtensions = new String[] {"*"};
        this.filterNames      = new String[] {"All Files"};
    }

    
    /**
     * Implementation: do nothing
     * @see org.eclipse.swt.events.SelectionListener#
     * widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetDefaultSelected(SelectionEvent e) {
    }
    
    
    /**
     * Sets the filter extensions and names for the file dialog.
     * @param extensions Extensions to use.
     * @param names Names to use. 
     */
    public void setFilter(String [] extensions, String [] names){
        this.filterExtensions = extensions;
        this.filterNames      = names;
    }

    
    /**
     * @see org.eclipse.swt.events.SelectionListener#
     * widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    public void widgetSelected(SelectionEvent e) {
        String chosenFile = null;
        
        if (this.isDir){
            DirectoryDialog dialog = new DirectoryDialog(this.shell);
            dialog.setFilterPath(this.textfield.getText());
            chosenFile = dialog.open();
        } else {
            FileDialog dialog = new FileDialog(this.shell);
            dialog.setFilterPath(this.textfield.getText());
            dialog.setFilterExtensions(this.filterExtensions);
            dialog.setFilterNames(this.filterNames);
            chosenFile = dialog.open();
        }
        
        if (chosenFile!=null){
            this.textfield.setText(chosenFile);
        }
    }

}
