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
package org.eclipselabs.matclipse.mconsole.matlab.impl;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipselabs.matclipse.mconsole.matlab.MOutputParser;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabCommunicationException;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabInterpreter;
import org.eclipselabs.matclipse.mconsole.matlab.MatlabNotStartedException;


public class OctaveInterpreter implements MatlabInterpreter {

	public void refreshWorkspaceView() {
		// TODO Auto-generated method stub

	}

	public String getMatlabPwd() throws MatlabCommunicationException,
			MatlabNotStartedException {
		// TODO Auto-generated method stub
		return null;
	}

	public void changeMatlabDirectory(String path, boolean b)
			throws MatlabCommunicationException, MatlabNotStartedException {
		// TODO Auto-generated method stub

	}

	public String eval(String cmd, boolean b, boolean c, boolean d)
			throws MatlabNotStartedException, MatlabCommunicationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String eval(String command) throws MatlabCommunicationException,
			MatlabNotStartedException {
		// TODO Auto-generated method stub
		return null;
	}

	public String eval(String string, boolean b, boolean c)
			throws MatlabCommunicationException, MatlabNotStartedException {
		return "bla";
	}

	public void evalNoOutput(String command)
			throws MatlabCommunicationException, MatlabNotStartedException {
		// TODO Auto-generated method stub

	}

	public void evalInConsole(String command, boolean b)
			throws MatlabCommunicationException, MatlabNotStartedException {
		// TODO Auto-generated method stub

	}

	public MOutputParser MatlabMLint(String osString)
			throws MatlabCommunicationException, MatlabNotStartedException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isMatlabAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	public void changeMatlabDirectoryToResource(IResource resource) {
		// TODO Auto-generated method stub

	}

	public void outputDirectoryChangeError(Throwable throwable) {
		// TODO Auto-generated method stub

	}

	public void start() throws CoreException {
		// TODO Auto-generated method stub

	}

	public void stop() {
		// TODO Auto-generated method stub

	}

	public void changeMatlabDirectoryToPath(String dirtochange) {
		// TODO Auto-generated method stub

	}

	public void sendBreak() throws IOException {
		// TODO Auto-generated method stub

	}

	public void setLogging(boolean b) {
		// TODO Auto-generated method stub

	}

	public void outputBusyError(Throwable throwable) {
		// TODO Auto-generated method stub

	}

	public void outputNotStartedError(Throwable throwable) {
		// TODO Auto-generated method stub

	}

	public ArrayList<String> getLocalVarNames()
			throws MatlabCommunicationException, MatlabNotStartedException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<String> getGlobalVarNames()
			throws MatlabCommunicationException, MatlabNotStartedException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<MOutputParser> getLocalVars()
			throws MatlabCommunicationException, MatlabNotStartedException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<MOutputParser> getGlobalVars()
			throws MatlabCommunicationException, MatlabNotStartedException {
		// TODO Auto-generated method stub
		return null;
	}

	public MOutputParser getMatlabVarXML(String string)
			throws MatlabNotStartedException, MatlabCommunicationException {
		// TODO Auto-generated method stub
		return null;
	}

	public void changeMatlabDirectoryNoOutput(String solutiondir)
			throws MatlabCommunicationException, MatlabNotStartedException {
		// TODO Auto-generated method stub

	}

}
