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
package org.eclipselabs.matclipse.mconsole.decorators;

import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class MatlabDecorator implements ILabelDecorator {

	public Image decorateImage(Image image, Object element) {
//		if (element instanceof IFile) {
//			IFile file=(IFile) element;
//			try {
//				if (file.getProject().hasNature(MatlabNature.NATURE_ID) && file.getName().endsWith(".m")) {
//					if (file.ge)
//				} else return image;
//			} catch (CoreException e) {
//				return null;
//			}
//		}
//		else return null;
		return null;
	}

	public String decorateText(String text, Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub
		
	}

	

}
