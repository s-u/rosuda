package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.PrintStream;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** color coded fluctuation diagram (dirty hack)
    @version $Id$
*/
public class FCCCanvas extends FluctCanvas {
    SVar ccv;
    public FCCCanvas(PlotComponent ppc, Frame f, SVar v1, SVar v2, SMarker mark, SVar ccvar) {
	super(ppc,f,v1,v2,mark,null);
	ccv=ccvar;
    };

    public void paintPoGraSS(PoGraSS g) {
	Rectangle r=getBounds();
	g.setBounds(r.width,r.height);
	g.begin();
	g.defineColor("white",255,255,255);
        g.defineColor("marked",128,255,128);
	g.defineColor("black",0,0,0);
	g.defineColor("outline",0,0,0);
	g.defineColor("red",255,0,0);
	g.defineColor("lines",96,96,255);	
	g.defineColor("selText",255,0,0);
	g.defineColor("selBg",255,255,192);
	g.defineColor("splitRects",128,128,255);

	if (ccv!=null)
	    for(int cc=0;cc<ccv.getNumCats();cc++) {
		int rr=0,gg=0,bb=0;
		int bm=cc+1;
		if ((bm&1)==1) bb=255;
		if ((bm&2)==2) rr=255;
		if ((bm&4)==4) gg=255;
		//if ((bm&8)==8) { rr=rr*2/3; gg=gg*2/3; bb=bb*2/3; };
		if ((bm&8)==8) { rr/=2; gg/=2; bb/=2; };
		g.defineColor("class"+cc,rr,gg,bb);
	    };
 
	Dimension Dsize=getSize();
	if (Dsize.width!=TW || Dsize.height!=TH)
	    updatePoints();

	if (TW<50||TH<50) {
	    g.setColor("red");
	    g.drawLine(0,0,TW,TH); 
	    g.drawLine(0,TH,TW,0); 
	    return;
	};

	//g.setColor("white");
	//g.fillRect(X,Y,W,H);

        g.setColor("black");
        g.drawLine(X,Y,X,Y+H);
        g.drawLine(X,Y+H,X+W,Y+H);

        {
            double f=A[0].getSensibleTickDistance(50,26);
            double fi=A[0].getSensibleTickStart(f);
            //if (Common.DEBUG>0)
            //System.out.println("SP.A[0]:"+A[0].toString()+", distance="+f+", start="+fi);
            while (fi<A[0].vBegin+A[0].vLen) {
                int t=A[0].getValuePos(fi);
                g.drawLine(t,Y+H,t,Y+H+5);
                if (showLabels)
                    g.drawString(v[0].isCat()?v[0].getCatAt((int)fi).toString():
                                 A[0].getDisplayableValue(fi),t-5,Y+H+20);
                fi+=f;
            };
        }

        {
            double f=A[1].getSensibleTickDistance(50,18);
            double fi=A[1].getSensibleTickStart(f);
            //if (Common.DEBUG>0)
            //System.out.println("SP.A[1]:"+A[1].toString()+", distance="+f+", start="+fi);
            while (fi<A[1].vBegin+A[1].vLen) {
                int t=A[1].getValuePos(fi);
                g.drawLine(X-5,t,X,t);
                if(showLabels)
                    g.drawString(v[1].isCat()?Common.getTriGraph(v[1].getCatAt((int)fi).toString()):A[1].getDisplayableValue(fi),X-25,t+5);
                fi+=f;
            };
        }

        int pic=0;
        for (int yp=0;yp<v2l;yp++)
            for (int xp=0;xp<v1l;xp++) {
                double ct=Counts[pic]; double mct=Marked[pic];
                pic++;
                if (ct>0) {
                    int lx=A[0].getCatLow(xp);
                    int ly=A[1].getCatLow(yp);
                    int dx=A[0].getCatUp(xp)-lx;
                    int dy=A[1].getCatUp(yp)-ly;
                    if (dx<0) { lx+=dx; dx=-dx; };
                    if (dy<0) { ly+=dy; dy=-dy; };
                    g.setColor("white");
		    if (ccv!=null && ccv.isCat()) {
			Object c1=v[0].getCatAt(xp);
			Object c2=v[1].getCatAt(yp);
			//System.out.println("Looking for: "+c1+" and "+c2);
			for(int cw=0;cw<v[0].size();cw++)
			    if (v[0].at(cw).toString().equals(c1.toString()) && 
				v[1].at(cw).toString().equals(c2.toString())) {
				//System.out.println("found at "+cw);
				g.setColor("class"+ccv.getCatIndex(cw)); break;
			    }
			ly+=3; dy-=3;
			lx+=3; dx-=3;
		    };
                    int rdx=(int)(((double)dx)*Math.sqrt(ct/maxCount));
                    int rdy=(int)(((double)dy)*Math.sqrt(ct/maxCount));
                    int mdy=(int)(((double)rdy)*mct/ct);
                    g.fillRect(lx,ly,rdx,rdy);
                    if (mdy>0) {
                        g.setColor("marked");
                        g.fillRect(lx,ly+rdy-mdy,rdx,mdy);
                    };
                    g.setColor((mct>0)?"red":"black");
                    g.drawRect(lx,ly,rdx,rdy);
                };
            } ;

	paintDragLayer(g);

	g.end();
        setUpdateRoot(2); // by default no repaint is necessary unless resize occurs
    }
} 
