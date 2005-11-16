/*
 * PPrimBox.java
 *
 * Created on 14. November 2005, 15:51
 *
 */

package org.rosuda.ibase.toolkit;

import java.awt.Rectangle;

/**
 *
 * @author Tobias Wichtrey
 */
public class PPrimBox extends PPrimBase {
    
    Rectangle r;
    
    public int med,uh,lh,uh15,lh15;
    public double uh3,lh3;
    public int x,w,lowEdge,lastTop,highEdge;
    public double[] lastR;
    public int[] valPos;
    public String fillColor,drawColor;
    
    /** Creates a new instance of PPrimBox */
    public PPrimBox() {
    }
    
    public boolean intersects(java.awt.Rectangle rt) {
        if(r!=null && r.intersects(rt)) return true;
        return false;
    }
    
    public void paint(org.rosuda.pograss.PoGraSS g, int orientation) {
        //Rectangle r=pc.getBounds();
        if (fillColor!=null) {
            g.setColor(fillColor);
            g.fillRect(x,uh,
                    w,lh-uh);
        };
        g.setColor(drawColor);
        g.drawRect(x,uh,
                w,lh-uh);
        g.drawLine(x,med,
                x+w,med);
        g.drawLine(x,uh15,
                x+w,uh15);
        g.drawLine(x,lh15,
                x+w,lh15);
        g.drawLine(x+w/2,uh,
                x+w/2,uh15);
        g.drawLine(x+w/2,lh,
                x+w/2,lh15);
        int i=lowEdge;
        while(i>=0) {
            double val=lastR[i];
            if (val<lh3)
                g.drawOval(x+w/2-2,valPos[i]-2,3,3);
            else
                g.fillRect(x+w/2-1,valPos[i]-1,2,2);
            i--;
        };
        i=highEdge;
        while(i<lastTop) {
            double val=lastR[i];
            if (val>uh3)
                g.drawOval(x+w/2-2,valPos[i]-2,3,3);
            else
                g.fillRect(x+w/2-1,valPos[i]-1,2,2);
            i++;
        };
    }
    
    public void paintSelected(org.rosuda.pograss.PoGraSS g, int orientation, org.rosuda.ibase.SMarker m) {
    }
    
    public boolean contains(int x, int y) {
        if(r!=null && r.contains(x,y)) return true;
        return false;
    }
    
}
