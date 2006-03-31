//
//  PPrimRectangle.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Oct 29 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;


public class PPrimRectangle extends PPrimBase {
    static final String COL_OUTLINE = "outline";
    protected String COL_BORDER = COL_OUTLINE;
    public Rectangle r;
    public boolean drawBorder=true;
    public boolean drawSelectionBorder=false;

    /** checks whether the PlotPrimitive contains (or in case of a point primitive equals to) the given point.*/
    public boolean contains(final int x, final int y) { return (r==null)?false:r.contains(x,y); }

    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    public boolean intersects(final Rectangle rt) { return (r==null)?false:r.intersects(rt); }

    /** paint the primitive */
    public void paint(final PoGraSS g, final int orientation) {
        if (r==null) return;
        if (col!=null)
            g.setColor(col.getRed(),col.getGreen(),col.getBlue());
        else
            g.setColor("object");
        g.fillRect(r.x,r.y,r.width,r.height);
        if (drawBorder) {
            g.setColor(COL_OUTLINE);
            g.drawRect(r.x,r.y,r.width,r.height);
        }
    }

    public void paintSelected(final PoGraSS g, final int orientation, final SMarker m) {
        if (r==null) return;
		int rX=r.x,rY=r.y,rW=r.width,rH=r.height;
		final double totW=rW;
        final double totH=rH;
		int mark=-1;
		double shift=0d;
		boolean hasAny=false;
		
		while (mark<=m.getMaxMark()) {
			final double sa=getMarkedProportion(m,mark);
			//System.out.println("pp["+i+"] sa="+sa+" "+pp);
			if (sa>0d) {
				hasAny=true;
				if (orientation==0) { // bottom-up
					rH=(int)(totH*sa+0.5);
					rY=r.y+(int)(totH-totH*shift+0.5)-rH;
				} else if (orientation==2) { // top-down
					rH=(int)(totH*sa+0.5);
					rY=r.y+(int)(totH*shift+0.5);
				} else if (orientation==1) { // left-right
					rW=(int)(totW*sa+0.5);
					rX=r.x+(int)(totW*shift+0.5);					
				} else if (orientation==3) { // right-left
					rW=(int)(totW*sa+0.5);
					rX=r.x+(int)(totW-totW*shift+0.5)-rW;
				}
				shift+=sa;
				if (mark==-1)
					g.setColor(COL_MARKED);
				else
					g.setColor(ColorBridge.getMain().getColor(mark));
				g.fillRect(rX,rY,rW,rH);
				g.setColor(COL_OUTLINE);
				if (drawSelectionBorder)
					g.drawRect(rX,rY,rW,rH);
			}
			if (mark==-1 && m.getSecCount()<1) break;
			mark++;			
        }
		if (hasAny) {
			g.setColor(COL_BORDER);
			g.drawRect(r.x,r.y,r.width,r.height);
		}
    }

    public String toString() {
        return "PPrimRectangle("+((r==null)?"<null rectangle>":(""+r.x+":"+r.y+","+r.width+":"+r.height))
        +", cases="+cases()+", drawBorder="+drawBorder+", drawSelBorder="+drawSelectionBorder+")";
    }
}
