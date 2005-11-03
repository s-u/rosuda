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
 * @author tobias
 */
public class PPrimCircle extends PPrimBase {
    
    public int x,y,diam;
    
    public boolean intersects(java.awt.Rectangle rt) {
        // baaaaaaaaaad!
        return rt.intersects(new Rectangle(x-diam/2,y-diam/2, diam,diam));
    }
    
    public void paint(org.rosuda.pograss.PoGraSS g, int orientation) {
        g.setColor("outline");
        g.drawOval(x-diam/2,y-diam/2, diam,diam);
        g.fillOval(x-diam/2,y-diam/2, diam,diam);
    }
    
    public void paintSelected(org.rosuda.pograss.PoGraSS g, int orientation, org.rosuda.ibase.SMarker m) {
        if(ref!=null){
            for(int i=0; i<ref.length; i++){
                if(m.at(ref[i])){
                    g.setColor("marked");
                    g.drawOval(x-diam/2,y-diam/2, diam,diam);
                    g.fillOval(x-diam/2,y-diam/2, diam,diam);
                    return;
                }
            }
        }
    }
    
    public boolean contains(int x, int y) {
        return((x-this.x)*(x-this.x)+(y-this.y)*(y-this.y) <= diam*diam/4);
    }
    
    public String toString() {
        return("PPrimCircle(x=" + x + ", y=" + y + ", diam="+diam+")");
    }
    
}
