//
//  AxisCatSequence.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jun 04 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$

package org.rosuda.ibase.toolkit;

import java.util.Vector;
import org.rosuda.ibase.*;
import org.rosuda.util.*;

/* Special SCatSequence which caches the graphical representations as well */
public class AxisCatSequence implements Dependent {
    Axis a;
    int[] left;
    int[] right;
    int gap,cats;
    SCatSequence seq;
    
    AxisCatSequence(Axis axis, SCatSequence cs) {
        a=axis;
        seq=cs;
        seq.addDepend(this);
        axis.addDepend(this);
        updateCats();
    }

    public void Notifying(NotifyMsg msg, Object src, Vector path) {
        // the only case we can get notified is that the sequence changed
        updateCats();
    }

    void updateCats() {
        if (Global.DEBUG>0)
            System.out.println("AxisCatSequence.updateCats() [cats="+cats+"]");
        if (a.type!=Axis.T_EqCat && a.type!=Axis.T_PropCat) return;

        SVar v=a.getVariable();
        cats=seq.size();
        left=new int[cats];
        right=new int[cats];
        double sig=1;
        int isig=1;
        int i=0;
        gap=a.gap;
        if (a.gLen<0) { sig=-1.0; isig=-1; }
        int tl=a.gLen-(isig*gap*(cats-1));
        if (tl*a.gLen<0) {
            while (gap>0 && tl*a.gLen<0) {
                gap--;
                tl=a.gLen-(isig*gap*(cats-1));
            }
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: updateCats: not enough space for categories ("+cats+") if gap ("+a.gap+") is respected. Adjusting gap to "+gap);
        }

        boolean equal=(a.type==Axis.T_EqCat);

        double running=(double)a.gBegin;
        double dtl=(double)tl;
        double all=equal?((double)cats):((double)v.size());
        gap*=isig;
        while (i<cats) {
            double cs=
            equal?
            (double)1:
            (double)v.getSizeCatAt(seq.catAtPos(i));
            left[i]=(int)running;
            running+=dtl*(cs/all);
            right[i]=(int)running;
            running+=(double)gap;
            i++;
        }
    }

    public int getLowerEdgeOfCatAt(int p) {
        if (a.type!=Axis.T_EqCat && a.type!=Axis.T_PropCat) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getLowerEdgeOfCatAt("+p+") but Axis type is not categorical.");
        if (p<0 || p>=cats) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getLowerEdgeOfCatAt("+p+") out of range (cats="+cats+").");
        if (left==null) updateCats();
        return left[p];
    }

    public int getUpperEdgeOfCatAt(int p) {
        if (a.type!=Axis.T_EqCat && a.type!=Axis.T_PropCat) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getUpperEdgeOfCatAt("+p+") but Axis type is not categorical.");
        if (p<0 || p>=cats) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getUpperEdgeOfCat("+p+") out of range (cats="+cats+").");
        if (left==null) updateCats();
        return right[p];
    }

    public int getCenterOfCatAt(int p) {
        if (a.type!=Axis.T_EqCat && a.type!=Axis.T_PropCat) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getCenterOfCatAt("+p+") but Axis type is not categorical.");
        if (p<0 || p>=cats) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getCenterOfCatAt("+p+") out of range (cats="+cats+").");
        if (left==null) updateCats();
        return (right[p]+left[p])/2;
    }

    public int getLowerEdgeOfCat(int c) {
        if (a.type!=Axis.T_EqCat && a.type!=Axis.T_PropCat) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getLowerEdgeOfCat("+c+") but Axis type is not categorical.");
        if (c<0 || c>=cats) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getLowerEdgeOfCat("+c+") out of range (cats="+cats+").");
        if (left==null) updateCats();
        return left[seq.posOfCat(c)];
    }

    public int getUpperEdgeOfCat(int c) {
        if (a.type!=Axis.T_EqCat && a.type!=Axis.T_PropCat) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getUpperEdgeOfCat("+c+") but Axis type is not categorical.");
        if (c<0 || c>=cats) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getUpperEdgeOfCat("+c+") out of range (cats="+cats+").");
        if (left==null) updateCats();
        return right[seq.posOfCat(c)];
    }

    public int getCenterOfCat(int c) {
        if (a.type!=Axis.T_EqCat && a.type!=Axis.T_PropCat) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getCenterOfCat("+c+") but Axis type is not categorical.");
        if (c<0 || c>=cats) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getCenterOfCat("+c+") out of range (cats="+cats+").");
        if (left==null) updateCats();
        return (right[seq.posOfCat(c)]+left[seq.posOfCat(c)])/2;
    }

    public int getCatByGeometryPos(int pos) {
        if (a.type!=Axis.T_EqCat && a.type!=Axis.T_PropCat) return
            Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getCatByGeometryPos("+pos+") but Axis type is not categorical.");
        int i=0;
        if (left==null) updateCats();
        while (i<cats) {
            int lo=left[i], hi=right[i];
            if (lo>hi) { hi=lo; lo=right[i]; }
            if (lo<=pos && hi>=pos) return seq.catAtPos(i);
            i++;
        }
        Global.runtimeWarning("AxisCatSequence for Axis["+a.toString()+"]: getCatByGeometryPos("+pos+") - no category found.");
        return -1;
    }

    public String toString() {
        String s;
        s="AxisCatSequence(cats="+cats;
        int i=0;
        while (i<cats) {
            s+=","+left[i]+":"+right[i];
            i++;
        }
        return s+")";
    }
}
