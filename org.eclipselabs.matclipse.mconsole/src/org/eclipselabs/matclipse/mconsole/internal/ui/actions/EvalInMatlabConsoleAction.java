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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;
import org.eclipselabs.matclipse.mconsole.views.MatlabConsoleView;
import org.eclipselabs.matclipse.util.MatclipseUtilPlugin;


public class EvalInMatlabConsoleAction implements IEditorActionDelegate,
		IViewActionDelegate {

	private IAction fAction;
	private IEditorPart fEditorPart;
	private IViewPart fViewPart = null;

	/**
	 * 
	 */
	public EvalInMatlabConsoleAction() {
		super();
	}

	public IDocument getDocument() {
		return getTextEditor().getDocumentProvider().getDocument(
				fEditorPart.getEditorInput());
	}

	protected ITextSelection getSelection() {
		ITextEditor editor = getTextEditor();
		if (editor != null) {
			ISelection selection = editor.getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				return (ITextSelection) selection;
			}
		}
		return new TextSelection(0, 0);
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

		ITextSelection selection = getSelection();
		String selectedText = null;
		try {
			selectedText = getDocument().get(selection.getOffset(),
					selection.getLength());
			try {
				MConsolePlugin
						.getDefault()
						.getWorkbench()
						.getActiveWorkbenchWindow()
						.getActivePage()
						.showView(
								MatlabConsoleView.VIEW_ID);
			} catch (Exception e) {

			}

			MatlabConsoleView.getDefault().run(selectedText, null, true, true);
		} catch (BadLocationException e) {
			MatclipseUtilPlugin.getDefault().logError(
					"Problem running selection in Matlab Console", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fAction = action;
		updateWith(selection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface
	 * .action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		fEditorPart = targetEditor;
		fAction = action;
		if (targetEditor != null && targetEditor.getEditorSite() != null
				&& targetEditor.getEditorSite().getSelectionProvider() != null) {
			updateWith(targetEditor.getEditorSite().getSelectionProvider()
					.getSelection());
		}
	}

	public void updateWith(ISelection selection) {
		if (fAction != null) {
			boolean enable = false;
			if (selection != null) {
				if (selection instanceof ITextSelection) {
					if (((ITextSelection) selection).getLength() > 0) {
						enable = true;
					}
				} else {
					enable = !selection.isEmpty();
				}
			}
			fAction.setEnabled(enable);
		}
	}
}
