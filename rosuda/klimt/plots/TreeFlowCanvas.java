//
//  TreeFlowCanvas.java
//  Klimt
//
//  Created by Simon Urbanek on Mon Jul 21 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.klimt.plots;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;
import org.rosuda.klimt.*;

public class TreeFlowCanvas extends PGSCanvas implements Dependent {
    SNode roots[];
    int lastw,lasth;

    int[] levl; // # of vars per level (max 64)
    SVar[] vg;  // 64*32 matrix of vars (l*32+i)
    int ls;     // # of levels

    public TreeFlowCanvas(TFrame f, SNode[] trees) {
        super(2);
        setFrame(f);
        roots=trees;

        levl=new int[64];
        vg=new SVar[64*32];

        int t=0;
        while (t<roots.length) {
            SNode n=roots[t];
            System.out.println("Tree#"+t);
            if (n!=null) recDown(n,0);
            t++;
        }
        ls++;
        System.out.println("Total "+ls+" levels.");
        int i=0;
        while (i<ls) {
            System.out.print("l="+i+": ");
            int j=0;
            while (j<levl[i]) {
                System.out.print("["+vg[j+i*32]+"] ");
                j++;
            }
            System.out.println();
            i++;
        }
    }

    public void recDown(SNode n, int l) {
        System.out.println("recDown("+n+","+l+")");
        int chs=n.count(), ct=0;
        if (chs>0) {
            SNode ln=(SNode)n.at(0);
            SVar sv=ln.splitVar;
            int j=0;
            boolean found=false;
            while (j<levl[l]) {
                if (vg[j+l*32]==sv) { found=true; break; }
                j++;
            }
            System.out.println("found="+found);
            if (!found) {
                vg[levl[l]+l*32]=sv;
                levl[l]++;
            }
            if (l>ls) ls=l;
        }
        while (ct<chs) {
            recDown((SNode)n.at(ct++),l+1);
        }
    }
    
    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        repaint();
    }

    int w,h;
    
    public void paintPoGraSS(PoGraSS g) {
        Dimension Dsize=getSize();
        w=Dsize.width; h=Dsize.height;

        g.setBounds(w,h);
        g.begin();
        g.defineColor("fill",255,255,255);
        g.defineColor("outline",0,0,0);
        g.defineColor("red",255,0,0);
        g.defineColor("labels",0,0,64);
        g.setColor("outline");

        int yspc=h/(ls+1);
        int y=yspc/2;
        int i=0;
        while (i<ls) {
            int vw=w/(levl[i]+1);
            int spc=(w-(vw*levl[i]))/(levl[i]+1);
            int j=0;
            int x=spc;
            while (j<levl[i]) {
                g.drawLine(x,y,x+vw,y);
                g.drawString(vg[i*32+j].getName(),x,y+15);
                x+=vw+spc;
                j++;
            }
            System.out.println();
            i++;
            y+=yspc;
        }

        int t=0;
        while (t<roots.length) {
            SNode n=roots[t];
            if (n!=null) drawNode(g,n,w/2,0,0);
            t++;
        }
        
        g.end();
    }

    public void drawNode(PoGraSS g, SNode n, int ox, int oy, int l) {
        int chs=n.count();
        if (chs<1) return;
        SNode cn=(SNode)n.at(0);
        SVar sv=cn.splitVar;
        int yspc=h/(ls+1);
        int y=yspc*l+yspc/2;
        int x=ox;
        int i=0;
        while (i<levl[l]) {
            if (vg[l*32+i]==sv) {
                int vw=w/(levl[l]+1);
                int spc=(w-(vw*levl[l]))/(levl[l]+1);
                x=spc+(vw+spc)*i;
                if (sv.isNum()) {
                    double sl=cn.splitValF;
                    double vr=sv.getMax()-sv.getMin();
                    int sp=(int)(((sl-sv.getMin())/vr)*((double)vw));
                    x+=sp;
                    g.drawLine(x,y-5,x,y+5);
                    g.drawLine(ox,oy,x,y);
                }
                break;
            }
            i++;
        }
        int ch=0;
        while (ch<chs) {
            drawNode(g,(SNode)n.at(ch++),x,y,l+1);
        }
    }
}
