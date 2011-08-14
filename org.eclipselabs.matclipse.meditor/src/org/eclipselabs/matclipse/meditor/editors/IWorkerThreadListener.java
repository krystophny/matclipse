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

package org.eclipselabs.matclipse.meditor.editors;


import java.util.EventListener;
import java.util.HashMap;

import org.eclipse.core.runtime.IPath;


/**
 * Interface for listeners for threads that want to announce exceptions and 
 * the completion of their work.
 * @author Georg Huhs
 */
public interface IWorkerThreadListener extends EventListener {
    public void exceptionOccured(String message, Exception e);
    public void workFinished(HashMap<String, IPath> generatedFiles);

}
