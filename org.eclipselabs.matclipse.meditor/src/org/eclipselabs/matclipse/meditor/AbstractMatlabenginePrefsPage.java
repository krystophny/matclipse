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
 *     2008-01-22 
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;

import org.eclipselabs.matclipse.meditor.dialogs.FileDialogListener;


public abstract class AbstractMatlabenginePrefsPage extends PreferencePage 
        implements IWorkbenchPreferencePage {
    
    protected OverlayPreferenceStore fOverlayStore;
    
    private Map<Button, String> fCheckBoxes= new HashMap<Button, String>();
    
    private SelectionListener fCheckBoxListener= new SelectionListener() {
        public void widgetDefaultSelected(SelectionEvent e) {
        }
        public void widgetSelected(SelectionEvent e) {
            Button button= (Button) e.widget;
            fOverlayStore.setValue((String) fCheckBoxes.get(button), button.getSelection());
        }
    };
    
    private Map<Text, String> fTextFields= new HashMap<Text, String>();
    private ModifyListener fTextFieldListener= new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            Text text= (Text) e.widget;
            fOverlayStore.setValue((String) fTextFields.get(text), text.getText());
        }
    };

    private Map<Combo, String> fComboFields= new HashMap<Combo, String>();
    private ModifyListener fComboFieldListener= new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            Combo combo = (Combo) e.widget;
            fOverlayStore.setValue((String) fComboFields.get(combo), combo.getText());
        }
    };

    private ArrayList<Text> fNumberFields= new ArrayList<Text>();
    private ModifyListener fNumberFieldListener= new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            numberFieldChanged((Text) e.widget);
        }
    };
        
    /**
     * Tells whether the fields are initialized.
     * @since 3.0
     */
    private boolean fFieldsInitialized= false;
    
    /**
     * List of master/slave listeners when there's a dependency.
     * 
     * @see #createDependency(Button, String, Control)
     */
    private ArrayList<SelectionListener> fMasterSlaveListeners = 
        new ArrayList<SelectionListener>();


    public AbstractMatlabenginePrefsPage(String description){
        setDescription(description); 
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        
        fOverlayStore= createOverlayStore();
    }

    
    protected abstract Control createAppearancePage(Composite parent);
    
    protected abstract OverlayPreferenceStore createOverlayStore();

    
    /*
     * @see IWorkbenchPreferencePage#init()
     */    
    public void init(IWorkbench workbench) {
    }

    
    /*
     * @see PreferencePage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE);
    }
    
    
    /*
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        
        fOverlayStore.load();
        fOverlayStore.start();
        
        Control control= createAppearancePage(parent);

        initialize();
        Dialog.applyDialogFont(control);
        return control;
    }
    
    
    protected void initialize() {
        initializeFields();
    }
    
    
    private void initializeFields() {
        
        Iterator<Button> checkBoxIterator = fCheckBoxes.keySet().iterator();
        while (checkBoxIterator.hasNext()) {
            Button checkBox = checkBoxIterator.next();
            String key = fCheckBoxes.get(checkBox);
            checkBox.setSelection(fOverlayStore.getBoolean(key));
        }
        
        Iterator<Text> textFieldIterator = fTextFields.keySet().iterator();
        while (textFieldIterator.hasNext()) {
            Text textField = textFieldIterator.next();
            String key = fTextFields.get(textField);
            textField.setText(fOverlayStore.getString(key));
        }
        
        Iterator<Combo> comboBoxIterator= fComboFields.keySet().iterator();
        while (comboBoxIterator.hasNext()) {
            Combo comboBox = comboBoxIterator.next();
            String key = fComboFields.get(comboBox);
            comboBox.setText(fOverlayStore.getString(key));
        }

        fFieldsInitialized= true;
        updateStatus(validatePositiveNumber("0")); 
        
        // Update slaves
        Iterator<SelectionListener> selectionListenerIterator = 
            fMasterSlaveListeners.iterator();
        while (selectionListenerIterator.hasNext()) {
            SelectionListener listener = selectionListenerIterator.next();
            listener.widgetSelected(null);
        }
    }
    
    
    /*
     * @see PreferencePage#performOk()
     */
    public boolean performOk() {
        fOverlayStore.propagate();
        Activator.getDefault().savePluginPreferences();
        return true;
    }
    
    
    /*
     * @see PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        fOverlayStore.loadDefaults();
        initializeFields();
        super.performDefaults();
    }
    
    
    /*
     * @see DialogPage#dispose()
     */
    public void dispose() {
        
        if (fOverlayStore != null) {
            fOverlayStore.stop();
            fOverlayStore= null;
        }
        
        super.dispose();
    }
    
    
    /**
     * Creates a checkbox and adds it to the parent. 
     * @param parent Composite to add the checkbox into.
     * @param label Label for this textfield. 
     * @param key Key of the corresponding preference.
     * @param indentation Horizontal indentation of the checkbox. 
     * @return The created checkbox 
     */
    protected Button addCheckBox(Composite parent, String label, String key, int indentation) {        
        Button checkBox= new Button(parent, SWT.CHECK);
        checkBox.setText(label);
        
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= indentation;
        gd.horizontalSpan= 2;
        checkBox.setLayoutData(gd);
        checkBox.addSelectionListener(fCheckBoxListener);
        
        fCheckBoxes.put(checkBox, key);
        
        return checkBox;
    }
    
    
    /**
     * Creates a labeled textfield and adds it to the parent. (Label on the left site.) 
     * @param parent Composite to add the textfield into.
     * @param label Label for this textfield. 
     * @param key Key of the corresponding preference.
     * @param textLimit Maximum number of characters in the textfield. 
     * @param indentation Horizontal indentation of the label. 
     * @param isNumber Specifies if this is a textfield for numbers. 
     * @return the created textfield. 
     */
    protected Control addTextField(Composite parent, String label, String key, 
            int textLimit, int indentation, boolean isNumber) {
        
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
            gd.widthHint= convertWidthInCharsToPixels(textLimit + 1);
        }
        textControl.setLayoutData(gd);
        textControl.setTextLimit(textLimit);
        fTextFields.put(textControl, key);
        if (isNumber) {
            fNumberFields.add(textControl);
            textControl.addModifyListener(fNumberFieldListener);
        } else {
            textControl.addModifyListener(fTextFieldListener);
        }
            
        return textControl;
    }
    
    
    /**
     * Creates and adds three Elements to the parent: A label, a textfield 
     * and a "Browse..." button that opens a FileDialog where a file or directory, 
     * whose path will be written into the textfield, can be specified. 
     * The last two elements are encapsulated by a Composite.
     * @param parent Composite to add the elements into.
     * @param label Label of this line. 
     * @param key Key of the corresponding preference.
     * @param indentation Horizontal indent of the label.
     * @param isDir specifies if a directory is searched. 
     * @param filterExtensions See org.eclipse.swt.widgets.FileDialog.setFilterExtensions(...)
     * @param filterNames See org.eclipse.swt.widgets.FileDialog.setFilterNames(...)
     * @return the created textfield.
     */
    protected Control addFileLine(Composite parent, String label, String key, 
            int indentation, boolean isDir, String[] filterExtensions, String[] filterNames) {

        Label labelControl= new Label(parent, SWT.NONE);
        labelControl.setText(label);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalIndent= indentation;
        labelControl.setLayoutData(gd);

        Composite rightContainer = parent;
        if (((GridLayout)parent.getLayout()).numColumns == 2){
            rightContainer = new Composite(parent, SWT.NONE);
            GridLayout layoutRContainer= new GridLayout(); 
            layoutRContainer.numColumns= 2;
            rightContainer.setLayout(layoutRContainer);
            gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
            rightContainer.setLayoutData(gd);
        }
        return addFileLine(rightContainer, key, 0, isDir, filterExtensions, filterNames);
    }
    
    
    /**
     * Creates and adds two Elements to the parent: A textfield and a "Browse..." button 
     * that opens a FileDialog where a file or directory, whose path will be written
     * into the textfield, can be specified. 
     * @param parent Composite to add the elements into.
     * @param key Key of the corresponding preference.
     * @param indentation Horizontal indent of the textfield.
     * @param isDir specifies if a directory is searched. 
     * @param filterExtensions See org.eclipse.swt.widgets.FileDialog.setFilterExtensions(...)
     * @param filterNames See org.eclipse.swt.widgets.FileDialog.setFilterNames(...)
     * @return the created textfield.
     */
    protected Control addFileLine(Composite parent, String key, 
            int indentation, boolean isDir, String[] filterExtensions, String[] filterNames) {
        
        Text textControl= new Text(parent, SWT.BORDER | SWT.SINGLE);        
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalIndent = indentation;
        textControl.setLayoutData(gd);
        textControl.setTextLimit(Text.LIMIT);
        fTextFields.put(textControl, key);
        textControl.addModifyListener(fTextFieldListener);
            
        Button browseButton = new Button(parent, SWT.PUSH);
        browseButton.setText("Browse...");
        FileDialogListener listener = 
            new FileDialogListener(textControl, isDir, getShell());
        if (filterExtensions != null && filterNames != null){
            listener.setFilter(filterExtensions, filterNames);
        }
        browseButton.addSelectionListener(listener);
        
        return textControl;
    }

    
    /**
     * Creates a Combo box and adds it to the parent Composite.
     * @param parent Composite to add this Combo box into
     * @param label The Combo's label. 
     * @param key Key of the corresponding preference
     * @param entries Array of Strings which represent the entries of the Combo.
     * @param indentation Horizontal indent of the Combo
     * @return the created Combo. 
     */
    protected Control addComboField(Composite parent, String label, String key, 
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
        gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        comboControl.setLayoutData(gd);
        
        fComboFields.put(comboControl, key);
        comboControl.addModifyListener(fComboFieldListener);
            
        return comboControl;
    }
    
    
    /**
     * Adds a group that can be used to add elements with addXYZ(...) into. 
     * @param parent Composite to add this Group into
     * @param title Title of this Group, may be null if no title should be displayed
     * @return generated Group
     */
    protected Group addGroup(Composite parent, String title, int verticalIndent, 
            int hSpan, int numInternalCols) {
        Group group = new Group(parent, SWT.NONE);
        if (title != null){
            group.setText(title);
        }
        
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = hSpan;
        gd.verticalIndent = verticalIndent;
        group.setLayoutData(gd);
        
        GridLayout layoutGroup= new GridLayout(); 
        layoutGroup.numColumns= numInternalCols;
        group.setLayout(layoutGroup);
        
        return group;
    }

    
    /**
     * Creates an exclusive radio button field and adds it to the parent Composite. 
     * All components of this field are enclosed by a Container. 
     * @param parent Composite to add the whole field into. 
     * @param title A title for this field which occures as a Label, may be null if no title 
     *        should be used
     * @param buttonInfo String matrix which contains the information for all radio buttons. 
     *        One button is described by one line which contains in it's first row the button's text
     *        and in the second row the corresponding preference key 
     * @param verticalIndent Vertical indent for the container
     * @param hSpan Horizontal span for the container
     * @param radioButtonIndent Indent of the buttons with respect to the field's container.
     * @return the Container that keeps all generated elements. 
     */
    protected Composite addRadioButtonField(Composite parent, String title, String[][] buttonInfo, 
            int verticalIndent, int hSpan, int radioButtonIndent) {
        
        Composite mainContainer = new Composite(parent, SWT.NULL);
        GridLayout mainLayout = new GridLayout();
        mainLayout.numColumns = 1;
        mainContainer.setLayout(mainLayout);

        if (title != null){
            Label label= new Label(mainContainer, SWT.NONE);
            label.setText(title);
        }

        Composite buttonContainer = new Composite(mainContainer, SWT.NULL);
        RowLayout buttonLayout = new RowLayout();
        buttonLayout.type = SWT.VERTICAL;
        buttonContainer.setLayout(buttonLayout);

        for (int i=0; i<buttonInfo.length; i++) {
            String buttonText = buttonInfo[i][0];
            String buttonKey = buttonInfo[i][1];
            Button button = new Button(buttonContainer, SWT.RADIO);
            button.setText(buttonText);
            fCheckBoxes.put(button, buttonKey);
            button.addSelectionListener(fCheckBoxListener);
        }

        GridData mainGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        mainGd.horizontalSpan = hSpan;
        mainGd.verticalIndent = verticalIndent;
        mainContainer.setLayoutData(mainGd);
        
        GridData buttonGd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        buttonGd.horizontalIndent = radioButtonIndent;
        buttonContainer.setLayoutData(buttonGd);

        return mainContainer;
    }
    
    
    /** 
     * Creates a a Label and adds it to the parent Composite.
     * @param parent Composite to add the label into.
     * @param text The Label's text. 
     * @param verticalIndent Vertical indent of the Label. 
     * @param hSpan Horizontal span of the Label. 
     * @return the created Label
     */
    protected Label addLabel(Composite parent, String text, int verticalIndent, int hSpan){
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.horizontalSpan = hSpan;
        gd.verticalIndent = verticalIndent;
        label.setLayoutData(gd);
        return label;
    }

    /**
     * Creates a dependency between a master and a slave element. 
     * This means that the slave will be disabled until the master gets selected. 
     * If inverse behavior is chosen, the slave is enabled if the master is not selected.
     * @param master A Button that controls the slave. 
     * @param masterKey The master's corresponding property key. 
     * @param slave A Control that will be enabled/disabled depending on the master's state. 
     * @param isInverse Defines the character of the relationship. 
     *        if false: slave is enabled if master is selected
     *        if true:  slave is enabled if master is not selected
     */
    protected void createDependency(final Button master, String masterKey, 
            final Control slave, final boolean isInverse) {
        
        boolean masterState= fOverlayStore.getBoolean(masterKey);
        slave.setEnabled(masterState^isInverse);
        
        SelectionListener listener= new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                slave.setEnabled(master.getSelection()^isInverse);
            }

            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        master.addSelectionListener(listener);
        fMasterSlaveListeners.add(listener);
    }
    
    /**
     * Creates a dependency between several masters and a slave element. 
     * This means that the slave will be disabled until all masters' 
     * selection states equal a defined set of states. Inverse behavior is selectable 
     * for the slave and for each single master, thus any relationship can be defined. 
     * If all invert-flags are false, the slave will be enabled only if all masters 
     * are selected. If only the global behavior is inverted, the slave will be 
     * disabled only if all masters are selected. If all masters' behavior is inverted, 
     * the slave will be enabled only if all masters are not selected. And so on.  
     * @param masters Array of MasterInfo. Each entry contains a button, 
     *        its preference key and a flag for inverted use of this master. 
     * @param slave A Control that will be enabled/disabled depending of the masters' states.
     * @param invertAll Global invert flag.
     */
    protected void createMultiDependency(final MasterInfo[] masters, 
            final Control slave, final boolean invertAll) {
        
        boolean mastersState = true;
        for (MasterInfo master:masters){
            mastersState = mastersState && fOverlayStore.getBoolean(master.key)^master.inverse;
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
        fMasterSlaveListeners.add(listener);
    }

    /**
     * Indents the specified Control by a certain amount. 
     * @param control Control to indent.
     */
    protected static void indent(Control control) {
        GridData gridData;
        gridData = (GridData)control.getLayoutData();
        if (gridData == null){
            gridData= new GridData();
        }
        gridData.horizontalIndent= 20;
        control.setLayoutData(gridData);        
    }
    
    
    private void numberFieldChanged(Text textControl) {
        String number= textControl.getText();
        IStatus status= validatePositiveNumber(number);
        if (!status.matches(IStatus.ERROR))
            fOverlayStore.setValue((String) fTextFields.get(textControl), number);
        updateStatus(status);
    }
    
    
    private IStatus validatePositiveNumber(String number) {
        StatusInfo status= new StatusInfo();
        if (number.length() == 0) {
            status.setError("empty_input??"); 
        } else {
            try {
                int value= Integer.parseInt(number);
                if (value < 0)
                    status.setError("invalid_input??"); 
            } catch (NumberFormatException e) {
                status.setError("invalid_input??"); 
            }
        }
        return status;
    }
    
    
    private void updateStatus(IStatus status) {
        if (!fFieldsInitialized)
            return;
        
        if (!status.matches(IStatus.ERROR)) {
            for (int i= 0; i < fNumberFields.size(); i++) {
                Text text= (Text) fNumberFields.get(i);
                IStatus s= validatePositiveNumber(text.getText());
                status= s.getSeverity() > status.getSeverity() ? s : status;
            }
        }    
        setValid(!status.matches(IStatus.ERROR));
        applyToStatusLine(this, status);
    }

    
    /**
     * Applies the status to the status line of a dialog page.
     * @param page the dialog page
     * @param status the status
     */
    public void applyToStatusLine(DialogPage page, IStatus status) {
        String message= status.getMessage();
        switch (status.getSeverity()) {
            case IStatus.OK:
                page.setMessage(message, IMessageProvider.NONE);
                page.setErrorMessage(null);
                break;
            case IStatus.WARNING:
                page.setMessage(message, IMessageProvider.WARNING);
                page.setErrorMessage(null);
                break;                
            case IStatus.INFO:
                page.setMessage(message, IMessageProvider.INFORMATION);
                page.setErrorMessage(null);
                break;            
            default:
                if (message.length() == 0) {
                    message= null;
                }
                page.setMessage(null);
                page.setErrorMessage(message);
                break;        
        }
    }
    
    
    /**
     * Sets default preference values
     */
    protected static void initializeDefaultPreferences(Preferences prefs) {
    }
    
    
    static public Preferences getPreferences() {
        return     Activator.getDefault().getPluginPreferences();
    }

    
    protected class MasterInfo {
        public Button  button;
        public String  key;
        public boolean inverse;
        
        public MasterInfo(Button button, String key, boolean inverse){
            this.button  = button;
            this.key     = key;
            this.inverse = inverse;
        }
    }
}
