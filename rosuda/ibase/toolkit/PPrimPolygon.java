//
//  PPrimPolygon.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Oct 29 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** Plot primitive based on {@link PPrimBase},  using a list of IDs and implementing polygons.
@version $Id$
*/
public class PPrimPolygon extends PPrimBase {
    public Polygon pg;
    
    public boolean drawBorder=true;
    public boolean useSelAlpha=true;

    /** checks whether the PlotPrimitive contains the given point.*/
    public boolean contains(int x, int y) { return (pg==null)?false:pg.contains(x,y); }

    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    public boolean intersects(Rectangle rt) { return (pg==null)?false:pg.intersects(rt); }

    /** paint the primitive */
    public void paint(PoGraSS g, int orientation) {
        if (pg==null) return;
        if (col!=null)
            g.setColor(col.getRed(),col.getGreen(),col.getBlue());
        else
            g.setColor("object");
        g.fillPolygon(pg.xpoints,pg.ypoints,pg.npoints);
        if (drawBorder) {
            g.setColor("outline");
            g.drawPolygon(pg.xpoints,pg.ypoints,pg.npoints);
        }
    }

    public void paintSelected(PoGraSS g, int orientation, SMarker m) {
        if (pg==null) return;
        double sa=getMarkedProportion(m,-1);
        //System.out.println("pp["+i+"] sa="+sa+" "+pp);
        if (sa>0d) {
            if (useSelAlpha && sa<1.0)
                g.setColor(((float)Common.selectColor.getRed())/255.0F,
                           ((float)Common.selectColor.getGreen())/255.0F,
                           ((float)Common.selectColor.getBlue())/255.0F,(float)sa);
            else
                g.setColor("marked");
            g.fillPolygon(pg.xpoints,pg.ypoints,pg.npoints);
            if (drawBorder) {
                g.setColor("outline");
                g.drawPolygon(pg.xpoints,pg.ypoints,pg.npoints);
            }
        }
    }
    
    public String toString() {
        return "PPrimPolygon("+((pg==null)?"<null polygon>":(""+pg.npoints+" points"))+", drawBorder="+drawBorder+", useSelAlpha="+useSelAlpha+")";
    }
}
