package org.rosuda.JGR;

//
//  RConsole.java
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
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import org.rosuda.JRI.*;
import org.rosuda.JGR.toolkit.*;
import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;

public class RConsole extends iFrame implements ActionListener, KeyListener,
    FocusListener, RMainLoopCallbacks, MouseListener, Runnable {

    private IconButton newButton;
    private IconButton openButton;
    private IconButton saveButton;
    private IconButton undoButton;
    private IconButton redoButton;
    private IconButton cutButton;
    private IconButton copyButton;
    private IconButton pasteButton;
    private IconButton helpButton;

    private GridBagLayout layout = new GridBagLayout();
    private JSplitPane back = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private JScrollPane scrollAreaTop = new JScrollPane();
    private JScrollPane scrollAreaBottom = new JScrollPane();
    public ResultOutput output = new ResultOutput();
    public CmdInput input = new CmdInput();
    private Document inputDoc = input.getDocument();
    private Document outputDoc = output.getDocument();

    private final InsertRemoveUndoManager undoMgr = new InsertRemoveUndoManager(this);

    private String wspace = null;
    public static String directory = System.getProperty("user.home");

    private int currentHistPosition = 0;

    private StringBuffer console = new StringBuffer();

    public RConsole() {
        this(null);
    }

    public RConsole(File workSpace) {
        super("Console", iFrame.clsMain);
        JGR.MAINRCONSOLE = this;
        String[] Menu = {
            "+", "File", "@NNew Workspace", "newwspace", "@OLoad Workspace",
            "loadwspace",
            "@SSave Workspace", "savewspace", "!SSave Workspace as",
            "savewspaceas",
            "-", "Load Datafile", "loaddata", "~File.Quit",
            "~Edit",
            "+", "Tools", "Editor", "editor", "Object Manager", "objectmgr",
            "DataTable", "table", "-", "Increase Font", "fontBigger",
            "Decrease Font", "fontSmaller",
            "+", "Packages", "Package Manager", "packagemgr",/* "Install Package",
            "packageinst",*/
            "~Window",
            "~Help", "R Help", "rhelp", /*"JJGR FAQ", "jrfaq", */ "~About", "0"};
        iMenu.getMenu(this, this, Menu);

        if (JGR.RHISTORY == null) {
            JGR.RHISTORY = new Vector();
        }
        currentHistPosition = JGR.RHISTORY.size();

        output.setToolTipText("R Output");
        input.setToolTipText("R Input");

        input.addKeyListener(this);
        //input.setWordWrap(false);
        //output.setWordWrap(false);

        //output.setText("\n");

        output.addKeyListener(this);


        inputDoc.addUndoableEditListener(undoMgr);

        output.setEditable(false);

        input.addFocusListener(this);
        output.addFocusListener(this);

        scrollAreaTop.getViewport().add(output, null);
        scrollAreaBottom.getViewport().add(input, null);

        back.setTopComponent(scrollAreaTop);
        back.setBottomComponent(scrollAreaBottom);
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                back.setDividerLocation( ( (int) ( (double) getHeight() * 0.65)));
            }
        });
        this.addKeyListener(this);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                execute("q()\n");
            }
        });

        this.getContentPane().setLayout(layout);
        initIconBar();
        this.getContentPane().add(back,
                                  new GridBagConstraints(0, 1, 11, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(2, 5, 5, 5), 0, 0));

        this.setSize(new Dimension(600,
                                   Common.screenRes.height < 800 ?
                                   Common.screenRes.height - 50 : 700));
        back.setDividerLocation( ( (int) ( (double)this.getHeight() * 0.65)));
        this.show();
    }

    public void execute(final String cmd) {
        try {
            JGR.RHISTORY.add(cmd);
            currentHistPosition = JGR.RHISTORY.size();
            outputDoc.insertString(outputDoc.getLength()," "+cmd+"\n",Preferences.CMD);
            if (!isHelpCMD(cmd)) {
                Thread t = new Thread() {
                    public void run() {
                        JGR.READY = false;
                        JGR.RCSync.triggerNotification(cmd);
                    }
                };
                t.start();
            }
        }
        catch (Exception e) {}
        finally {  }
    }

    public boolean isHelpCMD(String cmd) {
        if (cmd.startsWith("help") || cmd.startsWith("?") ) {
            help(cmd);
            return true;
        }
        return false;
    }



    public void help(String help) {
        boolean exact = false;
        if (help != null) {
            help = help.replaceAll("[\"|(|)]", "");
            if (help.startsWith("help.search")) {
                help = help.replaceFirst("help.search", "");
            }
            else if (help.startsWith("help.start")) help=null;
            else {
                help = help.replaceFirst("help", "");
                help = help.replaceFirst("\\?", "");
                exact = true;
            }
        }
        final boolean e = exact;
        if (RHelp.last == null) {
            final String h;
            if (help!=null) h = help.trim();
            else h = null;
            Thread t = new Thread() {
                public void run() {
                    progress.start("Working");
                    setWorking(true);
                    try {
                        new RHelp();
                        if (h!=null) RHelp.last.search(h,e);
                    } catch (Exception e1) {
                    }
                    setWorking(false);
                }
            };
            t.start();
        }
        else {
            setWorking(true);
            RHelp.last.show();
            if (help!=null && help.trim().length() > 0) RHelp.last.search(help.trim(),e);
            setWorking(false);
        }
        output.append("> ",Preferences.CMD);
    }


    public void loadWorkSpace() {
        FileSelector fopen = new FileSelector(this, "Open Workspace",
                                              FileSelector.OPEN, directory);
        if (fopen.getFile() != null) {
            wspace = (directory = fopen.getDirectory()) + fopen.getFile();
            execute("load(\""+wspace+"\")");
        }
    }

    public void newWorkSpace() {
        int neww = JOptionPane.showConfirmDialog(null, "Save current workspace?",
                                                  "New Workspace",
                                                  JOptionPane.
                                                  YES_NO_CANCEL_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE);

         if (neww == 0) {
             if (saveWorkSpaceAs()) execute("rm(list=ls())");
         }
         else if (neww == 1) execute("rm(list=ls())");
    }

    public void saveWorkSpace(final String file) {
        if (file==null) execute("save.image()");
        else execute("save.image(\""+(file == null ? "" : file)+"\")");
        JGR.writeHistory();
    }

    public boolean saveWorkSpaceAs() {
        FileSelector fsave = new FileSelector(this, "Save Workspace as...",
                                              FileSelector.SAVE, directory);
        if (fsave.getFile() != null) {
            String file = (directory = fsave.getDirectory()) + fsave.getFile();
            saveWorkSpace(file);
            JGR.writeHistory();
            return true;
        }
        else return false;
    }

    public void initIconBar() {
        this.getContentPane().add(newButton = new IconButton("/icons/new.png",
            "New", this, "newwspace"),
                                  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 2, 5), 0, 0));
        this.getContentPane().add(openButton = new IconButton("/icons/open.png",
            "Open", this, "loadwspace"),
                                  new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 2, 5), 0, 0));
        this.getContentPane().add(saveButton = new IconButton("/icons/save.png",
            "Save", this, "savewspace"),
                                  new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 2, 5), 0, 0));
        this.getContentPane().add(undoButton = undoMgr.undoButton,
                                  new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 20, 2, 5), 0, 0));
        this.getContentPane().add(redoButton = undoMgr.redoButton,
                                  new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 2, 5), 0, 0));
        this.getContentPane().add(cutButton = new IconButton("/icons/cut.png",
            "Cut", this, "cut"),
                                  new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 2, 5), 0, 0));
        this.getContentPane().add(copyButton = new IconButton("/icons/copy.png",
            "Copy", this, "copy"),
                                  new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 2, 5), 0, 0));
        this.getContentPane().add(pasteButton = new IconButton(
            "/icons/paste.png", "Paste", this, "paste"),
                                  new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 5, 2, 5), 0, 0));
        this.getContentPane().add(helpButton = new IconButton("/icons/help.png",
            "R Help", this, "rhelp"),
                                  new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(2, 20, 2, 5), 0, 0));
        this.getContentPane().add(progress,
                                  new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.EAST, GridBagConstraints.NONE,
            new Insets(2, 50, 2, 5), 0, 0));

    }


    public void   rWriteConsole(Rengine re, String text) {
        console.append(text);
        if (console.length() > 100) {
            output.append(console.toString(),Preferences.RESULT);
            console.delete(0,console.length());
            output.setCaretPosition(outputDoc.getLength());
            try { Thread.sleep(5); } catch (Exception e) {}
        }
        //output.append(text,Preferences.RESULT);
        //try { wait(100); } catch (Exception e) {}
        //output.setCaretPosition(outputDoc.getLength());
    }

    public void   rBusy(Rengine re, int which) {
        if (which==0) {
            output.append(console.toString(),Preferences.RESULT);
            console.delete(0,console.length());
            output.setCaretPosition(outputDoc.getLength());
            setWorking(false);
        }
        else {
            progress.start("Working");
            setWorking(true);
            JGR.READY = false;
        }
    }

    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        JGR.READY = true;
        setWorking(false);
        if (prompt.indexOf("Save workspace") > -1) return JGR.exit();
        else {
            output.append(prompt,Preferences.CMD);
            output.setCaretPosition(outputDoc.getLength());
            String s = JGR.RCSync.waitForNotification();
            return (s==null||s.length()==0)?"\n":s+"\n";
        }
    }

    public void   rShowMessage(Rengine re, String message) {
        JOptionPane.showMessageDialog(this,message,"R Message",JOptionPane.INFORMATION_MESSAGE);
    }

    public void run() {
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "about") new AboutDialog(this);
        else if (cmd == "cut") input.cut();
        else if (cmd == "copy") {
            input.copy();
            output.copy();
        } else if (cmd == "delete") {
            try {
                int i = 0;
                inputDoc.remove( (i = input.getSelectionStart()),
                                input.getSelectionEnd() - i);
            } catch (BadLocationException ex) {}
        } else if (cmd == "editor") new REditor();
        else if (cmd == "exit") execute("q()\n");
        else if (cmd == "fontBigger") FontTracker.current.setFontBigger();
        else if (cmd == "fontSmaller") FontTracker.current.setFontSmaller();
        else if (cmd == "loaddata") new RDataFileDialog(this, directory);
        else if (cmd == "loadwspace") loadWorkSpace();
        else if (cmd == "newwspace") newWorkSpace();
        else if (cmd == "objectmgr") execute("object.manager()");
        else if (cmd == "packagemgr") execute("package.manager()");
        else if (cmd == "paste") input.paste();
        else if (cmd == "prefs") new PrefsDialog(this);
        else if (cmd == "redo") {
            try {
                if (undoMgr.canRedo()) {
                    undoMgr.redo();
                }
            } catch (CannotUndoException ex) {}
        } else if (cmd == "rhelp")  execute("help.start()");
        else if (cmd == "table") new DataTable(null);
        else if (cmd == "savewspace") saveWorkSpace(wspace);
        else if (cmd == "savewspaceas") saveWorkSpaceAs();
        else if (cmd == "selAll") {
            if (input.isFocusOwner()) {
                input.selectAll();
            } else if (output.isFocusOwner()) {
                output.selectAll();
            }
        } else if (cmd == "undo") {
            try {
                if (undoMgr.canUndo()) {
                    undoMgr.undo();
                }
            } catch (Exception ex) {}
        }
    }

    public void keyTyped(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
        if (ke.getSource().equals(output) && ke.getKeyCode() == KeyEvent.VK_V && (ke.isControlDown() || ke.isMetaDown())) input.paste();
    }

    public void keyReleased(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_TAB) {
            String text = null;
            int pos = input.getCaretPosition();
            if (pos == 0)
                return;
            try {
                text = input.getText(input.getLineStartOffset(input.
                    getLineOfOffset(pos)), pos);
            }
            catch (Exception e) {}
            if (text == null)
                return;
            int tl = text.length(), tp = 0, quotes = 0, dquotes = 0,
                lastQuote = -1;
            while (tp < tl) {
                char c = text.charAt(tp);
                if (c == '\\')
                    tp++;
                else {
                    if (dquotes == 0 && c == '\'') {
                        quotes ^= 1;
                        if (quotes == 0)
                            lastQuote = tp;
                    }
                    if (quotes == 0 && c == '"') {
                        dquotes ^= 1;
                        if (dquotes == 0)
                            lastQuote = tp;
                    }
                }
                tp++;
            }
            String last = input.getLastPart();
            if (last != null) {
                String result = null;
                if ( (quotes + dquotes) > 0)
                  result = RTalk.completeFile(last.substring(last.
                      lastIndexOf("\"", pos - 1) + 1));
                else
                    result = RTalk.completeCode(last);
                if (result != null && !result.equals(last))
                    input.insertAt(pos, result);
                else
                    Toolkit.getDefaultToolkit().beep();
            }
            else
                Toolkit.getDefaultToolkit().beep();
        }
        else if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            if (ke.isControlDown() || ke.isMetaDown()) {
                //we insert new line and set the cursor to last position
                try { inputDoc.insertString(input.getCaretPosition(), "\n", null); } catch (Exception e) {}
            }
            else {
                //remove \n when the user want to send a cmd
                try { inputDoc.remove(input.getCaretPosition()-1,"\n".length()); } catch (Exception e) { e.printStackTrace();}
                String cmd = input.getText().trim();//send the cmd
                input.setText("");
                input.setCaretPosition(0);
                input.requestFocus();
                execute(cmd);
            }
        }
        else if (ke.getKeyCode() == KeyEvent.VK_UP && currentHistPosition > 0) {
            int line = -1;
            try {
                line = input.getLineOfOffset(input.getCaretPosition());
            }
            catch (Exception e) {}
            if (line == 0) {
                if (currentHistPosition == JGR.RHISTORY.size() &&
                    input.getText().trim().length() > 0) {
                    JGR.RHISTORY.add(input.getText().trim());
                    //we set the cursor to last hist and save the current writing in the history
                }
                input.setText(JGR.RHISTORY.get(--currentHistPosition).toString());
            }
        }
        else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
            int line = -1;
            try {
                line = input.getLineOfOffset(input.getCaretPosition());
            }
            catch (Exception e) {}
            if (line == input.getLineCount() - 1) {
                if (currentHistPosition < JGR.RHISTORY.size() - 1) {
                    //we set the cursor to the next hist
                    input.setText(JGR.RHISTORY.get(++currentHistPosition).
                                  toString());
                }
                else if (JGR.RHISTORY.size() > 0 &&
                         currentHistPosition < JGR.RHISTORY.size()) {
                    //we empty the input field
                    input.setText("");
                    currentHistPosition++;
                }
            }
        }
    }

    public void focusGained(FocusEvent e) {
        if (e.getSource().equals(output)) {
            cutButton.setEnabled(false);
            //pasteButton.setEnabled(false);
            iMenu.getItem(this, "cut").setEnabled(false);
            //iMenu.getItem(this, "paste").setEnabled(false);
        } else if (e.getSource().equals(input)) {
            cutButton.setEnabled(true);
            //pasteButton.setEnabled(true);
            iMenu.getItem(this, "cut").setEnabled(true);
            //iMenu.getItem(this, "paste").setEnabled(true);
            //output.setCaretPosition(outputDoc.getLength());
        }
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

    class CmdInput extends SyntaxArea {

        public String getToolTipText(MouseEvent e) {
            String s = getCurrentWord();
            if (s!=null && JGR.STARTED) return RTalk.getArgs(s);
            else return "R Input";
        }
    }

    class ResultOutput extends JTextPane {
        public ResultOutput() {
            if (FontTracker.current == null) FontTracker.current = new FontTracker();
            FontTracker.current.add(this);
        }

        public void append(String str, AttributeSet a) {
            Document doc = getDocument();
                if (doc != null) {
                    try {
                        doc.insertString(doc.getLength(), str, a);
                    } catch (BadLocationException e) {
                    }
                }
        }
    }
}