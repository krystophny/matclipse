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

import org.eclipselabs.matclipse.util.MatclipseUtilPlugin;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

public class MOutputParser {
	private String xmltext = "";

	private String name = "";

	private String orientation = "";

	private String type = "";
	private boolean structure;
	private MatlabData data;

	public MOutputParser() {
		super();
		data = new MatlabData();
		this.structure = false;
	}

	public MOutputParser(boolean structure) {
		super();
		data = new MatlabData(structure);
		this.structure = structure;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public MOutputParser(String xmltext) {
		super();
		this.xmltext = xmltext;

	}

	public void parse() {
		try {
			IXMLParser parser // 2
			= XMLParserFactory.createDefaultXMLParser();
			IXMLReader reader // 3
			= StdXMLReader.stringReader(xmltext);
			parser.setReader(reader);

			IXMLElement matlab // 4
			= (IXMLElement) parser.parse();
			// Enumeration enumeration = matlab.enumerateAttributeNames();
			this.name = matlab.getAttribute("name", "");
			// this.orientation = matlab.getAttribute("orientation");

			IXMLElement datatype = (IXMLElement) matlab.getChildren().get(0);
			this.type = datatype.getFullName();

			data = new MatlabData(datatype, structure);
			// data.setName(datatype.getAttribute("name"));
			// data.setSize(datatype.getAttribute("size"));
			// data.setType(this.type);

			// writer.write(xml);
		} catch (Throwable t) {
			MatclipseUtilPlugin.getDefault().errorDialog(
					"Problem with XML Parser: " + xmltext, t);
		}
		//
	}

	public String getXmltext() {
		return xmltext;
	}

	public void setXmltext(String xmltext) {
		this.xmltext = xmltext;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public MatlabData getData() {
		return data;
	}

	public void setData(MatlabData data) {
		this.data = data;
	}

	public boolean isStructure() {
		return structure;
	}

	public void setStructure(boolean structure) {
		this.structure = structure;
	}
}
