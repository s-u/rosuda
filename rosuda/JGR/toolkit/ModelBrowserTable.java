package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---


import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.*;

import org.rosuda.JGR.JGRObjectManager;
import org.rosuda.JGR.robjects.RModel;
import org.rosuda.JGR.util.*;

/**
 *  ModelBrowserTable - show models, and provide possibility to compare them
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDa 2003 - 2004 
 */

public class ModelBrowserTable extends JTable implements MouseListener, DragGestureListener, DragSourceListener { 
	
	private Vector models;
	private TableSorter sorter;
	private JGRObjectManager objmgr;
    private DragSource dragSource;

    
	public ModelBrowserTable(JGRObjectManager parent, Vector models) {
		this.models = models;
		this.objmgr = parent;
        this.setColumnModel(new ModelTableColumnModel());
        sorter = new TableSorter(new ModelTableModel());
        this.setShowGrid(true);
        this.setModel(sorter);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        FontTracker.current.add(this);
        this.setRowHeight((int) (JGRPrefs.FontSize*1.5));
        this.getTableHeader().setReorderingAllowed(false);
        sorter.setTableHeader(this.getTableHeader());

        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
        
        this.addMouseListener(this);
	}
	
	/**
	 * Refresh models form workspace.
	 */
	public void refresh() {
        sorter = new TableSorter(new ModelTableModel());
        sorter.setTableHeader(this.getTableHeader());
        this.setModel(sorter);
	}
	
	/**
	 * dragGestureRecognized: handle dragGesture event: when dragging a model, provide the R-call.
	 */
    public void dragGestureRecognized(DragGestureEvent evt) {
    	RModel m = (RModel) models.elementAt(this.rowAtPoint(evt.getDragOrigin()));
		if (m == null || m.getCall().trim().length() == 0) return;
		
        Transferable t = new java.awt.datatransfer.StringSelection(m.getName()+" <- "+m.getCall());
        dragSource.startDrag (evt, DragSource.DefaultCopyDrop, t, this);
    }
    
    /**
     * dragEnter: handle drag source drag event.
     */
    public void dragEnter(DragSourceDragEvent evt) {
        // Called when the user is dragging this drag source and enters
        // the drop target.
    }
    
    /**
     * dragOver: handle drag source drag event.
     */
    public void dragOver(DragSourceDragEvent evt) {
        // Called when the user is dragging this drag source and moves
        // over the drop target.
    }
    
    
    /**
     * dragExit: handle drag source event.
     */
    public void dragExit(DragSourceEvent evt) {
        // Called when the user is dragging this drag source and leaves
        // the drop target.
    }
    
    
    /**
     * dragActionChanged: handle drag source drag event.
     */
    public void dropActionChanged(DragSourceDragEvent evt) {
        // Called when the user changes the drag action between copy or move.
    }
    
    
    /**
     * dragDropEnd: handle drag source drop event.
     */
    public void dragDropEnd(DragSourceDropEvent evt) {
        // Called when the user finishes or cancels the drag operation.
    }	
	
    /**
     * mouseClicked: handle mouse event.
     */
    public void mouseClicked(MouseEvent e) {
	}	
	
    /**
     * mouseEntered: handle mouse event.
     */
    public void mouseEntered(MouseEvent e) {
	}

    /**
     * mouseExited: handle mouse event.
     */
    public void mouseExited(MouseEvent e) {
	}

    /**
     * mousePressed: handle mouse event: show call of model.
     */
    public void mousePressed(MouseEvent e) {
		if (objmgr.summary != null)	objmgr.summary.hide();
		if (e.isPopupTrigger()) {
			objmgr.cursorWait();
			JToolTip call  = new JToolTip();
			RModel m = (RModel) models.elementAt(this.rowAtPoint(e.getPoint()));
			if (m == null || m.getToolTip().trim().length() == 0) return;
			String tip = m.getToolTip();
			if (tip==null) {
				objmgr.cursorDefault();
				return;
			}
			call.setTipText(m.getToolTip());
			Point p = e.getPoint();
			SwingUtilities.convertPointToScreen(p,this);
			objmgr.summary = PopupFactory.getSharedInstance().getPopup(this,call,p.x+20,p.y+25);
			objmgr.summary.show();
			objmgr.cursorDefault();
		}
     }

    /**
     * dragEnter: handle drag source event: show call of model.
     */
    public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			objmgr.cursorWait();
			JToolTip call  = new JToolTip();
			RModel m = (RModel) models.elementAt(this.rowAtPoint(e.getPoint()));
			if (m == null || m.getToolTip().trim().length() == 0) return;
			String tip = m.getToolTip();
			if (tip==null) {
				objmgr.cursorDefault();
				return;
			}
			call.setTipText(m.getToolTip());
			Point p = e.getPoint();
			SwingUtilities.convertPointToScreen(p,this);
			objmgr.summary = PopupFactory.getSharedInstance().getPopup(this,call,p.x+20,p.y+25);
			objmgr.summary.show();
			objmgr.cursorDefault();
		}
	}	

    class ModelTableModel extends AbstractTableModel {


        private String[] colnames = {"Name","Data","Type","family","df","r.squared","aic","deviance"};

        public int getColumnCount() {
            return colnames.length;
        }

        public int getRowCount() {
            return models.size();
        }

        public String getColumnName(int col) {
            return colnames[col];
        }

        public Object getValueAt(int row, int col) {
            return ((Vector) ((RModel) models.elementAt(row)).getInfo()).elementAt(col); 
        }

        public Class getColumnClass(int col) {
            int i = 0;
            while (((Vector) ((RModel) models.elementAt(i)).getInfo()).elementAt(col) == null) i++;
            if (i > getRowCount()) return null;
            return ((Vector) ((RModel) models.elementAt(i)).getInfo()).elementAt(col).getClass();
        }
    }
	
    class ModelTableColumnModel extends DefaultTableColumnModel {

        public ModelTableColumnModel() {
        }

        public void addColumn(TableColumn col) {
            col.setCellRenderer(new DefaultTableCellRenderer());
            if (col.getModelIndex() == 0) {
                col.setPreferredWidth(100);
            }
            else if (col.getModelIndex() == 1) {
                col.setPreferredWidth(80);
            }
            else if (col.getModelIndex() == 2) {
                col.setPreferredWidth(50);
            }
            else if (col.getModelIndex() == 4) {
                col.setPreferredWidth(40);
            }
            super.addColumn(col);
        }

        public TableColumn getColumn(int index) {
            return super.getColumn(index < 0 ? 0 : index);
        }
    }
}
