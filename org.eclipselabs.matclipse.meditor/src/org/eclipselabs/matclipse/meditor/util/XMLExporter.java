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

package org.eclipselabs.matclipse.meditor.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;

import org.eclipselabs.matclipse.meditor.Activator;
import org.eclipselabs.matclipse.meditor.MatlabengineExportPrefsPage;
import org.eclipselabs.matclipse.meditor.editors.partitioner.MatlabPartitionScanner;


public class XMLExporter {

    public  final static String XML_ENCODING = "UTF-8";
    private final static String XML_HEAD = 
        "<?xml version=\"1.0\" encoding=\"" + XML_ENCODING + "\"?>";
    
    private final static String XML_ROOTTAG          = "matlab_file";
    private final static String XML_CONTENTTAG       = "formatted_content";
    private final static String XML_FILENAMETAG      = "filename";
    private final static String XML_AUTHORTAG        = "author";
    private final static String XML_AUTHORNAMETAG    = "name";
    private final static String XML_AUTHOREMAILTAG   = "email";
    private final static String XML_AUTHORWWWTAG     = "homepage";
    private final static String XML_FILETAG          = "file";
    private final static String XML_FILETITLETAG     = "title";
    private final static String XML_FILECONTENTTAG   = "content";
    private final static String XML_FILEDATETAG      = "date";
    private final static String XML_FILETYPETAG      = "type";
    private final static String XML_PARTITIONINGTAG  = "partitioning";
    private final static String XML_PARTITIONTAG     = "partition";
    private final static String XML_PARTITIONTYPEARG = "type";
    private final static String XML_LINETAG          = "line";
    private final static String XML_LINENUMBERARG    = "linenumber";
    private final static String XML_MARKUPTAG        = "markuptext";
    private final static String XML_SOURCETAG        = "source";
    private final static String XML_INDENT_STR       = "    ";
    private final static String XML_M_HEADER1TAG     = "header1";
    private final static String XML_M_HEADER2TAG     = "header2";
    private final static String XML_M_HEADER3TAG     = "header3";
    private final static String XML_M_TEXTTAG        = "text";
    private final static String XML_M_NEWLINETAG     = "markupnewline";
    private final static String XML_M_TRUETYPETAG    = "truetype";
    private final static String XML_M_LINKTAG        = "link";
    private final static String XML_M_CEQUATIONTAG   = "cequation";
    private final static String XML_M_EQUATIONNUMARG = "eqnum";
    private final static String XML_M_LISTTAG        = "list";
    private final static String XML_M_LISTITEMTAG    = "listitem";
    private final static String XML_M_NUMBEREDLISTTAG= "numberedlist";
    private final static String XML_M_NUMBEREDLISTITEMTAG = "numberedlistitem";
    private final static String XML_M_ITEMNUMBERARG       = "number";
    private final static String XML_M_LATEXCMDTAG         = "latex_cmd";
    
    private final static String XML_VARLISTTAG       = "variable_list";
    private final static String XML_VARTAG           = "variable";
    
    private final static String XML_FUNCLISTTAG      = "function_list";
    private final static String XML_FUNCTAG          = "function";
    
    private final static String CONTENT_TAB_STR      = "&#160;&#160;&#160;&#160;";
    private final static String CONTENT_SPACE_STR    = "&#160;";
    private final static String LATEX_REP_STRING     = "<!--formula-->";
    
    /** keys and default values for the properties */
    private final static String  PROPERTYFILE          = "config/XML_export_properties.xml";
    public  final static String  OUTPUT_XML_KEY        = "OUTPUT_XML";
    public  final static String  OUTPUT_HTML_KEY       = "OUTPUT_HTML";
    public  final static String  OUTPUT_LATEX_KEY      = "OUTPUT_LATEX";
    public  final static String  OUTPUT_PDF_KEY        = "OUTPUT_PDF";
    public  final static String  OUTPUT_COREONLY_KEY   = "OUTPUT_COREONLY";
    public  final static boolean OUTPUT_COREONLY_DEF   = false;
    public  final static String  CONFIGFILEDIR_KEY     = "CONFIGFILEDIR";
    public  final static String  CONFIGFILEDIR_DEF     = "./";
    public  final static String  OUTPUTDIR_KEY         = "OUTPUTDIR";
    private final static String  OUTPUTDIR_DEF         = "./";
//    public  final static String  SCHEMAFILE_KEY        = "SCHEMAFILE";
    public final static String  SCHEMAFILE_DEF        = "export.xsd";
    public  final static String  OUTPUT_LINENUM_KEY    = "OUTPUT_LINENUMBERS";
    public  final static String  OUTPUT_LINENUM_DEF    = "true";
    public  final static String  XSLT_HTML_FILE_KEY    = "XSLT_HTML_FILE";
    public  final static String  XSLT_HTML_FILE_DEF    = 
        MatlabengineExportPrefsPage.DEFAULT_XSLT_HTML_FILE;
    public  final static String  XSLT_HTML_L_FILE_KEY  = "XSLT_HTML_LINENUM_FILE";
    public  final static String  XSLT_HTML_L_FILE_DEF  = 
        MatlabengineExportPrefsPage.DEFAULT_XSLT_HTML_L_FILE;
    public  final static String  XSLT_LATEX_FILE_KEY   = "XSLT_LATEX_FILE";
    public  final static String  XSLT_LATEX_FILE_DEF   = 
        MatlabengineExportPrefsPage.DEFAULT_XSLT_LATEX_FILE;
    public  final static String  XSLT_LATEX_L_FILE_KEY = "XSLT_LATEX_LINENUM_FILE";
    public  final static String  XSLT_LATEX_L_FILE_DEF = 
        MatlabengineExportPrefsPage.DEFAULT_XSLT_LATEX_L_FILE;
    public  final static String  XSLT_LATEX_C_FILE_KEY = "XSLT_LATEX_CORE_FILE";
    public  final static String  XSLT_LATEX_C_FILE_DEF = 
        MatlabengineExportPrefsPage.DEFAULT_XSLT_LATEX_C_FILE;
    public  final static String  XSLT_LATEX_CL_FILE_KEY = "XSLT_LATEX_CORE_LINENUM_FILE";
    public  final static String  XSLT_LATEX_CL_FILE_DEF = 
        MatlabengineExportPrefsPage.DEFAULT_XSLT_LATEX_CL_FILE;
    
    /** keys and default values for metadata properties */
    public  final static String FILETITLE_KEY      = "FILETITLE";
    private final static String FILETITLE_DEF      = "";
    public  final static String AUTHOR_NAME_KEY    = "AUTHOR_NAME";
    private final static String AUTHOR_NAME_DEF    = "";
    public  final static String AUTHOR_EMAIL_KEY   = "AUTHOR_EMAIL";
    private final static String AUTHOR_EMAIL_DEF   = "";
    public  final static String AUTHOR_WWW_KEY     = "AUTHOR_WWW";
    private final static String AUTHOR_WWW_DEF     = "";
    public  final static String FILE_TITLE_KEY     = "FILE_TITLE";
    private final static String FILE_TITLE_DEF     = "";
    public  final static String FILE_CONTENT_KEY   = "FILE_CONTENT";
    private final static String FILE_CONTENT_DEF   = "";
    public  final static String FILE_DATE_KEY      = "FILE_DATE";
    private final static String FILE_DATE_DEF      = "";
    public  final static String FILE_TYPE_KEY      = "FILE_TYPE";
    private final static String FILE_TYPE_DEF      = "";
    
    /** file extensions */
    private final static String FILEEXTENSION_XML  = ".xml";
    private final static String PICDIR_EXT         = "_pics";
    
    /** some directories and files */
    private final static String TOOLS_DIR          = "ext_tools/";
    private final static String CSS_FILE           = "mltutor_style.css";
    private final static String LATEX_PIC_TEMPLATE = "latexpic.template";
    private final static String LATEX_PR_FILE      = "shorts.tex";
    private final static String LATEX_MLTDEF_FILE  = "shortsmltutor.tex";
    
    /** textmodes */
    private final static int MODE_NOMODE = 0;
    private final static int MODE_CODE   = 1;
    private final static int MODE_MARKUP = 2;
    private final static String NO_LISTTAG = "";
    
    public final static String PLACEHOLDER_CONFIGDIR = 
        MatlabengineExportPrefsPage.PLACEHOLDER_CONFIGDIR;
    
    /** Error messages of exceptions to throw */
    private final static String EXC_TEXT_BADLOCATION = 
        "Wasn't able to generate XML-output due to an internal error";
    private final static String EXC_TEXT_XMLWRITE = 
        "Wasn't able to write XML-File";
    private final static String EXC_TEXT_PDFCONVERT =
        "Wasn't able to convert to PDF";
    
    private Vector<MarkupRegex> regexList = null;
    private IDocument document            = null;
    private IPath configfileDir           = Path.fromOSString("./config/");
    private IPath baseDir                 = Path.fromOSString("./");
    private boolean skipNewline           = false;
    private int textMode                  = MODE_NOMODE;
    private String currentModeTag         = "";
    private Vector<String> equations      = null;
    private IPath XMLFilePath             = null;
    
    private String currentEnumerationTag  = NO_LISTTAG;
    private int    currentListitemNumber  = 0;
    private Hashtable<String, String> listingTags;
    
    private Exception occuredException = null;
    private HashMap<String, IPath> generatedFiles = new HashMap<String, IPath>();

    
    public XMLExporter(){
        this.regexList = new Vector<MarkupRegex>();
        this.regexList.add(new MarkupRegex("(%\\s*====(.*)====).*",  XML_M_HEADER3TAG));
        this.regexList.add(new MarkupRegex("(%\\s*===(.*)===).*",    XML_M_HEADER2TAG));
        this.regexList.add(new MarkupRegex("(%\\s*==(.*)==).*",      XML_M_HEADER1TAG));
        this.regexList.add(new MarkupRegex("(%%\\s*)",               XML_M_NEWLINETAG));
        this.regexList.add(new MarkupRegex("(%%(.*))",               XML_M_TEXTTAG));
        this.regexList.add(new MarkupRegex(".*(<tt>(.*)</tt>).*",    XML_M_TRUETYPETAG));
        this.regexList.add(new MarkupRegex(".*(\\[(.*)\\]).*",       XML_M_LINKTAG));
        this.regexList.add(new MarkupRegex(".*(\\$\\$(.*)\\$\\$).*", XML_M_CEQUATIONTAG));
        this.regexList.add(new MarkupRegex("(%\\*\\s*(.*))",         XML_M_LISTITEMTAG));
        this.regexList.add(new MarkupRegex("(%#\\s*(.*))",           XML_M_NUMBEREDLISTITEMTAG));
        this.regexList.add(new MarkupRegex("(%(\\\\.*))",            XML_M_LATEXCMDTAG));
        
        this.equations = new Vector<String>();
        
        this.listingTags = new Hashtable<String, String>(2, (float)1.0);
        this.listingTags.put(XML_M_LISTITEMTAG, XML_M_LISTTAG);
        this.listingTags.put(XML_M_NUMBEREDLISTITEMTAG, XML_M_NUMBEREDLISTTAG);
    }
    
    
    public static void main(String[] args) {
        try {
            if (args.length > 0){
                IPath  inputFilePath  = Path.fromOSString(args[0]);
                IPath  configfileDir  = Path.fromOSString(CONFIGFILEDIR_DEF);
                IPath  outputDir      = Path.fromOSString(OUTPUTDIR_DEF);
                String XMLOutputFileName = 
                    inputFilePath.lastSegment().replace(".m", FILEEXTENSION_XML);
                boolean xmlOutput     = false;
                boolean htmlOutput    = false;
                boolean latexOutput   = false;
                boolean pdfOutput     = false;
                boolean coreonly      = false;
                boolean onlyVariables = false;
                boolean onlyFunctions = false;
                String  metadataFile  = null;
                
                for (int argCount = 1; argCount < args.length; argCount++){
                    if (args[argCount].equals("-o")){
                        XMLOutputFileName = args[argCount+1];
                        if (!XMLOutputFileName.endsWith(FILEEXTENSION_XML)){
                            XMLOutputFileName += FILEEXTENSION_XML;
                        }
                        argCount++;
                    } else if (args[argCount].equals("--xml")){
                        xmlOutput = true;
                    } else if (args[argCount].equals("--html")){
                        htmlOutput = true;
                    } else if (args[argCount].equals("--latex")){
                        latexOutput = true;
                    } else if (args[argCount].equals("--pdf")){
                        pdfOutput = true;
                    } else if (args[argCount].equals("--coreonly")){
                        coreonly = true;
                    } else if (args[argCount].equals("--varonly")){
                        onlyVariables = true;
                    } else if (args[argCount].equals("--funconly")){
                        onlyFunctions = true;
                    } else if (args[argCount].equals("-m")){
                        metadataFile = args[argCount+1];
                        argCount++;
                    }
                }
                if (latexOutput && coreonly){
                    pdfOutput = false;
                }
                                    
                // load properties
                File propertyFile = new File(PROPERTYFILE);
                FileInputStream propertyFileInputStream = null;
                Properties exporterProperties = new Properties();
                if (propertyFile.exists()){
                    propertyFileInputStream = new FileInputStream(propertyFile);
                    exporterProperties.loadFromXML(propertyFileInputStream);
                    configfileDir = Path.fromOSString(exporterProperties.getProperty(
                            CONFIGFILEDIR_KEY, CONFIGFILEDIR_DEF));
                    outputDir = toAbsolutePath(exporterProperties.getProperty(
                            OUTPUTDIR_KEY, OUTPUTDIR_DEF));
                }
                exporterProperties.setProperty(
                        OUTPUT_COREONLY_KEY, Boolean.toString(coreonly && latexOutput));
                
                // generate XML
                XMLExporter exporter = new XMLExporter();
                exporter.initDocument(inputFilePath, configfileDir);
                String filetitle = inputFilePath.lastSegment();
                IPath XMLFilePath = 
                    outputDir.addTrailingSeparator().append(XMLOutputFileName);
                
                Properties metadata = new Properties();
                if (metadataFile != null){
                    metadata.loadFromXML(new FileInputStream(metadataFile));
                }
                metadata.setProperty(FILETITLE_KEY, filetitle);

                if (onlyVariables){
                    exporter.exportOnlyVars(metadata, XMLFilePath);
                } else if (onlyFunctions){
                    exporter.exportOnlyFuncs(metadata, XMLFilePath);
                } else {
                    exporter.export(exporterProperties, metadata, XMLFilePath);
                    
                    // do requested transformations
                    String[] xsltFiles = extractXsltFiles(exporterProperties);
                    if (htmlOutput){
                        String xsltFile = xsltFiles[0].replace(
                                PLACEHOLDER_CONFIGDIR, configfileDir.toOSString());
                        if (!exporter.transformToHTML(filetitle, xsltFile)){
                            System.err.println(exporter.fetchException().getMessage());
                        }
                    }
                    if (latexOutput || pdfOutput){
                        String xsltFile = xsltFiles[1].replace(
                                PLACEHOLDER_CONFIGDIR, configfileDir.toOSString());
                        exporter.transformToLatexAndPdf(latexOutput, pdfOutput, xsltFile);
                    }
                    
                    if (!xmlOutput){
                        exporter.deleteXMLFile();
                    }
                } 
                
                //System.out.println(output);
            } else {
                System.out.println("Usage:");
                System.out.println("java org.eclipselabs.matclipse.util.XMLExporter" +
                        " <inputfile> [-o <outputfile>] [--html]");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private static IPath toAbsolutePath(String path){
        return Path.fromOSString((new File(path)).getAbsolutePath());
    }

    
    /**
     * Transforms an XML-File into HTML by using this class' transform method.
     * Several files will be created in the directory that contains the XML-file:
     *   a HTML-File with the same filename as the XML-File (excepting the extension)
     *   a CSS-File
     *   if the source contains equations, a subdirectory with all pics that contain the 
     *     equations will be generated
     * @param filetitle Title of the file
     * @param XMLFilePath Complete Path to the XML-file to transform
     * @throws FileNotFoundException
     * @throws TransformerException
     */
    public boolean transformToHTML(String filetitle, String xsltFile) 
            throws FileNotFoundException, TransformerException {
        
        boolean success = false;
        boolean picSuccess = false;
        if (this.XMLFilePath != null){
            IPath outputDir = this.XMLFilePath.removeLastSegments(1);
            try {
                exportPics(outputDir, filetitle);
                picSuccess = true;
            } catch (Exception e) {
                this.occuredException = new Exception(
                        "No equation pics were created (the rest may be ok)", e);
            }
            IPath htmlFilePath = 
                this.XMLFilePath.removeFileExtension().addFileExtension("html");
            xsltFile = xsltFile.replace(PLACEHOLDER_CONFIGDIR, 
                    this.configfileDir.addTrailingSeparator().toOSString());
            transform(this.XMLFilePath.toOSString(), xsltFile, htmlFilePath.toOSString());
            copyConfigFile(CSS_FILE, outputDir);
            success = true;
            this.generatedFiles.put(OUTPUT_HTML_KEY, htmlFilePath);
        } else {
            throw new IllegalArgumentException("No XML file specified");
        }
        return success && picSuccess;
    }

    
    /**
     * Transforms the generated XML file to LaTeX and PDF output. If LaTeX output is generated
     * some more files need to be copied to the output directory. 
     * @param texOutput Specifies if LaTeX output should be generated. 
     * @param pdfOutput Specifies if PDF output should be generated. 
     * @param xsltFile The transformation file to use. 
     * @throws Exception
     */
    public void transformToLatexAndPdf(boolean texOutput, boolean pdfOutput, String xsltFile)
            throws Exception {
        
        IPath latexFilePath = this.XMLFilePath.removeFileExtension().addFileExtension("tex");
        IPath outputDir = this.XMLFilePath.removeLastSegments(1);
        xsltFile = xsltFile.replace(PLACEHOLDER_CONFIGDIR, 
                this.configfileDir.addTrailingSeparator().toOSString());
        transform(this.XMLFilePath.toOSString(), xsltFile, latexFilePath.toOSString());
        copyConfigFile(LATEX_PR_FILE, outputDir);
        copyConfigFile(LATEX_MLTDEF_FILE, outputDir);
        try {
            if (pdfOutput){
                String[] cmds = new String[]{
                		"bash",
                		"--login",
                        this.baseDir.addTrailingSeparator().append(
                                TOOLS_DIR+"tex2pdf.sh").toOSString(), 
                        latexFilePath.removeLastSegments(1).toOSString(),
                        latexFilePath.lastSegment() 
                        };
                
                try {
                	
                	
                    Runtime.getRuntime().exec(cmds,null).waitFor();
                    //Process p = Runtime.getRuntime().exec("cat test.tex", null,latexFilePath.removeLastSegments(1).toFile());
                    

                } catch (Exception e) {
                  e.printStackTrace();
                }
                //Runtime.getRuntime().exec(cmds, null, this.baseDir.toFile()).waitFor();
                IPath pdfFilePath = 
                    latexFilePath.removeFileExtension().addFileExtension("pdf");
                this.generatedFiles.put(OUTPUT_PDF_KEY, pdfFilePath);
            }
            if (!texOutput){
                latexFilePath.toFile().delete();
            } else {
                this.generatedFiles.put(OUTPUT_LATEX_KEY, latexFilePath);
            }
        } catch (Exception e) {
            throw new Exception(EXC_TEXT_PDFCONVERT, e);
        }
    }
    
    
    /**
     * Copies a file from the config directory to the output directory
     * @param filename Name of the file to copy.
     * @param outputDir Target directory.
     * @return true if the copy process ended successfully. 
     * @throws FileNotFoundException
     */
    private boolean copyConfigFile(String filename, IPath outputDir) 
            throws FileNotFoundException {
        
        boolean success = true;
        FileNotFoundException noFile = null;
        try {
            String filePath = 
                this.configfileDir.addTrailingSeparator().append(filename).toOSString();
            if (new File(filePath).exists()){
                String[] cmds = new String[]{
                        "cp", 
                        filePath, 
                        outputDir.toOSString()};

                Runtime.getRuntime().exec(cmds,
                        null, this.baseDir.toFile()).waitFor();
            } else {
                noFile = generateFNFException(filePath, 
                        "\nFile wasn't copied to output directory, the rest may be ok");
            }
        } catch (InterruptedException e) {
            success = false;
        } catch (IOException e) {
            success = false;
		}
        
        if (noFile != null){
            throw noFile;
        }
        return success;
    }
    
    
    public boolean deleteXMLFile(){
        boolean deleted = this.XMLFilePath.toFile().delete();
        if (deleted){
            this.generatedFiles.remove(OUTPUT_XML_KEY);
        }
        // delete schema file
        this.XMLFilePath.removeLastSegments(1).append(SCHEMAFILE_DEF).toFile().delete();
        return deleted;
    }

    
    /**
     * Writes a file with the specified content. 
     * @param content Content to write
     * @param outputFilePath Path to the file to generate
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static void writeToFile(String content, IPath outputFilePath) 
            throws FileNotFoundException, UnsupportedEncodingException, IOException {
        
        FileOutputStream os = new FileOutputStream(outputFilePath.toFile());
        OutputStreamWriter outputWriter = new OutputStreamWriter(os, XML_ENCODING);
        outputWriter.write(content);
        outputWriter.flush();
    }

    
    /**
     * Initializes the IDocument that is needed to export the file 
     * (and stores the configfileDir).
     * This is only necessary if there is no MLTutor instance running, which can provide a 
     * usable IDocument. 
     * @param inputFilePath File to export
     * @param configfileDir Directory where the config-files for the partitioner are stored in
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void initDocument(IPath inputFilePath, IPath configfileDir) 
            throws FileNotFoundException, IOException {
        
        File docFile = inputFilePath.toFile();
        if (docFile.canRead()){
            FileReader reader = new FileReader(docFile);
            StringBuffer content = new StringBuffer();
            char[] buffer = new char[1024];
            int readBytes = 0;
            while ((readBytes = reader.read(buffer)) != -1){
                content.append(buffer, 0, readBytes);
            }
            
            this.document = new Document(content.toString());
            IDocumentPartitioner partitioner =
                new FastPartitioner(
                        new MatlabPartitionScanner(configfileDir.toOSString()),
                        MatlabPartitionScanner.getConfiguredContentTypes());
            partitioner.connect(this.document);
            this.document.setDocumentPartitioner(partitioner);
        }
        this.configfileDir = configfileDir;
    }
    
    
    /**
     * Exports the content of the specified document into XML. 
     * This function should be used only from within a running eclipse instance. 
     * @param document Document to export
     * @param properties Export-properties
     * @param filetitle Title of the file, usually its name
     * @param XMLOutputFilePath Path to the XML-file to generate.
     *        If null, no file will be created.
     * @return XML content
     * @throws Exception 
     */
    public String export(IDocument document, Properties properties, 
            Properties metadata, IPath XMLOutputFilePath) 
            throws Exception {
        
        this.configfileDir = 
            Path.fromOSString(Activator.getDefault().getPluginDir(Activator.CONFIGDIR));
        this.baseDir = 
            Path.fromOSString(Activator.getDefault().getPluginDir(Activator.BASEDIR));
        this.document = document;
        return export(properties, metadata, XMLOutputFilePath);
    }

    
    /**
     * Exports the content of the specified document into XML. 
     * This function can be used if a document has already been initialized. 
     * @param properties Export-properties
     * @param filetitle Title of the file, usually its name
     * @param XMLOutputFilePath Path to the XML-file to generate. 
     *        If null, no file will be created.
     * @return Content of the XML-file
     * @throws Exception 
     */
    public String export(Properties properties, Properties metadata, IPath XMLOutputFilePath) 
            throws Exception {
        
        StringBuffer output = new StringBuffer();
        try {
            String schemaFile    = SCHEMAFILE_DEF;
//            if (properties != null){
//                schemaFile = properties.getProperty(SCHEMAFILE_KEY, SCHEMAFILE_DEF);
//            }

            output.append(XML_HEAD + "\n");
            output.append("<" + XML_ROOTTAG + "\n" +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                    "xsi:noNamespaceSchemaLocation=\"" +
                    schemaFile + "\">\n");

            // write metadata
            appendMetadata(output, metadata);
            
            // write content
            output.append("<" + XML_CONTENTTAG + ">\n");
            this.textMode       = MODE_NOMODE;
            this.currentModeTag = "";
            
            for (int currentLine = 0; currentLine < document.getNumberOfLines(); currentLine++){
                processLine(output, currentLine); 
            }
            
            output.append(changeMode(MODE_NOMODE));
            output.append("</" + XML_CONTENTTAG + ">\n");

            // write source
            output.append(genSingleLineTag(
                    XML_SOURCETAG, null, encloseWithCdata(document.get())));
            
            output.append("</" + XML_ROOTTAG + ">\n");

            // write to a file
            if (XMLOutputFilePath != null){
                try {
                    writeToFile(output.toString(), XMLOutputFilePath);
                    this.XMLFilePath = XMLOutputFilePath;
                    this.generatedFiles.put(OUTPUT_XML_KEY, this.XMLFilePath);
                    copyConfigFile(XMLExporter.SCHEMAFILE_DEF, XMLOutputFilePath.removeLastSegments(1));
                } catch (Exception e) {
                    throw new Exception(EXC_TEXT_XMLWRITE, e);
                }
            }
            
        } catch (BadLocationException e) {
            throw new Exception(EXC_TEXT_BADLOCATION, e);
        }
        return output.toString();
    }


    /**
     * Exports all variables in the given document into a String (XML-format)
     * and a file if requested.
     * @param document IDocument to export
     * @param metadata Some metadata
     * @param XMLOutputFilePath Path to the output-file, null if no file should be written
     * @return A XML document which contains all variables as a String
     * @throws IOException 
     */
    public String exportOnlyVars(IDocument document,  Properties metadata, IPath XMLOutputFilePath) 
            throws Exception {
        
        this.configfileDir = 
            Path.fromOSString(Activator.getDefault().getPluginDir(Activator.CONFIGDIR));
        this.baseDir = 
            Path.fromOSString(Activator.getDefault().getPluginDir(Activator.BASEDIR));
        this.document = document;
        return exportOnlyVars(metadata, XMLOutputFilePath);
    }

    
    /**
     * Exports all variables in the current document into a String (XML-format)
     * and a file if requested.
     * How to search for variables:
     *   Divide document into lines
     *   Divide each line into commands which are separated by ';'
     *   every string which is a valid variable name on the left side of a '=' is a variable
     * @param metadata Some metadata
     * @param XMLOutputFilePath Path to the output-file, null if no file should be written
     * @return A XML document which contains all variables as a String
     * @throws Exception 
     */
    public String exportOnlyVars(Properties metadata, IPath XMLOutputFilePath) 
            throws Exception {
        
        MarkupRegex varRegex = new MarkupRegex("\\s*([a-zA-Z][\\w_]*)(\\(.*\\))?\\s*=.*", null);
        
        StringBuffer output = new StringBuffer();
        output.append(XML_HEAD + "\n");
        /*            output.append("<" + XML_ROOTTAG + "\n" +
                            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                            "xsi:noNamespaceSchemaLocation=\"" +
                            schemaFile + "\">\n");
        */
        output.append("<" + XML_ROOTTAG + ">\n");
        
        // write metadata
        appendMetadata(output, metadata);
        
        // write content
        output.append("<" + XML_VARLISTTAG + ">\n");
        
        try {
            // used to avoid listing a variable twice
            ArrayList<String> foundVars = new ArrayList<String>();

            int numLines = this.document.getNumberOfLines();
            for (int currentLineNum = 0; currentLineNum < numLines; currentLineNum++){
                IRegion lineInfo = this.document.getLineInformation(currentLineNum);
                String currentLine = this.document.get(lineInfo.getOffset(), lineInfo.getLength());
                String[] commands = currentLine.split(";");
                for (int i = 0; i < commands.length; i++) {
                    String[] match = varRegex.match(commands[i]);
                    if (match != null){
                        String var = match[0];

                        // avoid to list a variable twice
                        if (!foundVars.contains(var)){
                            output.append(genSingleLineTag(XML_VARTAG, null, var));
                            foundVars.add(var);
                        }
                    }
                }
            }
        } catch (BadLocationException e) {
            throw new Exception(EXC_TEXT_BADLOCATION, e);
        }
        output.append("</" + XML_VARLISTTAG + ">\n");
        output.append("</" + XML_ROOTTAG + ">\n");
        
        // write to a file
        if (XMLOutputFilePath != null){
            try {
                writeToFile(output.toString(), XMLOutputFilePath);
                this.XMLFilePath = XMLOutputFilePath;
            } catch (Exception e) {
                throw new Exception(EXC_TEXT_XMLWRITE, e);
            }
        }
        return output.toString();
    }
    
    
    /**
     * Exports all Matlab functions used used in the given document into a String (XML-format)
     * and a file if requested.
     * @param document IDocument to export
     * @param metadata Some metadata
     * @param XMLOutputFilePath Path to the output-file, null if no file should be written
     * @return An XML document that contains all functions as a String
     * @throws IOException 
     */
    public String exportOnlyFuncs(IDocument document,  Properties metadata, IPath XMLOutputFilePath) 
            throws Exception {
        
        this.configfileDir = 
            Path.fromOSString(Activator.getDefault().getPluginDir(Activator.CONFIGDIR));
        this.baseDir = 
            Path.fromOSString(Activator.getDefault().getPluginDir(Activator.BASEDIR));
        this.document = document;
        return exportOnlyFuncs(metadata, XMLOutputFilePath);
    }

    
    /**
     * Exports all used Matlab functions in the given document into a String (XML-format)
     * and a file if requested.
     * The search is based on the partitioning of the document. 
     * @param metadata Some metadata
     * @param XMLOutputFilePath Path to the output-file, null if no file should be written
     * @return An XML document that contains all functions as a String
     * @throws Exception 
     */
    public String exportOnlyFuncs(Properties metadata, IPath XMLOutputFilePath) 
            throws Exception {
        
        StringBuffer output = new StringBuffer();
        output.append(XML_HEAD + "\n");
        /*            output.append("<" + XML_ROOTTAG + "\n" +
                            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                            "xsi:noNamespaceSchemaLocation=\"" +
                            schemaFile + "\">\n");
        */
        output.append("<" + XML_ROOTTAG + ">\n");
        
        // write metadata
        appendMetadata(output, metadata);
        
        // write content
        output.append("<" + XML_FUNCLISTTAG + ">\n");
        
        try {
            ITypedRegion[] partitions = 
                this.document.computePartitioning(0, this.document.getLength());
            
            // used to avoid listing a function twice
            ArrayList<String> foundFuncs = new ArrayList<String>();
            
            for (ITypedRegion currentPartition:partitions){
                if (currentPartition.getType().equals(MatlabPartitionScanner.MATLAB_FUNCTION) ||
                        currentPartition.getType().equals(MatlabPartitionScanner.TOOLBOX_FUNCTION)){
                    int offset = currentPartition.getOffset();
                    int length = currentPartition.getLength();
                    String func = document.get(offset, length);

                    // avoid to list a function twice
                    if (!foundFuncs.contains(func)){
                        output.append(genSingleLineTag(XML_FUNCTAG, null, func));
                        foundFuncs.add(func);
                    }
                }
            }
        } catch (BadLocationException e) {
            throw new Exception(EXC_TEXT_BADLOCATION, e);
        }
        output.append("</" + XML_FUNCLISTTAG + ">\n");
        output.append("</" + XML_ROOTTAG + ">\n");
        
        // write to a file
        if (XMLOutputFilePath != null){
            try {
                writeToFile(output.toString(), XMLOutputFilePath);
                this.XMLFilePath = XMLOutputFilePath;
            } catch (Exception e) {
                throw new Exception(EXC_TEXT_XMLWRITE, e);
            }
        }

        return output.toString();
    }

    
    /**
     * Creates a directory and exports all equations as png-pics into this directory.
     * One of the export-functions must be executed before this one, because there the 
     * equations are found and stored. 
     * If no equations have been found, no directory will be created.
     * @param outputDir XML-output directory
     * @param filetitle Title of the file, usually its name
     * @throws IOException 
     */
    public void exportPics(IPath outputDir, String filetitle) throws IOException{
        if (outputDir != null){
            if (this.equations.size()>0){
                IPath picDir = outputDir.addTrailingSeparator().append(
                        filetitle + PICDIR_EXT).addTrailingSeparator();
                picDir.toFile().mkdir();

                for (int piccount = 0; piccount < this.equations.size(); piccount++){
                    latexToPic(this.equations.get(piccount), 
                            this.configfileDir.addTrailingSeparator().append(LATEX_PIC_TEMPLATE), 
                            picDir, 
                            piccount+1);
                }
            }
        } else {
            throw new IllegalArgumentException("No outputdir specified");
        }
    }
    
    
    private void processLine(StringBuffer output, int currentLine) 
    throws BadLocationException{

        int lineOffset = this.document.getLineOffset(currentLine);
        int lineLength = this.document.getLineLength(currentLine);
        ITypedRegion[] partitions = document.computePartitioning(lineOffset, lineLength);
        
        Vector<TextInfo> lineParts = new Vector<TextInfo>();
        int currentLineMode = MODE_NOMODE;
        
        String oldEnumerationTag = this.currentEnumerationTag; 
        this.currentEnumerationTag = NO_LISTTAG;
        if (oldEnumerationTag.equals(NO_LISTTAG)){
            this.currentListitemNumber = 1;
        }
        
        // collect line information
        generateLineInfo(partitions, lineParts, currentLineMode);
                      
        // write collected information to output
        appendLineToOutput(output, lineParts, oldEnumerationTag, currentLine);
    }
    
    
    private int generateLineInfo(ITypedRegion[] partitions, 
        Vector<TextInfo> lineParts, int currentLineMode) 
        throws BadLocationException {
    
        TextInfo linePartInfo = null;
        for (ITypedRegion currentPartition:partitions){
            TextInfo handledPartitionInfo = handlePartition(currentPartition);
            if (handledPartitionInfo != null){
                if (handledPartitionInfo.mode == currentLineMode){
                    linePartInfo.mode = handledPartitionInfo.mode;
                    linePartInfo.content = linePartInfo.content + handledPartitionInfo.content;
                } else {
                    if (linePartInfo!=null){
                        lineParts.add(linePartInfo);
                    }
                    linePartInfo = new TextInfo();
                    linePartInfo.mode = handledPartitionInfo.mode;
                    linePartInfo.content = handledPartitionInfo.content;
                    currentLineMode = handledPartitionInfo.mode;
                }
            }
        }
        if (linePartInfo!=null){
            lineParts.add(linePartInfo);
        }
        return currentLineMode;
    }
    
    
    private void appendLineToOutput(
        StringBuffer output, Vector<TextInfo> lineParts, 
        String oldEnumerationTag, int currentLine) {
    
        String xmlLineEndTag   = XML_INDENT_STR + "</" + XML_LINETAG + ">\n";
        String xmlLineStartTag = XML_INDENT_STR + "<" + XML_LINETAG + 
            " " + XML_LINENUMBERARG + "=\"" + (currentLine+1) + "\">\n";
        
        for (TextInfo linePart : lineParts) {
            
            // write opening and closing (markup) list tags 
            if (!this.currentEnumerationTag.equals(oldEnumerationTag)){
                String enumerationTags = "";
                if (!oldEnumerationTag.equals(NO_LISTTAG)){
                    enumerationTags = "</" + oldEnumerationTag + ">";
                }
                if (!this.currentEnumerationTag.equals(NO_LISTTAG)){
                    enumerationTags = enumerationTags + "<" + this.currentEnumerationTag + ">";
                }
                output.append(enumerationTags);
            }
        
            output.append(changeMode(linePart.mode));
            if (linePart.mode == MODE_CODE){
                output.append(xmlLineStartTag);
                output.append(linePart.content);
                output.append(xmlLineEndTag);
            } else {
                output.append(linePart.content);
            }
        }
    }


    /**
     * Appends something to output, depending on currentPartition.
     * @param output XML-text that contains the partitioning up to the current partition
     * @param currentPartition Partition to examine. 
     * @throws BadLocationException
     */
    private TextInfo handlePartition(ITypedRegion currentPartition) 
            throws BadLocationException {
        
        TextInfo partitionInfo = new TextInfo();
        
        // get needed information
        String type = currentPartition.getType();
        int offset  = currentPartition.getOffset();
        int length  = currentPartition.getLength();
        String content = "";
        String args = genArgString(XML_PARTITIONTYPEARG, type);
        partitionInfo.mode    = MODE_CODE;
        partitionInfo.content = content;
        
        // decide what to do
        if (length==0) {  // this happens if there is an empty line at the end of the file
            return partitionInfo;
        }
        
        if (type.equals(MatlabPartitionScanner.MATLAB_NEWLINE)){
            if (this.skipNewline){
                this.skipNewline = false;
                return null;
            }
        } else if (type.equals(MatlabPartitionScanner.MATLAB_WHITESPACE)){
            content = this.document.get(offset, length)
                    .replace(" ", CONTENT_SPACE_STR)
                    .replace("\t", CONTENT_TAB_STR);
        } else if (type.equals(MatlabPartitionScanner.MATLAB_COMMENT)){
            String docContent = this.document.get(offset, length);
            content = parseComment(docContent);
            if (!content.equals(docContent)){
                this.skipNewline = true;
                partitionInfo.mode = MODE_MARKUP;
            }
        } else {
            content = encloseWithCdata(this.document.get(offset, length));
        }
                
        // generate XML-content
        String indentation = XML_INDENT_STR + XML_INDENT_STR;
        if (this.skipNewline){
            indentation = "";
        }
        if (partitionInfo.mode == MODE_CODE){
            if (!type.equals(MatlabPartitionScanner.MATLAB_NEWLINE)){
                partitionInfo.content = indentation + 
                        genSingleLineTag(XML_PARTITIONTAG, args, content);
            } else {
                partitionInfo.content = content;
            }
        } else {
            partitionInfo.content = indentation + content;
        }
        return partitionInfo;
    }
    
    
    /**
     * Parses the specified text for markup-parts and transforms them to XML.
     * Regexes for finding markups and there corresponding tags are stored in regexList. 
     * If one regex matches, the capturing groups define how the content changes. 
     * If there are at least two cg's, all in the first cg will be replaced by the content 
     * of the second one, after it has been enclosed with the correct tag. If there is only one cg, 
     * it will be replaced by an empty tag. 
     * Further equations are stored in an array for later transformations. 
     * @param content Text to examine
     * @return Transformed text, equals input if no markup was found. 
     */
    private String parseComment(String content) {
        for (int regexIndex = 0; regexIndex < this.regexList.size(); regexIndex++){
            MarkupRegex regex = this.regexList.get(regexIndex);
            String[] markupContent = regex.match(content);
            if (markupContent != null){
                String replacement = "";
                if (markupContent.length >= 2){
                    String args = null;
                    String coreContent = markupContent[1];
                    if (regex.getTag().equals(XML_M_CEQUATIONTAG)){
                        // store all equations
                        this.equations.add(
                                "\\begin{equation*}" + coreContent + "\\end{equation*}");
                        // set equation number
                        args = genArgString(XML_M_EQUATIONNUMARG, this.equations.size());
                        // to avoid problems with <, >, &
                        coreContent = encloseWithCdata(coreContent);
                    } else if (regex.getTag().equals(XML_M_NUMBEREDLISTITEMTAG)){
                        args = genArgString(XML_M_ITEMNUMBERARG, this.currentListitemNumber);
                        this.currentListitemNumber++;
                    }
                    replacement = genSingleLineTag(regex.getTag(), args, coreContent, false);
                } else if (markupContent.length == 1) {
                    replacement = "<" + regex.getTag() + " />";
                }
                if (regex.useNewline()){
                    replacement = replacement + "\n" + XML_INDENT_STR;
                }
                
                if(isEnumerationItemTag(regex.getTag())){
                    this.currentEnumerationTag = this.listingTags.get(regex.getTag());
                } else {
                    this.currentEnumerationTag = NO_LISTTAG;
                }

                content = content.replace(markupContent[0], replacement);
                regexIndex--; // to look if current regex may be found once more
            }
        }
        return content;
    }
    
    
    /**
     * Appends the metadata to the the given buffer (in a XML-format)
     * @param output Buffer to use
     * @param metadata Data to append
     */
    private void appendMetadata(StringBuffer output, Properties metadata) {
        String filetitle    = FILETITLE_DEF;
        String author_name  = AUTHOR_NAME_DEF;
        String author_email = AUTHOR_EMAIL_DEF;
        String author_www   = AUTHOR_WWW_DEF;
        String file_title   = FILE_TITLE_DEF;
        String file_content = FILE_CONTENT_DEF;
        String file_date    = FILE_DATE_DEF;
        String file_type    = FILE_TYPE_DEF;
        if (metadata != null){
            filetitle    = metadata.getProperty(FILETITLE_KEY, FILETITLE_DEF);
            author_name  = metadata.getProperty(AUTHOR_NAME_KEY, AUTHOR_NAME_DEF);
            author_email = metadata.getProperty(AUTHOR_EMAIL_KEY, AUTHOR_EMAIL_DEF);
            author_www   = metadata.getProperty(AUTHOR_WWW_KEY, AUTHOR_WWW_DEF);
            file_title   = metadata.getProperty(FILE_TITLE_KEY, FILE_TITLE_DEF);
            file_content = metadata.getProperty(FILE_CONTENT_KEY, FILE_CONTENT_DEF);
            file_date    = metadata.getProperty(FILE_DATE_KEY, FILE_DATE_DEF);
            file_type    = metadata.getProperty(FILE_TYPE_KEY, FILE_TYPE_DEF);
        }
        output.append(genSingleLineTag(XML_FILENAMETAG, null, filetitle));
        output.append("<" + XML_AUTHORTAG + ">\n");
        appendTag(output, XML_AUTHORNAMETAG, author_name);
        appendTag(output, XML_AUTHOREMAILTAG, author_email);
        appendTag(output, XML_AUTHORWWWTAG, author_www);
        output.append("</" + XML_AUTHORTAG + ">\n");

        output.append("<" + XML_FILETAG + ">\n");
        appendTag(output, XML_FILETITLETAG, file_title);
        appendTag(output, XML_FILEDATETAG, file_date);
        appendTag(output, XML_FILECONTENTTAG, file_content);
        appendTag(output, XML_FILETYPETAG, file_type);
        output.append("</" + XML_FILETAG + ">\n");
    }

    
    private void appendTag(StringBuffer output, String tag, String content) {
        if (!content.equals("")){
            output.append(genSingleLineTag(tag, null, content));
        }
    }

    
    /**
     * Changing mode means writing an endtag for the old mode and a starttag for the new one.
     * The text with the correct tags is returned and the new mode is stored. 
     * @param newMode Mode to switch into
     * @return Text that can be used in the XML-file. 
     */
    private String changeMode(int newMode){
        StringBuffer output = new StringBuffer();
        if (newMode != this.textMode){
            if (this.textMode != MODE_NOMODE){
                if (this.textMode == MODE_MARKUP){
                    output.append("\n");
                }
                output.append("</" + this.currentModeTag + ">\n");
            }
            switch (newMode){
            case MODE_NOMODE:
                break;
            case MODE_CODE:
                output.append("<" + XML_PARTITIONINGTAG + ">\n");
                this.currentModeTag = XML_PARTITIONINGTAG;
                break;
            case MODE_MARKUP:
                output.append("<" + XML_MARKUPTAG + ">\n"+XML_INDENT_STR);
                this.currentModeTag = XML_MARKUPTAG;
                break;
            }
            this.textMode = newMode;
        }
        return output.toString();
    }
    
    
    private String encloseWithCdata(String content){
        return "<![CDATA[" + content + "]]>";
    }

    /**
     * Generates: opening tag with arguments, then content, a closing tag 
     * and after all a newline.
     * @param tag Tagname
     * @param args Arguments to include, null for no args
     * @param content The element's content
     * @return The constructed tag as String. 
     */
    public static String genSingleLineTag(String tag, String args, String content){
        return genSingleLineTag(tag, args, content, true);
    }
    
    
    /**
     * Generates: opening tag with arguments, then content, a closing tag 
     * and a newline if wanted.
     * @param tag Tagname
     * @param args Arguments to include, null for no args
     * @param content The element's content
     * @param newline Tells if a newline after the tag should be included
     * @return The constructed tag as String. 
     */
    public static String genSingleLineTag(
            String tag, String args, String content, boolean newline){
        
        StringBuffer output = new StringBuffer();
        output.append("<" + tag); 
        if (args != null){
            output.append(" " + args);
        }
        output.append(">");
        output.append(content);
        output.append("</" + tag + ">");
        if (newline){
            output.append("\n");
        }
        return output.toString();
    }
    
    
    /**
     * Generates: a string that can be used as argument in XML/HTML tags. 
     * It's formatting is:    name="value"
     * @param name Specifies the argument
     * @param value The argument's value
     * @return Generated String
     */
    public static String genArgString(String name, String value){
        return name + "=\"" + value + "\"";
    }
    
    /**
     * Generates: a string that can be used as argument in XML/HTML tags. 
     * It's formatting is:    name="value"
     * @param name Specifies the argument
     * @param value The argument's value
     * @return Generated String
     */
    public static String genArgString(String name, int value){
        return name + "=\"" + value + "\"";
    }

    /**
     * Checks if the provided tag is one of the known tags for items of any kind of lists.
     * @param tag Tag to check
     * @return true if provided tag is a known list item tag
     */
    private boolean isEnumerationItemTag(String tag){
        return (tag.equals(XML_M_LISTITEMTAG) || tag.equals(XML_M_NUMBEREDLISTITEMTAG));
    }
        
    
    /**
     * Generates a png-pic from a given string in LaTeX syntax
     * @param latex LaTeX code to transform
     * @param templateFilePath Path to a LaTeX template file the string to transform
     *        can be inserted to
     * @param picOutputDir Directory where the resulting pic should be stored. 
     *        Some temporary files will be stored there too. 
     * @param picNum Number of the pic, will be contained in its filename
     * @throws IOException
     * @throws InterruptedException
     */
    private void latexToPic(String latex, IPath templateFilePath, 
            IPath picOutputDir, int picNum) 
            throws IOException{
        
        IPath tempfileName = picOutputDir.addTrailingSeparator().append("temp");
        
        // get the whole LaTeX text
        RandomAccessFile templateFile = new RandomAccessFile(templateFilePath.toFile(), "r");
        byte[] buffer = new byte[(int)templateFile.length()];
        templateFile.read(buffer);
        String latexFileContent = new String(buffer);
        latexFileContent = latexFileContent.replace(LATEX_REP_STRING, latex);
        
        // write temporary LaTeX file
        FileOutputStream texFile = 
            new FileOutputStream(tempfileName.addFileExtension("tex").toFile());
        texFile.write(latexFileContent.getBytes());
        texFile.flush();
        
        // do the transformation
        IPath picPath = picOutputDir.append(String.valueOf(picNum)).addFileExtension("png");
        String[] cmds = new String[]{
                "bash", 
                "--login",
                this.baseDir.addTrailingSeparator().append(TOOLS_DIR+"tex2png.sh").toOSString(), 
                picOutputDir.toOSString(), 
                picPath.toOSString()
                };
        try {
            Runtime.getRuntime().exec(cmds, null, this.baseDir.toFile()).waitFor();
        } catch (InterruptedException e) {
            // do nothing
        }
    }
    
    /**
     * Executes an XSL-transformation
     * @param xmlFilePath XML-file to transform
     * @param xslFilePath File that specifies the transformation
     * @param outputFilePath Name of the file to generate
     * @throws FileNotFoundException
     * @throws TransformerException
     */
    public static void transform(String xmlFilePath, String xslFilePath, String outputFilePath) 
    throws FileNotFoundException, TransformerException{
        
        TransformerFactory tFactory = TransformerFactory.newInstance();
        File xslFile = new File(xslFilePath);
        if (!xslFile.exists()){
            throw generateFNFException(xslFile.getAbsolutePath(), "");
        }
        File xmlFile = new File(xmlFilePath);
        if (!xmlFile.exists()){
            throw generateFNFException(xmlFile.getAbsolutePath(), "");
        }
        Transformer transformer = tFactory.newTransformer(new StreamSource(xslFile));
        // TODO ueberpruefen ob transformer==null (sein kann) 
        // bzw. was passiert wenn xsl-Format nicht passt
        transformer.transform(new StreamSource(xmlFilePath), 
                new StreamResult(new FileOutputStream(outputFilePath)));
    }
    
    
    /**
     * Extracts the correct transformation files from the properties. 
     * @param exportProperties Properties that contain the needed information. 
     * @return Array with the correct transformation files as saved in the properties.
     *         The first entry contains the HTML transformation file, 
     *         the second entry the LaTeX transformation file. 
     */
    public static String[] extractXsltFiles(Properties exportProperties){
        String[] xsltFiles = new String[2];

        boolean lines = Boolean.parseBoolean(exportProperties.getProperty(
                OUTPUT_LINENUM_KEY, OUTPUT_LINENUM_DEF));
        boolean coreonly = Boolean.parseBoolean(exportProperties.getProperty(
                OUTPUT_COREONLY_KEY, Boolean.toString(OUTPUT_COREONLY_DEF)));
        
        String latexFile, latexFileLines;
        if (coreonly){
            latexFile = exportProperties.getProperty(
                    XSLT_LATEX_C_FILE_KEY, XSLT_LATEX_C_FILE_DEF);
            latexFileLines = exportProperties.getProperty(
                    XSLT_LATEX_CL_FILE_KEY, XSLT_LATEX_CL_FILE_DEF);
        } else {
            latexFile = exportProperties.getProperty(
                    XSLT_LATEX_FILE_KEY, XSLT_LATEX_FILE_DEF);
            latexFileLines = exportProperties.getProperty(
                    XSLT_LATEX_L_FILE_KEY, XSLT_LATEX_L_FILE_DEF);
        }

        if (lines){
            xsltFiles[0] = exportProperties.getProperty(
                    XSLT_HTML_L_FILE_KEY, XSLT_HTML_L_FILE_DEF);
            xsltFiles[1] = latexFileLines;
        } else {
            xsltFiles[0] = exportProperties.getProperty(
                    XSLT_HTML_FILE_KEY, XSLT_HTML_FILE_DEF);
            xsltFiles[1] = latexFile;
        }
        return xsltFiles;
    }
    
    
    /**
     * If an Exception that is not fatal enough to stop the export occurred, it will 
     * be saved and can be requested by this function. 
     * A call to this function also deletes the stored Exception!
     * @return The stored Exception, if none occurred null. 
     */
    public Exception fetchException(){
        Exception tempException = this.occuredException;
        this.occuredException = null;
        return tempException;
    }
    
    
    private static FileNotFoundException generateFNFException(String file, 
            String additionalMessage){
        
        return new FileNotFoundException(
                file + " (No such file or directory)" + additionalMessage);
    }
    
    
    public HashMap<String, IPath> getGeneratedFiles(){
        return this.generatedFiles;
    }

    
    /**
     * Helper class for storing data and processing simple routines 
     * concerning the markup language in comments.
     * This class stores a regex-pattern and a tag, which is associated with this pattern.
     * @author Georg Huhs
     */
    private class MarkupRegex {
        private Pattern pattern;
        private String tag;
        private boolean newline;
        
        public MarkupRegex(String patternString, String tag){
            this.pattern = Pattern.compile(patternString);
            this.tag = tag;
        }
        
        public MarkupRegex(String patternString, String tag, boolean newline){
            this.pattern = Pattern.compile(patternString);
            this.tag = tag;
            this.newline = newline;
        }
        /**
         * Tests if this regex matches the specified String. 
         * If yes, all capturing groups are returned. 
         * @param content String to test. 
         * @return Capturing groups as array, starting with the first one. 
         *         Null if not matching. 
         */
        public String[] match(String content){
            String[] data = null;
            Matcher matcher = this.pattern.matcher(content);
            if (matcher.matches()){
                int groups = matcher.groupCount();
                data = new String[groups];
                for (int groupIndex = 0; groupIndex < groups; groupIndex++){
                    data[groupIndex] = matcher.group(groupIndex+1);
                }
            }
            return data;
        }
        
        public String getTag(){
            return this.tag;
        }
        
        public String toString(){
            return "Tag: " + this.tag + "   pattern: " + this.pattern.toString();
        }
        
        public boolean useNewline(){
            return this.newline;
        }
    }
    
    
    /**
     * Helper class for storing information about parts of a text. 
     * @author Georg Huhs
     */
    private class TextInfo{
        
        int mode;
        String content;

        public TextInfo(){
           this.mode = MODE_NOMODE;
           this.content = null;
        }

        @Override
        public String toString() {
            StringBuffer retString = new StringBuffer();
            switch (this.mode){
            case MODE_NOMODE:
                retString.append("MODE_NOMODE");
                break;
            case MODE_MARKUP:
                retString.append("MODE_MARKUP");
                break;
            case MODE_CODE:
                retString.append("MODE_CODE  ");
                break;
            default:
                retString.append("unkn. mode ");
            }
            retString.append(" |  " + this.content);
            return retString.toString();
        }
    }
    
}
