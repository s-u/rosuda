package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** BaseCanvas - basis for all interactive plots which rely on plot primitives concept. To ensure consistent behavior all plots should be based on this class whenever possible. BaseCanvas includes key and mouse handling, selection and queries.<p>Although BaseCanvas is not abstract, is it not usable on its own (except maybe for testing). Any subclasses should override at least the {@link #updateObjects} method to initialize plot primitives. Displaying and selection of following plot primitives is supported out-of-the-box: points, rectangles and polygons. The subclass constructor should (beside calling super constructor) set any of the control flags to customize the behavior of this class.<p>BaseCanvas implements all key and mouse listeners as well as commander interface (actions are mapped into commands). This implies that a subclass it free to overload any individual methods of those. Just make sure that you provide calls to parent methods to preserve all functionality.
 *
 * @version $Id$
 */
public class BaseCanvas
//#ifdef XTREME
//extends PGSJoglCanvas
//#else
        extends PGSCanvas
//#endif
        implements Dependent, MouseListener, MouseMotionListener, KeyListener, ActionListener {
    protected static final String C_BLACK = "black";
    protected static final String C_WHITE = "white";
    static final String C_MARKED = "marked";
    static final String C_ASELBG = "aSelBg";
    static final String C_ADRAGBG = "aDragBg";
    static final String C_OBJECT = "object";
    static final String M_PRINT = "print";
    static final String M_EXPORTCASES = "exportCases";
    static final String M_ROTATE = "rotate";
    protected static final String M_RESETZOOM = "resetZoom";
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
    /** if set to <code>true</code> then move-dragging is allowed. */
    protected boolean allowDragMove=false;
    /** determines whether axis labels should be shown. */
    private boolean showLabels=true;
    /** run-time flag is set to <code>true</code> if baseDragging is in process */
    protected boolean baseDrag;
    /** run-time flag is set to <code>true</code> if the current baseDrag is a selection */
    protected boolean selDrag; // current baseDrag is selection baseDrag
    /** run-time flag is set to <code>true</code> if the current baseDrag is a zoom */
    protected boolean zoomDrag;
    /** run-time flag is set to <code>true</code> if the current baseDrag is a move */
    protected boolean moveDrag;
    
    /** run-time flag is set to <code>true</code> if query mode is on - (currently it means that <Alt> is held down) */
    protected boolean inQuery=false;
    /** run-time flag is set to <code>true</code> if zoom mode is on */
    protected boolean inZoom=false;
    
    /** if set to <code>true</code> only hilighted primitives will be shown */
    protected boolean showOnlyHilited=false;
    
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
    
    /** prevents painting (and thus calling updateObjects) until set to false. */
    protected boolean dontPaint=true;
    
    /** if set to <code>true</code> then next repaint will force update of geometry, that is it will behave as if the canvas size was changed resulting in updated axes and objects. {@link #paintPoGraSS} resets this flag to <code>false</code> after calling {@link #updateObjects} and setting everything up. */
    protected boolean updateGeometry=false;
    
    /** if set to <code>true</code> don't paint objects on the margins defined by mLeft etc. */
    protected boolean objectClipping=false;
    
    /** if set to <code>true</code> alpha will be applied to highlighting, too */
    protected boolean alphaHighlighting=false;
    
    protected float ppAlpha = 1.0f;
    
    /** arrays of additional axes that are updated upon resize. can be null */
    protected Axis[] opAx; // axes parallel to ax
    protected Axis[] opAy; // axes parallel to ay
    
    /** PlotText object containing labels. Can be null. */
    protected PlotText labels;
    
    /** basic constructor. Every subclass must call this constructor
     * @param f frame owning this canvas. since BaseCanvas itself doesn't modify any attribute of the frame except for title it is possible to put more canvases into one frame. This doesn't have to hold for subclasses, especially those providing their own menus.
     * @param mark marker which will be used for selection/linked highlighting
     */
    public BaseCanvas(final PlotComponent ppc, final Frame f, final SMarker mark) {
        super(ppc,4); // 4 layers; 0=bg, 1=sel, 2=baseDrag, 3=pm
        Global.forceAntiAliasing = true;
        m=mark; setFrame(f);
        ax=ay=null;
        zoomSequence=new Vector();
        mLeft=mRight=mTop=mBottom=0;
        pc.setBackground(Common.backgroundColor);
        pc.addMouseListener(this);
        pc.addMouseMotionListener(this);
        pc.addKeyListener(this);
        f.addKeyListener(this);
        qi=PlotComponentFactory.createQueryPopup(pc,f,mark==null?null:mark.getMasterSet(),"BaseCanvas");
        labels=new PlotText(getPlotManager());
        labels.setLayer(0);
    }
    
    /** notification handler - rebuild objects if necessary (AxisDataChange/VarChange) and repaint */
    public void Notifying(final NotifyMsg msg, final Object o, final Vector path) {
        if (ignoreNotifications) {
            if (Global.DEBUG>0) System.out.println("Warning, BaseCanvas received notification ("+msg+"), with ignoreNotifications set. Ignoring event.");
            return;
        }
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
    public void paintPoGraSS(final PoGraSS g) {
        if(dontPaint) return;
        adjustMargin();
        //System.out.println("BaseCanvas.paintPoGraSS(): "+g.localLayerCache);
        final Rectangle r=pc.getBounds();
        final int w=r.width;
        final int h=r.height;
        if (Global.DEBUG>0)
            System.out.println("BaseCanvas.paint: real bounds ["+w+":"+h+"], existing ["+W+":"+H+"], orientation="+orientation+" mTop="+mTop+",mBottom="+mBottom);
        boolean marginsAdjusted=false;
        do{
            if (ay!=null && (H!=h || updateGeometry)){
                switch (orientation) {
                    case 0: ay.setGeometry(Axis.O_Y,h-mBottom,mTop+mBottom-h); break;
                    case 1: ay.setGeometry(Axis.O_X,mLeft,w-mLeft-mRight); break;
                    case 2: ay.setGeometry(Axis.O_Y,mTop,h-mTop-mBottom); break;
                    case 3: ay.setGeometry(Axis.O_X,w-mRight,mLeft+mRight-w); break;
                }
            }
            if(opAy!=null && (H!=h || updateGeometry)){
                switch (orientation) {
                    case 0: for(int i=0; i<opAy.length; i++) if(opAy[i]!=null) opAy[i].setGeometry(Axis.O_Y,h-mBottom,mTop+mBottom-h); break;
                    case 1: for(int i=0; i<opAy.length; i++) if(opAy[i]!=null) opAy[i].setGeometry(Axis.O_X,mLeft,w-mLeft-mRight); break;
                    case 2: for(int i=0; i<opAy.length; i++) if(opAy[i]!=null) opAy[i].setGeometry(Axis.O_Y,mTop,h-mTop-mBottom); break;
                    case 3: for(int i=0; i<opAy.length; i++) if(opAy[i]!=null) opAy[i].setGeometry(Axis.O_X,w-mRight,mLeft+mRight-w); break;
                }
            }
            if (ax!=null && (W!=w || updateGeometry)){
                switch (orientation) {
                    case 0: ax.setGeometry(Axis.O_X,mLeft,w-mLeft-mRight); break;
                    case 1: ax.setGeometry(Axis.O_Y,mTop,h-mTop-mBottom); break;
                    case 2: ax.setGeometry(Axis.O_X,w-mRight,mLeft+mRight-w); break;
                    case 3: ax.setGeometry(Axis.O_Y,h-mBottom,mTop+mBottom-h); break;
                }
            }
            if(opAx!=null && (W!=w || updateGeometry)){
                switch (orientation) {
                    case 0: for(int i=0; i<opAx.length; i++) if(opAx[i]!=null) opAx[i].setGeometry(Axis.O_X,mLeft,w-mLeft-mRight); break;
                    case 1: for(int i=0; i<opAx.length; i++) if(opAx[i]!=null) opAx[i].setGeometry(Axis.O_Y,mTop,h-mTop-mBottom); break;
                    case 2: for(int i=0; i<opAx.length; i++) if(opAx[i]!=null) opAx[i].setGeometry(Axis.O_X,w-mRight,mLeft+mRight-w); break;
                    case 3: for(int i=0; i<opAx.length; i++) if(opAx[i]!=null) opAx[i].setGeometry(Axis.O_Y,h-mBottom,mTop+mBottom-h); break;
                }
            }
            marginsAdjusted = adjustMargin();
            updateGeometry = updateGeometry || marginsAdjusted;
        } while (marginsAdjusted);
        if (H!=h || W!=w || updateGeometry) {
            W=w; H=h;
            updateObjects();
        }
        updateGeometry=false;
        if (Global.DEBUG>0)
            System.out.println("BarCanvas.paint: [w="+w+"/h="+h+"] ax="+ax+" ay="+ay);
        
        g.setBounds(w,h);
        g.begin();
        g.defineColor(C_WHITE,255,255,255);
        g.defineColor(C_BLACK,0,0,0);
        g.defineColor(C_MARKED,Common.selectColor.getRed(),Common.selectColor.getGreen(),Common.selectColor.getBlue());
        final float[] scc=Common.selectColor.getRGBComponents(null);
        g.defineColor(C_ASELBG,scc[0],scc[1],scc[2],0.3f);
        g.defineColor(C_ADRAGBG,0.0f,0.3f,1.0f,0.25f);
        paintInit(g);
        if (dontCache || g.localLayerCache<0 || g.localLayerCache==0) paintBack(g);
        if ((dontCache || g.localLayerCache<0 || g.localLayerCache==0) && !showOnlyHilited) paintObjects(g);
        nextLayer(g);
        if (dontCache || g.localLayerCache<0 || g.localLayerCache==1) paintSelected(g);
        nextLayer(g);
        if (baseDrag && !(allowDragMove && moveDrag) && (dontCache || g.localLayerCache<0 || g.localLayerCache==2)) {
            /* no clipping
            int dx1=A[0].clip(x1),dy1=A[1].clip(y1),
            dx2=A[0].clip(x2),dy2=A[1].clip(y2);
             */
            int dx1=baseDragX1, dx2=baseDragX2, dy1=baseDragY1, dy2=baseDragY2;
            if (dx1>dx2) { final int hh=dx1; dx1=dx2; dx2=hh; }
            if (dy1>dy2) { final int hh=dy1; dy1=dy2; dy2=hh; }
            g.setColor((selDrag)?C_ASELBG:C_ADRAGBG);
            g.fillRect(dx1,dy1,dx2-dx1,dy2-dy1);
            g.setColor(C_BLACK);
            g.drawRect(dx1,dy1,dx2-dx1,dy2-dy1);
        }
        
        nextLayer(g);
        if (dontCache || g.localLayerCache<0 || g.localLayerCache==3) paintPost(g);
        g.end();
        setUpdateRoot(4);
    }
    
    public void paintInit(final PoGraSS g) {
        //System.out.println("BaseCanvas.paintInit");
        g.defineColor("outline",0,0,0);
        g.defineColor(C_OBJECT,Common.objectsColor.getRed(),Common.objectsColor.getGreen(),Common.objectsColor.getBlue());
    }
    
    public void paintBack(final PoGraSS g) {
        //System.out.println("BaseCanvas.paintBack");
    }
    
    public void paintObjects(final PoGraSS g) {
        //System.out.println("BaseCanvas.paintObjects, (cache="+g.localLayerCache+") pp="+pp);
        final Stopwatch sw=new Stopwatch();
        if(objectClipping) g.setClip(mLeft, mTop, pc.getBounds().width-mLeft-mRight, pc.getBounds().height-mTop-mBottom);
        if (pp!=null) {
            
            g.setColor(C_OBJECT);
            g.setGlobalAlpha(ppAlpha);
            int i = 0;
            while (i<pp.length) {
                if (pp[i]!=null) pp[i].paint(g, orientation);
                i++;
            }
            g.resetGlobalAlpha();
        }
        if(objectClipping) g.resetClip();
        sw.profile("BaseCanvas.paintObjects");
    }
    
    public void paintSelected(final PoGraSS g) {
        final Stopwatch sw=new Stopwatch();
        
        //System.out.println("BaseCanvas.paintSelected, pp="+pp);
        if(objectClipping) g.setClip(mLeft, mTop, pc.getBounds().width-mLeft-mRight, pc.getBounds().height-mTop-mBottom);
        if (pp!=null) {
            
            if(alphaHighlighting) g.setGlobalAlpha(ppAlpha);
            
            g.setColor(C_MARKED);
            int i = 0;
            while (i<pp.length) {
                if (pp[i]!=null)
                    pp[i].paintSelected(g,orientation,m);
                i++;
            }
            if(alphaHighlighting) g.resetGlobalAlpha();
        }
        if(objectClipping) g.resetClip();
        sw.profile("BaseCanvas.paintSelected");
    }
    
    public void paintPost(final PoGraSS g) { }
    
    public void mouseClicked(final MouseEvent ev) {
        final int x=ev.getX();
        final int y=ev.getY();
        
        if(baseDragX1==x && baseDragY1==y){
            final Point cl=getFrame().getLocation();
            final Point tl=pc.getLocation(); cl.x+=tl.x; cl.y+=tl.y;
            
            
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
            
            
            final boolean actionSelect=Common.isSelectTrigger(ev);
            final boolean actionQuery=Common.isQueryTrigger(ev);
            
            if (Global.DEBUG>0)
                System.out.println("select="+actionSelect+", query="+actionQuery+", isMac="+Common.isMac());
            
            //System.out.println("BarCanvas.mouseClicked; Alt="+ev.isAltDown()+", Ctrl="+ev.isControlDown()+
            //		   ", Shift="+ev.isShiftDown()+", popup="+ev.isPopupTrigger());
            boolean effect = false;
            boolean hideQI = true;
            if (actionQuery || actionSelect) {
                boolean setTo = false;
                final int selMode=Common.getSelectMode(ev);
                if (selMode>1) setTo=true;
                if (pp!=null) {
                    if (actionQuery) {
                        final PlotPrimitive p = getFirstPrimitiveContaining(x,y);
                        if(p!=null){
                            if (p.cases()>0) {
                                if (p.getPrimaryCase()!=-1) {
                                    setQueryText(queryObject(p),p.getPrimaryCase());
                                } else {
                                    setQueryText(queryObject(p),p.getCaseIDs());
                                }
                            } else {
                                setQueryText(queryObject(p));
                            }
                            qi.setLocation(cl.x+x,cl.y+y);
                            qi.show(); hideQI=false;
                        }
                    } else {
                        final PlotPrimitive[] pps = getPrimitivesContaining(x,y);
                        if (selMode==0){
                            m.selectNone();
                            effect=true;
                        }
                        int i=0;
                        while (i<pps.length) {
                            if (pps[i]!=null) {
                                effect=true;
                                
                                pps[i].setMark(m,setTo);
                            }
                            i++;
                        }
                        if(!effect) m.selectNone();
                    }
                }
            }
            if (!effect && actionSelect) {
                if(m.getList().size()>0){
                    m.selectNone();
                    effect=true;
                }
            }
            if (effect) m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
            if (hideQI) qi.hide();
        }
    }
    
    public String queryObject(final int i) {
        return "object ID "+i;
    }
    
    public String queryObject(final PlotPrimitive p) {
        return "object "+p.toString();
    }
    
    public void rotate(final int amount) {
        orientation=(orientation+amount)&3;
        if (!allow180) orientation&=1;
        setUpdateRoot(0);
        updateGeometry=true;
        if (resizeOnRotate && (amount==1 || amount==3)) {
            pc.setSize(H,W);
            getFrame().pack();
        } else
            repaint();
    }
    
    public void rotate() { rotate(1); };
    
    class ZoomDescriptorComponent {
        double vBegin, vLen;
        int gBegin, gLen, dc;
        boolean dummy;
        Axis axis;
        
        ZoomDescriptorComponent() {
            dummy=true;
        }
        
        ZoomDescriptorComponent(final Axis a) {
            vBegin=a.vBegin; vLen=a.vLen; gBegin=a.gBegin; gLen=a.gLen;
            dc=a.datacount;
            axis=a;
            dummy=false;
        }
    }
    
    public void performZoomIn(final int x1, final int y1, final int x2, final int y2) {
        performZoomIn(x1, y1, x2, y2, ax, ay);
    }
    
    public void performZoomIn(final int x1, final int y1, final int x2, final int y2, final Axis xAx, final Axis xAy) {
        if (Global.DEBUG>0) System.out.println("performZoomIn("+x1+","+y1+","+x2+","+y2+") [zoomSequence.len="+zoomSequence.size()+"]");
        final boolean ins=ignoreNotifications;
        ignoreNotifications=true;
        
        double xExtent = 1.0;
        double xCenter = 1.0;
        if (xAx!=null) {
            double ax2 = 1.0;
            double ax1 = 1.0;
            ax1=xAx.getValueForPos(x1); ax2=xAx.getValueForPos(x2);
            if ((ax2-ax1)*xAx.vLen<0.0) { // fix signum - must be same as vLen
                final double ah=ax2; ax2=ax1; ax1=ah;
            }
            xExtent=(x1==x2)?xAx.vLen/2.0:ax2-ax1;
            xCenter=(ax1+ax2)/2.0;
        }
        double yExtent = 1.0;
        double yCenter = 1.0;
        if (xAy!=null) {
            double ay1 = 1.0;
            double ay2 = 1.0;
            ay1=xAy.getValueForPos(y1); ay2=xAy.getValueForPos(y2);
            if ((ay2-ay1)*xAy.vLen<0.0) { // fix signum - must be same as vLen
                final double ah=ay2; ay2=ay1; ay1=ah;
            }
            yExtent=(y1==y2)?xAy.vLen/2.0:ay2-ay1;
            yCenter=(ay1+ay2)/2;
        }
        if (xAx!=null && xAy!=null && zoomRetainsAspect) {
            double ratioPre=xAx.vLen/xAy.vLen;
            if (ratioPre<0.0) ratioPre=-ratioPre;
            double ratioPost=xExtent/yExtent;
            if (ratioPost<0.0) ratioPost=-ratioPost;
            if (ratioPost>ratioPre) // x1/y1 < x2/y2 => inflate y
                yExtent*=ratioPost/ratioPre;
            else // otherwise inflate x
                xExtent*=ratioPost/ratioPre;
        }
        if (xAx!=null) {
            zoomSequence.addElement(new ZoomDescriptorComponent(ax));
            ax.setValueRange(xCenter-xExtent/2.0,xExtent);
        } else zoomSequence.addElement(new ZoomDescriptorComponent());
        ignoreNotifications=ins;
        if (xAy!=null) {
            zoomSequence.addElement(new ZoomDescriptorComponent(xAy));
            xAy.setValueRange(yCenter-yExtent/2.0,yExtent);
        } else zoomSequence.addElement(new ZoomDescriptorComponent());
        updateObjects();
        setUpdateRoot(0);
        repaint();
    }
    
    public void performZoomOut(final int x, final int y) {
        if (Global.DEBUG>0) System.out.println("performZoomOut("+x+","+y+") [zoomSequence.len="+zoomSequence.size()+"]");
        final int tail=zoomSequence.size()-1;
        if (tail<1) return;
        final ZoomDescriptorComponent zx;
        zx=(ZoomDescriptorComponent)zoomSequence.elementAt(tail-1);
        final ZoomDescriptorComponent zy;
        zy=(ZoomDescriptorComponent)zoomSequence.elementAt(tail);
        final boolean ins=ignoreNotifications;
        ignoreNotifications=true;
        if (!zx.dummy && zx.axis!=null)
            zx.axis.setValueRange(zx.vBegin,zx.vLen);
        ignoreNotifications=ins;
        if (!zy.dummy && zy.axis!=null)
            zy.axis.setValueRange(zy.vBegin,zy.vLen);
        zoomSequence.removeElement(zy);
        zoomSequence.removeElement(zx);
        updateObjects();
        setUpdateRoot(0);
        repaint();
    }
    
    public void resetZoom() {
        if (Global.DEBUG>0) System.out.println("resetZoom() [zoomSequence.len="+zoomSequence.size()+"]");
        if (zoomSequence.size()>1) {
            final ZoomDescriptorComponent zx;
            zx=(ZoomDescriptorComponent)zoomSequence.elementAt(0);
            final ZoomDescriptorComponent zy;
            zy=(ZoomDescriptorComponent)zoomSequence.elementAt(1);
            final boolean ins=ignoreNotifications;
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
    
    public void mousePressed(final MouseEvent ev) {
        if (Global.DEBUG>0) System.out.println("Event:"+ev);
        
        baseDragX1=ev.getX(); baseDragY1=ev.getY();
        qi.hide();
        selDrag=Common.isSelectTrigger(ev);
        zoomDrag=Common.isZoomTrigger(ev);
        moveDrag=Common.isMoveTrigger(ev);
        if (selDrag || (allowDragZoom && zoomDrag) || (allowDragMove && moveDrag))
            baseDrag=true;
    }
    
    public void mouseReleased(final MouseEvent e) {
        if (Global.DEBUG>0) System.out.println("Event:"+e);
        
        int X1=baseDragX1, Y1=baseDragY1, X2=baseDragX2, Y2=baseDragY2;
        if (!baseDrag || (X1==e.getX() && Y1==e.getY())) { // if p1=p2 then this is a click so let "mouse clicked" handle it. we also bail out if no dragging is performed
            baseDrag=false;
            return;
        }
        if (baseDragX1>baseDragX2) { X2=baseDragX1; X1=baseDragX2; }
        if (baseDragY1>baseDragY2) { Y2=baseDragY1; Y1=baseDragY2; }
        final Rectangle sel=new Rectangle(X1,Y1,X2-X1,Y2-Y1);
        baseDrag=false;
        
        //System.out.println("BaseCanvas.mouseReleased");
        setUpdateRoot(2);
        if (selDrag && pp!=null) {
            boolean setTo=false;
            if (Common.getSelectMode(e)==2) setTo=true;
            if (Common.getSelectMode(e)==0) m.selectNone();
            
            final PlotPrimitive[] pps=getPrimitivesIntersecting(sel);
            int i=0;
            while (i<pps.length) {
                if (pps[i]!=null) pps[i].setMark(m,setTo);
                i++;
            }
            m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
            setUpdateRoot(1);
        }
        if (zoomDrag)
            performZoomIn(X1,Y1,X2,Y2);
        repaint();
    }
    
    public void mouseEntered(final MouseEvent e) {};
    public void mouseExited(final MouseEvent e) {};
    public void mouseDragged(final MouseEvent e) {
        if (baseDrag) {
            final int x=e.getX();
            final int y=e.getY();
            if (x!=baseDragX2 || y!=baseDragY2) {
                baseDragX2=x; baseDragY2=y;
                setUpdateRoot(2);
                repaint();
            }
        }
    };
    public void mouseMoved(final MouseEvent ev) {
        final int x=ev.getX();
        final int y=ev.getY();
        
        final Point cl=getFrame().getLocation();
        final Point tl=pc.getLocation(); cl.x+=tl.x; cl.y+=tl.y;
        
        boolean hideQI = true;
        final boolean actionQuery=Common.isQueryTrigger(ev);
        
        if (actionQuery) {
            if (pp!=null) {
                int i=0;
                while (i<pp.length) {
                    if (pp[i]!=null && pp[i].contains(x,y)) {
                        if (pp[i].cases()>0) {
                            if (pp[i].getPrimaryCase()!=-1) {
                                setQueryText(queryObject(i),pp[i].getPrimaryCase());
                            } else {
                                setQueryText(queryObject(i),pp[i].getCaseIDs());
                            }
                        } else {
                            setQueryText(queryObject(i));
                        }
                        qi.setLocation(cl.x+x,cl.y+y);
                        qi.show(); hideQI=false;
                    }
                    i++;
                }
            }
        }
        final boolean effect = false;
        if (effect) m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
        if (hideQI) qi.hide();
    };
    
    public void keyTyped(final KeyEvent e) {
        if (e.getKeyChar()=='P') run(this,M_PRINT);
        if (e.getKeyChar()=='X') run(this,"exportPGS");
        if (e.getKeyChar()=='C') run(this,M_EXPORTCASES);
    };
    
    public void keyPressed(final KeyEvent e) {
        final int kc=e.getKeyCode();
        if (kc==KeyEvent.VK_ALT && !inZoom && !inQuery) {
            pc.setCursor(Common.cur_query);
            inQuery=true;
        }
        if (kc==KeyEvent.VK_META && allowZoom && !inZoom && !inQuery) {
            pc.setCursor(Common.cur_zoom);
            inZoom=true;
        }
    };
    
    public void keyReleased(final KeyEvent e) {
        final int kc=e.getKeyCode();
        if (kc==KeyEvent.VK_ALT && !inZoom) {
            pc.setCursor(Common.cur_arrow);
            inQuery=false;
        }
        if (kc==KeyEvent.VK_META && allowZoom && !inQuery) {
            pc.setCursor(Common.cur_arrow);
            inZoom=false;
        }
    };
    
    public Object run(final Object o, final String cmd) {
        super.run(o,cmd);
        if (m!=null) m.run(o,cmd);
        if (M_PRINT.equals(cmd)) run(o,"exportPS");
        if (M_ROTATE.equals(cmd)) rotate(1);
        if ("flip".equals(cmd) && allow180) rotate(2);
        if ("exit".equals(cmd)) WinTracker.current.Exit();
        if (M_EXPORTCASES.equals(cmd)) {
            final Vector vars = new Vector();
            SVar var;
            int i=0;
            while((var=getData(i++))!=null) vars.add(var);
            if(vars.size()>0) {
                final SVar[] v = new SVar[vars.size()];
                vars.toArray(v);
                try {
                    final PrintStream p=Tools.getNewOutputStreamDlg(myFrame,"Export selected cases to ...","selected.txt");
                    if (p!=null) {
                        String str = v[0].getName();
                        for(int j=1; j<v.length; j++) str += "\t"+v[j].getName();
                        p.println(str);
                        i=0;
                        final int sz=v[0].size();
                        Object oo;
                        while(i<sz) {
                            if (m.at(i)) {
                                oo=v[0].at(i);
                                str = ((oo==null)?"NA":oo.toString());
                                for(int j=1; j<v.length; j++){
                                    oo=v[j].at(i);
                                    str += "\t"+((oo==null)?"NA":oo.toString());
                                }
                                p.println(str);
                            }
                            i++;
                        }
                        p.close();
                    }
                } catch (Exception eee) {}
            }
        }
        if(M_RESETZOOM.equals(cmd)){
            resetZoom();
        }
        return null;
    };
    
    public void actionPerformed(final ActionEvent e) {
        if (e==null) return;
        run(e.getSource(),e.getActionCommand());
    };
    
    public void setQueryText(final String s) {
        qi.setContent(s);
    }
    
    public void setQueryText(final String s, final int cid) {
        qi.setContent(s,cid);
    }
    
    public void setQueryText(final String s, final int[] cid) {
        qi.setContent(s,cid);
    }
    
    public boolean isShowLabels() {
        return showLabels;
    }
    
    public void setShowLabels(final boolean showLabels) {
        this.showLabels = showLabels;
        labels.show=showLabels;
    }
    
    /**
     * Determine the plot primitives containing the given point.
     * Can be overridden to achieve better performance.
     * @return Array of matching primitives.
     */
    protected PlotPrimitive[] getPrimitivesContaining(final int x, final int y){
        final PlotPrimitive buf[] = new PlotPrimitive[pp.length];
        int i=0;
        int j=0;
        while (i<pp.length) {
            if (pp[i]!=null && pp[i].contains(x,y)) buf[j++]=pp[i];
            i++;
        }
        final PlotPrimitive ret[] = new PlotPrimitive[j];
        System.arraycopy(buf, 0, ret, 0, j);
        return ret;
    }
    
    /**
     * Determine the first plot primitive containing the given point.
     * Can be overridden to achieve better performance.
     * @return The matching primitive or null if point doesn't belong to any primitive.
     */
    protected PlotPrimitive getFirstPrimitiveContaining(final int x, final int y){
        int i=0;
        while (i<pp.length) {
            if (pp[i]!=null && pp[i].contains(x,y)) return pp[i];
            i++;
        }
        return null;
    }
    
    /**
     * Determine the plot primitives intersecting the given rectangle.
     * Can be overridden to achieve better performance.
     * @return Array of intersecting primitives.
     */
    protected PlotPrimitive[] getPrimitivesIntersecting(final Rectangle rec){
        final PlotPrimitive buf[] = new PlotPrimitive[pp.length];
        int i=0;
        int j=0;
        while (i<pp.length) {
            //System.out.println("pp["+i+"]="+pp[i]);
            if (pp[i]!=null && pp[i].intersects(rec)) buf[j++]=pp[i];
            i++;
        }
        final PlotPrimitive ret[] = new PlotPrimitive[j];
        System.arraycopy(buf, 0, ret, 0, j);
        return ret;
    }
    
    /**
     * Possibly adjust mLeft etc.
     * @return Returns <code>true</code> if margins have been changed
     **/
    public boolean adjustMargin(){return false;};
    
    protected void createMenu(Frame f, boolean rotate, boolean zoom, String[] view){
        String myMenu[] = new String[((view==null)?0:(view.length)) + 12];
        int i=0;
        myMenu[i++] = "+";
        myMenu[i++] = "File";
        myMenu[i++] = "~File.Graph";
        myMenu[i++] = "~Edit";
        if(view!=null && view.length>0){
            myMenu[i++] = "+";
            myMenu[i++] = "View";
            if(rotate){
                myMenu[i++] = "@RRotate";
                myMenu[i++] = M_ROTATE;
            }
            if(zoom){
                myMenu[i++] = "@HReset zoom";
                myMenu[i++] = M_RESETZOOM;
            }
            for (int j=0; j<view.length; j++){
                myMenu[i++] = view[j];
            }
        }
        myMenu[i++] = "~Window";
        myMenu[i++] = "0";
        EzMenu.getEzMenu(f,this,myMenu);
    }
}
