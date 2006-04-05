/*
 * PPrimCircle.java
 *
 * Created on 28. Oktober 2005, 10:33
 *
 */

package org.rosuda.ibase.toolkit;

import java.awt.Rectangle;

/**
 *
 * @author Tobias Wichtrey
 */
public class PPrimCircle extends PPrimBase {
    
    public int x,y,diam;
    
    public boolean visible = true;
    public boolean queryable=true;
    
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
    
    public void paint(final org.rosuda.pograss.PoGraSS g, final int orientation) {
        if(visible){
            g.setColor("outline");
            g.fillOval(x-diam/2,y-diam/2, diam,diam);
        }
    }
    
    public void paintSelected(final org.rosuda.pograss.PoGraSS g, final int orientation, final org.rosuda.ibase.SMarker m) {
        if(visible && ref!=null){
            int[] accMarks;
            if(ref.length>1){ // color brushing pie
                accMarks = new int[m.getMaxMark()+2];
                for(int i=0; i<ref.length; i++){
                    accMarks[m.get(ref[i])+1]++;
                }
                int numUsedMarks=0; //not counting mark 0
                for(int i=0; i<accMarks.length; i++){
                    if(i!=1 && accMarks[i]>0) numUsedMarks++;
                }
                if(numUsedMarks==0) return;
                double[] votes = new double[numUsedMarks];
                int[] maps = new int[numUsedMarks];
                int j=0;
                int total=0;
                for(int i=0; i<accMarks.length; i++){
                    if(i!=1 && accMarks[i]>0){
                        votes[j] = accMarks[i];
                        maps[j] = i;
                        total += votes[j];
                        j++;
                    }
                }
                int[] props = roundProportions(votes,total,360);
                int shift=0;
                for(int i=0; i<props.length; i++){
                    int mark = maps[i]-1;
                    if(mark==-1)
                        g.setColor(COL_MARKED);
                    else
                        g.setColor(ColorBridge.getMain().getColor(mark));
                    g.fillArc(x-diam/2,y-diam/2, diam,diam, shift, props[i]);
                    shift += props[i];
                }
            } else{
                final int mark = m.get(ref[0]);
                if (mark!=0) {
                    // FIXME: if we represent more that 1 ID then we're screwed ..
                    if (mark==-1)
                        g.setColor(COL_MARKED);
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
