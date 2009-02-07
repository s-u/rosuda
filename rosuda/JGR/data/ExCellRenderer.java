/**
 * ExTable is a Graphical table that extends JTable and provides
 * superior ease of data entry and manipulation. The goal is to mirror
 * Excel's behavior
 * Copyright (C) 2009  Ian Fellows
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.rosuda.JGR.data;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.rosuda.ibase.Common;


/**
 * Tiger Stripes the rows for easier visibility
 * 
 * @author Ian Fellows
 *
 */
public class ExCellRenderer extends DefaultTableCellRenderer
{
	private Color whiteColor = new Color(254, 254, 254);
	private Color alternateColor = new Color(237, 243, 254);
	private Color selectedColor = new Color(61, 128, 223);

	public Component getTableCellRendererComponent(JTable table,
					Object value, boolean selected, boolean focused,
					int row, int column){
		super.getTableCellRendererComponent(table, value,
				selected, focused, row, column);

		// Set the background color
		Color bg;
		if (!selected)
			bg = (row % 2 == 0 ? alternateColor : whiteColor);
		else
			bg = selectedColor;
		setBackground(bg);

		// Set the foreground to white when selected
		Color fg;
		if (selected)
			fg = Color.white;
		else
			fg = Color.black;
		setForeground(fg);

		return this;
	}
}
