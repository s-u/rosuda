//
//  MapSegment.java
//  Klimt
//
//  Created by Simon Urbanek on Tue Nov 19 2002.
//  $Id$
//

package org.rosuda.ibase;

import java.util.Vector;
import org.rosuda.util.Tools;

public class MapSegment {
    Vector ents;
    boolean isFirst;
    public double minX, minY, maxX, maxY;
    
    public MapSegment() {
        ents=new Vector();
        isFirst=true;
    }

    public void add(double[] xpt, double[] ypt) {
        MapSegmentEntry mse=new MapSegmentEntry(xpt,ypt,false,true);
        add(mse);
    }

    public void add(double[] xpt, double[] ypt,boolean isLake) {
        MapSegmentEntry mse=new MapSegmentEntry(xpt,ypt,isLake,true);
        add(mse);
    }

    public void add(MapSegmentEntry mse) {
        ents.addElement(mse);
        if (mse!=null) {
            if (isFirst) {
                minX=mse.minX; maxX=mse.maxX; minY=mse.minY; maxY=mse.maxY;
                isFirst=false;
            } else {
                if (mse.minX<minX) minX=mse.minX;
                if (mse.minY<minY) minY=mse.minY;
                if (mse.maxX>maxX) maxX=mse.maxX;
                if (mse.maxY>maxY) maxY=mse.maxY;
            }
        }
    }

    public int count() {
        return ents.size();
    }

    public MapSegmentEntry at(int i) {
        return (MapSegmentEntry) ents.elementAt(i);
    }

    public int getSizeAt(int i) {
        MapSegmentEntry mse=(MapSegmentEntry)ents.elementAt(i);
        return (mse==null)?0:mse.xp.length;
    }
    
    public double[] getXat(int i) {
        MapSegmentEntry mse=(MapSegmentEntry)ents.elementAt(i);
        return (mse==null)?null:mse.xp;
    }

    public double[] getYat(int i) {
        MapSegmentEntry mse=(MapSegmentEntry)ents.elementAt(i);
        return (mse==null)?null:mse.yp;
    }

    public boolean isLakeAt(int i) {
        MapSegmentEntry mse=(MapSegmentEntry)ents.elementAt(i);
        return (mse==null)?false:mse.isLake;
    }

    public int[] getXtransAt(int id, double scale, double off, int poff) {
        MapSegmentEntry mse=(MapSegmentEntry)ents.elementAt(id);
        if (mse==null) return null;
        int[] xs=new int[mse.xp.length];
        int i=0;
        while (i<mse.xp.length) { xs[i]=poff+(int)((mse.xp[i]+off)*scale); i++; };
        return xs;
    }

    public int[] getYtransAt(int id, double scale, double off, int poff) {
        MapSegmentEntry mse=(MapSegmentEntry)ents.elementAt(id);
        if (mse==null) return null;
        int[] ys=new int[mse.yp.length];
        int i=0;
        while (i<mse.yp.length) { ys[i]=poff+(int)((mse.yp[i]+off)*scale); i++; };
        return ys;
    }
    
    public String toString() {
        return "Map segment ("+Tools.getDisplayableValue(minX)+","+Tools.getDisplayableValue(minY)+
        ")-("+Tools.getDisplayableValue(maxX)+","+Tools.getDisplayableValue(maxY)+
        ")";
    }
}
