/*
 * Created on 08.08.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.rosuda.JGR.toolkit;

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
 * @author Markus
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
	
	public void refresh() {
        sorter = new TableSorter(new ModelTableModel());
        sorter.setTableHeader(this.getTableHeader());
        this.setModel(sorter);
	}
	
    public void dragGestureRecognized(DragGestureEvent evt) {
    	RModel m = (RModel) models.elementAt(this.rowAtPoint(evt.getDragOrigin()));
		if (m == null || m.getCall().trim().length() == 0) return;
		
        Transferable t = new java.awt.datatransfer.StringSelection(m.getName()+" <- "+m.getCall());
        dragSource.startDrag (evt, DragSource.DefaultCopyDrop, t, this);
    }
    public void dragEnter(DragSourceDragEvent evt) {
        // Called when the user is dragging this drag source and enters
        // the drop target.
    }
    public void dragOver(DragSourceDragEvent evt) {
        // Called when the user is dragging this drag source and moves
        // over the drop target.
    }
    public void dragExit(DragSourceEvent evt) {
        // Called when the user is dragging this drag source and leaves
        // the drop target.
    }
    public void dropActionChanged(DragSourceDragEvent evt) {
        // Called when the user changes the drag action between copy or move.
    }
    public void dragDropEnd(DragSourceDropEvent evt) {
        // Called when the user finishes or cancels the drag operation.
    }	
	
	public void mouseClicked(MouseEvent e) {
	}	
	
	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		if (objmgr.summary != null)	objmgr.summary.hide();
		if (e.isPopupTrigger()) {
			objmgr.cursorWait();
			JToolTip call  = new JToolTip();
			RModel m = (RModel) models.elementAt(this.rowAtPoint(e.getPoint()));
			if (m == null || m.getCall().trim().length() == 0) return;
			call.setTipText(m.getCall());
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
