package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---


import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.*;
import javax.swing.text.TabExpander;

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
	private Vector fmodels;
	private TableSorter sorter;
	private JGRObjectManager objmgr;
    private DragSource dragSource;
    /** Panel which includes textfiels for column filters */
    public FilterPanel filter;
        
	public ModelBrowserTable(JGRObjectManager parent, Vector models) {
		this.models = models;
		this.fmodels = new Vector(this.models);
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

		ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        ToolTipManager.sharedInstance().setReshowDelay(0);
		
        filter = new FilterPanel(this);
        
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, this);
        
        this.addMouseListener(this);
	}
	
	/**
	 * Refresh models form workspace.
	 */
	public void refresh() {
		int[] st = new int[sorter.getColumnCount()];
		for (int i = 0; i < st.length; i++)
			st[i] = sorter.getSortingStatus(i);
        sorter = new TableSorter(new ModelTableModel());
        sorter.setTableHeader(this.getTableHeader());
        for (int i = 0; i < st.length; i++)
        	sorter.setSortingStatus(i,st[i]);
        this.setModel(sorter);
	}
	
	public String getToolTipText(MouseEvent e) {
		if (objmgr.summary != null)	objmgr.summary.hide();
        if (e.isAltDown()) {
            objmgr.cursorWait();
			JToolTip call  = new JToolTip();
			RModel m = (RModel) fmodels.elementAt(sorter.modelIndex(this.rowAtPoint(e.getPoint())));
			if (m == null || m.getToolTip().trim().length() == 0) return null;
			String tip = m.getToolTip();
			if (tip!=null) {
				objmgr.cursorDefault();
                return tip;
            }
			
			return null; 
        }
		objmgr.cursorDefault();
		return null;
	}
	
	/**
	 * dragGestureRecognized: handle dragGesture event: when dragging a model, provide the R-call.
	 */
    public void dragGestureRecognized(DragGestureEvent evt) {
    	RModel m = (RModel) fmodels.elementAt(sorter.modelIndex(this.rowAtPoint(evt.getDragOrigin())));
		if (m == null || m.getCall().trim().length() == 0) return;
		
        Transferable t = new java.awt.datatransfer.StringSelection(m.getName()+" <- "+m.getTypeName()+"("+m.getCall()+(m.getFamily()!=null?(",family="+m.getFamily()):"")+(m.getData()!=null?(",data="+m.getData()):"")+")");
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
			RModel m = (RModel) fmodels.elementAt(sorter.modelIndex(this.rowAtPoint(e.getPoint())));
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
			RModel m = (RModel) fmodels.elementAt(sorter.modelIndex(this.rowAtPoint(e.getPoint())));
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

    private String[] colnames = {"Name","Data","Type","family","df","r.squared","aic","deviance"};
    
    class ModelTableModel extends AbstractTableModel {


        public int getColumnCount() {
            return colnames.length;
        }

        public int getRowCount() {
            return fmodels.size();
        }

        public String getColumnName(int col) {
            return colnames[col];
        }

        public Object getValueAt(int row, int col) {
            return ((Vector) ((RModel) fmodels.elementAt(row)).getInfo()).elementAt(col); 
        }

        public Class getColumnClass(int col) {
            int i = 0;
            while (((Vector) ((RModel) fmodels.elementAt(i)).getInfo()).elementAt(col) == null) i++;
            if (i > getRowCount()) return null;
            return ((Vector) ((RModel) fmodels.elementAt(i)).getInfo()).elementAt(col).getClass();
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
    
    class FilterPanel extends JPanel implements KeyListener {
    	
    	JTextField name = new JTextField();
    	JTextField data = new JTextField();
    	JTextField type = new JTextField();
    	JTextField family = new JTextField();
    	
    	JTextField[] filters = {name,data,type,family};
    	
    	ModelBrowserTable table;
    	
    	public FilterPanel(ModelBrowserTable table) {
    		family.setNextFocusableComponent(name);
    		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
    		fl.setVgap(0);
    		fl.setHgap(0);
    		this.setLayout(fl);
    		this.table = table;
    		this.add(name);
    		this.add(data);
    		this.add(type);
    		this.add(family);
    		name.addKeyListener(this);
    		data.addKeyListener(this);
    		type.addKeyListener(this);
    		family.addKeyListener(this);
            this.addComponentListener(new ComponentAdapter() {
            	public void componentResized(ComponentEvent e) {
            		resizeFields();
            	}
            });
    	}
    	
    	public void resizeFields() {
    		name.setPreferredSize(new Dimension(table.getColumn(colnames[0]).getWidth(),25));
    		data.setPreferredSize(new Dimension(table.getColumn(colnames[1]).getWidth(),25));
    		type.setPreferredSize(new Dimension(table.getColumn(colnames[2]).getWidth(),25));
    		family.setPreferredSize(new Dimension(table.getColumn(colnames[3]).getWidth(),25));
    	}
    	
    	public void setSize(Dimension d) {
    		resizeFields();
    		super.setSize(d);
    	}
    	
    	public void setSize(int w, int h) {
    		resizeFields();
    		super.setSize(w,h);
    	}
    	
    	private void filterModels() {
    		fmodels.clear();
    		Iterator i = models.iterator();
    		while (i.hasNext()) {
    			RModel r = (RModel) i.next();
    			if (matches(r.getInfo()))
    				fmodels.add(r);
    		}
    		table.refresh();
    	}
    	
    	private boolean matches(Vector v) {
    		for (int i = 0; i < filters.length; i++) {
    			String f = filters[i].getText();
    			String m = (String) v.elementAt(i);
    			if (f != null && f.trim().length() > 0 && (m == null || m.trim().length() == 0)) return false;
    			if (f != null && m != null && !compareF(m.trim(),f.trim())) return false;
    		}
    		return true;
    	}
    	
    	private boolean compareF(String s1, String s2) {
    		if (s1.startsWith(s2)) return true;
    		try {
    			if (s1.matches(s2)) return true;
    		} catch (Exception e) {} // if there's an regex exception
    		return false;
    	}

		/**
		 * keyTyped: handle key event.
		 */
		public void keyTyped(KeyEvent e) {
		}

		/**
		 * keyPressed: handle key event.
		 */
		public void keyPressed(KeyEvent e) {
		}

		/**
		 * keyReleased: handle key event: filter models.
		 */
		public void keyReleased(KeyEvent e) {
			filterModels();
		}
    }
}
