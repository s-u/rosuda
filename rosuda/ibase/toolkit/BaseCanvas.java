package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** BaseCanvas - basis for all interactive plots which rely on plot primitives concept. To ensure consistent behavior all plots should be based on this class whenever possible. BaseCanvas includes key and mouse handling, selection and queries.<p>Although BaseCanvas is not abstract, is it not usable on its own (except maybe for testing). Any subclasses should override at least the {@link #updateObjects} method to initialize plot primitives. Displaying and selection of following plot primitives is supported out-of-the-box: points, rectangles and polygons. The subclass constructor should (beside calling super constructor) set any of the control flags to customize the behavior of this class.<p>BaseCanvas implements all key and mouse listeners as well as commander interface (actions are mapped into commands). This implies that a subclass it free to overload any individual methods of those. Just make sure that you provide calls to parent methods to preserve all functionality.

    @version $Id$
*/
public class BaseCanvas extends PGSCanvas implements Dependent, MouseListener, MouseMotionListener, KeyListener, ActionListener
{
    /** query popup window */
    protected QueryPopup qi;

    /** plot primitives which form the basis for data display and selection */
    protected PlotPrimitive[] pp;

    /** optional axis. Axis is not initialized by BaseCanvas (since BaseCanvas knows nothing of variables). They are provided to support some degree of consistence among plots which use one or two axes. If used, axis geometry is updated upon resize by the default paint routine and value range is changed by default zooming methods (atm for axes of type T_Num only). The axes are defined with respect to the data, NOT to the geometry. Calling the default {@link #rotate} method results in updated geometry of the axes, meaning that ax does not necessarily have the orientation O_X (unless rotation is prohibited). */

    /** by default BaseCanvas caches layers whenever possible, that is only {@link #paintInit} is guaranteed        to be run before {@link #paintBack}, {@link #paintObjects} and {@link #paintSelected}. This implies that none of the later three can rely on anything happening in the preceeding paint methods. This is not the usual behavior of PoGraSS. Setting this dontCache flag to <code>true</code> will enforce the defined PoGraSS behavior which means that all paint.. parts are called in the specified order, no matter which layer is being updated. */
    protected boolean dontCache=false;

    /** marker of the plot. This marker is used for linked highlighting. */
    protected SMarker m;

    /** plot area margins. Axes are set in a way to always respect those margins. They default to 0 for BaseCanvas. */
    protected int mLeft, mRight, mTop, mBottom;
    /** orientation of the plot. Value between 0 and 3. Increasing orientation results in plot rotating by 90 degrees clock-wise. */
    protected int orientation=0;
    /** if set to <code>true</code> then rotating the canvas also results in flipped size geometry of the canvas and re-packed parent frame. */
    protected boolean resizeOnRotate=true;
    /** if set to <code>false</code> then rotating is allowed only between orientation values 0 and 1 (ergo rotation over 180 degrees is not allowed) */
    protected boolean allow180=false;
    /** if set to <code>true</code> then zoom is allowed */
    protected boolean allowZoom=true;
    /** if set to <code>true</code> then zoom-dragging is allowed. Otherwise stepwise zoom by clicing is the only available zoom method. */
    protected boolean allowDragZoom=true;
    /** if set to <code>true</code> then zooming-in always retians the aspect ratio (at least default zoom handling via axes does. If you implement a subclass providing its own zooming features you should honor this flag where applicable) */
    protected boolean zoomRetainsAspect=true;
    
    /** run-time flag is set to <code>true</code> if baseDragging is in process */
    protected boolean baseDrag;
    /** run-time flag is set to <code>true</code> if the current baseDrag is a selection */
    protected boolean selDrag; // current baseDrag is selection baseDrag
    /** run-tim flag is set to <code>true</code> if the current baseDrag is a zoom */
    protected boolean zoomDrag;

    /** run-time flag is set to <code>true</code> if query mode is on - (currently it means that <Alt> is held down) */
    protected boolean inQuery=false;
    /** run-time flag is set to <code>true</code> if zoom mode is on */
    protected boolean inZoom=false;

    /** if set to <code>true</code> all notifications are rejected. Any subclass is free to use it, BaseCanvas modifies this flag in default zoom processing methods to prevent partial updates when ax and ay are updated sequentially. Any method changing this flag should always restore the state of the flag after it finishes! Also use with care in multi-threaded applications to prevent deadlocks. */
    protected boolean ignoreNotifications=false;

    /** this vector can be used to track the sequence of zooms. Zoom out should return to the state before last zoom in (if sensible in the context of the given plot). Any implementation of {@link #performZoomIn} and {@link #performZoomOut} is free to use this vector in any way which suits the implementation.<p>The current default implementation uses pairs of {@link ZoomDescriptorComponent} objects to store status of {@link #ax} and {@link #ay} axes. The vector is automatically initilized to an empty vector by the base constructor. */
    protected Vector zoomSequence;

    protected int W,H;

    protected int baseDragX1,baseDragX2,baseDragY1,baseDragY2;

    /** if set to <code>true</code> outline of plot primitives is painted (applies to default paint handler. subclasses of BaseCanvas don't have to respect this flag if may display more complex primitives) */
    protected boolean paintOutline=true;
    /** same functionality as {@link #paintOutline} but applies to selected objects only */
    protected boolean selectedPaintOutline=true;
    /** if set to <code>true</code> plot primitives are filled. */
    protected boolean fillInside=true;

    /** if set to <code>true</code> then next repaint will force update of geometry, that is it will behave as if the canvas size was changed resulting in updated axes and objects. {@link #paintPoGraSS} resets this flag to <code>false</code> after calling {@link #updateObjects} and setting everything up. */
    protected boolean updateGeometry=false;

    /** basic constructor. Every subclass must call this constructor
        @param f frame owning this canvas. since BaseCanvas itself doesn't modify any attribute of the frame except for title it is possible to put more canvases into one frame. This doesn't have to hold for subclasses, especially those providing their own menus.
        @param mark marker which will be used for selection/linked highlighting
        */
    public BaseCanvas(Frame f, SMarker mark) {
        super(4); // 4 layers; 0=bg, 1=sel, 2=baseDrag, 3=pm
	m=mark; setFrame(f);
        ax=ay=null;
        zoomSequence=new Vector();
        mLeft=mRight=mTop=mBottom=0;
	setBackground(Common.backgroundColor);
	addMouseListener(this);
	addMouseMotionListener(this);
        addKeyListener(this);
        f.addKeyListener(this);
        qi=new QueryPopup(f,mark==null?null:mark.getMasterSet(),"BaseCanvas");
    };

    /** notification handler - rebuild objects if necessary (AxisDataChange/VarChange) and repaint */
    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        if (ignoreNotifications) {
            if (Global.DEBUG>0) System.out.println("Warning, BaseCanvas received notification ("+msg+"), with ignoreNotifications set. Ignoring event.");
            return;
        };
        if((msg.getMessageID()&Common.NM_MASK)==Common.NM_VarChange ||
           msg.getMessageID()==Common.NM_AxisDataChange
           )
            updateObjects();
        setUpdateRoot((msg.getMessageID()==Common.NM_MarkerChange)?1:0);
        repaint();
    };

    /** rebuilds plot primitives - subclasses should override this method. It is not defined as abstract only for convenience, but minimal subclass simply overrides this method. */
    public void updateObjects() {
    };

    public Dimension getMinimumSize() { return new Dimension(mLeft+mRight+20,mTop+mBottom+20); };

    /** actual paint method - subclasses shound NOT override this method! use paintInit/Back/Objects/Selected/Post instead. Splitting into pieces allows more effective layer caching and results in better performance */
    public void paintPoGraSS(PoGraSS g) {
        //System.out.println("BaseCanvas.paintPoGraSS(): "+g.localLayerCache);
	Rectangle r=getBounds();
	int w=r.width, h=r.height;
        if (Global.DEBUG>0)
            System.out.println("BaseCanvas.paint: real bounds ["+w+":"+h+"], existing ["+W+":"+H+"], orientation="+orientation+" mTop="+mTop+",mBottom="+mBottom);
        if (ay!=null && (H!=h || updateGeometry))
            switch (orientation) {
                case 0: ay.setGeometry(Axis.O_Y,h-mBottom,mTop+mBottom-h); break;
                case 1: ay.setGeometry(Axis.O_X,mLeft,w-mLeft-mRight); break;
                case 2: ay.setGeometry(Axis.O_Y,mTop,h-mTop-mBottom); break;
                case 3: ay.setGeometry(Axis.O_X,w-mRight,mLeft+mRight-w); break;
            };
        if (ax!=null && (W!=w || updateGeometry))
            switch (orientation) {
                case 0: ax.setGeometry(Axis.O_X,mLeft,w-mLeft-mRight); break;
                case 1: ax.setGeometry(Axis.O_Y,mTop,h-mTop-mBottom); break;
                case 2: ax.setGeometry(Axis.O_X,w-mRight,mLeft+mRight-w); break;
                case 3: ax.setGeometry(Axis.O_Y,h-mBottom,mTop+mBottom-h); break;
            };
        if (H!=h || W!=w || updateGeometry) {
            W=w; H=h;
            updateObjects();
        }
        updateGeometry=false;
        if (Global.DEBUG>0)
            System.out.println("BarCanvas.paint: [w="+w+"/h="+h+"] ax="+ax+" ay="+ay);
	int basey=h-mBottom;
	g.setBounds(w,h);
	g.begin();
        g.defineColor("white",255,255,255);
        g.defineColor("black",0,0,0);
        g.defineColor("marked",Common.selectColor.getRed(),Common.selectColor.getGreen(),Common.selectColor.getBlue());
        float[] scc=Common.selectColor.getRGBComponents(null);
        g.defineColor("aSelBg",scc[0],scc[1],scc[2],0.3f);
        g.defineColor("aDragBg",0.0f,0.3f,1.0f,0.25f);
        paintInit(g);
        if (dontCache || g.localLayerCache<0 || g.localLayerCache==0) paintBack(g);
        if (dontCache || g.localLayerCache<0 || g.localLayerCache==0) paintObjects(g);
        nextLayer(g);
        if (dontCache || g.localLayerCache<0 || g.localLayerCache==1) paintSelected(g);
        nextLayer(g);
        if (baseDrag && (dontCache || g.localLayerCache<0 || g.localLayerCache==2)) {
            /* no clipping
            int dx1=A[0].clip(x1),dy1=A[1].clip(y1),
            dx2=A[0].clip(x2),dy2=A[1].clip(y2);
            */
            int dx1=baseDragX1, dx2=baseDragX2, dy1=baseDragY1, dy2=baseDragY2;
            if (dx1>dx2) { int hh=dx1; dx1=dx2; dx2=hh; };
            if (dy1>dy2) { int hh=dy1; dy1=dy2; dy2=hh; };
            g.setColor((selDrag)?"aSelBg":"aDragBg");
            g.fillRect(dx1,dy1,dx2-dx1,dy2-dy1);
            g.setColor("black");
            g.drawRect(dx1,dy1,dx2-dx1,dy2-dy1);
        };
        
        nextLayer(g);
        if (dontCache || g.localLayerCache<0 || g.localLayerCache==3) paintPost(g);
	g.end();
        setUpdateRoot(4);
    }

    public void paintInit(PoGraSS g) {
        //System.out.println("BaseCanvas.paintInit");
        g.defineColor("outline",0,0,0);
        g.defineColor("object",Common.objectsColor.getRed(),Common.objectsColor.getGreen(),Common.objectsColor.getBlue());
    }

    public void paintBack(PoGraSS g) {
        //System.out.println("BaseCanvas.paintBack");
    }
    
    public void paintObjects(PoGraSS g) {
        //System.out.println("BaseCanvas.paintObjects, (cache="+g.localLayerCache+") pp="+pp);
        Stopwatch sw=new Stopwatch();
        
        if (pp!=null) {
            int i=0;
            g.setColor("object");
            while (i<pp.length) {
                if (pp[i]!=null) {
                    if (pp[i].r!=null) {
                        if (fillInside) {
                            if (pp[i].col!=null)
                                g.setColor(pp[i].col.getRed(),pp[i].col.getGreen(),pp[i].col.getBlue());
                            else
                                g.setColor("object");
                            g.fillRect(pp[i].r.x,pp[i].r.y,
                                       pp[i].r.width,pp[i].r.height);
                        }
                        if (paintOutline) {
                            g.setColor("outline");
                            g.drawRect(pp[i].r.x,pp[i].r.y,
                                       pp[i].r.width,pp[i].r.height);
                        }
                    }
                    if (pp[i].pt!=null) {
                        g.fillOval(pp[i].pt.x,pp[i].pt.y,2,2);
                    }
                    if (pp[i].pg!=null) {
                        if (fillInside) {
                            if (pp[i].col!=null)
                                g.setColor(pp[i].col.getRed(),pp[i].col.getGreen(),pp[i].col.getBlue());
                            else
                                g.setColor("object");
                            g.fillPolygon(pp[i].pg.xpoints,pp[i].pg.ypoints,pp[i].pg.npoints);
                        }
                        if (paintOutline) {
                            g.setColor("outline");
                            g.drawPolygon(pp[i].pg.xpoints,pp[i].pg.ypoints,pp[i].pg.npoints);
                        }
                    }
                }
                i++;
            }
        }
        sw.profile("BaseCanvas.paintObjects");
    }

    public void paintSelected(PoGraSS g) {
        Stopwatch sw=new Stopwatch();
        
        //System.out.println("BaseCanvas.paintSelected, pp="+pp);
        if (pp!=null) {
            int i=0;
            g.setColor("marked");
            while (i<pp.length) {
                if (pp[i]!=null) {
                    double sa=pp[i].getMarkedProportion(m,-1);
                    //System.out.println("pp["+i+"] sa="+sa+" "+pp);
                    if (sa>0d) {
                        if (pp[i].r!=null) {
                            int rX=pp[i].r.x,rY=pp[i].r.y,rW=pp[i].r.width,rH=pp[i].r.height;
                            if (orientation==0) { // bottom-up
                                int nrH=(int)(((double)rH)*sa);
                                rY+=rH-nrH;
                                rH=nrH;
                            } else if (orientation==2) { // top-down
                                rH=(int)(((double)rH)*sa);
                            } else if (orientation==1) { // left-right
                                rW=(int)(((double)rW)*sa);
                            } else if (orientation==3) { // right-left
                                int nrW=(int)(((double)rW)*sa);
                                rX+=rW-nrW;
                                rW=nrW;
                            }
                            if (fillInside) {
                                g.setColor("marked");
                                g.fillRect(rX,rY,rW,rH);
                            }
                            if (selectedPaintOutline) {
                                g.setColor("outline");
                                g.drawRect(rX,rY,rW,rH);
                            }
                        }
                        if (pp[i].pt!=null) {
                            g.fillOval(pp[i].pt.x,pp[i].pt.y,2,2);
                        }
                        if (pp[i].pg!=null) {
                            // use filling color according to sa?
                            if (fillInside) {
                                g.setColor("marked");
                                g.fillPolygon(pp[i].pg.xpoints,pp[i].pg.ypoints,pp[i].pg.npoints);
                            }
                            if (selectedPaintOutline) {
                                g.setColor("outline");
                                g.drawPolygon(pp[i].pg.xpoints,pp[i].pg.ypoints,pp[i].pg.npoints);
                            }
                        }
                    }
                }
                i++;
            }
        }
        sw.profile("BaseCanvas.paintSelected");
    }

    public void paintPost(PoGraSS g) { }
    
    public void mouseClicked(MouseEvent ev) 
    {
	int x=ev.getX(), y=ev.getY();

        Point cl=getFrame().getLocation();
	Point tl=getLocation(); cl.x+=tl.x; cl.y+=tl.y;
	boolean setTo=false;

        if (Global.DEBUG>0) {
            String mods="";
            if (ev.isShiftDown()) mods+=" SHIFT";
            if (ev.isAltDown()) mods+=" ALT";
            if (ev.isControlDown()) mods+=" CTRL";
            if (ev.isMetaDown()) mods+=" META";
            if (ev.isAltGraphDown()) mods+=" ALT.GR";
            if ((ev.getModifiers()&MouseEvent.BUTTON1_MASK)==MouseEvent.BUTTON1_MASK) mods+=" M1";
            if ((ev.getModifiers()&MouseEvent.BUTTON2_MASK)==MouseEvent.BUTTON2_MASK) mods+=" M2";
            if ((ev.getModifiers()&MouseEvent.BUTTON3_MASK)==MouseEvent.BUTTON3_MASK) mods+=" M3";
            if (ev.isPopupTrigger()) mods+=" POPUP";
            System.out.println("Event:"+ev+mods);
        }

        if (Common.isZoomTrigger(ev)) {
            performZoomOut(x,y);
            return;
        }
        
        boolean effect=false, hideQI=true;
        boolean actionSelect=Common.isSelectTrigger(ev);
        boolean actionQuery=Common.isQueryTrigger(ev);
        boolean actionExtQuery=Common.isExtQuery(ev);
        if (Global.DEBUG>0)
            System.out.println("select="+actionSelect+", query="+actionQuery+", isMac="+Common.isMac());
        
        //System.out.println("BarCanvas.mouseClicked; Alt="+ev.isAltDown()+", Ctrl="+ev.isControlDown()+
	//		   ", Shift="+ev.isShiftDown()+", popup="+ev.isPopupTrigger());
        if (actionQuery || actionSelect) {
            int selMode=Common.getSelectMode(ev);
            if (selMode>1) setTo=true;
            if (pp!=null) {
                int i=0;
                while (i<pp.length) {
                    if (pp[i]!=null && pp[i].contains(x,y)) {
                        if (actionQuery) {
                            if (pp[i].ref!=null) {
                                if (pp[i].ref.length==1)
                                    qi.setContent(queryObject(i),pp[i].ref[0]);
                                else
                                    qi.setContent(queryObject(i),pp[i].ref);
                            } else
                                qi.setContent(queryObject(i));
                            qi.setLocation(cl.x+x,cl.y+y);
                            qi.show(); hideQI=false;
                        } else {
                            effect=true;
                            if (selMode==0) m.selectNone();
                            pp[i].setMark(m,setTo);
                        }
                    }
                    i++;
                }
            }
        }
        if (!effect && actionSelect) {
            m.selectNone();
            effect=true;
        }
        if (effect) m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
	if (hideQI) qi.hide();
    }

    public String queryObject(int i) {
        return "object ID "+i;
    }

    public void rotate(int amount) {
        orientation=(orientation+amount)&3;
        if (!allow180) orientation&=1;
        setUpdateRoot(0);
        updateGeometry=true;
        if (resizeOnRotate && (amount==1 || amount==3)) {
            setSize(H,W);
            getFrame().pack();
        } else
            repaint();
    }

    public void rotate() { rotate(1); };

    class ZoomDescriptorComponent {
        double vBegin, vLen;
        int gBegin, gLen, dc;
        boolean dummy;

        ZoomDescriptorComponent() {
            dummy=true;
        }

        ZoomDescriptorComponent(Axis a) {
            vBegin=a.vBegin; vLen=a.vLen; gBegin=a.gBegin; gLen=a.gLen;
            dc=a.datacount;
            dummy=false;
        }
    }

    public void performZoomIn(int x1, int y1, int x2, int y2) {
        if (Global.DEBUG>0) System.out.println("performZoomIn("+x1+","+y1+","+x2+","+y2+") [zoomSequence.len="+zoomSequence.size()+"]");
        boolean ins=ignoreNotifications;
        ignoreNotifications=true;
        double ax1=1.0, ax2=1.0, ay1=1.0, ay2=1.0;
        double xExtent=1.0, yExtent=1.0, xCenter=1.0, yCenter=1.0;
        if (ax!=null) {
            ax1=ax.getValueForPos(x1); ax2=ax.getValueForPos(x2);
            if ((ax2-ax1)*ax.vLen<0.0) { // fix signum - must be same as vLen
                double ah=ax2; ax2=ax1; ax1=ah;
            }
            xExtent=(x1==x2)?ax.vLen/2.0:ax2-ax1;
            xCenter=(ax1+ax2)/2.0;
        }
        if (ay!=null) {
            ay1=ay.getValueForPos(y1); ay2=ay.getValueForPos(y2);
            if ((ay2-ay1)*ay.vLen<0.0) { // fix signum - must be same as vLen
                double ah=ay2; ay2=ay1; ay1=ah;
            }
            yExtent=(y1==y2)?ay.vLen/2.0:ay2-ay1;
            yCenter=(ay1+ay2)/2;
        }
        if (ax!=null && ay!=null && zoomRetainsAspect) {
            double ratioPre=ax.vLen/ay.vLen;
            if (ratioPre<0.0) ratioPre=-ratioPre;
            double ratioPost=xExtent/yExtent;
            if (ratioPost<0.0) ratioPost=-ratioPost;
            if (ratioPost>ratioPre) // x1/y1 < x2/y2 => inflate y
                yExtent*=ratioPost/ratioPre;
            else // otherwise inflate x
                xExtent*=ratioPost/ratioPre;
        }
        if (ax!=null) {
            zoomSequence.addElement(new ZoomDescriptorComponent(ax));
            ax.setValueRange(xCenter-xExtent/2.0,xExtent);
        } else zoomSequence.addElement(new ZoomDescriptorComponent());
        ignoreNotifications=ins;
        if (ay!=null) {
            zoomSequence.addElement(new ZoomDescriptorComponent(ay));
            ay.setValueRange(yCenter-yExtent/2.0,yExtent);
        } else zoomSequence.addElement(new ZoomDescriptorComponent());
        updateObjects();
        setUpdateRoot(0);
        repaint();
    }

    public void performZoomOut(int x, int y) {
        if (Global.DEBUG>0) System.out.println("performZoomOut("+x+","+y+") [zoomSequence.len="+zoomSequence.size()+"]");
        int tail=zoomSequence.size()-1;
        if (tail<1) return;
        ZoomDescriptorComponent zx,zy;
        zx=(ZoomDescriptorComponent)zoomSequence.elementAt(tail-1);
        zy=(ZoomDescriptorComponent)zoomSequence.elementAt(tail);
        boolean ins=ignoreNotifications;
        ignoreNotifications=true;
        if (ax!=null && !zx.dummy)
            ax.setValueRange(zx.vBegin,zx.vLen);
        ignoreNotifications=ins;
        if (ay!=null && !zy.dummy)
            ay.setValueRange(zy.vBegin,zy.vLen);
        zoomSequence.removeElement(zy);
        zoomSequence.removeElement(zx);
        updateObjects();
        setUpdateRoot(0);
        repaint();
    }

    public void resetZoom() {
        if (Global.DEBUG>0) System.out.println("resetZoom() [zoomSequence.len="+zoomSequence.size()+"]");
        if (zoomSequence.size()>1) {
            ZoomDescriptorComponent zx,zy;
            zx=(ZoomDescriptorComponent)zoomSequence.elementAt(0);
            zy=(ZoomDescriptorComponent)zoomSequence.elementAt(1);
            boolean ins=ignoreNotifications;
            ignoreNotifications=true; // prevent processing of AxisChanged notification for ax
            if (ax!=null && !zx.dummy)
                ax.setValueRange(zx.vBegin,zx.vLen);
            ignoreNotifications=ins;
            if (ay!=null && !zy.dummy)
                ay.setValueRange(zy.vBegin,zy.vLen);
            updateObjects();
            setUpdateRoot(0);
            repaint();
        }
        zoomSequence.removeAllElements();
    }
    
    public void mousePressed(MouseEvent ev)
    {
        if (Global.DEBUG>0) System.out.println("Event:"+ev);

        baseDragX1=ev.getX(); baseDragY1=ev.getY();
        qi.hide();
        selDrag=Common.isSelectTrigger(ev);
        zoomDrag=Common.isZoomTrigger(ev);
        if (selDrag || (allowDragZoom && zoomDrag))
            baseDrag=true;
    }

    public void mouseReleased(MouseEvent e)
    {
        if (Global.DEBUG>0) System.out.println("Event:"+e);

        int X1=baseDragX1, Y1=baseDragY1, X2=baseDragX2, Y2=baseDragY2;
        if (!baseDrag || (X1==e.getX() && Y1==e.getY())) { // if p1=p2 then this is a click so let "mouse clicked" handle it. we also bail out if no dragging is performed
            baseDrag=false;
            return;
        }
        if (baseDragX1>baseDragX2) { X2=baseDragX1; X1=baseDragX2; };
        if (baseDragY1>baseDragY2) { Y2=baseDragY1; Y1=baseDragY2; };
        Rectangle sel=new Rectangle(X1,Y1,X2-X1,Y2-Y1);
        baseDrag=false;

        //System.out.println("BaseCanvas.mouseReleased");
        setUpdateRoot(2);
        if (selDrag) {
            boolean setTo=false;
            if (Common.getSelectMode(e)==2) setTo=true;
            if (Common.getSelectMode(e)==0) m.selectNone();

            int i=0;
            while (i<pp.length) {
                //System.out.println("pp["+i+"]="+pp[i]);
                if (pp[i]!=null && pp[i].intersects(sel)) {
                    pp[i].setMark(m,setTo);
                    if (pp[i].ref!=null)
                        while (i<pp.length-1 && pp[i+1]!=null && pp[i+1].ref!=null && pp[i+1].ref[0]==pp[i].ref[0]) i++;
                }
                i++;
            };
            m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
            setUpdateRoot(1);
        };
        if (zoomDrag)
            performZoomIn(X1,Y1,X2,Y2);
        repaint();
    }
    
    public void mouseEntered(MouseEvent e) {};
    public void mouseExited(MouseEvent e) {};
    public void mouseDragged(MouseEvent e)
    {
        if (baseDrag) {
            int x=e.getX(), y=e.getY();
            if (x!=baseDragX2 || y!=baseDragY2) {
                baseDragX2=x; baseDragY2=y;
                setUpdateRoot(2);
                repaint();
            };
        };
    };
    public void mouseMoved(MouseEvent ev) {};
    
    public void keyTyped(KeyEvent e) 
    {
	if (e.getKeyChar()=='P') run(this,"print");
	if (e.getKeyChar()=='X') run(this,"exportPGS");
	if (e.getKeyChar()=='C') run(this,"exportCases");
    };

    public void keyPressed(KeyEvent e) {
        int kc=e.getKeyCode();
        if (kc==KeyEvent.VK_ALT && !inZoom && !inQuery) {
            setCursor(Common.cur_query);
            inQuery=true;
        }
        if (kc==KeyEvent.VK_META && allowZoom && !inZoom && !inQuery) {
            setCursor(Common.cur_zoom);
            inZoom=true;
        }
    };

    public void keyReleased(KeyEvent e) {
        int kc=e.getKeyCode();
        if (kc==KeyEvent.VK_ALT && !inZoom) {
            setCursor(Common.cur_arrow);
            inQuery=false;
        }
        if (kc==KeyEvent.VK_META && allowZoom && !inQuery) {
            setCursor(Common.cur_arrow);
            inZoom=false;
        }
    };

    public Object run(Object o, String cmd) {
	super.run(o,cmd);
        if (m!=null) m.run(o,cmd);
	if (cmd=="print") run(o,"exportPS");
        if (cmd=="rotate") rotate(1);
        if (cmd=="flip" && allow180) rotate(2);
	return null;
    };

    public void actionPerformed(ActionEvent e) {
	if (e==null) return;
	run(e.getSource(),e.getActionCommand());
    };
}
