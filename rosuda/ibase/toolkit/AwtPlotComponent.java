package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class AwtPlotComponent implements PlotComponent {

	private PlotCanvas comp;
	public Graphics gc;
	protected LayerCanvas lcanv;
	
	public AwtPlotComponent() {
		comp = new PlotCanvas();
		comp.gc = comp.getGraphics(); // returns null
		gc = comp.gc;
	}
	
	// do not use this method
	public Component getComponent() {
		return comp;
	}
	
	public void initializeGraphics(Window w) {
		comp.gc = comp.getGraphics();
		gc = comp.gc;
		System.out.println("gc " + gc);
	}
	
	public void initializeLayerCanvas(LayerCanvas l) {
		comp.lcanv = l;
		lcanv = comp.lcanv;
	}

	public int getGraphicsEngine() {
		return AWT;
	}
	
	public PlotComponent getAssociatedPlotComponent() {
		return new AwtPlotComponent();
	}
	
	// redirected methods
	public void repaint() {
		comp.repaint();
	}
	public void setCursor(Cursor cursor) {
		comp.setCursor(cursor);
	}
	public void setBackground(Color c) {
		comp.setBackground(c);
	}
	public void addMouseMotionListener(MouseMotionListener l) {
		comp.addMouseMotionListener(l);
	}
	public void addMouseListener(MouseListener l) {
		comp.addMouseListener(l);
	}
	public void addKeyListener(KeyListener l) {
		comp.addKeyListener(l);
	}
	public Rectangle getBounds(){
		return comp.getBounds();
	}
	public Point getLocation() {
		return comp.getLocation();
	}
	public void setSize(int width, int height) {
		comp.setSize(width,height);
	}
	public void setSize(Dimension d) {
		comp.setSize(d);
	}
	public Dimension getSize() {
		return comp.getSize();
	}
	public Image createImage(int width, int height) {
		return comp.createImage(width,height);
	}
	public Color getForeground() {
		return comp.getForeground();
	}
	public Graphics getGraphics() {
		return comp.getGraphics();
	}
	
	// additions to SWING: do nothing in AWT
	public void setPreferredSize(Dimension d) {}
	public void setMinimumSize(Dimension d) {}
	public void setMaximumSize(Dimension d) {}
	public void setToolTipText(String s) {}
}
