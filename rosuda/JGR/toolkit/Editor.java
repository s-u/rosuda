package org.rosuda.JGR.toolkit;

/**
*  Editor
 *
 * 	editor with syntaxhighlighting and autocompletion
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
    private SyntaxInput editArea = new SyntaxInput(true);
    private Document editDoc = editArea.getDocument();
	
    private ToolBar toolBar;
	
    private String fileName = null;
    private static String directory = System.getProperty("user.home");
    private String keyWord = null;
	
    private boolean modified = false;
	
    private TextFinder textFinder = new TextFinder(editArea);
	
	
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
            "+", "Tools", "Increase Font Size", "fontBigger", "Decrease Font Size",
            "fontSmaller", "-", "@FFind", "search", "@GFind Next", "searchnext",
            "~Window",
            "~Help", "R Help", "help", "~About", "0"};
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
		
        toolBar = new ToolBar(this,false, progress);
		
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
		this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                super.componentResized(evt);
				setTitle(fileName);
            }
        });
        this.setMinimumSize(new Dimension(600,600));
        this.setSize(new Dimension(600,
                                   Common.screenRes.height < 800 ?
                                   Common.screenRes.height - 50 : 700));
        this.setLocation(this.getLocation().x+100, 10);
        this.setVisible(true);
        //progress.setVisible(false);
        if (file != null) this.fileName = file;
        if (this.fileName != null) loadFile();
        this.setTitle("Editor"+(fileName == null ? "" : (" - "+fileName)));
        editArea.requestFocus();
    }
	
	
    public void setTitle(String title) {
		int length,cc=1;
		if (System.getProperty("os.name").startsWith("Win")) {
			super.setTitle(title);
			return;
		}
		try {
			length = this.getFontMetrics(this.getFont()).stringWidth(title);
		} catch (Exception e) {
			super.setTitle(title==null?"Editor":title);
			return;
		}
		boolean next = true;
		while (length > this.getWidth()-100 && next) {
			StringTokenizer st = new StringTokenizer(title,File.separator);
			int i = st.countTokens();
			if (!JGRPrefs.isMac) title = st.nextElement()+""+File.separator;
			else title = File.separator;
			if (cc > i) {
				for (int z = 1; z< i && st.hasMoreTokens(); z++)
					st.nextToken();
				title = st.nextToken();
				next = false;
			}
			else {
				for (int z = 1; z <= i && st.hasMoreTokens(); z++) {
					if (z <= i/2 - (cc - cc/2) || z > i/2 + cc /2 )
						title += st.nextToken()+""+(st.hasMoreTokens()?File.separator:"");
					else {
						title += "..."+File.separator;
						st.nextToken();
					}
				}
				next = true;
			}
			length = this.getFontMetrics(this.getFont()).stringWidth(title);
			cc++;
		}
		super.setTitle(title);
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
        String newFile = null;
        FileSelector fopen = new FileSelector(this, "Open...",
                                              FileSelector.OPEN, directory);
        if (fopen.getFile() != null) {
            if (!modified) editArea.setText("");
            newFile = (directory = fopen.getDirectory()) + fopen.getFile();
        }
        if (!modified && newFile != null && newFile.trim().length() > 0) { fileName = newFile; loadFile();}
        else if (newFile != null && newFile.trim().length() > 0) new Editor(newFile);
    }
	
    public void loadFile() {
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
                    } catch (Exception e) {
                        setWorking(false);
                    }
                    setWorking(false);
                }
            };
            t.start();
        }
        catch (Exception e) {
            setWorking(false);
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
        this.setTitle(fileName==null?"Editor":fileName);
        editArea.requestFocus();
    }
	
    public void setText(StringBuffer sb) {
        cursorWait();
        editArea.setText(sb.toString());
        cursorDefault();
    }
	
	
    public void print() {
        DocumentRenderer docrender = new DocumentRenderer();
        docrender.print(editArea);
    }
	
    public boolean saveFile() {
        if (fileName == null || fileName.equals("")) {
            return saveFileAs();
        } else {
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
        else if (cmd == "fontBigger") FontTracker.current.setFontBigger();
        else if (cmd == "fontSmaller") FontTracker.current.setFontSmaller();
        else if (cmd == "fontBigger") FontTracker.current.setFontBigger();
        else if (cmd == "fontSmaller") FontTracker.current.setFontSmaller();
        else if (cmd == "new") startNew();
        else if (cmd == "objectmgr") JGR.MAINRCONSOLE.execute("object.manager()");
        else if (cmd == "open") open();
        else if (cmd.startsWith("recent:")) {
            if (modified) new Editor(cmd.replaceFirst("recent:",""));
            else {
                fileName = cmd.replaceFirst("recent:","");
                loadFile();
            }
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
        else if (cmd == "search") textFinder.showFind(false);
        else if (cmd == "searchnext") textFinder.showFind(true);
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
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            if ((ke.isControlDown() || ke.isMetaDown()) && JGR.MAINRCONSOLE != null && editArea.getSelectedText() != null) {
                JGR.MAINRCONSOLE.execute(editArea.getSelectedText());
            }
            else if (editArea.mComplete != null && editArea.mComplete.isVisible()) {
                editArea.mComplete.completeCommand();
            }
            else editArea.insertAt(editArea.getCaretPosition(),"\n");
        }
        else if (ke.getKeyCode() == KeyEvent.VK_UP) {
            if (editArea.mComplete != null && editArea.mComplete.isVisible()) {
                editArea.mComplete.selectPrevios();
            }
        }
        else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
            if (editArea.mComplete != null && editArea.mComplete.isVisible()) {
                editArea.mComplete.selectNext();
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
