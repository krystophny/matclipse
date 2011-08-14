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

package org.eclipselabs.matclipse.meditor.util;


/**
 * Exception, thrown if there arose a problem during launching a browser
 * @author Georg Huhs
 */
public class BrowserException extends Exception {

    private final static long serialVersionUID = 1L;

    private final static String MESSAGE_START = "Unable to open browser - ";

    BrowserException(){
        super();
    }

    BrowserException(String message){
        super(MESSAGE_START + message);
    }

    BrowserException(String message, Throwable cause){
        super(MESSAGE_START + message, cause);
    }

    BrowserException(Throwable cause){
        super(cause);
    }
}
