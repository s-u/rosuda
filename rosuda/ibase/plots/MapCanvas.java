import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.PrintStream;

/** implementation of maps (uses {@link BaseCancas})
    @version $Id$
*/
class MapCanvas extends BaseCanvas
{
    /** map variable */
    SVar v;

    double minX, minY, maxX, maxY;

    public MapCanvas(Frame f, SVar mv, SMarker mark) {
        super(f,mark);
        setTitle("Polygon Map ("+mv.getName()+")");
        v=mv;
        allow180=true;

        ax=new Axis(null,Axis.O_X,Axis.T_Num); ax.addDepend(this);
        ay=new Axis(null,Axis.O_Y,Axis.T_Num); ay.addDepend(this);

        String myMenu[]={"+","File","~File.Graph","~Edit","+","View","!RRotate","rotate","@BToggle border lines","bounds","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        mLeft=mRight=mTop=mBottom=10;
        // note: Map's updateObjects relies on equality of all margins!
        pp=null;
    }

    public void updateObjects() {
        Dimension Dsize=getSize();
        int w=Dsize.width, h=Dsize.height;
        if (Common.DEBUG>0)
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
        double xscale=((double)w)/(maxX-minX);
        double yscale=((double)h)/(maxY-minY);
        if (orientation==1 || orientation==3) {
            xscale=((double)h)/(maxX-minX);
            yscale=((double)w)/(maxY-minY);
        };
        double scale=(xscale<yscale)?xscale:yscale;
        int reqW=(int)(scale*(maxX-minX));
        int reqH=(int)(scale*(maxY-minY));
        if (orientation==0 || orientation==2) {
            if (W>reqW+mLeft*2+25 || H>reqH+mLeft*2+25) {
                if (Common.DEBUG>0)
                    System.out.println("MapCanvas.updateObjects(): W/H="+W+"/"+H+" req="+(reqW+mLeft*2)+"/"+(reqH+mLeft*2));
                setSize(reqW+mLeft*2+20,reqH+mLeft*2+20);
                getFrame().pack();
                return;
            }
        } else if (H>reqW+mLeft*2+25 || W>reqH+mLeft*2+25) {
            if (Common.DEBUG>0)
                System.out.println("MapCanvas.updateObjects(): W/H="+W+"/"+H+" req="+(reqW+mLeft*2)+"/"+(reqH+mLeft*2));
            setSize(reqH+mLeft*2+20,reqW+mLeft*2+20);
            getFrame().pack();
            return;
        }

        if (Common.DEBUG>0)
            System.out.println(" X:["+ax+"]["+minX+".."+maxX+"] Y:["+ay+"]["+minY+".."+maxY+"]");
        
        int i=0;
        int ps=0;
        while (i<v.size() && ps<pp.length) {
            MapSegment ms=(MapSegment) v.at(i);
            if (ms!=null) {
                int j=0;
                while (j<ms.count()) {
                    pp[ps]=new PlotPrimitive();
                    pp[ps].ref=new int[1];
                    pp[ps].ref[0]=i;

/*                    
                    if (orientation==0 || orientation==2)
                        pp[ps].pg=new Polygon(ms.getXtransAt(j,(orientation==0)?scale:-scale,-minX,
                                                         (orientation==0)?mLeft:W-mLeft),
                                          ms.getYtransAt(j,(orientation==0)?-scale:scale,-minY,(orientation==0)?H-mLeft:mLeft),
                                          ms.getSizeAt(j));
                    else
                        pp[ps].pg=new Polygon(ms.getYtransAt(j,(orientation==1)?scale:-scale,-minY,(orientation==1)?mLeft:W-mLeft),
                                          ms.getXtransAt(j,(orientation==1)?-scale:scale,-minX,
                                                         (orientation==1)?H-mLeft:mLeft),
                                          ms.getSizeAt(j));
*/
                    if (orientation==0 || orientation==2)
                        pp[ps].pg=new Polygon(ms.transViaAxisX(j,ax),ms.transViaAxisY(j,ay),ms.getSizeAt(j));
                    else
                        pp[ps].pg=new Polygon(ms.transViaAxisY(j,ay),ms.transViaAxisX(j,ax),ms.getSizeAt(j));
                    
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
	if (e.getKeyChar()=='R' || e.getKeyChar()=='r') run(this,"rotate");
	if (e.getKeyChar()=='b') run(this,"bounds");
    }

    public Object run(Object o, String cmd) {
	super.run(o,cmd);
        if (cmd=="bounds") {
            setUpdateRoot(0);
            paintOutline=!paintOutline;
            repaint();
        }        	
	return null;
    }
}
