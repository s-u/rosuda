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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;

import org.rosuda.ibase.Common;


/**
 * All features except the row headers are implemented in
 * this extension of the JTable.
 * 
 * @author Ian Fellows
 *
 */
public class ExTable extends JTable{
	
	private CopyPasteAdapter excelCopyPaste;

	private ColumnHeaderListener columnListener;
	
	public JScrollPane parentPane = null;
	
	public ExTable(){
		super();
		// Enable cell selection
		this.setColumnSelectionAllowed(true);
		this.setRowSelectionAllowed(true);
		//do Tiger Striping
		this.setDefaultRenderer(Object.class, new ExCellRenderer());
		//enable copy paste
		excelCopyPaste = new CopyPasteAdapter(this);
		//enable contextual menus for column headers
		columnListener = new ColumnHeaderListener(this);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.getTableHeader().setResizingAllowed(true);

	}

	public ExTable(TableModel model){
		super(model);
		// Enable cell selection
		this.setColumnSelectionAllowed(true);
		this.setRowSelectionAllowed(true);
		//do Tiger Striping
		this.setDefaultRenderer(Object.class, new ExCellRenderer());
		//enable copy paste
		excelCopyPaste = new CopyPasteAdapter(this);
		//enable contextual menus for column headers
		columnListener = new ColumnHeaderListener(this);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.getTableHeader().setResizingAllowed(true);

	}
	
	/**
	 * Overrides the editCellAt function to allow one click editing
	 * as opposed to the appending of cell edits that is default in
	 * JTable
	 */
	public boolean editCellAt(int row, int column, EventObject e){
		boolean result = super.editCellAt(row, column, e);;		
		final Component editor = getEditorComponent();
		if (editor != null && editor instanceof JTextComponent){
			if (e == null || e.getClass().toString().endsWith("KeyEvent")){
				((JTextComponent)editor).selectAll();
			}
			else
			{
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){
						((JTextComponent)editor).selectAll();
					}
				});
			}
		}

		return result;
	}
	
	public void selectColumn(int colIndex){

		Point currentPnt=null;
		if(parentPane!=null){
			currentPnt = parentPane.getViewport().getViewPosition();
		}
		changeSelection(getRowCount()-1, colIndex, false, false);
		if(Common.isMac())
			changeSelection(0, colIndex, false, true);
		else
			changeSelection(0, colIndex, true, true);
		if(parentPane!=null)
			parentPane.getViewport().setViewPosition(currentPnt);
	}
	
	public void selectRow(int rowIndex){
		Point currentPnt=null;
		if(parentPane!=null){
			currentPnt = parentPane.getViewport().getViewPosition();
		}

		changeSelection(rowIndex,getColumnCount()-1,  false, false);
		if(Common.isMac())
			changeSelection(rowIndex,0, false, true);
		else
			changeSelection(rowIndex,0,  true, true);
		if(parentPane!=null)
			parentPane.getViewport().setViewPosition(currentPnt);
	}
	
	public CopyPasteAdapter getCopyPasteAdapter() { return excelCopyPaste;}
	
	
	public void copySelection(){
		excelCopyPaste.copy();
	}
	
	public void cutSelection(){
		excelCopyPaste.cut();
	}
	
	public void cutColumn(int colNumber){
		getCopyPasteAdapter().cut();

	}
	public void removeColumn(int colNumber){
		getColumnModel().removeColumn(getColumnModel().getColumn(colNumber));		
	}
	
	public void insertNewColumn(int colNumber){
		getColumnModel().moveColumn(getColumnModel().getColumnCount()-1,colNumber );
		getColumnModel().getColumn(colNumber).setHeaderValue("New Column");
	}
	
	public void insertColumn(int colNumber){
		if(getCopyPasteAdapter().getClipBoard().indexOf("\t")!=-1){
			JOptionPane.showMessageDialog(null, "Invalid Insertion",
					"Invalid Insertion Selection",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		insertNewColumn(colNumber);
		selectColumn(colNumber);
		getCopyPasteAdapter().paste();
	}
	
	public void pasteSelection(){
		excelCopyPaste.paste();
	}




	public void removeRow(int index) {
		// TODO Auto-generated method stub
		
	}
	
	
}

