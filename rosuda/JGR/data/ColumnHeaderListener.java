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

import java.awt.event.*;


import javax.swing.*;
import javax.swing.table.*;

import org.rosuda.ibase.Common;

/**
 * Handles the column header contextual menu
 * 
 * @author Ian Fellows
 *
 */
public class ColumnHeaderListener extends MouseAdapter  {
	private ExTable table;
	private JPopupMenu menu;
	
	public ColumnHeaderListener(ExTable t){
		table = t;
		JTableHeader header = table.getTableHeader();
		header.addMouseListener(this);
	}
	
	
	public void mouseClicked(MouseEvent evt){
		System.out.println("column clicked: "+evt.isPopupTrigger());
		boolean isMac = System.getProperty("java.vendor").indexOf("Apple")>-1;
		TableColumnModel colModel = table.getColumnModel();		
		int vColIndex = colModel.getColumnIndexAtX(evt.getX());
		int mColIndex = table.convertColumnIndexToModel(vColIndex);	
		table.selectColumn(vColIndex);
		
		if(evt.getButton()==MouseEvent.BUTTON3 && !isMac){
			new ColumnContextMenu(evt);
		}
	}
	
	
	public void mousePressed(MouseEvent evt){
		boolean isMac = System.getProperty("java.vendor").indexOf("Apple")>-1;
		if(evt.isPopupTrigger() && isMac){
			new ColumnContextMenu(evt);	
		}
	}

	
	class ColumnContextMenu  implements ActionListener{
		int vColIndex,mColIndex;
		
		public ColumnContextMenu(MouseEvent evt){
			TableColumnModel colModel = table.getColumnModel();		
			vColIndex = colModel.getColumnIndexAtX(evt.getX());
			mColIndex = table.convertColumnIndexToModel(vColIndex);	
			menu = new JPopupMenu();
			table.getTableHeader().add(menu);
			JMenuItem copyItem = new JMenuItem ("Copy");
			copyItem.addActionListener(this);
			menu.add( copyItem );
			JMenuItem cutItem = new JMenuItem ("Cut");
			cutItem.addActionListener(this);
			menu.add( cutItem );
			JMenuItem pasteItem = new JMenuItem ("Paste");
			pasteItem.addActionListener(this);
			menu.add ( pasteItem );
			menu.addSeparator();
			JMenuItem insertItem = new JMenuItem ("Insert");
			insertItem.addActionListener(this);
			menu.add( insertItem );
			JMenuItem insertNewItem = new JMenuItem ("Insert New Column");
			insertNewItem.addActionListener(this);
			menu.add( insertNewItem );
			JMenuItem removeItem = new JMenuItem ("Remove Column");
			removeItem.addActionListener(this);
			menu.add( removeItem );
			menu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
		
		public void actionPerformed(ActionEvent e){
			
			JMenuItem source = (JMenuItem)(e.getSource());
			System.out.println("column Contextual Menu selected: "+source.getText());
			if(source.getText()=="Copy"){
				table.getCopyPasteAdapter().copy();
			} else if(source.getText()=="Cut"){
				table.cutColumn(vColIndex);
			} else if(source.getText()=="Paste"){
				table.getCopyPasteAdapter().paste();
			} else if(source.getText()=="Insert"){
				table.insertColumn(vColIndex);
			} else if(source.getText()=="Insert New Column"){
				table.insertNewColumn(vColIndex);
			} else if(source.getText()=="Remove Column"){
				table.removeColumn(vColIndex);
			}
			menu.setVisible(false);
		}
		
	}
	
	
}
