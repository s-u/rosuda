


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
import org.rosuda.JGR.util.ErrorMsg;

/**ExTable is a Graphical table that extends JTable and provides
 * superior ease of data entry and manipulation. The goal is to mirror
 * Excel's behavior
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

	public ExTable(ExDefaultTableModel model){
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
	
	public ColumnHeaderListener getColumnListener(){return columnListener;}
	public void setColumnListener(ColumnHeaderListener lis){columnListener = lis;}
	
	/**
	 * Overrides the editCellAt function to allow one click editing
	 * as opposed to the appending of cell edits that is default in
	 * JTable
	 */
	public boolean editCellAt(int row, int column, EventObject e){
		boolean result = super.editCellAt(row, column, e);	
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
		this.requestFocus();
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
		removeColumn(colNumber);
	}
	public void removeColumn(int colNumber){
		try{
			((ExDefaultTableModel) 	getModel()).removeColumn(colNumber);		
		}catch(Exception e){
			new ErrorMsg(e);
		}	
	}
	
	public void insertNewColumn(int colNumber){
		try{
			((ExDefaultTableModel) 	getModel()).insertNewColumn(colNumber);		
		}catch(Exception e){
			new ErrorMsg(e);
		}

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



	public void cutRow(int rowIndex){
		getCopyPasteAdapter().cut();
		removeRow(rowIndex);
	}
	
	public void removeRow(int index) {
		try{
		((ExDefaultTableModel) getModel()).removeRow(index);		
		}catch(Exception e){
			new ErrorMsg(e);
		}
	}
	
	public void insertNewRow(int index){
		try{
			((ExDefaultTableModel) getModel()).insertNewRow(index);		
			}catch(Exception e){
				new ErrorMsg(e);
			}
	}
	
	
}

