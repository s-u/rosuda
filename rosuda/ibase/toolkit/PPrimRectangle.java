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
		int rX=r.x,rY=r.y,rW=r.width,rH=r.height;
		double totW=(double)rW, totH=(double)rH;
		int mark=-1;
		double shift=0d;
		boolean hasAny=false;
		
		while (mark<=m.getMaxMark()) {
			double sa=getMarkedProportion(m,mark);
			//System.out.println("pp["+i+"] sa="+sa+" "+pp);
			if (sa>0d) {
				hasAny=true;
				if (orientation==0) { // bottom-up
					rH=(int)(totH*sa);
					rY=r.y+(int)(totH-totH*shift)-rH;
				} else if (orientation==2) { // top-down
					rH=(int)(totH*sa);
					rY=r.y+(int)(totH*shift);
				} else if (orientation==1) { // left-right
					rW=(int)(totW*sa);
					rX=r.x+(int)(totW*shift);					
				} else if (orientation==3) { // right-left
					rW=(int)(totW*sa);
					rX=r.x+(int)(totW-totW*shift)-rW;
				}
				shift+=sa;
				if (mark==-1)
					g.setColor("marked");
				else
					g.setColor(ColorBridge.getMain().getColor(mark));
				g.fillRect(rX,rY,rW,rH);
				g.setColor("outline");
				if (drawSelectionBorder)
					g.drawRect(rX,rY,rW,rH);
			}
			if (mark==-1 && m.getSecCount()<1) break;
			mark++;			
        }
		if (hasAny) {
			g.setColor("outline");
			g.drawRect(r.x,r.y,r.width,r.height);
		}
    }

    public String toString() {
        return "PPrimRectangle("+((r==null)?"<null rectangle>":(""+r.x+":"+r.y+","+r.width+":"+r.height))
        +", cases="+cases()+", drawBorder="+drawBorder+", drawSelBorder="+drawSelectionBorder+")";
    }
}
