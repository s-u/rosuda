package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;

public class PlotCanvas extends Canvas implements MouseListener, MouseMotionListener {

	GraphicsDevice grdev;
	
	public PlotCanvas(GraphicsDevice gd) {
		grdev=gd;
	}

	public void update(Graphics g) {
	    grdev.update(g);
	}
	
	public void paint(Graphics g) {
	    grdev.paint(g);
	}
	
	public void mouseClicked(final MouseEvent e) {
		
	}
	public void mouseEntered(final MouseEvent e) {
		
	}
	public void mouseExited(final MouseEvent e) {
		
	}
	public void mousePressed(final MouseEvent e) {
		
	}
	public void mouseReleased(final MouseEvent e) {
		
	}
	public void mouseMoved(final MouseEvent e) {
		
	}
	public void mouseDragged(final MouseEvent e) {
		
	}
}
