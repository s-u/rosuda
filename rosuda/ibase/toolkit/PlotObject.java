package org.rosuda.ibase.toolkit;

import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** Plot objects are basic building blocks to add graphical objects to iPlots.
This class provides an basis on which all other plot objects must be based.
<p>
Some important notes for developers designing plot objects:<ul>
<li>Read the documentation of {@link #draw} and follow the rules stated there.</li>
<li>Don't use any update commands in your code. Plot objects must be designed to allow batch changes, therefore updates within the object code are unwanted.</li></ul>*/
public class PlotObject {
    /** coordinates system: absolute display coordinates (i.e. no transformation) */
    public static final int CS_ABS = 0;
    /** coordinates system: variable space (i.e. {@link Axis} objects are used for direct transformation) */
    public static final int CS_VAR = 1;
    /** coordinates system: relative. 0 corresponds to the left/top edge of the data space and 1 corresponds to the right/bottom edge. */
    public static final int CS_REL = 2;
    
	/** internal flag for NaN in integer context */
	static final int INaN = -2147483648;
	
    /** plot manager - the container containing this object. Any object can be owned by one plot manager only. */
    PlotManager pm;
    /** visibility flag. if set to <code>false</code> then this object is not painted */
    boolean visible=true;
    /** clipping flag. if set to <code>true</code> then this object should make sure to not plot outside the data area (see {@link Axis}) */
    boolean clip=false;
    /** coordinate system for the X values (see CS_xxx constants). */
    int coordX=1; // 0=abs-geom, 1=var.space, 2=relative geom. (-1=min, 1=max)
    /** coordinate system for the Y values (see CS_xxx constants). */
    int coordY=1;
    /** draw color of the object */
    PlotColor cold;
    /** fill color of the object */
    PlotColor colf;
    /** layer of this object (0=in the back, -1=on the top (last layer), 1,2,3,... specific layer) */
    int layer=-1;

    public PlotObject(final PlotManager p) { pm=p; p.add(this); }

    public void dispose() {
	pm.rm(this);
	pm=null;
	cold=null;
	colf=null;
    }

    /** all subclasses should override this method.<br>
        Some points to keep in mind when implementing new objects:<ul>
        <li>use {@link #getXPos} and {@link #getYPos} to map to screen coordinates whenever possible.</li>
        <li>respect the {@link #clip} flag. {@link #getXPos} and {@link #getYPos} DON'T perform clipping. This is on purpose to avoid mistakes where clipping one coordinate influences the other one (compare clipping of a rectangle (independent) and a line (dependent))</li>
        <li>respect {@link col} attribute if relevant. You're free to define other colors, but there should be one main color which is set by {@link #setColor}.</li>
        <li>don't use any update medthods, such as repaint.</li>
        </ul>
        @param g graphics context for painting
        */
    public void draw(final PoGraSS g) {
    }

    /** set draw color of the object. The interpretation of the color is object-dependent, but simple objects should adhere to the reasoning that this color is used for drawing lines.
        @param c color to use
        */
    public void setDrawColor(final PlotColor c) { cold=c; }

    /** set fill color of the object. The interpretation of the color is object-dependent, but simnple objects should adhere to the reasoning that this color is used for area filling.
        @param c color to use
        */
    public void setFillColor(final PlotColor c) { colf=c; }
    
    /** visibility
        @ return <code>true</code> if the object is visible */
    public boolean isVisible() { return visible; }
    /** set visibility. if set to <code>false</code> the object won't be painted
        */
    public void setVisible(final boolean vis) {
        if (Global.DEBUG>0)
            System.out.println("Setting visibility to \""+vis+"\", was "+visible+" ["+toString()+"]");
        visible=vis;
    }
    /** set a common coordinate system for both axes (see CS_xxx constants for details)
        @param ct coordinate system specification - one of the CS_xxx constants */
    public void setCoordinates(final int ct) { coordX=coordY=ct; };
    /** set one coordinate system for each axis separately
        @param cx coordinate system specification for the X axis - must be one of the CS_xxx constants
        @param cy coordinate system specification for the Y axis - must be one of the CS_xxx constants
        */
    public void setCoordinates(final int cx, final int cy) { coordX=cx; coordY=cy; };

    /** maps plot object X coordinate to a graphical coordinate which can be used in the {@link #draw} method.
        @param v value to convert
        @return corresponding value of the graphical X coordinate */
    public int getXPos(final double v) {
        if (coordX==0) return (int)v;
        final Axis a=pm.getXAxis();
        if (a==null) return 0;
        return (coordX==1)?a.getValuePos(v):a.gBegin+(int)((a.gLen)*v);
    }

    /** maps plot object Y coordinate to a graphical coordinate which can be used in the {@link #draw} method.
        @param v value to convert
        @return corresponding value of the graphical X coordinate */
    public int getYPos(final double v) {
        if (coordY==0) return (int)v;
        final Axis a=pm.getYAxis();
        if (a==null) return 0;
        return (coordY==1)?a.getValuePos(v):a.gBegin+(int)((a.gLen)*v);
    }

    /** set clipping flag. if set to <code>true</code> then the object shouldn't plot outside the data area.
        @param cl clipping flag */
    public void setClip(final boolean cl) { clip=cl; }

    /** get drawing color
        @return drawing color of the object */
    public PlotColor gerDrawColor() { return cold; }

    /** get fill color
        @return fill color of the object */
    public PlotColor gerFillColor() { return colf; }

    /** move the object to another layer */
    public void setLayer(final int l) {
	layer=l;
    }

    public int getLayer() { return layer; }

    /** causes the entire plot object system to be updated (redraws only the layer of this object and above). by default is simply calls the {@link PlotManager.update()} method with the layer of the object. */
    public void update() {
        if (Global.DEBUG>0)
            System.out.println("["+toString()+"] initiated update at layer "+layer);
        pm.update(layer);
    }

    /** toString is rather useful for debugging purposes */
    public String toString() {
        return "PlotObject(co="+coordX+":"+coordY+
        ",dc="+((cold==null)?"none":cold.toString())+
        ",fc="+((colf==null)?"none":colf.toString())+")";
    }
}
