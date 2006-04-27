package org.rosuda.ibase.toolkit;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.rosuda.ibase.SVarSet;
import org.rosuda.pograss.*;

/** PlotComponent units graphic-, mouse- and keyboard interactivity
 * @version $Id$
 */

public abstract class PlotComponent {
	
	public final static int AWTGrDevID = 0;
	public final static int SWINGGrDevID = 1;
	public final static int JOGLGrDevID = 2;
	
	public final int GrDevID;
	
	private GraphicsDevice grdev;
	public int layers;
	
	/**
	 * @param gd 0 == AWT, 1 == SWING, 2 == JOGL
	 * @param layers
	 */
	public PlotComponent(int gd, int _layers) {
		GrDevID=gd;
		layers=_layers;
		grdev=null;
		if(gd==0) {
			grdev = new AWTGraphicsDevice(layers);
		} else if(gd==1) {
			grdev = new SWINGGraphicsDevice(layers);
		} else if(gd==2) {
			/* this is a rather generic approach and we should probably use it instead of the silly fixed integers - that would allow arbitrary graphics devices loaded even at run time by class name */
			try {
				Class cl = Class.forName("org.rosuda.ibase.toolkit.JOGLGraphicsDevice");
				if (cl!=null) {
					Constructor con = cl.getConstructor(new Class[] { Integer.TYPE, Boolean.TYPE });
					if (con != null) {
						grdev = (GraphicsDevice) con.newInstance(new Object[] { new Integer(_layers), new Boolean(true) });
					}
				}
			} catch (Exception e) {};
		}
		if (grdev==null) { // AWT is the fall-back
			grdev = new AWTGraphicsDevice(layers);
		}
		grdev.setPCOwner(this);
	}
	
	public PlotComponent(int gd) {
		this(gd,1);
	}
	
	// don't really need this for grdev
	public void paintLayer(int layers) {
		grdev.paintLayer(layers);
	}
	
	public abstract void paintPoGraSS(PoGraSS p);
	
	protected abstract void beginPaint(PoGraSS p);
	
	protected abstract void endPaint(PoGraSS p);
	
	public void repaint() {
//		System.out.println("PlotComponent: repaint()");
		grdev.repaint();
	}
	
	public void setUpdateRoot(int root) {
		grdev.setUpdateRoot(root);
	}
	
	public QueryPopup newQueryPopup(final Window win, final SVarSet vs, final String ct, final int w, final int cid) {
		if(GrDevID==SWINGGrDevID) {
			return new SwingQueryPopup((SWINGGraphicsDevice)grdev,win,vs,ct,w,cid);
		} else if(GrDevID==JOGLGrDevID) {
			return new AwtQueryPopup(win,vs,ct,w,cid);
		} else { // AWTGrDevID is default
			return new AwtQueryPopup(win,vs,ct,w,cid);
		}
	}
	
	public QueryPopup newQueryPopup(final Window win, final SVarSet vs, final String ct) {
		return newQueryPopup(win, vs, ct, -1, -1);
	}
	
	// for SWING, make it accessable for every GraphicsDevice
	public void setToolTipText(String s) {
		if(GrDevID==SWINGGrDevID) {
			((PlotJPanel)(grdev.getComponent())).setToolTipText(s);
		}
	}

		
	// following lines are references to component to draw on
	public Component getComponent() {return grdev.getComponent();} // try to make it not public
	public Rectangle getBounds() {return grdev.getBounds();}
	public void setSize(int w, int h) {grdev.setSize(w,h);}
	public Dimension getSize() {return grdev.getSize();}
	public void setBackground(Color c) {grdev.setBackground(c);}
	public void addMouseListener(MouseListener l) {grdev.addMouseListener(l);}
	public void addMouseMotionListener(MouseMotionListener l) {grdev.addMouseMotionListener(l);}
	public void addKeyListener(KeyListener l) {grdev.addKeyListener(l);}
	public Point getLocation() {return grdev.getLocation();}
	public void setCursor(Cursor c) {grdev.setCursor(c);}
	public void setSize(Dimension d) {grdev.setSize(d);}
	public int getWidth() {return grdev.getWidth();}
	public int getHeight() {return grdev.getHeight();}
	public Container getParent() {return grdev.getParent();}
	
}