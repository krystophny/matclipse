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
import org.eclipse.core.runtime.IAdaptable;

	public class CommandHistoryObject implements IAdaptable {
		private String name;
		private String displayname;
		private CommandHistoryEntry parent;
		private boolean date;
		
		public CommandHistoryObject(String name, String displayname, boolean date) {
			this.name = name;
			this.displayname=displayname;
			this.date=date;
		}

		public CommandHistoryObject(String name, boolean date) {
			this.name = name;
			this.displayname=name;
			this.date=date;
		}
		
		public String getName() {
			return name;
		}

		public void setParent(CommandHistoryEntry parent) {
			this.parent = parent;
		}

		public CommandHistoryEntry getParent() {
			return parent;
		}

		public String toString() {
			return getName();
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			
			return null;
		}

		public String getDisplayname() {
			return displayname;
		}

		public void setDisplayname(String displayname) {
			this.displayname = displayname;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isDate() {
			return date;
		}

		public void setDate(boolean date) {
			this.date = date;
		}

		
	}
