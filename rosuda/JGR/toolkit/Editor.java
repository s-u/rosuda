package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.CannotUndoException;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.editor.FindReplaceDialog;
import org.rosuda.JGR.util.DocumentRenderer;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.ibase.Common;
import org.rosuda.util.RecentList;

import org.rosuda.ibase.toolkit.EzMenu;
import org.rosuda.ibase.toolkit.TJFrame;

/**
 * Editor - simple implementation of an editor with syntaxhighlighting.
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2005
 */

public class Editor extends TJFrame implements ActionListener, KeyListener {

	private final CaretListenerLabel caretStatus = new CaretListenerLabel();

	private final JLabel modifiedStatus = new JLabel();

	private final SyntaxInput editArea = new SyntaxInput("editor", true);

	private final Document editDoc = editArea.getDocument();

	private ToolBar toolBar;

	String fileName = null;

	private boolean modified = false;

	private final TextFinder textFinder = new TextFinder(editArea);

	/** Recent documents which where opened the last times with the editor. */
	public static RecentList recentOpen;

	/** Menuitem for the recent-list */
	public Menu recentMenu;

	public Editor() {
		this(null);
	}

	/**
	 * Create a new editor with supplied file opening immediatly.
	 * 
	 * @param file
	 *            file to open
	 */
	public Editor(String file) {
		super("Editor", false, TJFrame.clsEditor);
		
		String[] Menu = { "+", "File", "@NNew", "new", "@OOpen", "open", "#Open Recent", "@SSave", "save", "!SSave as", "saveas", "-", "@PPrint", "print", "-",
				"@QQuit", "quit", 
				"+", "Edit", "@ZUndo","undo","!ZRedo","redo","-","@XCut", "cut", "@CCopy", "copy", "@VPaste", "paste", "-", 
				"@KComment","commentcode","!KUncomment Code","uncommentcode","-","!LShift Left","shiftleft","!RShift Right","shiftright","-","@RRun Selection","runselection","!RRun all","runall","-", "@FFind", "find", "@GFind next",
				"findnext", 
				
				"+", "Tools", "Increase Font Size", "fontBigger", "Decrease Font Size", "fontSmaller", 
				"~Window", "+","Help","R Help","rhelp", "~About", "0" };
		EzMenu.getEzMenu(this, this, Menu);
		Menu rm = recentMenu = (Menu) EzMenu.getItem(this,"Open Recent");
		System.out.println(rm);
		if (rm != null) {
			rm.removeAll();
			if (recentOpen == null)
				recentOpen = new RecentList("JGR", "RecentOpenFiles", 8);
			String[] shortNames = recentOpen.getShortEntries();
			String[] longNames = recentOpen.getAllEntries();
			int i = 0;
			while (i < shortNames.length) {
				MenuItem mi = new MenuItem(shortNames[i]);
				mi.setActionCommand("recent:" + longNames[i]);
				mi.addActionListener(this);
				rm.add(mi);
				i++;
			}
			if (i > 0)
				rm.addSeparator();
			MenuItem ca = new MenuItem("Clear list");
			ca.setActionCommand("recent-clear");
			ca.addActionListener(this);
			rm.add(ca);
			if (i == 0)
				ca.setEnabled(false);
		}

		toolBar = new ToolBar(this, false);

		editArea.addCaretListener(caretStatus);
		editArea.addKeyListener(this);
		editArea.setWordWrap(false);
		// editArea.setDragEnabled(true);

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
		this.getContentPane().add(toolBar, BorderLayout.NORTH);
		JScrollPane sp = new JScrollPane(editArea);
		sp
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// LineNumbers ln = new LineNumbers(editArea, sp);
		// sp.setRowHeaderView(ln);

		this.getContentPane().add(sp, BorderLayout.CENTER);
		this.getContentPane().add(status, BorderLayout.SOUTH);

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

		this.setMinimumSize(new Dimension(600, 600));
		this.setSize(new Dimension(600,
				Common.screenRes.height < 800 ? Common.screenRes.height - 50
						: 700));
		this.setLocation(this.getLocation().x + 100, 10);
		this.setVisible(true);
		// progress.setVisible(false);
		editArea.mComplete.setVisible(false);
		if (file != null)
			fileName = file;
		if (fileName != null)
			loadFile();
		this.setTitle(fileName == null ? "" : fileName);
		editArea.requestFocus();
	}

	/**
	 * Set title of the editor, which will be the shortend filename.
	 */
	public void setTitle(String title) {
		int length, cc = 1;
		if (System.getProperty("os.name").startsWith("Win")) {
			super.setTitle(title == null ? "Editor" : title);
			return;
		}
		try {
			length = this.getFontMetrics(this.getFont()).stringWidth(title);
		} catch (Exception e) {
			super.setTitle(title == null ? "Editor" : title);
			return;
		}
		boolean next = true;
		while (length > this.getWidth() - 100 && next) {
			StringTokenizer st = new StringTokenizer(title, File.separator);
			int i = st.countTokens();
			if (!JGRPrefs.isMac)
				title = st.nextElement() + "" + File.separator;
			else
				title = File.separator;
			if (cc > i) {
				for (int z = 1; z < i && st.hasMoreTokens(); z++)
					st.nextToken();
				if (st.hasMoreTokens())
					title = st.nextToken();
				next = false;
			} else {
				for (int z = 1; z <= i && st.hasMoreTokens(); z++)
					if (z <= i / 2 - (cc - cc / 2) || z > i / 2 + cc / 2)
						title += st.nextToken() + ""
								+ (st.hasMoreTokens() ? File.separator : "");
					else {
						title += "..." + File.separator;
						st.nextToken();
					}
				next = true;
			}
			length = this.getFontMetrics(this.getFont()).stringWidth(title);
			cc++;
		}
		super.setTitle(title);
	}

	/**
	 * Exit editor but ask the user if he wants to save current content.
	 * 
	 * @return true if it should be disposed now or not
	 */
	public boolean exit() {
		if (modified) {
			int i = JOptionPane.showConfirmDialog(this, "Save File?", "Exit",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (i == 1) {
				dispose();
				return true;
			} else if (i == 0 && saveFile()) {
				dispose();
				return true;
			} else
				return false;
		}
		dispose();
		return true;
	}

	/**
	 * Open a file and load it into editor.
	 */
	public void open() {
		FileSelector fopen = new FileSelector(this, "Open...",
				FileSelector.LOAD, JGRPrefs.workingDirectory);
		fopen.setVisible(true);

		if (Common.isMac())
			openFile(fopen);
		else if (((JFileChooser) fopen.getSelector()).toString().indexOf(
				"APPROVE_OPTION") != -1)
			openFile(fopen);
	}

	private void openFile(FileSelector fopen) {
		String newFile = null;
		if (fopen.getFile() != null)
			newFile = (JGRPrefs.workingDirectory = fopen.getDirectory())
					+ fopen.getFile();
		if (editArea.getText().length() == 0 && newFile != null
				&& newFile.trim().length() > 0) {
			fileName = newFile;
			loadFile();
		} else if (newFile != null && newFile.trim().length() > 0)
			new Editor(newFile);
	}

	/**
	 * Load file into textarea.
	 */
	public void loadFile() {
		setWorking(true);
		editArea.setText("");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			/*
			 * Thread t = new Thread() { public void run() {
			 */
			try {
				StringBuffer text = new StringBuffer();
				while (reader.ready()) {
					text.append(reader.readLine() + "\n");
					if (text.length() > 32000) {
						editArea.append(text.toString());
						text.delete(0, text.length());
						try {
							Thread.sleep(2);
						} catch (Exception e) {
						}
					}
				}
				reader.close();
				editArea.append(text.toString());
				text.delete(0, text.length());
			} catch (Exception e) {
				new ErrorMsg(e);
				setWorking(false);
			}
			setWorking(false);
			// }
			/*
			 * }; t.start();
			 */
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this,
					"Could not find selected File.", "File not found!",
					JOptionPane.OK_OPTION);
		} catch (Exception e) {
			new ErrorMsg(e);
		} finally {
			editArea.setCaretPosition(0);
			setWorking(false);
		}
		recentOpen.addEntry(fileName);
		Menu rm = recentMenu = (Menu) EzMenu.getItem(this, "Open Recent");
		if (rm != null) {
			rm.removeAll();
			if (recentOpen == null)
				recentOpen = new RecentList("JGR", "RecentOpenFiles", 8);
			String[] shortNames = recentOpen.getShortEntries();
			String[] longNames = recentOpen.getAllEntries();
			int i = 0;
			while (i < shortNames.length) {
				MenuItem mi = new MenuItem(shortNames[i]);
				mi.setActionCommand("recent:" + longNames[i]);
				mi.addActionListener(this);
				rm.add(mi);
				i++;
			}
			if (i > 0)
				rm.addSeparator();
			MenuItem ca = new MenuItem("Clear list");
			ca.setActionCommand("recent-clear");
			ca.addActionListener(this);
			rm.add(ca);
			if (i == 0)
				ca.setEnabled(false);
		}
		this.setTitle(fileName == null ? "Editor" : fileName);
		editArea.requestFocus();
	}

	/**
	 * Set text into area, while doing this show spinwheel.
	 * 
	 * @param sb
	 */
	public void setText(StringBuffer sb) {
		this.setWorking(true);
		editArea.setText(sb.toString());
		this.setWorking(false);
	}

	/**
	 * Print current content.
	 */
	public void print() {
		DocumentRenderer docrender = new DocumentRenderer();
		docrender.print(editArea);
	}

	/**
	 * Save current content to file.
	 * 
	 * @return if saved true else false
	 */
	public boolean saveFile() {
		if (fileName == null || fileName.equals(""))
			return saveFileAs();
		setWorking(true);
		new FileSave(this);
		this.setTitle(fileName == null ? "Editor" : fileName);
		setModified(modified = false);
		return true;
	}

	/**
	 * Save current content to a choosen filename.
	 * 
	 * @return true if saved else false
	 */
	public boolean saveFileAs() {
		FileSelector fsave = new FileSelector(this, "Save as...",
				FileSelector.SAVE, JGRPrefs.workingDirectory);
		fsave.setVisible(true);
		if (fsave.getFile() != null) {
			fileName = (JGRPrefs.workingDirectory = fsave.getDirectory())
					+ fsave.getFile();
			return saveFile();
		}
		return false;
	}

	private void setModified(boolean mod) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				modifiedStatus.setText(modified ? "Modified" : "");
			}
		});
	}

	private void startNew() {
		new Editor();
	}

	/**
	 * actionPerformed: handle action event: menus and buttons
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == "about")
			new AboutDialog(this);
		else if (cmd == "cut")
			editArea.cut();
		else if (cmd == "commentcode") {
			if (editArea.getSelectedText().trim().length() > 0)
				try {
					editArea.commentSelection(true);
				} catch (BadLocationException e1) {
				}
		} else if (cmd == "uncommentcode") {
			if (editArea.getSelectedText().trim().length() > 0)
				try {
					editArea.commentSelection(false);
				} catch (BadLocationException e1) {
				}
		}

		else if (cmd == "copy")
			editArea.copy();
		else if (cmd == "delete")
			try {
				int i = 0;
				editDoc.remove((i = editArea.getSelectionStart()), editArea
						.getSelectionEnd()
						- i);
			} catch (BadLocationException ex) {
			}
		else if (cmd == "exit")
			exit();
		else if (cmd == "fontBigger")
			FontTracker.current.setFontBigger();
		else if (cmd == "fontSmaller")
			FontTracker.current.setFontSmaller();
		else if (cmd == "fontBigger")
			FontTracker.current.setFontBigger();
		else if (cmd == "fontSmaller")
			FontTracker.current.setFontSmaller();
		else if (cmd == "help")
			JGR.MAINRCONSOLE.execute("help.start()", false);
		else if (cmd == "new")
			startNew();
		else if (cmd == "objectmgr")
			JGR.MAINRCONSOLE.execute("object.manager()", false);
		else if (cmd == "open")
			open();
		else if (cmd.startsWith("recent:")) {
			if (modified)
				new Editor(cmd.replaceFirst("recent:", ""));
			else {
				fileName = cmd.replaceFirst("recent:", "");
				loadFile();
			}
		} else if (cmd == "paste")
			editArea.paste();
		else if (cmd == "prefs")
			new PrefsDialog(this);
		else if (cmd == "print")
			print();
		else if (cmd == "recent-clear") {
			if (recentOpen != null && recentMenu != null) {
				recentMenu.removeAll();
				recentMenu.addSeparator();
				MenuItem ca = new MenuItem("Clear list");
				ca.setActionCommand("recent-clear");
				ca.addActionListener(this);
				ca.setEnabled(false);
				recentMenu.add(ca);
				recentOpen.reset();
			}
		} else if (cmd == "redo")
			try {
				if (toolBar.undoMgr.canRedo())
					toolBar.undoMgr.redo();
			} catch (CannotUndoException ex) {
			}
		else if (cmd == "runall")
			try {
				String s = editArea.getText();
				if (s.length() > 0)
					JGR.MAINRCONSOLE.execute(s.trim(), true);
			} catch (Exception ex) {
			}
		else if (cmd == "runselection")
			try {
				String s = editArea.getSelectedText().trim();
				if (s.length() > 0)
					JGR.MAINRCONSOLE.execute(s.trim(), true);
			} catch (Exception ex) {
			}
		else if (cmd == "save")
			saveFile();
		else if (cmd == "saveas")
			saveFileAs();
		else if (cmd == "search") 
			//textFinder.showFind(false);
			FindReplaceDialog.findExt(this,editArea);
		else if (cmd == "searchnext")
			//textFinder.showFind(true);
			FindReplaceDialog.findNextExt(this,editArea);
		else if (cmd == "selAll")
			editArea.selectAll();
		else if (cmd == "shiftleft")
			try {
				editArea.shiftSelection(-1);
			} catch (BadLocationException e1) {
			}
		else if (cmd == "shiftright")
			try {
				editArea.shiftSelection(1);
			} catch (BadLocationException e2) {
			}
		else if (cmd == "undo")
			try {
				if (toolBar.undoMgr.canUndo())
					toolBar.undoMgr.undo();
			} catch (CannotUndoException ex) {
			}

	}

	/**
	 * keyTyped: handle key event.
	 */
	public void keyTyped(KeyEvent ke) {
	}

	/**
	 * keyPressed: handle key event: send command to console.
	 */
	public void keyPressed(KeyEvent ke) {
		setModified(modified = true);
		if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
			if ((ke.isControlDown() || ke.isMetaDown())
					&& JGR.MAINRCONSOLE != null
					&& editArea.getSelectedText() != null)
				JGR.MAINRCONSOLE.execute(editArea.getSelectedText(), true);
			else if (editArea.mComplete != null
					&& editArea.mComplete.isVisible())
				editArea.mComplete.completeCommand();
			else if (!(ke.isControlDown() || ke.isMetaDown())) {
				try {
					editArea.replaceSelection("");
				} catch (Exception ex) {
				}
				editArea.insertAt(editArea.getCaretPosition(), "\n");
			}
		} else if (ke.getKeyCode() == KeyEvent.VK_UP) {
			if (editArea.mComplete != null && editArea.mComplete.isVisible())
				editArea.mComplete.selectPrevious();
		} else if (ke.getKeyCode() == KeyEvent.VK_DOWN)
			if (editArea.mComplete != null && editArea.mComplete.isVisible())
				editArea.mComplete.selectNext();
	}

	/**
	 * keyReleased: handle key event.
	 */
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
						int currentline = editArea.getLineOfOffset(editArea
								.getCaretPosition());
						currentpos -= chars;
						setText(currentline
								+ ":"
								+ (currentline == 0 ? currentpos + 1
										: currentpos));
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
			fileName = editor.fileName;
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
				JOptionPane.showMessageDialog(editor, "Permisson denied",
						"File Errror", JOptionPane.OK_OPTION);
			} finally {
				editor.setWorking(false);
			}
		}
	}
}
