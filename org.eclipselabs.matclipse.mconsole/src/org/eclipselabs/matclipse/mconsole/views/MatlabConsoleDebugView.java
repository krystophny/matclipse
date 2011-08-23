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


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class MatlabConsoleDebugView extends ViewPart {
	private static MatlabConsoleDebugView plugin;
	private StyledText loggingText;
	private static String logString="";
	private Action clearAction;
	public static String VIEW_ID="org.eclipselabs.matclipse.mconsole.views.MatlabConsoleDebugView";
	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	public static MatlabConsoleDebugView getDefault() {
		return plugin;
	}
	/**
	 * The constructor.
	 */
	public MatlabConsoleDebugView() {
		plugin=this;
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		loggingText = new StyledText(parent, SWT.V_SCROLL
				| SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.READ_ONLY);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(clearAction);
		
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(clearAction);
		
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(clearAction);
		
	}
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				MatlabConsoleDebugView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(loggingText);
		loggingText.setMenu(menu);
		
	}
	private void makeActions() {
		clearAction = new Action() {
			public void run() {
				logString="";
				loggingText.setText("");
			}
		};
		clearAction.setText("Clear");
		clearAction.setToolTipText("Clear Command History");
		clearAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		
	}

	public void log(String log) {
		logString=logString+log;
		loggingText.setText(logString);
	}

	public void jumpToEnd() {
		loggingText.setSelection(loggingText.getText().length());
		loggingText.showSelection();
	}


/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		loggingText.setFocus();
	}
}
