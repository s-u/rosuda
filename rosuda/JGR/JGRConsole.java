package org.rosuda.JGR;

// JGR - Java Gui for R, see http://www.rosuda.org/JGR/
// Copyright (C) 2003 - 2005 Markus Helbig
// --- for licensing information see LICENSE file in the original JGR
// distribution ---

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.undo.CannotUndoException;

import org.rosuda.JGR.editor.Editor;
import org.rosuda.JGR.editor.FindReplaceDialog;
import org.rosuda.JGR.toolkit.AboutDialog;
import org.rosuda.JGR.toolkit.ConsoleOutput;
import org.rosuda.JGR.toolkit.DataTable;
import org.rosuda.JGR.toolkit.FileSelector;
import org.rosuda.JGR.toolkit.FontTracker;
import org.rosuda.JGR.toolkit.JGRPrefs;
import org.rosuda.JGR.toolkit.PrefDialog;
import org.rosuda.JGR.toolkit.SelectionPreservingCaret;
import org.rosuda.JGR.toolkit.SyntaxInput;
import org.rosuda.JGR.toolkit.ToolBar;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REngineException;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.ibase.toolkit.TJFrame;
import org.rosuda.ibase.toolkit.WTentry;
import org.rosuda.ibase.toolkit.WinTracker;

/**
 * JGRConsole Console Frame, the main window of JGR.
 * 
 * @author Markus Helbig RoSuDa 2003 - 2005
 */

public class JGRConsole extends TJFrame implements ActionListener, KeyListener,
		FocusListener, RMainLoopCallbacks {

	private static final long serialVersionUID = 7379785188574795119L;

	private final JSplitPane consolePanel = new JSplitPane(
			JSplitPane.VERTICAL_SPLIT);

	/** Console output text panel */
	public ConsoleOutput output = new ConsoleOutput();

	/** Console command input area */
	public SyntaxInput input = new SyntaxInput("console", true);

	public Document inputDoc = input.getDocument();

	public Document outputDoc = output.getDocument();

	public static final int MENUMODIFIER = Common.isMac() ? Event.META_MASK
			: Event.CTRL_MASK;

	private ToolBar toolBar;

	private String wspace = null;

	public int currentHistPosition = 0;

	private final StringBuffer console = new StringBuffer();

	private boolean wasHistEvent = false;

	/** Position where the R splash ends (need for clearing the console */
	public int end = 0;

	public static String guiEnv = "gui.working.env";

	private Integer clearpoint = null;

	public JGRConsole() {
		this(null);
	}

	/**
	 * Create a new Console window.
	 * 
	 * @param workSpace
	 *            workspace which should be loaded when starting JGR
	 */
	public JGRConsole(File workSpace) {
		super("Console", false, TJFrame.clsMain);

		// Initialize JGRConsoleMenu
		String[] Menu = { "+", "File", "New Data", "newdata", "@LLoad Data",
				"loaddata", "-", "@NNew Document", "new", "@OOpen Document",
				"open", "!OSource File...", "source", "@SSave", "save", "-",
				"@DSet Working Directory", "setwd", "~File.Quit", "+", "Edit",
				"@ZUndo", "undo", "!ZRedo", "redo", "-", "@XCut", "cut",
				"@CCopy", "copy", "#Copy Special", "-", "@VPaste", "paste",
				"Delete", "delete", "@ASelect All", "selAll", "-", "@FFind",
				"search", "@GFind Next", "searchnext", "-", "!LClear Console",
				"clearconsole", "-", "!IIncrease Font Size", "fontBigger",
				"!DDecrease Font Size", "fontSmaller", "+", "Workspace",
				"Open", "openwsp", "Save", "savewsp", "Save as...",
				"saveaswsp", "-", "Clear All", "clearwp", "+",
				"Packages & Data", "@BObject Browser", "objectmgr",
				"DataTable", "table", "-", "Package Manager", "packagemgr",
				"Package Installer", "packageinst", "~Window", "+", "Help",
				"R Help", "help", "~Preferences", "~About", "0" };
		JMenuBar mb = EzMenuSwing.getEzMenu(this, this, Menu);
		JMenu rm = (JMenu) EzMenuSwing.getItem(this, "Copy Special");
		if (rm != null) {
			JMenuItem item1 = new JMenuItem("Copy Output");
			item1.setActionCommand("copyoutput");
			item1.addActionListener(this);
			rm.add(item1);
			JMenuItem item2 = new JMenuItem("Copy Commands");
			item2.setActionCommand("copycmds");
			item2.addActionListener(this);
			rm.add(item2);
			JMenuItem item3 = new JMenuItem("Copy Result");
			item3.setActionCommand("copyresult");
			item3.addActionListener(this);
			rm.add(item3);
		}

		// preference and about for non-mac systems
		if (!Common.isMac()) {
			EzMenuSwing.addMenuSeparator(this, "Edit");
			EzMenuSwing.addJMenuItem(this, "Edit", "Preferences",
					"preferences", this);
			EzMenuSwing.addJMenuItem(this, "Help", "About", "about", this);

			for (int i = 0; i < mb.getMenuCount(); i++) {
				if (mb.getMenu(i).getText().equals("Preferences")
						|| mb.getMenu(i).getText().equals("About")) {
					mb.remove(i);
					i--;
				}
				if (mb.getMenu(i).getText().equals("Edit")) {
					JMenuItem prefer = (JMenuItem) mb.getMenu(i)
							.getMenuComponent(
									mb.getMenu(i).getMenuComponentCount() - 1);
					prefer.setAccelerator(KeyStroke.getKeyStroke(',',
							MENUMODIFIER));
				}
			}
		}

		// Add History if we didn't found one in the user's home directory
		if (JGR.RHISTORY == null)
			JGR.RHISTORY = new Vector();
		currentHistPosition = JGR.RHISTORY.size();
		// Add default toolbar with stop button to interrupt R
		toolBar = new ToolBar(this, true);

		input.addKeyListener(this);
		input.setWordWrap(false);
		input.addFocusListener(this);
		inputDoc.addUndoableEditListener(toolBar.undoMgr);

		output.setEditable(false);
		output.addFocusListener(this);
		output.addKeyListener(this);
		output.setDragEnabled(true);
		output.setCaret(new SelectionPreservingCaret());

		JScrollPane sp1 = new JScrollPane(output);
		sp1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		consolePanel.setTopComponent(sp1);
		JScrollPane sp2 = new JScrollPane(input);
		sp2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		consolePanel.setBottomComponent(sp2);
		consolePanel
				.setDividerLocation(((int) ((double) this.getHeight() * 0.65)));

		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent evt) {
				super.componentResized(evt);
				if (JGR.getREngine() != null && JGR.STARTED) {
					try {
						JGR.eval("options(width=" + getFontWidth() + ")");
					} catch (REngineException e) {
						new ErrorMsg(e);
					} catch (REXPMismatchException e) {
						new ErrorMsg(e);
					}
				}
				consolePanel
						.setDividerLocation(((int) ((double) getHeight() * 0.70)));
			}

		});
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				dispose();
			}
		});
		this.addKeyListener(this);

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(toolBar, BorderLayout.NORTH);
		this.getContentPane().add(consolePanel, BorderLayout.CENTER);
		this.setMinimumSize(new Dimension(555, 650));
		this.setSize(new Dimension(800,
				Common.screenRes.height < 1000 ? Common.screenRes.height - 50
						: 900));
		// Point center = new
		// Point(Common.screenRes.width/2-this.getWidth()/2,40);
		// this.setLocation(center);
		this.setVisible(true);
		this
				.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		// progress.setVisible(false);
		input.mComplete.setVisible(false);
		new Thread(new Refresher()).start();
	}

	/**
	 * Close Console, but not before we asked the user if he wants to save
	 * opened Editors.
	 */
	public void exit() {
		Enumeration e = WinTracker.current.elements();
		while (e.hasMoreElements()) {
			WTentry we = (WTentry) e.nextElement();
			if (we.wclass == TJFrame.clsEditor)
				if (!((Editor) we.w).exit())
					return;
		}
		execute("q()", false);
	}

	/**
	 * Close Console, but not before we asked the user if he wants to save
	 * opened Editors.
	 */
	public void dispose() {
		Enumeration e = WinTracker.current.elements();
		while (e.hasMoreElements()) {
			WTentry we = (WTentry) e.nextElement();
			if (we.wclass == TJFrame.clsEditor)
				if (!((Editor) we.w).exit())
					return;
		}
		execute("q()", false);
	}

	/**
	 * Execute a coomand and add it to history
	 * 
	 * @param cmd
	 *            command for execution
	 */
	public void execute(String cmd) {
		execute(cmd, true);
	}

	/**
	 * Execute a coomand and add it to history
	 * 
	 * @param cmd
	 *            command for execution
	 */
	public void executeLater(String cmd) {
		final String cm = cmd;
		Runnable doWorkRunnable = new Runnable() {
			public void run() {
				execute(cm, true);
			}
		};
		SwingUtilities.invokeLater(doWorkRunnable);
	}

	/**
	 * Execute a coomand and add it to history
	 * 
	 * @param cmd
	 *            command for execution
	 * @param addToHist
	 *            indicates wether the command should be added to history or not
	 */
	public void executeLater(String cmd, boolean addToHist) {
		final String cm = cmd;
		final boolean add = addToHist;
		Runnable doWorkRunnable = new Runnable() {
			public void run() {
				execute(cm, add);
			}
		};
		SwingUtilities.invokeLater(doWorkRunnable);

	}

	/**
	 * Execute a command and add it into history.
	 * 
	 * @param cmd
	 *            command for execution
	 * @param addToHist
	 *            indicates wether the command should be added to history or not
	 */
	public void execute(String cmd, boolean addToHist) {
		if (!JGR.STARTED)
			return;
		if (addToHist && JGR.RHISTORY.size() == 0)
			JGR.RHISTORY.add(cmd);
		else if (addToHist && cmd.trim().length() > 0
				&& JGR.RHISTORY.size() > 0
				&& !JGR.RHISTORY.lastElement().equals(cmd.trim()))
			JGR.RHISTORY.add(cmd);
		currentHistPosition = JGR.RHISTORY.size();

		String[] cmdArray = cmd.split("\n");

		String c = null;
		for (int i = 0; i < cmdArray.length; i++) {
			c = cmdArray[i];
			if (isSupported(c))
				JGR.rSync.triggerNotification(c.trim());
		}
	}

	/**
	 * Gets a unique name based on a starting string
	 * 
	 * @param var
	 * @return the value of var concatinated with a number
	 */
	public String getUniqueName(String var) {
		JGR.refreshObjects();
		var = RController.makeValidVariableName(var);
		if (!JGR.OBJECTS.contains(var))
			return var;
		int i = 1;
		while (true) {
			if (!JGR.OBJECTS.contains(var + i))
				return var + i;
			i++;
		}
	}

	/**
	 * Gets a unique name based on a starting string
	 * 
	 * @param var
	 * @param envName
	 *            The name of the enviroment in which to look
	 * @return the value of var concatinated with a number
	 */
	public String getUniqueName(String var, String envName) {
		var = RController.makeValidVariableName(var);

		try {
			REXPLogical temp = (REXPLogical) JGR.eval("is.environment("
					+ envName + ")");
			boolean isEnv = temp.isTRUE()[0];
			if (!isEnv)
				return var;
		} catch (REngineException e) {
			new ErrorMsg(e);
			return var;
		} catch (REXPMismatchException e) {
			new ErrorMsg(e);
			return var;
		}

		boolean isUnique = false;

		try {

			REXPLogical temp = (REXPLogical) JGR.eval("exists('" + var
					+ "',where=" + envName + ",inherits=FALSE)");
			isUnique = temp.isFALSE()[0];
			if (isUnique)
				return var;

		} catch (REngineException e) {
			new ErrorMsg(e);
			return var;
		} catch (REXPMismatchException e) {
			new ErrorMsg(e);
			return var;
		}

		int i = 1;
		while (true) {

			try {

				REXPLogical temp = (REXPLogical) JGR
						.eval("exists('" + (var + i) + "',where=" + envName
								+ ",inherits=FALSE)");
				isUnique = temp.isFALSE()[0];

			} catch (REngineException e) {
				new ErrorMsg(e);
			} catch (REXPMismatchException e) {
				new ErrorMsg(e);
			}

			if (isUnique)
				return var + i;
			i++;
		}
	}

	private boolean isSupported(String cmd) {
		cmd = cmd.trim();
		if (cmd.startsWith("fix(") || cmd.startsWith("edit(")
				|| cmd.startsWith("edit.data.frame(")) {
			try {
				output.append(cmd + "\n",JGRPrefs.CMD);
			} catch (Exception e) {
			}
			try {
				output.append("Editing is not supported yet!", JGRPrefs.RESULT);
			} catch (Exception e) {
			}
			try {
				output.append( "\n" + RController.getRPrompt(), JGRPrefs.CMD);
			} catch (Exception e) {
			}
			return false;
		}

		return true;
	}

	/**
	 * Clear the console's content, if it's too full.
	 */
	public void clearconsole() {
		try {
			if (end > 0 && clearpoint == null)
				clearpoint = new Integer(output.getLineEndOffset(output
						.getLineOfOffset(end) - 1) + 2);
			if (clearpoint != null)
				output.removeAllFrom(clearpoint.intValue());
		} catch (Exception e) {
			new ErrorMsg(e);/* e.printStackTrace(); */
		}
	}

	/**
	 * Load a workspace, R-command: load(...).
	 */
	public void loadWorkSpace() {
		FileSelector fopen = new FileSelector(this, "Open Workspace",
				FileSelector.LOAD);
		fopen.setVisible(true);
		if (fopen.getFile() != null) {
			wspace = (JGRPrefs.workingDirectory = fopen.getDirectory())
					+ fopen.getFile();
			execute("load(\"" + wspace.replace('\\', '/') + "\")", false);
			try {
				// to avoid quoting hell we use an assignment
				JGR.getREngine().assign(".$JGR",
						new REXPString(JGRPrefs.workingDirectory));
				JGR.eval("try({setwd(`.$JGR`); rm(`.$JGR`)},silent=T)");
			} catch (REngineException e) {
				new ErrorMsg(e);
			} catch (REXPMismatchException e) {
				new ErrorMsg(e);
			}
		}
	}

	/**
	 * Save workspace with specified filename, R-command: save.image(...).
	 * 
	 * @param file
	 *            filename
	 */
	public void saveWorkSpace(String file) {
		if (file == null)
			executeLater("save.image()");
		else
			executeLater("save.image(\""
					+ (file == null ? "" : file.replace('\\', '/'))
					+ "\",compress=TRUE)");
		JGR.writeHistory();
	}

	/**
	 * Save workspace to a different file then .RData.
	 */
	public void saveWorkSpaceAs() {
		FileSelector fsave = new FileSelector(this, "Save Workspace as...",
				FileSelector.SAVE);
		fsave.setVisible(true);
		if (fsave.getFile() != null) {
			String file = (JGRPrefs.workingDirectory = fsave.getDirectory())
					+ fsave.getFile();
			saveWorkSpace(file);
			JGR.writeHistory();
		}
	}

	/**
	 * Get the font's width form current settings using {@see FontMetrics}.
	 * 
	 * @return fontwidth
	 */
	public int getFontWidth() {
		int width = output.getFontMetrics(output.getFont()).charWidth('M');
		width = output.getWidth() / width;
		return (int) (width) - (JGRPrefs.isMac ? 0 : 1);
	}

	// ======================================================= R callbacks ===

	/**
	 * Write output from R into console (new R callback).
	 * 
	 * @param re
	 *            used Rengine
	 * @param text
	 *            output
	 * @param oType
	 *            output type (0=regular, 1=warning/error)
	 */
	public void rWriteConsole(org.rosuda.JRI.Rengine re, String text, int oType) {
		if (readCount == 2) {
			end = output.getText().length();
			readCount = 3;
		}
		final String t = text;
		Runnable doWork = new Runnable() {
			public void run() {
				console.append(t);
			}
		};
		SwingUtilities.invokeLater(doWork);
	}

	/**
	 * Write output from R into console (old R callback).
	 * 
	 * @param re
	 *            used Rengine
	 * @param text
	 *            output
	 */
	public void rWriteConsole(org.rosuda.JRI.Rengine re, String text) {
		rWriteConsole(re, text, 0);
	}

	/**
	 * Invoke the busy cursor (R callback).
	 * 
	 * @param re
	 *            used Rengine
	 * @param which
	 *            busy (1) or not (0)
	 */
	public void rBusy(org.rosuda.JRI.Rengine re, int which) {
		final int fWhich = which;
		Runnable doWork = new Runnable() {
			public void run() {
				if (fWhich == 0) {
					setWorking(false);
				} else {
					toolBar.stopButton.setEnabled(true);
					setWorking(true);
				}
			}
		};
		SwingUtilities.invokeLater(doWork);

	}

	private int readCount = 0;

	/**
	 * Read the commands from input area (R callback).
	 * 
	 * @param re
	 *            used Rengine
	 * @param prompt
	 *            prompt from R
	 * @param addToHistory
	 *            is it an command which to add to the history
	 */
	public String rReadConsole(org.rosuda.JRI.Rengine re, String prompt,
			int addToHistory) {
		if (readCount < 2)
			readCount++;
		Runnable doWork = new Runnable() {
			public void run() {
				toolBar.stopButton.setEnabled(false);
			}
		};
		SwingUtilities.invokeLater(doWork);
		if (prompt.indexOf("Save workspace") > -1) {
			String retVal = JGR.exit();
			if (wspace != null && retVal.indexOf('y') >= 0) {
				try {
					JGR.getREngine().eval(
							new REXPString("save.image(\""
									+ wspace.replace('\\', '/') + "\")"), null,
							false);
				} catch (REngineException e) {
					new ErrorMsg(e);
				} catch (REXPMismatchException e) {
					new ErrorMsg(e);
				}
				return "n\n";
			} else
				return retVal;
		} else {
			final String fPrompt = prompt;
			Runnable doWork1 = new Runnable() {
				public void run() {
					if (console.length() > 0) {
						output.append(console.toString(), JGRPrefs.RESULT);
						console.delete(0, console.length());
						output.setCaretPosition(outputDoc.getLength());
					}
					output.append(fPrompt, JGRPrefs.CMD);
					output.setCaretPosition(outputDoc.getLength());
				}
			};
			SwingUtilities.invokeLater(doWork1);
			final String s = JGR.rSync.waitForNotification();
			Runnable doWork2 = new Runnable() {
				public void run() {
					try {
						output.append(s + "\n",JGRPrefs.CMD);
						if (console.length() > 0) {
							output.append(console.toString(), JGRPrefs.RESULT);
							console.delete(0, console.length());
							output.setCaretPosition(outputDoc.getLength());
						}
					} catch (Exception e) {
						new ErrorMsg(e);
					}
				}
			};
			SwingUtilities.invokeLater(doWork2);

			return (s == null || s.length() == 0) ? "\n" : s + "\n";
		}

	}

	/**
	 * Showing a message from the rengine (R callback).
	 * 
	 * @param re
	 *            used Rengine
	 * @param message
	 *            message from R
	 */
	public void rShowMessage(org.rosuda.JRI.Rengine re, String message) {
		JOptionPane.showMessageDialog(this, message, "R Message",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Choose a file invoked be file.choose() (R callback).
	 * 
	 * @param re
	 *            used Rengine
	 * @param newFile
	 *            if it's a new file
	 */
	public String rChooseFile(org.rosuda.JRI.Rengine re, int newFile) {
		FileSelector fd = new FileSelector(this,
				(newFile == 0) ? "Select a file" : "Select a new file",
				(newFile == 0) ? FileDialog.LOAD : FileDialog.SAVE);
		fd.setVisible(true);
		String res = null;
		if (fd.getDirectory() != null && fd.getFile() != null)
			res = fd.getDirectory();
		if (fd.getFile() != null)
			res = (res == null) ? fd.getFile() : (res + fd.getFile());
		return res;
	}

	/**
	 * Flush the console (R callback). !! not implemented yet !!
	 * 
	 * @param re
	 *            used Rengine
	 */
	public void rFlushConsole(org.rosuda.JRI.Rengine re) {
	}

	/**
	 * Load history from a file (R callback).
	 * 
	 * @param re
	 *            used Rengine
	 * @param filename
	 *            history file
	 */
	public void rLoadHistory(org.rosuda.JRI.Rengine re, String filename) {
		File hist = null;
		try {
			if ((hist = new File(filename)).exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(hist));
				if (JGR.RHISTORY == null)
					JGR.RHISTORY = new Vector();
				while (reader.ready())
					JGR.RHISTORY.add(reader.readLine() + "\n");
				reader.close();
			}
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}

	/**
	 * Save history to a file (R callback).
	 * 
	 * @param re
	 *            used Rengine
	 * @param filename
	 *            history file
	 */
	public void rSaveHistory(org.rosuda.JRI.Rengine re, String filename) {
		try {
			File hist = new File(filename);
			BufferedWriter writer = new BufferedWriter(new FileWriter(hist));
			Enumeration e = JGR.RHISTORY.elements();
			while (e.hasMoreElements())
				writer.write(e.nextElement().toString() + "#\n");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}

	// ======================================================= other events ===

	/**
	 * actionPerformed: handle action event: menus.
	 */
	public void actionPerformed(ActionEvent e) {

		String cmd = e.getActionCommand();
		if (cmd == "about")
			new AboutDialog(this);
		else if (cmd == "cut")
			input.cut();
		else if (cmd == "clearwsp")
			execute("rm(list=ls())", false);
		else if (cmd == "copy") {
			input.copy();
			output.copy();
		} else if (cmd == "copyoutput")
			output.copyOutput();
		else if (cmd == "print") {
			try {
				// output.print();
			} catch (Exception exc) {
			}
		} else if (cmd == "copycmds")
			output.copyCommands();
		else if (cmd == "copyresult")
			output.copyResults();
		else if (cmd == "clearconsole")
			clearconsole();
		else if (cmd == "delete")
			try {
				int i = 0;
				inputDoc.remove((i = input.getSelectionStart()), input
						.getSelectionEnd()
						- i);
			} catch (BadLocationException ex) {
			}
		else if (cmd == "editor")
			new Editor();
		else if (cmd == "exit")
			dispose();
		else if (cmd == "exportOutput")
			output.startExport();
		else if (cmd == "fontBigger")
			FontTracker.current.setFontBigger();
		else if (cmd == "fontSmaller")
			FontTracker.current.setFontSmaller();
		else if (cmd == "newdata") {
			String inputValue = JOptionPane.showInputDialog("Data Name: ");
			if (inputValue != null)
				execute(inputValue.trim() + "<-data.frame()");
		} else if (cmd == "loaddata")
			new DataLoader();
		else if (cmd == "open") {
			Editor temp = new Editor(null, false);
			temp.open();
		} else if (cmd == "openwsp")
			loadWorkSpace();
		else if (cmd == "new")
			new Editor();
		// else if (cmd == "newwsp") newWorkSpace();
		else if (cmd == "objectmgr")
			execute("object.browser()", false);
		else if (cmd == "packagemgr")
			execute("JGR::package.manager()", false);
		else if (cmd == "packageinst")
			execute("installPackages()", false);
		else if (cmd == "paste")
			input.paste();
		else if (cmd == "preferences") {
			PrefDialog inst = PrefDialog.showPreferences(this);
			inst.setLocationRelativeTo(null);
			inst.setVisible(true);
		} else if (cmd == "redo")
			try {
				if (toolBar.undoMgr.canRedo())
					toolBar.undoMgr.redo();
			} catch (CannotUndoException ex) {
			}
		else if (cmd == "help")
			executeLater("help.start()");
		else if (cmd == "table")
			new DataTable(null, null, true);
		else if (cmd == "save")
			output.startExport();
		else if (cmd == "savewsp")
			saveWorkSpace(wspace);
		else if (cmd == "saveaswsp")
			saveWorkSpaceAs();
		else if (cmd == "clearwp") {
			int doIt = JOptionPane
					.showConfirmDialog(
							this,
							"Are you sure you wish to clear "
									+ "your workspace?\nAll unsaved objects will be deleted.",
							"Clear Workspace", JOptionPane.YES_NO_OPTION);
			if (doIt == JOptionPane.OK_OPTION)
				execute("rm(list=ls())");
		} else if (cmd == "search")
			// textFinder.showFind(false);
			FindReplaceDialog.findExt(this, output);
		else if (cmd == "searchnext")
			FindReplaceDialog.findNextExt(this, output);
		// textFinder.showFind(true);
		else if (cmd == "source")
			execute("source(file.choose())", false);
		else if (cmd == "stop") {
			try {
				new Thread(new Runnable() {
					public void run() {
						((org.rosuda.REngine.JRI.JRIEngine) JGR.getREngine())
								.getRni().rniStop(0);
					}
				}).start();
			} catch (Exception exe) {
				new ErrorMsg(exe);
			}
		} else if (cmd == "selAll") {
			if (input.isFocusOwner())
				input.selectAll();
			else if (output.isFocusOwner())
				output.selectAll();
		} else if (cmd == "undo")
			try {
				if (toolBar.undoMgr.canUndo())
					toolBar.undoMgr.undo();
			} catch (Exception ex) {
			}
		else if (cmd == "setwd") {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Choose Working Directory");
			chooser.setApproveButtonText("Choose");
			int r = chooser.showOpenDialog(this);
			if (r == JFileChooser.CANCEL_OPTION)
				return;
			if (chooser.getSelectedFile() != null)
				JGRPrefs.workingDirectory = chooser.getSelectedFile()
						.toString();
			executeLater("setwd(\""
					+ chooser.getSelectedFile().toString().replace('\\', '/')
					+ "\")");
		} else if (cmd == "update")
			execute("update.JGR(contriburl=\"http://rosuda.org/R/nightly\")",
					false);
	}

	/**
	 * keyTyped: handle key event.
	 */
	public void keyTyped(KeyEvent ke) {
	}

	/**
	 * keyPressed: handle key event, like: adding a new line, history ....
	 */
	public void keyPressed(KeyEvent ke) {
		if (ke.getSource().equals(output) && !ke.isMetaDown()
				&& !ke.isControlDown() && !ke.isAltDown())
			input.requestFocus();
		if (ke.getKeyCode() == KeyEvent.VK_UP) {
			if (input.mComplete != null && input.mComplete.isVisible())
				input.mComplete.selectPrevious();
			else if (currentHistPosition > 0)
				if (input.getCaretPosition() == 0
						|| input.getCaretPosition() == input.getText().length()) {
					input.setText(JGR.RHISTORY.get(--currentHistPosition)
							.toString());
					input.setCaretPosition(input.getText().length());
					wasHistEvent = true;
				}
		} else if (ke.getKeyCode() == KeyEvent.VK_DOWN)
			if (input.mComplete != null && input.mComplete.isVisible())
				input.mComplete.selectNext();
			else if (input.getCaretPosition() == 0
					|| input.getCaretPosition() == input.getText().length()) {
				if (currentHistPosition < JGR.RHISTORY.size() - 1) {
					input.setText(JGR.RHISTORY.get(++currentHistPosition)
							.toString());
					input.setCaretPosition(input.getText().length());
				} else if (JGR.RHISTORY.size() > 0
						&& currentHistPosition < JGR.RHISTORY.size()) {
					input.setText("");
					currentHistPosition++;
				}
				wasHistEvent = true;
			}
	}

	/**
	 * keyReleased: handle key event, sending the command.
	 */
	public void keyReleased(KeyEvent ke) {
		if (ke.getKeyCode() == KeyEvent.VK_ENTER)
			if (input.mComplete != null && input.mComplete.isVisible()
					&& !(ke.isControlDown() || ke.isMetaDown()))
				input.mComplete.completeCommand();
			else if (ke.isControlDown() || ke.isMetaDown())
				try {
					inputDoc.insertString(input.getCaretPosition(), "\n", null);
					input.mComplete.setVisible(false);
				} catch (Exception e) {
				}
			else {
				String cmd = input.getText().trim();
				input.setText("");
				input.setCaretPosition(0);
				input.requestFocus();
				execute(cmd);
			}
		if (ke.getSource().equals(output) && ke.getKeyCode() == KeyEvent.VK_V
				&& (ke.isControlDown() || ke.isMetaDown())) {
			input.requestFocus();
			input.paste();
			input.setCaretPosition(input.getText().length());
		} else if ((ke.getKeyCode() == KeyEvent.VK_UP || ke.getKeyCode() == KeyEvent.VK_DOWN)
				&& wasHistEvent) {
			wasHistEvent = false;
			input.setCaretPosition(input.getText().length());
		}
	}

	/**
	 * focusGained: handle focus event, enable and disable cut and paste button.
	 */
	public void focusGained(FocusEvent e) {
		if (e.getSource().equals(output)) {
			toolBar.cutButton.setEnabled(false);
			EzMenuSwing.getItem(this, "cut").setEnabled(false);
			toolBar.pasteButton.setEnabled(false);
			EzMenuSwing.getItem(this, "paste").setEnabled(false);
		} else if (e.getSource().equals(input)) {
			toolBar.cutButton.setEnabled(true);
			EzMenuSwing.getItem(this, "cut").setEnabled(true);
			toolBar.pasteButton.setEnabled(true);
			EzMenuSwing.getItem(this, "paste").setEnabled(true);
		}
	}

	/**
	 * focusLost: handle focus event.
	 */
	public void focusLost(FocusEvent e) {
	}

	class Refresher implements Runnable {
		public Refresher() {
		}

		public void run() {
			while (true)
				try {
					Thread.sleep(1000);
					Runnable doWorkRunnable = new Runnable() {
						public void run() {
							if (console.length() > 0) {
								output.append(console.toString(),
										JGRPrefs.RESULT);
								console.delete(0, console.length());
								output.setCaretPosition(outputDoc.getLength());
							}
						}
					};
					SwingUtilities.invokeLater(doWorkRunnable);
				} catch (Exception e) {
					new ErrorMsg(e);
				}
		}
	}

	public PrintStream getStdOutPrintStream() {
		final MutableAttributeSet coloring = JGRPrefs.RESULT;
		return getPrintStream(coloring);
	}

	private PrintStream getPrintStream(final MutableAttributeSet coloring) {
		OutputStream out = new OutputStream() {
			public void write(int b) throws IOException {
				output.append(String.valueOf((char) b), coloring);
			}
		};
		PrintStream p = new PrintStream(out);
		return p;
	}
	
	public PrintStream getStdErrPrintStream() {
		final MutableAttributeSet coloring = JGRPrefs.RESULT;
		return getPrintStream(coloring);
	}

}
