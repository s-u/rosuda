//
//  PPrimRectangle.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Oct 29 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.util.Arrays;

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
        int shift=0;
        boolean hasAny=false;
        
        double totalProp=0;
        double[] props = new double[m.getMaxMark()+2];
        for(int i=-1; i<=m.getMaxMark(); i++){
            props[i+1] = getMarkedProportion(m,i);
            totalProp += props[i+1];
        }
        if(totalProp>=0.0000001){
            int[] pieces = roundProportions(props,totalProp,((orientation&1)==0)?rH:rW);
            for(int i=-1; i<=m.getMaxMark(); i++){
                if (props[i+1]>0d) {
                    hasAny=true;
                    if (orientation==0) { // bottom-up
                        rH=pieces[i+1];
                        rY=r.y+r.height-shift-pieces[i+1];
                    } else if (orientation==2) { // top-down
                        rH=pieces[i+1];
                        rY=r.y+shift;
                    } else if (orientation==1) { // left-right
                        rW=pieces[i+1];
                        rX=r.x+shift;
                    } else if (orientation==3) { // right-left
                        rW=pieces[i+1];
                        rX=r.x+r.width-shift-pieces[i+1];
                    }
                    shift+=pieces[i+1];
                    if (i==-1)
                        g.setColor(COL_MARKED);
                    else
                        g.setColor(ColorBridge.getMain().getColor(i));
                    g.fillRect(rX,rY,rW,rH);
                    g.setColor(COL_OUTLINE);
                    if (drawSelectionBorder)
                        g.drawRect(rX,rY,rW,rH);
                }
                if (i==-1 && m.getSecCount()<1) break;
            }
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
