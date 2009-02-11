package org.rosuda.JGR.data;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.rosuda.ibase.Common;
import org.rosuda.JGR.JGR;
import org.rosuda.JGR.data.ColumnHeaderListener.ColumnContextMenu;



public class ExScrollableTable extends JScrollPane{

	private ExTable table;
	private RowNamesListModel rowNamesHeaderModel;
	private JList rowHeader;
	private static int widthMin=40;
	private static int widthMult=8;
	
	public ExScrollableTable(ExTable t){
		super();
		table=t;
		setViewportView(table);
		table.parentPane=this;
		rowNamesHeaderModel = new RowNamesListModel();
		rowNamesHeaderModel.initHeaders(table.getRowCount());
		rowHeader = new JList(rowNamesHeaderModel);
		rowHeader.setFixedCellWidth(Math.max(widthMin, rowNamesHeaderModel.getMaxNumChar()*widthMult+10));
	    rowHeader.setFixedCellHeight(table.getRowHeight());
	    rowHeader.setCellRenderer(new RowHeaderRenderer(table));
	    setRowHeaderView(rowHeader);
	    new RowListener();	    
	}
	
	public ExTable getExTable(){return table;}
	
	public RowNamesListModel getRowNamesModel(){return rowNamesHeaderModel;}
	public void setRowNamesModel(RowNamesListModel model){
		rowNamesHeaderModel=model;
		rowHeader = new JList(rowNamesHeaderModel);
		rowHeader.setFixedCellWidth(Math.max(widthMin, rowNamesHeaderModel.getMaxNumChar()*widthMult+10));
	    rowHeader.setFixedCellHeight(table.getRowHeight());
	    rowHeader.setCellRenderer(new RowHeaderRenderer(table));
	    setRowHeaderView(rowHeader);
	    new RowListener();
	}
	
	public void insertNewRow(int index) {
		table.insertNewRow(index);
		getRowNamesModel().refresh();
	}
	
	public void insertRow(int index){
		if(table.getCopyPasteAdapter().getClipBoard().indexOf("\n")<(table.getCopyPasteAdapter().getClipBoard().length()-2) && 
				table.getCopyPasteAdapter().getClipBoard().length()>0){
			JOptionPane.showMessageDialog(null, "Invalid Row Insertion",
					"Invalid Insertion Selection",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		insertNewRow(index);
		table.selectRow(index);
		table.requestFocus();
		table.getCopyPasteAdapter().paste();
	}
	
	
	
	/**
	 * This Class Renders the Row Headers for the table.
	 * 
	 *
	 */
	class RowHeaderRenderer extends JLabel implements ListCellRenderer {
		   
		RowHeaderRenderer(JTable table) {
			JTableHeader header = table.getTableHeader();
			setOpaque(true);
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setHorizontalAlignment(CENTER);
			setForeground(header.getForeground());
			setBackground(header.getBackground());
			setFont(header.getFont());
		}
		
		public Component getListCellRendererComponent( JList list, 
				Object value, int index, boolean isSelected, boolean cellHasFocus) {
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}


	/**
	 * This can be extended further. currently all
	 * that is allowed is single row selection.
	 *
	 */
	class RowListener extends MouseAdapter {

		public RowListener(){
			rowHeader.addMouseListener(this);
		}
		
		
		public void mouseClicked(MouseEvent evt){
			if(evt.getButton()==MouseEvent.BUTTON3 && !Common.isMac()){
				int selectedRow =rowHeader.locationToIndex(evt.getPoint());
				//JGR.R.eval("print('"+selectedRow+"')");
				table.requestFocus();
				table.selectRow(selectedRow);
				new RowContextMenu(evt,selectedRow);

			}
		}
		
		
		public void mousePressed(MouseEvent evt){
			int selectedRow =rowHeader.locationToIndex(evt.getPoint());
			//JGR.R.eval("print('"+selectedRow+"')");
			table.requestFocus();			
			table.selectRow(selectedRow);
			if(evt.isPopupTrigger() && Common.isMac()){
				new RowContextMenu(evt,selectedRow);	
			}
			
		}
	}
	
	class RowContextMenu implements ActionListener{
		int index;
		private JPopupMenu menu;;
		
		public RowContextMenu(MouseEvent evt,int selectedRow){
			index = selectedRow;
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
			JMenuItem insertNewItem = new JMenuItem ("Insert New Row");
			insertNewItem.addActionListener(this);
			menu.add( insertNewItem );
			JMenuItem removeItem = new JMenuItem ("Remove Row");
			removeItem.addActionListener(this);
			menu.add( removeItem );
			menu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
		
		public void actionPerformed(ActionEvent e){
			//index =rowHeader.get .getSelectedIndex();
			JMenuItem source = (JMenuItem)(e.getSource());
			System.out.println("row Contextual Menu selected: "+index);
			if(source.getText()=="Copy"){
				table.getCopyPasteAdapter().copy();
			} else if(source.getText()=="Cut"){
				table.cutRow(index);
			} else if(source.getText()=="Paste"){
				table.getCopyPasteAdapter().paste();
			} else if(source.getText()=="Insert"){
				insertRow(index);
			} else if(source.getText()=="Insert New Row"){
				insertNewRow(index);
			} else if(source.getText()=="Remove Row"){
				table.removeRow(index);
				getRowNamesModel().refresh();
			}
			menu.setVisible(false);
		}
		
	}
	
	
	
}
