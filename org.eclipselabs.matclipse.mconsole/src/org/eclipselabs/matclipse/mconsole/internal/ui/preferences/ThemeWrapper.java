/*******************************************************************************
 * Copyright (c) 2005, 2011 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrei Loskutov - refactoring, themes and full presentation feature set
 *******************************************************************************/
package org.eclipselabs.matclipse.mconsole.internal.ui.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.internal.themes.CascadingColorRegistry;
import org.eclipse.ui.internal.themes.CascadingFontRegistry;
import org.eclipse.ui.internal.themes.CascadingTheme;
import org.eclipse.ui.themes.ITheme;

/**
 * @author Andrei
 */
@SuppressWarnings("restriction")
public class ThemeWrapper extends CascadingTheme {

    private Map<String,Integer> intMap;
    private Map<String,Boolean> booleanMap;

    /**
     * @param currentTheme
     */
    public ThemeWrapper(ITheme currentTheme) {
        super(currentTheme,
                new CascadingColorRegistry(currentTheme.getColorRegistry()),
                new CascadingFontRegistry(currentTheme.getFontRegistry()));
        intMap = new HashMap<String,Integer>();
        booleanMap = new HashMap<String,Boolean>();
    }

    public boolean getBoolean(String key) {
        Boolean b = (Boolean) booleanMap.get(key);
        if(b == null) {
            return super.getBoolean(key);
        }
        return b.booleanValue();
    }

    public int getInt(String key) {
        Integer in = (Integer) intMap.get(key);
        if(in == null){
            return super.getInt(key);
        }
        return in.intValue();
    }

    public void setBoolean(String key, boolean value){
        booleanMap.put(key, Boolean.valueOf(value));
    }

    public void setInt(String key, int value){
        intMap.put(key, Integer.valueOf(value));
    }

    public void propertyChanged(String key, Object value){
        if(value instanceof Boolean){
            setBoolean(key, ((Boolean)value).booleanValue());
        } else if(value instanceof Integer){
            setInt(key, ((Integer)value).intValue());
        } else {
            return;
        }
    }

    public Color getColor(String key){
        Color c = getColorRegistry().get(key);
        if(c == null){
            // default if configuration is broken
            c = getColorRegistry().get(ThemeConstants.CONSOLE_TEXT_COLOR);
        }
        return c;
    }

    public Font getFont(String key){
        Font c = getFontRegistry().get(key);
        if(c == null){
            // default if configuration is broken
            c = getFontRegistry().get(ThemeConstants.CONSOLE_FONT);
        }
        return c;
    }

    public void dispose() {
        intMap = null;
        booleanMap = null;
        super.dispose();
    }


}
