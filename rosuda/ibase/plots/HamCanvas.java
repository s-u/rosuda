package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.PrintStream;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** implementation of hammock plots (uses {@link BaseCanvas})
@version $Id$
*/
public class HamCanvas extends BaseCanvas
{
    SVar[] v;
    SVar vx;

    boolean showLabels=true;
    boolean useX3=false;
    int gap=0;

    Axis[] ai;
    
    public HamCanvas(Frame f, SVar[] mv, SMarker mark) {
        super(f,mark);
        setTitle("Hammock Plot");
        v=mv;
        allow180=false;
        vx=new SVarObj("Hammock.index",true);

        ay=new Axis(null,Axis.O_Y,Axis.T_Num); ay.addDepend(this); ay.setValueRange(0,1);
        ai=new Axis[mv.length];
        
        int i=0;
        while (i<mv.length) {
            //System.out.println("var "+i+": "+mv[i]);
            ai[i]=new Axis(mv[i], Axis.O_Y, mv[i].isCat()?Axis.T_EqCat:Axis.T_Num);
            vx.add(mv[i].getName());
            i++;
        }

        ax=new Axis(vx,Axis.O_X,Axis.T_EqCat); ax.addDepend(this);

        String myMenu[]={"+","File","~File.Graph","~Edit","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        mLeft=mRight=mTop=mBottom=10;
        // note: Map's updateObjects relies on equality of all margins!
        pp=null;
    }

    public void updateObjects() {
        Dimension Dsize=getSize();
        int w=Dsize.width, h=Dsize.height;
        int axi=0; while (axi<ai.length)
            ai[axi++].setGeometry(Axis.O_Y, mLeft, h-mLeft*2);
        w=w-mLeft*2; h=h-mLeft*2;

        if (pp==null) {
            int full=0;
            // pass 1: get # of pps
            int j=0;
            while (j<v.length-1) {
                SVar v1=v[j];
                SVar v2=v[j+1];
                int c1=v1.getNumCats();
                int c2=v2.getNumCats();
                int n=v1.size();
                if (c1>0 && c2>0) {
                    int cct[]=new int[c1*c2];
                    int i=0;
                    while (i<n) {
                        int cat1=v1.getCatIndex(i);
                        int cat2=v2.getCatIndex(i);
                        if (cat1>-1 && cat2>-1)
                            cct[cat1+cat2*c1]++;
                        i++;
                    }
                    i=0;
                    while (i<c1*c2) {
                        if (cct[i]>0) full++;
                        i++;
                    }
                }
                j++;
            }

            pp=new PPrimHam[full];

            int ppix=0;
            // pass 2: fill pps
            j=0;
            while (j<v.length-1) {
                SVar v1=v[j];
                SVar v2=v[j+1];
                int c1=v1.getNumCats();
                int c2=v2.getNumCats();
                int n=v1.size();
                if (c1>0 && c2>0) {
                    int ixl[]=new int[n];
                    int cct[]=new int[c1*c2];
                    int cix[]=new int[c1*c2];
                    int i=0;
                    while (i<n) {
                        int cat1=v1.getCatIndex(i);
                        int cat2=v2.getCatIndex(i);
                        if (cat1>-1 && cat2>-1) {
                            cct[cat1+cat2*c1]++;
                            if (cix[cat1+cat2*c1]==0) {
                                ppix++;
                                cix[cat1+cat2*c1]=ppix;
                            }
                            ixl[i]=1+cat1+cat2*c1;
                        }
                        i++;
                    }
                    i=0;
                    while (i<c1*c2) {
                        if (cct[i]>0) {
                            PPrimHam p=new PPrimHam();
                            p.ref=new int[cct[i]];
                            p.tmp=0;
                            p.total=n;
                            p.leftVar=j;
                            pp[cix[i]-1]=p;
                        }
                        i++;
                    }
                    i=0;
                    while (i<n) {
                        if (ixl[i]>0) {
                            PPrimHam p=(PPrimHam) pp[cix[ixl[i]-1]-1];
                            p.ref[p.tmp]=i;
                            p.tmp++;
                        }
                        i++;
                    }
                }
                j++;
            }
        }

        int k=0;
        while (k<pp.length) {
            PPrimHam p=(PPrimHam) pp[k];
            if (p.ref!=null && p.ref.length>0) {
                int fcid = p.ref[0]; // first case id
                int lv = p.leftVar;  // left variable id
                int x1=ax.getValuePos(lv)+gap;
                int x2=ax.getValuePos(lv+1)-gap;
                int y1=ai[lv].getCasePos(fcid);
                int y2=ai[lv+1].getCasePos(fcid);
                //System.out.println("["+x1+","+y1+"]-["+x2+","+y2+"], h="+h+", count="+p.cases());
                p.updateAnchors(x1,y1,x2,y2,h);
            }
            k++;
        }
        
        setUpdateRoot(0);
    }

    public void paintBack(PoGraSS g) {
        /* draw labels for X axis */
        double f=ax.getSensibleTickDistance(50,26);
        double fi=ax.getSensibleTickStart(f);
        while (fi<ax.vBegin+ax.vLen) {
            int t=ax.getValuePos(fi);
            if (showLabels)
                g.drawString(vx.isCat()?((useX3)?Common.getTriGraph(vx.getCatAt((int)fi).toString()):vx.getCatAt((int)fi).toString()):
                             ax.getDisplayableValue(fi),t,H-mLeft,0.5,0.5);
            fi+=f;
        }
    }
    
    public String queryObject(int i)
    {
        PPrimHam p=(PPrimHam) pp[i];
        double sd=p.getMarkedProportion(m,-1);
        int marked=(int)(((double)p.cases())*sd+0.5);
        int lv=p.leftVar;
        return v[lv].getName()+": "+v[lv].atS(p.ref[0])+"\n"+
            v[lv+1].getName()+": "+v[lv+1].atS(p.ref[0])+"\n"+
            "\n"+marked+" of "+p.cases()+" ("+Tools.getDisplayableValue(sd*100.0,1)+"%) selected";
    }

    public void keyTyped(KeyEvent e)
    {
        super.keyTyped(e);
        if (e.getKeyChar()=='l') run(this,"labels");
        if (e.getKeyChar()=='t') run(this,"tri");
        if (e.getKeyChar()=='a') run(this,"alpha");
        if (e.getKeyChar()=='.') { gap+=3; setUpdateRoot(0); updateObjects(); repaint(); }
        if (e.getKeyChar()==',') { gap-=3; setUpdateRoot(0); updateObjects(); repaint(); }
    }
    
    public Object run(Object o, String cmd) {
        super.run(o,cmd);
        
        if (cmd=="tri") { useX3=!useX3; setUpdateRoot(0); repaint(); };
        if (cmd=="labels") { showLabels=!showLabels; setUpdateRoot(0); repaint(); };
        if (cmd=="alpha" && pp!=null && pp.length>0) {
            boolean a=!((PPrimHam)pp[0]).alwaysAlpha;
            int i=0;
            while (i<pp.length) { ((PPrimHam)pp[i++]).alwaysAlpha=a; }
            setUpdateRoot(0); repaint();
        }
        return null;
    }    
}
