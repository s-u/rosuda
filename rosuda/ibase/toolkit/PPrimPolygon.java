//
//  PPrimPolygon.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Oct 29 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;


/** Plot primitive based on {@link PPrimBase},  using a list of IDs and implementing polygons.
 * @version $Id$
 */
public class PPrimPolygon extends PPrimBase {
    public Polygon pg;
    
    public boolean drawBorder=true;
    public boolean useSelAlpha=true;
    public boolean closed=true;
    public boolean fill=true;
    public boolean selectByCorners=false;
    public boolean drawCorners=false;
    public float[] lineWidth;
    public boolean[] invisibleLines;
    public boolean showInvisibleLines=false;
    private boolean[] gapDots;
    public boolean[] noDotsAt;
    public boolean showGapDots=true;
    private java.util.List gapDotPs;
    
    public Color COL_INVISIBLELINES = new Color(255,255,0);
    
    private int nodeSize=2;
    
    /** checks whether the PlotPrimitive contains the given point.*/
    public boolean contains(final int x, final int y) {
        if(pg==null) return false;
        if(drawCorners){
            for(int i=0; i<pg.npoints; i++)
                if((x-pg.xpoints[i])*(x-pg.xpoints[i])+(y-pg.ypoints[i])*(y-pg.ypoints[i]) <= nodeSize*nodeSize)
                    return true;
        }
        if(drawBorder){
            for(int i=1; i<pg.npoints; i++){
                if(!invisibleLines[i-1]){
                    int my,My;
                    my=Math.min(pg.ypoints[i-1],pg.ypoints[i]);
                    My=Math.max(pg.ypoints[i-1],pg.ypoints[i]);
                    if(x>=pg.xpoints[i-1] && x<=pg.xpoints[i] && y>=my && y<=My){
                        double t = (double)(x-pg.xpoints[i-1]) / (pg.xpoints[i]-pg.xpoints[i-1]);
                        double ydiff = Math.abs(pg.ypoints[i-1] + t*(pg.ypoints[i]-pg.ypoints[i-1]) - y);
                        if(ydiff<=1) return true;
                    }
                }
            }
        }
        if(gapDotPs!=null)
            for (Iterator it = gapDotPs.iterator(); it.hasNext();)
                if(((PPrimCircle)it.next()).contains(x,y))
                    return true;
        return false;
    }
    
    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    public boolean intersects(final Rectangle rt) {
        if(pg==null) return false;
        Rectangle2D.Double r2d = new Rectangle2D.Double(rt.x,rt.y,rt.width,rt.height);
        if(selectByCorners){
            for(int i=0; i<pg.npoints; i++)
                if(
                    (new Ellipse2D.Double(pg.xpoints[i]-nodeSize, pg.ypoints[i]-nodeSize,2*nodeSize+1,2*nodeSize+1))
                    .intersects(r2d))
                    return true;
        } else{
            for(int i=1; i<pg.npoints; i++){
                if(!invisibleLines[i-1]){
                    Line2D.Double l = new Line2D.Double(pg.xpoints[i-1],pg.ypoints[i-1],pg.xpoints[i],pg.ypoints[i]);
                    if (l.intersects(r2d)) return true;
                }
            }
        }
        for(Iterator it = gapDotPs.iterator(); it.hasNext();)
            if(((PPrimCircle)it.next()).intersects(rt))
                return true;
        return false;
    }
    
    /** paint the primitive */
    public void paint(final PoGraSS g, final int orientation, final SMarker m) {
        if (pg==null) return;
        final int mark = m.getSec(ref[0]);
        Color color;
        if(mark>0) {
            color = ColorBridge.getMain().getColor(mark);
        } else{
            color = borderColor;
        }
        paintPolygon(g,orientation,m,false,color,fillColor);
        if(showGapDots && gapDots!=null){
            for(Iterator it = gapDotPs.iterator(); it.hasNext();) ((PPrimCircle)it.next()).paint(g,orientation,m);
        }
    }
    
    public void paintSelected(final PoGraSS g, final int orientation, final SMarker m) {
        if (pg==null) return;
        final int mark = m.get(ref[0]);
        if(mark==-1) {
            paintPolygon(g,orientation,m,true,borderColorSel,fillColorSel);
        }
        if(showGapDots && gapDots!=null){
            for(Iterator it = gapDotPs.iterator(); it.hasNext();) ((PPrimCircle)it.next()).paintSelected(g,orientation,m);
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
    
    private void paintPolygon(final PoGraSS g, final int orientation, final SMarker m, boolean paintingSelected, final Color colOutline, final Color colFill) {
        if(fill){
            if (colFill!=null)
                g.setColor(colFill);
            else
                g.setColor(Common.objectsColor);
            g.fillPolygon(pg.xpoints,pg.ypoints,pg.npoints);
        }
        if (drawBorder) {
            g.setColor(colOutline);
            //g.drawPolygon(pg.xpoints,pg.ypoints,pg.npoints,closed);
            for(int i=1; i<pg.npoints; i++){
                if(!invisibleLines[i-1]){
                    if(lineWidth!=null) g.setLineWidth(lineWidth[i-1]);
                    // ??g.setColor(colOutline);
                    g.drawLine(pg.xpoints[i-1], pg.ypoints[i-1], pg.xpoints[i], pg.ypoints[i]);
                }
            }
            if(closed){
                if(lineWidth!=null) g.setLineWidth(lineWidth[pg.npoints-1]);
                g.drawLine(pg.xpoints[pg.npoints-1], pg.ypoints[pg.npoints-1], pg.xpoints[0], pg.ypoints[0]);
            }
            if(!paintingSelected && showInvisibleLines && invisibleLines!=null){
                g.setColor(COL_INVISIBLELINES);
                for(int i=0; i<invisibleLines.length-1; i++){
                    if(invisibleLines[i]){
                        g.drawLine(pg.xpoints[i],pg.ypoints[i],pg.xpoints[i+1],pg.ypoints[i+1]);
                    }
                }
            }
        }
        if(drawCorners){
            g.setColor(colOutline);
            for(int i=0; i<pg.npoints; i++){
                if(noDotsAt==null || !noDotsAt[i])
                    g.fillOval(pg.xpoints[i]-nodeSize, pg.ypoints[i]-nodeSize, 2*nodeSize+1,2*nodeSize+1);
            }
        }
    }
    
    public void setGapDots(final boolean[] gapDots) {
        this.gapDots = gapDots;
        if(gapDots!=null){
            if(gapDotPs==null) gapDotPs = new ArrayList(gapDots.length/2);
            else gapDotPs.clear();
            
            for(int i=0; i<gapDots.length; i++){
                if(gapDots[i]){
                    final PPrimCircle gd = new PPrimCircle();
                    gd.x = pg.xpoints[i];
                    gd.y = pg.ypoints[i];
                    gd.diam = 2*nodeSize+1;
                    gd.ref = ref;
                    gd.fillColor = COL_OUTLINE;
                    
                    gapDotPs.add(gd);
                }
            }
        }
    }
}
