package org.rosuda.ibase.toolkit;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.*;

public class PlotJPanel extends JPanel {

	LayerCanvas lcanv;
	Graphics gc;
	
	public PlotJPanel() {
		
	}
	
	public void paint(Graphics g) {
		if(lcanv==null) super.paint(g);
		else lcanv.paint(g);
	}
	
	public void update(Graphics g) {
		if(lcanv==null) super.update(g);
		else lcanv.update(g);
	}
	public void mouseClicked(MouseEvent e) {
		
	}
	public void mouseEntered(MouseEvent e) {
		
	}
	public void mouseExited(MouseEvent e) {
		
	}
	public void mousePressed(MouseEvent e) {
		
	}
	public void mouseReleased(MouseEvent e) {
		
	}
	public void mouseMoved(MouseEvent e) {
		
	}
	public void mouseDragged(MouseEvent e) {
		
	}
}
