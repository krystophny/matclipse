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

package org.eclipselabs.matclipse.meditor.dialogs;


import java.util.Properties;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.MatlabengineExportPrefsPage;


/**
 * Dialog used for file exports. The user can specify file specific data and export options 
 * that may change from one export to the next. 
 * @author Georg Huhs
 */
public class ExportDialog extends Dialog {

    /* Keys for properties */
    private final static String KEY_WRITE_DATA   = MatlabengineExportPrefsPage.WRITE_FILE_METADATA;
    private final static String KEY_TITLE        = MatlabengineExportPrefsPage.TITLE;
    private final static String KEY_DATE         = MatlabengineExportPrefsPage.DATE;
    private final static String KEY_CONTENT      = MatlabengineExportPrefsPage.CONTENT;
    private final static String KEY_TYPE         = MatlabengineExportPrefsPage.TYPE;
    private final static String KEY_OUT_XML      = MatlabengineExportPrefsPage.OUTPUT_XML;
    private final static String KEY_OUT_HTML     = MatlabengineExportPrefsPage.OUTPUT_HTML;
    private final static String KEY_OUT_LATEX    = MatlabengineExportPrefsPage.OUTPUT_LATEX;
    private final static String KEY_OUT_PDF      = MatlabengineExportPrefsPage.OUTPUT_PDF;
    private final static String KEY_COREONLY     = MatlabengineExportPrefsPage.OUTPUT_COREONLY;
    private final static String KEY_SHOWOUTPUT   = MatlabengineExportPrefsPage.SHOW_OUTPUT;
    private final static String KEY_OUT_DIR      = MatlabengineExportPrefsPage.OUTPUT_DIRECTORY;
    private final static String KEY_USE_INPUTDIR = MatlabengineExportPrefsPage.OUT_USE_INPUTDIR;
    private final static String KEY_SHOW_DLG     = MatlabengineExportPrefsPage.SHOW_DIALOG;
    
    private final static int INDENT_H = 20;
    private final static int INDENT_V = 20;
    
    private IPreferenceStore preferences;
    private Properties exportProperties, loadedProperties;
    
    private Button checkboxWriteData; 
    private Text textTitle, textDate, textContent; 
    private Combo comboType;
    private Button checkboxOutputXML, checkboxOutputHTML, 
            checkboxOutputLatex, checkboxOutputPDF;
    private Button checkboxShowOutput;
    private Button checkboxCoreonly;
    private Text textOutputDir;
    private Button buttonOutputUseInputDir;
    private Button checkboxShowDialog;
    
    
    public ExportDialog(Shell parentShell) {
        super(parentShell);
        this.preferences = Activator.getDefault().getPreferenceStore();
        if (this.preferences == null){
            this.preferences = new PreferenceStore();
        }
        this.exportProperties = new Properties();
        this.loadedProperties = null;
    }
    
    
    public void setExportProperties(Properties properties){
        this.loadedProperties = properties;
    }
        

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText("File specific export options");
        Composite mainContainer = new Composite(parent, SWT.BORDER);
        
        GridLayout layout= new GridLayout(); 
        layout.numColumns= 2;
        mainContainer.setLayout(layout);

        this.checkboxWriteData = 
            addCheckBox(mainContainer, "Write file specific metadata", 0);
        
        this.textTitle   = addTextField(mainContainer, "Title:", Text.LIMIT, 0, 400);
        this.textDate    = addTextField(mainContainer, "Date:", Text.LIMIT, 0);
        this.textContent = addTextField(mainContainer, "Content:", Text.LIMIT, 0);
        
        this.comboType = 
            addComboField(mainContainer, "Type:", MatlabengineExportPrefsPage.TYPES, 0);

        addLabel(mainContainer, "Output Formats", INDENT_V, 2);
        this.checkboxOutputXML   = addCheckBox(mainContainer, "XML",   INDENT_H);
        this.checkboxOutputHTML  = addCheckBox(mainContainer, "HTML",  INDENT_H);
        this.checkboxOutputLatex = addCheckBox(mainContainer, "LaTeX", INDENT_H);
        this.checkboxCoreonly    = 
            addCheckBox(mainContainer, "export LaTeX core only", 2*INDENT_H);
        this.checkboxOutputPDF   = addCheckBox(mainContainer, "PDF",   INDENT_H);
        
        this.checkboxShowOutput  = 
            addCheckBox(mainContainer, "Show generated files after export", 0);
        
        addLabel(mainContainer, "Output directory", INDENT_V, 2);
        this.buttonOutputUseInputDir = 
            addCheckBox(mainContainer, "Use the Matlab file's directory", INDENT_H);
        Composite outputDirContainer = new Composite(mainContainer, SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.LEFT, true, false);
        gd.horizontalSpan = 2;
        outputDirContainer.setLayoutData(gd);
        GridLayout outputDirLayout= new GridLayout(); 
        outputDirLayout.numColumns= 2;
        outputDirContainer.setLayout(outputDirLayout);
        this.textOutputDir = addFileLine(outputDirContainer, INDENT_H);

        this.checkboxShowDialog = 
            addCheckBox(mainContainer, "Show this dialog before each export", 0);
        ((GridData)this.checkboxShowDialog.getLayoutData()).verticalIndent = INDENT_V;

        if (this.loadedProperties != null){
            setPropertiesSelection(this.checkboxWriteData,       KEY_WRITE_DATA);
            setPropertiesText(     this.textTitle,               KEY_TITLE);
            setPropertiesText(     this.textDate,                KEY_DATE);
            setPropertiesText(     this.textContent,             KEY_CONTENT);
            setPropertiesSelection(this.comboType,               KEY_TYPE);
            setPropertiesSelection(this.checkboxOutputXML,       KEY_OUT_XML);
            setPropertiesSelection(this.checkboxOutputHTML,      KEY_OUT_HTML);
            setPropertiesSelection(this.checkboxOutputLatex,     KEY_OUT_LATEX);
            setPropertiesSelection(this.checkboxOutputPDF,       KEY_OUT_PDF);
            setPropertiesSelection(this.checkboxCoreonly,        KEY_COREONLY);
            setPropertiesSelection(this.checkboxShowOutput,      KEY_SHOWOUTPUT);
            setPropertiesText(     this.textOutputDir,           KEY_OUT_DIR);
            setPropertiesSelection(this.buttonOutputUseInputDir, KEY_USE_INPUTDIR);
            setPropertiesSelection(this.checkboxShowDialog,      KEY_SHOW_DLG);
        } else {
            setPreferenceSelection(this.checkboxWriteData,       KEY_WRITE_DATA);
            setPreferenceText(     this.textTitle,               KEY_TITLE);
            setPreferenceText(     this.textDate,                KEY_DATE);
            setPreferenceText(     this.textContent,             KEY_CONTENT);
            setPreferenceSelection(this.comboType,               KEY_TYPE);
            setPreferenceSelection(this.checkboxOutputXML,       KEY_OUT_XML);
            setPreferenceSelection(this.checkboxOutputHTML,      KEY_OUT_HTML);
            setPreferenceSelection(this.checkboxOutputLatex,     KEY_OUT_LATEX);
            setPreferenceSelection(this.checkboxOutputPDF,       KEY_OUT_PDF);
            setPreferenceSelection(this.checkboxCoreonly,        KEY_COREONLY);
            setPreferenceSelection(this.checkboxShowOutput,      KEY_SHOWOUTPUT);
            setPreferenceText(     this.textOutputDir,           KEY_OUT_DIR);
            setPreferenceSelection(this.buttonOutputUseInputDir, KEY_USE_INPUTDIR);
            setPreferenceSelection(this.checkboxShowDialog,      KEY_SHOW_DLG);
        }

        createDependency(this.checkboxWriteData, this.textTitle,   false);
        createDependency(this.checkboxWriteData, this.textDate,    false);
        createDependency(this.checkboxWriteData, this.textContent, false);
        createDependency(this.checkboxWriteData, this.comboType,   false);
        
        createDependency(this.checkboxOutputLatex, this.checkboxCoreonly, false);
        createMultiDependency(
                new MasterInfo[]{
                        new MasterInfo(this.checkboxOutputLatex, false), 
                        new MasterInfo(this.checkboxCoreonly, false) }, 
                this.checkboxOutputPDF, true);

        createDependency(this.buttonOutputUseInputDir, this.textOutputDir, true);
        return mainContainer;
    }

    
    /**
     * Saves all data to the preferences. Does not start an export!
     */
    @Override
    protected void okPressed() {
        boolean bValue = this.checkboxWriteData.getSelection();
        this.preferences.setValue(KEY_WRITE_DATA, bValue);
        this.exportProperties.setProperty(KEY_WRITE_DATA, Boolean.toString(bValue));
        String value = this.textTitle.getText();
        this.preferences.setValue(KEY_TITLE, value);
        this.exportProperties.setProperty(KEY_TITLE, value);
        value = this.textDate.getText();
        this.preferences.setValue(KEY_DATE, value);
        this.exportProperties.setProperty(KEY_DATE, value);
        value = this.textContent.getText();
        this.preferences.setValue(KEY_CONTENT, value);
        this.exportProperties.setProperty(KEY_CONTENT, value);
        value = this.comboType.getItem(this.comboType.getSelectionIndex());
        this.preferences.setValue(KEY_TYPE, value);
        this.exportProperties.setProperty(KEY_TYPE, value);
        bValue = this.checkboxOutputXML.getSelection();
        this.preferences.setValue(KEY_OUT_XML, bValue);
        this.exportProperties.setProperty(KEY_OUT_XML, Boolean.toString(bValue));
        bValue = this.checkboxOutputHTML.getSelection();
        this.preferences.setValue(KEY_OUT_HTML, bValue);
        this.exportProperties.setProperty(KEY_OUT_HTML, Boolean.toString(bValue));
        bValue = this.checkboxOutputLatex.getSelection();
        this.preferences.setValue(KEY_OUT_LATEX, bValue);
        this.exportProperties.setProperty(KEY_OUT_LATEX, Boolean.toString(bValue));
        bValue = this.checkboxCoreonly.getSelection();
        this.preferences.setValue(KEY_COREONLY, bValue);
        this.exportProperties.setProperty(KEY_COREONLY, Boolean.toString(bValue));
        bValue = this.checkboxOutputPDF.getSelection();
        this.preferences.setValue(KEY_OUT_PDF, bValue);
        this.exportProperties.setProperty(KEY_OUT_PDF, Boolean.toString(bValue));
        bValue = this.checkboxShowOutput.getSelection();
        this.preferences.setValue(KEY_SHOWOUTPUT, bValue);
        this.exportProperties.setProperty(KEY_SHOWOUTPUT, Boolean.toString(bValue));
        bValue = this.buttonOutputUseInputDir.getSelection();
        this.preferences.setValue(KEY_USE_INPUTDIR, bValue);
        this.exportProperties.setProperty(KEY_USE_INPUTDIR, Boolean.toString(bValue));
        value = this.textOutputDir.getText();
        this.preferences.setValue(KEY_OUT_DIR, value);
        this.exportProperties.setProperty(KEY_OUT_DIR, value);
        bValue = this.checkboxShowDialog.getSelection();
        this.preferences.setValue(KEY_SHOW_DLG, bValue);
        this.exportProperties.setProperty(KEY_SHOW_DLG, Boolean.toString(bValue));
        super.okPressed();
    }
    
    
    public Properties getExportProperties(){
        return this.exportProperties;
    }
    
    
    protected void setPreferenceText(Text textEdit, String key){
        String preferenceText = this.preferences.getString(key);
        textEdit.setText(preferenceText);
    }

    
    protected void setPropertiesText(Text textEdit, String key){
        String preferenceText = this.loadedProperties.getProperty(key, 
                this.preferences.getString(key));
        textEdit.setText(preferenceText);
    }

    
    protected void setPreferenceSelection(Combo combo, String key){
        String preferenceText = this.preferences.getString(key);
        String [] comboItems = combo.getItems();
        for (int itemIndex = 0; itemIndex<comboItems.length; itemIndex++){
            if (preferenceText.equals(comboItems[itemIndex])){
                combo.select(itemIndex);
            }
        }
    }

    
    protected void setPropertiesSelection(Combo combo, String key){
        String preferenceText = this.loadedProperties.getProperty(key, 
                this.preferences.getString(key));
        String [] comboItems = combo.getItems();
        for (int itemIndex = 0; itemIndex<comboItems.length; itemIndex++){
            if (preferenceText.equals(comboItems[itemIndex])){
                combo.select(itemIndex);
            }
        }
    }

    
    protected void setPreferenceSelection(Button button, String key){
        boolean selected = this.preferences.getBoolean(key);
        button.setSelection(selected);
    }

    
    protected void setPropertiesSelection(Button button, String key){
        String property = this.loadedProperties.getProperty(key);
        boolean selected;
        if (property != null){
          selected = Boolean.parseBoolean(property); 
        } else {
            selected = this.preferences.getBoolean(key);
        }
        button.setSelection(selected);
    }

    
    protected Button addCheckBox(Composite parent, String label, int indentation) {     
        Button checkBox= new Button(parent, SWT.CHECK|SWT.BORDER);
        checkBox.setText(label);
        
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= indentation;
        gd.horizontalSpan= 2;
        checkBox.setLayoutData(gd);

        return checkBox;
    }

    
    protected Text addTextField(Composite parent, String label, 
            int textLimit, int indentation) {
        
        return addTextField(parent, label, textLimit, indentation, SWT.DEFAULT);
    }

    
    protected Text addTextField(Composite parent, String label, 
            int textLimit, int indentation, int width) {

        Label labelControl= new Label(parent, SWT.NONE);
        labelControl.setText(label);
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= indentation;
        labelControl.setLayoutData(gd);
        
        Text textControl= new Text(parent, SWT.BORDER | SWT.SINGLE);        
        if (textLimit == Text.LIMIT){
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        } else {
            gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            gd.widthHint = convertWidthInCharsToPixels(textLimit + 1);
        }
        if (width != SWT.DEFAULT){
            gd.widthHint = width;
        }
        textControl.setLayoutData(gd);
        textControl.setTextLimit(textLimit);

        return textControl;
    }
    
    
    protected Combo addComboField(Composite parent, String label, 
            String[] entries, int indentation) {
        
        Label labelControl= new Label(parent, SWT.NONE);
        labelControl.setText(label);
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= indentation;
        labelControl.setLayoutData(gd);
        
        Combo comboControl = new Combo(parent, SWT.DROP_DOWN);
        for (String entry : entries) {
            comboControl.add(entry);
        }
        comboControl.select(0);
        
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        comboControl.setLayoutData(gd);
        
        return comboControl;
    }
    
    
    protected Label addLabel(Composite parent, String text, int verticalIndent, int hSpan){
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = hSpan;
        gd.verticalIndent = verticalIndent;
        label.setLayoutData(gd);
        return label;
    }

    
    protected Text addFileLine(Composite parent, int indentation) {
        Text textControl= new Text(parent, SWT.BORDER | SWT.SINGLE);        
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalIndent = indentation;
        textControl.setLayoutData(gd);
        textControl.setTextLimit(Text.LIMIT);
            
        Button browseButton = new Button(parent, SWT.PUSH);
        browseButton.setText("Browse...");
        FileDialogListener listener = 
            new FileDialogListener(textControl, true, getShell());
        browseButton.addSelectionListener(listener);
        
        return textControl;
    }

    
    protected void createDependency(final Button master, 
            final Control slave, final boolean isInverse) {
        
        boolean masterState= master.getSelection();
        slave.setEnabled(masterState^isInverse);
        
        SelectionListener listener= new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                slave.setEnabled(master.getSelection()^isInverse);
            }

            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        master.addSelectionListener(listener);
    }
    
    
    protected void createMultiDependency(final MasterInfo[] masters, 
            final Control slave, final boolean invertAll) {
        
        boolean mastersState = true;
        for (MasterInfo master:masters){
            mastersState = mastersState && master.button.getSelection()^master.inverse;
        }
        slave.setEnabled(mastersState^invertAll);
        
        SelectionListener listener= new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                boolean mastersSelected = true;
                for (MasterInfo master:masters){
                    mastersSelected = mastersSelected && 
                        master.button.getSelection()^master.inverse;
                }
                slave.setEnabled(mastersSelected^invertAll);
            }

            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        for (MasterInfo master:masters){
            master.button.addSelectionListener(listener);
        }
    }

    
    /** 
     * Helper class that keeps the information about a master 
     * needed by createMultiDependency. 
     * @author Georg Huhs
     */
    protected class MasterInfo {
        public Button  button;
        public boolean inverse;
        
        public MasterInfo(Button button, boolean inverse){
            this.button  = button;
            this.inverse = inverse;
        }
    }
   
}
