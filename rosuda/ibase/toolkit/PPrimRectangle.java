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
    protected Color COL_BORDER = COL_OUTLINE;
    
    // public to avoid calling getter methods. setBounds should be used in combination with MINHEIGHT,MINWIDTH.
    public Rectangle r=new Rectangle();
    public boolean drawBorder=true;
    public boolean drawSelectionBorder=false;
    
    private int[] pieces;
    
    private int MINHEIGHT=1;
    private int MINWIDTH=1;
    
    /** checks whether the PlotPrimitive contains (or in case of a point primitive equals to) the given point.*/
    public boolean contains(final int x, final int y) { return (r==null)?false:r.contains(x,y); }
    
    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    public boolean intersects(final Rectangle rt) { return (r==null)?false:r.intersects(rt); }
    
    /** paint the primitive */
    public void paint(final PoGraSS g, final int orientation, final SMarker m) {
        if (r==null) return;
        
        if(!isBrushed(m)){ // no color brushing
            pieces=null;
            Color fillColor, borderColor;
            
            if (col!=null) fillColor = col;
            else fillColor = Common.objectsColor;
            if (drawBorder) borderColor = COL_BORDER;
            else borderColor = null;
            
            drawRect(g,r,fillColor,borderColor);
        } else{ // color brushing
            brushRect(g,m,orientation,r,COL_BORDER);
        }
    }
    
    public void paintSelected(final PoGraSS g, final int orientation, final SMarker m) {
        if (r==null) return;
        final double sa = getMarkedProportion(m,-1);
        if(sa>0d){
            boolean hasAny=false;
            if(!isBrushed(m)){ // no color brushing
                int rX=r.x,rY=r.y,rW=r.width,rH=r.height;
                hasAny=true;
                switch (orientation){
                    case 0:
                        rH = getPropSize(rH,sa);
                        rY += r.height-rH;
                        break;
                    case 1:
                        rW = getPropSize(rW,sa);
                        break;
                    case 2:
                        rH = getPropSize(rH,sa);
                        break;
                    case 3:
                        rW = getPropSize(rW,sa);
                        rX += r.width - rW;
                        break;
                }
                
                drawRect(g,rX,rY,rW,rH,Common.selectColor,drawSelectionBorder?COL_BORDER:null);
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
                                    rH=getPropSize(pieces[i],rmp);
                                    rY=r.y+r.height-shift-rH;
                                } else if (orientation==2) { // top-down
                                    rH=getPropSize(pieces[i],rmp);
                                    rY=r.y+shift;
                                } else if (orientation==1) { // left-right
                                    rW=getPropSize(pieces[i],rmp);
                                    rX=r.x+shift;
                                } else if (orientation==3) { // right-left
                                    rW=getPropSize(pieces[i],rmp);
                                    rX=r.x+r.width-shift-rW;
                                }
                                g.setColor(Common.selectColor);
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
    
    public void setBounds(int x, int y, int w, int h) {
        r.setBounds(x, y, Math.max(w,MINWIDTH), Math.max(h,MINHEIGHT));
    }
       
    protected void brushRect(PoGraSS g, SMarker m, int orientation, Rectangle r, Color borderColor) {
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
        
        if (hasAny) drawRect(g,r,null,borderColor);
    }
    
    
    protected static void drawRect(PoGraSS g, Rectangle r, Color fillColor, Color borderColor) {
        drawRect(g,r.x,r.y,r.width,r.height,fillColor,borderColor);
    }

    protected static void drawRect(PoGraSS g, int rX, int rY, int rW, int rH, Color fillColor, Color borderColor) {
        if(fillColor!=null){
            g.setColor(fillColor);
            g.fillRect(rX,rY,rW,rH);
        }
        if(borderColor!=null){
            g.setColor(borderColor);
            g.drawRect(rX,rY,rW,rH);
        }
    }
    
    protected boolean isBrushed(final SMarker m){
        return m.getSecCount()>=1;
    }
}
