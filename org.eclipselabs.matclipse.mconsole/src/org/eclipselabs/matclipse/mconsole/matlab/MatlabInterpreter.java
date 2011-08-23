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
package org.eclipselabs.matclipse.mconsole.matlab;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public interface MatlabInterpreter {

	String getMatlabPwd() throws MatlabCommunicationException,
			MatlabNotStartedException;

	void changeMatlabDirectory(String path, boolean b)
			throws MatlabCommunicationException, MatlabNotStartedException;

	String eval(String cmd, boolean b, boolean c, boolean d)
			throws MatlabNotStartedException, MatlabCommunicationException;

	String eval(String command) throws MatlabCommunicationException,
			MatlabNotStartedException;

	String eval(String string, boolean b, boolean c)
			throws MatlabCommunicationException, MatlabNotStartedException;

	void evalNoOutput(String command) throws MatlabCommunicationException,
			MatlabNotStartedException;

	void evalInConsole(String command, boolean b)
			throws MatlabCommunicationException, MatlabNotStartedException;

	MOutputParser MatlabMLint(String osString)
			throws MatlabCommunicationException, MatlabNotStartedException;

	boolean isMatlabAvailable();

	void changeMatlabDirectoryToResource(IResource resource);

	void outputDirectoryChangeError(Throwable throwable);

	void start() throws CoreException;

	void stop();

	void changeMatlabDirectoryToPath(String dirtochange);

	void sendBreak() throws IOException;

	void setLogging(boolean b);

	void outputBusyError(Throwable throwable);

	void outputNotStartedError(Throwable throwable);

	List<String> getLocalVarNames() throws MatlabCommunicationException,
			MatlabNotStartedException;

	List<String> getGlobalVarNames() throws MatlabCommunicationException,
			MatlabNotStartedException;

	List<MOutputParser> getLocalVars() throws MatlabCommunicationException,
			MatlabNotStartedException;

	List<MOutputParser> getGlobalVars() throws MatlabCommunicationException,
			MatlabNotStartedException;

	MOutputParser getMatlabVarXML(String string)
			throws MatlabNotStartedException, MatlabCommunicationException;

	void changeMatlabDirectoryNoOutput(String solutiondir)
			throws MatlabCommunicationException, MatlabNotStartedException;
}
