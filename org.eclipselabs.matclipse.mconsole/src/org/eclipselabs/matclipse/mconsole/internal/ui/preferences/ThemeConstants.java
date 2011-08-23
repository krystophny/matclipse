/*******************************************************************************
 * Copyright (c) 2006, 2011 Graz University of Technology,
 * Institute of Theoretical and Computational Physics (ITPCP) 
 *
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Camhy, Winfried Kernbichler, Georg Huhs (ITPCP) - 
 *        initial API and implementation
 *     Christopher Albert (ITPCP) - refactoring
 *******************************************************************************/
package org.eclipselabs.matclipse.mconsole.internal.ui.preferences;

public interface ThemeConstants {
    String ID = "org.eclipselabs.matclipse.mconsole";
 
    String CONSOLE_FONT = ID + ".font.consolefont";
    String CONSOLE_TEXT_COLOR = ID + ".color.text";
    String CONSOLE_TEXTINPUT_COLOR= ID+ ".color.input";
    String CONSOLE_TEXTERROR_COLOR= ID+ ".color.error";
    String CONSOLE_BACKGROUND_COLOR = ID + ".color.consolebackground";
    String CONSOLE_INPUT_FONT = ID + ".font.inputconsolefont";
    String CONSOLE_DIRCHOOSER_WSCOLOR = ID+".color.consoledirchooser";
    String WORKSPACE_FONT = ID+ ".font.workspacefont";
    String WORKSPACE_TEXT_COLOR = ID + ".color.workspacetext";
    String WORKSPACE_BACKGROUND_COLOR=ID + ".color.workspacebackground";
    String WORKSPACE_TREE_FONT = ID+ ".font.workspacetreefont";
    String COMMANDHISTORY_BACKGROUND_COLOR=ID + ".color.commandhistorybackground";
    String COMMANDHISTORY_FOREGROUND_COLOR=ID + ".color.commandhistoryforegorund";
    String COMMANDHISTORY_FONT=ID+".font.commandhistoryfont";
    String COMMANDHISTORY_DATE_COLOR=ID + ".color.commandhistorydatecolor";
    String PROJECTSVIEW_TREE_FONT=ID+".font.matlabprojectsview";
}
