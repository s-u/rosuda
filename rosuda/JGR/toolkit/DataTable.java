package org.rosuda.JGR.toolkit;

//
//  DataTable.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.JGR.*;

public class DataTable extends iFrame implements ActionListener, MouseListener,
    KeyListener, TableColumnModelListener {

    private GridBagLayout layout = new GridBagLayout();
    private JScrollPane scrollArea = new JScrollPane();
    private JTable dataTable = new JTable();
    private JTableHeader tableHeader = dataTable.getTableHeader();
    private JButton save = new JButton();

    /** current used SVarSet*/
    private SVarSet vs;

    /** DataTableModel*/
    private DataTableModel tabModel;
    /** DataTableColumnModel*/
    private DataTableColumnModel columnModel;
    /** DataTableCellEditor currently we use a JTextField*/
    private DataTableCellEditor cell = new DataTableCellEditor();

    private TableSorter sorter;

    /** currently selected Columnindex*/
    private static int selectedColumn = -1;

    /** currently searchindex, we have to know it because of search next*/
    private static int searchIndex[] = {
        -1, -1};
    /** also we have to know the keyword*/
    private static String searchString = "";

    /** current filename */
    private String fileName = null;

    /** current directory for the FileDialog */
    private static String directory = System.getProperty("user.home");

    private boolean modified = false;


    /** create a Table without a SVarSet*/
    public DataTable() {
        this(null);
    }

    /** create Table with specified SVarSet*/
    public DataTable(SVarSet vs) {
        super("DataTable Editor", 153);
        if (vs == null) {
            vs = new SVarSet();
            vs.setName("Data");
            //vs.add(new SVarObj("Var0",false,false));
            //vs.insertCaseAt(0);
            save.setText("Save");
            save.setToolTipText("Save");
            save.setActionCommand("saveData");
        }
        else {
            save.setText("Update");
            save.setToolTipText("Update");
            save.setActionCommand("updateToR");
            this.setTitle("DataTable - "+vs.getName().replaceFirst("jgr_temp",""));
        }
        this.vs = vs;


        String myMenu[] = {
            "+", "File", "@OOpen", "loadData", "@SSave", "saveData",
            "!SSave as", "saveDataAs", "~File.Basic.End",
            "~Edit",
            "+", "Tools", "Add Column", "addCol","Remove Column","rmCol", "Add Row", "addRow", "Remove Row","rmRow","-",
            "@FFind", "find", "@GFind Next", "findnext", "-",
            "Goto Case", "gotoCase",
            "~Window",
            "~Help", "R Help", "rhelp", /*"JJGR FAQ", "jrfaq",*/ "~About", "0"};
        iMenu.getMenu(this, this, myMenu);
        iMenu.getItem(this, "undo").setEnabled(false);
        iMenu.getItem(this, "redo").setEnabled(false);

        FontTracker.current.add(dataTable);

        save.addActionListener(this);

        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        columnModel = new DataTableColumnModel(this);
        dataTable.setFont(iPreferences.DefaultFont);
        dataTable.setColumnModel(columnModel);
        sorter = new TableSorter(tabModel = new DataTableModel(this));
        dataTable.setModel(sorter);
        sorter.setTableHeader(tableHeader);
        dataTable.setToolTipText(vs.getName());
        dataTable.setShowGrid(true);
        dataTable.setRowHeight((int) (iPreferences.FontSize*1.5));
        dataTable.setColumnSelectionAllowed(true);
        dataTable.setRowSelectionAllowed(true);
        dataTable.setCellSelectionEnabled(true);
        dataTable.addMouseListener(this);
        dataTable.addKeyListener(this);
        tableHeader.addMouseListener(this);

        //((JTextComponent) cell.getComponent()).setDocument(new DataTableCellDocument());
        cell.getComponent().addMouseListener(this);
        cell.getComponent().addKeyListener(this);

        //try { scrollArea.setWheelScrollingEnabled(true); } catch (Exception e) {}
        scrollArea.getViewport().add(dataTable);

        this.getContentPane().setLayout(layout);
        this.getContentPane().add(new JScrollPane(dataTable),
                                  new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0,
            GridBagConstraints.WEST, GridBagConstraints.BOTH,
            new Insets(5, 5, 2, 5), 0, 0));
        this.getContentPane().add(save,
                                  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(2, 5, 5, 5), 0, 0));

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exit(); //we have to ask the user something about what to do with the current file
            }

            public void windowDeactivated(java.awt.event.WindowEvent evt) {
            /*try {
                dataTable.setRowSelectionInterval(0, 0);
                dataTable.setColumnSelectionInterval(0, tabModel.cols - 1);
                             }
                             catch (Exception ex) {
                new iError(ex);
                             }*/
            }
        });
        this.addKeyListener(this);
        this.setLocation(this.getLocation().x, 10);
        int h = dataTable.getRowHeight();
        int rc = dataTable.getRowCount();
        int cc = dataTable.getColumnCount();
        int width = cc * 77;
        int height = (int) (rc * h * 1.5);
        Dimension d = new Dimension((width < 400 && cc < 2)?400:width, (height < 400 && rc < 11)?300:height);
        this.setSize(new Dimension(d.width > Common.screenRes.width?
                                   Common.screenRes.width - 50 : d.width,
                                   d.height > Common.screenRes.height ?
                                   Common.screenRes.height - 50 : d.height));

        this.show();
    }

    /** add a column (we allow numerical variables or strings), we add this column after the selected one, if none is selected we  add at the end*/
    private void addColumn() {
        String[] val = new NewColumnDialog(this).showInputDialog();
        String[] varType = {"Numeric (double)", "Numeric (integer)", "Factor"};
        if (val != null) {
            SVar v;
            if (val[1].equals(varType[0])) v = new SVarDouble(null, dataTable.getRowCount());
            else if (val[1].equals(varType[1])) v = new SVarInt(null, dataTable.getRowCount());
            else v = new SVarFact(null, dataTable.getRowCount());
            if (val[0].equals("")) vs.insert(selectedColumn < 1 ? tabModel.cols - 1 : selectedColumn,v);
            else vs.insert(val[0], selectedColumn < 1 ? tabModel.cols - 1 : selectedColumn, v);
            refresh();
        }
    }

    /** add a row, we add rows after the selected one, if none is selected we  add at the bottom*/
    private void addRow() {
        int selectedRow = currentRow();
        vs.insertCaseAt(selectedRow + 1);
        refresh();
    }

    /** get current selected column*/
    public int currentCol() {
        return dataTable.getSelectedColumn();
    }

    /** get current selected column,
     * @param e MouseEvent */
    public int currentCol(MouseEvent e) {
        if (e.getSource().equals(tableHeader)) {
            return tableHeader.columnAtPoint(e.getPoint());
        }
        return dataTable.getSelectedColumn() == -1 ?
            tableHeader.columnAtPoint(e.getPoint()) :
            dataTable.getSelectedColumn();
    }

    /** get selected range of columns*/
    public int[] currentCols() {
        return dataTable.getSelectedColumns();
    }

    /** get current row*/
    public int currentRow() {
        return dataTable.getSelectedRow();
    }

    /** get current selected range of rows*/
    public int[] currentRows() {
        return dataTable.getSelectedRows();
    }

    /** delete content of selected cells
     * @param cols colrange
     * @param rows rowrange */
    private void deleteContent(int[] cols, int[] rows) {
        for (int i = 0; i < cols.length; i++) {
            if (cols[i] != 0) {
                for (int z = 0; z < rows.length; z++) {
                    vs.at(cols[i] - 1).replace(rows[z], null);
                }
            }
        }
        refresh();
    }


    public void exit() {
        if (modified) {
            int i;
            if (save.getText()=="Save") i = JOptionPane.showConfirmDialog(this,"Save data?","Exit",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
            else i = JOptionPane.showConfirmDialog(this,"Send back to R?","Exit",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
            if (i==1) dispose();
            else {
                if (i == 0 && save.getText()=="Update" && updateToR()) dispose();
                else if (i == 0 && saveData()) dispose();
            }	
        }
        else dispose();
    }

    /** find a specified object in the table
     * @param x x-coordinate we begin to search from this index
     * @param y y-coordinate we begin to search from this index*/
    private void find(int x, int y) {
        if (searchString == "" || y == -1) {
            searchString = ( (String) JOptionPane.showInputDialog(new
                JTextField(), "Search:", "Search for:",
                JOptionPane.PLAIN_MESSAGE));
        }
        if (searchString != null) {
            int[] f_index = vs.whereis(searchString, x == -1 ? 0 : x,
                                       y == -1 ? 0 : y);
            if (f_index[0] == -1 && f_index[1] == -1) {
                JOptionPane.showMessageDialog(this, "Not found", "Result",
                                              JOptionPane.WARNING_MESSAGE);
            } else {
                gotoCell(f_index[0] + 1, f_index[1]);
            }
            searchIndex[0] = f_index[0];
            if (f_index[0] + 1 == tabModel.cols - 1) {
                searchIndex[0] = -1;
                searchIndex[1] = f_index[1] + 1;
            } else {
                searchIndex[0] = f_index[0] + 1;
                searchIndex[1] = f_index[1];
            }
        }
    }

    /** selecte case (row)
     * @param c row which should be searched*/
    private void gotoCase(int c) {
        String val = null;
        JScrollBar vscroll = scrollArea.getVerticalScrollBar();
        int rowHeight = dataTable.getRowHeight();
        if (c == -1) {
            val = ( (String) JOptionPane.showInputDialog(new JTextField(),
                "Goto Case:", "Goto Case", JOptionPane.PLAIN_MESSAGE));
        }
        if (val != null) {
            try {
                int Case = c == -1 ? Integer.parseInt(val) - 1 : c;
                dataTable.setRowSelectionInterval(Case, Case);
                dataTable.setColumnSelectionInterval(0, tabModel.cols - 1);
                vscroll.setValue(Case * rowHeight - vscroll.getVisibleAmount() +
                                 10 < 0 ? 0 :
                                 Case * rowHeight - vscroll.getVisibleAmount() +
                                 rowHeight + 10);
            } catch (Exception e) {
                return;
            }
        }
    }

    /** selected cell
     * @param x x-coordinate
     * @param y y-coordinate*/
    private void gotoCell(int col, int row) {
        int rowHeight = dataTable.getRowHeight();
        dataTable.setRowSelectionInterval(row, row);
        JScrollBar vscroll = scrollArea.getVerticalScrollBar();
        vscroll.setValue(row * rowHeight - vscroll.getVisibleAmount() + 10 < 0 ?
                         0 :
                         row * rowHeight - vscroll.getVisibleAmount() +
                         rowHeight +
                         10);
        dataTable.setColumnSelectionInterval(col, col);
        JScrollBar hscroll = scrollArea.getHorizontalScrollBar();
        hscroll.setValue(col * 75 - hscroll.getVisibleAmount() + 30 < 0 ? 0 :
                         col * 75 - hscroll.getVisibleAmount() + 75 + 10);
    }

    public void loadData() {
        FileSelector fopen = new FileSelector(this,"Open...",FileSelector.OPEN,directory);
        if (fopen.getFile() != null) {
            this.cursorWait();
            fileName = (directory = fopen.getDirectory()) + fopen.getFile();
            try {
                vs = new SVarSet();
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                Loader.LoadTSV(br, vs, true);
                br.close();
                vs.setMarker(new SMarker(vs.at(0).size()));
                vs.setName(fileName);
                this.setTitle("DataTable - "+vs.getName());
                refresh();
                dataTable.setModel(sorter);
            } catch (Exception e) {
                new iError(e);
            }
            this.cursorDefault();
        }
    }

    /** move the vars in the SVarSet
     * @param from old column index
     * @param to new column index */
    private void moveColumns(int from, int to) {
        vs.move(from - 1, to - 1);
        refresh();
    }

    private void refresh() {
        sorter = new TableSorter(tabModel = new DataTableModel(this));
        dataTable.setModel(sorter);
        sorter.setTableHeader(tableHeader);
    }

    /** rename columnheader
     * @param index columnindex*/
    private void renameColumn(int index) {
        String old_name = vs.at(index - 1).getName();
        String val = (String) JOptionPane.showInputDialog(new JTextField(),
            "Rename Column into:", "Rename Column", JOptionPane.PLAIN_MESSAGE, null, null,
            old_name);
        if (val != null) {
            vs.at(index - 1).setName(val);
            refresh();
        }
    }

    /** remove selected columns*/
    private void removeColumns() {
        int[] selectedColumns = currentCols();
        if (selectedColumns.length == 0) {
            selectedColumns = new int[1];
            selectedColumns[0] = selectedColumn;
        }
        for (int i = 0; i < selectedColumns.length; i++) {
            if (selectedColumns[i] > 0) {
                vs.remove(selectedColumns[i] - 1 - i);
            }
        }
        refresh();
    }

    /** remove selected rows*/
    private void removeRows() {
        int[] selectedRows = currentRows();
        for (int i = selectedRows.length; i > 0; i--) {
            vs.removeCaseAt(selectedRows[i - 1]);
        }
        refresh();
    }

    /**save file*/
    public boolean saveData() {
        if (fileName == null || fileName.equals("")) return saveDataAs();
        else {
            this.cursorWait();
            BufferedWriter out = null;
            String s = "";
            try {
                out = new BufferedWriter(new FileWriter(fileName));
                int cols = vs.count();
                for (int k = 0; k < cols - 1; k++) {
                    out.write(vs.at(k).getName() + "\t");
                }
                out.write(vs.at(cols - 1).getName() + "\n");
                out.flush();
                for (int i = 0; i < vs.length(); i++) {
                    for (int z = 0; z < cols - 1; z++) {
                        s = vs.at(z).at(i) == null ? " " :
                            vs.at(z).at(i).toString();
                        out.write(s + "\t");
                    }
                    s = vs.at(cols - 1).at(i) == null ? " " :
                        vs.at(cols - 1).at(i).toString();
                    out.write(s + "\n");
                    out.flush();
                    out.close();
                }
                out.flush();
            } catch (Exception e) {
                new iError(e);
            } finally {
                this.cursorDefault();
            }
            return true;
        }
    }

    /** save file as with a new filename*/
    public boolean saveDataAs() {
        FileSelector fsave = new FileSelector(this,"Save as...",FileSelector.SAVE,directory);
        if (fsave.getFile() != null) {
            fileName = (directory = fsave.getDirectory()) + fsave.getFile();
            return saveData();
        }
        return false;
    }

    public boolean updateToR() {
        return RTalk.putToR(vs);
    }

    /** PopupMenu
     * @param e MouseEvent */
    private void popUpMenu(MouseEvent e) {
        JPopupMenu tabMenue = new JPopupMenu();
        JMenuItem renameColItem = new JMenuItem();
        JMenuItem addColItem = new JMenuItem();
        JMenuItem rmColItem = new JMenuItem();
        JMenuItem addRowItem = new JMenuItem();
        JMenuItem rmRowItem = new JMenuItem();
        JMenuItem pasteItem = new JMenuItem();
        JMenuItem titleItem = new JMenuItem();
        JMenuItem copyItem = new JMenuItem();
        JMenuItem cutItem = new JMenuItem();

        renameColItem.setToolTipText("Rename Column");
        renameColItem.setActionCommand("renameCol");
        renameColItem.setText("Rename Column");
        renameColItem.addActionListener(this);
        addColItem.setToolTipText("Insert Column");
        addColItem.setActionCommand("addCol");
        addColItem.setText("Insert Column");
        addColItem.addActionListener(this);
        rmColItem.setToolTipText("Remove Column");
        rmColItem.setActionCommand("rmCol");
        rmColItem.setText("Remove Column");
        rmColItem.addActionListener(this);
        addRowItem.setToolTipText("Insert Row");
        addRowItem.setActionCommand("addRow");
        addRowItem.setText("Insert Row");
        addRowItem.addActionListener(this);
        rmRowItem.setActionCommand("rmRow");
        rmRowItem.setText("Remove Row");
        rmRowItem.setToolTipText("Remove Row");
        rmRowItem.addActionListener(this);
        cutItem.setToolTipText("Cut");
        cutItem.setActionCommand("cut");
        cutItem.setText("Cut");
        cutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke('X',
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        cutItem.addActionListener(this);
        copyItem.setToolTipText("Copy");
        copyItem.setActionCommand("copy");
        copyItem.setText("Copy");
        copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke('C',
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        copyItem.addActionListener(this);
        pasteItem.setToolTipText("Paste");
        pasteItem.setActionCommand("paste");
        pasteItem.setText("Paste");
        pasteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke('V',
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        pasteItem.addActionListener(this);
        titleItem.setEnabled(false);

        tabMenue.add(titleItem);
        tabMenue.addSeparator();
        tabMenue.add(cutItem);
        tabMenue.add(copyItem);
        tabMenue.add(pasteItem);
        tabMenue.addSeparator();
        tabMenue.add(renameColItem);
        tabMenue.addSeparator();
        tabMenue.add(addColItem);
        tabMenue.add(rmColItem);
        tabMenue.addSeparator();
        tabMenue.add(addRowItem);
        tabMenue.add(rmRowItem);

        if (!e.getSource().equals(cell.getComponent())) {
            cutItem.setEnabled(false);
            copyItem.setEnabled(false);
            pasteItem.setEnabled(false);
            if (e.getSource().equals(tableHeader)) {
                renameColItem.setEnabled(true);
                addColItem.setEnabled(true);
                rmColItem.setEnabled(true);
                if (currentCol(e) == 0) {
                    addRowItem.setEnabled(true);
                    rmRowItem.setEnabled(true);
                } else {
                    addRowItem.setEnabled(false);
                    rmRowItem.setEnabled(false);
                }
            } else {
                if (dataTable.getSelectedRowCount() == tabModel.rows) {
                    renameColItem.setEnabled(true);
                    addColItem.setEnabled(true);
                    rmColItem.setEnabled(true);
                } else {
                    renameColItem.setEnabled(false);
                    addColItem.setEnabled(false);
                    rmColItem.setEnabled(false);
                }
                if (currentCol(e) == 0) {
                    addRowItem.setEnabled(true);
                    rmRowItem.setEnabled(true);
                } else {
                    addRowItem.setEnabled(false);
                    rmRowItem.setEnabled(false);
                }
            }
        } else {
            cutItem.setEnabled(true);
            copyItem.setEnabled(true);
            pasteItem.setEnabled(true);
            renameColItem.setEnabled(false);
            addColItem.setEnabled(false);
            rmColItem.setEnabled(false);
            addRowItem.setEnabled(false);
            rmRowItem.setEnabled(false);
        }
        tabMenue.show(e.getComponent(), e.getX(), e.getY());
    }

    public void actionPerformed(ActionEvent e) {
        try {
            String cmd = e.getActionCommand();
            if (cmd == "about") new AboutDialog(this);
            else if (cmd == "addCol") addColumn();
            else if (cmd == "addRow") addRow();
            else if (cmd == "cut") ((JTextComponent) cell.getComponent()).cut();
            else if (cmd == "copy") ((JTextComponent) cell.getComponent()).copy();
            else if (cmd == "delete") deleteContent(dataTable.getSelectedColumns(),dataTable.getSelectedRows());
            else if (cmd == "exit") exit();
            else if (cmd == "find") find( -1, -1);
            else if (cmd == "findnext") find(searchIndex[0], searchIndex[1]);
            else if (cmd == "gotoCase") gotoCase( -1);
            else if (cmd == "loadData") loadData();
            else if (cmd == "rhelp") JGR.MAINRCONSOLE.execute("help.start()");
            else if (cmd == "paste") ((JTextComponent) cell.getComponent()).paste();
            else if (cmd == "renameCol" && selectedColumn > 0) {
                renameColumn(selectedColumn);
            }
            else if (cmd == "rmCol") removeColumns();
            else if (cmd == "rmRow") removeRows();
            else if (cmd == "saveData") saveData();
            else if (cmd == "saveDataAs") saveDataAs();
            else if (cmd == "selAll") dataTable.selectAll();
            else if (cmd == "updateToR") updateToR();
            else if (cmd == "prefs") new PrefsDialog(this);

        } catch (Exception ex) {
            new iError(ex);
        }
    }

    public void columnAdded(TableColumnModelEvent e) {
    }

    public void columnMarginChanged(ChangeEvent e) {

    }

    public void columnMoved(TableColumnModelEvent e) {
    }

    public void columnRemoved(TableColumnModelEvent e) {
    }

    public void columnSelectionChanged(ListSelectionEvent e) {
    }

    public void keyTyped(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_DELETE &&
            (dataTable.getSelectedColumnCount() > 1 ||
             dataTable.getSelectedRowCount() > 1)) {
            deleteContent(dataTable.getSelectedColumns(),
                          dataTable.getSelectedRows());
        }
        else if (ke.getKeyCode() == KeyEvent.VK_F3) {
            find(searchIndex[0], searchIndex[1]);
        }
        else if (ke.getKeyCode() == KeyEvent.VK_ENTER && dataTable.getSelectedRow() == tabModel.rows-1 && dataTable.getSelectedColumn() == tabModel.cols-1 ) {
            if (dataTable.isEditing()) tabModel.setValueAt(((JTextComponent) cell.getComponent()).getText(),dataTable.getSelectedRow(),dataTable.getSelectedColumn());
            //we have to set the value manually because of some bugs in the jdk, perhaps it's no bug but bad implementation

            addRow();
            tabModel.fireTableStructureChanged();
            //if (Preferences.isMac) gotoCell(tabModel.rows-1,tabModel.cols-1);
        }
        else if (ke.getKeyCode() == KeyEvent.VK_TAB  && dataTable.getSelectedRow() == 0 && dataTable.getSelectedColumn() == tabModel.cols-1 && !ke.isShiftDown()) {
            if (dataTable.isEditing()) tabModel.setValueAt(((JTextComponent) cell.getComponent()).getText(),dataTable.getSelectedRow(),dataTable.getSelectedColumn());
            selectedColumn = -1;
            addColumn();
            tabModel.fireTableStructureChanged();
        }
    }

    public void keyReleased(KeyEvent ke) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        System.out.println(dataTable.getSelectedColumn());
        modified = dataTable.getSelectedColumn()>0?true:false;
        System.out.println(modified);
        try {
            selectedColumn = currentCol(e);
            if (e.isPopupTrigger()) {
                popUpMenu(e);
            } else if (e.getSource().equals(tableHeader)) {
                if (e.isPopupTrigger()) {
                    popUpMenu(e);
                }
                else {
                    if (e.getClickCount() == 2 && selectedColumn > 0)
                        renameColumn(selectedColumn);
                }
            } else if (selectedColumn == 0) {
                dataTable.setColumnSelectionInterval(0, tabModel.cols - 1);
            }
        } catch (Exception ex) {
            new iError(ex);
        }
    }

    public void mouseReleased(MouseEvent e) {
        try {
            if (e.getSource().equals(tableHeader)) {
                if (e.isPopupTrigger()) {
                    popUpMenu(e);
                }
                else {
                    selectedColumn = currentCol(e);
                    int from = dataTable.getColumn(dataTable.getColumnName(
                        selectedColumn)).getModelIndex();
                    int to = selectedColumn;
                    if (from != to) {
                        moveColumns(from, to);
                    }
                }
            }
            else if (e.isPopupTrigger()) {
                System.out.println("popup");
                popUpMenu(e);
            }
        } catch (Exception ex) {
            new iError(ex);
        }

    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    class DataTableColumnModel extends DefaultTableColumnModel {

        public DataTableColumnModel(DataTable tab) {
        }

        public void addColumn(TableColumn col) {
            cell.getComponent().setFont(iPreferences.DefaultFont);
            FontTracker.current.add((JTextComponent) cell.getComponent());
            col.setCellEditor(cell);
            col.setCellRenderer(new DefaultTableCellRenderer());
            if (col.getModelIndex() == 0) {
                col.setMaxWidth(50);
            }
            col.setMinWidth(50);
            super.addColumn(col);
        }

        public TableColumn getColumn(int index) {
            return super.getColumn(index < 0 ? 0 : index);
        }
    }

    class DataTableModel extends AbstractTableModel {

        DataTable tab;
        int cols, rows;

        DataTableModel(DataTable tab) {
            this.tab = tab;
            if (tab.vs == null || tab.vs.count() == 0) {
                cols = 1;
                rows = 1;
            } else {
                cols = vs.count() + 1;
                rows = vs.at(0).size();
            }
        }

        public int getColumnCount() {
            return cols;
        }

        public int getRowCount() {
            return rows;
        }

        public Object getValueAt(int row, int col) {
            Object value = null;
            if (col == 0) {
                value = new Integer(row + 1);
            } else if (vs != null) {
                try {
                    value = tab.vs.at(col - 1).at(row);
                } catch (Exception e) {}
            }
            return value == null? SVar.missingCat : value;
        }

        public void setValueAt(Object value, int row, int col) {
            try {
                if (value.toString().equals("") || value.toString().equals("NA")) value = null;
                if (vs.at(col - 1).isNum()) {
                    if (value != null) {
                        if (value.toString().indexOf(".") != -1) {
                            tab.vs.at(col - 1).replace(row,
                                Double.parseDouble(value.toString()));
                        }
                        else {
                            tab.vs.at(col - 1).replace(row,
                                Integer.parseInt(value.toString()));
                        }
                    }
                    else {
                        if (tab.vs.at(col-1).getClass().getName().equals("org.rosuda.ibase.SVarDouble")) tab.vs.at(col - 1).replace(row, SVar.double_NA);
                        else if (tab.vs.at(col-1).getClass().getName().equals("org.rosuda.ibase.SVarInt")) tab.vs.at(col - 1).replace(row, SVar.int_NA);
                        else tab.vs.at(col - 1).replace(row, Double.NaN);
                    }
                } else {
                    if (tab.vs.at(col-1).getClass().getName().equals("org.rosuda.ibase.SVarFact")) tab.vs.at(col - 1).replace(row, value==null?SVar.missingCat:value);
                    else tab.vs.at(col - 1).replace(row, value);
                }
            } catch (Exception e) {
                new iError(e);
            }
        }

        public String getColumnName(int col) {
            String colname = new String(" ");
            if (tab.vs != null && col != 0) {
                try {
                    colname = tab.vs.at(col - 1).getName();
                } catch (Exception e) {
                    return "C " + col;
                }
            }
            return colname == "" ? "C " + col : colname;
        }

        public Class getColumnClass(int c) {
            System.out.println(getValueAt(0, c).getClass());
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return col == 0 ? false : true;
        }
    }

    public class DataTableCellEditor extends AbstractCellEditor implements
        TableCellEditor {

        JTextField component = new JTextField(new DataTableCellDocument(),null,1);

        public Component getComponent() {
            return component;
        }

        public Component getTableCellEditorComponent(JTable t, Object v, boolean b, int r, int c) {
            dataTable.setColumnSelectionInterval(c,c);
            component.setText(v.toString().equals("NA")?"":v.toString());
            return component;
        }

        public Object getCellEditorValue() {
            return (Object) component.getText();
        }

    }

    public class DataTableCellDocument extends PlainDocument {

        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            try {
                if (((SVar) vs.at(dataTable.getSelectedColumn()-1)).isNum() && !str.matches("[+|-]*[[0-9]+.[0-9]+]*[0-9]*")) {
                    str = "";
                    Toolkit.getDefaultToolkit().beep();
                }
            } catch (Exception e) {new iError(e);}
            super.insertString(offset, str, a);
        }

    }

    public class DataTableCellRenderer extends JLabel implements TableCellRenderer {

            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {

                if (isSelected) {
                }

                if (hasFocus) {
                }

                setText(value.toString());

                //setToolTipText((String)value);

                return this;
            }

            public void validate() {}
            public void revalidate() {}
            protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
            public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
        }




    public class NewColumnDialog extends JDialog implements ActionListener {

        String[] varType = {"Numeric (double)", "Numeric (integer)", "Factor"};
        String[] result = null;
        JComboBox typeChooser = new JComboBox(varType);
        JTextField name = new JTextField();
        JButton cancel = new JButton("Cancel");
        JButton ok = new JButton("Ok");

        public NewColumnDialog(Frame f) {
            super(f,"Add Column",true);

            cancel.setActionCommand("cancel");
            ok.setActionCommand("ok");
            cancel.addActionListener(this);
            ok.addActionListener(this);

            this.getContentPane().setLayout(new GridBagLayout());
            this.getContentPane().add(name,
                                  new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.BOTH,
            new Insets(5, 5, 2, 5), 0, 0));
            this.getContentPane().add(typeChooser,
                                  new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.BOTH,
            new Insets(2, 5, 2, 5), 0, 0));
            this.getContentPane().add(cancel,
                                  new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(2, 50, 2, 5), 0, 0));
            this.getContentPane().add(ok,
                                  new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 2, 5), 0, 0));

            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.getRootPane().setDefaultButton(ok);
            this.setResizable(false);
            this.setSize(250,120);
            this.setLocation(f.getLocation().x+(f.getWidth()/2 -125),f.getLocation().y+(f.getHeight()/2-60));
        }

        public String[] showInputDialog() {
            this.show();
            return result;
        }

        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if (cmd=="cancel") dispose();
            else if (cmd=="ok") {
                result = new String[2];
                result[0] = name.getText();
                result[1] = typeChooser.getSelectedItem().toString();
                dispose();
            }
        }


    }



    /*Selectionmodell to link table with plots*/
    class MarkerSelectionModel implements ListSelectionModel, Dependent {
        SMarker m;
        Vector ls;

        MarkerSelectionModel(SMarker mark) {
            m = mark;
            ls = new Vector();
            m.addDepend(this);
        }

        protected void finalize() {
            if (m != null) {
                m.delDepend(this);
            }
            ls.removeAllElements();
            m = null;
        }

        public void Notifying(NotifyMsg msg, Object src, Vector path) {
            //System.out.println("MarkerSelectionModel.Notifying");
            int i = 0;
            ListSelectionEvent lse = new ListSelectionEvent(this, 0, m.size(), false);
            while (i < ls.size()) {
                ( (ListSelectionListener) ls.elementAt(i)).valueChanged(lse);
                i++;
            }
        }

        int anchor, lead;

        public void setSelectionInterval(int index0, int index1) {
            //System.out.println("MarkerSelectionModel.setSelectionInterval("+index0+","+index1+")");
            m.selectNone();
            addSelectionInterval(index0, index1);
        }

        public void addSelectionInterval(int index0, int index1) {
            //System.out.println("MarkerSelectionModel.addSelectionInterval("+index0+","+index1+")");
            anchor = index0;
            lead = index1;
            int i = (index0 < index1) ? index0 : index1;
            int j = (index0 < index1) ? index1 : index0;
            while (i <= j) {
                m.set(i++, true);
            }
            m.NotifyAll(new NotifyMsg(m, Common.NM_MarkerChange));
        }

        public void removeSelectionInterval(int index0, int index1) {
            //System.out.println("MarkerSelectionModel.removeSelectionInterval("+index0+","+index1+")");
            int i = (index0 < index1) ? index0 : index1;
            int j = (index0 < index1) ? index1 : index0;
            while (i <= j) {
                m.set(i++, false);
            }
            m.NotifyAll(new NotifyMsg(m, Common.NM_MarkerChange));
        }

        public int getMinSelectionIndex() {
            if (isSelectionEmpty()) {
                return -1;
            }
            int i = 0, j = m.size();
            while (i < j) {
                if (m.at(i)) {
                    return i;
                }
                i++;
            }
            return -1;
        }

        public int getMaxSelectionIndex() {
            if (isSelectionEmpty()) {
                return -1;
            }
            int i = m.size() - 1;
            while (i >= 0) {
                if (m.at(i)) {
                    return i;
                }
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
            //g.println("MarkerSelectionModel.setAnchorSelectionIndex("+index+")");
            anchor = index;
        }

        public int getLeadSelectionIndex() {
            //System.out.println("MarkerSelectionModel.getLeadSelectionIndex()="+lead);
            return lead;
        }

        public void setLeadSelectionIndex(int index) {
            //System.out.println("MarkerSelectionModel.setLeadSelectionIndex("+index+") [anchor="+anchor+",lead="+lead+"]");
            if (index == lead) {
                return;
            }
            if (index >= anchor) {
                if (lead < anchor) {
                    removeSelectionInterval(lead, anchor - 1);
                }
                if (index < lead) {
                    removeSelectionInterval(index + 1, lead);
                    lead = index;
                } else {
                    addSelectionInterval(anchor, index);
                }
            } else {
                if (lead > anchor) {
                    removeSelectionInterval(anchor + 1, lead);
                }
                if (index > lead) {
                    removeSelectionInterval(lead, index - 1);
                    lead = index;
                } else {
                    addSelectionInterval(anchor, index);
                }
            }
        }

        public void clearSelection() {
            //System.out.println("MarkerSelectionModel.clearSelection()");
            m.selectNone();
            m.NotifyAll(new NotifyMsg(m, Common.NM_MarkerChange));
        }

        public boolean isSelectionEmpty() {
            return (m.marked() == 0);
        }

        public void insertIndexInterval(int index, int length, boolean before) {
        //System.out.println("insertIndexInterval: I don't really know what to do here ("+index+","+length+","+before+")");
        }

        public void removeIndexInterval(int index0, int index1) {
        //System.out.println("removeIndexInterval("+index0+","+index1+") unsupported");
        }

        boolean isadj = false;

        public void setValueIsAdjusting(boolean valueIsAdjusting) {
            //System.out.println("MarkerSelectionModel.setValueIsAdjusting("+valueIsAdjusting+")");
            isadj = valueIsAdjusting;
        }

        public boolean getValueIsAdjusting() {
            //System.out.println("MarkerSelectionModel.getValueIsAdjusting()="+isadj);
            return isadj;
        }

        public void setSelectionMode(int selectionMode) {
        //System.out.println("setSelectionMode("+selectionMode+") [supported only "+ListSelectionModel.MULTIPLE_INTERVAL_SELECTION+"]");
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
}