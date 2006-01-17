package org.rosuda.ibase.toolkit;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.*;

public class PlotJPanel extends JPanel {

	LayerCanvas lcanv;
	Graphics gc;
	
	public PlotJPanel() {
		
	}
	
	public void paint(final Graphics g) {
		if(lcanv==null) super.paint(g);
		else lcanv.paint(g);
	}
	
	public void update(final Graphics g) {
		if(lcanv==null) super.update(g);
		else lcanv.update(g);
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
