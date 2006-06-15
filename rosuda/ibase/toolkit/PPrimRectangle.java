//
//  PPrimRectangle.java
//  Kplimt
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
    
    // public to avoid calling getter methods. setBounds should be used in combination with MINHEIGHT,MINWIDTH.
    public Rectangle r=new Rectangle();
    public boolean drawBorder=true;
    
    /**
     * allow color brushing?
     */
    public boolean allowColorBrushing = true;
    /**
     * whether the rectangle should be filled
     */
    public boolean filled = true;
    
    private int[] pieces;
    
    private int MINHEIGHT=1;
    private int MINWIDTH=1;
	
    protected int orientation=0;
    private boolean useCanvasOrientation=true; // TODO: das ist nur vorübergehend
    
    public PPrimRectangle() {
		super();
		useCanvasOrientation=true;
    }
    
    public PPrimRectangle(int or) {
    	super();
    	orientation=or;
    	useCanvasOrientation=false;
    }
    
    public int getOrientation() {return orientation;}
    
    
    /** checks whether the PlotPrimitive contains (or in case of a point primitive equals to) the given point.*/
    public boolean contains(final int x, final int y) { return (r==null)?false:r.contains(x,y); }
    
    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    public boolean intersects(final Rectangle rt) { return (r==null)?false:r.intersects(rt); }
    
    /** paint the primitive */
    public void paint(final PoGraSS g, final int orientation, final SMarker m) {
        if (r==null) return;
        
        if(dragging || !allowColorBrushing || !isBrushed(m)){ // dragging or no color brushing
            pieces=null;
            Color fillCol, borderCol;
            
            if(dragging && fillColorDrag!=null) fillCol = fillColorDrag;
            else fillCol = fillColor;
            
            if (drawBorder) borderCol = borderColor;
            else borderCol = null;
            
            drawRect(g,r,fillCol,borderCol);
        } else{ // color brushing
            brushRect(g,m,orientation,r,borderColor);
        }
    }
    
    public void paintSelected(final PoGraSS g, int orientation, final SMarker m) {
        if (r==null) return;
        // TODO: this is really ugly and should be done better
        // so orientation has to be moved to plotprimitives
        if(!useCanvasOrientation) orientation=this.orientation;
        
        final double sa = getMarkedProportion(m,-1);
        if(sa>0d){
            boolean hasAny=false;
            if(!allowColorBrushing || !isBrushed(m)){ // no color brushing
                int rX=r.x,rY=r.y,rW=r.width-1,rH=r.height-1;
                
                hasAny=true;
                switch (orientation){
                    case 0:
                        rH = getPropSize(rH,sa);
                        rY += r.height-rH;
                        rX++;
                        break;
                    case 1:
                        rW = getPropSize(rW,sa);
                        rX++;
                        rY++;
                        break;
                    case 2:
                        rH = getPropSize(rH,sa);
                        rX++;
                        rY++;
                        break;
                    case 3:
                        rW = getPropSize(rW,sa);
                        rX += r.width - rW;
                        rY++;
                        break;
                }
                
                drawRect(g,rX,rY,rW,rH,fillColorSel,null);
            } else { // color brushing
                int rX=r.x,rY=r.y,rW=r.width,rH=r.height;
                final double totW=rW;
                final double totH=rH;
                int mark=-1;
                int shift=0;
                
                if(pieces!=null){
                    /* old code for "zebra hiliting"
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
                                g.setColor(fillColorSel);
                                g.fillRect(rX,rY,rW,rH);
                            }
                            shift+=pieces[i];
                        }
                    }
                     */
                }
            }
            if (hasAny) {
                g.setColor(borderColorSel);
                g.drawRect(r.x,r.y,r.width,r.height);
            }
        }
    }
    
    public String toString() {
        return "PPrimRectangle("+((r==null)?"<null rectangle>":(""+r.x+":"+r.y+","+r.width+":"+r.height+","+orientation))
        +", cases="+cases()+", drawBorder="+drawBorder+")";
    }
    
    public void setBounds(int x, int y, int w, int h) {
		r.setBounds(x, y, Math.max(w,MINWIDTH), Math.max(h,MINHEIGHT));
    }
    
    public void setBounds(double x, double y, double w, double h) {
        setBounds((int)x,(int)y,(int)(w),(int)h);
    }
    
    protected void brushRect(PoGraSS g, SMarker m, int orientation, Rectangle r, Color borderColor) {
        int rX=r.x,rY=r.y,rW=r.width,rH=r.height;
        final double totW=rW;
        final double totH=rH;
        int mark=-1;
        int shift=0;
        boolean hasAny=false;
        
        double totalProp=0;
        double[] props = new double[m.getMaxMark()+2];
        for(int i=-1; i<=m.getMaxMark(); i++){
            props[i+1] = getMarkedProportion(m,i,true);
            totalProp += props[i+1];
        }
        if(totalProp>=0.0000001){
            final int hw = (((orientation&1)==0)?rH:rW);
            
            pieces = roundProportions(props,totalProp,hw);
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
                    g.setColor((i==-1)?fillColorSel:ColorBridge.getMain().getColor(i));
                    g.fillRect(rX,rY,rW,rH);
                }
            }
        }
        
        if (hasAny) drawRect(g,r,null,borderColor);
    }
    
    
    protected void drawRect(PoGraSS g, Rectangle r, Color fillColor, Color borderColor) {
        drawRect(g,r.x,r.y,r.width,r.height,fillColor,borderColor);
    }

    protected void drawRect(PoGraSS g, int rX, int rY, int rW, int rH, Color fillColor, Color borderColor) {
        if(filled && fillColor!=null){
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

    public void move(final int x, final int y) {
        r.setLocation(x,y);
    }

    public void moveX(final int x) {
        move(x,r.y);
    }

    public void moveY(final int y) {
        move(r.x,y);
    }

    public int getMINHEIGHT() {
        return this.MINHEIGHT;
    }

    public void setMINHEIGHT(final int MINHEIGHT) {
        this.MINHEIGHT = MINHEIGHT;
        if(r!=null && r.height < MINHEIGHT) r.height = MINHEIGHT;
    }

    public int getMINWIDTH() {
        return this.MINWIDTH;
    }

    public void setMINWIDTH(final int MINWIDTH) {
        this.MINWIDTH = MINWIDTH;
        if(r!=null && r.width < MINWIDTH) r.width = MINWIDTH;
    }
}
