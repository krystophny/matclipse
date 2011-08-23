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
package org.eclipselabs.matclipse.mconsole.internal.ui.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;


public class ChangeMatlabDirectoryAction implements IViewActionDelegate {
	private IStructuredSelection selection;
	public ChangeMatlabDirectoryAction() {
		// TODO Auto-generated constructor stub
	}

	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		
	}

	public void run(IAction action) {
		if (MConsolePlugin.getDefault().isMatlabInitialized()) {
			if (MConsolePlugin.getDefault().getMatlab()
					.isMatlabAvailable()) {
				try {
					Object obj = selection.getFirstElement();
					if (obj instanceof IResource) {
						IResource resource = (IResource) obj;
						MConsolePlugin.getDefault().getMatlab().changeMatlabDirectoryToResource(resource);
					}
				} catch (Exception e) {
					
				}
				
			} else
				MConsolePlugin.getDefault().getMatlab()
						.outputDirectoryChangeError(
								new Throwable());
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection)
		this.selection=(IStructuredSelection) selection;

	}

}
