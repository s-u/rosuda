// $Id$

package org.rosuda.ibase.toolkit;

import org.rosuda.pograss.*;
/** PlotLine is a PlotObject implementing a singe line. */
public class PlotLine extends PlotObject {
    /** coordinates of the two points (x1,y1)-(x2,y2) defining the line */
    double x1,y1,x2,y2;

    /** creates a new line */
    public PlotLine(final PlotManager p) { super(p);}

    /** set both points */
    public void set(final int X1, final int Y1, final int X2, final int Y2) {
	set(X1,Y1,X2,Y2);
    }

    /** set both points */
    public void set(final double X1, final double Y1, final double X2, final double Y2) {
	x1=X1; y1=Y1; x2=X2; y2=Y2;
    }

    /** draw the line */
    public void draw(final PoGraSS g) {
        if (cold!=null) cold.use(g);
        g.drawLine(getXPos(x1),getYPos(y1),getXPos(x2),getYPos(y2));
    }

    /** textual info about the line */
    public String toString() {
        return "PlotLine("+x1+":"+y1+"-"+x2+":"+y2+",coord="+coordX+"/"+coordY+")";
    }
}
