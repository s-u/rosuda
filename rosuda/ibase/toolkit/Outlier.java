/*
 * Outlier.java
 *
 * Created on 6. März 2006, 12:19
 *
 */

package org.rosuda.ibase.toolkit;

import java.awt.Rectangle;
import org.rosuda.ibase.SMarker;
import org.rosuda.pograss.PoGraSS;

/**
 *
 * @author tobias
 */
public class Outlier extends PPrimBase {
    PPrimCircle out;
    private double value;
    
    Outlier(int x, int y, int r, double v){
        out  = new PPrimCircle();
        out.x=x;
        out.y=y;
        out.diam=2*r-1;
        value=v;
    }
    
    public boolean contains(int x, int y) {
        return out.contains(x,y);
    }
    
    public boolean intersects(Rectangle rt) {
        return out.intersects(rt);
    }
    
    public void paint(PoGraSS g, int orientation, SMarker m) {
        //....
    }
    
    public void paintSelected(PoGraSS g, int orientation, SMarker m) {
        //....
    }
    
    public double getValue() {
        return value;
    }
}