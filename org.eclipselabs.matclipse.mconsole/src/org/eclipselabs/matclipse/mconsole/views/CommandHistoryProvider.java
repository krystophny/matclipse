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
package org.eclipselabs.matclipse.mconsole.views;

import java.util.Vector;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;




public class CommandHistoryProvider implements IStructuredContentProvider {
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		Vector<CommandHistoryEntry> history;
		try {
			history=MConsolePlugin.getDefault().getCommandHistory();
		} catch (NullPointerException e) {
			history=new Vector<CommandHistoryEntry>();
		}
		CommandHistoryEntry[] retVal= new CommandHistoryEntry[history.size()];
		CommandHistoryEntry[] failure=new CommandHistoryEntry[0];
		//failure[0]= "";
		if (history.size()>0) {
		
		for (int i=0;i<history.size();i++) {
			CommandHistoryEntry hist= (CommandHistoryEntry) history.get(i);
			retVal[i]=hist;
		}
		} else {
		
		retVal=failure;
		}
			
		return retVal; 
	}
}

