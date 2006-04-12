/*
 * PPrimCircle.java
 *
 * Created on 28. Oktober 2005, 10:33
 *
 */

package org.rosuda.ibase.toolkit;

import java.awt.Rectangle;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.SMarker;

/**
 *
 * @author Tobias Wichtrey
 */
public class PPrimCircle extends PPrimBase {
    
    public int x,y,diam;
    
    public boolean visible = true;
    public boolean queryable=true;
    
    public int startArc;
    
    private int[] pieces;
    
    /**
     * whether {@link #intersects} and {@link #contains} check for intersection
     * with the whole circle area (if true) or just with the center point (if
     * false)
     */
    public boolean intersectionByArea = true;
    
    public boolean intersects(final java.awt.Rectangle rt) {
        if(intersectionByArea){
            // baaaaaaaaaad!
            return rt.intersects(new Rectangle(x-diam/2,y-diam/2, diam,diam));
        } else{
            return rt.contains(x,y);
        }
    }
    
    public void paint(final org.rosuda.pograss.PoGraSS g, final int orientation, final SMarker m) {
        if(visible){
            if(m.getSecCount()<1){ // no color brushing
                pieces=null;
                g.setColor(COL_OUTLINE);
                g.fillOval(x-diam/2,y-diam/2, diam,diam);
                
            } else if(ref!=null && ref.length==1){ // color brushing for one case
                pieces=null;
                g.setColor(ColorBridge.getMain().getColor(m.getSec(ref[0])));
                g.fillOval(x-diam/2,y-diam/2, diam,diam);
                
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
                    g.fillArc(x-diam/2,y-diam/2, diam,diam, shift + startArc, pieces[i]);
                    shift += pieces[i];
                }
            }
        }
    }
    
    public void paintSelected(final org.rosuda.pograss.PoGraSS g, final int orientation, final org.rosuda.ibase.SMarker m) {
        if(visible && ref!=null){
            
            if(ref.length>1){ // color brushing pie
                if(pieces!=null){
                    int shift=0;
                    for(int i=0; i<pieces.length; i++){
                        if (pieces[i]>0) {
                            final double rmp = getRelativeMarkedProportion(m,i);
                            if (rmp>0d){
                                g.setColor(Common.selectColor);
                                g.fillArc(x-diam/2,y-diam/2, diam,diam, shift + startArc, getPropSize(pieces[i],rmp));
                            }
                            shift+=pieces[i];
                        }
                    }
                }
            } else{
                final int mark = m.get(ref[0]);
                if (mark!=0) {
                    if (mark==-1)
                        g.setColor(Common.selectColor);
                    else
                        g.setColor(ColorBridge.getMain().getColor(mark));
                    g.fillOval(x-diam/2,y-diam/2, diam,diam);
                    return;
                }
            }
        }
    }
    
    public boolean contains(final int x, final int y) {
        if(intersectionByArea){
            return((x-this.x)*(x-this.x)+(y-this.y)*(y-this.y) <= diam*diam/4);
        } else{
            return(Math.max(Math.abs(x-this.x),Math.abs(y-this.y))<=Math.min(4,diam/2));
        }
    }
    
    public String toString() {
        return("PPrimCircle(x=" + x + ", y=" + y + ", diam="+diam+")");
    }
    
    public boolean isQueryable() {
        return queryable;
    }
    
}
