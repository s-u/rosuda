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
    static final String COL_WHITE = "white";
    static final String COL_BLACK = "black";
    static final String COL_SELFILL = "selfill";
    static final String COL_SEL = "sel";
    
    Rectangle r;
    
    public int med,uh,lh,uh15,lh15;
    public double uh3,lh3;
    public int x,w,lowEdge,lastTop,highEdge;
    public double[] lastR;
    public int[] valPos;
    
    // parallel variables for selections
    public int smed,suh,slh,suh15,slh15;
    public double suh3,slh3;
    public int sx,sw,slowEdge,slastTop,shighEdge;
    public double[] slastR;
    public int[] svalPos;
    
    /** Creates a new instance of PPrimBox */
    public PPrimBox() {
    }
    
    public boolean intersects(final java.awt.Rectangle rt) {
        if(r!=null && r.intersects(rt)) return true;
        return false;
    }
    
    public void paint(final org.rosuda.pograss.PoGraSS g, final int orientation) {
        g.defineColor(COL_WHITE,255,255,255);
        g.defineColor(COL_BLACK,0,0,0);
        
        r = new Rectangle(x,uh,w-x, lh-uh-uh);
        
        g.setColor(COL_WHITE);
        g.fillRect(x,uh,
                w,lh-uh);
        
        g.setColor(COL_BLACK);
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
            final double val=lastR[i];
            if (val<lh3)
                g.drawOval(x+w/2-2,valPos[i]-2,3,3);
            else
                g.fillRect(x+w/2-1,valPos[i]-1,2,2);
            i--;
        }
        i=highEdge;
        while(i<lastTop) {
            final double val=lastR[i];
            if (val>uh3)
                g.drawOval(x+w/2-2,valPos[i]-2,3,3);
            else
                g.fillRect(x+w/2-1,valPos[i]-1,2,2);
            i++;
        }
    }
    
    public void paintSelected(final org.rosuda.pograss.PoGraSS g, final int orientation, final org.rosuda.ibase.SMarker m) {
        if(slastR==null) return;
        g.defineColor(COL_SELFILL,0,255,0);
        g.defineColor(COL_SEL,0,128,0);
        
        g.setColor(COL_SELFILL);
        g.fillRect(sx,suh,
                sw,slh-suh);
        g.setColor(COL_SEL);
        g.drawRect(sx,suh,
                sw,slh-suh);
        g.drawLine(sx,smed,
                sx+sw,smed);
        g.drawLine(sx,suh15,
                sx+sw,suh15);
        g.drawLine(sx,slh15,
                sx+sw,slh15);
        g.drawLine(sx+sw/2,suh,
                sx+sw/2,suh15);
        g.drawLine(sx+sw/2,slh,
                sx+sw/2,slh15);
        int i=slowEdge;
        while(i>=0) {
            final double val=slastR[i];
            if (val<slh3)
                g.drawOval(sx+sw/2-2,svalPos[i]-2,3,3);
            else
                g.fillRect(sx+sw/2-1,svalPos[i]-1,2,2);
            i--;
        }
        i=shighEdge;
        while(i<slastTop) {
            final double val=slastR[i];
            if (val>suh3)
                g.drawOval(sx+sw/2-2,svalPos[i]-2,3,3);
            else
                g.fillRect(sx+sw/2-1,svalPos[i]-1,2,2);
            i++;
        }
    }
    
    public boolean contains(final int x, final int y) {
        if(r!=null && r.contains(x,y)) return true;
        return false;
    }
    
}
