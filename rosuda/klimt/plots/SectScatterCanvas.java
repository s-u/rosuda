//
//  SectScatterCanvas.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.klimt.plots;

import java.awt.*;
import java.awt.event.*;
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
    DataRoot dr;
    float alpha=0.1f;
    
    boolean paintAllTrees=true;

    public SectScatterCanvas(DataRoot dr, Frame f, SVar v1, SVar v2, SMarker mark, NodeMarker nm) {
        super(f,v1,v2,mark);
        this.nm=nm; this.dr=dr;
        nm.addDepend(this);
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

    /** paints partitioning for a single node (and descends recursively) */
    public void paintSplit(PoGraSS g, SNode n, int edge, int x1, int y1, int x2, int y2) {
        if (edge==1) g.drawLine(x1,y1,x2,y1);
        if (edge==2) g.drawLine(x2,y1,x2,y2);
        if (edge==3) g.drawLine(x1,y2,x2,y2);
        if (edge==4) g.drawLine(x1,y1,x1,y2);
        if (n.isLeaf() || n.isPruned()) return;
        for(Enumeration e=n.children();e.hasMoreElements();) {
            SNode c=(SNode)e.nextElement();
            int nx1=x1, nx2=x2, ny1=y1, ny2=y2;
            if (c.splitVar==v[0]) {
                if (!c.splitVar.isCat()) {
                    int spl=A[0].getValuePos(c.splitValF);
                    if (c.splitComp==-1) { nx2=spl; edge=2; }
                    if (c.splitComp==1) { nx1=spl; edge=4; }
                }
            }
            if (c.splitVar==v[1]) {
                if (!c.splitVar.isCat()) {
                    int spl=A[1].getValuePos(c.splitValF);
                    if (c.splitComp==-1) { ny1=spl; edge=1; }
                    if (c.splitComp==1) { ny2=spl; edge=3; }
                }
            }
            paintSplit(g,c,edge,nx1,ny1,nx2,ny2);
        }
    }

    public void keyTyped(KeyEvent e)  {
        super.keyPressed(e);
		if (e.getKeyChar()==',') {
			alpha/=2f; setUpdateRoot(0); repaint();
		}
        if (e.getKeyChar()=='.') {
            alpha*=2f; if (alpha>1f) alpha=1f; setUpdateRoot(0); repaint();
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

        if (paintAllTrees) {
            if (dr!=null) {
                TreeRegistry tr=dr.getTreeRegistry();
                if (tr!=null) {
                    SNode[] rs=tr.getRoots();
                    g.setColor(0f,0f,1f,alpha);
                    if (rs!=null) {
                        int i=0;
                        while (i<rs.length) {
                            paintSplit(g,rs[i],0,X,Y,X+W,Y+H);
                            i++;
                        }
                    }
                }
            }
        }
    }

    
}
