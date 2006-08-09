package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.WTentry;
import org.rosuda.ibase.toolkit.WinTracker;

/**
 * iMenu - implementation of an menu build form an String array.
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2005
 * 
 */

public class JGRMenu {

	/**
	 * Get JMenuBar build from menuDef.
	 * 
	 * @param f
	 *            frame where menubar should be added
	 * @param al
	 *            ActionListener which cares about menu-actions
	 * @param menuDef
	 *            menuitem list
	 * @return menubar
	 */
	public static JMenuBar getMenu(JFrame f, ActionListener al, String[] menuDef) {
		WinTracker wt = WinTracker.current;
		WTentry we = (wt == null) ? null : wt.getEntry(f);
		JMenuBar mb = f.getJMenuBar();
		if (mb == null)
			mb = new JMenuBar();
		JMenu m = null;
		int i = 0;
		boolean lastSep = false;
		while (menuDef[i] != "0") {
			JMenuItem mi;
			boolean isNext = false;
			if (menuDef[i] == "0")
				break;
			if (menuDef[i] == "~File.Quit") {
				i++;
				isNext = true;
				if (!Common.isMac()) {
					m.addSeparator();
					mi = new JMenuItem("Quit");
					mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('Q',
							Toolkit.getDefaultToolkit()
									.getMenuShortcutKeyMask(), false));
					mi.setActionCommand("exit");
					mi.addActionListener(al);
					m.add(mi);
				}
			}
			if (menuDef[i] == "~File.Basic.End") {
				i++;
				isNext = true;
				if (Common.isMac()) {
					mi = new JMenuItem("Close window");
					mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('W',
							Toolkit.getDefaultToolkit()
									.getMenuShortcutKeyMask(), false));
					// mi.setActionCommand("WTMclose"+we.id);
					mi.setActionCommand("exit");
					mi.addActionListener(al);
					m.add(mi);
				}
				if (!Common.isMac()) {
					m.addSeparator();
					mi = new JMenuItem("Exit");
					mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('Q',
							Toolkit.getDefaultToolkit()
									.getMenuShortcutKeyMask(), false));
					mi.setActionCommand("exit");
					mi.setMnemonic('x');
					mi.addActionListener(al);
					m.add(mi);
				}
			}
			if (menuDef[i].indexOf("~Edit") >= 0) {
				i++;
				isNext = true;
				mb.add(m = new JMenu("Edit"));
				m.setMnemonic('E');
				mi = new JMenuItem("Undo");
				mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('Z',
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
						false));
				mi.setActionCommand("undo");
				mi.setMnemonic('U');
				mi.addActionListener(al);
				m.add(mi);
				mi = new JMenuItem("Redo");
				mi.setAccelerator(javax.swing.KeyStroke
						.getKeyStroke('Z', Toolkit.getDefaultToolkit()
								.getMenuShortcutKeyMask() + 1, false));
				mi.setActionCommand("redo");
				mi.setMnemonic('R');
				mi.addActionListener(al);
				m.add(mi);
				m.addSeparator();
				mi = new JMenuItem("Cut");
				mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('X',
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
						false));
				mi.setActionCommand("cut");
				mi.setMnemonic('t');
				mi.addActionListener(al);
				m.add(mi);
				mi = new JMenuItem("Copy");
				mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('C',
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
						false));
				mi.setActionCommand("copy");
				mi.setMnemonic('C');
				mi.addActionListener(al);
				m.add(mi);
				if (menuDef[i - 1].equals("~EditC")) {
					JMenu m2 = new JMenu("Copy Special");
					JMenuItem mi21 = new JMenuItem("Copy Output");
					mi21.setActionCommand("copyoutput");
					mi21.addActionListener(al);
					m2.add(mi21);
					JMenuItem mi22 = new JMenuItem("Copy Commands");
					mi22.setActionCommand("copycmds");
					mi22.addActionListener(al);
					m2.add(mi22);
					JMenuItem mi23 = new JMenuItem("Copy Results");
					mi23.setActionCommand("copyresult");
					mi23.addActionListener(al);
					m2.add(mi23);
					m.add(m2);
				}
				mi = new JMenuItem("Paste");
				mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('V',
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
						false));
				mi.setActionCommand("paste");
				mi.setMnemonic('P');
				mi.addActionListener(al);
				m.add(mi);
				mi = new JMenuItem("Delete");
				mi.setActionCommand("delete");
				mi.setMnemonic('D');
				mi.addActionListener(al);
				m.add(mi);
				mi = new JMenuItem("Select All");
				mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('A',
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
						false));
				mi.setActionCommand("selAll");
				mi.setMnemonic('A');
				mi.addActionListener(al);
				m.add(mi);
				m.addSeparator();
				if (menuDef[i - 1].equals("~Editor")) {
					mi = new JMenuItem("Comment Code");
					mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('K',
							Toolkit.getDefaultToolkit()
									.getMenuShortcutKeyMask(), false));
					mi.setActionCommand("commentcode");
					mi.addActionListener(al);
					m.add(mi);
					mi = new JMenuItem("Uncomment Code");
					mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('K',
							Toolkit.getDefaultToolkit()
									.getMenuShortcutKeyMask() + 1, false));
					mi.setActionCommand("uncommentcode");
					mi.addActionListener(al);
					m.add(mi);

					mi = new JMenuItem("Shift Right");
					mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('R',
							Toolkit.getDefaultToolkit()
									.getMenuShortcutKeyMask() + 1, false));
					mi.setActionCommand("shiftright");
					mi.setMnemonic('S');
					mi.addActionListener(al);
					m.add(mi);
					mi = new JMenuItem("Shift Left");
					mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('L',
							Toolkit.getDefaultToolkit()
									.getMenuShortcutKeyMask() + 1, false));
					mi.setActionCommand("shiftleft");
					mi.setMnemonic('h');
					mi.addActionListener(al);
					m.add(mi);

					m.addSeparator();
					mi = new JMenuItem("Run selection");
					mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('R',
							Toolkit.getDefaultToolkit()
									.getMenuShortcutKeyMask(), false));
					mi.setActionCommand("runselection");
					mi.addActionListener(al);
					m.add(mi);
					mi = new JMenuItem("Run all");
					mi.setActionCommand("runall");
					mi.addActionListener(al);
					m.add(mi);
					m.addSeparator();
				}
				mi = new JMenuItem("Find");
				mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('F',
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
						false));
				mi.setActionCommand("search");
				mi.setMnemonic('F');
				mi.addActionListener(al);
				m.add(mi);
				mi = new JMenuItem("Find Next");
				mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('G',
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(),
						false));
				mi.setActionCommand("searchnext");
				mi.setMnemonic('N');
				mi.addActionListener(al);
				m.add(mi);
				if (menuDef[i - 1].equals("~EditC")) {
					m.addSeparator();
					mi = new JMenuItem("Clear Console");
					mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke('L',
							Toolkit.getDefaultToolkit()
									.getMenuShortcutKeyMask(), false));
					mi.setActionCommand("clearconsole");
					mi.addActionListener(al);
					m.add(mi);
				}
				if (!Common.isMac()) {
					m.addSeparator();
					mi = new JMenuItem("Preferences...");
					mi.setActionCommand("prefs");
					mi.addActionListener(al);
					m.add(mi);
				}
			}
			;
			if (menuDef[i] == "~Window") {
				i++;
				isNext = true;
				if (we != null && we.getWindowMenu() != null)
					mb.add((JMenu) we.getWindowMenu()); // add window menu
			}
			;
			if (menuDef[i] == "~Help") {
				i++;
				isNext = true;
				m = new JMenu("Help");
				m.setMnemonic('H');
				mb.add(m);
				mi = new JMenuItem("R Help");
				mi.setActionCommand("help");
				mi.setMnemonic('H');
				mi.addActionListener(al);
				mi.setAccelerator(KeyStroke
						.getKeyStroke(KeyEvent.VK_HELP,
								Toolkit.getDefaultToolkit()
										.getMenuShortcutKeyMask() + 1, false));
				m.add(mi);
			}
			;
			if (menuDef[i] == "~About") {
				i++;
				isNext = true;
				if (!Common.isMac()) {
					m.addSeparator();
					mi = new JMenuItem("About");
					mi.setMnemonic('A');
					mi.setActionCommand("about");
					mi.addActionListener(al);
					m.add(mi);
				}
			}
			;
			if (menuDef[i] == "~Preferences") {
				i++;
				isNext = true;
				if (!Common.isMac()) {
					m.addSeparator();
					mi = new JMenuItem("Preferences...");
					mi.setActionCommand("prefs");
					mi.addActionListener(al);
					m.add(mi);
				}
			}
			;
			if (menuDef[i] == "+") {
				i++;
				isNext = true;
				m = getMenu(f, menuDef[i]);
				if (m == null) {
					mb.add(m = new JMenu(menuDef[i].substring(menuDef[i]
							.charAt(0) == '$' ? 2 : 0)));
					if (menuDef[i].charAt(0) == '$')
						m.setMnemonic(menuDef[i].charAt(1));
				}
				i++;
			}
			;
			if (isNext)
				lastSep = false;
			if (menuDef[i] == "-") {
				if (!lastSep)
					m.addSeparator();
				lastSep = true;
				i++;
				isNext = true;
			}
			;
			if (!isNext) {
				String rac = menuDef[i + 1];
				if (rac == "WTMclose")
					rac = "WTMclose" + we.id;
				mi = getItem(f, rac);
				if (mi == null) {
					if (menuDef[i].charAt(0) == '#') {
						m.add(mi = new JMenu(menuDef[i].substring(menuDef[i]
								.charAt(1) == '$' ? 3 : 1)));
						if (menuDef[i].charAt(1) == '$')
							mi.setMnemonic(menuDef[i].charAt(2));
					} else if (menuDef[i].charAt(0) == '@'
							|| menuDef[i].charAt(0) == '!') {
						mi = new JMenuItem(menuDef[i].substring(menuDef[i]
								.charAt(2) == '$' ? 4 : 2));
						mi.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
								menuDef[i].charAt(1), Toolkit
										.getDefaultToolkit()
										.getMenuShortcutKeyMask()
										+ ((menuDef[i].charAt(0) == '!') ? 1
												: 0), false));
						if (menuDef[i].charAt(2) == '$')
							mi.setMnemonic(menuDef[i].charAt(3));
						mi.setActionCommand(rac);
						m.add(mi);
					} else {
						mi = new JMenuItem(menuDef[i].substring(menuDef[i]
								.charAt(0) == '$' ? 2 : 0));
						if (menuDef[i].charAt(0) == '$')
							mi.setMnemonic(menuDef[i].charAt(1));
						mi.setActionCommand(rac);
						m.add(mi);
					}
					mi.addActionListener(al);
					if (menuDef[i + 1] == "WTMclose")
						mi.addActionListener(wt);
					lastSep = false;
				}
				;
				i += 2;
			}
			;
		}
		;

		f.setJMenuBar(mb);
		return mb;
	};

	/**
	 * Add menu to existing menubar.
	 * 
	 * @param f
	 *            frame where to add menu.
	 * @param name
	 *            menuname
	 */
	public static void addMenu(JFrame f, String name) {
		JMenuBar mb = f.getJMenuBar();
		mb.add(new JMenu(name), (mb.getMenuCount() - 2));
	}

	/**
	 * Add menuitem to existing menu.
	 * 
	 * @param f
	 *            frame which contains menu
	 * @param menu
	 *            menu where to add new item
	 * @param name
	 *            name of new item
	 * @param command
	 *            ActionCommand of this new item
	 * @param al
	 *            ActionListener which should be attached to this menuitem
	 */
	public static void addMenuItem(JFrame f, String menu, String name,
			String command, ActionListener al) {
		JMenu m = getMenu(f, menu);
		JMenuItem mi = new JMenuItem(name);
		mi.addActionListener(al);
		mi.setActionCommand(command);
		m.add(mi);
	}

	/**
	 * Add a menuseparator to existing menu.
	 * 
	 * @param f
	 *            frame which contains menu
	 * @param menu
	 *            menu where to ad separator
	 */
	public static void addMenuSeparator(JFrame f, String menu) {
		JMenu m = getMenu(f, menu);
		m.addSeparator();
	}

	/**
	 * Get JMenu to supplied name.
	 * 
	 * @param f
	 *            frame which might contains this menu
	 * @param name
	 *            name of menu
	 * @return JMenu which matches name
	 */
	public static JMenu getMenu(JFrame f, String name) {
		JMenuBar mb = f.getJMenuBar();
		if (mb == null)
			return null;
		int mc = mb.getMenuCount();
		int i = 0;
		while (i < mc) {
			JMenu m = mb.getMenu(i);
			if (m.getText().equals(name))
				return m;
			i++;
		}
		;
		return null;
	};

	/**
	 * Get JMenuItem to supplied name (ActionCommand).
	 * 
	 * @param f
	 *            frame which might contain menuitem
	 * @param name
	 *            name of searched menuitem
	 * @return JMenuItem which equals to name
	 */
	public static JMenuItem getItem(JFrame f, String name) {
		JMenuBar mb = f.getJMenuBar();
		if (mb == null)
			return null;
		int mc = mb.getMenuCount();
		int i = 0;
		while (i < mc) {
			JMenu m = mb.getMenu(i);
			int ic = m.getItemCount();
			int j = 0;
			while (j < ic) {
				JMenuItem mi = m.getItem(j);
				if (mi != null && mi.getActionCommand() == name)
					return mi;
				j++;
			}
			;
			i++;
		}
		;
		return null;
	};

	/**
	 * Get JMenuItem to supplied name (Label).
	 * 
	 * @param f
	 *            frame which might contain menuitem
	 * @param name
	 *            name of searched menuitem
	 * @return JMenuItem which equals to name
	 */
	public static JMenuItem getItemByLabel(JFrame f, String name) {
		JMenuBar mb = f.getJMenuBar();
		if (mb == null)
			return null;
		int mc = mb.getMenuCount();
		int i = 0;
		while (i < mc) {
			JMenu m = mb.getMenu(i);
			int ic = m.getItemCount();
			int j = 0;
			while (j < ic) {
				JMenuItem mi = m.getItem(j);
				if (mi != null && mi.getText().compareTo(name) == 0)
					return mi;
				j++;
			}
			;
			i++;
		}
		;
		return null;
	};
}
