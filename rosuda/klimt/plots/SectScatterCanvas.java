//
//  SectScatterCanvas.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.klimt.plots;

import java.awt.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.plots.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;
import org.rosuda.pograss.*;
import org.rosuda.klimt.*;

public class SectScatterCanvas extends ScatterCanvas {
    SNode paint_cn;
    NodeMarker nm;

    public SectScatterCanvas(Frame f, SVar v1, SVar v2, SMarker mark, NodeMarker nm) {
        super(f,v1,v2,mark);
        this.nm=nm;
    }

    /** paints partitioning for a single node (and descends recursively) */
    public void paintNode(PoGraSS g, SNode n, int x1, int y1, int x2, int y2, boolean sub) {
        if (n.tmp==2) {
            g.setColor("selBg");
            g.fillRect(x1,y1,x2-x1,y2-y1);
        } else {
            if (shading && (n.splitVar==v[0] || n.splitVar==v[1])) {
                int level=255-n.getLevel()*16; if (level<128) level=128;
                g.setColor(level,level,level);
                g.fillRect(x1,y1,x2-x1,y2-y1);
            }
        }
        g.setColor("splitRects");
        g.drawRect(x1,y1,x2-x1,y2-y1);
        if (n.isLeaf() || n.isPruned() || (bgTopOnly && n==paint_cn)) return;
        for(Enumeration e=n.children();e.hasMoreElements();) {
            SNode c=(SNode)e.nextElement();
            int nx1=x1, nx2=x2, ny1=y1, ny2=y2;
            if (c.splitVar==v[0]) {
                if (!c.splitVar.isCat()) {
                    int spl=A[0].getValuePos(c.splitValF);
                    if (c.splitComp==-1) nx2=spl;
                    if (c.splitComp==1) nx1=spl;
                }
            }
            if (c.splitVar==v[1]) {
                if (!c.splitVar.isCat()) {
                    int spl=A[1].getValuePos(c.splitValF);
                    if (c.splitComp==-1) ny1=spl;
                    if (c.splitComp==1) ny2=spl;
                }
            }
            paintNode(g,c,nx1,ny1,nx2,ny2,(n.tmp==2)?true:sub);
        }
    }

    public void paintBackground(PoGraSS g) {
        SNode cn=(nm!=null)?nm.getNode():null;
        paint_cn=cn;

        if (cn!=null) {
            if (Global.DEBUG>0)
                System.out.println("SectScatterCanvas: current node present, constructing partitions");
            ((SNode)cn.getRoot()).setAllTmp(0);
            SNode t=cn;
            t.tmp=2;
            while (t.getParent()!=null) {
                t=(SNode)t.getParent();
                t.tmp=1;
            };
            paintNode(g,t,X,Y,X+W,Y+H,false);
        }
    }
}
