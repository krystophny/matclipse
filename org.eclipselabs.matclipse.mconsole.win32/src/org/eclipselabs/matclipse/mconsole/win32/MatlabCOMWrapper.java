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
 *     Christopher Albert (ITPCP) - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.matclipse.mconsole.win32;

public interface MatlabCOMWrapper {

	/**
	 * Executes a command in the Matlab console and returns the output.
	 * 
	 * @param input the Matlab console input
	 * @return      the Matlab console output
	 * 
	 * @see <a href="http://www.mathworks.com/help/techdoc/ref/execute.html">Matlab documentation</a>
	 */
	public abstract String execute(String input);

	/**
	 * Quits Matlab
	 */
	public abstract void quit();

	/**
	 * Evaluates a Matlab function and returns the result.
	 *  
	 * @param functionname the name of the Matlab function
	 * @param numout       the number of output arguments
	 * @param args         the input arguments
	 * @return             the function result
	 * 
	 * @see <a href="http://www.mathworks.com/help/techdoc/ref/com.feval.html">Matlab documentation</a>
	 */
	public abstract Object feval(String functionname, int numout,
			Object... args);

}