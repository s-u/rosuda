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
    
    public boolean intersects(final java.awt.Rectangle rt) {
        if(intersectionByArea){
            // baaaaaaaaaad!
            return rt.intersects(new Rectangle(x-diam/2,y-diam/2, diam,diam));
        } else{
            return rt.contains(x,y);
        }
    }
    
    public void paint(final org.rosuda.pograss.PoGraSS g, final int orientation) {
        g.setColor("outline");
        g.fillOval(x-diam/2,y-diam/2, diam,diam);
    }
    
    public void paintSelected(final org.rosuda.pograss.PoGraSS g, final int orientation, final org.rosuda.ibase.SMarker m) {
        if(ref!=null){
            for(int i=0; i<ref.length; i++){
				final int mark = m.get(ref[i]);
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
    
}
