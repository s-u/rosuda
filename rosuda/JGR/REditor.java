package org.rosuda.JGR;

//
//  REditor.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import org.rosuda.JGR.toolkit.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;

public class REditor extends iFrame implements ActionListener, FocusListener,
    KeyListener, MouseListener, Runnable {

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
    private JScrollPane scrollArea = new JScrollPane();
    private CaretListenerLabel caretStatus = new CaretListenerLabel();
    private JLabel modifiedStatus = new JLabel();
    private EditInput editArea = new EditInput();
    private Document editDoc = editArea.getDocument();

    private final InsertRemoveUndoManager undoMgr = new InsertRemoveUndoManager(this);

    private String fileName = null;
    private static String directory = System.getProperty("user.home");
    private String keyWord = null;

    private boolean modified = false;


    public static RecentList recentOpen;
    public JMenu recentMenu;

    public REditor() {
        this(null);
    }

    public REditor(String fileName) {
        super("Editor", iFrame.clsEditor);
        String[] Menu = {
            "+", "File", "@NNew", "new", "@OOpen", "open","#Open Recent","",
            "@SSave", "save", "!SSave as", "saveas",
            "-", "@PPrint", "print","~File.Basic.End",
            "~Edit",
            "+", "Tools", "Increase", "fontBigger", "Decrease Font",
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


        editDoc.addUndoableEditListener(undoMgr);

        scrollArea.getViewport().add(editArea);

        caretStatus.setMinimumSize(new Dimension(100, 15));
        caretStatus.setPreferredSize(new Dimension(100, 15));
        caretStatus.setMaximumSize(new Dimension(100, 15));

        modifiedStatus.setMinimumSize(new Dimension(80, 15));
        modifiedStatus.setPreferredSize(new Dimension(80, 15));
        modifiedStatus.setMaximumSize(new Dimension(80, 15));


        this.getContentPane().setLayout(layout);
        this.getContentPane().add(initIconBar(),
                                  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(5, 5, 2, 5), 0, 0));
        this.getContentPane().add(scrollArea,
                                  new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 5, 2, 5), 0, 0));


        JPanel status = new JPanel(new GridBagLayout());
        status.setMinimumSize(new Dimension(600,20));
        status.setSize(new Dimension(600,20));
        status.add(modifiedStatus,
                                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));
        status.add(caretStatus,
                                  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));


        this.getContentPane().add(status,
                                  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
            new Insets(0, 0, 0, 5), 0, 0));


        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exit();
            }
        });
        editArea.addCaretListener(caretStatus);
        editArea.addFocusListener(this);
        editArea.addKeyListener(this);
        editArea.setToolTipText(fileName == null ? "Editor" : fileName);
        editArea.setWordWrap(false);
        this.setTitle("Editor"+(fileName == null ? "" : (" - "+fileName)));
        this.setMinimumSize(new Dimension(600,600));
        this.setSize(new Dimension(600,
                                   Common.screenRes.height < 800 ?
                                   Common.screenRes.height - 50 : 700));
        this.setLocation(this.getLocation().x+100, 10);
        this.show();
        if (fileName != null) {
            new FileLoad(this);
        }
        editArea.requestFocus();
    }

    public JPanel initIconBar() {
        JPanel buttons = new JPanel(new GridBagLayout());
        buttons.add(newButton = new IconButton("/icons/new.png",
            "New", this, "new"),
                                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 5), 0, 0));
        buttons.add(openButton = new IconButton("/icons/open.png",
            "Open", this, "open"),
                                  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 5, 0, 5), 0, 0));
        buttons.add(saveButton = new IconButton("/icons/save.png",
            "Save", this, "save"),
                                  new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 5, 0, 5), 0, 0));
        buttons.add(undoButton = undoMgr.undoButton,
                                  new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 20, 0, 5), 0, 0));
        buttons.add(redoButton = undoMgr.redoButton,
                                  new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 5, 0, 5), 0, 0));
        buttons.add(cutButton = new IconButton("/icons/cut.png",
            "Cut", this, "cut"),
                                  new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 5, 0, 5), 0, 0));
        buttons.add(copyButton = new IconButton("/icons/copy.png",
            "Copy", this, "copy"),
                                  new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 5, 0, 5), 0, 0));
        buttons.add(pasteButton = new IconButton(
            "/icons/paste.png", "Paste", this, "paste"),
                                  new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 5, 0, 5), 0, 0));
        buttons.add(findButton = new IconButton("/icons/find.png",
            "Find", this, "find"),
                                  new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 20, 0, 5), 0, 0));
        buttons.add(helpButton = new IconButton("/icons/help.png",
            "R Help", this, "rhelp"),
                                  new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(0, 20, 0, 5), 0, 0));
        buttons.add(progress,
                                  new GridBagConstraints(10, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(0, 50, 0, 5), 0, 0));
        return buttons;
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
        if (modified) {
            int i = JOptionPane.showConfirmDialog(this, "Save File?", "Exit",
                                                  JOptionPane.
                                                  YES_NO_CANCEL_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE);
            if (i == 0 && !saveFile()) return;
        }
        FileSelector fopen = new FileSelector(this, "Open...",
                                              FileSelector.OPEN, directory);
        if (fopen.getFile() != null) {
            editArea.setText("");
            fileName = (directory = fopen.getDirectory()) + fopen.getFile();
        }
        loadFile();
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
        new REditor();
    }

    public void run() {
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        //System.out.println("cmd "+cmd);
        if (cmd == "about") new AboutDialog(this);
        else if (cmd == "cut") editArea.cut();
        else if (cmd == "copy") editArea.copy();
        else if (cmd == "delete") {
            try {
                int i = 0;
                editDoc.remove( (i = editArea.getSelectionStart()),
                               editArea.getSelectionEnd() - i);
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
                if (undoMgr.canRedo()) {
                    undoMgr.redo();
                }
            } catch (CannotUndoException ex) {}
        } else if (cmd == "rhelp") JGR.MAINRCONSOLE.execute("help.start()");
        else if (cmd == "save") saveFile();
        else if (cmd == "saveas") saveFileAs();
        else if (cmd == "selAll") editArea.selectAll();
        else if (cmd == "undo") {
            try {
                if (undoMgr.canUndo()) {
                    undoMgr.undo();
                }
            } catch (CannotUndoException ex) {}
        }

    }

    public void keyTyped(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
        setModified(modified = true);
        if (ke.getKeyCode() == KeyEvent.VK_TAB) {
            String text = null;
            int pos = editArea.getCaretPosition();
            if (pos==0) return;
            try {
            text = editArea.getText(editArea.getLineStartOffset(editArea.getLineOfOffset(pos)),pos);
            } catch (Exception e) {}
            if (text == null) return;
            int tl = text.length(), tp=0, quotes=0, dquotes=0, lastQuote=-1;
            while (tp<tl) {
                    char c=text.charAt(tp);
                    if (c=='\\') tp++;
                    else {
                        if (dquotes==0 && c=='\'') {
                            quotes^=1;
                            if (quotes==0) lastQuote=tp;
                        }
                        if (quotes==0 && c=='"') {
                            dquotes^=1;
                            if (dquotes==0) lastQuote=tp;
                        }
                    }
                    tp++;
            }
            String last = editArea.getLastPart();
            if (last != null) {
                String result = null;
                if ((quotes+dquotes)>0) result = RTalk.completeFile(last.substring(last.lastIndexOf("\"",pos-1)+1));
                else result = RTalk.completeCode(last);
                if (result != null && !result.equals(last)) editArea.insertAt(pos,result);
                else Toolkit.getDefaultToolkit().beep();
            }
            else Toolkit.getDefaultToolkit().beep();
        }
        else if (ke.getKeyCode() == KeyEvent.VK_ENTER &&
            (ke.isControlDown() || ke.isMetaDown())) {
            if (JGR.MAINRCONSOLE != null) {
                JGR.MAINRCONSOLE.execute(editArea.getSelectedText());
            }
        }
    }

    public void keyReleased(KeyEvent ke) {
    }

    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    protected class CaretListenerLabel extends JLabel implements CaretListener {
        public CaretListenerLabel() {
        }

        public void caretUpdate(CaretEvent e) {
            displayInfo(e);
        }

        protected void displayInfo(final CaretEvent e) {
            //System.out.println("Area "+editArea);
            //System.out.println("Text "+editArea.getText());

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

    class FileLoad extends Thread {

        private String fileName;
        private BufferedReader reader;
        private REditor editor;

        public FileLoad(REditor editor) {
            this.fileName = editor.fileName;
            this.editor = editor;
            try {
                if (new File(fileName).exists()) {
                    reader = new BufferedReader(new FileReader(fileName));
                }
            } catch (Exception e) {
                new iError(e);
            }
            editArea.setToolTipText(fileName);
            this.start();
        }

        public void run() {
            try {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            editArea.removeCaretListener(editArea);
                            BufferedReader reader = new BufferedReader(new
                            FileReader(fileName));
                            StringBuffer text = new StringBuffer();
                            while (reader.ready()) {
                                text.append(reader.readLine() + "\n");
                                if (text.length() > 80) {
                                    editArea.append(text.toString());
                                    text.delete(0,text.length());
                                }
                            }
                            editArea.append(text.toString());
                            text.delete(0,text.length());
                        } catch (Exception e) {
                            new iError(e);
                        }
                        finally {
                            editArea.addCaretListener(editArea);
                            editor.setWorking(false);
                            editArea.select(0,0);
                        }
                    }
                };
                t.start();
            } catch (Exception e) {
                new iError(e);
            }
        }
    }

    class FileSave extends Thread {

        private String fileName;
        private BufferedWriter writer;
        private REditor editor;

        public FileSave(REditor editor) {
            this.fileName = editor.fileName;
            this.editor = editor;
            try {
                writer = new BufferedWriter(new FileWriter(fileName));
            } catch (Exception e) {
                new iError(e);
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

    class EditInput extends SyntaxArea {

        public String getToolTipText(MouseEvent e) {
            String s = getCurrentWord();
            if (s!=null && JGR.STARTED) return RTalk.getArgs(s);
            else return fileName;
        }
    }
}
