package org.rosuda.JGR;

/**
*  JGRConsole Console Frame
 *
 *	@author Markus Helbig
 *
 * 	RoSuDA 2003 - 2004
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;

import org.rosuda.JRI.*;
import org.rosuda.JGR.toolkit.*;
import org.rosuda.JGR.util.*;

public class JGRConsole extends iFrame implements ActionListener, KeyListener,
FocusListener, RMainLoopCallbacks {

    private JSplitPane consolePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    public ConsoleOutput output = new ConsoleOutput();
    public SyntaxInput input = new SyntaxInput(true);
    private Document inputDoc = input.getDocument();
    private Document outputDoc = output.getDocument();

    private TextFinder textFinder = new TextFinder(output);

    private ToolBar toolBar;

    private String wspace = null;
    public static String directory = System.getProperty("user.home");

    private int currentHistPosition = 0;

    private StringBuffer console = new StringBuffer();

    private boolean wasHistEvent = false;

    public int end = 0;
    private Integer clearpoint = null;

    public JGRConsole() {
        this(null);
    }

    public JGRConsole(File workSpace) {
        super("Console", iFrame.clsMain);

        //Initialize JGRConsoleMenu
        String[] Menu = {
            "+", "File","Load Datafile", "loaddata"/*,"Export Output","exportOutput"*/,"-","@NNew Workspace", "new", "@OLoad Workspace",
            "open",
            "@SSave Workspace", "save", "!SSave Workspace as",
            "saveas","~File.Quit",
            "~EditCPS",
            "+", "Tools", "@EEditor", "editor", "@BObject Browser", "objectmgr",
            "DataTable", "table", "-", "Increase Font Size", "fontBigger",
            "Decrease Font Size", "fontSmaller",
            "+", "Packages", "Package Manager", "packagemgr",
            "~Window",
            "~Help", "R Help", "help", "~About", "0"};
        iMenu.getMenu(this, this, Menu);

        //Add History if we didn't found one in the user's home directory
        if (JGR.RHISTORY == null) {
            JGR.RHISTORY = new Vector();
        }
        currentHistPosition = JGR.RHISTORY.size();

        //Add default toolbar with stop button to interrupt R
        toolBar = new ToolBar(this,true,progress);

        input.addKeyListener(this);
        input.setWordWrap(false);
        input.addFocusListener(this);
        inputDoc.addUndoableEditListener(toolBar.undoMgr);

        output.setEditable(false);
        output.addFocusListener(this);
        output.addKeyListener(this);

        JScrollPane sp1 = new JScrollPane(output);
        sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        consolePanel.setTopComponent(sp1);
        JScrollPane sp2 = new JScrollPane(input);
        sp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        consolePanel.setBottomComponent(sp2);
        consolePanel.setDividerLocation( ( (int) ( (double)this.getHeight() * 0.65)));

        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                super.componentResized(evt);
                if (JGR.R != null && JGR.STARTED) JGR.R.eval("options(width="+getFontWidth()+")");
                consolePanel.setDividerLocation( ( (int) ( (double) getHeight() * 0.65)));
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                dispose();
            }
        });
        this.addKeyListener(this);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(toolBar,BorderLayout.NORTH);
        this.getContentPane().add(consolePanel,BorderLayout.CENTER);
        this.setMinimumSize(new Dimension(555,650));
        this.setSize(new Dimension(600,
                                   Common.screenRes.height < 800 ?
                                   Common.screenRes.height - 50 : 700));
        this.setVisible(true);
        progress.setVisible(false);
    }


    public void exit() {
        dispose();
    }

    public void dispose() {
        Enumeration e = WinTracker.current.elements();
        while (e.hasMoreElements()) {
            WTentry we = (WTentry) e.nextElement();
            if (we.wclass == iFrame.clsEditor) {
                if (!((Editor) we.w).exit()) return;
            }
        }
        execute("q()");
    }

    public void execute(String cmd) {
        if (!JGR.STARTED) return;
        if (JGR.RHISTORY.size()==0)  JGR.RHISTORY.add(cmd);
        else if (cmd.trim().length() > 0 && JGR.RHISTORY.size() > 0 && !JGR.RHISTORY.lastElement().equals(cmd.trim())) JGR.RHISTORY.add(cmd);
        currentHistPosition = JGR.RHISTORY.size();

        String[] cmdArray = cmd.split("\n");

        String c = null;
        for (int i = 0; i < cmdArray.length; i++) {
            c = cmdArray[i];
            if (!isHelpCMD(c))
                JGR.rSync.triggerNotification(c);
            else
                try { outputDoc.insertString(outputDoc.getLength()," "+c+"\n> ",JGRPrefs.CMD); } catch (Exception e) {}
        }
    }

    // later i hope it will be possible let R do this
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
        if (JGRHelp.current == null) {
            final String h;
            if (help!=null) h = help.trim();
            else h = null;
            Thread t = new Thread() {
                public void run() {
                    setWorking(true);
                    try {
                        new JGRHelp();
                        if (h!=null) JGRHelp.current.search(h,e);
                    } catch (Exception e1) {
                        new ErrorMsg(e1);
                    }
                    setWorking(false);
                }
            };
            t.start();
        }
        else {
            if (help!=null && help.trim().length() > 0) {
                final String h = help.trim();
                Thread t = new Thread() {
                    public void run() {
                        setWorking(true);
                        JGRHelp.current.show();
                        try {
                            JGRHelp.current.search(h,e);
                        } catch (Exception e1) {
                            new ErrorMsg(e1);
                        }
                        setWorking(false);
                    }
                };
                t.start();
            }
        }
    }


    public void clearconsole() {
        try {
            if (clearpoint==null) clearpoint = new Integer(output.getLineEndOffset(output.getLineOfOffset(end)-1)+2);
            output.removeAllFrom(clearpoint.intValue());
        } catch (Exception e) { new ErrorMsg(e);/*e.printStackTrace();*/ }
    }


    public void loadWorkSpace() {
        FileSelector fopen = new FileSelector(this, "Open Workspace",
                                              FileSelector.OPEN, directory);
        if (fopen.getFile() != null) {
            wspace = (directory = fopen.getDirectory()) + fopen.getFile();
            execute("load(\""+wspace.replace('\\','/')+"\")");
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
        else execute("save.image(\""+(file == null ? "" : file.replace('\\','/'))+"\")");
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

    public void   rWriteConsole(Rengine re, String text) {
        console.append(text);
        if (console.length() > 100) {
            output.append(console.toString(),JGRPrefs.RESULT);
            console.delete(0,console.length());
            output.setCaretPosition(outputDoc.getLength());
        }
    }

    public void   rBusy(Rengine re, int which) {
        if (which==0) {
            if (console != null) {
                output.append(console.toString(), JGRPrefs.RESULT);
                console.delete(0, console.length());
            }
            output.setCaretPosition(outputDoc.getLength());
            setWorking(false);
        }
        else {
            toolBar.stopButton.setEnabled(true);
            setWorking(true);
        }
    }

    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        toolBar.stopButton.setEnabled(false);
        if (prompt.indexOf("Save workspace") > -1) return JGR.exit();
        else {
            output.append(prompt,JGRPrefs.CMD);
            output.setCaretPosition(outputDoc.getLength());
            String s = JGR.rSync.waitForNotification();
            try { outputDoc.insertString(outputDoc.getLength()," "+s+"\n",JGRPrefs.CMD); } catch (Exception e) {}
            return (s==null||s.length()==0)?"\n":s+"\n";
        }
    }

    public void   rShowMessage(Rengine re, String message) {
        JOptionPane.showMessageDialog(this,message,"R Message",JOptionPane.INFORMATION_MESSAGE);
    }

    public int getFontWidth() {
        int width = output.getFontMetrics(output.getFont()).getMaxAdvance();
        width = output.getWidth() / width;
        if (JGRPrefs.isMac) {
            return (int) (width*1.5);
        }
        else {
            return (int) (width*2.5);
        }
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd == "about") new AboutDialog(this);
        else if (cmd == "cut") input.cut();
        else if (cmd == "copy") {
            input.copy();
            output.copy();
        } else if (cmd=="copyoutput") output.copyOutput();
        else if (cmd=="copycmds") output.copyCommands();
        else if (cmd=="copyresult") output.copyResults();
        else if (cmd == "clearconsole") clearconsole();
        else if (cmd == "delete") {
            try {
                int i = 0;
                inputDoc.remove((i = input.getSelectionStart()),input.getSelectionEnd()-i);
            } catch (BadLocationException ex) {}
        } else if (cmd == "editor") new Editor();
        else if (cmd == "exit") dispose();
        else if (cmd == "exportOutput") output.startExport();
        else if (cmd == "fontBigger") FontTracker.current.setFontBigger();
        else if (cmd == "fontSmaller") FontTracker.current.setFontSmaller();
        else if (cmd == "loaddata") new JGRDataFileDialog(this, directory);
        else if (cmd == "open") loadWorkSpace();
        else if (cmd == "new") newWorkSpace();
        else if (cmd == "objectmgr") execute("object.manager()");
        else if (cmd == "packagemgr") execute("package.manager()");
        else if (cmd == "paste") input.paste();
        else if (cmd == "prefs") new PrefsDialog(this);
        else if (cmd == "redo") {
            try {
                if (toolBar.undoMgr.canRedo())
                    toolBar.undoMgr.redo();
            } catch (CannotUndoException ex) {}
        } else if (cmd == "help")  execute("help.start()");
        else if (cmd == "table") new DataTable(null,null);
        else if (cmd == "save") saveWorkSpace(wspace);
        else if (cmd == "saveas") saveWorkSpaceAs();
        else if (cmd == "search") textFinder.showFind(false);
        else if (cmd == "stop") JGR.R.rniStop(1);
        else if (cmd == "selAll") {
            if (input.isFocusOwner()) {
                input.selectAll();
            } else if (output.isFocusOwner()) {
                output.selectAll();
            }
        } else if (cmd == "undo") {
            try {
                if (toolBar.undoMgr.canUndo())
                    toolBar.undoMgr.undo();
            } catch (Exception ex) {}
        }
    }

    public void keyTyped(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
        if (JGRPrefs.useEmacsKeyBindings) {
            if (ke.getKeyCode() == KeyEvent.VK_E && ke.isControlDown()) {
                input.setCaretPosition(input.getText().length());
            }
            if (ke.getKeyCode() == KeyEvent.VK_A && ke.isControlDown()) {
                input.setCaretPosition(0);
            }
        }
        if (ke.getKeyCode() == KeyEvent.VK_UP) {
            if (input.mComplete != null && input.mComplete.isVisible()) {
                input.mComplete.selectPrevios();
            }
            else if (currentHistPosition > 0){
                if (input.getCaretPosition()==0 || input.getCaretPosition()==input.getText().length()) {
                    if (input.getText().trim().length() > 0) {
                        if (currentHistPosition==JGR.RHISTORY.size() && !input.getText().trim().equals(JGR.RHISTORY.elementAt(currentHistPosition-1))) {
                            JGR.RHISTORY.add(input.getText().trim());
                        }
                    }
                    input.setText(JGR.RHISTORY.get(--currentHistPosition).toString());
                    input.setCaretPosition(input.getText().length());
                    wasHistEvent = true;
                }
            }
        }
        else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
            if (input.mComplete != null && input.mComplete.isVisible()) {
                input.mComplete.selectNext();
            }
            else {
                if (input.getCaretPosition()==0 || input.getCaretPosition()==input.getText().length()) {
                    if (currentHistPosition < JGR.RHISTORY.size() - 1) {
                        input.setText(JGR.RHISTORY.get(++currentHistPosition).toString());
                        input.setCaretPosition(input.getText().length());
                    }
                    else if (JGR.RHISTORY.size() > 0 && currentHistPosition < JGR.RHISTORY.size()) {
                        input.setText("");
                        currentHistPosition++;
                    }
                    wasHistEvent = true;
                }
            }
        }
    }

    public void keyReleased(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            if (input.mComplete != null && input.mComplete.isVisible() && !(ke.isControlDown() || ke.isMetaDown())) {
                input.mComplete.completeCommand();
            }
            else {
                if (ke.isControlDown() || ke.isMetaDown()) {
                    try {
                        inputDoc.insertString(input.getCaretPosition(), "\n", null);
                        input.mComplete.setVisible(false);
                    } catch (Exception e) {}
                }
                else {
                    String cmd = input.getText().trim();
                    input.setText("");
                    input.setCaretPosition(0);
                    input.requestFocus();
                    execute(cmd);
                }
            }
        }
        if (ke.getSource().equals(output) && ke.getKeyCode() == KeyEvent.VK_V && (ke.isControlDown() || ke.isMetaDown())) {
            input.requestFocus();
            //input.setCaretPosition(input.getText().length());
            input.paste();
            input.setCaretPosition(input.getText().length());
        }
        else if ((ke.getKeyCode() == KeyEvent.VK_UP || ke.getKeyCode() == KeyEvent.VK_DOWN) && wasHistEvent) {
            wasHistEvent = false;
            input.setCaretPosition(input.getText().length());
        }
    }

    public void focusGained(FocusEvent e) {
        if (e.getSource().equals(output)) {
            toolBar.cutButton.setEnabled(false);
            iMenu.getItem(this, "cut").setEnabled(false);
        } else if (e.getSource().equals(input)) {
            toolBar.cutButton.setEnabled(true);
            iMenu.getItem(this, "cut").setEnabled(true);
        }
    }

    public void focusLost(FocusEvent e) {
    }
}
