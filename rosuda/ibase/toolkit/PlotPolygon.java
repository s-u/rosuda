//
//  PlotPolygon.java
//  Klimt
//
//  Created by Simon Urbanek on Sun Mar 23 2003.
//  Copyright (c) 2003 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.ibase.toolkit;

import org.rosuda.pograss.*;
import org.rosuda.util.*;

import java.util.Vector;
import java.util.Enumeration;

/**
Polygon object to be used in plots
 
@version $Id$
 */
public class PlotPolygon extends PlotObject {
    /** data coordinates of the points */
    double x[], y[];
    /** geometrical coordinates of the points. this field is updated/created by {@link #recalc} */
    int dx[], dy[];
	/** set to <code>true</code> if there are NaN coordinates (non-contiguous lines) */
	boolean hasNaNs;
	/** vector holding polygons if there is more than one */
	Vector polys;
	
    /** create a new polygon object and add it to the specified {@link PlotManager} */
    public PlotPolygon(final PlotManager pm) {
        super(pm);
		polys=new Vector();
        setDrawColor(new PlotColor("black"));
    }

    /** set polygon points. note that the polygon is not closed automatically when drawing outline, but filled are is always closed (if fill color is not <code>null</code>)
        @param xx X coordinates of the points
        @param yy Y coordinates of the points
        */
    public void set(final double[] xx, final double[] yy) {
        if (Global.DEBUG>0)
            System.out.println("["+toString()+"] set(x[],y[]): "+xx+"/"+yy);
        x=xx; y=yy; recalc();
    }

    /* recalculate point transformations between coordinate systems */
    public void recalc() {
        final int l;
        if (x==null || y==null) return;
        l=(x.length>y.length)?y.length:x.length;
        if (dx==null || dy==null || dx.length!=l || dy.length!=l) {
            dx=new int[l]; dy=new int[l];
        }
		polys.removeAllElements();
		hasNaNs=false;
        int i = 0, lb = 0;
        while (i<l) {
			if (Double.isNaN(x[i]) || Double.isNaN(y[i])) {
				if (i-lb>1) {
					int rdx[] = new int[i-lb];
					int rdy[] = new int[i-lb];
					System.arraycopy(dx,lb,rdx,0,i-lb);
					System.arraycopy(dy,lb,rdy,0,i-lb);
					polys.addElement(rdx);
					polys.addElement(rdy);
					lb=i+1;
				}
				dx[i]=INaN;
				dy[i]=INaN;
				hasNaNs=true;
			} else {
				dx[i]=getXPos(x[i]);
				dy[i]=getYPos(y[i]);
			}
            i++;
        }
		if (hasNaNs && i-lb>0) {
			int rdx[] = new int[i-lb];
			int rdy[] = new int[i-lb];
			System.arraycopy(dx,lb,rdx,0,i-lb);
			System.arraycopy(dy,lb,rdy,0,i-lb);
			polys.addElement(rdx);
			polys.addElement(rdy);
		}			
    }

    /** draw the polygon */
    public void draw(final PoGraSS g) {
        if (dx==null || dy==null || dx.length<1) return;
        recalc(); // we should be more intelligent here and recalc only if necessary ...
        if (colf!=null) {
            colf.use(g);
			if (hasNaNs) {
				for (Enumeration e = polys.elements() ; e.hasMoreElements() ;) {
					int cx[] = (int[])e.nextElement();
					int cy[] = (int[])e.nextElement();					
					g.fillPolygon(cx,cy,cx.length);
				}
			} else
				g.fillPolygon(dx,dy,dx.length);
        }
        if (cold!=null) {
            cold.use(g);
			if (hasNaNs) {
				for (Enumeration e = polys.elements() ; e.hasMoreElements() ;) {
					int cx[] = (int[])e.nextElement();
					int cy[] = (int[])e.nextElement();					
					g.drawPolyline(cx,cy,cx.length);
				}
			} else
				g.drawPolyline(dx,dy,dx.length);
        }
    }

    // accessor methods
    public int[] getDX() { return dx; }
    public int[] getDY() { return dy; }
    public double[] getX() { return x; }
    public double[] getY() { return y; }

    public String toString() {
	return "PlotPolygon(coord="+coordX+":"+coordY+
	",dc="+((cold==null)?"none":cold.toString())+
        ",fc="+((colf==null)?"none":colf.toString())+",points="+((dx==null)?"none":(""+dx.length))+",visible="+visible+")";
    }
}
