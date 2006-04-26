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

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** implementation of histograms (new version - based on BaseCanvas).
 * @version $Id$
 */
public class HistCanvas extends BaseCanvas {
    static final String M_PLUS = "+";
    static final String M_BINUP = "binUp";
    static final String M_BINDOWN = "binDown";
    static final String M_ANCHORLEFT = "anchorLeft";
    static final String M_ANCHORRIGHT = "anchorRight";
    /** associated variable */
    protected SVar v;
    
    protected double anchor, binw;
    
    private static final int DRAGMODE_NONE = 0;
    private static final int DRAGMODE_BINW = 1;
    private static final int DRAGMODE_ANCHOR = 2;
    
    protected int dragMode;
    protected int dragX;
    
    protected int dragBinwBars; // number of bars left of the cursor while dragging (binw)
    
    protected int bars=22;
    
    // needed for axis-query
    private int[] axcoordX,axcoordY;
    private int[] aycoordX,aycoordY;
    
    private int paintpp;
    
    private double maxVal=Double.NEGATIVE_INFINITY,minVal=Double.POSITIVE_INFINITY;
    
    private boolean crosshairs = false;
    private int qx,qy;
    
    /** creates a new histogram canvas
     * @param f frame owning this canvas or <code>null</code> if none
     * @param var source variable
     * @param mark associated marker
     */
    public HistCanvas(final int gd, final Frame f, final SVar var, final SMarker mark) {
        super(gd,f,mark);
        v=var; setTitle("Histogram ("+v.getName()+")");
        ax=new Axis(var,Axis.O_X,Axis.T_Num); ax.addDepend(this);
        binw=ax.vLen/bars;
        anchor=v.getMin()-binw;
        ay=new Axis(var,Axis.O_Y,Axis.T_EqSize); ay.addDepend(this);
        createMenu(f,true,false,false,new String[]{
            "Increase bin width (up)",M_BINUP,
            "Decrease bin width (down)",M_BINDOWN,
            "Move anchor left (left)",M_ANCHORLEFT,
            "Move anchor right (right)",M_ANCHORRIGHT
        });
        
        setDefaultMargins(new int[] {20,10,10,20, 40,10,10,20, 40,10,20,10, 10,40,10,20});
        
        axcoordX=new int[2]; axcoordY=new int[2];
        aycoordX=new int[2]; aycoordY=new int[2];
        allow180=true;
        allowDragZoom=false;
        
        dontPaint=false;
    };
    
    private void setBoundValues() {
        if(pp==null) return;
        double temp=0;
        for(int i=0;i<pp.length;i++) {
            temp=pp[i].cases();
            if(maxVal<temp) maxVal=temp;
            if(minVal>temp) minVal=temp;
        }
    }
    
    public SVar getData(final int id) { return (id==0)?v:null; }
    
    public void updateObjects() {
        final Stopwatch sw=new Stopwatch();
        
        // we should set recalcBar to false if anchor/binw didn't change
        bars=((int)((v.getMax()-anchor)/binw))+1;
        if (dragMode!=DRAGMODE_BINW)
            ax.setValueRange(anchor,bars*binw);
        boolean recalcBars = true;
        if (pp==null || pp.length!=bars) {
            pp=new PlotPrimitive[bars];
            recalcBars=true;
        }
        
        if (recalcBars) {
            paintpp=0;
            int i=0;
            while(i<bars) { pp[i]=new PPrimRectangle(); i++; }
            sw.profile("HistCanvasNew.updateObject reset primitives");
            
            
            final int count[]=new int[bars];
            final int es=v.size();
            final int id2bar[]=new int[es];
            i=0;
            int countMax = 0;
            while (i<es) {
                final Object o=v.at(i);
                if (o!=null) {
                    final double f=((Number)o).doubleValue();
                    final int box=(int)((f-ax.vBegin)/binw);
                    id2bar[i]=box+1;
                    if (box>=0 && box<bars) {
                        count[box]++;
                        if (count[box]>countMax) countMax=count[box];
                    }
                }
                i++;
            }
            sw.profile("HistCanvasNew.updateObject calculate counts");
            i=0;
            ay.setValueRange(countMax);
            final int bly=ay.getValuePos(0);
            while(i<es) {
                int b=id2bar[i];
                if (b>0) {
                    b--;
                    final PPrimRectangle pr=(PPrimRectangle)pp[b];
                    if (pr.ref==null) {
                        pr.ref=new int[count[b]];
                        final int ly=bly;
                        final int x1=ax.getValuePos(ax.vBegin+b*binw);
                        final int x2=ax.getValuePos(ax.vBegin+(b+1)*binw);
                        final int vy=ay.getValuePos(count[b]);
                        if (orientation==0)
                            pr.setBounds(x1,vy,x2-x1,ly-vy);
                        else if (orientation==2)
                            pr.setBounds(x2,ly,x1-x2,vy-ly);
                        else if (orientation==1)
                            pr.setBounds(ly,x1,vy-ly,x2-x1);
                        else
                            pr.setBounds(vy,x2,ly-vy,x1-x2);
                        paintpp++;
                    }
                    count[b]--;
                    pr.ref[count[b]]=i;
                }
                i++;
            }
            setBoundValues();
            sw.profile("HistCanvasNew.updateObject create primitives");
        }
    }
    
    public void setHistParam(final double anchor, final double binw) {
        this.anchor=anchor; this.binw=binw;
        updateObjects();
        setUpdateRoot(0);
        repaint();
    }
    
    public double[] getHistParam() {
        final double[] hp=new double[3];
        hp[0]=anchor; hp[1]=binw; hp[2]=v.getMax();
        return hp;
    }
    
    public void paintBack(final PoGraSS g) {
        int maxLabelLength=0;
        {
            final Axis axis = (orientation==0 || orientation==2)?ay:ax;
            final double f=axis.getSensibleTickDistance(50,18);
            double fi=axis.getSensibleTickStart(f);
            while (fi<axis.vBegin+axis.vLen) {
                final String s=axis.getDisplayableValue(fi);
                if(s.length()>maxLabelLength) maxLabelLength=s.length();
                fi+=f;
            }
        }
        
        if(orientation==3){
            final int omRight=mRight;
            if(maxLabelLength*8>20){
                mRight = maxLabelLength*8+2;
            } else mRight=20;
            if(mRight!=omRight) updateObjects();
        } else{
            final int omLeft=mLeft;
            if(maxLabelLength*8>20){
                mLeft = maxLabelLength*8+2;
            } else mLeft=20;
            if(mLeft!=omLeft) updateObjects();
        }
        
        g.setColor(COL_OUTLINE);
        
        // draw axes
        if(orientation==0) {
            setAxCoord(mLeft,H-mBottom,W-mRight,H-mBottom);
            setAyCoord(mLeft,H-mBottom,mLeft,mTop);
        } else if(orientation==1) {
            setAxCoord(mLeft,H-mBottom-100,mLeft,mTop);
            setAyCoord(mLeft,H-mBottom,W-mRight,H-mBottom);
        } else if(orientation==2) {
            setAxCoord(mLeft,mTop,W-mRight,mTop);
            setAyCoord(mLeft,H-mBottom,mLeft,mTop);
        } else if(orientation==3) {
            setAxCoord(W-mRight,H-mBottom,W-mRight,mTop);
            setAyCoord(mLeft,H-mBottom,W-mRight,H-mBottom);
        }
        
        g.drawLine(axcoordX[0],axcoordY[0],axcoordX[1],axcoordY[1]);
        g.drawLine(aycoordX[0],aycoordY[0],aycoordX[1],aycoordY[1]);
        
        labels.clear();
        // draw y lables and ticks
        if (orientation==0 || orientation==2) {
            final double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                final int t=ay.getValuePos(fi);
                g.drawLine(mLeft-5,t,mLeft,t);
                labels.add(mLeft-8,t+5,1,0,ay.getDisplayableValue(fi));
                fi+=f;
            }
        } else {
            final double f=ay.getSensibleTickDistance(50,35);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                final int t=ay.getValuePos(fi);
                final int bl = getSize().height-mBottom;
                g.drawLine(t,bl,t,bl+5);
                labels.add(t,bl+5,0.5,1,ay.getDisplayableValue(fi));
                fi+=f;
            }
        }
        
        // draw x lables and ticks
        final double f;
        double fi;
        switch (orientation){
            case 0:
                f=ax.getSensibleTickDistance(50,35);
                fi=ax.getSensibleTickStart(f);
                while (fi<ax.vBegin+ax.vLen) {
                    final int t=ax.getValuePos(fi);
                    final int bl = getSize().height-mBottom;
                    g.drawLine(t,bl,t,bl+5);
                    labels.add(t,bl+5,0.5,1,ax.getDisplayableValue(fi));
                    fi+=f;
                }
                break;
            case 2:
                f=ax.getSensibleTickDistance(50,35);
                fi=ax.getSensibleTickStart(f);
                while (fi<ax.vBegin+ax.vLen) {
                    final int t=ax.getValuePos(fi);
                    g.drawLine(t,mTop-5,t,mTop);
                    labels.add(t,mTop-7,0.5,0,ax.getDisplayableValue(fi));
                    fi+=f;
                }
                break;
            case 1:
                f=ax.getSensibleTickDistance(50,18);
                fi=ax.getSensibleTickStart(f);
                while (fi<ax.vBegin+ax.vLen) {
                    final int t=ax.getValuePos(fi);
                    g.drawLine(mLeft-5,t,mLeft,t);
                    labels.add(mLeft-8,t+5,1,0,ax.getDisplayableValue(fi));
                    fi+=f;
                }
                break;
            case 3:
                f=ax.getSensibleTickDistance(50,18);
                fi=ax.getSensibleTickStart(f);
                while (fi<ax.vBegin+ax.vLen) {
                    final int t=ax.getValuePos(fi);
                    final int rl = getSize().width-mRight;
                    g.drawLine(rl,t,rl+5,t);
                    labels.add(rl+8,t+5,0,0,ax.getDisplayableValue(fi));
                    fi+=f;
                }
                break;
        }
        labels.finishAdd();
    }
    
    public void paintPost(PoGraSS g) {
        if (crosshairs) {
            g.setColor(COL_OUTLINE);
            if((orientation&1) == 0){ // no rotation or 180°
                if (qx==ax.clip(qx) && qy==ay.clip(qy)) {
                    g.drawLine(ax.gBegin,qy,ax.gBegin+ax.gLen,qy);
                    g.drawLine(qx,ay.gBegin,qx,ay.gBegin+ay.gLen);
                    g.drawString(ay.getDisplayableValue(ax.getValueForPos(qx)),qx+2,getHeight()-mBottom-2);
                    g.drawString(ay.getDisplayableValue(ay.getValueForPos(qy)),mLeft+2,qy+11);
                }
            } else {
                if (qx==ay.clip(qx) && qy==ax.clip(qy)) {
                    g.drawLine(qx,ax.gBegin,qx,ax.gBegin+ax.gLen);
                    g.drawLine(ay.gBegin,qy,ay.gBegin+ay.gLen,qy);
                    g.drawString(ax.getDisplayableValue(ay.getValueForPos(qx)),qx+2,getHeight()-mBottom-2);
                    g.drawString(ax.getDisplayableValue(ax.getValueForPos(qy)),mLeft+2,qy+11);
                }
            }
        }
        super.paintPost(g);
    }
    
    public void mousePressed(final MouseEvent ev) {
        final int x=ev.getX();
        final int y=ev.getY();
        if(orientation==0 || orientation==2){
            if (Common.isMoveTrigger(ev) && (orientation==0 && y<=H-mBottom) || (orientation==2 && y>=mTop)) {
                
                dragBinwBars=-1;
                double bwp;
                while((orientation==0 && x>(bwp=ax.getValuePos(ax.vBegin+(dragBinwBars+1)*binw))-3)
                || (orientation==2 && x<(bwp=ax.getValuePos(ax.vBegin+(dragBinwBars+1)*binw))+3)){
                    if (x<bwp+3) dragMode=DRAGMODE_BINW;
                    dragBinwBars++;
                }
                if(dragBinwBars==0) dragMode=DRAGMODE_ANCHOR;
                dragX=x;
            } else super.mousePressed(ev);
        } else {
            if (Common.isMoveTrigger(ev) && ((orientation==1 && x>=mLeft) || (orientation==3 && x<=W-mRight))) {
                
                dragBinwBars=-1;
                double bwp;
                while((orientation==1 && y>(bwp=ax.getValuePos(ax.vBegin+(dragBinwBars+1)*binw))-3)
                || (orientation==3 && y<(bwp=ax.getValuePos(ax.vBegin+(dragBinwBars+1)*binw))+3)){
                    if (y<bwp+3) dragMode=DRAGMODE_BINW;
                    dragBinwBars++;
                }
                if(dragBinwBars==0) dragMode=DRAGMODE_ANCHOR;
                dragX=y;
            } else super.mousePressed(ev);
        }
    };
    
    public void mouseMoved(final MouseEvent ev) {
        super.mouseMoved(ev);
        final boolean ocrosshairs = crosshairs;
        crosshairs = ev.getModifiersEx()==MouseEvent.SHIFT_DOWN_MASK;
        qx=ev.getX();
        qy=ev.getY();
        if(crosshairs || crosshairs!=ocrosshairs){
            setUpdateRoot(3); repaint();
        }
    }
    
    public void mouseReleased(final MouseEvent e) {
        if (dragMode!=DRAGMODE_NONE) {
            dragMode=DRAGMODE_NONE;
            updateObjects();
            setUpdateRoot(0);
            repaint();
        } else super.mouseReleased(e);
    }
    
    public void mouseDragged(final MouseEvent e) {
        if (dragMode==DRAGMODE_NONE) {
            super.mouseDragged(e);
            return;
        }
        final int x;
        if(orientation==0 || orientation==2){
            x=e.getX();
        } else{
            x=e.getY(); // sic!
        }
        if (x!=dragX) {
            if (dragMode==DRAGMODE_BINW) {
                final double nbv=ax.getValueForPos(x);
                if (nbv-ax.vBegin>0) {
                    binw=(nbv-ax.vBegin)/dragBinwBars;
                    updateObjects();
                    setUpdateRoot(0);
                    repaint();
                }
            }
            if (dragMode==DRAGMODE_ANCHOR) {
                final double na=ax.getValueForPos(x);
                anchor=na; if (anchor>v.getMin()) anchor=v.getMin();
                if (anchor<v.getMin()-binw) anchor=v.getMin()-binw;
                updateObjects();
                setUpdateRoot(0);
                repaint();
            }
        }
    };
    
    public String queryObject(int i) {
        String qs="";
        boolean actionExtQuery = isExtQuery;
        if(actionExtQuery) {
            if (pp!=null && pp[i]!=null) {
                int mark=(int)(((double) pp[i].cases())*pp[i].getMarkedProportion(m,-1)+0.5);
                double la=ax.vBegin+binw*i;
                qs =  "["+ax.getDisplayableValue(la)+", "+ax.getDisplayableValue(la+binw)+")\n";
                if(mark>0) {
                    qs+="count: "+pp[i].cases()+" ("+Tools.getDisplayableValue(100.0*(pp[i].cases())/((double)v.size()),2)+"% of total)\n"+
                            "selected: "+mark+" ("+Tools.getDisplayableValue(100.0*pp[i].getMarkedProportion(m, -1)  ,2)+"% of this cat., "+
                            Tools.getDisplayableValue(100.0*(mark)/((double)v.size()),2)+"% of total, "+
                            Tools.getDisplayableValue(100.0*(mark)/((double)m.marked()),2)+"% of total selection)";
                } else {
                    qs += "count: "+pp[i].cases()+" ("+
                            Tools.getDisplayableValue(100.0*((double)pp[i].cases())/((double)v.size()),2)+
                            "% of total)";
                }
            } else qs = "N/A";
        } else {
            if (pp!=null && pp[i]!=null) {
                int mark=(int)(((double) pp[i].cases())*pp[i].getMarkedProportion(m,-1)+0.5);
                double la=ax.vBegin+binw*i;
                qs =  "["+ax.getDisplayableValue(la)+", "+ax.getDisplayableValue(la+binw)+")\n"+
                        "count: "+pp[i].cases()+(mark>0?"\nselected: "+mark:"");
            } else qs = "N/A";
        }
        return qs;
    }
    
    public String queryPlotSpace() {
        if(v==null) return null;
        else return "Histogram\nmin: "+minVal+"\nmax: "+maxVal+"\nconsist of "+paintpp+" bins"+(m.marked()>0?"\n"+m.marked()+" selected case(s)":"");
    }
    
    public void keyPressed(final KeyEvent e) {
        switch(e.getKeyCode()){
            case (KeyEvent.VK_UP): run(this,M_BINUP); break;
            case (KeyEvent.VK_DOWN): run(this,M_BINDOWN); break;
            case (KeyEvent.VK_LEFT): run(this,M_ANCHORLEFT); break;
            case (KeyEvent.VK_RIGHT): run(this,M_ANCHORRIGHT); break;
        }
    }
    
    public Object run(final Object o, final String cmd) {
        super.run(o,cmd);
        
        if(M_BINUP.equals(cmd)) {
            binw*=1.1;
            updateObjects();
            setUpdateRoot(0);
            repaint();
        }
        if(M_BINDOWN.equals(cmd)){
            final double newBinw=Math.min(binw/1.1, 1);
            if(Math.abs(newBinw-binw)>0.00001){
                binw=newBinw;
                updateObjects();
                setUpdateRoot(0);
                repaint();
            }
        }
        if (M_ANCHORRIGHT.equals(cmd)) {
            anchor = Math.min(anchor+0.1*binw, v.getMin());
            updateObjects();
            setUpdateRoot(0);
            repaint();
        }
        if (M_ANCHORLEFT.equals(cmd)) {
            anchor = Math.max(anchor-0.1*binw, v.getMin()-binw);
            updateObjects();
            setUpdateRoot(0);
            repaint();
        }
        return null;
    }
    
    private void setAxCoord(int x1,int y1,int x2,int y2) {
        if(x1<x2) {axcoordX[0]=x1; axcoordX[1]=x2;} else {axcoordX[0]=x2; axcoordX[1]=x1;}
        if(y1<y2) {axcoordY[0]=y1; axcoordY[1]=y2;} else {axcoordY[0]=y2; axcoordY[1]=y1;}
    }
    
    private void setAyCoord(int x1,int y1,int x2,int y2) {
        if(x1<x2) {aycoordX[0]=x1; aycoordX[1]=x2;} else {aycoordX[0]=x2; aycoordX[1]=x1;}
        if(y1<y2) {aycoordY[0]=y1; aycoordY[1]=y2;} else {aycoordY[0]=y2; aycoordY[1]=y1;}
    }
    
    protected Axis getMouseOverAxis(int x, int y) {
        if(x>=axcoordX[0]-2 && x<= axcoordX[1]+2 && y>=axcoordY[0]-2 && y<=axcoordY[1]+2) return ax;
        else if(x>=aycoordX[0]-2 && x<= aycoordX[1]+2 && y>=aycoordY[0]-2 && y<=aycoordY[1]+2) return ay;
        else return null;
    }
    
    protected String getAxisQuery(int x, int y) {
//    	System.out.println("x: " + x + ", y: " + y + ", axX[0]: " + axcoordX[0] + ", axX[1]: " + axcoordX[1] + ", axY[0]: " + axcoordY[0] + ", axY[1]: " + axcoordY[1]);
        Axis a=getMouseOverAxis(x,y);
        if(a==null) return null;
        return "axis name: " + a.getVariable().getName()+
                "\nbin width: " + Tools.getDisplayableValue(binw,2)+
                "\nanchor: "  + Tools.getDisplayableValue(anchor,2)+
                (v.hasMissing()?"\nmissings: "+v.getMissingCount():"");
    }
}
