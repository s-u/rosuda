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
    public Color col;
    
    /** references (IDs) to cases represented by this primitive */
    public int[] ref;

    static final String COL_MARKED = "marked";

    public String toString() {
        return "PPrimBase["+cases()+" cases]";
    }

    public int[] getCaseIDs() { return ref; }
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

    /** calculates the proportion of cases with the given mark in relation to total population of cases represented by this PlotPrimitive. The value ranges between 0.0 (no cases with such mark) to 1.0 (all cases with that mark) */
    public double getMarkedProportion(final SMarker m, final int mark) {
        
        if (ref!=null && m!=null) {
            
            int j=0;
            final int pts=ref.length;
            if (pts==0) return 0d;
            int sc=0;
            while (j<pts) {
                if (m.get(ref[j])==mark) sc++;
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
        return true;
    }
}
