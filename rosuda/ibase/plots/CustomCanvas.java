package org.rosuda.ibase.plots;

import org.rosuda.ibase.toolkit.*;
import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.Tools;

import java.awt.*;

import org.rosuda.JRI.*;


public class CustomCanvas extends BaseCanvas {
	
	Rengine re;
	String rcall;
	SVar[] v;
	
	public CustomCanvas(int gd, Frame f, SVar[] v, SMarker mark, String rcall) {
		super(gd,f,mark);
		
		allowDragMove=false;
		setDefaultMargins(new int[] {10,10,10,20,40,10,10,10});
		
		this.v=v;
		this.rcall=rcall;
		Mutex.verbose=true;
		re=Rengine.getMainEngine();
		if(re==null) return;
		if (!re.waitForR()) {
            System.out.println("Cannot load R");
            return;
        }
		
        createMenu(f,true,false,false,true,null);

		dontPaint=false;
	}
	
	public synchronized void updateObjects() {
		super.updateObjects();
		System.out.println("davor");
		re.eval("iagepyr.definition$construct(.iplots[[iplot.cur()]],"+getWidth()+","+getHeight()+",dat)");
		System.out.println("dahinter");
		setUpdateRoot(0);
		repaint();
	}
	
	public void setAxis(Axis a) {
		if(a==null) return;
		
		int or=a.getOrientation();
		if(or==Axis.O_X) {
			if(ax==null) ax=a;
			else {
				if(opAx==null) opAx=new Axis[]{a};
				else {
					Axis[] temp=opAx; opAx=new Axis[temp.length+1];
					System.arraycopy(temp,0,opAx,0,temp.length);
					opAx[opAx.length-1]=a; temp=null;
				}
			}
		} else if(or==Axis.O_Y) {
			if(ay==null) ay=a;
			else {
				if(opAy==null) opAy=new Axis[]{a};
				else {
					Axis[] temp=opAy; opAy=new Axis[temp.length+1];
					System.arraycopy(temp,0,opAy,0,temp.length);
					opAy[opAy.length-1]=a; temp=null;
				}
			}
		} else {}
	}
	
    public void paintBack(final PoGraSS g) {
    	if(pp!=null) {
    		for(int i=0;i<pp.length;i++) {
    			if(pp[i] instanceof PPrimRectangle)
    				pp[i].paint(g,((PPrimRectangle)pp[i]).getOrientation(),m);
    			else
    				pp[i].paint(g,orientation,m);
    		}
    	}
    	if(ax!=null) drawAxes(g,ax);
    	if(ay!=null) drawAxes(g,ay);
    	if(opAx!=null) for(int i=0;i<opAx.length;i++) drawAxes(g,opAx[i]);
    	if(opAy!=null) for(int i=0;i<opAy.length;i++) drawAxes(g,opAy[i]);
    }
    
    protected void drawAxes(PoGraSS g, Axis a) {
        final Rectangle r=getBounds();
        final int w=r.width;
        final int h=r.height;

    	if(a.getOrientation()==Axis.O_X) g.drawLine(a.gBegin,h-mBottom,a.gBegin+a.gLen,h-mBottom);
    	else if(a.getOrientation()==Axis.O_Y) g.drawLine(w/2,a.gBegin,w/2,a.gBegin+a.gLen); // nur für age pyramide
    }
    
    public String queryObject(final int i) {
    	String qs="";
    	final boolean actionExtQuery=isExtQuery;
    	if(actionExtQuery) {
    		if(pp!=null && pp[i]!=null) {
    			qs="extended query";
    		}
    	} else {
    		if(pp!=null && pp[i]!=null) {
                final int mark=(int)((pp[i].cases())*pp[i].getMarkedProportion(m,-1)+0.5);
                qs+="count: "+pp[i].cases();
                qs+="\nselected: "+mark+" ("+Tools.getDisplayableValue(100.0*pp[i].getMarkedProportion(m, -1)  ,2)+"% of this cat., "+
                ((v!=null&&v[0]!=null)?Tools.getDisplayableValue(100.0*mark/((double)v[0].size()),2)+"% of total, ":"")+
                Tools.getDisplayableValue(100.0*mark/((double)m.marked()),2)+"% of total selection)";
    		}
    	}
    	return qs;
    }
    
    public String queryPlotSpace() {
        if(v==null) return null;
        else return (m.marked()>0?"Custom plot: "+m.marked()+" selected case(s)":"");
    }

    
    
    // the following two methods are only experimental and should be replaced by better ones
    public void addPP(PPrimRectangle p) {
    	if(p==null) {System.out.println("P IS NULL"); return;}
    	if(pp==null) pp=new PlotPrimitive[]{p};
    	PlotPrimitive[] temp=pp;
    	pp=new PlotPrimitive[temp.length+1];
    	System.arraycopy(temp,0,pp,0,temp.length);
    	pp[pp.length-1]=p;
    	temp=null;
    }
    
    public void resetPP() {
    	pp=null;
    }
	
}
