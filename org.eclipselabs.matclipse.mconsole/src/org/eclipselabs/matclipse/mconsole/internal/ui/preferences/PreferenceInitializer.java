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

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = MConsolePlugin.getDefault().getPreferenceStore();
		store.setDefault(IPreferenceConstants.P_MATLABPATH, "/usr/local/bin");
		store.setDefault(IPreferenceConstants.P_MATLABINTERPRETER, "matlab");
		store.setDefault(IPreferenceConstants.P_COMMANDHISTORY,"Default value");
		store.setDefault(IPreferenceConstants.P_REMEMBERCOMMANDHISTORY,true);
		store.setDefault(IPreferenceConstants.P_CDFEEDBACK,false);
		store.setDefault(IPreferenceConstants.P_AUTOMATICCD,true);
		store.setDefault(IPreferenceConstants.P_AUTOMATICCDEDITOR,true);
		store.setDefault(IPreferenceConstants.P_SHRINKOUTPUT,true);
	}

}
