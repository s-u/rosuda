//
//  HistCanvas.java
//  Klimt
//
//  Created by Simon Urbanek on Thu Dec 05 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//
//  $Id$

package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** implementation of histograms (new version - based on BaseCanvas).
 * @version $Id$
 */
public class HistCanvas extends BaseCanvas {
    /** associated variable */
    protected SVar v;
    
    protected double anchor, binw;
    
    protected boolean inTick=false;
    
    protected int dragMode; // 0=none, 1=binw, 2=anchor
    protected int dragX;
    protected Rectangle tickMark1,tickMark2;
    
    protected int bars=22;
    
    /** creates a new histogram canvas
     * @param f frame owning this canvas or <code>null</code> if none
     * @param var source variable
     * @param mark associated marker
     */
    public HistCanvas(PlotComponent pc, Frame f, SVar var, SMarker mark) {
        super(pc,f,mark);
        v=var; setTitle("Histogram ("+v.getName()+")");
        ax=new Axis(var,Axis.O_X,Axis.T_Num); ax.addDepend(this);
        binw=ax.vLen/bars;
        anchor=v.getMin()-binw;
        ay=new Axis(var,Axis.O_Y,Axis.T_EqSize); ay.addDepend(this);
        String myMenu[]={"+","File","~File.Graph","~Edit","+","View","@RRotate","rotate","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        mLeft=40; mRight=10; mTop=10; mBottom=20;
        allow180=true;
        allowDragZoom=false;
        tickMark1=new Rectangle();
        tickMark2=new Rectangle();
    };
    
    public SVar getData(int id) { return (id==0)?v:null; }
    
    public void updateObjects() {
        Stopwatch sw=new Stopwatch();
        boolean recalcBars=true;
        // we should set recalcBar to false if anchor/binw didn't change
        bars=((int)((v.getMax()-anchor)/binw))+1;
        if (dragMode!=1)
            ax.setValueRange(anchor,bars*binw);
        if (pp==null || pp.length!=bars) {
            pp=new PlotPrimitive[bars];
            recalcBars=true;
        }
        
        if (recalcBars) {
            int i=0;
            while(i<bars) { pp[i]=new PPrimRectangle(); i++; };
            sw.profile("HistCanvasNew.updateObject reset primitives");
            
            int countMax=0;
            int count[]=new int[bars];
            int es=v.size();
            int id2bar[]=new int[es];
            i=0;
            while (i<es) {
                Object o=v.at(i);
                if (o!=null) {
                    double f=((Number)o).doubleValue();
                    int box=(int)((f-ax.vBegin)/binw);
                    id2bar[i]=box+1;
                    if (box>=0 && box<bars) {
                        count[box]++;
                        if (count[box]>countMax) countMax=count[box];
                    };
                };
                i++;
            };
            sw.profile("HistCanvasNew.updateObject calculate counts");
            i=0;
            ay.setValueRange(countMax);
            int bly=ay.getValuePos(0);
            while(i<es) {
                int b=id2bar[i];
                if (b>0) {
                    b--;
                    PPrimRectangle pr=(PPrimRectangle)pp[b];
                    if (pr.ref==null) {
                        pr.ref=new int[count[b]];
                        int ly=bly;
                        int x1=ax.getValuePos(ax.vBegin+b*binw);
                        int x2=ax.getValuePos(ax.vBegin+(b+1)*binw);
                        int vy=ay.getValuePos(count[b]);
                        if (orientation==0)
                            pr.r=new Rectangle(x1,vy,x2-x1,ly-vy);
                        else if (orientation==2)
                            pr.r=new Rectangle(x2,ly,x1-x2,vy-ly);
                        else if (orientation==1)
                            pr.r=new Rectangle(ly,x1,vy-ly,x2-x1);
                        else
                            pr.r=new Rectangle(vy,x2,ly-vy,x1-x2);
                    }
                    count[b]--;
                    pr.ref[count[b]]=i;
                }
                i++;
            }
            sw.profile("HistCanvasNew.updateObject create primitives");
        }
    }
    
    public void setHistParam(double anchor, double binw) {
        this.anchor=anchor; this.binw=binw;
        updateObjects();
        setUpdateRoot(0);
        repaint();
    }
    
    public double[] getHistParam() {
        double[] hp=new double[3];
        hp[0]=anchor; hp[1]=binw; hp[2]=v.getMax();
        return hp;
    }
    
    public void paintBack(PoGraSS g) {
        g.setColor("black");
        // draw border lines
        if (orientation!=3)
            g.drawLine(mLeft,H-mBottom,mLeft,mTop);
        else
            g.drawLine(W-mRight,H-mBottom,W-mRight,mTop);
        
        if (orientation!=2)
            g.drawLine(mLeft,H-mBottom,W-mRight,H-mBottom);
        else
            g.drawLine(mLeft,mTop,W-mRight,mTop);
        
        // draw lines to resize the bars
        int t1=ax.getValuePos(ax.vBegin);
        int t2=ax.getValuePos(ax.vBegin+binw);
        int x1,y1,w,h,x2,y2,lw,lh;
        if (orientation==0 || orientation==2) {
            x1=t1;
            x2=t2;
            y1=y2=((orientation==0)?(H-mBottom+5):5);
            w=5;
            lw=0;
            h=lh=10;
        } else {
            x1=x2=((orientation==1)?5:(W-mRight+5));
            y1=t1;
            y2=t2;
            w=lw=10;
            h=5;
            lh=0;
        }
        g.drawLine(x1, y1, x1+lw, y1+lh);
        g.drawLine(x2, y2, x2+lw, y2+lh);
        tickMark1.setSize(w,h);
        tickMark1.setLocation(x1,y1);
        tickMark2.setSize(w,h);
        tickMark2.setLocation(x2,y2);
        
        labels.clear();
        // draw y lables and ticks
        if (orientation==0 || orientation==2) {
            double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                int t=ay.getValuePos(fi);
                g.drawLine(mLeft-5,t,mLeft,t);
                labels.add(mLeft-8,t+5,1,0,ay.getDisplayableValue(fi));
                fi+=f;
            }
        } else {
            double f=ay.getSensibleTickDistance(50,35);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                int t=ay.getValuePos(fi);
                int bl = pc.getSize().height-mBottom;
                g.drawLine(t,bl,t,bl+5);
                labels.add(t,bl+5,0.5,1,ay.getDisplayableValue(fi));
                fi+=f;
            }
        }
        
        // draw x lables and ticks
        double f,fi;
        switch (orientation){
            case 0:
                f=ax.getSensibleTickDistance(50,35);
                fi=ax.getSensibleTickStart(f);
                while (fi<ax.vBegin+ax.vLen) {
                    int t=ax.getValuePos(fi);
                    int bl = pc.getSize().height-mBottom;
                    g.drawLine(t,bl,t,bl+5);
                    labels.add(t,bl+5,0.5,1,ax.getDisplayableValue(fi));
                    fi+=f;
                }
                break;
            case 2:
                f=ax.getSensibleTickDistance(50,35);
                fi=ax.getSensibleTickStart(f);
                while (fi<ax.vBegin+ax.vLen) {
                    int t=ax.getValuePos(fi);
                    g.drawLine(t,mTop-5,t,mTop);
                    labels.add(t,mTop-7,0.5,0,ax.getDisplayableValue(fi));
                    fi+=f;
                }
                break;
            case 1:
                f=ax.getSensibleTickDistance(50,18);
                fi=ax.getSensibleTickStart(f);
                while (fi<ax.vBegin+ax.vLen) {
                    int t=ax.getValuePos(fi);
                    g.drawLine(mLeft-5,t,mLeft,t);
                    labels.add(mLeft-8,t+5,1,0,ax.getDisplayableValue(fi));
                    fi+=f;
                }
                break;
            case 3:
                f=ax.getSensibleTickDistance(50,18);
                fi=ax.getSensibleTickStart(f);
                while (fi<ax.vBegin+ax.vLen) {
                    int t=ax.getValuePos(fi);
                    int rl = pc.getSize().width-mRight;
                    g.drawLine(rl,t,rl+5,t);
                    labels.add(rl+8,t+5,0,0,ax.getDisplayableValue(fi));
                    fi+=f;
                }
                break;
        }
        labels.finishAdd();
    }
    
    public void mousePressed(MouseEvent ev) {
        int x=ev.getX(), y=ev.getY();
        if(orientation==0 || orientation==2){
            if ((orientation==0 && y>H-mBottom) || (orientation==2 && y<mTop)) {
                if (x>mLeft-3 && x<mLeft+3) dragMode=2;
                int bwp=ax.getValuePos(ax.vBegin+binw);
                if (x>bwp-3 && x<bwp+3) dragMode=1;
                dragX=x;
            } else super.mousePressed(ev);
        } else {
            if ((orientation==1 && x<mLeft) || (orientation==3 && x>W-mRight)) {
                if (y>mTop-3 && y<mTop+3) dragMode=2;
                int bwp=ax.getValuePos(ax.vBegin+binw);
                if (y>bwp-3 && y<bwp+3) dragMode=1;
                dragX=y;
            } else super.mousePressed(ev);
        }
    };
    
    public void mouseReleased(MouseEvent e) {
        if (dragMode!=0) {
            dragMode=0;
            updateObjects();
            setUpdateRoot(0);
            repaint();
        } else super.mouseReleased(e);
    }
    
    public void mouseMoved(MouseEvent e) {
    	super.mouseMoved(e);
        int x=e.getX(), y=e.getY();
        if (tickMark1.contains(x,y) || tickMark2.contains(x,y)) {
            if (!inTick && !inZoom && !inQuery && !baseDrag) {
                inTick=true;
                pc.setCursor(Common.cur_tick);
            }
        } else {
            if (inTick) {
                inTick=false;
                pc.setCursor(Common.cur_arrow);
            }
        }
    }
    
    public void mouseDragged(MouseEvent e) {
        if (dragMode==0) {
            super.mouseDragged(e);
            return;
        }
        int x;
        if(orientation==0 || orientation==2){
            x=e.getX();
        } else{
            x=e.getY(); // sic!
        }
        if (x!=dragX) {
            if (dragMode==1) {
                double nbv=ax.getValueForPos(x);
                if (nbv-ax.vBegin>0) {
                    binw=nbv-ax.vBegin;
                    updateObjects();
                    setUpdateRoot(0);
                    repaint();
                };
            };
            if (dragMode==2) {
                double na=ax.getValueForPos(x);
                anchor=na; if (anchor>v.getMin()) anchor=v.getMin();
                if (anchor<v.getMin()-binw) anchor=v.getMin()-binw;
                updateObjects();
                setUpdateRoot(0);
                repaint();
            };
        };
    };
    
    public String queryObject(int i) {
        if (pp!=null && pp[i]!=null) {
            int mark=(int)(((double) pp[i].cases())*pp[i].getMarkedProportion(m,-1)+0.5);
            double la=ax.vBegin+binw*i;
            return "["+ax.getDisplayableValue(la)+", "+ax.getDisplayableValue(la+binw)+")\n"+((mark>0)?(""+mark+" of "+pp[i].cases()+" selected"):(""+pp[i].cases()+" cases"));
        }
        return "N/A";
    }
    
    public void rotate(int amount) {
        switch((orientation+amount)&3){
            case 0:
            case 1:
                mLeft=40; mRight=10; mTop=10; mBottom=20;
                break;
            case 2:
                mLeft=40; mRight=10; mTop=20; mBottom=10;
                break;
            case 3:
                mLeft=10; mRight=40; mTop=10; mBottom=20;
                break;
        }
        super.rotate(amount);
    }
}
