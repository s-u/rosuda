//
//  PlotPrimitive.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** PlotPrimitive provides a generic interfrace to objects on the screen and their linkage to cases. This basic implementation supports rectangles, points and polygons (always just one per instance). A subclass of {@link BaseCanvas} is free to use its own implementation (for example optimized for speed). Default methods of {@link BaseCanvas} use an array of PlotPrimitives to display graphical objects and map them into cases of the underlying dataset. The mapping can be 1:1 (see scatterplot), 1:n (see histograms), m:1 (see maps) or m:n (see faceplots). Please note that PlotPrimitives are geometric objects, that is specified in geometry coorditates (for reasons of speed).<p>Note: current implementation checks the contents in the sequence: rectangle, point, polygon. First non-null object is taken. Marking is based on the {@link #ref} array which contains case IDs for each case corresponding to the PlotPrimitive. In x:1 case the array is simply of the length 1. Currently there is no specific constructor, meaning that every newly allocated PlotPrimitive is empty. */
public class PlotPrimitive {
    public Rectangle r;
    public Polygon pg;
    public Point pt;
    public Color col;

    /** references to cases represented by this primitive */
    public int[] ref;

    public String toString() {
        return "PlotPrimitive["+r+"/"+pt+"/"+pg+"]-"+ref;
    }

    /** checks whether the PlotPrimitive contains (or in case of a point primitive equals to) the given point.*/
    public boolean contains(int x, int y) {
        if (r!=null) return r.contains(x,y);
        if (pt!=null) return (pt.getX()==x && pt.getY()==y);
        if (pg!=null) return pg.contains(x,y);
        return false;
    }

    /** checks whether the PlotPrimitive intersects (or is contained) in the given rectangle. */
    public boolean intersects(Rectangle rt) {
        if (r!=null) return r.intersects(rt);
        if (pt!=null) return rt.contains(pt);
        if (pg!=null) return pg.intersects(rt);
        return false;
    }

    /** sets mark for cases represented by this PlotPrimitive in following fashion: if the case is already marked then its mark is set to setTo, otherwise the mark is set to 1. */
    public void setMark(SMarker m, boolean setTo) {
        if (ref!=null) {
            int j=0, pts=ref.length;
            while (j<pts) {
                m.set(ref[j],m.at(ref[j])?setTo:true);
                j++;
            }
        }
    }

    /** calculates the proportion of cases with the given mark in relation to total population of cases represented by this PlotPrimitive. The value ranges between 0.0 (no cases with such mark) to 1.0 (all cases with that mark) */
    public double getMarkedProportion(SMarker m, int mark) {
        Stopwatch sw=new Stopwatch();
        if (ref!=null) {
            int j=0, pts=ref.length;
            if (pts==0) return 0d;
            int sc=0;
            while (j<pts) {
                if (m.get(ref[j])==mark) sc++;
                j++;
            }
            sw.profile("PlotPrimitive.getMarkedProportion");
            return ((double)sc)/((double)pts);
        }
        return 0d;
    }

    public int[] getMarkedList(SMarker m) { // [total][count mark 0][mark][count][mark][count]...
        if (m==null) return null;
        if (ref!=null) {
            if (ref.length<1) return null;
            if (ref.length==1) {
                int mark=m.get(ref[0]);
                int[] lst=new int[(mark!=0)?4:2];
                lst[0]=1;
                lst[1]=(mark==0)?1:0;
                if (mark!=0) {
                    lst[2]=mark;
                    lst[3]=1;
                }
                return lst;
            }
            int pts=ref.length;
            int j=0;
            int[] cts=new int[m.getMaxMark()+2];
            while (j<pts) {
                int mark=m.get(ref[j]);
                mark++;
                if (mark<cts.length) cts[mark]++;
                j++;
            }
            int ums=0;
            j=2;
            while(j<cts.length) { if (cts[j++]>0) ums++; };
            int lst[]=new int[3+(ums*2)];
            lst[0]=pts;
            lst[1]=cts[1];
            j=0;
            int k=2;
            while(j<cts.length) {
                if (j!=1 && cts[j]>0) {
                    lst[k++]=j-1; lst[k++]=cts[j];
                }
                j++;
            }
            return lst;
        }
        return null;
    }

    public Color getColor() { return col; }
}

