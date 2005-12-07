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
import org.rosuda.ibase.plots.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;
import org.rosuda.klimt.*;

public class TreeFlowCanvas extends PGSCanvas implements Dependent, KeyListener, MouseListener, ActionListener {
    SNode roots[];
    int lastw,lasth;
    float alpha=0.1f;
    boolean eq=true, red=false, grColor=false, useLW=false;
    boolean tri=false;
    int xstretch=1;
    QueryPopup qi;
    
    int[] levl; // # of vars per level (max 64)
    SVar[] vg;  // 64*32 matrix of vars (l*32+i)
    int ls;     // # of levels
    int seq=0;
    int redIndex=0;
    SVar[] vars;
	
	int dsSize;  // normalization for line width - data set size

    public TreeFlowCanvas(TFrame f, SNode[] trees) {
        super(2);
        setFrame(f);
        roots=trees;

        pc.addKeyListener(this); f.addKeyListener(this);
        pc.addMouseListener(this);
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
        qi=PlotComponentFactory.createQueryPopup(pc,f,null,"Trace plot");
        String myMenu[]={"+","File","~File.Graph","~Edit",//"+","View","@RRotate","rotate","@LHide labels","labels","!HToggle hilight. style","selRed","@JToggle jittering","jitter","@BToggle back-lines","backlines","-","Set X Range ...","XrangeDlg","Set Y Range ...","YrangeDlg",
            "~Window","0"};
		EzMenu.getEzMenu(f,this,myMenu);
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

    SVar splitHist;
    SVar splitLeft, splitRight;
    boolean createPlots=false;
    int countQ=0, countL=0;    

    public int queryMatrix(int mx, int my) {
        int yspc=h/(ls+1);
        int l=my/yspc;
        int i=0;
        while (i<levl[l]) {
            int vw=w/(levl[l]+1);
            int spc=(w-(vw*levl[l]))/(levl[l]+1);
            int x=spc+(vw+spc)*i;
            if (eq) {
                vw=w/seq;
                spc=vw/seq;
                x=(vw+spc)*i+spc;
            }
            if (x<mx && x+vw>mx)
                return i+l*32;
            i++;
        }
        return -1;
    }

    public void queryPlot(int vid) {
        if (vid<0) return;
        int l=vid/32;
        int i=vid-(l*32);
        if (Global.DEBUG>0) System.out.println("query at level "+l+", index "+i+", variable "+vg[i+l*32]);
        SVar sv=vg[vid];
        if (sv.isCat()) {
            splitLeft=new SVarObj("SL",true);
            splitRight=new SVarObj("SR",true);
        } else {
            splitHist=new SVarObj("SVH",false);
        }
        int t=0;
        while (t<roots.length) {
            SNode n=roots[t++];
            queryDown(n, 0, sv, l);
        }
        if (sv.isCat()) {
            TFrame f=new TFrame("Barchart (left "+sv.getName()+" at "+l+")",TFrame.clsBar);
            f.addWindowListener(Common.getDefaultWindowListener());
            int xdim=400, ydim=300;
            SMarker dm=new SMarker(sv.size());
            BarCanvas bc=new BarCanvas(null,f,splitLeft,dm);
            xdim=100+40*splitLeft.getNumCats(); ydim=200;
            if (xdim>800) xdim=800;
            dm.addDepend(bc);
            bc.setSize(new Dimension(xdim,ydim));
            f.add(bc.getComponent()); f.pack(); f.show();
            f.initPlacement();
            f=new TFrame("Barchart (right "+sv.getName()+" at "+l+")",TFrame.clsBar);
            f.addWindowListener(Common.getDefaultWindowListener());
            xdim=400; ydim=300;
            dm=new SMarker(sv.size());
            bc=new BarCanvas(null,f,splitRight,dm);
            xdim=100+40*splitRight.getNumCats(); ydim=200;
            if (xdim>800) xdim=800;
            dm.addDepend(bc);
            bc.setSize(new Dimension(xdim,ydim));
            f.add(bc.getComponent()); f.pack(); f.show();
            f.initPlacement();
        } else {
            TFrame f=new TFrame("Histogram (s.f. of "+sv.getName()+" at "+l+")",TFrame.clsHist);
            f.addWindowListener(Common.getDefaultWindowListener());
            int xdim=400, ydim=300;
            SMarker dm=new SMarker(sv.size());
            HistCanvas hc=new HistCanvas(null,f,splitHist,dm);
            dm.addDepend(hc);
            hc.setSize(new Dimension(xdim,ydim));
            f.add(hc.getComponent()); f.pack(); f.show();
            f.initPlacement();
        }
    }

    public void queryResult(SNode n) {
        SVar sv=n.splitVar;
        if (Global.DEBUG>0)
            System.out.println("queryResult("+n+") with "+sv+", split="+n.splitValF);
        if (!createPlots) {
            countQ++;
            return;
        }
        if (sv.isCat()) {
            String s=n.splitVal;
            System.out.println("qR: left=\""+s+"\"");
            StringTokenizer st=new StringTokenizer(s,",");
            while (st.hasMoreTokens()) {
                splitLeft.add(st.nextToken());
            }
            SNode par=(SNode)n.getParent();
            if (par!=null) par=(SNode)par.at(1);
            if (par!=null) {
                s=par.splitVal;
                System.out.println("qR: right=\""+s+"\"");
                st=new StringTokenizer(s,",");
                while (st.hasMoreTokens()) {
                    splitRight.add(st.nextToken());
                }
            }
        } else {
            splitHist.add(new Double(n.splitValF));
        }
    }
    
    public void queryDown(SNode n, int l, SVar target, int targetL) {
        if (Global.DEBUG>0) System.out.println("queryDown("+n+","+l+")");
        int chs=n.count(), ct=0;
        if (chs>0) {
            SNode ln=(SNode)n.at(0);
            SVar sv=ln.splitVar;
            if (sv==target && targetL==l) queryResult(ln);
            if (targetL==l) countL++;
        }
        l++;
        if (l>targetL) return;
        while (ct<chs) {
            queryDown((SNode)n.at(ct),l,target,targetL);
            ct++;
        }
    }

    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        repaint();
    }

    int w,h;
    
    public void paintPoGraSS(PoGraSS g) {
        Dimension Dsize=pc.getSize();
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
			dsSize=n.Cases;
            if (red && t==redIndex) System.out.println("RED ("+redIndex+") is "+n);
            if (red && t==redIndex) g.setColor(1f,0f,0f,1f);
            if (n!=null && (!red || t==redIndex)) drawNode(g,n,w/2,0,0, red && t==redIndex, n.getRootInfo().response);
            if (red && t==redIndex) g.setColor(0f,0f,0f,alpha);
            t++;
        }
        
        g.end();
    }

    public void drawNode(PoGraSS g, SNode n, int ox, int oy, int l, boolean selected, SVar resp) {
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

				if (selected)
					g.setColor(1f,0f,0f,1f);
				else if (grColor && resp!=null) {
					boolean hasColor=false;
					if (resp.isCat()) {
						int ci=resp.getCatIndex(cn.Name);
						if (ci>=0) {
							int cs=resp.getNumCats();
							if (cs>0) {
								float[] scc=ColorBridge.getMain().getColor(64+(ci*64/cs)).getRGBComponents(null);
								g.setColor(scc[0],scc[1],scc[2],alpha);
								hasColor=true;
							}
						}
					}
					if (!hasColor)
						g.setColor(0f,0f,0f,alpha);
				} else
					g.setColor(0f,0f,0f,alpha);
				if (useLW)
					g.setLineWidth(8f*(float)Math.sqrt(((double)cn.Cases)/((double)dsSize)));
                if (sv.isNum()) {
                    double sl=cn.splitValF;
                    double vr=sv.getMax()-sv.getMin();
                    int sp=(int)(((sl-sv.getMin())/vr)*((double)vw));
                    x+=sp;
                    g.drawLine(ox*xstretch,oy,x*xstretch,y);
					if (useLW)
						g.setLineWidth(1f);
                    g.drawLine(x*xstretch,y-5,x*xstretch,y+5);
                } else {
                    x+=vw/2;
                    g.drawLine(ox*xstretch,oy,x*xstretch,y);
					if (useLW)
						g.setLineWidth(1f);
                    g.drawLine(x*xstretch,y-5,x*xstretch,y+5);
                }
                break;
            }
            i++;
        }
        int ch=0;
        while (ch<chs) {
            drawNode(g,(SNode)n.at(ch++),x,y,l+1,selected,resp);
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
        if (e.getKeyChar()=='w') { useLW=!useLW; setUpdateRoot(0); repaint(); }
        if (e.getKeyChar()=='c') { grColor=!grColor; setUpdateRoot(0); repaint(); }
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
        if (e.getKeyChar()=='.') { redIndex++; if (redIndex>roots.length-1) redIndex=0; setUpdateRoot(0); repaint(); }
        if (e.getKeyChar()==',') { redIndex--; if (redIndex<0) redIndex=roots.length-1; setUpdateRoot(0); repaint(); }
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

    public void mouseClicked(MouseEvent e) {
        int i=queryMatrix(e.getX(),e.getY());
        if (i>=0) {
            if (e.isShiftDown()) {
                createPlots=true;
                queryPlot(i);
            } else {
                createPlots=false;
                countQ=countL=0;
                int t=0;
                int l=i/32;
                while (t<roots.length) {
                    SNode n=roots[t++];
                    queryDown(n, 0, vg[i], l);
                }
                int x=e.getX(), y=e.getY();
                Point cl=getFrame().getLocation();
                Point tl=pc.getLocation(); cl.x+=tl.x; cl.y+=tl.y;
                
                SVar v=vg[i];
                qi.setContent("Variable: "+v.getName()+"\nSplits: "+countQ+" (of "+countL+" s.l.)");
                qi.setLocation(cl.x+x,cl.y+y);
                qi.show();
            }
        }
    }
    
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
	if (e==null) return;
	run(e.getSource(),e.getActionCommand());
    }
}
