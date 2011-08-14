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
 *     2008-01-19
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor;


import java.util.ArrayList;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipselabs.matclipse.meditor.util.ColorEditor;


public class MatlabenginePrefsPage extends AbstractMatlabenginePrefsPage {
    
//     Preferences
    //To add a new preference it needs to be included in
    //createAppearancePage
    //createOverlayStore
    //initializeDefaultPreferences
    //declaration of fAppearanceColorListModel if it is a color
    //constants (here)
    
    //text
    public static final String TAB_WIDTH = "TAB_WIDTH";
    public static final int DEFAULT_TAB_WIDTH = 4;
    
    public static final String DEFAULT_EDITOR_PRINT_MARGIN_COLUMN = "80";
        
    //checkboxes
    public static final String SUBSTITUTE_TABS = "SUBSTITUTE_TABS";
    public static final boolean DEFAULT_SUBSTITUTE_TABS = true;
    
    public static final boolean DEFAULT_EDITOR_OVERVIEW_RULER = false;
    public static final boolean DEFAULT_EDITOR_LINE_NUMBER_RULER = true;
    private static final boolean DEFAULT_EDITOR_CURRENT_LINE = true;
    public static final boolean DEFAULT_EDITOR_PRINT_MARGIN = true;
    public static final boolean DEFAULT_EDITOR_USE_CUSTOM_CARETS = false;
    public static final boolean DEFAULT_EDITOR_WIDE_CARET = false;
    
    //matching
    public static final boolean DEFAULT_USE_MATCHING_BRACKETS = true;
    public static final String USE_MATCHING_BRACKETS = "USE_MATCHING_BRACKETS";
    public static final RGB DEFAULT_MATCHING_BRACKETS_COLOR = new RGB(64,128,128);
    public static final String MATCHING_BRACKETS_COLOR = "EDITOR_MATCHING_BRACKETS_COLOR";
    
    public static final String NUMBER_COLOR = "NUMBER_COLOR";
    private static final RGB DEFAULT_NUMBER_COLOR = new RGB(128, 0, 0);

    public static final String CODE_COLOR = "CODE_COLOR";
    private static final RGB DEFAULT_CODE_COLOR = new RGB(0, 0, 0);
    
    public static final String KEYWORD_COLOR = "KEYWORD_COLOR";
    private static final RGB DEFAULT_KEYWORD_COLOR = new RGB(0, 0, 255);
    
    public static final String FUNCTION_COLOR = "FUNCTION_COLOR";
    private static final RGB DEFAULT_FUNCTION_COLOR = new RGB(0, 200, 255);
    
    public static final String TOOLBOX_COLOR = "TOOLBOX_COLOR";
    private static final RGB DEFAULT_TOOLBOX_COLOR = new RGB(0, 200, 150);
    
    public static final String STRING_COLOR = "STRING_COLOR";
    private static final RGB DEFAULT_STRING_COLOR = new RGB(0, 170, 0);
    
    public static final String COMMENT_COLOR = "COMMENT_COLOR";
    private static final RGB DEFAULT_COMMENT_COLOR = new RGB(153, 153, 153);
    
    private static final RGB DEFAULT_PREFERENCE_COLOR_BACKGROUND = new RGB(255, 255, 255);    
    public static final boolean DEFAULT_EDITOR_BACKGROUND_COLOR_SYSTEM_DEFAULT = true;
    private static final RGB DEFAULT_EDITOR_CURRENT_LINE_COLOR = new RGB(244, 255, 255);    
    public static final RGB DEFAULT_EDITOR_LINE_NUMBER_RULER_COLOR = new RGB(0, 0, 0);
    public static final RGB DEFAULT_EDITOR_PRINT_MARGIN_COLOR = new RGB(192,192,192);
    
    //see initializeDefaultColors for selection defaults
    public static final boolean DEFAULT_EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR = true;
    public static final boolean DEFAULT_EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR = true;
    
    public static final String CONNECT_TIMEOUT = "CONNECT_TIMEOUT";
    public static final int DEFAULT_CONNECT_TIMEOUT = 20000;
    
    public static final String RUN_MANY_SCRIPT_LOCATION = "RUN_MANY_SCRIPT_LOCATION";
    public static final String DEFAULT_RUN_MANY_SCRIPT_LOCATION = "";
    
    private final String[][] fAppearanceColorListModel= new String[][] {
        {"Code", CODE_COLOR, null},
        {"Numbers", NUMBER_COLOR, null},
        {"Matching brackets", MATCHING_BRACKETS_COLOR, null},
        {"Keywords", KEYWORD_COLOR, null},
        {"Matlab-Functions", FUNCTION_COLOR, null},
        {"Toolbox-Functions", TOOLBOX_COLOR, null},
        {"Strings", STRING_COLOR, null},
        {"Comments", COMMENT_COLOR, null},
        {"Background", AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT},
        {"Current line highlight", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR, null},
        {"Line numbers", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR, null},         
        {"Print margin", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, null}, 
        {"Selection foreground", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR}, 
        {"Selection background", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR}, 
    };
        
    private List fAppearanceColorList;
    private ColorEditor fAppearanceColorEditor;
    private Button fAppearanceColorDefault;
    
    
    public MatlabenginePrefsPage() {
        super("Matlab Editor settings:");
    }
    
    
    protected OverlayPreferenceStore createOverlayStore() {
        
        ArrayList<OverlayPreferenceStore.OverlayKey> overlayKeys = 
            new ArrayList<OverlayPreferenceStore.OverlayKey>();
        
        //text
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, TAB_WIDTH));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
        
        //matching
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, USE_MATCHING_BRACKETS));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING,  MATCHING_BRACKETS_COLOR));
        
        //checkbox        
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, SUBSTITUTE_TABS));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WIDE_CARET));
        
        //colors
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CODE_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, NUMBER_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, KEYWORD_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, FUNCTION_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, TOOLBOX_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, STRING_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, COMMENT_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR));
        
        OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
        overlayKeys.toArray(keys);
        return new OverlayPreferenceStore(getPreferenceStore(), keys);
    }
    
    
    private void handleAppearanceColorListSelection() {    
        int i= fAppearanceColorList.getSelectionIndex();
        String key= fAppearanceColorListModel[i][1];
        RGB rgb= PreferenceConverter.getColor(fOverlayStore, key);
        fAppearanceColorEditor.setColorValue(rgb);        
        updateAppearanceColorWidgets(fAppearanceColorListModel[i][2]);
    }

    
    private void updateAppearanceColorWidgets(String systemDefaultKey) {
        if (systemDefaultKey == null) {
            fAppearanceColorDefault.setSelection(false);
            fAppearanceColorDefault.setVisible(false);
            fAppearanceColorEditor.getButton().setEnabled(true);
        } else {
            boolean systemDefault= fOverlayStore.getBoolean(systemDefaultKey);
            fAppearanceColorDefault.setSelection(systemDefault);
            fAppearanceColorDefault.setVisible(true);
            fAppearanceColorEditor.getButton().setEnabled(!systemDefault);
        }
    }
    
    
    protected Control createAppearancePage(Composite parent) {

        Composite appearanceComposite= new Composite(parent, SWT.NONE);
        GridLayout layout= new GridLayout(); 
        layout.numColumns= 2;
        appearanceComposite.setLayout(layout);

        addTextField(appearanceComposite, "Tab length:", TAB_WIDTH, 3, 0, true);
        addTextField(appearanceComposite, "Print margin column:", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 3, 0, true);
        addCheckBox(appearanceComposite, "Substitute spaces for tabs?", SUBSTITUTE_TABS, 0);
        addCheckBox(appearanceComposite, "Show overview ruler", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, 0);
        addCheckBox(appearanceComposite, "Show line numbers", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, 0);
        addCheckBox(appearanceComposite, "Highlight current line", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, 0);
        addCheckBox(appearanceComposite, "Show print margin", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, 0);

        Button master= addCheckBox(appearanceComposite, "Use custom caret", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS, 0);

        Button slave= addCheckBox(appearanceComposite, "Enable thick caret", AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WIDE_CARET, 0);
        createDependency(
                master, 
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS, 
                slave, 
                false);
        indent(slave);

        Label l= new Label(appearanceComposite, SWT.LEFT );
        GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan= 2;
        gd.heightHint= convertHeightInCharsToPixels(1) / 2;
        l.setLayoutData(gd);
        
        l= new Label(appearanceComposite, SWT.LEFT);
        l.setText("Appearance color options:"); 
        gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan= 2;
        l.setLayoutData(gd);

        Composite editorComposite= new Composite(appearanceComposite, SWT.NONE);
        layout= new GridLayout();
        layout.numColumns= 2;
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        editorComposite.setLayout(layout);
        gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
        gd.horizontalSpan= 2;
        editorComposite.setLayoutData(gd);        

        fAppearanceColorList= new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
        gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.heightHint= convertHeightInCharsToPixels(5);
        fAppearanceColorList.setLayoutData(gd);
                        
        Composite stylesComposite= new Composite(editorComposite, SWT.NONE);
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        layout.numColumns= 2;
        stylesComposite.setLayout(layout);
        stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        l= new Label(stylesComposite, SWT.LEFT);
        l.setText("Color:"); 
        gd= new GridData();
        gd.horizontalAlignment= GridData.BEGINNING;
        l.setLayoutData(gd);

        fAppearanceColorEditor= new ColorEditor(stylesComposite);
        Button foregroundColorButton= fAppearanceColorEditor.getButton();
        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment= GridData.BEGINNING;
        foregroundColorButton.setLayoutData(gd);

        SelectionListener colorDefaultSelectionListener= new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                boolean systemDefault= fAppearanceColorDefault.getSelection();
                fAppearanceColorEditor.getButton().setEnabled(!systemDefault);
                
                int i= fAppearanceColorList.getSelectionIndex();
                String key= fAppearanceColorListModel[i][2];
                if (key != null)
                    fOverlayStore.setValue(key, systemDefault);
            }
            public void widgetDefaultSelected(SelectionEvent e) {}
        };
        
        fAppearanceColorDefault= new Button(stylesComposite, SWT.CHECK);
        fAppearanceColorDefault.setText("System default"); 
        gd= new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment= GridData.BEGINNING;
        gd.horizontalSpan= 2;
        fAppearanceColorDefault.setLayoutData(gd);
        fAppearanceColorDefault.setVisible(false);
        fAppearanceColorDefault.addSelectionListener(colorDefaultSelectionListener);
        
        fAppearanceColorList.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                handleAppearanceColorListSelection();
            }
        });
        foregroundColorButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                int i= fAppearanceColorList.getSelectionIndex();
                String key= fAppearanceColorListModel[i][1];
                
                PreferenceConverter.setValue(fOverlayStore, key, fAppearanceColorEditor.getColorValue());
            }
        });
        
        return appearanceComposite;
    }
    
    
    /*
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        initializeDefaultColors();
        return super.createContents(parent);
    }
    
    
    protected void initialize() {
        super.initialize();

        for (int i= 0; i < fAppearanceColorListModel.length; i++)
            fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
        fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (fAppearanceColorList != null && !fAppearanceColorList.isDisposed()) {
                    fAppearanceColorList.select(0);
                    handleAppearanceColorListSelection();
                }
            }
        });
    }
    
    
    private void initializeDefaultColors() {    
        if (!getPreferenceStore().contains(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR)) {
            RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION).getRGB();
            PreferenceConverter.setDefault(fOverlayStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, rgb);
            PreferenceConverter.setDefault(getPreferenceStore(), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR, rgb);
        }
        if (!getPreferenceStore().contains(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR)) {
            RGB rgb= getControl().getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT).getRGB();
            PreferenceConverter.setDefault(fOverlayStore, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, rgb);
            PreferenceConverter.setDefault(getPreferenceStore(), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR, rgb);
        }
    }
    
    
    /*
     * @see PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        super.performDefaults();
        handleAppearanceColorListSelection();
    }
    
    
    /**
     * Sets default preference values
     */
    protected static void initializeDefaultPreferences(Preferences prefs) {
        //text
        prefs.setDefault(TAB_WIDTH, DEFAULT_TAB_WIDTH);
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 
                DEFAULT_EDITOR_PRINT_MARGIN_COLUMN);
        
        //checkboxes
        prefs.setDefault(SUBSTITUTE_TABS, DEFAULT_SUBSTITUTE_TABS);
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, 
                StringConverter.asString(DEFAULT_EDITOR_OVERVIEW_RULER));
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, 
                StringConverter.asString(DEFAULT_EDITOR_LINE_NUMBER_RULER));
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, 
                StringConverter.asString(DEFAULT_EDITOR_CURRENT_LINE));
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, 
                StringConverter.asString(DEFAULT_EDITOR_PRINT_MARGIN));
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS, 
                StringConverter.asString(DEFAULT_EDITOR_USE_CUSTOM_CARETS));
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WIDE_CARET, 
                StringConverter.asString(DEFAULT_EDITOR_WIDE_CARET));
        
        //matching
        prefs.setDefault(USE_MATCHING_BRACKETS, DEFAULT_USE_MATCHING_BRACKETS);
        prefs.setDefault(
                MATCHING_BRACKETS_COLOR, 
                StringConverter.asString(DEFAULT_MATCHING_BRACKETS_COLOR));
        
        //colors
        prefs.setDefault(
                CODE_COLOR,
                StringConverter.asString(DEFAULT_CODE_COLOR));
        prefs.setDefault(
                NUMBER_COLOR,
                StringConverter.asString(DEFAULT_NUMBER_COLOR));
        prefs.setDefault(
                KEYWORD_COLOR,
                StringConverter.asString(DEFAULT_KEYWORD_COLOR));
        prefs.setDefault(
                FUNCTION_COLOR,
                StringConverter.asString(DEFAULT_FUNCTION_COLOR));
        prefs.setDefault(
                TOOLBOX_COLOR,
                StringConverter.asString(DEFAULT_TOOLBOX_COLOR));
        prefs.setDefault(
                STRING_COLOR,
                StringConverter.asString(DEFAULT_STRING_COLOR));
        prefs.setDefault(
                COMMENT_COLOR,
                StringConverter.asString(DEFAULT_COMMENT_COLOR));
        prefs.setDefault(
                AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, 
                StringConverter.asString(DEFAULT_PREFERENCE_COLOR_BACKGROUND));
        prefs.setDefault(
                AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, 
                StringConverter.asString(DEFAULT_EDITOR_BACKGROUND_COLOR_SYSTEM_DEFAULT));
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR, 
                StringConverter.asString(DEFAULT_EDITOR_CURRENT_LINE_COLOR));
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR, 
                StringConverter.asString(DEFAULT_EDITOR_LINE_NUMBER_RULER_COLOR));
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR, 
                StringConverter.asString(DEFAULT_EDITOR_PRINT_MARGIN_COLOR));
        
        //for selection colors see initializeDefaultColors()
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR, 
                StringConverter.asString(DEFAULT_EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR));
        prefs.setDefault(
                AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR, 
                StringConverter.asString(DEFAULT_EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR));
        
        //no UI
        prefs.setDefault(CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT);
        prefs.setDefault(RUN_MANY_SCRIPT_LOCATION, DEFAULT_RUN_MANY_SCRIPT_LOCATION);        
    }
    
}
