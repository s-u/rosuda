package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.PrintStream;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** implementation of maps (uses {@link BaseCanvas})
    @version $Id$
*/
public class MapCanvas extends BaseCanvas
{
    /** map variable */
    SVar v;

	boolean fixedAspectRatio = true;
    double minX, minY, maxX, maxY;

    public MapCanvas(int gd, Frame f, SVar mv, SMarker mark) {
        super(gd,f,mark);
        setTitle("Polygon Map ("+mv.getName()+")");
        v=mv;
        allow180=true;

        ax=new Axis(null,Axis.O_X,Axis.T_Num); ax.addDepend(this);
        ay=new Axis(null,Axis.O_Y,Axis.T_Num); ay.addDepend(this);

        String myMenu[]={"+","File","~File.Graph","~Edit","+","View","@RRotate","rotate","@BToggle border lines","bounds","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        setDefaultMargins(new int[] {10,10,10,10});
        // note: Map's updateObjects relies on equality of all margins!
        pp=null;
        
        dontPaint=false;
    }

    public void updateObjects() {
        Dimension Dsize=getSize();
        int w=Dsize.width, h=Dsize.height;
        if (Global.DEBUG>0)
            System.out.println("MapCanvas.updateObjects(): ("+w+","+h+")/("+W+","+H+") pp="+pp);
        /* beware! mLeft=mRight=mTop=mBottom should be true! For optimization we assumes that. */
        w=W-mLeft*2; h=H-mLeft*2;
        boolean first=true;
        if (pp==null) {
            int tot=0;
            int i=0;
            while (i<v.size()) {
                MapSegment ms=(MapSegment) v.at(i);
                if (ms!=null) {
                    //System.out.println("segment "+i+": "+ms);
                    tot+=ms.count();
                    if (first) {
                        minX=ms.minX; minY=ms.minY; maxX=ms.maxX; maxY=ms.maxY;
                        first=false;
                    } else {
                        if (ms.minX<minX) minX=ms.minX;
                        if (ms.minY<minY) minY=ms.minY;
                        if (ms.maxX>maxX) maxX=ms.maxX;
                        if (ms.maxY>maxY) maxY=ms.maxY;
                    };
                }
                i++;
            };
            pp=new PlotPrimitive[tot];
            //System.out.println("global bounds: ("+minX+","+minY+")-("+maxX+","+maxY+")");
            boolean ins=ignoreNotifications;
            ignoreNotifications=true;
            ax.setValueRange(minX,maxX-minX);
            ay.setValueRange(minY,maxY-minY);
            ignoreNotifications=ins;
        }

		boolean ratioIsOk=!fixedAspectRatio;
		while (!ratioIsOk) {
			// retain aspect ratio (maybe we should make this more generic ...)
			double xscale=((double)ax.gLen)/(ax.vLen);
			double yscale=((double)ay.gLen)/(ay.vLen);
			if (xscale<0) xscale=-xscale;
			if (yscale<0) yscale=-yscale;
			/*
			 if (orientation==1 || orientation==3) {
            xscale=((double)h)/(maxX-minX);
				 yscale=((double)w)/(maxY-minY);
			 };
			 */
			double relscale=(xscale<yscale)?yscale/xscale:xscale/yscale;
			Axis aa = (xscale<yscale)?ay:ax;
			double aMid = aa.vBegin + aa.vLen/2.0;
			double aRng = aa.vLen*relscale;
			aa.setValueRange(aMid-aRng/2.0,aRng);
			
			// special case: if both axes propose vranges larger than their data ranges then prune back to the data range. This prevents "shrinking" phenomenon on resize and allows 'zooming' on the global scale
			if (ax.vLen>(maxX-minX)*1.01 && ay.vLen>(maxY-minY)*1.01) {
				ax.setValueRange(minX,maxX-minX);
				ay.setValueRange(minY,maxY-minY);
			} else ratioIsOk=true;
		}
		
		/*
        int reqW=(int)(scale*(maxX-minX));
        int reqH=(int)(scale*(maxY-minY));

		 if (orientation==0 || orientation==2) {
            if (W>reqW+mLeft*2+25 || H>reqH+mLeft*2+25) {
                if (Global.DEBUG>0)
                    System.out.println("MapCanvas.updateObjects(): W/H="+W+"/"+H+" req="+(reqW+mLeft*2)+"/"+(reqH+mLeft*2));
                setSize(reqW+mLeft*2+20,reqH+mLeft*2+20);
                getFrame().pack();
                return;
            }
        } else if (H>reqW+mLeft*2+25 || W>reqH+mLeft*2+25) {
            if (Global.DEBUG>0)
                System.out.println("MapCanvas.updateObjects(): W/H="+W+"/"+H+" req="+(reqW+mLeft*2)+"/"+(reqH+mLeft*2));
            setSize(reqH+mLeft*2+20,reqW+mLeft*2+20);
            getFrame().pack();
            return;
        }
		 */
        if (Global.DEBUG>0)
            System.out.println(" X:["+ax+"]["+minX+".."+maxX+"] Y:["+ay+"]["+minY+".."+maxY+"]");
        
        int i=0;
        int ps=0;
        while (i<v.size() && ps<pp.length) {
            MapSegment ms=(MapSegment) v.at(i);
            if (ms!=null) {
                int j=0;
                while (j<ms.count()) {
                    PPrimPolygon pri=new PPrimPolygon();
                    pri.ref=new int[1];
                    pri.ref[0]=i;
					pri.drawBorder=paintOutline;
                    if (orientation==0 || orientation==2)
                        pri.pg=new Polygon(MapSegmentTools.transViaAxisX(ms,j,ax),MapSegmentTools.transViaAxisY(ms,j,ay),ms.getSizeAt(j));
                    else
                        pri.pg=new Polygon(MapSegmentTools.transViaAxisY(ms,j,ay),MapSegmentTools.transViaAxisX(ms,j,ax),ms.getSizeAt(j));
                    pp[ps]=pri;
                    j++; ps++;
                }
            }
            i++;
        }
        setUpdateRoot(0);
    }
    
    public String queryObject(int i) 
    {
        return "Map segment";
    }

    public void keyTyped(KeyEvent e) 
    {
        super.keyTyped(e);
	if (e.getKeyChar()=='b') run(this,"bounds");
    }

    public Object run(Object o, String cmd) {
	super.run(o,cmd);
        if (cmd=="bounds") {
            setUpdateRoot(0);
            paintOutline=!paintOutline;
			updateObjects(); // currently the outline is a property of the objects, so we need to re-create them.. silly, I know ...
            repaint();
        }        	
	return null;
    }
    
    public SVar getData(int id) { return (id==0)?v:null; }
}
