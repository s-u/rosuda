package org.rosuda.JGR;
//
//  RObjectManager.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import org.rosuda.JGR.rhelp.*;
import org.rosuda.JGR.robjects.*;
import org.rosuda.JGR.toolkit.*;
import org.rosuda.ibase.*;
import org.rosuda.util.*;

public class RObjectManager extends iFrame implements ActionListener {


    private JTabbedPane tabArea = new JTabbedPane();
    private ToolTipTree dataTree;
    private ToolTipTree othersTree;
    private MTable modelTable = new MTable();
    private TableSorter sorter;

    private JButton close = new JButton("Close");
    private JButton refresh = new JButton("Refresh");

    public static Vector data = JGR.DATA;
    public static Vector models = JGR.MODELS;
    public static Vector others = JGR.OTHERS;


    public RObjectManager() {
        super("Object Browser",156);

        String[] Menu = {
            "+", "File", "~File.Basic.End",
            "~Window","0"};
            //"~Help", "R Help", "rhelp",/* "JJGR FAQ", "jrfaq",*/ "~About", "0"};
        iMenu.getMenu(this, this, Menu);

        close.setActionCommand("exit");
        close.addActionListener(this);
        refresh.setActionCommand("refresh");
        refresh.addActionListener(this);

        while(!JGR.STARTED);

        RTalk.refreshObjects();

        dataTree = new ToolTipTree(new DataTreeModel(this).getModel());
        dataTree.addMouseListener(new TreeMouseListener());
        dataTree.setRootVisible(true);
        FontTracker.current.add(dataTree);
        tabArea.add("Data",new JScrollPane(dataTree));

        sorter = new TableSorter(new ModelTable());
        modelTable.setShowGrid(true);
        modelTable.setModel(sorter);
        FontTracker.current.add(modelTable);
        modelTable.getTableHeader().setReorderingAllowed(false);
        sorter.setTableHeader(modelTable.getTableHeader());
        tabArea.add("Models",new JScrollPane(modelTable));


        othersTree = new ToolTipTree(new OthersTreeModel(this).getModel());
        othersTree.addMouseListener(new TreeMouseListener());
        othersTree.setRootVisible(true);
        FontTracker.current.add(othersTree);
        tabArea.add("Other",new JScrollPane(othersTree));

        ToolTipManager.sharedInstance().registerComponent(dataTree);
        ToolTipManager.sharedInstance().registerComponent(othersTree);

        addListeners(dataTree);
        addListeners(othersTree);

        JPanel buttons = new JPanel();
        buttons.add(refresh);
        buttons.add(close);

        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add(tabArea,
                                  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(5, 5, 1, 5), 0, 0));
        this.getContentPane().add(buttons,
                                  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            , GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(2, 5, 5, 5), 0, 0));
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                dispose(); //we have to ask the user something about what to do with the current file
            }
        });
        this.setSize(600,500);
        //this.show(); do it manually when you really want to see it
    }

    public void addListeners(final JTree t) {
        t.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
                    try {
                        DefaultMutableTreeNode n = (DefaultMutableTreeNode) t.getSelectionPath().getLastPathComponent();
                        RObject o= (RObject) n.getUserObject();
                        if (o.getParent() == null) {
                            cursorWait();
                            JGR.MAINRCONSOLE.execute("rm("+o.getName()+")");
                            ((DefaultTreeModel) t.getModel()).removeNodeFromParent(n);
                            cursorDefault();
                        }
                    } catch (Exception ex) {}
                }
            }
        });
    }

    public void refresh() {
        this.cursorWait();
        RTalk.refreshObjects();
        dataTree.setModel(new DataTreeModel(this).getModel());
        sorter = new TableSorter(new ModelTable());
        sorter.setTableHeader(modelTable.getTableHeader());
        modelTable.setModel(sorter);
        othersTree.setModel(new OthersTreeModel(this).getModel());
        this.cursorDefault();
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd=="exit") dispose();
        else if (cmd=="refresh") refresh();

    }

    class ModelTable extends AbstractTableModel {


        private String[] colnames = {"Name","Type","family","df","r.squared","aic","deviance"};
        private Object[] content;

        public ModelTable() {
            content= new Object[models.size()];
            for (int i = 0; i < content.length; i++) content[i] = ((model) models.toArray()[i]).getInfo();
        }

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
            try {
                if(Class.forName("java.lang.Number").isAssignableFrom((((Object[] )content[row])[col]).getClass())) {
                    DecimalFormat df = new DecimalFormat("#0.00");
                    return df.format(((Object[]) content[row])[col]).replace(',','.');
                };
            } catch (Exception e) {}
            return ((Object[]) content[row])[col];
        }

        public Class getClass(int col) {
            return ((Object) content[col]).getClass();
        }
    }

    class DataTreeModel {

        DefaultTreeModel treeModel;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("data"), df, table, v;

        public DataTreeModel(RObjectManager objmgr) {
            df = new DefaultMutableTreeNode("data.frames");
            table = new DefaultMutableTreeNode("tables");
            for (int i = 0; i < objmgr.data.size(); i++) {
                RObject obj = (RObject) objmgr.data.elementAt(i);
                if (obj.getType()==RObject.DATAFRAME) {
                    dataframe f = (dataframe) obj;
                    v = new DefaultMutableTreeNode(f);
                    for (int z = 0; z < f.vars.size(); z++) {
                        v.add(new DefaultMutableTreeNode(f.vars.elementAt(z)));
                    }
                    df.add(v);
                }
                else if (obj.getType()==RObject.TABLE) {
                    table t = (table) obj;
                    v = new DefaultMutableTreeNode(t);
                    for (int z = 0; z < t.vars.size(); z++) {
                        v.add(new DefaultMutableTreeNode(t.vars.elementAt(z)));
                    }
                    table.add(v);
                }
                else df = new DefaultMutableTreeNode(obj);
                root.add(df);
                root.add(table);
            }
            treeModel = new DefaultTreeModel(root);
        }

        public DefaultTreeModel getModel() {
            return treeModel;
        }

    }

    class OthersTreeModel {

        DefaultTreeModel treeModel;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("others"), o, v;

        public OthersTreeModel(RObjectManager objmgr) {
            for (int i = 0; i < objmgr.others.size(); i++) {
                RObject obj = (RObject) objmgr.others.elementAt(i);
                if (obj.getType()==RObject.LIST) {
                    list l = (list) obj;
                    v = new DefaultMutableTreeNode(l);
                    for (int z = 0; z < l.vars.size(); z++) {
                        v.add(new DefaultMutableTreeNode(l.vars.elementAt(z)));
                    }
                    root.add(v);
                }
                else root.add(new DefaultMutableTreeNode(obj));
            }
            treeModel = new DefaultTreeModel(root);
        }

        public DefaultTreeModel getModel() {
            return treeModel;
        }

    }

    class MTable extends JTable {

        public String getToolTipText(MouseEvent e) {
            Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            int colIndex = columnAtPoint(p);
            if (colIndex == 0) return ((model) models.elementAt(rowIndex)).getToolTip();
            return null;
        }
    }

    class ToolTipTree extends JTree implements DragGestureListener, DragSourceListener {


        DragSource dragSource;

        public ToolTipTree(TreeModel m) {
            super(m);
            dragSource = new DragSource();
            dragSource.createDefaultDragGestureRecognizer(
                this, DnDConstants.ACTION_COPY, this);
        }

        public String getToolTipText(MouseEvent e) {
            Point p = e.getPoint();
            RObject obj = null;
            try {
                obj = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this,p.x,p.y).getLastPathComponent()).getUserObject();
            } catch (Exception ex) {}
            /*if ((e.isMetaDown() || e.isControlDown())) return obj==null?null:obj.getSummary();
            else*/ return  obj==null?null:obj.getToolTip();
        }

        public void dragGestureRecognized(DragGestureEvent evt) {
            Point p = evt.getDragOrigin();
            RObject obj = null;
            try {
                obj = (RObject) ((DefaultMutableTreeNode) getUI().getClosestPathForLocation(this,p.x,p.y).getLastPathComponent()).getUserObject();
            } catch (Exception ex) {}
            String drag = null;
            if (obj != null && obj.getParent()!=null) {
                drag = ((RObject) obj.getParent()).getType()==RObject.DATAFRAME?((RObject) obj.getParent()).getName()+"$"+obj.getName():obj.getName();
            }
            else if (obj != null) drag = obj.getName();
            Transferable t = new StringSelection(obj==null?"dragFailure":drag);
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

    }

    class TreeMouseListener implements MouseListener {

        public TreeMouseListener() {
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount()==2) {
                Point p = e.getPoint();
                try {
                    dataframe d= (dataframe) ((DefaultMutableTreeNode)((JTree)e.getSource()).getSelectionPath().getLastPathComponent()).getUserObject();
                    cursorWait();
                    new DataTable(RTalk.getVarSet(d));
                    cursorDefault();

                } catch (Exception ex) {}
            }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

    }

}
