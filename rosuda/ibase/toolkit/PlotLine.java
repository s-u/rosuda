// $Id$

package org.rosuda.ibase.toolkit;

import org.rosuda.pograss.*;
/** PlotLine is a PlotObject implementing a singe line. */
public class PlotLine extends PlotObject {
    /** coordinates of the two points (x1,y1)-(x2,y2) defining the line */
    double x1,y1,x2,y2;

    /** creates a new line */
    public PlotLine(PlotManager p) { super(p);}

    /** set both points */
    public void set(int X1, int Y1, int X2, int Y2) {
	set((double)X1,(double)Y1,(double)X2,(double)Y2);
    }

    /** set both points */
    public void set(double X1, double Y1, double X2, double Y2) {
	x1=X1; y1=Y1; x2=X2; y2=Y2;
    }

    /** draw the line */
    public void draw(PoGraSS g) {
        if (cold!=null) cold.use(g);
        g.drawLine(getXPos(x1),getYPos(y1),getXPos(x2),getYPos(y2));
    }

    /** textual info about the line */
    public String toString() {
        return "PlotLine("+x1+":"+y1+"-"+x2+":"+y2+",coord="+coordX+"/"+coordY+")";
    }
}
