package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

public class PPrimHam extends PPrimPolygon {
    int x1,y1,x2,y2;
    public boolean alwaysAlpha=false;
    public double fudge=0.2;
    public int total=0;
    public int tmp=0;
    public int leftVar=0;

    int dx,dy;

    public PPrimHam() {
        drawBorder=false;
    }
    
    public void updateAnchors(int ax1, int ay1, int ax2, int ay2, int totHeight) {
        x1=ax1; y1=ay1; x2=ax2; y2=ay2;
        if (cases()<1) return;
        double ddx=(double)(x2-x1);
        double ddy=(double)(y2-y1);
        double cs=(double)cases();
        if (total==0) total=cases()*2; // safety fallback

        double t=cs/((double)total)*((double)totHeight)*fudge*0.5;

        dy=(int) (t/Math.sqrt(1+(ddy*ddy)/(ddx*ddx)));
        dx=(int) -(ddy*t/Math.sqrt(ddx*ddx+ddy*ddy));

        if (dy<1 && dx<1)
            dy=1;

        int xp[]=new int[4];
        int yp[]=new int[4];
        xp[0]=x1-dx; yp[0]=y1-dy;
        xp[1]=x2-dx; yp[1]=y2-dy;
        xp[2]=x2+dx; yp[2]=y2+dy;
        xp[3]=x1+dx; yp[3]=y1+dy;

        pg=new Polygon(xp,yp,4);
    }
    
    /** paint the primitive */
    public void paint(PoGraSS g, int orientation) {
        if (cases()>1) {
            super.paint(g, orientation);
            return;
        } else {
            if (col!=null)
                g.setColor(col.getRed(),col.getGreen(),col.getBlue());
            else
                g.setColor("object");
            g.drawLine(x1,y1,x2,y2);
        }
    }

    public void paintSelected(PoGraSS g, int orientation, SMarker m) {
        if (cases()>1) {
            int adx=(dx>0)?dx:-dx;
            int ady=(dy>0)?dy:-dy;
            if (alwaysAlpha || adx+ady<3) {
                super.paintSelected(g, orientation, m);
                return;
            }
            double sd=getMarkedProportion(m,-1);
            Polygon orig=pg;
            int xp[]=new int[4];
            int yp[]=new int[4];
            int ndx=(int)(((double)dx)*(2.0*sd-1.0));
            int ndy=(int)(((double)dy)*(2.0*sd-1.0));
            xp[0]=x1-ndx; yp[0]=y1-ndy;
            xp[1]=x2-ndx; yp[1]=y2-ndy;
            xp[2]=x2+dx; yp[2]=y2+dy;
            xp[3]=x1+dx; yp[3]=y1+dy;
            boolean alphaOrig=useSelAlpha;
            useSelAlpha=false;
            pg=new Polygon(xp,yp,4);
            super.paintSelected(g, orientation, m);
            pg=orig;
            useSelAlpha=alphaOrig;
            
            return;
        } else {
            // want do we do??            
            double sa=getMarkedProportion(m,-1);
            if (sa>0) {
                g.setColor("marked");
                g.drawLine(x1,y1,x2,y2);

            }
        }
    }

    public String toString() {
        return "PPrimHam("+x1+","+y1+","+x2+","+y2+", cases="+cases()+", drawBorder="+drawBorder+")";
    }
}
