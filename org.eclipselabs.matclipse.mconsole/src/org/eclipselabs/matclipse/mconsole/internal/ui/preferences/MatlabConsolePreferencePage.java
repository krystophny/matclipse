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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;


public class MatlabConsolePreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public MatlabConsolePreferencePage() {
		super(GRID);
		setPreferenceStore(MConsolePlugin.getDefault().getPreferenceStore());
		setDescription("General Matlab Console Settings");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		String[][] matlab = { { "matlab", "matlab" },
				{ "octave (experimental)", "octave" } };
		addField(new DirectoryFieldEditor(IPreferenceConstants.P_MATLABPATH,
				"&Path to matlab executable ($MATLABPATH/bin):", parent));
		addField(new ComboFieldEditor(IPreferenceConstants.P_MATLABINTERPRETER,
				"Matlab Interpreter:", matlab, parent));
		addField(new BooleanFieldEditor(
				IPreferenceConstants.P_REMEMBERCOMMANDHISTORY,
				"Remember CommandHistory", getFieldEditorParent()));
		addField(new BooleanFieldEditor(IPreferenceConstants.P_CDFEEDBACK,
				"Show Directory Change Feedback in Console", parent));
		addField(new BooleanFieldEditor(IPreferenceConstants.P_AUTOMATICCD,
				"Automatically change directory to selected resource", parent));
		addField(new BooleanFieldEditor(
				IPreferenceConstants.P_AUTOMATICCDEDITOR,
				"Automatically change directory to selected Matlab Editor",
				parent));
		addField(new BooleanFieldEditor(IPreferenceConstants.P_SHRINKOUTPUT,
				"Shrink matlab output in workspace view", parent));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
