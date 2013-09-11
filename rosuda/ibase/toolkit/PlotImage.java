package org.rosuda.ibase.toolkit;

import org.rosuda.pograss.*;

/** PlotLine is a PlotObject implementing a bitmap image */
public class PlotImage extends PlotObject {
	/** coordinates of the two points (x1,y1)-(x2,y2) defining the corners */
	double x1, y1, x2, y2;
	/** image */
	PoGraSSimage img;
	
	/** creates a new image */
	public PlotImage(final PlotManager p) { super(p);}
	
	public void setImage(PoGraSSimage img) {
		this.img = img;
	}
	
	public void setImage(byte[] payload) {
		try {
			java.io.ByteArrayInputStream inp = new java.io.ByteArrayInputStream(payload);
			this.img = new PoGraSSimage(inp);
		} catch (java.io.IOException e) { // FIXME: should we throw this?
			System.err.println("WARNING: cannot create image from bytes: " + e);
		}
	}
	
	public void setImage(String filename) {
		try {
			this.img = new PoGraSSimage(filename);
		} catch (java.io.IOException e) { // FIXME: should we throw this?
			System.err.println("WARNING: cannot create image from file '" + filename + "': " + e);
		}
	}
	
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
		if (cold != null) cold.use(g);
		// orientation matters and the image is y-"flipped" relative to the
		// graphics coordinates so we have to go from y2->y1
		g.drawImage(img, getXPos(x1), getYPos(y2), getXPos(x2), getYPos(y1));
	}
	
	/** textual info about the line */
	public String toString() {
		return "PlotImage("+x1+":"+y1+"-"+x2+":"+y2+",coord="+coordX+"/"+coordY+")";
	}
}
