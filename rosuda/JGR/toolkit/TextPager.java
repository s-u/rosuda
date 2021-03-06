package org.rosuda.JGR.toolkit;

// JGR - Java Gui for R, see http://www.rosuda.org/JGR/
// Copyright (C) 2003 - 2005 Markus Helbig
// --- for licensing information see LICENSE file in the original JGR
// distribution ---

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.rosuda.ibase.Common;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.ibase.toolkit.TJFrame;

/**
 * Simple text pager that displays a specified file.
 * 
 * @author Simon Urbanek and Markus Helbig RoSuDa 2003 - 2005
 */
public class TextPager extends TJFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2204127122909644542L;

	JTextArea t = new JTextArea();;

	TextFinder textFinder = new TextFinder(t);

	public TextPager(String file, String header, String title, boolean deleteFile) {
		super(title, clsHelp);

		String myMenu[] = { "+", "Edit", "@CCopy", "copy", "-", "@FFind", "search", "@GFind next", "searchnext", "~Window", "0" };
		EzMenuSwing.getEzMenu(this, this, myMenu);

		getContentPane().add(new JScrollPane(t));
		t.setEditable(false);
		t.setFont(new Font("Monospaced", Font.PLAIN, 10));
		t.setDragEnabled(true);
		FontTracker.current.add(t);
		t.setBackground(Color.white);
		setSize(400, 600);
		try {
			BufferedReader r = new BufferedReader(new FileReader(file));
			while (r.ready()) {
				t.append(r.readLine());
				t.append("\n");
			}
			r.close();
			r = null;
			if (deleteFile)
				new File(file).delete();
		} catch (Exception e) {
			t.append("Unable to open file \"" + file + "\": " + e.getMessage());
		}
		addWindowListener(Common.getDefaultWindowListener());
		setVisible(true);
	}

	/**
	 * actionPeformed: handle action events: menu;
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd == "copy")
			t.copy();
		else if (cmd == "search")
			textFinder.showFind(false);
		else if (cmd == "searchnext")
			textFinder.showFind(true);

	}

	/**
	 * Launch textpager.
	 * 
	 * @param file
	 *            file to show
	 * @param header
	 *            header
	 * @param title
	 *            title for pager
	 * @param deleteFile
	 *            delete file after displaying it
	 */
	public static void launchPager(String file, String header, String title, boolean deleteFile) {
		new TextPager(file, header, title, deleteFile);
	}
}
