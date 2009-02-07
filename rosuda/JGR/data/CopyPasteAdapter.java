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
import java.awt.event.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.util.List;

import org.rosuda.ibase.Common;
/**
* ExcelAdapter enables Copy-Paste Clipboard functionality on JTables.
* The clipboard data format used by the adapter is compatible with
* the clipboard format used by Excel. This provides for clipboard
* interoperability between enabled JTables and Excel.
* 
* @author Ian Fellows (2008) adapted from the work of Walter Bogaardt (1999)
* 
* 1. Now handles empty cells correctly. 
* 2. \r carriage returns are okay (Excel 2007)
* 3. Now works with os x. 
* 4. Added cut.
* 
* 
*/
public class CopyPasteAdapter implements ActionListener{
	private String rowstring,value;
	private Clipboard system;
	private StringSelection stsel;
	private JTable jTable1 ;
	
	
	/**
	 * The Excel Adapter is constructed with a
	 * JTable on which it enables Copy-Paste and acts
	 * as a Clipboard listener.
	 */

	public CopyPasteAdapter(JTable myJTable)
	{
			KeyStroke copy,cut, paste;
			jTable1 = myJTable;
			if(!Common.isMac()){
				copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.CTRL_MASK,false);
				cut = KeyStroke.getKeyStroke(KeyEvent.VK_X,ActionEvent.CTRL_MASK,false);
				paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.CTRL_MASK,false);
			}else{
				copy = KeyStroke.getKeyStroke(KeyEvent.VK_C,ActionEvent.META_MASK,false);
				cut = KeyStroke.getKeyStroke(KeyEvent.VK_X,ActionEvent.META_MASK,false);
				paste = KeyStroke.getKeyStroke(KeyEvent.VK_V,ActionEvent.META_MASK,false);   	  
			}
			jTable1.registerKeyboardAction(this,"Copy",copy,JComponent.WHEN_FOCUSED);
			jTable1.registerKeyboardAction(this,"Cut",cut,JComponent.WHEN_FOCUSED);
			jTable1.registerKeyboardAction(this,"Paste",paste,JComponent.WHEN_FOCUSED);
			system = Toolkit.getDefaultToolkit().getSystemClipboard();
	}
	/**
	 * Public Accessor methods for the Table on which this adapter acts.
	 */
	public JTable getJTable() {return jTable1;}
	public void setJTable(JTable jTable1) {this.jTable1=jTable1;}
	
	public void copyCut(boolean isCut){
		StringBuffer sbf=new StringBuffer();
		// Check to ensure we have selected only a contiguous block of
		// cells
		int numcols=jTable1.getSelectedColumnCount();
		int numrows=jTable1.getSelectedRowCount();
		int[] rowsselected=jTable1.getSelectedRows();
		int[] colsselected=jTable1.getSelectedColumns();
		if (!((numrows-1==rowsselected[rowsselected.length-1]-rowsselected[0] &&
				numrows==rowsselected.length) &&
				(numcols-1==colsselected[colsselected.length-1]-colsselected[0] &&
						numcols==colsselected.length))){
			JOptionPane.showMessageDialog(null, "Invalid Copy Selection",
					"Invalid Copy Selection",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		try{jTable1.getCellEditor().cancelCellEditing();}catch(Exception e){}
		for (int i=0;i<numrows;i++){
			for (int j=0;j<numcols;j++){
				Object temp =jTable1.getValueAt(rowsselected[i],colsselected[j]);
				if(isCut){
					jTable1.setValueAt(null,rowsselected[i],colsselected[j]);
				}
				sbf.append((temp==null) ? "" : temp);
				if (j<numcols-1) sbf.append("\t");
			}
			sbf.append("\n");
		}
		stsel  = new StringSelection(sbf.toString());
		system = Toolkit.getDefaultToolkit().getSystemClipboard();
		system.setContents(stsel,stsel);
	}
	
	public void cut(){ copyCut(true);}
	public void copy(){ copyCut(false);}
	
	public String getClipBoard(){
		String aString=null;
		try{aString = (String)(system.getContents(this).getTransferData(DataFlavor.stringFlavor));}catch(Exception e){}
		return aString;
	}
	
	public void paste(){
		System.out.println("Trying to Paste");
		int startRow=(jTable1.getSelectedRows())[0];
		int startCol=(jTable1.getSelectedColumns())[0];
		try{
			StringTokenizer st1;
			String trstring= getClipBoard();
			if(trstring.startsWith("\n")|trstring.startsWith("\r"))
				trstring="\t".concat(trstring);
			System.out.println("String is:"+trstring.indexOf("\r"));
			if(trstring.indexOf("\n")<0)
				st1=new StringTokenizer(trstring,"\r");
			else
				st1=new StringTokenizer(trstring,"\n");
			try{jTable1.getCellEditor().cancelCellEditing();}catch(Exception e){}
			for(int row=0;st1.hasMoreTokens();row++){
				rowstring=st1.nextToken();
				StringTokenizer st2=new StringTokenizer(rowstring,"\t",true);
				int col=0;
				String lastValue="";
				for(int j=0;st2.hasMoreTokens();j++){
					value=(String)st2.nextToken();
					if(value.indexOf("\t")<0){
						if (startRow+row< jTable1.getRowCount()  &&
								startCol+col< jTable1.getColumnCount())
							jTable1.setValueAt(value,startRow+row,startCol+col);
						System.out.println("Putting "+ value+"atrow="+(startRow+row)+"column="+(startCol+col));
						col++;
					}else if((lastValue.indexOf("\t")>=0) && (value.indexOf("\t")>=0)){
						if (startRow+row< jTable1.getRowCount()  &&
								startCol+col< jTable1.getColumnCount())
							jTable1.setValueAt(null,startRow+row,startCol+col);
						col++;                	   
					}
					if(value.indexOf("\t")>=0 && !st2.hasMoreTokens()){
						if (startRow+row< jTable1.getRowCount()  &&
								startCol+col< jTable1.getColumnCount())
							jTable1.setValueAt(null,startRow+row,startCol+col);
					}
					if(j==0 && value.indexOf("\t")>=0){
						if (startRow+row< jTable1.getRowCount()  &&
								startCol+col< jTable1.getColumnCount())
							jTable1.setValueAt(null,startRow+row,startCol+col);
						col++;
					}
					lastValue=value;
				}
			}
		}
		catch(Exception ex){ex.printStackTrace();}
	}
	
	/**
	 * This method is activated on the Keystrokes we are listening to
	 * in this implementation. Here it listens for Copy and Paste ActionCommands.
	 * Selections comprising non-adjacent cells result in invalid selection and
	 * then copy action cannot be performed.
	 * Paste is done by aligning the upper left corner of the selection with the
	 * 1st element in the current selection of the JTable.
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (e.getActionCommand().compareTo("Copy")==0 || e.getActionCommand().compareTo("Cut")==0){
			copyCut(e.getActionCommand().compareTo("Cut")==0);
		}
		if (e.getActionCommand().compareTo("Paste")==0){
			paste();
		}
	}
}
