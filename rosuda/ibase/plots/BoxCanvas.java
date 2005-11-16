package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** OrdStats - ordinal statistics of a variable, used internally by {@link BoxCanvas}
 * to get necessary information to plot bopxplots */
class OrdStats { // get ordinal statistics to be used in boxplot
    double med, uh, lh, uh15, lh15, uh3, lh3;
    int[] lastR;
    int lastTop;
    /** indexes of points just above/below the 1.5 hinge
     * beware, this is relative to the used r[] so
     * use with care and only with the corresponding r[] */
    int lowEdge, highEdge;
    
    OrdStats() { med=uh=lh=uh3=lh3=0; };
    
    double medFrom(SVar v,int[] r,int min,int max) {
        return (((max-min)&1)==0)
        ?v.atF(r[min+(max-min)/2])
        :((v.atF(r[min+(max-min)/2])+v.atF(r[min+(max-min)/2+1]))/2);
    };
    
    void update(SVar v, int[] r) {
        update(v,r,r.length);
    };
    
    /* v=variable, r=ranked index as returned by getRanked, n=# of el to use */
    void update(SVar v, int[] r, int n) {
        lastTop=n;
        if (n<1) return;
        med=medFrom(v,r,0,n-1);
        uh=medFrom(v,r,n/2,n-1);
        if (n>1 && (n&1)==1)
            lh=medFrom(v,r,0,n/2-1);
        else
            lh=medFrom(v,r,0,n/2);
        lh15=lh-(double)1.5*(uh-lh);
        lh3=lh-3*(uh-lh);
        double x=lh;
        int i=n/4; // find lh15 as extreme between lh and lh15
        while (i>=0) {
            double d=v.atF(r[i]);
            if (d<lh15) break;
            if (d<x) x=d;
            i--;
        };
        lowEdge=i;
        lh15=x;
        uh15=uh+(double)1.5*(uh-lh);
        uh3=uh+3*(uh-lh);
        x=uh;
        i=n*3/4-1; if (i<0) i=0; // find uh15
        while (i<n) {
            double d=v.atF(r[i]);
            if (d>uh15) break;
            if (d>x) x=d;
            i++;
        };
        uh15=x;
        highEdge=i;
        lastR=r;
    };
};

/** BoxCanvas - implementation of the boxplots
 * @version $Id$
 */
public class BoxCanvas extends BaseCanvas {
    /** associated numerical variable */
    SVar v;
    /** associated categorical variable if {@link #vsCat} is <code>true</code> */
    SVar cv;
    /** if <code>true</code> then side-by-side bosplots grouped by {@link #cv} are drawn,
     * otherwise draw just a single boxpolot */
    boolean vsCat=false;
    boolean valid=false, dragMode=false, areMarked=false;
    boolean vertical=true;
    
    // for vsCat version
    int rk[][];
    int rs[];
    int cs;
    Object cats[];
    OrdStats oss[];
    
    // for plain version
    OrdStats OSdata;
    OrdStats OSsel;
    
    /** create a boxplot canvas for a single boxplot
     * @param f associated frame (or <code>null</code> if none)
     * @param var source variable
     * @param mark associated marker */
    public BoxCanvas(PlotComponent pc, Frame f, SVar var, SMarker mark) {
        super(pc,f,mark);
        v=var;
        setTitle("Boxplot ("+v.getName()+")");
        ax=new Axis(v,Axis.O_Y,Axis.T_Num); ax.addDepend(this);
        if (var!=null && !var.isCat() && var.isNum())
            valid=true; // valid are only numerical vars non-cat'd
        if (valid) {
            OSdata=new OrdStats();
            OSsel=new OrdStats();
            int dr[]=v.getRanked();
            OSdata.update(v,dr);
            updateObjects();
        };
        String myMenu[]={"+","File","~File.Graph","~Edit","+","View","@RRotate","rotate","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
    };
    
    /** create a boxplot canvas for a multiple grouped boxplots side-by-side
     * @param f associated frame (or <code>null</code> if none)
     * @param var source numerical variable
     * @param cvar categorical variable for grouping
     * @param mark associated marker */
    public BoxCanvas(PlotComponent pc, Frame f, SVar var, SVar cvar, SMarker mark) { // multiple box vs cat
        super(pc,f,mark);
        v=var; m=mark; cv=cvar; setFrame(f);
        setTitle("Boxplot ("+v.getName()+" grouped by "+cv.getName()+")");
        ax=new Axis(v,Axis.O_Y,Axis.T_Num); ax.addDepend(this);
        // get some space around (this comes from the scatterplots)
        ax.setValueRange(v.getMin()-(v.getMax()-v.getMin())/20,(v.getMax()-v.getMin())*1.1);
        pc.setBackground(new Color(255,255,192));
        pc.addMouseListener(this);
        pc.addMouseMotionListener(this);
        pc.addKeyListener(this); f.addKeyListener(this);
        if (var!=null && !var.isCat() && var.isNum() && cvar.isCat())
            valid=true; // valid are only numerical vars non-cat'd, cvar is cat
        if (valid) { // split into ranked chunks by cat.
            vsCat=true;
            cs=cv.getNumCats();
            cats=cv.getCategories();
            int[] r=v.getRanked();
            oss=new OrdStats[cs*2+2];
            rk=new int[cs*2+2][];
            rs=new int[cs*2+2];
            int i=0;
            while (i<cs) {
                rs[i]=0;
                int j=cv.getSizeCatAt(i);
                rk[i]=new int[j];
                rk[cs+1+i]=new int[j];
                oss[i]=new OrdStats();
                oss[cs+1+i]=new OrdStats();
                i++;
            };
            i=0;
            while(i<r.length) {
                int x=cv.getCatIndex(cv.at(r[i]));
                if (x<0) x=cs;
                rk[x][rs[x]]=r[i];
                rs[x]++;
                i++;
            };
            i=0;
            while(i<cs) {
                oss[i].update(v,rk[i],rs[i]);
                i++;
            };
            updateObjects();
            String myMenu[]={"+","File","~File.Graph","~Edit","~Window","0"};
            EzMenu.getEzMenu(f,this,myMenu);
        };
    };
    
    public SVar getData(int id) { return (id==0)?v:((id==1)?cv:null); }
    
    public Dimension getMinimumSize() { return new Dimension(60,50); };
    
    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        updateObjects();
        repaint();
    };
    
    public void updateObjects() {
        if (!valid) return;
        int md[]=v.getRanked(m,-1);
        areMarked=(md!=null);
        if (vsCat) {
            if (areMarked) {
                int i=0;
                while (i<cs) { rs[cs+1+i]=0; i++; };
                i=0;
                while(i<md.length) {
                    int x=cv.getCatIndex(cv.at(md[i]));
                    if (x<0) x=cs;
                    x+=cs+1;
                    rk[x][rs[x]]=md[i];
                    rs[x]++;
                    i++;
                };
                i=cs+1;
                while(i<2*cs+1) {
                    oss[i].update(v,rk[i],rs[i]);
                    i++;
                };
            };
        } else {
            if (areMarked)
                OSsel.update(v,md);
        };
        
        if (!vsCat) {
            pp = new PlotPrimitive[areMarked?2:1];
            pp[0] = createBox(OSdata,40,20,"white","black");
            if (areMarked)
                pp[1] = createBox(OSsel,48,10,"selfill","sel");
        } else {
            Vector boxes = new Vector();
            int i=0;
            while(i<cs) {
                boxes.add(createBox(oss[i],40+40*i,20,"white","black"));
                if (areMarked && rs[cs+1+i]>0)
                    boxes.add(createBox(oss[cs+1+i],48+40*i,10,"selfill","sel"));
                i++;
            };
            pp = new PlotPrimitive[boxes.size()];
            boxes.toArray(pp);
        };
    };
    
    private PPrimBox createBox(OrdStats os, int x, int w, String fillColor, String drawColor){
        PPrimBox box = new PPrimBox();
        box.x=x;
        box.w=w;
        box.med = ax.getValuePos(os.med);
        box.lh = ax.getValuePos(os.lh);
        box.uh = ax.getValuePos(os.uh);
        box.lh15 = ax.getValuePos(os.lh15);
        box.uh15 = ax.getValuePos(os.uh15);
        box.lh3 = os.lh3;
        box.uh3 = os.uh3;
        box.fillColor = fillColor;
        box.drawColor = drawColor;
        box.lowEdge = os.lowEdge;
        box.lastR = new double[os.lastR.length];
        box.valPos = new int[os.lastR.length];
        for(int i=0; i< box.lastR.length; i++){
            box.lastR[i] = v.atF(os.lastR[i]);
            box.valPos[i] = ax.getValuePos(box.lastR[i]);
        }
        box.lastTop = os.lastTop;
        box.highEdge = os.highEdge;
        return box;
    }
    
    protected int X,Y,W,H, TW,TH;
    
    public void paintBack(PoGraSS g) {
        Rectangle r=pc.getBounds();
        g.setBounds(r.width,r.height);
        
        int w=r.width, h=r.height;
        TW=w; TH=h;
        int innerL=30, innerB=30, lshift=0;
        int innerW=w-innerL-10, innerH=h-innerB-10;
        Y=TH-innerB-innerH;
        
        //g.begin();
        if (!valid) {
            g.defineColor("red",255,0,0);
            g.drawLine(0,0,r.width,r.height);
            g.drawLine(0,r.height,r.width,0);
//	    g.end();
            return;
        };
        g.defineColor("white",255,255,255);
        g.defineColor("black",0,0,0);
        g.defineColor("selfill",0,255,0);
        g.defineColor("sel",0,128,0);
        if (vertical) {
            ax.setGeometry(Axis.O_Y,h-innerB,-(H=innerH));
            //a.setGeometry(Axis.O_Y,r.height-20,-r.height+30);
            /* draw ticks and labels for Y axis */
            {
                int X=30;
                double f=ax.getSensibleTickDistance(30,18);
                double fi=ax.getSensibleTickStart(f);
                //if (Common.DEBUG>0)
                //System.out.println("SP.A[1]:"+A[1].toString()+", distance="+f+", start="+fi);
                while (fi<ax.vBegin+ax.vLen) {
                    int t=ax.getValuePos(fi);
                    g.drawLine(X-5,t,X,t);
                    //if(showLabels)
                    g.drawString(ax.getDisplayableValue(fi),X-25,t+5);
                    fi+=f;
                };
                g.drawLine(X,ax.gBegin,X,ax.gLen);
            }
        } else {
            ax.setGeometry(Axis.O_X,40,r.width-50);
        }
        
        if (vsCat) {
            int i=0;
            while(i<cs) {
                g.drawString(Common.getTriGraph(cv.getCatAt(i).toString()),40+40*i,Y+H+20,PoGraSS.TA_Center);
                i++;
            };
        };
        
//	g.end();
    };
    
    public Object run(Object o, String cmd) {
        super.run(o,cmd);
        if (cmd=="rotate") {
            //    rotate();
        };
        if (cmd=="print") run(o,"exportPS");
        /*if (cmd=="exportCases") {
            try {
                PrintStream p=Tools.getNewOutputStreamDlg(myFrame,"Export selected cases to ...","selected.txt");
                if (p!=null) {
                    p.println(v.getName()+(vsCat?("\t"+cv.getName()):""));
                    int i=0, sz=v.size();
                    while (i<sz) {
                        Object oo=v.at(i);
                        if (m.at(i)) {
                            if (vsCat)
                                p.println((oo==null)?"NA":oo.toString()+"\t"+cv.at(i).toString());
                            else
                                p.println((oo==null)?"NA":oo.toString());
                        }
                        i++;
                    }
                    p.close();
                }
            } catch (Exception eee) {}
        }*/
        return null;
    }
}
