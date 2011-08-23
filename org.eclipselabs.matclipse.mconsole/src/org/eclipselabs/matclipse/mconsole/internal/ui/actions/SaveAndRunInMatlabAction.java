/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.matclipse.mconsole.internal.ui.actions;


import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;
import org.eclipselabs.matclipse.mconsole.views.MatlabConsoleView;

public class SaveAndRunInMatlabAction extends Action implements IEditorActionDelegate, IViewActionDelegate {

	private IEditorPart fEditorPart;
	private IViewPart fViewPart = null;

	/**
	 * 
	 */
	public SaveAndRunInMatlabAction() {
		super();
	}

	protected ITextEditor getTextEditor() {
		
		ITextEditor editor = null;
		IWorkbenchPart activePart = fViewPart;
		
		if (activePart == null) {
			activePart = fEditorPart;
		}
		
		if (activePart instanceof ITextEditor) {
			editor = (ITextEditor) activePart;
		}
		
		if (editor == null) {
			editor = (ITextEditor) activePart.getAdapter(ITextEditor.class);
		}
		return editor;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		fViewPart = view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		MConsolePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().saveEditor(getTextEditor(),false);
		MConsolePlugin.getDefault().waitForAutoBuild();
		try {
			MConsolePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MatlabConsoleView.VIEW_ID);
			} catch (Exception e) {
				
			}

		
		IEditorInput editorInput = getTextEditor().getEditorInput();
		 IFile aFile = null;
		 if(editorInput instanceof IFileEditorInput){
			aFile = ((IFileEditorInput)editorInput).getFile();
		 }
		 String fn=	 aFile.getLocation().removeFileExtension().lastSegment();
		 String p = aFile.getLocation().removeLastSegments(1).toString();
		MatlabConsoleView.getDefault().run(fn, p);
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		fEditorPart = targetEditor;
		
	}

	public void selectionChanged(IAction action, ISelection selection) {
		
		
	}

}
