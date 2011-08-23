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

import java.util.ArrayList;

public class CommandHistoryEntry extends CommandHistoryObject {
	private ArrayList<CommandHistoryEntry> children;
	
	
	public CommandHistoryEntry(String name, String displayname, boolean date) {
		super(name,displayname,date);
		children = new ArrayList<CommandHistoryEntry>();
		
	}
	public CommandHistoryEntry(String name, boolean date) {
		super(name,name,date);
		children = new ArrayList<CommandHistoryEntry>();
		
	}
	public CommandHistoryEntry(String name) {
		super(name,name,false);
		children = new ArrayList<CommandHistoryEntry>();
		
	}

	public void addChild(CommandHistoryEntry child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(CommandHistoryEntry child) {
		children.remove(child);
		child.setParent(null);
	}

	public CommandHistoryEntry[] getChildren() {
		return (CommandHistoryEntry[]) children.toArray(new CommandHistoryEntry[children
				.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

}
