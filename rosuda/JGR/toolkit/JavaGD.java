package org.rosuda.JGR.toolkit;

//
//JavaGD.java
//JRGui
//
//Created by Simon Urbanek on Wed Apr 28 2004.
//Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//$Id$

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.WindowConstants;

import org.rosuda.javaGD.GDInterface;

/** Implementation of {@see JavaGD} which uses iFrame instead of Frame */
public class JavaGD extends GDInterface implements ActionListener,
		WindowListener {
	iFrame jfr;

	static int count = 0;

	/**
	 * Open JavaGD device.
	 */
	public void gdOpen(double w, double h) {
		open = true;
		if (jfr != null)
			gdClose();

		jfr = new iFrame("JavaGD", iFrame.clsJavaGD) {
			public void dispose() {
				if (c != null)
					executeDevOff();
				super.dispose();
			}
		};
		jfr.addWindowListener(this);

		String[] Menu = { "+", "File", "Save as PDF...", "savePDF",
				"Save as EPS...", "saveEPS", "+", "Edit", "@CCopy (as image)",
				"copyImg", "~Window", "0" };
		iMenu.getMenu(jfr, this, Menu);

		jfr.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		c = new org.rosuda.javaGD.JGDBufferedPanel(w, h);
		jfr.getContentPane().add((org.rosuda.javaGD.JGDPanel) c);
		jfr.setSize((int) w, (int) h);
		jfr.pack();
		jfr.setVisible(true);
	}

	/**
	 * Create new page.
	 */
	public void gdNewPage(int devNr) {
		super.gdNewPage(devNr);
		jfr.setTitle("JavaGD (" + (getDeviceNumber() + 1) + ")"
				+ (active ? " *active*" : ""));
	}

	/**
	 * Activate device.
	 */
	public void gdActivate() {
		super.gdActivate();
		jfr.toFront();
		jfr
				.setTitle("JavaGD "
						+ ((getDeviceNumber() > 0) ? ("("
								+ (getDeviceNumber() + 1) + ")") : "")
						+ " *active*");
	}

	/**
	 * Deactivate device.
	 */
	public void gdDeactivate() {
		super.gdDeactivate();
		jfr.setTitle("JavaGD (" + (getDeviceNumber() + 1) + ")");
	}

	/**
	 * Close device.
	 */
	public void gdClose() {
		super.gdClose();
		if (jfr != null) {
			c = null;
			jfr.getContentPane().removeAll();
			jfr.dispose();
			jfr = null;
		}
	}

	String getFileDlg(boolean newFile, String suffix) {
		FileSelector fd = new FileSelector(jfr, (!newFile) ? "Select a file"
				: "Select a new file", (!newFile) ? FileSelector.LOAD
				: FileSelector.SAVE,
				org.rosuda.JGR.toolkit.JGRPrefs.workingDirectory);
		fd.setVisible(true);
		String res = null;
		if (fd.getDirectory() != null && fd.getFile() != null)
			res = fd.getDirectory();
		if (fd.getFile() != null)
			res = (res == null) ? fd.getFile() : (res + fd.getFile());
		return res;
	}

	String escapeStr(String s) {
		int i = 0;
		StringBuffer r = new StringBuffer(s.length() + 16);
		while (i < s.length()) {
			char c = s.charAt(i);
			if (c == '"' || c == '\\')
				r.append("\\");
			r.append(c);
			i++;
		}
		return r.toString();
	}

	/**
	 * actionPerformed: handle action event: menus.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("copyImg"))
			org.rosuda.util.ImageSelection.copyComponent(
					(java.awt.Component) c, false, true);
		if (cmd.equals("savePDF")) {
			String fn = getFileDlg(true, "pdf");
			if (fn != null) {
				fn = escapeStr(fn);
				org.rosuda.JRI.Rengine.getMainEngine().eval(
						".jgr.save.JavaGD.as(useDevice=pdf, source="
								+ (getDeviceNumber() + 1) + ", file=\"" + fn
								+ "\",onefile=TRUE, paper=\"special\")");
			}
		}
		if (cmd.equals("saveEPS")) {
			String fn = getFileDlg(true, "eps");
			if (fn != null) {
				fn = escapeStr(fn);
				org.rosuda.JRI.Rengine.getMainEngine().eval(
						".jgr.save.JavaGD.as(useDevice=postscript, "
								+ (getDeviceNumber() + 1) + ", file=\"" + fn
								+ "\",onefile=TRUE, paper=\"special\")");
			}
		}
	}

	public void windowClosing(WindowEvent e) {
		if (c != null)
			executeDevOff();
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}
}
