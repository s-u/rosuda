//
//  HistCanvasNew.java
//  Klimt
//
//  Created by Simon Urbanek on Thu Dec 05 2002.
//  Copyright (c) 2002 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/** implementation of histograms (new version - based on BaseCanvas).
    @version $Id$
*/
public class HistCanvasNew extends BaseCanvas
{
    /** associated variable */
    SVar v;

    double anchor, binw;

    boolean inTick=false;

    int dragMode; // 0=none, 1=binw, 2=anchor
    int dragX;
    int tickMark1,tickMark2;
    
    int bars=22;

    /** creates a new histogram canvas
	@param f frame owning this canvas or <code>null</code> if none
	@param var source variable
	@param mark associated marker
    */
    public HistCanvasNew(Frame f, SVar var, SMarker mark) {
        super(f,mark);
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
    };

    public void updateObjects() {
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
            while(i<bars) { pp[i]=new PlotPrimitive(); i++; };
            
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
            i=0;
            ay.setValueRange(countMax);
            int ly=ay.getValuePos(0);
            while(i<es) {
                int b=id2bar[i];
                if (b>0) {
                    b--;
                    if (pp[b].ref==null) {
                        pp[b].ref=new int[count[b]];
                        int x1=ax.getValuePos(ax.vBegin+b*binw);
                        int x2=ax.getValuePos(ax.vBegin+(b+1)*binw);
                        int vy=ay.getValuePos(count[b]);
                        if (orientation==0 || orientation==2)
                            pp[b].r=new Rectangle(x1,vy,x2-x1,ly-vy);
                        else
                            pp[b].r=new Rectangle(vy,x1,ly-vy,x2-x1);
                    }
                    count[b]--;
                    pp[b].ref[count[b]]=i;
                }
                i++;
            }
        }
    }

    public void paintBack(PoGraSS g) {        
        g.setColor("black");
        g.drawLine(mLeft,H-mBottom,mLeft,mTop);
        g.drawLine(mLeft,H-mBottom,W-mRight,H-mBottom);

        tickMark1=ax.getValuePos(ax.vBegin);
        tickMark2=ax.getValuePos(ax.vBegin+binw);

        g.drawLine(tickMark1,H-mBottom+5,tickMark1,H-5);
        g.drawLine(tickMark2,H-mBottom+5,tickMark2,H-5);

        if (orientation==0 || orientation==2) {
            double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                int t=ay.getValuePos(fi);
                g.drawLine(mLeft-5,t,mLeft,t);
                String s=""+fi;
                if (s.length()>2 && s.substring(s.length()-2).compareTo(".0")==0)
                    s=s.substring(0,s.length()-2);
                g.drawString(s,5,t+5);
                fi+=f;
            }
        }
    }
    
    public void paintPost(PoGraSS g) {
        SNode cn=(m!=null)?m.getNode():null;
        if (cn!=null && cn.splitVar==v) {
            g.setColor("red");
            g.drawLine(ax.getValuePos(cn.splitValF),mTop,ax.getValuePos(cn.splitValF),H-mBottom);
        }
    }
    
    public void mousePressed(MouseEvent ev) {
	int x=ev.getX(), y=ev.getY();
	if (y>H-mBottom) {
	    if (x>mLeft-3 && x<mLeft+3) dragMode=2;
	    int bwp=ax.getValuePos(ax.vBegin+binw);
	    if (x>bwp-3 && x<bwp+3) dragMode=1;
	    dragX=x;
        } else super.mousePressed(ev);
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
        int x=e.getX(), y=e.getY();
        if (y>H-mBottom && ((x>tickMark1-3 && x<tickMark1+3) || (x>tickMark2-3 && x<tickMark2+3))) {
            if (!inTick && !inZoom && !inQuery && !baseDrag) {
                inTick=true;
                setCursor(Common.cur_tick);
            }
        } else {
            if (inTick) {
                inTick=false;
                setCursor(Common.cur_arrow);
            }
        }
    }
    
    public void mouseDragged(MouseEvent e) {
        if (dragMode==0) {
            super.mouseDragged(e);
            return;
        }
	int x=e.getX();
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
        if (pp!=null && pp[i]!=null && pp[i].ref!=null) {
            int mark=(int)(((double) pp[i].ref.length)*pp[i].getMarkedProportion(m,-1)+0.5);
            double la=ax.vBegin+binw*i;
            return "Interval: ["+ax.getDisplayableValue(la)+" - "+ax.getDisplayableValue(la+binw)+")\nCounts: "+mark+" of "+pp[i].ref.length+" selected";
        };
        return "N/A";
    }
    
    public Object run(Object o, String cmd) {
	super.run(o,cmd);
        if (m!=null) m.run(o,cmd);
        if (cmd=="print") run(o,"exportPS");
        if (cmd=="exportCases") {
	    try {
		PrintStream p=Tools.getNewOutputStreamDlg(myFrame,"Export selected cases to ...","selected.txt");
		if (p!=null) {
		    p.println(v.getName());
		    int i=0;
		    for (Enumeration e=v.elements(); e.hasMoreElements();) {
			Object oo=e.nextElement();
			if (m.at(i)) {
			    if (oo!=null)
				p.println(oo.toString());
			    else 
				p.println("NA");
			};
			i++;
		    };
		    p.close();
		};
	    } catch (Exception eee) {};
	};
	if (cmd=="exit") WinTracker.current.Exit();
	return null;
    };
};
