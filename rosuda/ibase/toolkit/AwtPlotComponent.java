package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import net.java.games.jogl.GLEventListener;
import org.rosuda.pograss.*;

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
	
	public void initializeGraphics(final Window w) {
		comp.gc = comp.getGraphics();
		gc = comp.gc;
//		System.out.println("gc " + gc);
	}
	
	public void initializeLayerCanvas(final LayerCanvas l) {
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
	public void paint(PoGraSS p) {
		comp.paint(((PoGraSSgraphics)p).g);
	}
	public void update(PoGraSS p) {
		comp.update(((PoGraSSgraphics)p).g);
	}
	public void setCursor(Cursor cursor) {
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
		//System.out.println("AwtPlotComponent["+this+"].getParentWindow().comp="+comp);
		Container p = getParent();
		//System.out.println("  container: "+p);
		while (p!=null && !(p instanceof Window)) {
			//System.out.println("  container: "+p);
			p=p.getParent();
		}
//		System.out.println("-->"+p);
		return (Window)p;
	}

	// additions to SWING: do nothing in AWT
	public void setPreferredSize(final Dimension d) {}
	public void setMinimumSize(final Dimension d) {}
	public void setMaximumSize(final Dimension d) {}
	public void setToolTipText(final String s) {}
	
	// additions to JOGL:
	public void addGLEventListener(GLEventListener l) {}
}