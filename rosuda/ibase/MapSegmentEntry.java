//
//  MapSegmentEntry.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$

package org.rosuda.ibase;

/** MapSegmentEntry encapsulates individual polygons in a map segment. They are specified by the coordinates of the points along the path. Each map segment may consist of multiple segment entries. */
public class MapSegmentEntry {
    /** coordinates of the polygon points */
    public double xp[], yp[];
    /** flag specifying whether this polygon is a "lake" */
    public boolean isLake;
    /** flag specifying whether this polygon has a border */
    public boolean hasBorder;

    /** extents of the polygon (bounding box) */
    public double minX, maxX, minY, maxY;

    /** create a new, empty map segment entry. */
    public MapSegmentEntry() {
        isLake=false;
        hasBorder=true;
    }

    /** designated constructor of a map segment entry. Note that xpt.length specifies the number of points used. If xpt.length>ypt.length then ypt is padded with zeros.
        @param xpt x-coordinates of the points (length of this array specifies the number of points of this entry)
        @param ypt y-coordinates of the points (padded with 0 if too short)
        @param lake flag specifying whether this polygon is a lake
        @param border border flag of this segment entry */
    public MapSegmentEntry(double[] xpt, double[] ypt, boolean lake, boolean border) {
        isLake=lake; hasBorder=border;
        xp=new double[xpt.length]; yp=new double[xpt.length];
        int i=0;
        while(i<xpt.length) {
            xp[i]=xpt[i];
            yp[i]=(i<ypt.length)?ypt[i]:0;
            if (i==0) { minX=maxX=xpt[0]; minY=maxY=ypt[0]; };
            if (xpt[i]<minX) minX=xpt[i];
            if (xpt[i]>maxX) maxX=xpt[i];
            if (ypt[i]<minY) minY=ypt[i];
            if (ypt[i]>maxY) maxY=ypt[i];
            i++;
        }
    }

    /** re-calculate bounding box. Call this method after modifying the {@link #xp} or {@link #yp} arrays. */
    public void fixBoundingBox() {
        int i=0;
        minX=maxX=minY=maxY=0;
        while(i<xp.length) {
            if (i==0) { minX=maxX=xp[0]; minY=maxY=yp[0]; };
            if (xp[i]<minX) minX=xp[i];
            if (xp[i]>maxX) maxX=xp[i];
            if (yp[i]<minY) minY=yp[i];
            if (yp[i]>maxY) maxY=yp[i];
            i++;
        }
    }
}
