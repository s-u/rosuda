package org.rosuda.ibase.plots;

import java.awt.Frame;
import java.awt.Rectangle;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.PoGraSS;

public class MosaicCanvas extends BaseCanvas {
	
	SVar[] v;
	int vs;
	
	int w,h,hgap,vgap; //width, height and gaps between mosaics
	double mUse = 0.95; //who much space we use
    
	int hPPs=1,vPPs=1,pps=1;  //amount of mosaics per row, col and all
	
	static final int OBSERVED = 0; 
	static final int FLUCT = 1;
	
	int mode = OBSERVED;
	
	FrequencyTable ft; 
	
	public MosaicCanvas(Frame f, SVar[] vars, SMarker mark) {
		super(f, mark);
		setTitle("Mosaic Plot");
		this.v = vars;
		this.vs = v.length; 
		String myMenu[]={"+","File","~File.Graph","+","View","Observed","observed","Fluctuation","fluctuation","~Edit","~Window","0"};
		EzMenu.getEzMenu(f,this,myMenu);
		mLeft=10; mRight=10; mTop=10; mBottom=10;
        for (int i = 0; i < v.length;) {
        	hPPs *= v[i].getNumCats();
        	pps *= v[i].getNumCats();
        	i++;
        	if (i < v.length) {
        		vPPs *= v[i].getNumCats();
        		pps *= v[i].getNumCats();
        		i++;
        	}
        }
        ft = new FrequencyTable(vars,hPPs,vPPs);
	}
	
	public void updateObjects() {
		int lx=mLeft,ly=mTop,sx=0,sy=0;
		w = super.W - mLeft - mRight;
        h = super.H - mTop - mBottom;
        hgap = (int) (w*(1-mUse)/(hPPs-1));
        vgap = (int) (h*(1-mUse)/(vPPs-1));
        sx = (int) (w*mUse/hPPs);
        sy = (int) (h*mUse/vPPs);
        
		if (pp==null || pp.length != pps) {
            pp=new PlotPrimitive[pps];
        }
        int mw=0,mh=0,ccnts=0,i = 0;
        
        while(i<pps) { pp[i]=new PPrimMosaic(); i++; };
        if (mode==OBSERVED) {
        	for (int c = 0; c < hPPs; c++) {
        		lx += mw+(c!=0?hgap:0);
        		mw = (int) (w*ft.getColCases(c)/ft.getCasesSize()*mUse);
        		ly = mTop; mh=0;
        		for (int r = 0; r < vPPs; r++) {
        			ly += mh+(r!=0?vgap:0);
        			ccnts = ft.getCountsAt(r,c);
        			mh = (int) (h*ccnts/ft.getColCases(c)*mUse);
        			PPrimMosaic pr = (PPrimMosaic) pp[(r*hPPs)+c]; 		
        			pr.r = new Rectangle(lx,ly,mw,mh);
        			pr.info = ft.getInfo(r,c);
        			pr.ref = ft.getCasesAt(r,c);
        			if (ccnts==0) pr.empty = true;
        		}
        	}
        }
        if (mode==FLUCT) {
        	for (int r = 0; r < vPPs; r++) {
        		ly = mTop + ((r+1)*sy+(r!=0?vgap:0));
        		for (int c = 0; c < hPPs; c++) {
        			lx = mLeft + ((c+1)*sx+(c!=0?hgap:0));
        			ccnts = ft.getCountsAt(r,c);
        			mw = (int) (sx*ccnts/ft.getMax()*mUse);
        			mh = (int) (sy*ccnts/ft.getMax()*mUse);
        			PPrimMosaic pr = (PPrimMosaic) pp[(r*hPPs)+c]; 		
        			pr.r = new Rectangle(lx-mw,ly-mh,mw,mh);
        			pr.info = ft.getInfo(r,c);
        			pr.ref = ft.getCasesAt(r,c);
        			if (ccnts==0) pr.empty = true;
        		}
        	}
        }
	}
	
    public void paintBack(PoGraSS g) {
    }
    
    public String queryObject(int i) {
        if (pp!=null && pp[i]!=null) {
        	int mark=(int)(((double) pp[i].cases())*pp[i].getMarkedProportion(m,-1)+0.5);
            return ((PPrimMosaic) pp[i]).toString()+"\n"+((mark>0)?(""+mark+" of "+pp[i].cases()+" selected"):(""+pp[i].cases()+" cases"));
        }
        return "N/A";
    }
    
    public Object run(Object o, String cmd) {
    	super.run(o,cmd);
        if (m!=null) m.run(o,cmd);
        if (cmd=="observed") { if(mode!=OBSERVED) {mode=OBSERVED; setUpdateRoot(0); updateObjects(); repaint();}}
        if (cmd=="fluctuation") { if(mode!=FLUCT) {mode=FLUCT; setUpdateRoot(0); updateObjects(); repaint();}}
        if (cmd=="print") run(o,"exportPS");
        if (cmd=="exit") WinTracker.current.Exit();
        return null;
    }    
	
}
