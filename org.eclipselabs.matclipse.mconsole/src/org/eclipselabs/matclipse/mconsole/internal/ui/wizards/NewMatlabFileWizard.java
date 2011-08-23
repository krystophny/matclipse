/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipselabs.matclipse.mconsole.internal.ui.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;

/**
 * Standard workbench wizard that create a new file resource in the workspace.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWorkbenchWizard wizard = new BasicNewFileResourceWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a file resource at the user-specified
 * workspace path is created, the dialog closes, and the call to
 * <code>open</code> returns.
 * </p>
 */
@SuppressWarnings("restriction")
public class NewMatlabFileWizard extends BasicNewResourceWizard {
    private WizardNewFileCreationPage mainPage;
    public static String WIZARD_ID="org.eclipselabs.matclipse.mconsole.wizards.NewMatlabFileWizard";
    /**
     * Creates a wizard for creating a new file resource in the workspace.
     */
    public NewMatlabFileWizard() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public void addPages() {
        super.addPages();
        mainPage = new WizardNewFileCreationPage("newFilePage1", getSelection());//$NON-NLS-1$
        mainPage.setTitle(ResourceMessages.FileResource_pageTitle);
        mainPage.setDescription(ResourceMessages.FileResource_description); 
        mainPage.setFileName("main.m");
        addPage(mainPage);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setWindowTitle(ResourceMessages.FileResource_shellTitle);
        setNeedsProgressMonitor(true);
    }

    /* (non-Javadoc)
     * Method declared on BasicNewResourceWizard.
     */
    protected void initializeDefaultPageImageDescriptor() {
       ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newfile_wiz.gif");//$NON-NLS-1$
	   setDefaultPageImageDescriptor(desc);
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public boolean performFinish() {
        IFile file = mainPage.createNewFile();
        if (file == null)
            return false;

        selectAndReveal(file);

        // Open editor on new file.
        IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
        try {
            if (dw != null) {
                IWorkbenchPage page = dw.getActivePage();
                if (page != null) {
                    IDE.openEditor(page, file, true);
                }
            }
        } catch (PartInitException e) {
            DialogUtil.openError(dw.getShell(), ResourceMessages.FileResource_errorMessage, 
                    e.getMessage(), e);
        }

        return true;
    }
}
