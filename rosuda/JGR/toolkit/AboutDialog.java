package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

import org.rosuda.ibase.Common;

/**
 * AboutDialog - show splashscreen with authors and copyright information
 * 
 * @author Markus Helbig
 * 
 * RoSuDa 2003 - 2005
 */

public class AboutDialog extends SplashScreen implements MouseListener {

	private final Dimension screenSize = Common.getScreenRes();

	public AboutDialog() {
		this(null);
	}

	/**
	 * Create new aboutdialog (splashscreen).
	 * 
	 * @param f
	 *            parent frame
	 */
	public AboutDialog(JFrame f) {
		this.addMouseListener(this);
		this.setVisible(true);
	}

	/**
	 * mouseClicked: handle mouse event: dispose when mouse clicks on splash
	 */
	public void mouseClicked(MouseEvent e) {
		this.dispose();
	}

	/**
	 * mouseEntered: handle mouse event.
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * mousePressed: handle mouse event.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * mouseReleased: handle mouse event.
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * mouseExited: handle mouse event.
	 */
	public void mouseExited(MouseEvent e) {
	}
}