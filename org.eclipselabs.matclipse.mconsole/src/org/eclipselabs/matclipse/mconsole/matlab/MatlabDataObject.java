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
package org.eclipselabs.matclipse.mconsole.matlab;
import org.eclipse.core.runtime.IAdaptable;

	public class MatlabDataObject implements IAdaptable {
		private Object fElement;

		private MatlabDataParent parent;

		public MatlabDataObject(Object element) {
			this.fElement = element;
		}

		public String getName() {
			if (fElement != null) {
				return fElement.toString();
			}
			return "<empty>";
		}

		public void setParent(MatlabDataParent parent) {
			this.parent = parent;
		}

		public MatlabDataParent getParent() {
			return parent;
		}

		public String toString() {
			return getName();
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			
			return null;
		}

		
	}
