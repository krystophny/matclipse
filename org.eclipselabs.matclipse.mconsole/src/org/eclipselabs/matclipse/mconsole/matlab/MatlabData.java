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
import java.util.List;

import org.eclipselabs.matclipse.util.MatclipseUtilPlugin;

import net.n3.nanoxml.IXMLElement;

public class MatlabData {

	private String name;

	private String size;

	private String type;

	private String min;
	private String max;
	private String mean;

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getMean() {
		return mean;
	}

	public void setMean(String mean) {
		this.mean = mean;
	}

	private IXMLElement xmlelement;

	private boolean structure;
	private List<?> data;

	public List<?> getData() {
		return data;
	}

	public MatlabData() {
		super();
		data = new ArrayList<Object>();

	}

	public MatlabData(boolean structure) {
		super();
		data = new ArrayList<Object>();
		this.structure = structure;
	}

	public MatlabData(IXMLElement xmlelement) {
		super();
		data = new ArrayList<MatlabData>();
		this.xmlelement = xmlelement;
		structure = false;
		parse();

	}

	public MatlabData(IXMLElement xmlelement, boolean structure) {
		super();
		data = new ArrayList<MatlabData>();
		this.xmlelement = xmlelement;
		this.structure = structure;
		parse();

	}

	private void setData(List<?> data) {
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isNumeric() {
		if (type != null) {
			if (type.equals("single") || type.equals("double")
					|| type.equals("logical") || type.equals("int8")
					|| type.equals("uint8") || type.equals("int16")
					|| type.equals("uint16") || type.equals("int32")
					|| type.equals("uint32") || type.equals("int64")
					|| type.equals("uint64")

			)
				return true;
		}
		return false;
	}

	public void parse() {

		this.name = xmlelement.getAttribute("name", "name");
		this.size = xmlelement.getAttribute("size", "size");
		this.min = xmlelement.getAttribute("min", "");
		this.max = xmlelement.getAttribute("max", "");
		this.mean = xmlelement.getAttribute("mean", "");
		this.type = xmlelement.getFullName();

		if (this.type.equals("single") || this.type.equals("double")
				|| this.type.equals("logical") || this.type.equals("int8")
				|| this.type.equals("uint8") || this.type.equals("int16")
				|| this.type.equals("uint16") || this.type.equals("int32")
				|| this.type.equals("uint32") || this.type.equals("int64")
				|| this.type.equals("uint64") || this.type.equals("handle")) {
			if (!structure) {

				ArrayList<String> numbers = new ArrayList<String>();
				try {
					if (xmlelement.getContent().indexOf(":") == -1) {
						String[] stringlist = xmlelement.getContent()
								.split(" ");
						for (int i = 0; i < stringlist.length; i++) {
							numbers.add(stringlist[i]);
						}
					} else {
						String[] realcomplex = xmlelement.getContent().split(
								":");
						String[] real = realcomplex[0].split(" ");
						String[] complex = realcomplex[1].split(" ");
						for (int i = 0; i < real.length; i++) {
							numbers.add(real[i] + complex[i] + "j");
						}
					}
				} catch (Throwable t) {
					numbers.add("");
				}
				setData(numbers);
			} else
				setData(outputStringList());

		} else if (this.type.equals("char")) {
			setData(outputStringList());
		} else if (this.type.equals("dchar")) {
			ArrayList<String> strings = new ArrayList<String>();
			String[] stringlist = xmlelement.getContent().split(" ");
			StringBuilder outputBuilder = new StringBuilder();
			for (int i = 0; i < stringlist.length; i++) {
				String str = stringlist[i];
				try {
					int j = Integer.parseInt(str.trim());
					String aChar = Character.valueOf((char) j).toString();
					outputBuilder.append(aChar);

				} catch (Throwable t) {
					continue;
				}
			}
			strings.add(outputBuilder.toString());
			setData(strings);
		} else if (this.type.equals("inline")) {
			MatlabData childrendata = new MatlabData(
					xmlelement.getChildAtIndex(0));
			setData(childrendata.getData());
		} else if (this.type.equals("function_handle")) {
			MatlabData childrendata = new MatlabData(
					xmlelement.getChildAtIndex(0));
			setData(childrendata.getData());
		} else if (this.type.equals("sym")) {
			MatlabData childrendata = new MatlabData(
					xmlelement.getChildAtIndex(0));
			setData(childrendata.getData());
		} else if (this.type.equals("sparse")) {
			ArrayList<String> strings = new ArrayList<String>();
			try {
				String xmlText = xmlelement.getContent();
				String[] xmlTextArray = xmlText.split(":");
				ArrayList<String> posX = new ArrayList<String>();
				if (!(xmlTextArray[0].trim().equals(""))) {
					String[] posListX = xmlTextArray[0].split(" ");
					for (int i = 0; i < posListX.length; i++) {
						posX.add(posListX[i]);
						System.out.println("X:" + posListX[i]);
					}
					ArrayList<String> posY = new ArrayList<String>();
					String[] posListY = xmlTextArray[1].split(" ");
					for (int i = 0; i < posListY.length; i++) {
						posY.add(posListY[i]);
						System.out.println("Y:" + posListY[i]);
					}
					ArrayList<String> numbers = new ArrayList<String>();
					if (xmlTextArray.length == 3) {
						String[] numbersList = xmlTextArray[2].split(" ");
						for (int i = 0; i < numbersList.length; i++) {
							numbers.add(numbersList[i]);
							System.out.println("Numbers:" + numbers.get(i));
						}
					} else if (xmlTextArray.length == 4) {
						String[] real = xmlTextArray[2].split(" ");
						String[] complex = xmlTextArray[3].split(" ");
						for (int i = 0; i < real.length; i++) {
							numbers.add(real[i] + "+" + complex[i] + "j");
							System.out.println("Numbers:" + numbers.get(i));
						}
					}
					for (int i = 0; i < posX.size(); i++) {
						strings.add("(" + posX.get(i).trim() + ","
								+ posY.get(i).trim() + ")" + "\t:\t"
								+ numbers.get(i).trim() + "\n");
					}
				} else
					strings.add("");

			} catch (Throwable t) {
				MatclipseUtilPlugin.getDefault().errorDialog(
						"Problem parsing sparse", t);
				strings.add("");
			}
			setData(strings);
		} else if (this.type.equals("cell")) {
			ArrayList<MatlabData> cells = new ArrayList<MatlabData>();
			for (int i = 0; i < xmlelement.getChildrenCount(); i++) {
				MatlabData childrendata = new MatlabData(
						xmlelement.getChildAtIndex(i));
				cells.add(childrendata);

			}
			setData(cells);
		} else if (this.type.equals("struct")) {
			ArrayList<MatlabData> cells = new ArrayList<MatlabData>();
			for (int i = 0; i < xmlelement.getChildrenCount(); i++) {
				MatlabData childrendata = new MatlabData(
						xmlelement.getChildAtIndex(i));
				cells.add(childrendata);

			}
			setData(cells);
		}

	}

	public List<String> outputStringList() {
		ArrayList<String> strings = new ArrayList<String>();

		try {
			strings.add(xmlelement.getContent().toString());
		} catch (Throwable t) {
			strings.add("");
		}
		return strings;
	}

	public boolean isStructure() {
		return structure;
	}

	public void setStructure(boolean isStructure) {
		this.structure = isStructure;
	}

}
