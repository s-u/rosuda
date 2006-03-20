package org.rosuda.ibase.plots;

import java.awt.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.plots.OrdStats;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.Tools;

/** BoxCanvas - implementation of the boxplots
 * @version $Id$
 */
public class BoxCanvas extends ParallelAxesCanvas {
    
    /** create a boxplot canvas for a single boxplot
     * @param f associated frame (or <code>null</code> if none)
     * @param var source variable
     * @param mark associated marker */
    public BoxCanvas(final PlotComponent pc, final Frame f, final SVar var, final SMarker mark) {
        this(pc,f,new SVar[]{var},mark);
    }
    
    /** create a boxplot canvas for multiple boxplots
     * @param f associated frame (or <code>null</code> if none)
     * @param var source variables
     * @param mark associated marker */
    public BoxCanvas(final PlotComponent ppc, final Frame f, final SVar[] var, final SMarker mark) {
        super(ppc,f,var,mark);
        updateMargins();
        
        String variables = v[0].getName();
        for(int i=1; i<v.length; i++) variables+=", " + v[i].getName();
        setTitle("Boxplot ("+ variables + ")");
        
        
        if(var.length==1){
            if (v[0]!=null && !v[0].isCat() && v[0].isNum())
                valid=true; // valid are only numerical vars non-cat'd
            else valid=false;
            if (valid) {
                OSdata=new OrdStats();
                final int dr[]=v[0].getRanked();
                OSdata.update(v[0],dr);
                //updateObjects();
            }
        } else{
            oss = new OrdStats[v.length];
            for(int i=0; i<v.length; i++){
                if (v[i]!=null && !v[i].isCat() && v[i].isNum())
                    valid=true; // valid are only numerical vars non-cat'd
                if (valid) {
                    oss[i]=new OrdStats();
                    final int dr[]=v[i].getRanked();
                    oss[i].update(v[i],dr);
                }
            }
        }
        dontPaint=false;
    };
    
    /** create a boxplot canvas for a multiple grouped boxplots side-by-side
     * @param f associated frame (or <code>null</code> if none)
     * @param var source numerical variable
     * @param cvar categorical variable for grouping
     * @param mark associated marker */
    public BoxCanvas(final PlotComponent ppc, final Frame f, final SVar var, final SVar cvar, final SMarker mark) { // multiple box vs cat
        super(ppc,f,var,cvar,mark);
        vsCat=true;
        updateMargins();
        
        setTitle("Boxplot ("+v[0].getName()+" grouped by "+cv.getName()+")");
        
        if (var!=null && !var.isCat() && var.isNum() && cvar.isCat())
            valid=true; // valid are only numerical vars non-cat'd, cvar is cat
        if (valid) { // split into ranked chunks by cat.
            cs=cv.getNumCats();
            cats=cv.getCategories();
            final int[] r=v[0].getRanked();
            oss=new OrdStats[cs*2+2];
            rk=new int[cs*2+2][];
            rs=new int[cs*2+2];
            int i=0;
            while (i<cs) {
                rs[i]=0;
                final int j=cv.getSizeCatAt(i);
                rk[i]=new int[j];
                rk[cs+1+i]=new int[j];
                oss[i]=new OrdStats();
                oss[cs+1+i]=new OrdStats();
                i++;
            }
            i=0;
            while(i<r.length) {
                int x=cv.getCatIndex(cv.at(r[i]));
                if (x<0) x=cs;
                rk[x][rs[x]]=r[i];
                rs[x]++;
                i++;
            }
            i=0;
            while(i<cs) {
                oss[i].update(v[0],rk[i],rs[i]);
                i++;
            }
            boolean[] validOss = new boolean[cs];
            int invalid=0;
            for(i=0; i<cs; i++){
                if(oss[i].lastR==null){
                    validOss[i]=false;
                    invalid++;
                } else validOss[i]=true;
            }
            if(invalid>0){
                OrdStats[] newOss = new OrdStats[2*(cs-invalid)+2];
                int j=0;
                for(i=0;i<cs; i++){
                    if(validOss[i]) newOss[j++]=oss[i];
                }
                newOss[cs]=oss[cs];
                j=0;
                for(i=0;i<cs; i++){
                    if(validOss[i]) newOss[cs-invalid+1+j++]=oss[cs+1+i];
                }
                oss=newOss;
                cs-=invalid;
            }
            updateObjects();
        }
        objectClipping=true;
        dontPaint=false;
    };
    
    public SVar getData(final int id) {
        if(cv==null) return super.getData(id);
        else return (id==0)?v[0]:((id==1)?cv:null);
    }
    
    public void updateObjects() {
        updateObjectsBox();
    };
    
    public void paintSelected(final PoGraSS g) {
        final int md[][] = new int[v.length][];
        for(int i=0; i<v.length; i++) md[i] = v[i].getRanked(m,-1);
        //if(md==null) return;
        if (vsCat) {
            if(md[0]==null) return;
            int i=0;
            while (i<cs) { rs[cs+1+i]=0; i++; }
            i=0;
            while(i<md[0].length) {
                int x=cv.getCatIndex(cv.at(md[0][i]));
                if (x<0) x=cs;
                x+=cs+1;
                rk[x][rs[x]]=md[0][i];
                rs[x]++;
                i++;
            }
            i=cs+1;
            while(i<2*cs+1) {
                oss[i].update(v[0],rk[i],rs[i]);
                i++;
            }
        } else {
            for(int i=0; i<v.length; i++) {
                if(md[i]!=null) markStats[i].update(v[i],md[i]);
                else markStats[i].update(v[i],new int[]{});
            }
        }
        for(int i=0; i<pp.length; i++){
            final PPrimBox box = ((PPrimBox)pp[i]);
            if(markStats[i].lastTop==0){
                box.slastR=null;
            } else{
                final Axis axis = (commonScale || i==0 || vsCat)?ay:opAy[i-1];
                box.sx = box.x + box.w*2/5;
                box.sw = box.w/2;
                box.smed = axis.getValuePos(markStats[i].med);
                box.slh = axis.getValuePos(markStats[i].lh);
                box.suh = axis.getValuePos(markStats[i].uh);
                box.slh15 = axis.getValuePos(markStats[i].lh15);
                box.suh15 = axis.getValuePos(markStats[i].uh15);
                box.slh3 = markStats[i].lh3;
                box.suh3 = markStats[i].uh3;
                box.slowEdge = markStats[i].lowEdge;
                if(markStats[i].lastR!=null){
                    box.slastR = new double[markStats[i].lastR.length];
                    box.svalPos = new int[markStats[i].lastR.length];
                    for(int j=0; j< box.slastR.length; j++){
                        box.slastR[j] = v[vsCat?0:i].atF(markStats[i].lastR[j]);
                        box.svalPos[j] = axis.getValuePos(box.slastR[j]);
                    }
                } else{
                    box.slastR = null;
                    box.svalPos = null;
                }
                box.slastTop = markStats[i].lastTop;
                box.shighEdge = markStats[i].highEdge;
            }
        }
        super.paintSelected(g);
    }
    
    static protected String getShortClassName() {
        return "Box";
    }
    
    protected void addLabelsAndTicks(PoGraSS g) {
        if (orientation==0) {
            //ay.setGeometry(Axis.O_Y,TH-mBottom,-(H=innerH));
            
            /* draw ticks and labels for Y axis */
            if(commonScale) {
                double f=ay.getSensibleTickDistance(30,18);
                double fi=ay.getSensibleTickStart(f);
                while (fi<ay.vBegin+ay.vLen) {
                    final int t=ay.getValuePos(fi);
                    g.drawLine(mLeft-5,t,mLeft,t);
                    labels.add(mLeft-7,t+5,1,0,ay.getDisplayableValue(fi));
                    fi+=f;
                }
                g.drawLine(mLeft,ay.gBegin,mLeft,ay.gBegin+ay.gLen);
            }
            
            if (vsCat || v.length>1) {
                /* draw labels for X axis */
                for(int i=0; i<xv.getNumCats(); i++){
                    labels.add(getAxCasePos(i),pc.getBounds().height-mBottom,0.5,0.5,boxwidth,(String)ax.getVariable().getCatAt(i));
                }
            }
        } else {
            
            /* draw ticks and labels for Y axis */
            if(commonScale) {
                double f=ay.getSensibleTickDistance(30,18);
                double fi=ay.getSensibleTickStart(f);
                while (fi<ay.vBegin+ay.vLen) {
                    final int t=ay.getValuePos(fi);
                    g.drawLine(t,TH-mBottom+4,t,TH-mBottom);
                    labels.add(t,TH-3,0.5,0,ay.getDisplayableValue(fi));
                    fi+=f;
                }
                g.drawLine(ay.gBegin,TH-mBottom,ay.gBegin+ay.gLen,TH-mBottom);
            }
            
            if (vsCat || v.length>1) {
                /* draw labels for X axis */
                for(int i=0; i<xv.getNumCats(); i++){
                    labels.add(mLeft-3,getAxCasePos(i),1,0,mLeft-3,(String)ax.getVariable().getCatAt(i));
                }
            }
        }
    }
    
    public String queryObject(final PlotPrimitive p) {
        PPrimBox box = (PPrimBox)p;
        if(box.queriedOutlier!=null)
            return "Outlier: " + Tools.getDisplayableValue(box.queriedOutlier.getValue());
        else
            return "lower hinge: " + Tools.getDisplayableValue(box.lhValue) + "\n" +
                    "median: " + Tools.getDisplayableValue(box.medValue) + "\n" +
                    "upper hinge: " + Tools.getDisplayableValue(box.uhValue) + "\n" +
                    "cases: " + box.cases();
    }
    
    protected void updateMargins() {
        switch(orientation){
            case 0:
                if(vsCat){
                    mLeft=defaultMLeft=commonScale?bigMLeft:smallMLeft;
                    mRight=defaultMRight=smallMRight;
                    mBottom=defaultMBottom=bigMBottom;
                    mTop=defaultMTop=smallMTop;
                } else{
                    mLeft=defaultMLeft=(commonScale || v.length==1)?bigMLeft:smallMLeft;
                    mRight=defaultMRight=smallMRight;
                    mBottom=defaultMBottom=(v.length==1)?smallMBottom:bigMBottom;
                    mTop=defaultMTop=smallMTop;
                }
                break;
            case 1:
                if(vsCat){
                    mLeft=defaultMLeft=bigMLeft;
                    mRight=defaultMRight=smallMRight;
                    mBottom=defaultMBottom=commonScale?bigMBottom:smallMBottom;
                    mTop=defaultMTop=smallMTop;
                } else{
                    mLeft=defaultMLeft=(v.length==1)?smallMLeft:bigMLeft;
                    mRight=defaultMRight=smallMRight;
                    mBottom=defaultMBottom=(commonScale || v.length==1)?bigMBottom:smallMBottom;
                    mTop=defaultMTop=smallMTop;
                }
                break;
        }
    }
    
    public boolean adjustMargin(final PoGraSS g) {
        if(orientation==0) return super.adjustMargin(g);
        else if (vsCat || v.length>1) {
            return super.adjustMargin(g);
        } else return false;
    }
}
