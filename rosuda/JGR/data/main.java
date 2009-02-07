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

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.rosuda.ibase.Common;
/**
 * A Simple Example main statement that instantiates the
 * table window with some data
 * @author Ian
 *
 */


public class main {

	/**
	 * @param args
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(final String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		
		if(!Common.isMac())
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		
		DefaultTableModel tableModel = new DefaultTableModel(
				new String[][] { { "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" ,  "Three", "Four" , "One", "Two" , "One", "Two" , "One", "Two" , "One", "Two" , "One", "Two" , "One", "Two" , "One", "Two" , "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" ,  "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },
					{ "One", "Two" }, { "Three", "Four" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" },{ "One", "Two" }},
				new String[] { "Column 1", "Column 3", "Column 4", "Column 5", "Column 6", "Column 7", "Column 8", "Column 9", "Column 10", "Column 11" });
		ExTable table = new ExTable();
		table.setModel(tableModel);/*
		TableWindow inst = new TableWindow(table,tableModel);
		inst.setLocationRelativeTo(null);
		inst.setVisible(true);*/
		DataFrameWindow fdw = new DataFrameWindow(table);
		fdw.setVisible(true);
	}

}

