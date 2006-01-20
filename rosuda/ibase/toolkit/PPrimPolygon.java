//
//  PPrimPolygon.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Oct 29 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;


/** Plot primitive based on {@link PPrimBase},  using a list of IDs and implementing polygons.
 * @version $Id$
 */
public class PPrimPolygon extends PPrimBase {
    static final String COL_RED = "red";
    static final String COL_OUTLINE = "outline";
    static final String COL_MARKED = "marked";
    public Polygon pg;
    public Polygon pg_ni=null;
    
    public boolean drawBorder=true;
    public boolean useSelAlpha=true;
    public boolean closed=true;
    public boolean fill=true;
    public boolean selectByCorners=false;
    public boolean drawCorners=false;
    public float[] lineWidth;
    public boolean[] invisibleLines;
    public boolean showInvisibleLines=false;
    public boolean[] gapDots;
    public boolean[] noDotsAt;
    public boolean showGapDots=true;
    
    private int nodeSize=2;
    
    /** checks whether the PlotPrimitive contains the given point.*/
    public boolean contains(final int x, final int y) {
        if(pg==null) return false;
        if(selectByCorners){
            for(int i=0; i<pg.npoints; i++)
                if(x==pg.xpoints[i] && y==pg.ypoints[i])
                    return true;
            return false;
        } else return ((pg_ni==null)?pg:pg_ni).contains(x,y);
    }
    
    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    public boolean intersects(final Rectangle rt) {
        if(pg==null) return false;
        if(selectByCorners){
            for(int i=0; i<pg.npoints; i++)
                if(rt.contains(pg.xpoints[i], pg.ypoints[i]))
                    return true;
            return false;
        } else return ((pg_ni==null)?pg:pg_ni).intersects(rt);
    }
    
    /** paint the primitive */
    public void paint(final PoGraSS g, final int orientation) {
        if (pg==null) return;
        g.defineColor(COL_RED,255,0,0);
        if(fill){
            if (col!=null)
                g.setColor(col.getRed(),col.getGreen(),col.getBlue());
            else
                g.setColor("object");
            g.fillPolygon(pg.xpoints,pg.ypoints,pg.npoints);
        }
        if (drawBorder) {
            g.setColor(COL_OUTLINE);
            //g.drawPolygon(pg.xpoints,pg.ypoints,pg.npoints,closed);
            for(int i=1; i<pg.npoints; i++){
                if(!invisibleLines[i-1]){
                    if(lineWidth!=null) g.setLineWidth(lineWidth[i-1]);
                    g.drawLine(pg.xpoints[i-1], pg.ypoints[i-1], pg.xpoints[i], pg.ypoints[i]);
                }
            }
            if(closed){
                if(lineWidth!=null) g.setLineWidth(lineWidth[pg.npoints-1]);
                g.drawLine(pg.xpoints[pg.npoints-1], pg.ypoints[pg.npoints-1], pg.xpoints[0], pg.ypoints[0]);
            }
            if(showInvisibleLines && invisibleLines!=null){
                g.setColor(COL_RED);
                for(int i=0; i<invisibleLines.length; i++){
                    if(invisibleLines[i]){
                        g.drawLine(pg.xpoints[i],pg.ypoints[i],pg.xpoints[i+1],pg.ypoints[i+1]);
                    }
                }
            }
        }
        if(drawCorners){
            g.setColor(COL_OUTLINE);
            for(int i=0; i<pg.npoints; i++){
                if(noDotsAt==null || !noDotsAt[i])
                    g.fillOval(pg.xpoints[i]-nodeSize, pg.ypoints[i]-nodeSize, 2*nodeSize+1,2*nodeSize+1);
            }
        }
        if(showGapDots && gapDots!=null){
            g.setColor(COL_OUTLINE);
            for(int i=0; i<gapDots.length; i++)
                if(gapDots[i])
                    g.fillOval(pg.xpoints[i]-nodeSize, pg.ypoints[i]-nodeSize, 2*nodeSize+1,2*nodeSize+1);
        }
    }
    
    public void paintSelected(final PoGraSS g, final int orientation, final SMarker m) {
        if (pg==null) return;
        g.defineColor(COL_RED,255,0,0);
        final double sa=getMarkedProportion(m,-1);
        //System.out.println("pp["+i+"] sa="+sa+" "+pp);
        if (sa>0d) {
            if(fill){
                if (useSelAlpha && sa<1.0)
                    g.setColor(((float)Common.selectColor.getRed())/255.0F,
                            ((float)Common.selectColor.getGreen())/255.0F,
                            ((float)Common.selectColor.getBlue())/255.0F,(float)sa);
                else
                    g.setColor(COL_MARKED);
                g.fillPolygon(pg.xpoints,pg.ypoints,pg.npoints);
            }
            if (drawBorder) {
                if(!fill){
                    if (useSelAlpha && sa<1.0)
                        g.setColor(((float)Common.selectColor.getRed())/255.0F,
                                ((float)Common.selectColor.getGreen())/255.0F,
                                ((float)Common.selectColor.getBlue())/255.0F,(float)sa);
                    else
                        g.setColor(COL_MARKED);
                } else g.setColor(COL_OUTLINE);
                //g.drawPolygon(pg.xpoints,pg.ypoints,pg.npoints,closed);
                for(int i=1; i<pg.npoints; i++){
                    if(!invisibleLines[i-1]){
                        if(lineWidth!=null) g.setLineWidth(lineWidth[i-1]);
                        g.drawLine(pg.xpoints[i-1], pg.ypoints[i-1], pg.xpoints[i], pg.ypoints[i]);
                    }
                }
                if(closed){
                    if(lineWidth!=null) g.setLineWidth(lineWidth[pg.npoints-1]);
                    g.drawLine(pg.xpoints[pg.npoints-1], pg.ypoints[pg.npoints-1], pg.xpoints[0], pg.ypoints[0]);
                }
            }
            if(drawCorners){
                g.setColor(COL_MARKED);
                for(int i=0; i<pg.npoints; i++){
                    if(noDotsAt==null || !noDotsAt[i])
                        g.fillOval(pg.xpoints[i]-nodeSize, pg.ypoints[i]-nodeSize, 2*nodeSize+1,2*nodeSize+1);
                }
            }
            if(showGapDots && gapDots!=null){
                g.setColor(COL_MARKED);
                for(int i=0; i<gapDots.length; i++)
                    if(gapDots[i])
                        g.fillOval(pg.xpoints[i]-nodeSize, pg.ypoints[i]-nodeSize, 2*nodeSize+1,2*nodeSize+1);
            }
        }
    }
    
    public String toString() {
        return "PPrimPolygon("+((pg==null)?"<null polygon>":(""+pg.npoints+" points"))+", drawBorder="+drawBorder+", useSelAlpha="+useSelAlpha+")";
    }
    
    public int getNodeSize() {
        return nodeSize;
    }
    
    public void setNodeSize(final int nodeSize) {
        if(nodeSize>0)
            this.nodeSize = nodeSize;
    }
    
    public void setNoInterior(){
        if(pg==null) return;
        int npoints = (pg.npoints-1)*2+1;
        int[] xpoints = new int[npoints];
        int[] ypoints = new int[npoints];
        
        System.arraycopy(pg.xpoints, 0, xpoints, 0, pg.npoints);
        System.arraycopy(pg.ypoints, 0, ypoints, 0, pg.npoints);
        for(int i=0; i<pg.npoints-1; i++){
            xpoints[npoints-1-i] = pg.xpoints[i];
            ypoints[npoints-1-i] = pg.ypoints[i];
        }
        
        pg_ni = new Polygon(xpoints, ypoints, npoints);
    }
}
