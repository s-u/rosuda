//
//  PlotText.java
//  Klimt
//
//  Created by Simon Urbanek on Fri May 09 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$

/** PlotText implements an array of text labels, specified by x,y,ax,ay and the text itself.
 * @version $Id$
 */

package org.rosuda.ibase.toolkit;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.rosuda.pograss.*;
import org.rosuda.ibase.*;

public class PlotText extends PlotObject {
    int dx[], dy[], maxw[], maxh[];
    double ax[], ay[], x[], y[];
    double rot[];
    String txt[];
    //Rectangle txtFields[]; // the place which each text element occupies, is initialised in draw method
    boolean show=true;
    
    public PlotText(final PlotManager p) { super(p);}
    
    /* Clears all arrays */
    public void clear(){
        x=y=null;
		dx=dy=null;
        ax=ay=null;
        rot=null;
        maxw=null;
        maxh=null;
        txt=null;
    }
    
    /** The actual draw method. */
    public void draw(final PoGraSS g) {
        if (!show || txt==null || txt.length==0) return;
        if (cold!=null) cold.use(g);
        //if(txtFields==null || txtFields.length!=txt.length) txtFields=new Rectangle[txt.length];
        for (int i=0; i<txt.length; i++) {
            if(txt[i]!=null){
                final double normalizedRot;
				double roti = 0.0;
				if (rot != null && rot.length>0) roti=rot[i % rot.length];
                if (roti<0) normalizedRot = roti-((int)(roti/360+1))*360;
                else normalizedRot = roti-((int)(roti/360))*360;
                
                if(normalizedRot<0.000001 || normalizedRot>359.999999){
                    final String t;
                    
                    if(maxw!=null && maxw[i]>=0 && g.getWidthEstimate(txt[i])>maxw[i]) t=Common.getTriGraph(txt[i]);
                    else t=txt[i];
                    
                    g.setColor(Color.BLACK);
					double axi = 0.0;
					double ayi = 0.0;
					if (ax != null && ax.length>0) axi = ax[i % ax.length];
					if (ay != null && ay.length>0) ayi = ay[i % ay.length];
                    g.drawString(t,dx[i],dy[i],axi,ayi);
                } else{
                    final double rotRad = normalizedRot*Math.PI/180;
                    final double s = Math.sin(rotRad);
                    final double c = Math.cos(rotRad);
                    String t;
                    
                    final int w = g.getWidthEstimate(txt[i]);
                    final int h = g.getHeightEstimate(txt[i]);
                    final int bbw = (int)Math.ceil(h*Math.abs(s) + w*Math.abs(c));
                    final int bbh = (int)Math.ceil(w*Math.abs(s) + h*Math.abs(c));
                    
                    boolean abbreviate= (maxw!=null && maxw[i]>=0 && bbw>maxw[i]);
                    
                    
                    if(!abbreviate && maxh[i]>=0 && bbh>maxh[i])
                        abbreviate=true;
                    
                    if(abbreviate) t=Common.getTriGraph(txt[i]);
                    else t=txt[i];
                    g.setColor(Color.BLACK);
					double axi = 0.0;
					double ayi = 0.0;
					if (ax != null && ax.length>0) axi = ax[i % ax.length];
					if (ay != null && ay.length>0) ayi = ay[i % ay.length];
                    g.drawString(t,dx[i],dy[i],axi,ayi,roti);
                }
            }
        }
    }
    
	public void set(double x, double y) {
		this.x=new double[1]; this.x[0]=x;
		this.y=new double[1]; this.y[0]=y;
		recalc();
	}
	
	public void set(double[] x, double[] y) {
		if (x==null) this.x=null; else {
			this.x=new double[x.length]; System.arraycopy(x, 0, this.x, 0, x.length);
		}
		if (y==null) this.y=null; else {
			this.y=new double[y.length]; System.arraycopy(y, 0, this.y, 0, y.length);
		}
		recalc();
	}

	public double[] getX() { return x; }
	public double[] getY() { return y; }
	public double[] getAX() { return ax; }
	public double[] getAY() { return ay; }
	public int[] getDX() { return dx; }
	public int[] getDY() { return dy; }

	
	public void set(String txt) {
		this.txt = new String[1]; this.txt[0] = txt;
	}
	
	public void set(String[] txt) {
		this.txt = new String[txt.length]; System.arraycopy(txt, 0, this.txt, 0, txt.length);
	}
	
	public void setAX(double ax) {
		this.ax = new double[1]; this.ax[0] = ax;
	}
	
	public void setAX(double[] ax) {
		this.ax=new double[ax.length]; System.arraycopy(ax, 0, this.ax, 0, ax.length);
	}
	
	public void setAY(double ay) {
		this.ay = new double[1]; this.ay[0] = ay;
	}
	
	public void setAY(double[] ay) {
		this.ay=new double[ay.length]; System.arraycopy(ay, 0, this.ay, 0, ay.length);
	}
	
    public String toString() {
        if (txt==null) return "PlotText(<no text>)";
        if (x==null || y==null) return "PlotText(<coordinates incomplete>)";
        int l=txt.length; if (x.length>l) l=x.length; if (y.length>l) l=y.length;
        return "PlotText(labels="+l+",coord="+coordX+"/"+coordY+",visible="+isVisible()+")";
    }

	/* recalculate point transformations between coordinate systems */
    public void recalc() {
        final int l;
        if (x==null || y==null) return;
        l=(x.length>y.length)?y.length:x.length;
        if (dx==null || dy==null || dx.length!=l || dy.length!=l) {
            dx=new int[l]; dy=new int[l];
        }
        int i = 0;
        while (i<l) {
			dx[i]=getXPos(x[i % x.length]);
			dy[i]=getYPos(y[i % y.length]);
            i++;
        }
    }
	
    int getMaxBoundingBoxWidth(PoGraSS g) {
        int maxBbw = 0;
        if(txt != null) for (int i=0; i<txt.length; i++) {
            if(txt[i]!=null){
				double roti = 0.0;
				if (rot != null && rot.length>0) roti=rot[i % rot.length];
                final double rotRad = roti*Math.PI/180;
                final double s = Math.sin(rotRad);
                final double c = Math.cos(rotRad);
                String t;
                
                final int w = g.getWidthEstimate(txt[i]);
                final int h = g.getHeightEstimate(txt[i]);
                final int bbw = (int)Math.ceil(h*Math.abs(s) + w*Math.abs(c));
                
                maxBbw = Math.max(maxBbw,bbw);
            }
        }
        return maxBbw;
    }
}
