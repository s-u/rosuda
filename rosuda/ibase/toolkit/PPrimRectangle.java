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
    
    private int[] pieces;
    
    /** checks whether the PlotPrimitive contains (or in case of a point primitive equals to) the given point.*/
    public boolean contains(final int x, final int y) { return (r==null)?false:r.contains(x,y); }
    
    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    public boolean intersects(final Rectangle rt) { return (r==null)?false:r.intersects(rt); }
    
    /** paint the primitive */
    public void paint(final PoGraSS g, final int orientation, final SMarker m) {
        if (r==null) return;
        
        if(m.getSecCount()<1){ // no color brushing
            pieces=null;
            if (col!=null)
                g.setColor(col.getRed(),col.getGreen(),col.getBlue());
            else
                g.setColor("object");
            g.fillRect(r.x,r.y,r.width,r.height);
            if (drawBorder) {
                g.setColor(COL_BORDER);
                g.drawRect(r.x,r.y,r.width,r.height);
            }
        } else{ // color brushing
            int rX=r.x,rY=r.y,rW=r.width,rH=r.height;
            final double totW=rW;
            final double totH=rH;
            int mark=-1;
            int shift=0;
            boolean hasAny=false;
            
            double totalProp=0;
            double[] props = new double[m.getMaxMark()+1];
            for(int i=0; i<=m.getMaxMark(); i++){
                props[i] = getMarkedProportion(m,i);
                totalProp += props[i];
            }
            if(totalProp>=0.0000001){
                pieces = roundProportions(props,totalProp,((orientation&1)==0)?rH:rW);
                for(int i=0; i<=m.getMaxMark(); i++){
                    if (props[i]>0d) {
                        hasAny=true;
                        if (orientation==0) { // bottom-up
                            rH=pieces[i];
                            rY=r.y+r.height-shift-pieces[i];
                        } else if (orientation==2) { // top-down
                            rH=pieces[i];
                            rY=r.y+shift;
                        } else if (orientation==1) { // left-right
                            rW=pieces[i];
                            rX=r.x+shift;
                        } else if (orientation==3) { // right-left
                            rW=pieces[i];
                            rX=r.x+r.width-shift-pieces[i];
                        }
                        shift+=pieces[i];
                        g.setColor(ColorBridge.getMain().getColor(i));
                        g.fillRect(rX,rY,rW,rH);
                    }
                }
            }
            
            if (hasAny) {
                g.setColor(COL_BORDER);
                g.drawRect(r.x,r.y,r.width,r.height);
            }
        }
    }
    
    public void paintSelected(final PoGraSS g, final int orientation, final SMarker m) {
        if (r==null) return;
        final double sa = getMarkedProportion(m,-1);
        if(sa>0d){
            boolean hasAny=false;
            if(m.getSecCount()<1){ // no color brushing
                int rX=r.x,rY=r.y,rW=r.width,rH=r.height;
                hasAny=true;
                switch (orientation){
                    case 0:
                        rH = (int)Math.round(rH*sa);
                        rY += r.height-rH;
                        break;
                    case 1:
                        rW = (int)Math.round(rW*sa);
                        break;
                    case 2:
                        rH = (int)Math.round(rH*sa);
                        break;
                    case 3:
                        rW = (int)Math.round(rW*sa);
                        rX += r.width - rW;
                        break;
                }
                
                g.setColor(COL_MARKED);
                g.fillRect(rX,rY,rW,rH);
                if(drawSelectionBorder){
                    g.setColor(COL_BORDER);
                    g.drawRect(rX,rY,rW,rH);
                }
            } else { // color brushing
                int rX=r.x,rY=r.y,rW=r.width,rH=r.height;
                final double totW=rW;
                final double totH=rH;
                int mark=-1;
                int shift=0;
                
                if(pieces!=null){
                    for(int i=0; i<=m.getMaxMark(); i++){
                        if (pieces[i]>0) {
                            double rmp = getRelativeMarkedProportion(m,i);
                            if (rmp>0d){
                                hasAny=true;
                                if (orientation==0) { // bottom-up
                                    rH=(int)Math.round(pieces[i]*rmp);
                                    rY=r.y+r.height-shift-rH;
                                } else if (orientation==2) { // top-down
                                    rH=(int)Math.round(pieces[i]*rmp);
                                    rY=r.y+shift;
                                } else if (orientation==1) { // left-right
                                    rW=(int)Math.round(pieces[i]*rmp);
                                    rX=r.x+shift;
                                } else if (orientation==3) { // right-left
                                    rW=(int)Math.round(pieces[i]*rmp);
                                    rX=r.x+r.width-shift-rW;
                                }
                                g.setColor(COL_MARKED);
                                g.fillRect(rX,rY,rW,rH);
                                if(drawSelectionBorder){
                                    g.setColor(COL_BORDER);
                                    g.drawRect(rX,rY,rW,rH);
                                }
                            }
                            shift+=pieces[i];
                        }
                    }
                }
            }
            if (hasAny && !drawSelectionBorder) {
                g.setColor(COL_BORDER);
                g.drawRect(r.x,r.y,r.width,r.height);
            }
        }
    }
    
    public String toString() {
        return "PPrimRectangle("+((r==null)?"<null rectangle>":(""+r.x+":"+r.y+","+r.width+":"+r.height))
        +", cases="+cases()+", drawBorder="+drawBorder+")";
    }
    
    private double getRelativeMarkedProportion(SMarker m, int mark) {
        double total=0;
        double selected=0;
        
        for(int i=0; i<ref.length; i++){
            if(m.getSec(ref[i])==mark){
                total++;
                if(m.get(ref[i])==-1) selected++;
            }
        }
        return ((total==0)?0:selected/total);
    }
    
}
