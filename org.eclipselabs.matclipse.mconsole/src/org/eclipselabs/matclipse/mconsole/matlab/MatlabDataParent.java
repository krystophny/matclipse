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

import java.util.ArrayList;

public class MatlabDataParent extends MatlabDataObject {
	private ArrayList<MatlabDataParent> children;

	private MatlabData matlabdata;

	public MatlabDataParent(String name, MatlabData matlabdata) {
		super(name);
		this.matlabdata = matlabdata;
		children = new ArrayList<MatlabDataParent>();
	}

	public void addChild(MatlabDataParent child) {
		children.add(child);
		child.setParent(this);
	}

	public void removeChild(MatlabDataParent child) {
		children.remove(child);
		child.setParent(null);
	}

	public MatlabDataParent[] getChildren() {
		return (MatlabDataParent[]) children.toArray(new MatlabDataParent[children
				.size()]);
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public MatlabData getMatlabdata() {
		return matlabdata;
	}

	
	public void setMatlabdata(MatlabData matlabdata) {
		this.matlabdata = matlabdata;
	}
}
