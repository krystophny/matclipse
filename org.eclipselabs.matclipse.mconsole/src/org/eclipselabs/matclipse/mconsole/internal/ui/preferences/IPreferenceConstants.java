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

/**
 * Constant definitions for plug-in preferences
 */
public interface IPreferenceConstants {

	public static String ID = "org.eclipselabs.matclipse.mconsole.";

	public static String P_MATLABPATH = ID+"matlabPath";
	public static String P_COMMANDHISTORY = ID+"McommandHistory";
	public static String P_REMEMBERCOMMANDHISTORY = ID+"rememberCommandHistory";
	public static String P_CDFEEDBACK = ID+"feedBackCdEvents";
	public static String P_AUTOMATICCD = ID+"automaticDirectoryChange";
	public static String P_AUTOMATICCDEDITOR = ID+"automaticDirectoryChangeEditor";
	public static String P_MATLABINTERPRETER = ID+"matlabInterpreter";
	public static String P_SHRINKOUTPUT = ID+"shrinkoutput";
}
