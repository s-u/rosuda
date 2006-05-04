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


/** PlotPrimitive provides a generic interfrace to objects on the screen and their linkage to cases. A subclass of {@link BaseCanvas} is free to use its own implementation (for example optimized for speed). Default methods of {@link BaseCanvas} use an array of PlotPrimitives to display graphical objects and map them into cases of the underlying dataset. The mapping can be 1:1 (see scatterplot), 1:n (see histograms), m:1 (see maps) or m:n (see faceplots). Please note that PlotPrimitives are geometric objects, that is their position is specified in geometry coorditates (for speed).
<p>
**  Since 10/2003 PlotPrimitive is just an interface. Basic linking capabilities are implemented by the PPrimBase class, real graphical representations are provided by its subclasses PPrimRectangle and PPrimPolygon.

@version $Id$
*/
public interface PlotPrimitive extends Queryable {
    /** checks whether the PlotPrimitive contains (or in case of a point primitive equals to) the given point.*/
    boolean contains(int x, int y);

    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    boolean intersects(Rectangle rt);

    /** returns the main color of the primitive (this is usually the fill color for primitives that have both fill and draw colors */
    Color getColor();

    /** paint the primitive (w/o selection) */
    void paint(PoGraSS g, int orientation, SMarker m);

    /** paint the selection */
    void paintSelected(PoGraSS g, int orientation, SMarker m);

    /** sets mark for cases represented by this PlotPrimitive in following fashion: if the case is already marked then its mark is set to setTo, otherwise the mark is set to 1. */
    void setMark(SMarker m, boolean setTo);

    /** calculates the proportion of cases with the given mark in relation to total population of cases represented by this PlotPrimitive. The value ranges between 0.0 (no cases with such mark or cases=0) to 1.0 (all cases with that mark) */
    double getMarkedProportion(SMarker m, int mark);

    // in order to provide easy transition from the old PlotPrimitive class, we support linked lists by default.
    // But it is unclear if this interface will me moved out of the class at some point.
    // By design PlotPrimitive should not care about the way the linking is done - implementations should, therefore it is possible that this approach will be removed in the future

    /** returns the IDs of all cases represented by this primitive. <code>null</code> is valid and means that this primitive represents no data
        @return list of case IDs */
    int[] getCaseIDs();

    /** returns the case ID for 1:1 (or m:1) relationships
        @return if exactly one case is associated with this primitive, then its case ID is returned. Otherwise -1 is returned. */
    int getPrimaryCase();

    /** checks whether the specified case is represented by this primitive.
        @param cid case ID to check for
        @return <code>true</code> if the case is represented by this primitive */
    boolean representsCase(int cid);

    /** returns the number of cases this primitive represents
        @return number of cases this primitive represents */
    int cases();
    
    /** sets visibility of this primitive */
    void setVisible(boolean b);

    /** returns whether this primitive is visible */
    boolean isVisible();
}

