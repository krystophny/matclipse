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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipselabs.matclipse.mconsole.MConsolePlugin;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.ThemeConstants;
import org.eclipselabs.matclipse.mconsole.internal.ui.preferences.ThemeWrapper;


class CommandHistoryLabelProvider extends CellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		if (cell.getElement() instanceof CommandHistoryEntry) {
			ThemeWrapper theme = MConsolePlugin.getDefault().getCurrentTheme();
			CommandHistoryEntry commandHistoryEntry = (CommandHistoryEntry) cell
					.getElement();

			String entry = commandHistoryEntry.getName();
			if (entry.contains(System.getProperty("line.separator"))) {
				cell.setText(commandHistoryEntry.toString().replaceAll(
						System.getProperty("line.separator"), "\\\\n"));
			} else {
				cell.setText(commandHistoryEntry.toString());
			}
			try {
				if (commandHistoryEntry.getName().startsWith("%")) {
					cell.setForeground(theme
							.getColor(ThemeConstants.COMMANDHISTORY_DATE_COLOR));
				} else
					cell.setForeground(theme
							.getColor(ThemeConstants.COMMANDHISTORY_FOREGROUND_COLOR));
				cell.setBackground(theme
						.getColor(ThemeConstants.COMMANDHISTORY_BACKGROUND_COLOR));
				cell.setFont(theme.getFont(ThemeConstants.COMMANDHISTORY_FONT));

			} catch (Exception e) {

			}
		}

	}
}
