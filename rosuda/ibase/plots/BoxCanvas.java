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
    boolean valid=false, dragMode=false;
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
    
    // Array mapping each PPrimBox to the OrdStats object which contains its selections
    OrdStats markStats[];
    
    /** create a boxplot canvas for a single boxplot
     * @param f associated frame (or <code>null</code> if none)
     * @param var source variable
     * @param mark associated marker */
    public BoxCanvas(PlotComponent pc, Frame f, SVar var, SMarker mark) {
        this(pc,f,new SVar[]{var},mark);
    }
    
    /** create a boxplot canvas for multiple boxplots
     * @param f associated frame (or <code>null</code> if none)
     * @param var source variables
     * @param mark associated marker */
    public BoxCanvas(PlotComponent ppc, Frame f, SVar[] var, SMarker mark) {
        super(ppc,f,mark);
        mLeft=30;
        mBottom=10;
        mTop=10;
        v=var[0];
        setTitle("Boxplot ("+v.getName()+")");
        ay=new Axis(v,Axis.O_Y,Axis.T_Num); ay.addDepend(this);
        ay.setValueRange(v.getMin()-(v.getMax()-v.getMin())/20,(v.getMax()-v.getMin())*1.1);
        if (v!=null && !v.isCat() && v.isNum())
            valid=true; // valid are only numerical vars non-cat'd
        if (valid) {
            OSdata=new OrdStats();
            OSsel=new OrdStats();
            int dr[]=v.getRanked();
            OSdata.update(v,dr);
            //updateObjects();
        };
        String myMenu[]={"+","File","~File.Graph","~Edit","+","View","@RRotate","rotate","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        objectClipping=true;
        dontPaint=false;
    };
    
    /** create a boxplot canvas for a multiple grouped boxplots side-by-side
     * @param f associated frame (or <code>null</code> if none)
     * @param var source numerical variable
     * @param cvar categorical variable for grouping
     * @param mark associated marker */
    public BoxCanvas(PlotComponent ppc, Frame f, SVar var, SVar cvar, SMarker mark) { // multiple box vs cat
        super(ppc,f,mark);
        mLeft=30;
        mBottom=30;
        mTop=10;
        v=var; m=mark; cv=cvar; setFrame(f);
        setTitle("Boxplot ("+v.getName()+" grouped by "+cv.getName()+")");
        ay=new Axis(v,Axis.O_Y,Axis.T_Num); ay.addDepend(this);
        // get some space around (this comes from the scatterplots)
        ay.setValueRange(v.getMin()-(v.getMax()-v.getMin())/20,(v.getMax()-v.getMin())*1.1);
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
        objectClipping=true;
        dontPaint=false;
    };
    
    public SVar getData(int id) { return (id==0)?v:((id==1)?cv:null); }
    
    public Dimension getMinimumSize() { return new Dimension(60,50); };
    
    public void updateObjects() {
        if (!valid) return;
        
        if (!vsCat) {
            pp = new PlotPrimitive[1];
            pp[0] = createBox(OSdata,40,20);
            PPrimBox p = ((PPrimBox)pp[0]);
            p.ref = v.getRanked();
            markStats = new OrdStats[1];
            markStats[0] = OSsel;
        } else {
            Vector boxes = new Vector();
            int i=0;
            while(i<cs) {
                PPrimBox box = createBox(oss[i],40+40*i,20);
                box.ref = rk[i];
                boxes.add(box);
                i++;
            };
            pp = new PlotPrimitive[boxes.size()];
            boxes.toArray(pp);
            markStats = new OrdStats[boxes.size()];
            System.arraycopy(oss, cs+1, markStats, 0, cs);
        };
        for(int i=0; i<pp.length; i++) ((PPrimBox)pp[i]).slastR=null;
    };
    
    private PPrimBox createBox(OrdStats os, int x, int w){
        PPrimBox box = new PPrimBox();
        box.x=x;
        box.w=w;
        box.med = ay.getValuePos(os.med);
        box.lh = ay.getValuePos(os.lh);
        box.uh = ay.getValuePos(os.uh);
        box.lh15 = ay.getValuePos(os.lh15);
        box.uh15 = ay.getValuePos(os.uh15);
        box.lh3 = os.lh3;
        box.uh3 = os.uh3;
        box.lowEdge = os.lowEdge;
        box.lastR = new double[os.lastR.length];
        box.valPos = new int[os.lastR.length];
        for(int i=0; i< box.lastR.length; i++){
            box.lastR[i] = v.atF(os.lastR[i]);
            box.valPos[i] = ay.getValuePos(box.lastR[i]);
        }
        box.lastTop = os.lastTop;
        box.highEdge = os.highEdge;
        
        //System.out.println("x: " + x + ", w: " + w + ", med: " + ay.getValuePos(os.med) + ", lh: " + ay.getValuePos(os.lh) + ", uh: " + ay.getValuePos(os.uh)
        //+  ", lh15: " + ay.getValuePos(os.lh15) + ", uh15: " + ay.getValuePos(os.uh15) + ", lh3:" +  os.lh3 + ", uh3: " + os.uh3 + ", lowedge: " + os.lowEdge);
        return box;
    }
    
    protected int X,Y,W,H, TW,TH;
    
    public void paintBack(PoGraSS g) {
        Rectangle r=pc.getBounds();
        g.setBounds(r.width,r.height);
        
        int w=r.width, h=r.height;
        
        int innerW=w-mLeft-10, innerH=h-mBottom-mTop;
        
        if (!valid) {
            g.defineColor("red",255,0,0);
            g.drawLine(0,0,r.width,r.height);
            g.drawLine(0,r.height,r.width,0);
            return;
        };
        if (vertical) {
            ay.setGeometry(Axis.O_Y,h-mBottom,-(H=innerH));

            /* draw ticks and labels for Y axis */
            {
                double f=ay.getSensibleTickDistance(30,18);
                double fi=ay.getSensibleTickStart(f);
                //if (Common.DEBUG>0)
                //System.out.println("SP.A[1]:"+A[1].toString()+", distance="+f+", start="+fi);
                while (fi<ay.vBegin+ay.vLen) {
                    int t=ay.getValuePos(fi);
                    g.drawLine(mLeft-5,t,mLeft,t);
                    //if(showLabels)
                    g.drawString(ay.getDisplayableValue(fi),mLeft-25,t+5);
                    fi+=f;
                };
                g.drawLine(mLeft,ay.gBegin,mLeft,ay.gBegin+ay.gLen);
            }
        } else {
            ay.setGeometry(Axis.O_X,40,r.width-50);
        }
        
        if (vsCat) {
            int i=0;
            while(i<cs) {
                g.drawString(Common.getTriGraph(cv.getCatAt(i).toString()),40+40*i,mTop+H+20,PoGraSS.TA_Center);
                i++;
            };
        };
    };
    
    public void paintSelected(PoGraSS g) {
        int md[]=v.getRanked(m,-1);
        if(md==null) return;
        if (vsCat) {
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
        } else {
            OSsel.update(v,md);
        };
        for(int i=0; i<pp.length; i++){
            PPrimBox box = ((PPrimBox)pp[i]);
            box.sx = box.x + box.w*2/5;
            box.sw = box.w/2;
            box.smed = ay.getValuePos(markStats[i].med);
            box.slh = ay.getValuePos(markStats[i].lh);
            box.suh = ay.getValuePos(markStats[i].uh);
            box.slh15 = ay.getValuePos(markStats[i].lh15);
            box.suh15 = ay.getValuePos(markStats[i].uh15);
            box.slh3 = markStats[i].lh3;
            box.suh3 = markStats[i].uh3;
            box.slowEdge = markStats[i].lowEdge;
            box.slastR = new double[markStats[i].lastR.length];
            box.svalPos = new int[markStats[i].lastR.length];
            for(int j=0; j< box.slastR.length; j++){
                box.slastR[j] = v.atF(markStats[i].lastR[j]);
                box.svalPos[j] = ay.getValuePos(box.slastR[j]);
            }
            box.slastTop = markStats[i].lastTop;
            box.shighEdge = markStats[i].highEdge;
        }
        super.paintSelected(g);
    }

    public void paintObjects(PoGraSS g) {
        updateObjects();
        super.paintObjects(g);
    }
    
}
