//
//  PluginTable.java
//  Klimt
//
//  Created by Simon Urbanek on Thu Jul 03 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.plugins;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;

public class PluginTable extends Plugin implements Commander,ActionListener {
    SVarSet vs;
    int[] vars;

    class MarkerSelectionModel implements ListSelectionModel, Dependent {
        SMarker m;
        Vector ls;
        
        MarkerSelectionModel(SMarker mark) {
            m=mark;
            ls=new Vector();
            m.addDepend(this);
        }

        protected void finalize() {
            if (m!=null)
                m.delDepend(this);
            ls.removeAllElements();
            m=null;
        }

        public void Notifying(NotifyMsg msg, Object src, Vector path) {
            //System.out.println("MarkerSelectionModel.Notifying");
            int i=0;
            ListSelectionEvent lse=new ListSelectionEvent(this,0,m.size(),false);
            while (i<ls.size()) {
                ((ListSelectionListener)ls.elementAt(i)).valueChanged(lse);
                i++;
            }
        }

        int anchor, lead;

        public void setSelectionInterval(int index0, int index1) {
            //System.out.println("MarkerSelectionModel.setSelectionInterval("+index0+","+index1+")");
            m.selectNone();
            addSelectionInterval(index0,index1);
        }

        public void addSelectionInterval(int index0, int index1) {
            //System.out.println("MarkerSelectionModel.addSelectionInterval("+index0+","+index1+")");
            anchor=index0; lead=index1;
            int i=(index0<index1)?index0:index1;
            int j=(index0<index1)?index1:index0;
            while (i<=j) {
                m.set(i++,true);
            }
            m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
        }
        
        public void removeSelectionInterval(int index0, int index1) {
            //System.out.println("MarkerSelectionModel.removeSelectionInterval("+index0+","+index1+")");
            int i=(index0<index1)?index0:index1;
            int j=(index0<index1)?index1:index0;
            while (i<=j) {
                m.set(i++,false);
            }
            m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
        }
        
        public int getMinSelectionIndex() {
            if (isSelectionEmpty()) return -1;
            int i=0,j=m.size();
            while (i<j) {
                if (m.at(i)) return i;
                i++;
            }
            return -1;
        }
        
        public int getMaxSelectionIndex() {
            if (isSelectionEmpty()) return -1;
            int i=m.size()-1;
            while (i>=0) {
                if (m.at(i)) return i;
                i--;
            }
            return -1;
        }
        
        public boolean isSelectedIndex(int index) {
            return m.at(index);
        }
        
        public int getAnchorSelectionIndex() {
            //System.out.println("MarkerSelectionModel.getAnchorSelectionIndex()="+anchor);            
            return anchor;
        }
        
        public void setAnchorSelectionIndex(int index) {
            System.out.println("MarkerSelectionModel.setAnchorSelectionIndex("+index+")");
            anchor=index;
        }
        
        public int getLeadSelectionIndex() {
            System.out.println("MarkerSelectionModel.getLeadSelectionIndex()="+lead);
            return lead;
        }
        
        public void setLeadSelectionIndex(int index) {
            //System.out.println("MarkerSelectionModel.setLeadSelectionIndex("+index+") [anchor="+anchor+",lead="+lead+"]");
            if (index==lead) return;
            if (index>=anchor) {
                if (lead<anchor) removeSelectionInterval(lead,anchor-1);
                if (index<lead) {
                    removeSelectionInterval(index+1,lead);
                    lead=index;
                } else
                    addSelectionInterval(anchor,index);
            } else {
                if (lead>anchor) removeSelectionInterval(anchor+1,lead);
                if (index>lead) {
                    removeSelectionInterval(lead,index-1);
                    lead=index;
                } else
                    addSelectionInterval(anchor,index);
            }
        }
        
        public void clearSelection() {
            System.out.println("MarkerSelectionModel.clearSelection()");
            m.selectNone();
            m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
        }
        
        public boolean isSelectionEmpty() {
            return (m.marked()==0);
        }
        
        public void insertIndexInterval(int index, int length, boolean before) {
            System.out.println("insertIndexInterval: I don't really know what to do here ("+index+","+length+","+before+")");
        }
        
        public void removeIndexInterval(int index0, int index1) {
            System.out.println("removeIndexInterval("+index0+","+index1+") unsupported");
        }

        boolean isadj=false;
        
        public void setValueIsAdjusting(boolean valueIsAdjusting) {
            //System.out.println("MarkerSelectionModel.setValueIsAdjusting("+valueIsAdjusting+")");            
            isadj=valueIsAdjusting;
        }
        
        public boolean getValueIsAdjusting() {
            System.out.println("MarkerSelectionModel.getValueIsAdjusting()="+isadj);            
            return isadj;
        }
        
        public void setSelectionMode(int selectionMode) {
            System.out.println("setSelectionMode("+selectionMode+") [supported only "+ListSelectionModel.MULTIPLE_INTERVAL_SELECTION+"]");
        }
        
        public int getSelectionMode() {
            return ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
        }
        
        public void addListSelectionListener(ListSelectionListener x) {
            ls.addElement(x);
        }
        public void removeListSelectionListener(ListSelectionListener x) {
            ls.removeElement(x);
        }
    }
    
    class SVSTableModel extends AbstractTableModel {
        PluginTable t;
        int cols,rows;
        
        SVSTableModel(PluginTable pt) {
            t=pt;
            cols=(vars==null)?vs.count():vars.length;
            rows=vs.at(0).size();
        }
        public int getColumnCount() { return cols; }
        public int getRowCount() { return rows; }
        public Object getValueAt(int row, int col) { return (t.vars==null)?t.vs.at(col).at(row):t.vs.at(t.vars[col]).at(row); }
        public String getColumnName(int col) {
            return (t.vars==null)?t.vs.at(col).getName():t.vs.at(t.vars[col]).getName();
        }
    }
    
    public PluginTable() {
        name = "Table sheet plugin";
        author = "Simon Urbanek";
        desc = "Provides a table view of the data";
    }

    public void setParameter(String par, Object val) {
        if (par.equals("dataset")) vs=(SVarSet) val;
        if (par.equals("varids")) vars=(int[]) val;
    }
    
    public Object getParameter(String par) {
        if (par.equals("dataset")) return(vs);
        if (par.equals("varids")) return(vars);
        return null;
    }

    public Object run(Object o, String cmd) {
        if (cmd=="") {
        }
        return null;
    }

    public static PluginTable runNew(SVarSet v, int[] vi) {
        PluginTable pt=new PluginTable();
        pt.vs=v;
        pt.vars=vi;
        pt.execPlugin();
        return pt;
    }

    public static PluginTable runNew(SVarSet v) { return runNew(v,null); }
    
    public boolean execPlugin() {
        if (vs==null) return false;
        TFrame f = new TFrame("Table ("+vs.getName()+")",TFrame.clsTable);
        JTable table = new JTable(new SVSTableModel(this));
        table.setSelectionModel(new MarkerSelectionModel(vs.getMarker()));
        JScrollPane scrollpane = new JScrollPane(table);
        f.add(scrollpane);
        String myMenu[]={"+","File","~File.Basic.End","~Edit","+","View","@RRotate","rotate","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        f.setVisible(true);
        return true;
    }

    public void actionPerformed(ActionEvent e) {
        if (e==null) return;
        run(e.getSource(),e.getActionCommand());
    }    
}
