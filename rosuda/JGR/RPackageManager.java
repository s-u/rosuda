package org.rosuda.JGR;

//
//  RObjectManager.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.rosuda.JGR.rhelp.*;
import org.rosuda.JGR.toolkit.*;
import org.rosuda.ibase.*;
import org.rosuda.util.*;

public class RPackageManager extends iFrame implements ActionListener {

    public Object[][] Packages = null;
    public String[] columnNames = {"loaded","Package","Description"};
    private JScrollPane scrollArea = new JScrollPane();
    private JButton close = new JButton("Close");
    private JButton refresh = new JButton("Refresh");
    private TableSorter sorter;
    private PTableModel pkgModel;
    private JTable pkgTable = new JTable();

    public RPackageManager() {
        super("Package Manager",157);
        try {
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

        Packages = RTalk.refreshPackages();

        sorter = new TableSorter(pkgModel = new PTableModel(this));
        scrollArea.setBackground(this.getBackground());
        pkgTable.setBackground(this.getBackground());
        pkgTable.setColumnModel(new PTableColumnModel());
        pkgTable.setModel(sorter);
        pkgTable.setShowGrid(false);
        pkgTable.setCellSelectionEnabled(false);
        pkgTable.setColumnSelectionAllowed(false);
        pkgTable.setRowSelectionAllowed(false);
        pkgTable.setFocusable(false);
        pkgTable.getTableHeader().setReorderingAllowed(false);
        sorter.setTableHeader(pkgTable.getTableHeader());

        scrollArea.getViewport().setBackground(this.getBackground());
        scrollArea.getViewport().add(pkgTable);

        JPanel buttons = new JPanel();
        buttons.add(refresh);
        buttons.add(close);

        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().add(scrollArea,
                                  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(5, 5, 1, 5), 0, 0));
        this.getContentPane().add(buttons,
                                  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            , GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(2, 5, 5, 5), 0, 0));

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.getRootPane().setDefaultButton(close);
        this.setSize(400,450);
        } catch (Exception e) { e.printStackTrace();}//this.show(); //do it manually when you really want to see it
    }

    public void refresh() {
        this.cursorWait();
        Packages = RTalk.refreshPackages();
        sorter = new TableSorter(pkgModel = new PTableModel(this));
        pkgTable.setModel(sorter);
        sorter.setTableHeader(pkgTable.getTableHeader());
        this.cursorDefault();
    }

    public void setPKGStatus(String pkg,String load) {
        this.cursorWait();
        if (load.equals("true")) JGR.MAINRCONSOLE.execute("library("+pkg+")");
        else JGR.MAINRCONSOLE.execute("detach(\"package:"+pkg+"\")");
        this.cursorDefault();
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd=="exit") dispose();
        else if (cmd=="refresh") refresh();

    }


    class PTableModel extends DefaultTableModel {

        public int cols, rows;

        public PTableModel(RPackageManager pm) {
            cols = pm.columnNames.length;
            rows = pm.Packages.length;
        }

        public Object getValueAt(int row, int col) {
            return Packages[row][col];
        }

        public int getColumnCount() {
            return cols; //columnNames.length;
        }

        public int getRowCount() {
            return rows; //Packages.length;
        }

        public void setValueAt(Object value, int row, int col) {
            setPKGStatus(getValueAt(row,1).toString(),value.toString());
            Packages[row][col] = value;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public boolean isCellEditable(int row, int col) {
            return col > 0 ? false : true;
        }

 	public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

    }

    class PTableColumnModel extends DefaultTableColumnModel {

        public PTableColumnModel() {
        }

        public void addColumn(TableColumn col) {
            if (col.getModelIndex() == 0) {
                col.setMinWidth(50);
                col.setPreferredWidth(50);
                col.setMaxWidth(50);
            }
            else if (col.getModelIndex() == 1) {
                col.setMinWidth(100);
                col.setMaxWidth(100);
            }
            super.addColumn(col);
        }
    }
}