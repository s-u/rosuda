package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;


import net.java.games.jogl.GLEventListener;

public class SwingPlotComponent implements PlotComponent {

	public PlotJPanel comp;
	public Graphics gc;
	protected LayerCanvas lcanv;
	
	public SwingPlotComponent() {
		comp = new PlotJPanel();
		comp.gc = comp.getGraphics(); // returns null
		gc = comp.gc;
	}
	
	public Component getComponent() {
		return comp;
	}
	
	public void initializeGraphics(final Window w) {
		comp.gc = comp.getGraphics();
		gc = comp.gc;
		System.out.println("gc " + gc);
	}
	
	public void initializeLayerCanvas(final LayerCanvas l) {
		comp.lcanv = l;
		lcanv = comp.lcanv;
	}

	public int getGraphicsEngine() {
		return SWING;
	}
	
	public PlotComponent getAssociatedPlotComponent() {
		return new SwingPlotComponent();
	}
	// redirected methods
	public void repaint() {
		comp.repaint();
	}
	public void setCursor(final Cursor cursor) {
		comp.setCursor(cursor);
	}
	public void setBackground(final Color c) {
		comp.setBackground(c);
	}
	public void addMouseMotionListener(final MouseMotionListener l) {
		comp.addMouseMotionListener(l);
	}
	public void addMouseListener(final MouseListener l) {
		comp.addMouseListener(l);
	}
	public void addKeyListener(final KeyListener l) {
		comp.addKeyListener(l);
	}
	public Rectangle getBounds(){
		return comp.getBounds();
	}
	public Point getLocation() {
		return comp.getLocation();
	}
	public void setSize(final int width, final int height) {
		comp.setSize(width,height);
	}
	public void setSize(final Dimension d) {
		comp.setSize(d);
		comp.setPreferredSize(d);
		comp.setMinimumSize(d);
	}
	public Dimension getSize() {
		return comp.getSize();
	}
	public Image createImage(final int width, final int height) {
		return comp.createImage(width,height);
	}
	public Color getForeground() {
		return comp.getForeground();
	}
	public Color getBackground() {
		return comp.getBackground();
	}
	public Graphics getGraphics() {
		return comp.getGraphics();
	}
	public Container getParent() {
		return comp.getParent();
	}
	public int getWidth() {
		return comp.getWidth();
	}
	public int getHeight() {
		return comp.getHeight();
	}
	public Window getParentWindow() {
		if (comp==null) return null;
		Container p = comp.getParent();
		while (p!=null && !(p instanceof Window)) p=p.getParent();
		return (Window)p;
	}
	
	// additions to SWING
	public void setPreferredSize(final Dimension d) {
		comp.setPreferredSize(d);
	}
	public void setMinimumSize(final Dimension d) {
		comp.setMinimumSize(d);
	}
	public void setMaximumSize(final Dimension d) {
		comp.setMaximumSize(d);
	}
	public void setToolTipText(final String s) {
		comp.setToolTipText(s);
	}
	
	// additions to JOGL:
	public void addGLEventListener(GLEventListener l) {}
}