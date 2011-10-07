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
package org.eclipselabs.matclipse.mconsole.win32.impl;
import org.eclipselabs.matclipse.mconsole.win32.MatlabCOMWrapper;

import com.jacob.activeX.ActiveXComponent;

/**
 * Exposes the Matlab(r) COM automation server functionality
 * 
 * @author Christopher Albert
 * @see <a href="http://www.mathworks.com/help/techdoc/ref/f16-35614.html#f16-54223">Matlab documentation</a>
 *
 */
public class JacobMatlabCOMWrapper implements MatlabCOMWrapper {
	ActiveXComponent matlab;
	
	/**
	 * Executes a command in the Matlab console and returns the output.
	 * 
	 * @param input the Matlab console input
	 * @return      the Matlab console output
	 * 
	 * @see <a href="http://www.mathworks.com/help/techdoc/ref/execute.html">Matlab documentation</a>
	 */
	public String execute(String input) {
		return (new ActiveXComponent("Matlab.Application")).invoke("Execute", input).getString();
	}
	
	/**
	 * Quits Matlab
	 */
	public void quit() {
		(new ActiveXComponent("Matlab.Application")).invoke("Quit");
	}
	
	
	/**
	 * Not implemented. Evaluates a Matlab function and returns the result.
	 *  
	 * @param functionname the name of the Matlab function
	 * @param numout       the number of output arguments
	 * @param args         the input arguments
	 * @return             the function result
	 * 
	 * @see <a href="http://www.mathworks.com/help/techdoc/ref/com.feval.html">Matlab documentation</a>
	 */
	public Object feval(String functionname, int numout, Object ... args) {
		// TODO
		throw new UnsupportedOperationException("Not implemented");
	}
}
