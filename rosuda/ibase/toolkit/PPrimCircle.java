/*
 * PPrimCircle.java
 *
 * Created on 28. Oktober 2005, 10:33
 *
 */

package org.rosuda.ibase.toolkit;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.SMarker;

/**
 *
 * @author Tobias Wichtrey
 */
public class PPrimCircle extends PPrimBase {
    /**
     * x coordinate
     */
    public int x;
    /**
     * y coordinate
     */
    public int y;
    /**
     * diameter
     */
    public int diam;
    /**
     * whether the circle should be filled
     */
    public boolean filled = true;
    /**
     * whether a border should be drawn
     */
    public boolean drawBorder = false;
    /**
     * allow color brushing?
     */
    public boolean allowColorBrushing = true;
    /**
     * color brush the circle clockwise?
     */
    public boolean brushClockwise = false;
    /**
     * the angle to start brushing
     */
    public int startArc;
    /**
     * whether {@link #intersects} and {@link #contains} check for intersection
     * with the whole circle area (if true) or just with the center point (if
     * false)
     */
    public boolean intersectionByArea = true;
    
    private int[] pieces;
    
    public boolean intersects(final java.awt.Rectangle rt) {
        if(intersectionByArea){
            final Rectangle2D r2 = new Rectangle2D.Double(rt.x,rt.y,rt.width,rt.height);
            final Ellipse2D e2 = new Ellipse2D.Double(x-diam/2,y-diam/2, diam,diam);
            return e2.intersects(r2);
        } else{
            return rt.contains(x,y);
        }
    }
    
    public void paint(final org.rosuda.pograss.PoGraSS g, final int orientation, final SMarker m) {
        if(visible){
            if(!allowColorBrushing || m.getSecCount()<1){ // no color brushing
                pieces=null;
                g.setColor(fillColor);
                if(filled) g.fillOval(x-diam/2,y-diam/2, diam,diam);
                g.setColor(borderColor);
                if(drawBorder) g.drawOval(x-diam/2,y-diam/2, diam,diam);
                
            } else if(ref!=null && ref.length==1){ // color brushing for one case
                pieces=null;
                g.setColor(ColorBridge.getMain().getColor(m.getSec(ref[0])));
                if(filled) g.fillOval(x-diam/2,y-diam/2, diam,diam);
                if(drawBorder) g.drawOval(x-diam/2,y-diam/2, diam,diam);
                
            } else{ // color brushing for multiple cases
                double totalProp=0;
                final double[] props = new double[m.getMaxMark()+1];
                for(int i=0; i<=m.getMaxMark(); i++){
                    props[i] = getMarkedProportion(m,i);
                    totalProp += props[i];
                }
                pieces = roundProportions(props,totalProp,360);
                int shift=0;
                for(int i=0; i<=m.getMaxMark(); i++){
                    g.setColor(ColorBridge.getMain().getColor(i));
                    final int startAngle = (brushClockwise?(shift + startArc - pieces[i]):(shift + startArc));
                    if(filled) g.fillArc(x-diam/2,y-diam/2, diam,diam, startAngle, pieces[i]);
                    if(drawBorder) g.drawArc(x-diam/2,y-diam/2, diam,diam, startAngle, pieces[i]);
                    shift += (brushClockwise?-1:1) * pieces[i];
                }
            }
        }
    }
    
    public void paintSelected(final org.rosuda.pograss.PoGraSS g, final int orientation, final org.rosuda.ibase.SMarker m) {
        if(visible && ref!=null){
            
            if(allowColorBrushing && ref.length>1 && pieces!=null){ // color brushing pie
                int shift=0;
                for(int i=0; i<pieces.length; i++){
                    if (pieces[i]>0) {
                        final double rmp = getRelativeMarkedProportion(m,i);
                        if (rmp>0d){
                            g.setColor(fillColorSel);
                            if(filled) g.fillArc(x-diam/2,y-diam/2, diam,diam, shift + startArc, getPropSize(pieces[i],rmp));
                            g.setColor(borderColorSel);
                            if(drawBorder) g.drawArc(x-diam/2,y-diam/2, diam,diam, shift + startArc, getPropSize(pieces[i],rmp));
                        }
                        shift+=pieces[i];
                    }
                }
            } else{
                final int mark = m.get(ref[0]);
                if (mark==-1) {
                    g.setColor(fillColorSel);
                    if(filled) g.fillOval(x-diam/2,y-diam/2, diam,diam);
                    g.setColor(borderColorSel);
                    if(drawBorder) g.drawOval(x-diam/2,y-diam/2, diam,diam);
                    return;
                }
            }
        }
    }
    
    public boolean contains(final int x, final int y) {
        if(intersectionByArea){
            return((x-this.x)*(x-this.x)+(y-this.y)*(y-this.y) <= diam*diam/4);
        } else{
            return(x==this.x && y==this.y);
        }
    }
    
    public String toString() {
        return("PPrimCircle(x=" + x + ", y=" + y + ", diam="+diam+")");
    }
    
    public void move(final int x, final int y) {
        this.x=x;
        this.y=y;
    }
    
    public void moveX(final int x) {
        move(x,y);
    }
    
    public void moveY(final int y) {
        move(x,y);
    }
    
    public void setDiam(int d) {
    	diam=d;
    }
    
}
