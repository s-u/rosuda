//
//  MapSegment.java
//  Klimt
//
//  Created by Simon Urbanek on Tue Nov 19 2002.
//  $Id$
//

import java.util.Vector;

class MapSegmentEntry {
    public double xp[], yp[];
    public boolean isLake;
    public boolean hasBorder;

    public double minX, maxX, minY, maxY;
    
    public MapSegmentEntry() {
        isLake=false;
        hasBorder=true;
    }

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
        };
    }
}

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

    public int[] transViaAxisX(int id, Axis a) {
        MapSegmentEntry mse=(MapSegmentEntry)ents.elementAt(id);
        if (mse==null) return null;
        int[] xs=new int[mse.xp.length];
        int i=0;
        while (i<mse.xp.length) { xs[i]=a.getValuePos(mse.xp[i]); i++; };
        return xs;
    }

    public int[] transViaAxisY(int id, Axis a) {
        MapSegmentEntry mse=(MapSegmentEntry)ents.elementAt(id);
        if (mse==null) return null;
        int[] ys=new int[mse.yp.length];
        int i=0;
        while (i<mse.yp.length) { ys[i]=a.getValuePos(mse.yp[i]); i++; };
        return ys;
    }

    public String toString() {
        return "Map segment ("+Tools.getDisplayableValue(minX)+","+Tools.getDisplayableValue(minY)+
        ")-("+Tools.getDisplayableValue(maxX)+","+Tools.getDisplayableValue(maxY)+
        ")";
    }
}
