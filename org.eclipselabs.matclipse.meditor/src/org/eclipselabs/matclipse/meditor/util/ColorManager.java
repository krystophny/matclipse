/*******************************************************************************
 * Copyright (c) 2005, 2011 Prashant Deva (WSMO studio project) and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Prashant Deva - initial API and implementation
 *     Georg Huhs, Winfried Kernbichler, David Camhy (ITPCP) - 
 *         some changes for using in Meditor
 * Last changed: 
 *     2007-10-24
 *******************************************************************************/

package org.eclipselabs.matclipse.meditor.util;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;


public class ColorManager {

    private Preferences preferences;
    private Map<RGB, Color> fColorTable         = new HashMap<RGB, Color>(10);
    private Map<String, Color> fNamedColorTable = new HashMap<String, Color>(10);
    private final static RGB STD_COLOR          = new RGB(255, 50, 0);

    
    public ColorManager(){
    }
    
    
    public void dispose() {
        Iterator<Color> colorIterator = fColorTable.values().iterator();
        while (colorIterator.hasNext())
            colorIterator.next().dispose();
    }
    
    
    public Color getColor(RGB rgb) {
        Color color = fColorTable.get(rgb);
        if (color == null) {
            color = new Color(Display.getCurrent(), rgb);
            fColorTable.put(rgb, color);
        }
        return color;
    }
    
    
    public Color getNamedColor(String name) {
        System.out.println(name);
        Color color = fNamedColorTable.get(name);
        if (color == null) {
            String colorCode =  preferences.getString(name);
            if (colorCode.length() == 0) {
                if (name.equals("RED")) {
                    color = getColor(new RGB(255, 0, 0));
                }
                else if (name.equals("BLACK")) {
                    color = getColor(new RGB(0,0,0));
                }
                else {
                    System.err.println("Unknown color:" + name);
                    color = getColor(new RGB(255,0,0));
                }
            }
            else {
                try {
                    RGB rgb = StringConverter.asRGB(colorCode);
                    color = new Color(Display.getCurrent(), rgb);
                    fNamedColorTable.put(name, color);
                }
                catch (DataFormatException e) {
                    // Data conversion failure, maybe someone edited our prefs by hand
                    e.printStackTrace();
                    color = new Color(Display.getCurrent(), STD_COLOR);
                }
            }
        }
        return color;
    }
}
