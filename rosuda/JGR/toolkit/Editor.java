package org.rosuda.JGR.toolkit;

//
//  Editor.java
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import org.rosuda.ibase.*;
import org.rosuda.util.*;

import org.rosuda.JGR.*;
import org.rosuda.JGR.util.*;

public class Editor extends iFrame implements ActionListener, KeyListener {

    private IconButton newButton;
    private IconButton openButton;
    private IconButton saveButton;
    private IconButton undoButton;
    private IconButton redoButton;
    private IconButton cutButton;
    private IconButton copyButton;
    private IconButton pasteButton;
    private IconButton findButton;
    private IconButton helpButton;

    private GridBagLayout layout = new GridBagLayout();
    private CaretListenerLabel caretStatus = new CaretListenerLabel();
    private JLabel modifiedStatus = new JLabel();
    private SyntaxInput editArea = new SyntaxInput(false);
    private Document editDoc = editArea.getDocument();

    private ToolBar toolBar;
    
    private String fileName = null;
    private static String directory = System.getProperty("user.home");
    private String keyWord = null;

    private boolean modified = false;


    public static RecentList recentOpen;
    public JMenu recentMenu;

    public Editor() {
        this(null);
    }

    public Editor(String file) {
        super("Editor", iFrame.clsEditor);
        String[] Menu = {
            "+", "File", "@NNew", "new", "@OOpen", "open","#Open Recent","",
            "@SSave", "save", "!SSave as", "saveas",
            "-", "@PPrint", "print","~File.Basic.End",
            "~Edit",
            "+", "Tools", "Increase Fontsize", "fontBigger", "Decrease Fontsize",
            "fontSmaller", "-", "@FFind", "find", "@GFind Next", "findnext",
            "~Window",
            "~Help", "R Help", "rhelp", "~About", "0"};
        iMenu.getMenu(this, this, Menu);
        JMenu rm=recentMenu=(JMenu) iMenu.getItemByLabel(this,"Open Recent");
        if (rm!=null) {
            if (recentOpen==null)
                recentOpen=new RecentList(Common.appName,"RecentOpenFiles",8);
            String[] shortNames=recentOpen.getShortEntries();
            String[] longNames =recentOpen.getAllEntries();
            int i=0;
            while (i<shortNames.length) {
                JMenuItem mi=new JMenuItem(shortNames[i]);
                mi.setActionCommand("recent:"+longNames[i]);
                mi.addActionListener(this);
                rm.add(mi);
                i++;
            }
            if (i>0) rm.addSeparator();
            JMenuItem ca=new JMenuItem("Clear list");
            ca.setActionCommand("recent-clear");
            ca.addActionListener(this);
            rm.add(ca);
            if (i==0) ca.setEnabled(false);
        }
        
        toolBar = new ToolBar(this,false);

        editArea.addCaretListener(caretStatus);
        editArea.addKeyListener(this);
        editArea.setWordWrap(false);
        
        editDoc.addUndoableEditListener(toolBar.undoMgr);

        caretStatus.setMinimumSize(new Dimension(100, 15));
        caretStatus.setPreferredSize(new Dimension(100, 15));
        caretStatus.setMaximumSize(new Dimension(100, 15));

        modifiedStatus.setMinimumSize(new Dimension(80, 15));
        modifiedStatus.setPreferredSize(new Dimension(80, 15));
        modifiedStatus.setMaximumSize(new Dimension(80, 15));
        
        JPanel status = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        status.add(modifiedStatus);
        status.add(caretStatus);
        
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(toolBar,BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(editArea);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.getContentPane().add(sp,BorderLayout.CENTER);
        this.getContentPane().add(status,BorderLayout.SOUTH);


        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exit();
            }
        });
        this.setMinimumSize(new Dimension(600,600));
        this.setSize(new Dimension(600,
                                   Common.screenRes.height < 800 ?
                                   Common.screenRes.height - 50 : 700));
        this.setLocation(this.getLocation().x+100, 10);
        this.setVisible(true);
        if (file != null) this.fileName = file;
        if (this.fileName != null) loadFile();
        this.setTitle("Editor"+(fileName == null ? "" : (" - "+fileName)));
        editArea.requestFocus();
    }

   
    public boolean exit() {
        if (modified) {
            int i = JOptionPane.showConfirmDialog(this,"Save File?","Exit",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
            if (i==1) {
                dispose();
                return true;
            }
            else if (i==0 && saveFile()) {
                dispose();
                return true;
            }
            else return false;
        }
        else {
            dispose();
            return true;
        }
    }

    public void open() {
        FileSelector fopen = new FileSelector(this, "Open...",
                                              FileSelector.OPEN, directory);
        if (fopen.getFile() != null) {
            if (!modified) editArea.setText("");
            fileName = (directory = fopen.getDirectory()) + fopen.getFile();
        }
        if (!modified) loadFile();
        else new Editor(fileName);
    }

    public void loadFile() {
        progress.start("Loading");
        setWorking(true);
        editArea.setText("");
        try {
            final BufferedReader reader = new BufferedReader(new FileReader(fileName));
            Thread t = new Thread() {
                public void run() {
                    try {
                        StringBuffer text = new StringBuffer();
                        while (reader.ready()) {
                            text.append(reader.readLine()+"\n");
                            if (text.length() > 32000) {
                                editArea.append(text.toString());
                                text.delete(0,text.length());
                                try { Thread.sleep(2);} catch (Exception e) {}
                            }
                        }
                        reader.close();
                        editArea.append(text.toString());
                        text.delete(0,text.length());
                    } catch (Exception e) {}
                    editArea.setToolTipText(fileName);
                    setWorking(false);
                }
            };
            t.start();
        }
        catch (Exception e) {
        }
        recentOpen.addEntry(fileName);
        JMenu rm=recentMenu=(JMenu) iMenu.getItemByLabel(this,"Open Recent");
        if (rm!=null) {
            rm.removeAll();
            if (recentOpen==null)
                recentOpen=new RecentList(Common.appName,"RecentOpenFiles",8);
            String[] shortNames=recentOpen.getShortEntries();
            String[] longNames =recentOpen.getAllEntries();
            int i=0;
            while (i<shortNames.length) {
                JMenuItem mi=new JMenuItem(shortNames[i]);
                mi.setActionCommand("recent:"+longNames[i]);
                mi.addActionListener(this);
                rm.add(mi);
                i++;
            }
            if (i>0) rm.addSeparator();
            JMenuItem ca=new JMenuItem("Clear list");
            ca.setActionCommand("recent-clear");
            ca.addActionListener(this);
            rm.add(ca);
            if (i==0) ca.setEnabled(false);
        }
        this.setTitle("Editor"+(fileName == null ? "" : (" - "+fileName)));
        editArea.requestFocus();
    }


    public void print() {
        DocumentRenderer docrender = new DocumentRenderer();
        docrender.print(editArea);
    }

    public boolean saveFile() {
        if (fileName == null || fileName.equals("")) {
            return saveFileAs();
        } else {
            progress.start("Saveing");
            setWorking(true);
            new FileSave(this);
            this.setTitle("Editor"+(fileName == null ? "" : (" - "+fileName)));
            setModified(modified = false);
            return true;
        }
    }

    public boolean saveFileAs() {
        FileSelector fsave = new FileSelector(this, "Save as...",
                                              FileSelector.SAVE, directory);
        if (fsave.getFile() != null) {
            fileName = (directory = fsave.getDirectory()) + fsave.getFile();
            return saveFile();
        }
        return false;
    }

    public void setModified(boolean mod) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                modifiedStatus.setText(modified ? "Modified" : "");
            }
        });
    }

    public void startNew() {
        new Editor();
    }


    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "about") new AboutDialog(this);
        else if (cmd == "cut") editArea.cut();
        else if (cmd == "copy") editArea.copy();
        else if (cmd == "delete") {
            try {
                int i = 0;
                editDoc.remove( (i = editArea.getSelectionStart()),editArea.getSelectionEnd() - i);
            } catch (BadLocationException ex) {}
        } else if (cmd == "exit") exit();
        else if (cmd == "find") TextFinder.showFind(editArea);
        else if (cmd == "findnext") TextFinder.showFind(editArea, 1);
        else if (cmd == "fontBigger") FontTracker.current.setFontBigger();
        else if (cmd == "fontSmaller") FontTracker.current.setFontSmaller();
        else if (cmd == "fontBigger") FontTracker.current.setFontBigger();
        else if (cmd == "fontSmaller") FontTracker.current.setFontSmaller();
        else if (cmd == "new") startNew();
        else if (cmd == "objectmgr") JGR.MAINRCONSOLE.execute("object.manager()");
        else if (cmd == "open") open();
        else if (cmd.startsWith("recent:")) {
            fileName = cmd.replaceFirst("recent:","");
            loadFile();
        }
        else if (cmd == "paste") editArea.paste();
        else if (cmd == "prefs") new PrefsDialog(this);
        else if (cmd == "print") print();
        else if (cmd == "recent-clear") {
            if (recentOpen!=null && recentMenu!=null) {
                recentMenu.removeAll();
                recentMenu.addSeparator();
                JMenuItem ca=new JMenuItem("Clear list");
                ca.setActionCommand("recent-clear");
                ca.addActionListener(this);
                ca.setEnabled(false);
                recentMenu.add(ca);
                recentOpen.reset();
            }
        }
        else if (cmd == "redo") {
            try {
                if (toolBar.undoMgr.canRedo()) 
                	toolBar.undoMgr.redo();
            } catch (CannotUndoException ex) {}
        } else if (cmd == "help") JGR.MAINRCONSOLE.execute("help.start()");
        else if (cmd == "save") saveFile();
        else if (cmd == "saveas") saveFileAs();
        else if (cmd == "selAll") editArea.selectAll();
        else if (cmd == "undo") {
            try {
                if (toolBar.undoMgr.canUndo()) 
                	toolBar.undoMgr.undo();
            } catch (CannotUndoException ex) {}
        }

    }

    public void keyTyped(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
        setModified(modified = true);
        if (ke.getKeyCode() == KeyEvent.VK_ENTER &&
            (ke.isControlDown() || ke.isMetaDown())) {
            if (JGR.MAINRCONSOLE != null) {
                JGR.MAINRCONSOLE.execute(editArea.getSelectedText());
            }
        }
    }

    public void keyReleased(KeyEvent ke) {
    }

    protected class CaretListenerLabel extends JLabel implements CaretListener {
        public CaretListenerLabel() {
        }

        public void caretUpdate(CaretEvent e) {
            displayInfo(e);
        }

        protected void displayInfo(final CaretEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        int currentpos = editArea.getCaretPosition();
                        int lastnewline = editArea.getText().lastIndexOf("\n",
                            currentpos - 1);
                        int chars = editArea.getText(0,
                            lastnewline < 0 ? 0 : lastnewline).length();
                        int currentline = editArea.getLineOfOffset(editArea.
                            getCaretPosition());
                        currentpos -= chars;
                        setText(currentline + ":" +
                                (currentline == 0 ? currentpos + 1 : currentpos));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    class FileSave extends Thread {

        private String fileName;
        private BufferedWriter writer;
        private Editor editor;

        public FileSave(Editor editor) {
            this.fileName = editor.fileName;
            this.editor = editor;
            try {
                writer = new BufferedWriter(new FileWriter(fileName));
            } catch (Exception e) {
                new ErrorMsg(e);
            }
            this.start();
        }

        public void run() {
            try {
                writer.write(editArea.getText());
                writer.flush();
                writer.close();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(editor,"Permisson denied","File Errror",JOptionPane.OK_OPTION);
            } finally {
                editor.setWorking(false);
            }
        }
    }
}