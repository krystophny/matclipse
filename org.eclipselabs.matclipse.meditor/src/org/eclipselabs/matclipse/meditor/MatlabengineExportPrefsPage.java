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

package org.eclipselabs.matclipse.meditor;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import org.eclipselabs.matclipse.meditor.util.XMLExporter;


public class MatlabengineExportPrefsPage extends AbstractMatlabenginePrefsPage {
    
    public static final String  AUTHOR               = "AUTHOR";
    public static final String  DEFAULT_AUTHOR       = "";
    public static final String  AUTHOR_EMAIL         = "AUTHOR_EMAIL";
    public static final String  DEFAULT_AUTHOR_EMAIL = "";
    public static final String  AUTHOR_WWW           = "AUTHOR_WWW";
    public static final String  DEFAULT_AUTHOR_WWW   = "";
    public static final String  DATE                 = "DATE";
    public static final String  DEFAULT_DATE         = GetCurrentDateString();
    public static final String  CONTENT              = "CONTENT";
    public static final String  DEFAULT_CONTENT      = "";
    public static final String  TITLE                = "TITLE";
    public static final String  DEFAULT_TITLE        = "";
    public static final String[] TYPES              = {"Script", "Function"};
    public static final String  TYPE                 = "TYPE";
    public static final String  DEFAULT_TYPE         = TYPES[0];
    public static final String  SHOW_DIALOG          = "SHOW_DIALOG";
    public static final boolean DEFAULT_SHOW_DIALOG  = true;
    public static final String  WRITE_FILE_METADATA  = "WRITE_FILE_MEDADATA";
    public static final boolean DEFAULT_WRITE_F_MDAT = true;
    
    public static final String  OUTPUT_XML           = "OUTPUT_XML";
    public static final boolean DEFAULT_OUTPUT_XML   = true;
    public static final String  OUTPUT_HTML          = "OUTPUT_HTML";
    public static final boolean DEFAULT_OUTPUT_HTML  = true;
    public static final String  OUTPUT_LATEX         = "OUTPUT_LATEX";
    public static final boolean DEFAULT_OUTPUT_LATEX = true;
    public static final String  OUTPUT_PDF           = "OUTPUT_PDF";
    public static final boolean DEFAULT_OUTPUT_PDF   = true;

    public static final String  OUTPUT_COREONLY            = "OUTPUT_CORE_ONLY";
    public static final boolean DEFAULT_OUTPUT_COREONLY    = false;
    public static final String  OUTPUT_DIRECTORY           = "OUTPUT_DIRECTORY";
    public static final String  DEFAULT_OUTPUT_DIRECTORY   = (new File("~")).getAbsolutePath();
    public static final String  OUT_USE_INPUTDIR           = "OUT_USE_INPUTDIR";
    public static final boolean DEFAULT_OUT_USE_INPUTDIR   = false;
    public static final String  SHOW_OUTPUT                = "SHOW_OUTPUT";
    public static final boolean DEFAULT_SHOW_OUTPUT        = false;

    public static final String  OUTPUT_ALWAYS_LINENUMBERS  = "OUTPUT_ALWAYS_LINENUMBERS";
    public static final String  OUTPUT_NO_LINENUMBERS      = "OUTPUT_NO_LINENUMBERS";
    public static final String  OUTPUT_EDIT_LINENUMBERS    = "OUTPUT_EDIT_LINENUMBERS";
    public static final String  DEFAULT_OUTPUT_LINENUMBERS = OUTPUT_EDIT_LINENUMBERS;
    
    public static final String  PLACEHOLDER_CONFIGDIR      = "CONFDIR";
    public static final String  XSLT_HTML_FILE             = "XSLT_HTML_FILE";
    public static final String  DEFAULT_XSLT_HTML_FILE     = 
        PLACEHOLDER_CONFIGDIR + "/html.xsl";
    public static final String  XSLT_HTML_L_FILE           = "XSLT_HTML_L_FILE";
    public static final String  DEFAULT_XSLT_HTML_L_FILE   = 
        PLACEHOLDER_CONFIGDIR + "/html_lines.xsl";
    public static final String  XSLT_LATEX_FILE            = "XSLT_LATEX_FILE";
    public static final String  DEFAULT_XSLT_LATEX_FILE    = 
        PLACEHOLDER_CONFIGDIR + "/latex.xsl";
    public static final String  XSLT_LATEX_L_FILE          = "XSLT_LATEX_L_FILE";
    public static final String  DEFAULT_XSLT_LATEX_L_FILE  = 
        PLACEHOLDER_CONFIGDIR + "/latex_lines.xsl";
    public static final String  XSLT_LATEX_C_FILE          = "XSLT_LATEX_C_FILE";
    public static final String  DEFAULT_XSLT_LATEX_C_FILE  = 
        PLACEHOLDER_CONFIGDIR + "/latex_core.xsl";
    public static final String  XSLT_LATEX_CL_FILE         = "XSLT_LATEX_CL_FILE";
    public static final String  DEFAULT_XSLT_LATEX_CL_FILE = 
        PLACEHOLDER_CONFIGDIR + "/latex_core_lines.xsl";
    
    private static final int INDENT_H       = 20;
    private static final int INDENT_V_BIG   = 20;
    private static final int INDENT_V_SMALL = 10;

    
    public MatlabengineExportPrefsPage(){
        super("Matlab Editor export settings:");
    }
    

    private static String GetCurrentDateString(){
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String dateString = dateFormat.format(today);
        return  dateString;
    }
    
    
    protected OverlayPreferenceStore createOverlayStore() {
        
        ArrayList<OverlayPreferenceStore.OverlayKey> overlayKeys = 
            new ArrayList<OverlayPreferenceStore.OverlayKey>();

        // author data
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, AUTHOR));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, AUTHOR_EMAIL));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, AUTHOR_WWW));

        // filespecific metadata
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, SHOW_DIALOG));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, WRITE_FILE_METADATA));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, DATE));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, CONTENT));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, TITLE));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, TYPE));
        
        // output formats
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, OUTPUT_XML));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, OUTPUT_HTML));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, OUTPUT_LATEX));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, OUTPUT_PDF));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, OUTPUT_COREONLY));

        // show output
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, SHOW_OUTPUT));
        
        // output directory
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, OUTPUT_DIRECTORY));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, OUT_USE_INPUTDIR));

        // show linenumber options
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, OUTPUT_ALWAYS_LINENUMBERS));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, OUTPUT_NO_LINENUMBERS));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.BOOLEAN, OUTPUT_EDIT_LINENUMBERS));
        
        // transformation files
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, XSLT_HTML_FILE));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, XSLT_HTML_L_FILE));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, XSLT_LATEX_FILE));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, XSLT_LATEX_L_FILE));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, XSLT_LATEX_C_FILE));
        overlayKeys.add(new OverlayPreferenceStore.OverlayKey(
                OverlayPreferenceStore.STRING, XSLT_LATEX_CL_FILE));

        OverlayPreferenceStore.OverlayKey[] keys= 
            new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
        overlayKeys.toArray(keys);
        return new OverlayPreferenceStore(getPreferenceStore(), keys);
    }

    
    protected Control createAppearancePage(Composite parent) {

        ScrolledComposite scrollFrame= new ScrolledComposite(parent, SWT.V_SCROLL);
        scrollFrame.setExpandHorizontal(true);
        scrollFrame.setExpandVertical(true);
        Composite appearanceComposite= new Composite(scrollFrame, SWT.NONE);
        scrollFrame.setContent(appearanceComposite);
        GridLayout layout= new GridLayout(); 
        layout.numColumns= 2;
        appearanceComposite.setLayout(layout);

        Group groupMetadata = addGroup(appearanceComposite, "Metadata", 0, 2, 2);
        addLabel(groupMetadata, "Author", 0, 2);
        addTextField(groupMetadata, "Name:", 
                AUTHOR, Text.LIMIT, INDENT_H, false);
        addTextField(groupMetadata, "Email:", 
                AUTHOR_EMAIL, Text.LIMIT, INDENT_H, false);
        addTextField(groupMetadata, "Homepage:", 
                AUTHOR_WWW, Text.LIMIT, INDENT_H, false);

        addLabel(groupMetadata, "File specific data", INDENT_V_SMALL, 2);
            addCheckBox(groupMetadata, "Show dialog before each export", SHOW_DIALOG, INDENT_H);
        Button buttonWriteData = 
            addCheckBox(groupMetadata, "Write file specific metadata", 
                    WRITE_FILE_METADATA, INDENT_H);
        Control textTitle = 
            addTextField(groupMetadata, "Title:", TITLE, Text.LIMIT, INDENT_H, false);
        Control textDate = 
            addTextField(groupMetadata, "Date:", DATE, Text.LIMIT, INDENT_H, false);
        Control textContent = 
            addTextField(groupMetadata, "Content:", CONTENT, Text.LIMIT, INDENT_H, false);
        Control comboType = 
            addComboField(groupMetadata, "Type:", TYPE, TYPES, INDENT_H);
        createDependency(buttonWriteData, SHOW_DIALOG, textTitle,   false);
        createDependency(buttonWriteData, SHOW_DIALOG, textDate,    false);
        createDependency(buttonWriteData, SHOW_DIALOG, textContent, false);
        createDependency(buttonWriteData, SHOW_DIALOG, comboType,   false);

        Group groupOutput = addGroup(appearanceComposite, "Output", INDENT_V_BIG, 2, 2);
        addLabel(groupOutput, "Output Formats", 0, 2);
        addCheckBox(groupOutput, "XML",   OUTPUT_XML,   INDENT_H);
        addCheckBox(groupOutput, "HTML",  OUTPUT_HTML,  INDENT_H);
        Button buttonOutputLatex = 
            addCheckBox(groupOutput, "LaTeX", OUTPUT_LATEX, INDENT_H);
        Button buttonLatexCoreonly = 
            addCheckBox(groupOutput, "export LaTeX core only", OUTPUT_COREONLY, 2*INDENT_H);
        Button buttonOutputPDF = 
            addCheckBox(groupOutput, "PDF",   OUTPUT_PDF,   INDENT_H);
        createDependency(buttonOutputLatex, OUTPUT_LATEX, buttonLatexCoreonly, false);
        createMultiDependency(
                new MasterInfo[]{
                        new MasterInfo(buttonOutputLatex, OUTPUT_LATEX, false), 
                        new MasterInfo(buttonLatexCoreonly, OUTPUT_COREONLY, false) }, 
                buttonOutputPDF, true);

        addCheckBox(groupOutput, "Show generated files after export", 
                SHOW_OUTPUT, 0);
        
        addLabel(groupOutput, "Output directory", INDENT_V_SMALL, 2);
        Button buttonOutputUseInputDir = 
            addCheckBox(groupOutput, "Use the Matlab file's directory", 
                    OUT_USE_INPUTDIR, INDENT_H);
        Control textOutputDir = 
            addFileLine(groupOutput, OUTPUT_DIRECTORY, INDENT_H, true, null, null);
        createDependency(buttonOutputUseInputDir, OUT_USE_INPUTDIR, textOutputDir, true);
        
        addRadioButtonField(groupOutput, 
                "Print line numbers:", 
                new String[][] { 
                    { "Always",  OUTPUT_ALWAYS_LINENUMBERS}, 
                    { "Never", OUTPUT_NO_LINENUMBERS }, 
                    { "Only if visible in editor", OUTPUT_EDIT_LINENUMBERS} }, 
                INDENT_V_SMALL, 
                2, 
                INDENT_H);

        String[] xslFilterExtensions = new String[] {"*.xsl"};
        String[] xslFilterNames      = new String[] {"XSL Transformation Files"};
        Group groupTransform = 
            addGroup(appearanceComposite, "Transformation Options", INDENT_V_BIG, 2, 3);
        addLabel(groupTransform, "XSLT Transformation Files (use " + PLACEHOLDER_CONFIGDIR + 
                " for the Plugin ConfigDir)", 0, 3);
        addFileLine(groupTransform, "HTML, no linenum:", 
                XSLT_HTML_FILE, 0, false, xslFilterExtensions, xslFilterNames);
        addFileLine(groupTransform, "HTML, linenum:", 
                XSLT_HTML_L_FILE, 0, false, xslFilterExtensions, xslFilterNames);
        addFileLine(groupTransform, "LaTeX, no linenum:", 
                XSLT_LATEX_FILE, 0, false, xslFilterExtensions, xslFilterNames);
        addFileLine(groupTransform, "LaTeX, linenum:", 
                XSLT_LATEX_L_FILE, 0, false, xslFilterExtensions, xslFilterNames);
        addFileLine(groupTransform, "LaTeX, core, no linenum:", 
                XSLT_LATEX_C_FILE, 0, false, xslFilterExtensions, xslFilterNames);
        addFileLine(groupTransform, "LaTeX, core, linenum:", 
                XSLT_LATEX_CL_FILE, 0, false, xslFilterExtensions, xslFilterNames);

        scrollFrame.setMinSize(appearanceComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        
        return scrollFrame;
    }

    
    /**
     * Sets default preference values
     */
    protected static void initializeDefaultPreferences(Preferences prefs) {
        prefs.setDefault(OUTPUT_XML,      DEFAULT_OUTPUT_XML);
        prefs.setDefault(OUTPUT_HTML,     DEFAULT_OUTPUT_HTML);
        prefs.setDefault(OUTPUT_LATEX,    DEFAULT_OUTPUT_LATEX);
        prefs.setDefault(OUTPUT_PDF,      DEFAULT_OUTPUT_PDF);
        prefs.setDefault(OUTPUT_COREONLY, DEFAULT_OUTPUT_COREONLY);
        prefs.setDefault(SHOW_OUTPUT,     DEFAULT_SHOW_OUTPUT);

        prefs.setDefault(OUTPUT_DIRECTORY, DEFAULT_OUTPUT_DIRECTORY);
        prefs.setDefault(OUT_USE_INPUTDIR, DEFAULT_OUT_USE_INPUTDIR);

        prefs.setDefault(AUTHOR,       DEFAULT_AUTHOR);
        prefs.setDefault(AUTHOR_EMAIL, DEFAULT_AUTHOR_EMAIL);
        prefs.setDefault(AUTHOR_WWW,   DEFAULT_AUTHOR_WWW);

        prefs.setDefault(WRITE_FILE_METADATA,  DEFAULT_WRITE_F_MDAT);
        prefs.setDefault(SHOW_DIALOG,          DEFAULT_SHOW_DIALOG);
        prefs.setDefault(DATE,                 DEFAULT_DATE);
        prefs.setDefault(CONTENT,              DEFAULT_CONTENT);
        prefs.setDefault(TITLE,                DEFAULT_TITLE);
        prefs.setDefault(TYPE,                 DEFAULT_TYPE);
        
        prefs.setDefault(OUTPUT_ALWAYS_LINENUMBERS,  false);
        prefs.setDefault(OUTPUT_NO_LINENUMBERS,      false);
        prefs.setDefault(OUTPUT_EDIT_LINENUMBERS,    false);
        prefs.setDefault(DEFAULT_OUTPUT_LINENUMBERS, true);

        prefs.setDefault(XSLT_HTML_FILE,     DEFAULT_XSLT_HTML_FILE);
        prefs.setDefault(XSLT_HTML_L_FILE,   DEFAULT_XSLT_HTML_L_FILE);
        prefs.setDefault(XSLT_LATEX_FILE,    DEFAULT_XSLT_LATEX_FILE);
        prefs.setDefault(XSLT_LATEX_L_FILE,  DEFAULT_XSLT_LATEX_L_FILE);
        prefs.setDefault(XSLT_LATEX_C_FILE,  DEFAULT_XSLT_LATEX_C_FILE);
        prefs.setDefault(XSLT_LATEX_CL_FILE, DEFAULT_XSLT_LATEX_CL_FILE);
    }

    
    public static Properties getMetadataAsProperties(){
        Properties metadata = new Properties();
        Preferences exportPreferences = getPreferences();
        
        metadata.setProperty(XMLExporter.AUTHOR_NAME_KEY, 
                exportPreferences.getString(AUTHOR));
        metadata.setProperty(XMLExporter.AUTHOR_EMAIL_KEY, 
                exportPreferences.getString(AUTHOR_EMAIL));
        metadata.setProperty(XMLExporter.AUTHOR_WWW_KEY, 
                exportPreferences.getString(AUTHOR_WWW));

        if (getPreferences().getBoolean(WRITE_FILE_METADATA)){
            metadata.setProperty(XMLExporter.FILE_DATE_KEY, 
                    exportPreferences.getString(DATE));
            metadata.setProperty(XMLExporter.FILE_CONTENT_KEY, 
                    exportPreferences.getString(CONTENT));
            metadata.setProperty(XMLExporter.FILE_TITLE_KEY, 
                    exportPreferences.getString(TITLE));
            metadata.setProperty(XMLExporter.FILE_TYPE_KEY, 
                    exportPreferences.getString(TYPE));
        }
        return metadata;
    }

    
    public static Properties getExportProperties(){
        Properties exportProperties = new Properties();
        Preferences exportPreferences = getPreferences();
        
        boolean lineNumbers = exportPreferences.getBoolean(OUTPUT_ALWAYS_LINENUMBERS) ||
                ( exportPreferences.getBoolean(OUTPUT_EDIT_LINENUMBERS) &&
                  exportPreferences.getBoolean(
                          AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER));
        exportProperties.setProperty(
                XMLExporter.OUTPUT_LINENUM_KEY, Boolean.toString(lineNumbers));

        exportProperties.setProperty(XMLExporter.OUTPUT_XML_KEY, 
                Boolean.toString(exportPreferences.getBoolean(OUTPUT_XML)));
        exportProperties.setProperty(XMLExporter.OUTPUT_HTML_KEY, 
                Boolean.toString(exportPreferences.getBoolean(OUTPUT_HTML)));
        exportProperties.setProperty(XMLExporter.OUTPUT_LATEX_KEY, 
                Boolean.toString(exportPreferences.getBoolean(OUTPUT_LATEX)));
        exportProperties.setProperty(XMLExporter.OUTPUT_PDF_KEY, 
                Boolean.toString(exportPreferences.getBoolean(OUTPUT_PDF)));
        exportProperties.setProperty(XMLExporter.OUTPUT_COREONLY_KEY, 
                Boolean.toString(exportPreferences.getBoolean(OUTPUT_COREONLY)));

        exportProperties.setProperty(XMLExporter.XSLT_HTML_FILE_KEY, 
                exportPreferences.getString(XSLT_HTML_FILE));
        exportProperties.setProperty(XMLExporter.XSLT_HTML_L_FILE_KEY, 
                exportPreferences.getString(XSLT_HTML_L_FILE));
        exportProperties.setProperty(XMLExporter.XSLT_LATEX_FILE_KEY, 
                exportPreferences.getString(XSLT_LATEX_FILE));
        exportProperties.setProperty(XMLExporter.XSLT_LATEX_L_FILE_KEY, 
                exportPreferences.getString(XSLT_LATEX_L_FILE));
        exportProperties.setProperty(XMLExporter.XSLT_LATEX_C_FILE_KEY, 
                exportPreferences.getString(XSLT_LATEX_C_FILE));
        exportProperties.setProperty(XMLExporter.XSLT_LATEX_CL_FILE_KEY, 
                exportPreferences.getString(XSLT_LATEX_CL_FILE));

        return exportProperties;
    }

}
