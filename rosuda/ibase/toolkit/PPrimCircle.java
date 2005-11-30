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
    
    /**
     * whether {@link #intersects} and {@link #contains} check for intersection
     * with the whole circle area (if true) or just with the center point (if
     * false)
     */
    public boolean intersectionByArea = false;
    
    public boolean intersects(java.awt.Rectangle rt) {
        if(intersectionByArea){
            // baaaaaaaaaad!
            return rt.intersects(new Rectangle(x-diam/2,y-diam/2, diam,diam));
        } else{
            return rt.contains(x,y);
        }
    }
    
    public void paint(org.rosuda.pograss.PoGraSS g, int orientation) {
        g.setColor("outline");
        g.fillOval(x-diam/2,y-diam/2, diam,diam);
    }
    
    public void paintSelected(org.rosuda.pograss.PoGraSS g, int orientation, org.rosuda.ibase.SMarker m) {
        if(ref!=null){
            for(int i=0; i<ref.length; i++){
                if(m.at(ref[i])){
                    g.setColor("marked");
                    g.fillOval(x-diam/2,y-diam/2, diam,diam);
                    return;
                }
            }
        }
    }
    
    public boolean contains(int x, int y) {
        if(intersectionByArea){
            return((x-this.x)*(x-this.x)+(y-this.y)*(y-this.y) <= diam*diam/4);
        } else{
            return(Math.max(Math.abs(x-this.x),Math.abs(y-this.y))<=Math.min(4,diam/2));
        }
    }
    
    public String toString() {
        return("PPrimCircle(x=" + x + ", y=" + y + ", diam="+diam+")");
    }
    
}
