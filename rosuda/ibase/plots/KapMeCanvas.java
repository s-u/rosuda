package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.PrintStream;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** implementation of Kaplan-Meier survival estimate plot (uses {@link BaseCanvas})
@version $Id$
*/
public class KapMeCanvas extends BaseCanvas
{
    SVar vEvent, vTime;

    double[] kmX, kmY, kmC;

    int filter[]=null;
    
    public boolean showCounts=true;
    
    public int calcKM(int[] ranks) {
        int times=0;
        double last=-1;
        
        int i=0;
        while (i<ranks.length) {
            double d=vTime.atD(ranks[i++]);
            if (!Double.isNaN(d) && d!=last) {
                times++;
                last=d;
            }
        }

        kmX=new double[times+2];
        kmY=new double[times+2];
        kmC=new double[times+2];

        int kmp=1;
        kmX[0]=0; kmY[0]=1; kmC[0]=1;

        double s=1.0;
        int n=ranks.length;
        int d=0, dsc=0;
        last=-1;
        
        i=0;
        double t=1.0;
        while (i<ranks.length) {
            t=vTime.atD(ranks[i]);
            if (!Double.isNaN(t) && t!=last) {
                if (last!=-1) {
                    s=s*((double)(n-d))/((double)n);
                    kmX[kmp]=t;
                    kmY[kmp]=s;
                    kmC[kmp]=((double)(n-d)/((double)ranks.length));
                    kmp++;
                }
                n-=dsc;
                last=t;
                d=0;
                dsc=0;
            }
            dsc++;
            if (vEvent.atS(ranks[i]).equals("dead"))
                d++;
            i++;
        }
        s=s*((double)(n-d))/((double)n);
        kmX[kmp]=t;
        kmY[kmp]=s;
        kmC[kmp]=((double)(n-d))/((double)ranks.length);
        return kmp+1;
    }
    
    public KapMeCanvas(Frame f, SVar time, SVar event, SMarker mark) {
        super(f,mark);
        setTitle("Kaplan-Meier Plot");
        allow180=false;

        vTime=time; vEvent=event;
        ay=new Axis(null,Axis.O_Y,Axis.T_Num); ay.addDepend(this); ay.setValueRange(0,1);
        ax=new Axis(vTime,Axis.O_X,Axis.T_Num); ax.addDepend(this);

        String myMenu[]={"+","File","~File.Graph","~Edit","+","View","Hide counts","counts","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        mLeft=mRight=mTop=mBottom=10;

        pp=null;
    }

    public void updateObjects() {
        int[] tRanks = vTime.getRanked();
        calcKM(tRanks);
    }

    public void paintKM(PoGraSS g) {
        int i=1;
        int x=ax.getValuePos(kmX[0]);
        int y=ay.getValuePos(kmY[0]);
        while (i<kmX.length) {
            int x2=ax.getValuePos(kmX[i]);
            int y2=ay.getValuePos(kmY[i]);

            if (y2==y) {
                g.drawLine(x,y,x2,y2);
            } else {
                g.drawLine(x,y,x,y2); g.drawLine(x,y2,x2,y2);
            }
            x=x2; y=y2;
            i++;
        }
    }

    public void paintInit(PoGraSS g) {
        float[] scc=Common.selectColor.getRGBComponents(null);
        g.defineColor("invMark",scc[0],scc[1],scc[2],0.3f);
        g.defineColor("counts",0f,0f,0f,0.2f);
        g.defineColor("countsMark",scc[0],scc[1],scc[2],0.2f);
        g.defineColor("countsShadow",0f,0f,0f,0.1f);
        g.defineColor("backShadow",0f,0f,0.5f,0.3f);
    }

    public void paintCounts(PoGraSS g, double weight) {
        if (!showCounts) return;
        int i=1;
        int x=ax.getValuePos(kmX[0]);
        int y2=ay.getValuePos(0);
        while (i<kmX.length) {
            int x2=ax.getValuePos(kmX[i]);
            int y=ay.getValuePos(kmC[i]*weight);
            g.fillRect(x,y,x2-x,y2-y);
            x=x2;
            i++;
        }
    }
    
    public void paintBack(PoGraSS g) {
        if (kmX==null) return;

        if (filter==null) { // no filter=everything is cached
            g.setColor("counts");
            paintCounts(g,1.0);
            g.setColor("back");
            paintKM(g);
        } else { // with filter we have to re-build KM
            g.setColor("countsShadow");
            paintCounts(g,1.0);
            g.setColor("backShadow");
            paintKM(g);

            int[] map = new int[vTime.size()];
            int i=0;
            while (i<filter.length) { map[filter[i++]]=-2; }
            int[] fullRanks = vTime.getRanked();
            int[] tRanks = SVar.filterRanksByMap(fullRanks, map, -2);

            double[] sX=kmX;
            double[] sY=kmY;
            double[] sC=kmC;
            if (tRanks==null || tRanks.length<2) return;
            calcKM(tRanks);
            g.setColor("counts");
            paintCounts(g,((double)tRanks.length/((double)vTime.size())));
            g.setColor("back");
            paintKM(g);
            kmX=sX;
            kmY=sY;
            kmC=sC;
        }
    }

    public void setFilter(int[] filter) {
        this.filter=filter;
        setUpdateRoot(0);
        repaint();
    }
    
    public void paintSelected(PoGraSS g) {
        double[] sX=kmX;
        double[] sY=kmY;
        double[] sC=kmC;
        int[] fullRanks = vTime.getRanked();
        int[] map = m.getMaskCopy(SMarker.MASK_PRIMARY);
        int delta = 0;
        if (filter!=null) {
            int i=0;
            while (i<filter.length) { map[filter[i++]]+=2; }
            delta=2;
        }
            
        int[] tRanks = SVar.filterRanksByMap(fullRanks, map, -1+delta);
        if (tRanks==null || tRanks.length<2) return;
        calcKM(tRanks);
        g.setColor("countsMark");
        paintCounts(g,((double)tRanks.length/((double)vTime.size())));
        g.setColor("marked");
        paintKM(g);
        kmX=sX;
        kmY=sY;
        kmC=sC;

        tRanks = SVar.filterRanksByMap(fullRanks, map, delta);
        if (tRanks==null || tRanks.length<2) return;
        calcKM(tRanks);
        g.setColor("invMark");
        paintKM(g);

        kmX=sX;
        kmY=sY;
        kmC=sC;
    }

    public void keyTyped(KeyEvent e)
    {
        super.keyTyped(e);
        if (e.getKeyChar()=='c') run(this,"counts");
    }
    
    public Object run(Object o, String cmd) {
        super.run(o,cmd);
        if (m!=null) m.run(o,cmd);
        if (cmd=="counts") {
            showCounts=!showCounts;
            EzMenu.getItem(getFrame(),"counts").setLabel((showCounts)?"Hide counts":"Show counts");
            setUpdateRoot(0);
            repaint();
        }
        return null;
    }        
}
