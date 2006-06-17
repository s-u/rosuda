//
//  PPrimBase.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Oct 29 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.*;

import org.rosuda.ibase.*;
import org.rosuda.util.*;

/** Very basic plot primitive without any graphical representation, but with case handling. This abstract class can be used as a base for any plot primitives that use ID reference lists. Methods concerning graphical representation (paint..., contains, intersects) are left abstract.
*/
public abstract class PPrimBase implements PlotPrimitive {
    /**
     * @deprecated Should be removed as it isn't used anymore.
     * Left here for compatibility reasons.
     */
    public Color col;
    public Color COL_OUTLINE = Color.BLACK;
    
    /** references (IDs) to cases represented by this primitive */
    public int[] ref;

    protected boolean visible=true;

    protected boolean dragging = false;
    
    /**
     * whether this primitive can be queried
     */
    public boolean queryable=true;

    public Color fillColor = Common.objectsColor;
    public Color borderColor = Color.BLACK;
    public Color fillColorSel = Common.selectColor;
    public Color borderColorSel = Common.selectColor;
    
    public Color fillColorDrag = new Color(128,128,128,70);

    public boolean performAlphaBlending = true;

    public String toString() {
        return "PPrimBase["+cases()+" cases]";
    }

    public int[] getCaseIDs() { return ref; }
    public void setCaseIDs(int[] r) { ref=r; }
    public void setCaseIDs(double[] r) {
    	if(r==null) return;
    	int[] ri=new int[r.length];
    	for(int i=0;i<ri.length;i++) ri[i]=(int)r[i];
    	ref=ri;
    }
    public int getPrimaryCase() { return (ref==null||ref.length!=1)?-1:ref[0]; }
    public boolean representsCase(final int cid) {
        if (ref==null || ref.length<1) return false;
        if (ref.length==1) return ref[0]==cid;
        int i=0;
        while (i<ref.length) { if (ref[i++]==cid) return true; }
        return false;
    }

    public int cases() { return (ref==null)?0:ref.length; }
    
    /** sets mark for cases represented by this PlotPrimitive in following fashion: if the case is already marked then its mark is set to setTo, otherwise the mark is set to true. */
    public void setMark(final SMarker m, final boolean setTo) {
        if (ref!=null) {
            int j=0;
            final int pts=ref.length;
            while (j<pts) {
                m.set(ref[j],m.at(ref[j])?setTo:true);
                j++;
            }
        }
    }
    
    public double getMarkedProportion(final SMarker m, final int mark) {
        return getMarkedProportion(m,mark,false);
    }

    /** calculates the proportion of cases with the given mark in relation to total population of cases represented by this PlotPrimitive. The value ranges between 0.0 (no cases with such mark) to 1.0 (all cases with that mark) */
    public double getMarkedProportion(final SMarker m, final int mark, final boolean dropPrimary) {
        if (ref!=null && m!=null) {
            
            int j=0;
            final int pts=ref.length;
            if (pts==0) return 0d;
            int sc=0;
            while (j<pts) {
                if ((mark==-1 && m.get(ref[j])==mark) || (mark>-1 && m.getSec(ref[j])==mark) && (dropPrimary?(m.get(ref[j])!=-1):(true))) sc++;
                j++;
            }
            final Stopwatch sw = new Stopwatch();
            sw.profile("PlotPrimitive.getMarkedProportion");
            return ((double)sc)/((double)pts);
        }
        return 0d;
    }

    /** returns the main color of the primitive */
    public Color getColor() { return col; }
    
    /**
     * Taken from org.rosuda.Mondrian.Util (CVS version 1.5).
     */
    int[] roundProportions(double[] votes, double total, int pie) {
        
        int[] rounds = new int[votes.length];
        
        int start = -1;
        int stop  = votes.length;
        while( votes[++start] == 0 ) {}
        while( votes[--stop]  == 0 ) {}
    //    System.out.println("Start: "+start+" Stop: "+stop);
        int k=1;
        double eps=0;
        int sum=0;
        int converge=24;
        while( sum != pie && k<64) {
            k++;
            sum=0;
            for(int i=start; i<=stop; i++) {
                if( k>=converge )
                    eps = Math.random() - 0.5;
                if( votes[i] < 0.0000000001 )
                    rounds[i] = 0;
                else
                    rounds[i] = (int)Math.round((double)(votes[i])/total*pie + eps);
                sum += rounds[i];
            }
            //System.out.println("k: "+k+" eps: "+eps+" sum: "+sum+" pie: "+pie);
            if( sum > pie )
                eps -= 1/Math.pow(2,k);
            else if( sum < pie )
                eps += 1/Math.pow(2,k);
        }
        if( sum != pie )
            System.out.println(" Rounding Failed !!!");
        
        return rounds;
    }

    public boolean isQueryable() {
        return queryable;
    }
    
    protected double getRelativeMarkedProportion(SMarker m, int mark) {
        double total=0;
        double selected=0;
        
        for(int i=0; i<ref.length; i++){
            if(m.getSec(ref[i])==mark){
                total++;
                if(m.get(ref[i])==-1) selected++;
            }
        }
        return ((total==0)?0:selected/total);
    }
    
    protected int getPropSize(int totalSize, double proportion) {
        int ret = (int)Math.round(totalSize*proportion);
        if(ret==0 && proportion>0) ret=1;
        else if(ret==totalSize && proportion<1) ret=totalSize-1;
        return ret;
    }

    public void setVisible(boolean b) {
        visible=b;
    }

    public boolean isVisible() {
        return visible;
    }
    
    public void setDragging(final boolean b){
        dragging=b;
    }
    
    public boolean isDragging(){
        return dragging;
    }
    
    // move methods. not abstract as PPrimPolygon has no anchor point to move
    public void move(final int x, final int y){};
    public void moveX(final int x){};
    public void moveY(final int y){};
    
    public boolean hilitcontains(int x, int y) {
    	return false;
    }

    public boolean isPerformingAlphaBlending() {
        return performAlphaBlending;
    }
}
