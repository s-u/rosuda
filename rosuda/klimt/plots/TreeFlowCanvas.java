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

public class TreeFlowCanvas extends PGSCanvas implements Dependent, KeyListener {
    SNode roots[];
    int lastw,lasth;
    float alpha=0.1f;
    boolean eq=false, red=false;
    boolean tri=false;
    int xstretch=1;
    
    int[] levl; // # of vars per level (max 64)
    SVar[] vg;  // 64*32 matrix of vars (l*32+i)
    int ls;     // # of levels
    int seq=0;
    SVar[] vars;

    public TreeFlowCanvas(TFrame f, SNode[] trees) {
        super(2);
        setFrame(f);
        roots=trees;

        addKeyListener(this); f.addKeyListener(this);
        levl=new int[64];
        vg=new SVar[64*32];

        int t=0;
        while (t<roots.length) {
            SNode n=roots[t];
            if (Global.DEBUG>0) System.out.println("Tree#"+t);
            if (n!=null) recDown(n,0);
            t++;
        }
        ls++;
        if (Global.DEBUG>0) System.out.println("Total "+ls+" levels.");
        int i=0;
        seq=1;
        while (i<ls) {
            if (Global.DEBUG>0) System.out.print("l="+i+": ");
            int j=0;
            while (j<levl[i]) {
                if (Global.DEBUG>0) System.out.print("["+vg[j+i*32]+"] ");
                if (vg[j+i*32].tag==0) vg[j+i*32].tag=seq++;
                j++;
            }
            if (Global.DEBUG>0) System.out.println();
            i++;
        }
        vars=new SVar[seq];
        int sq=1;
        i=0;
        while (i<ls) {
            int j=0;
            while (j<levl[i]) {
                if (vg[j+i*32].tag>0) vars[vg[j+i*32].tag]=vg[j+i*32];
                j++;
            }
            i++;
        }
    }

    public void recDown(SNode n, int l) {
        if (Global.DEBUG>0) System.out.println("recDown("+n+","+l+")");
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
            if (Global.DEBUG>0) System.out.println("found="+found);
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
                if (eq) {
                    vw=w/seq;
                    spc=vw/seq;
                    x=(vw+spc)*(vg[i*32+j].tag-1)+spc;
                }
                g.drawLine(x*xstretch,y,(x+vw)*xstretch,y);
                g.drawString(tri?Common.getTriGraph(vg[i*32+j].getName()):vg[i*32+j].getName(),x*xstretch,y+15);
                x+=vw+spc;
                j++;
            }
            if (Global.DEBUG>0) System.out.println();
            i++;
            y+=yspc;
        }

        g.setColor(0f,0f,0f,alpha);
        int t=0;
        while (t<roots.length) {
            SNode n=roots[t];
            if (red && t==roots.length-1) g.setColor(1f,0f,0f,1f);
            if (n!=null) drawNode(g,n,w/2,0,0);
            if (red && t==roots.length-1) g.setColor(0f,0f,0f,alpha);
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
                if (eq) {
                    vw=w/seq;
                    spc=vw/seq;
                    x=(vw+spc)*(sv.tag-1)+spc;
                }
                if (sv.isNum()) {
                    double sl=cn.splitValF;
                    double vr=sv.getMax()-sv.getMin();
                    int sp=(int)(((sl-sv.getMin())/vr)*((double)vw));
                    x+=sp;
                    g.drawLine(x*xstretch,y-5,x*xstretch,y+5);
                    g.drawLine(ox*xstretch,oy,x*xstretch,y);
                } else {
                    x+=vw/2;
                    g.drawLine(x*xstretch,y-5,x*xstretch,y+5);
                    g.drawLine(ox*xstretch,oy,x*xstretch,y);
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

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_LEFT) {
            alpha/=2f; setUpdateRoot(0); repaint();
        }
        if (e.getKeyCode()==KeyEvent.VK_RIGHT) {
            alpha*=2f; if (alpha>1f) alpha=1f; setUpdateRoot(0); repaint();
        }
    }

    public void keyTyped(KeyEvent e)
    {
        if (e.getKeyChar()=='0') { eq=!eq; setUpdateRoot(0); repaint(); }
        if (e.getKeyChar()=='R') { red=!red; setUpdateRoot(0); repaint(); }
        if (e.getKeyChar()=='t') { tri=!tri; setUpdateRoot(0); repaint(); }
        int sw=-1;
        if (e.getKeyChar()=='+') { xstretch+=1; setUpdateRoot(0); repaint(); }
        if (e.getKeyChar()=='-' && xstretch>1) { xstretch-=1; setUpdateRoot(0); repaint(); }
        if (e.getKeyChar()=='1') sw=1;
        if (e.getKeyChar()=='2') sw=2;
        if (e.getKeyChar()=='3') sw=3;
        if (e.getKeyChar()=='4') sw=4;
        if (e.getKeyChar()=='5') sw=5;
        if (e.getKeyChar()=='6') sw=6;
        if (e.getKeyChar()=='7') sw=7;
        if (e.getKeyChar()=='8') sw=8;
        if (e.getKeyChar()=='9') sw=9;

        if (sw>0) {
            sw++;
            int i=1;
            SVar a=vars[sw];
            a.tag=1;
            i=sw;
            while (i>1) {
                vars[i]=vars[i-1];
                vars[i].tag=i;
                i--;                
            }
            vars[1]=a;
            setUpdateRoot(0);
            repaint();
        }
//        if (e.getKeyChar()=='P') run(this,"print");
    }

    public void keyReleased(KeyEvent e) {
    }

    
}
