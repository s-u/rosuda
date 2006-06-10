package org.rosuda.ibase.plots;

import org.rosuda.ibase.toolkit.*;
import org.rosuda.ibase.*;
import org.rosuda.pograss.*;

import java.awt.*;

import org.rosuda.JRI.*;


public class CustomCanvas extends BaseCanvas {
	
	Rengine re;
	String rcall;
	SVar[] v;
	javax.swing.JFrame frame;

	public CustomCanvas(int gd, Frame f, SVar[] v, SMarker mark, String rcall) {
		super(gd,f,mark);
		
		allowDragMove=false;
		setDefaultMargins(new int[] {10,10,10,20,40,10,10,10});
		
		this.v=v;
		this.rcall=rcall;
		re=Rengine.getMainEngine();
		if(re==null) return;
		if (!re.waitForR()) {
            System.out.println("Cannot load R");
            return;
        }
		dontPaint=false;
	}
	
	public void updateObjects() {
		super.updateObjects();
		dontPaint=true;
//		re.eval(rcall);
//		ax.setVariable(v[0]);
//		ay.setVariable(v[1]);
		System.out.println("davor");
		re.eval("iagepyr.definition$construct(.iplots[[iplot.cur()]],300,300,dat)");
		System.out.println("dahinter");
		dontPaint=false;
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
    			pp[i].paint(g,orientation,m);
    		}
    	} else {
    		javax.swing.JFrame fr=new javax.swing.JFrame("kein pp vorhanden");
    		fr.setSize(300,300);
    		fr.setVisible(true);
    	}
    }
    
    public void drawAxes(Axis a) {}
    
    
    // the following two methods are only experimental and should be replaced by better ones
    public void addPP(PPrimRectangle p) {
    	String str;
    	if(p==null) System.out.println("P IS NULL");
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
