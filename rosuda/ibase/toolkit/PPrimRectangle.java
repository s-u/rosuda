//
//  PPrimRectangle.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Oct 29 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

public class PPrimRectangle extends PPrimBase {
    public Rectangle r;
    public boolean drawBorder=true;
    public boolean drawSelectionBorder=false;
    
    /** checks whether the PlotPrimitive contains (or in case of a point primitive equals to) the given point.*/
    public boolean contains(int x, int y) { return (r==null)?false:r.contains(x,y); }

    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    public boolean intersects(Rectangle rt) { return (r==null)?false:r.intersects(rt); }

    /** paint the primitive */
    public void paint(PoGraSS g, int orientation) {
        if (r==null) return;
        if (col!=null)
            g.setColor(col.getRed(),col.getGreen(),col.getBlue());
        else
            g.setColor("object");
        g.fillRect(r.x,r.y,r.width,r.height);
        if (drawBorder) {
            g.setColor("outline");
            g.drawRect(r.x,r.y,r.width,r.height);
        }
    }

    public void paintSelected(PoGraSS g, int orientation, SMarker m) {
        if (r==null) return;
        double sa=getMarkedProportion(m,-1);
        //System.out.println("pp["+i+"] sa="+sa+" "+pp);
        if (sa>0d) {
            int rX=r.x,rY=r.y,rW=r.width,rH=r.height;
            if (orientation==0) { // bottom-up
                int nrH=(int)(((double)rH)*sa);
                rY+=rH-nrH;
                rH=nrH;
            } else if (orientation==2) { // top-down
                rH=(int)(((double)rH)*sa);
            } else if (orientation==1) { // left-right
                rW=(int)(((double)rW)*sa);
            } else if (orientation==3) { // right-left
                int nrW=(int)(((double)rW)*sa);
                rX+=rW-nrW;
                rW=nrW;
            }
            g.setColor("marked");
            g.fillRect(rX,rY,rW,rH);
            g.setColor("outline");
            if (drawSelectionBorder)
                g.drawRect(rX,rY,rW,rH);
            else
                g.drawRect(r.x,r.y,r.width,r.height);
        }
    }
}
