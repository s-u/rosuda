//
//  PlotPrimitive.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
package org.rosuda.ibase.toolkit;

import java.awt.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** PlotPrimitive provides a generic interfrace to objects on the screen and their linkage to cases. This basic implementation supports rectangles, points and polygons (always just one per instance). A subclass of {@link BaseCanvas} is free to use its own implementation (for example optimized for speed). Default methods of {@link BaseCanvas} use an array of PlotPrimitives to display graphical objects and map them into cases of the underlying dataset. The mapping can be 1:1 (see scatterplot), 1:n (see histograms), m:1 (see maps) or m:n (see faceplots). Please note that PlotPrimitives are geometric objects, that is specified in geometry coorditates (for reasons of speed).<p>Note: current implementation checks the contents in the sequence: rectangle, point, polygon. First non-null object is taken. Marking is based on the {@link #ref} array which contains case IDs for each case corresponding to the PlotPrimitive. In x:1 case the array is simply of the length 1. Currently there is no specific constructor, meaning that every newly allocated PlotPrimitive is empty. */
public interface PlotPrimitive {
    /** checks whether the PlotPrimitive contains (or in case of a point primitive equals to) the given point.*/
    public boolean contains(int x, int y);

    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    public boolean intersects(Rectangle rt);

    /** returns the main color of the primitive (this is usually the fill color for primitives that have both fill and draw colors */
    public Color getColor();

    /** paint the primitive (w/o selection) */
    public void paint(PoGraSS g, int orientation);

    /** paint the selection */
    public void paintSelected(PoGraSS g, int orientation, SMarker m);

    /** sets mark for cases represented by this PlotPrimitive in following fashion: if the case is already marked then its mark is set to setTo, otherwise the mark is set to 1. */
    public void setMark(SMarker m, boolean setTo);

    /** calculates the proportion of cases with the given mark in relation to total population of cases represented by this PlotPrimitive. The value ranges between 0.0 (no cases with such mark) to 1.0 (all cases with that mark) */
    public double getMarkedProportion(SMarker m, int mark);

    public int[] getCaseIDs();
    public int getPrimaryCase();
    public boolean representsCase(int cid);
    public int cases();
}

