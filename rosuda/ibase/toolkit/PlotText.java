//
//  PlotText.java
//  Klimt
//
//  Created by Simon Urbanek on Fri May 09 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$

/** PlotText implements an array of text labels, specified by x,y,ax,ay and the text itself.
@version $Id$
*/

package org.rosuda.ibase.toolkit;

import org.rosuda.pograss.*;

public class PlotText extends PlotObject {
    double x[], y[], ax[], ay[];
    String txt[];

    public PlotText(PlotManager p) { super(p);}

    public void set(double X[], double Y[], double aX[], double aY[], String text[]) {
        ax=aX; ay=aY; x=X; y=Y; txt=text;
    }
    public void set(double X[], double Y[], String text[]) {
        x=X; y=Y; txt=text;
    }
    public void set(double X[], double Y[]) { x=X; y=Y; }
    public void set(String text[]) { txt=text; }

    public double []getX() { return x; }
    public double []getY() { return y; }
    public double []getAX() { return ax; }
    public double []getAY() { return ay; }
    public String []getText() { return txt; }
    
    // fall-back versions for scalar arguments
    public void set(double X, double Y, double aX, double aY, String text) {
        ax=new double[1]; ax[0]=aX; ay=new double[1]; ay[0]=aY;
        x=new double[1]; x[0]=X; y=new double[1]; y[0]=Y; txt=new String[1]; txt[0]=text;
    }
    public void set(double X, double Y, String text) {
        x=new double[1]; x[0]=X; y=new double[1]; y[0]=Y; txt=new String[1]; txt[0]=text;
    }
    public void set(double X, double Y) { x=new double[1]; x[0]=X; y=new double[1]; y[0]=Y; }
    public void set(String text) { txt=new String[1]; txt[0]=text; }

    /** The actual draw method. It moves in the arrays in a circular fashion and uses the maximal length of the arrays x,y and txt. */
    public void draw(PoGraSS g) {
        if (txt==null || x==null || y==null) return;
        if (cold!=null) cold.use(g);
        int i=0;
        int l=txt.length; if (x.length>l) l=x.length; if (y.length>l) l=y.length;
        int xc=0, yc=0, tc=0, axc=0, ayc=0; // we need separate counters to circulate in non-full arrays
        while (i<l) {
            if (ax==null || ay==null || ax.length==0 || ay.length==0)
                g.drawString(txt[tc++],getXPos(x[xc++]),getYPos(y[yc++]));
            else {
                g.drawString(txt[tc++],getXPos(x[xc++]),getYPos(y[yc++]),ax[axc++],ay[ayc++]);
                if (axc>=ax.length) axc=0;
                if (ayc>=ay.length) ayc=0;
            }                
            i++;
            if (tc>=txt.length) tc=0;
            if (xc>=x.length) xc=0;
            if (yc>=y.length) yc=0;
        }
    }

    public String toString() {
        int l=txt.length; if (x.length>l) l=x.length; if (y.length>l) l=y.length;
        return "PlotText(labels="+l+",coord="+coordX+"/"+coordY+",visible="+isVisible()+")";
    }
}
