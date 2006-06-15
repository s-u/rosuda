/*
 * PPrimBox.java
 *
 * Created on 14. November 2005, 15:51
 *
 */

package org.rosuda.ibase.toolkit;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.rosuda.ibase.Common;
import org.rosuda.ibase.SMarker;

/**
 *
 * @author Tobias Wichtrey
 */
public class PPrimBox extends PPrimBase {
    
    // radius of outliers
    public int RADOUTL = 5;
    
    // line widths
    public float LWRECT = 1.0f;
    public float LWMEDIAN = 1.5f;
    public float LWWHISKER = 1.0f;
    public float LWWHISKEREND = 1.0f;
    
    public float LWRECTSEL = 1.0f;
    public float LWMEDIANSEL = 1.5f;
    public float LWWHISKERSEL = 2.0f;
    public float LWWHISKERENDSEL = 1.5f;
    
    
    Rectangle r;
    private List outliers = new ArrayList();
    
    // variables containing the screen positions
    public int med,uh,lh,uh15,lh15;
    // variables containing the real values
    public double medValue,uhValue,lhValue,uh15Value,lh15Value;
    public double uh3,lh3;
    public int x,w,lowEdge,lastTop,highEdge;
    public double[] lastR;
    public int[] valPos;
    
    // parallel variables for selections
    public Rectangle sr;
    public int smed,suh,slh,suh15,slh15;
    public double suh3,slh3;
    public int sx;
    
    public Outlier queriedOutlier;

    public int sw;

    public boolean queriedSelection=false;

    public double sminValue;
    public double smaxValue;

    public int slowEdge, slastTop, shighEdge;
    public double[] slastR;
    public int[] svalPos;
    
    /** whether contains returns true if the queried point lies in hiliting box */
    public boolean containsInSelection = true;
    
    /** Creates a new instance of PPrimBox */
    public PPrimBox() {
        super();
        borderColorSel = COL_OUTLINE;
    }
    
    public boolean intersects(final java.awt.Rectangle rt) {
        /*if(r!=null && r.intersects(rt)) return true;
        for(Enumeration en = outliers.elements(); en.hasMoreElements();){
            if(((Outlier)en.nextElement()).intersects(rt)) return true;
        }*/
        return false;
    }
    
    public void paint(final org.rosuda.pograss.PoGraSS g, final int orientation, final SMarker m) {
        int i;
        int ox,oy;
        switch(orientation){
            case 0:
                r = new Rectangle(x,uh,w, lh-uh);
                
                g.setColor(dragging?fillColorDrag:fillColor);
                g.fillRect(x,uh,
                        w,lh-uh);
                
                g.setColor(borderColor);
                g.setLineWidth(LWRECT);
                g.drawRect(x,uh,
                        w,lh-uh);
                g.setLineWidth(LWMEDIAN);
                g.drawLine(x,med,
                        x+w,med);
                g.setLineWidth(LWWHISKEREND);
                g.drawLine(x,uh15,
                        x+w,uh15);
                g.drawLine(x,lh15,
                        x+w,lh15);
                g.setLineWidth(LWWHISKER);
                g.drawLine(x+w/2,uh,
                        x+w/2,uh15);
                g.drawLine(x+w/2,lh,
                        x+w/2,lh15);
                g.setLineWidth(1.0f);
                i=lowEdge;
                // draw outliers
                outliers.clear();
                ox=x+w/2-(RADOUTL+1)/2;
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
                r = new Rectangle(lh,x, uh-lh,w);
                
                g.setColor(dragging?fillColorDrag:fillColor);
                g.fillRect(lh,x,
                        uh-lh,w);
                
                g.setColor(borderColor);
                g.setLineWidth(LWRECT);
                g.drawRect(lh,x,
                        uh-lh,w);
                g.setLineWidth(LWMEDIAN);
                g.drawLine(med,x,
                        med,x+w);
                g.setLineWidth(LWWHISKER);
                g.drawLine(uh15,x,
                        uh15,x+w);
                g.drawLine(lh15,x,
                        lh15,x+w);
                g.setLineWidth(LWWHISKEREND);
                g.drawLine(uh,x+w/2,
                        uh15,x+w/2);
                g.drawLine(lh,x+w/2,
                        lh15,x+w/2);
                g.setLineWidth(1.0f);
                i=lowEdge;
                // draw outliers
                outliers.clear();
                oy=x+w/2-(RADOUTL+1)/2;
                while(i>=0) {
                    ox=valPos[i]-(RADOUTL+1)/2;
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
                    ox=valPos[i]-(RADOUTL+1)/2;
                    final double val=lastR[i];
                    outliers.add(new Outlier(ox,oy,RADOUTL,val));
                    if (val>uh3)
                        g.fillOval(ox,oy,RADOUTL,RADOUTL);
                    else
                        g.drawOval(ox,oy,RADOUTL,RADOUTL);
                    i++;
                }
        }
    }
    
    public boolean hilitcontains(int x, int y) {
    	if(sr!=null) return sr.contains(x,y);
    	else return false;
    }
    
    public void paintSelected(final org.rosuda.pograss.PoGraSS g, final int orientation, final org.rosuda.ibase.SMarker m) {
        if(slastR==null) return;
        int i;
        
        switch(orientation){
            case 0:
                sr = new Rectangle(sx,suh,sw, slh-suh);
                g.setColor(fillColorSel);
                g.fillRect(sx,suh,
                        sw,slh-suh);
                g.setColor(borderColorSel);
                g.setLineWidth(LWRECTSEL);
                g.drawRect(sx,suh,
                        sw,slh-suh);
                g.setLineWidth(LWMEDIANSEL);
                g.drawLine(sx,smed,
                        sx+sw,smed);
                g.setLineWidth(LWWHISKERSEL);
                g.drawLine(sx,suh15,
                        sx+sw,suh15);
                g.drawLine(sx,slh15,
                        sx+sw,slh15);
                g.setLineWidth(LWWHISKERENDSEL);
                g.drawLine(sx+sw/2,suh,
                        sx+sw/2,suh15);
                g.drawLine(sx+sw/2,slh,
                        sx+sw/2,slh15);
                g.setLineWidth(1.0f);
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
                sr = new Rectangle(slh,sx, suh-slh,sw);
                g.setColor(fillColorSel);
                g.fillRect(slh,sx,
                        suh-slh,sw);
                g.setColor(borderColorSel);
                g.setLineWidth(LWRECTSEL);
                g.drawRect(slh,sx,
                        suh-slh,sw);
                g.setLineWidth(LWMEDIANSEL);
                g.drawLine(smed,sx,
                        smed,sx+sw);
                g.setLineWidth(LWWHISKERSEL);
                g.drawLine(suh15,sx,
                        suh15,sx+sw);
                g.drawLine(slh15,sx,
                        slh15,sx+sw);
                g.setLineWidth(LWWHISKERENDSEL);
                g.drawLine(suh,sx+sw/2,
                        suh15,sx+sw/2);
                g.drawLine(slh,sx+sw/2,
                        slh15,sx+sw/2);
                g.setLineWidth(1.0f);
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
        if(r!=null && r.contains(x,y)) {
            queriedSelection = false;
            return true;
        }
        if(containsInSelection && sr!=null && sr.contains(x,y)){
            queriedSelection = true;
            return true;
        }
        for(Iterator it = outliers.listIterator(); it.hasNext();){
            Outlier o = (Outlier)it.next();
            if(o.contains(x,y)) {
                queriedOutlier=o;
                return true;
            }
        }
        return false;
    }

    public void move(final int x, final int y) {
        //TODO: this only moves parts of the box and ignores y part!
        // won`t work with rotated boxes
        this.x=x;
    }

    public void moveX(final int x) {
        move(x,0);
    }

    public void moveY(final int y) {
        move(y,0);
    }
}