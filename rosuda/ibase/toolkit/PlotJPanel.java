package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PlotJPanel extends JPanel implements MouseListener, MouseMotionListener {

	GraphicsDevice grdev;
	
	public PlotJPanel(GraphicsDevice gd) {
		grdev=gd;
	}

	public void update(Graphics g) {
		if(grdev.getGrDevID()==0) ((AWTGraphicsDevice)grdev).update(g);
		else if(grdev.getGrDevID()==1) ((SWINGGraphicsDevice)grdev).update(g);
//		else if(grdev.getGrDevID()==2) ((JOGLGraphicsDevice)grdev).update(g);
		//default is Canvas.update(g);
	}
	
	public void paint(Graphics g) {
		if(grdev.getGrDevID()==0) ((AWTGraphicsDevice)grdev).paint(g);
		else if(grdev.getGrDevID()==1) ((SWINGGraphicsDevice)grdev).paint(g);
//		else if(grdev.getGrDevID()==2) ((JOGLGraphicsDevice)grdev).paint(g);
		//default is Canvas.paint(g);
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
