/*
 * PPrimBox.java
 *
 * Created on 14. November 2005, 15:51
 *
 */

package org.rosuda.ibase.toolkit;

import java.awt.Rectangle;
import java.util.Enumeration;
import java.util.Vector;
import org.rosuda.ibase.SMarker;
import org.rosuda.pograss.PoGraSS;

/**
 *
 * @author Tobias Wichtrey
 */
public class PPrimBox extends PPrimBase {
    static final String COL_WHITE = "white";
    static final String COL_BLACK = "black";
    static final String COL_SELFILL = "selfill";
    static final String COL_SEL = "sel";
    
    // radius of outliers
    static final int RADOUTL = 5;
    
    Rectangle r;
    private Vector outliers = new Vector();
    
    // variables containing the screen positions
    public int med,uh,lh,uh15,lh15;
    // variables containing the real values
    public double medValue,uhValue,lhValue,uh15Value,lh15Value;
    public double uh3,lh3;
    public int x,w,lowEdge,lastTop,highEdge;
    public double[] lastR;
    public int[] valPos;
    
    // parallel variables for selections
    public int smed,suh,slh,suh15,slh15;
    public double suh3,slh3;
    public int sx;
    
    public Outlier queriedOutlier;
    
    public int sw, slowEdge, slastTop, shighEdge;
    public double[] slastR;
    public int[] svalPos;
    
    /** Creates a new instance of PPrimBox */
    public PPrimBox() {
    }
    
    public boolean intersects(final java.awt.Rectangle rt) {
        if(r!=null && r.intersects(rt)) return true;
        for(Enumeration en = outliers.elements(); en.hasMoreElements();){
            if(((Outlier)en.nextElement()).intersects(rt)) return true;
        }
        return false;
    }
    
    public void paint(final org.rosuda.pograss.PoGraSS g, final int orientation) {
        g.defineColor(COL_WHITE,255,255,255);
        g.defineColor(COL_BLACK,0,0,0);
        
        int i;
        
        switch(orientation){
            case 0:
                r = new Rectangle(x,uh,w, lh-uh);
                
                g.setColor(COL_WHITE);
                g.fillRect(x,uh,
                        w,lh-uh);
                
                g.setColor(COL_BLACK);
                g.drawRect(x,uh,
                        w,lh-uh);
                g.setLineWidth(1.5f);
                g.drawLine(x,med,
                        x+w,med);
                g.setLineWidth(1.0f);
                g.drawLine(x,uh15,
                        x+w,uh15);
                g.drawLine(x,lh15,
                        x+w,lh15);
                g.drawLine(x+w/2,uh,
                        x+w/2,uh15);
                g.drawLine(x+w/2,lh,
                        x+w/2,lh15);
                i=lowEdge;
                // draw outliers
                outliers.clear();
                final int ox=x+w/2-(RADOUTL+1)/2;
                int oy;
                while(i>=0) {
                    oy=valPos[i]-(RADOUTL+1)/2;
                    final double val=lastR[i];
                    outliers.add(new Outlier(ox,oy,RADOUTL,val));
                    if (val<lh3)
                        g.fillOval(ox,oy,RADOUTL,RADOUTL);
                    else
                        g.drawOval(ox,oy,RADOUTL,RADOUTL);
                    i--;
                }
                i=highEdge;
                while(i<lastTop) {
                    oy=valPos[i]-(RADOUTL+1)/2;
                    final double val=lastR[i];
                    outliers.add(new Outlier(ox,oy,RADOUTL,val));
                    if (val>uh3)
                        g.fillOval(ox,oy,RADOUTL,RADOUTL);
                    else
                        g.drawOval(ox,oy,RADOUTL,RADOUTL);
                    i++;
                }
                break;
            case 1:
                r = new Rectangle(uh,x, uh-lh,w);
                
                g.setColor(COL_WHITE);
                g.fillRect(lh,x,
                        uh-lh,w);
                
                g.setColor(COL_BLACK);
                g.drawRect(lh,x,
                        uh-lh,w);
                g.setLineWidth(1.5f);
                g.drawLine(med,x,
                        med,x+w);
                g.setLineWidth(1.0f);
                g.drawLine(uh15,x,
                        uh15,x+w);
                g.drawLine(lh15,x,
                        lh15,x+w);
                g.drawLine(uh,x+w/2,
                        uh15,x+w/2);
                g.drawLine(lh,x+w/2,
                        lh15,x+w/2);
                i=lowEdge;
                while(i>=0) {
                    final double val=lastR[i];
                    if (val<lh3)
                        g.fillOval(valPos[i]-(RADOUTL+1)/2,x+w/2-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    else
                        g.drawOval(valPos[i]-(RADOUTL+1)/2,x+w/2-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    i--;
                }
                i=highEdge;
                while(i<lastTop) {
                    final double val=lastR[i];
                    if (val>uh3)
                        g.fillOval(valPos[i]-(RADOUTL+1)/2,x+w/2-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    else
                        g.drawOval(valPos[i]-(RADOUTL+1)/2,x+w/2-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    i++;
                }
        }
    }
    
    public void paintSelected(final org.rosuda.pograss.PoGraSS g, final int orientation, final org.rosuda.ibase.SMarker m) {
        if(slastR==null) return;
        g.defineColor(COL_SELFILL,0,255,0);
        g.defineColor(COL_SEL,0,128,0);
        int i;
        
        switch(orientation){
            case 0:
                g.setColor(COL_SELFILL);
                g.fillRect(sx,suh,
                        sw,slh-suh);
                g.setColor(COL_SEL);
                g.drawRect(sx,suh,
                        sw,slh-suh);
                g.setLineWidth(1.5f);
                g.drawLine(sx,smed,
                        sx+sw,smed);
                g.setLineWidth(1.0f);
                g.drawLine(sx,suh15,
                        sx+sw,suh15);
                g.drawLine(sx,slh15,
                        sx+sw,slh15);
                g.drawLine(sx+sw/2,suh,
                        sx+sw/2,suh15);
                g.drawLine(sx+sw/2,slh,
                        sx+sw/2,slh15);
                i=slowEdge;
                while(i>=0) {
                    final double val=slastR[i];
                    if (val<slh3)
                        g.fillOval(sx+sw/2-(RADOUTL+1)/2,svalPos[i]-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    else
                        g.drawOval(sx+sw/2-(RADOUTL+1)/2,svalPos[i]-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    i--;
                }
                i=shighEdge;
                while(i<slastTop) {
                    final double val=slastR[i];
                    if (val>suh3)
                        g.fillOval(sx+sw/2-(RADOUTL+1)/2,svalPos[i]-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    else
                        g.drawOval(sx+sw/2-(RADOUTL+1)/2,svalPos[i]-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    i++;
                }
                break;
            case 1:
                g.setColor(COL_SELFILL);
                g.fillRect(slh,sx,
                        suh-slh,sw);
                g.setColor(COL_SEL);
                g.drawRect(slh,sx,
                        suh-slh,sw);
                g.setLineWidth(1.5f);
                g.drawLine(smed,sx,
                        smed,sx+sw);
                g.setLineWidth(1.0f);
                g.drawLine(suh15,sx,
                        suh15,sx+sw);
                g.drawLine(slh15,sx,
                        slh15,sx+sw);
                g.drawLine(suh,sx+sw/2,
                        suh15,sx+sw/2);
                g.drawLine(slh,sx+sw/2,
                        slh15,sx+sw/2);
                i=slowEdge;
                while(i>=0) {
                    final double val=slastR[i];
                    if (val<slh3)
                        g.fillOval(svalPos[i]-(RADOUTL+1)/2,sx+sw/2-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    else
                        g.drawOval(svalPos[i]-(RADOUTL+1)/2,sx+sw/2-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    i--;
                }
                i=shighEdge;
                while(i<slastTop) {
                    final double val=slastR[i];
                    if (val>suh3)
                        g.fillOval(svalPos[i]-(RADOUTL+1)/2,sx+sw/2-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    else
                        g.drawOval(svalPos[i]-(RADOUTL+1)/2,sx+sw/2-(RADOUTL+1)/2,RADOUTL,RADOUTL);
                    i++;
                }
                break;
        }
    }
    
    public boolean contains(final int x, final int y) {
        queriedOutlier=null;
        if(r!=null && r.contains(x,y)) return true;
        for(Enumeration en = outliers.elements(); en.hasMoreElements();){
            Outlier o = (Outlier)en.nextElement();
            if(o.contains(x,y)) {
                queriedOutlier=o;
                return true;
            }
        }
        return false;
    }
}