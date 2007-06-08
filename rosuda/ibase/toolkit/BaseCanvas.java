package org.rosuda.ibase.toolkit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.PopupMenu;
import java.awt.event.*;
import java.io.PrintStream;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.pograss.*;
import org.rosuda.util.Global;
import org.rosuda.util.Stopwatch;
import org.rosuda.util.Tools;

/** BaseCanvas - basis for all interactive plots which rely on plot primitives concept. To ensure consistent behavior all plots should be based on this class whenever possible. BaseCanvas includes key and mouse handling, selection and queries.<p>Although BaseCanvas is not abstract, is it not usable on its own (except maybe for testing). Any subclasses should override at least the {@link #updateObjects} method to initialize plot primitives. Displaying and selection of following plot primitives is supported out-of-the-box: points, rectangles and polygons. The subclass constructor should (beside calling super constructor) set any of the control flags to customize the behavior of this class.<p>BaseCanvas implements all key and mouse listeners as well as commander interface (actions are mapped into commands). This implies that a subclass it free to overload any individual methods of those. Just make sure that you provide calls to parent methods to preserve all functionality.
 *
 * @version $Id$
 */
public class BaseCanvas extends PGSCanvas implements Dependent, MouseListener, MouseMotionListener, KeyListener, ActionListener {
    public Color COL_OUTLINE=Color.BLACK;
    public Color COL_INVALID=Color.RED;
    public Color COL_SELBG = new Color(Common.selectColor.getRed(),Common.selectColor.getGreen(),Common.selectColor.getBlue(),76);
    public Color COL_ZOOMBG = new Color(0.0f,0.3f,1.0f,0.25f);
    
    protected static final String C_BLACK = "black";
    static final String C_MARKED = "marked";
    static final String C_ASELBG = "aSelBg";
    static final String C_ADRAGBG = "aDragBg";
    static final String C_OBJECT = "object";
    static final String M_PRINT = "print";
    static final String M_EXPORTCASES = "exportCases";
    static final String M_ROTATE = "rotate";
    static final String M_SONLYSELECTED = "showOnlySelected";
    static final String M_SEPERATEALPHAS = "seperateAlphas";
    static final String M_HALPHADOWN = "halphaDown";
    static final String M_HALPHAUP = "halphaUp";
    protected static final String M_RESETZOOM = "resetZoom";
    protected static final String M_TRANSHIGHL = "transparentHighlighting";
    protected static final String M_ALPHADOWN = "alphaDown";
    protected static final String M_ALPHAUP = "alphaUp";
    protected static final String M_SETCB1 = "setCB1";
    protected static final String M_SETCB64 = "setCB64";
    static final String M_RESETCB = "resetCB";
    static final String M_SETCBBYSEL = "setCBbyselection";
    
    public Color fillColor = null;
    public Color borderColor = null;
    public Color fillColorSel = null;
    public Color borderColorSel = null;
    
    /** query popup window */
    protected QueryPopup qi;
    
    /** plot primitives which form the basis for data display and selection */
    protected PlotPrimitive[] pp;
    
    /**
     * List of objects that respond to queries not including those contained in {@link #pp}.
     * Should contain only objects which implement the interface Queryable.
     */
    protected List queryables = new ArrayList();
    
    /** by default BaseCanvas caches layers whenever possible, that is only {@link #paintInit} is guaranteed        to be run before {@link #paintBack}, {@link #paintObjects} and {@link #paintSelected}. This implies that none of the later three can rely on anything happening in the preceeding paint methods. This is not the usual behavior of PoGraSS. Setting this dontCache flag to <code>true</code> will enforce the defined PoGraSS behavior which means that all paint.. parts are called in the specified order, no matter which layer is being updated. */
    protected boolean dontCache=false;
    
    /** marker of the plot. This marker is used for linked highlighting. */
    protected SMarker m;
    
    /** if set to <code>true</code> then the margins can be adjusted automatically */
    public boolean autoAdjustMargins=true;
    /** plot area margins. Axes are set in a way to always respect those margins. They default to 0 for BaseCanvas. */
    protected int mLeft, mRight, mTop, mBottom;
    /** orientation of the plot. Value between 0 and 3. Increasing orientation results in plot rotating by 90 degrees clock-wise. */
    protected int orientation=0;
    /** if set to <code>true</code> then rotating the canvas also results in flipped size geometry of the canvas. */
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
    
    /** run-time flag is set to <code>true</code> if query mode is on - (currently it means that <Ctrl> is held down) */
    protected boolean inQuery=false;
    /** run-time flag is set to <code>true</code> if zoom mode is on */
    protected boolean inZoom=false;
    
    /** if set to <code>true</code> only hilighted primitives will be shown */
    protected boolean showOnlyHilited=false;
    
    /** if set to <code>true</code> all notifications are rejected. Any subclass is free to use it, BaseCanvas modifies this flag in default zoom processing methods to prevent partial updates when ax and ay are updated sequentially. Any method changing this flag should always restore the state of the flag after it finishes! Also use with care in multi-threaded applications to prevent deadlocks. */
    protected boolean ignoreNotifications=false;
    
    /** this list can be used to track the sequence of zooms. Zoom out should return to the state before last zoom in (if sensible in the context of the given plot). Any implementation of {@link #performZoomIn} and {@link #performZoomOut} is free to use this list in any way which suits the implementation.<p>The current default implementation uses pairs of {@link ZoomDescriptorComponent} objects to store status of {@link #ax} and {@link #ay} axes. The list is automatically initilized to an empty list by the base constructor. */
    protected List zoomSequence;
    
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
    
    /** if set to <code>true</code> don't paint objects outside the margins defined by mLeft etc. */
    protected boolean objectClipping=false;
    
    /** if set to <code>true</code> alpha will be applied to highlighting, too */
    protected boolean alphaHighlighting=false;
    
    /** if set to <code>true</code> hilited and normal primitives can have different alphas */
    protected boolean seperateAlphas=false;
    
    protected float ppAlpha = 1.0f;
    
    /** alpha value for hilited primitives; is only used when {@link #seperateAlphas} is <code>true</code> */
    protected float ppAlphaH = 1.0f;
    
    /** arrays of additional axes that are updated upon resize. can be null */
    protected Axis[] opAx; // axes parallel to ax
    protected Axis[] opAy; // axes parallel to ay
    
    /** PlotText object containing labels. Can be null. */
    protected PlotTextVector xLabels;
    protected PlotTextVector yLabels;
    
    /** if set to <code>true</code> extended query is used */
    protected boolean isExtQuery = false;
    private boolean useExtQuery = false; // for manually generated extended query
    private String extQueryString = "";
    
    MenuItem MIsonlyselected=null;
    MenuItem MIseperatealphas=null;
    MenuItem MIalphaup=null;
    MenuItem MIalphadown=null;
    MenuItem MIhalphaup=null;
    MenuItem MIhalphadown=null;
    protected MenuItem MItransHighl=null;
    
    protected PopupMenu pop=new PopupMenu();
    
    protected int mouseX;
    protected int mouseY;
    
    protected PlotPrimitive lastQueriedPrimitive=null;
    protected int lastQueriedIndex;
    
    private boolean useObjectTranparency = true;
    
    // default values for margins; 1..3 indicates orientation
    protected int defaultMLeft;
    protected int defaultMRight;
    protected int defaultMTop;
    protected int defaultMBottom;
    protected int defaultMLeft1;
    protected int defaultMRight1;
    protected int defaultMTop1;
    protected int defaultMBottom1;
    protected int defaultMLeft2;
    protected int defaultMRight2;
    protected int defaultMTop2;
    protected int defaultMBottom2;
    protected int defaultMLeft3;
    protected int defaultMRight3;
    protected int defaultMTop3;
    protected int defaultMBottom3;
    
    
    public int horizontalMedDist=50;
    public int horizontalMinDist=35;
    public int verticalMedDist=50;
    
    private int maxMLeft = 60;
    public int verticalMinDist=35;
    
    public boolean rotateYLabels=true;
    
    private boolean alphaLimitReached=false;
    private final float maxAlpha = 1f;
    private final float minAlpha = 0.05f;
    
    public double rotateYLabelsBy=90;
    
    protected int changingHilitingNeedsUpdateRoot = 1;
    
    protected boolean showQuery=false;
    
    /** basic constructor. Every subclass must call this constructor
     * @param f frame owning this canvas. since BaseCanvas itself doesn't modify any attribute of the frame except for title it is possible to put more canvases into one frame. This doesn't have to hold for subclasses, especially those providing their own menus.
     * @param mark marker which will be used for selection/linked highlighting
     */
    public BaseCanvas(final int gd, final Frame f, final SMarker mark) {
	super(gd,4); // 4 layers; 0=bg, 1=sel, 2=baseDrag, 3=pm
	Global.forceAntiAliasing = true;
	m=mark; setFrame(f);
	ax=ay=null;
	zoomSequence=new ArrayList();
	mLeft=mRight=mTop=mBottom=0;
	setBackground(Common.backgroundColor);
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this);
	f.addKeyListener(this);
	qi=newQueryPopup(f,mark==null?null:mark.getMasterSet(),"BaseCanvas");
	xLabels=new PlotTextVector(getPlotManager(),PlotObject.CS_ABS,PlotObject.CS_ABS);
	yLabels=new PlotTextVector(getPlotManager(),PlotObject.CS_ABS,PlotObject.CS_ABS);
	xLabels.setLayer(0);
	yLabels.setLayer(0);
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
	setUpdateRoot((msg.getMessageID()==Common.NM_MarkerChange)?changingHilitingNeedsUpdateRoot:0);
	repaint();
    };
    
    /** rebuilds plot primitives - subclasses should override this method. It is not defined as abstract only for convenience, but minimal subclass simply overrides this method. */
    public void updateObjects() {
    };
    
    public Dimension getMinimumSize() { return new Dimension(mLeft+mRight+20,mTop+mBottom+20); };
    
    public Axis getAxisX() {
	return ax;
    }
    
    public Axis getAxisY() {
	return ay;
    }
    
    /** actual paint method - subclasses should NOT override this method! use paintInit/Back/Objects/Selected/Post instead. Splitting into pieces allows more effective layer caching and results in better performance */
    public void paintPoGraSS(final PoGraSS g) {
	if(dontPaint) return;
	if(autoAdjustMargins) adjustMargin(g);
	//System.out.println("BaseCanvas.paintPoGraSS(): "+g.localLayerCache);
	final Rectangle r=getBounds();
	final int w=r.width;
	final int h=r.height;
	if (Global.DEBUG>0)
	    System.out.println("BaseCanvas.paint: real bounds ["+w+":"+h+"], existing ["+W+":"+H+"], orientation="+orientation+" mTop="+mTop+",mBottom="+mBottom);
	boolean marginsAdjusted=false;
	final boolean ySizeChanged = ((orientation&1)==0)?(H!=h):(W!=w);
	final boolean xSizeChanged = ((orientation&1)==0)?(W!=w):(H!=h);
	do{
	    if (ay!=null && (ySizeChanged || updateGeometry)){
		setAyDefaultGeometry();
	    }
	    if(opAy!=null && (ySizeChanged || updateGeometry)){
		switch (orientation) {
		    case 0: for(int i=0; i<opAy.length; i++) if(opAy[i]!=null) opAy[i].setGeometry(Axis.O_Y,h-mBottom,mTop+mBottom-h); break;
		    case 1: for(int i=0; i<opAy.length; i++) if(opAy[i]!=null) opAy[i].setGeometry(Axis.O_X,mLeft,w-mLeft-mRight); break;
		    case 2: for(int i=0; i<opAy.length; i++) if(opAy[i]!=null) opAy[i].setGeometry(Axis.O_Y,mTop,h-mTop-mBottom); break;
		    case 3: for(int i=0; i<opAy.length; i++) if(opAy[i]!=null) opAy[i].setGeometry(Axis.O_X,w-mRight,mLeft+mRight-w); break;
		}
	    }
	    if (ax!=null && (xSizeChanged || updateGeometry)){
		setAxDefaultGeometry();
	    }
	    if(opAx!=null && (xSizeChanged || updateGeometry)){
		switch (orientation) {
		    case 0: for(int i=0; i<opAx.length; i++) if(opAx[i]!=null) opAx[i].setGeometry(Axis.O_X,mLeft,w-mLeft-mRight); break;
		    case 1: for(int i=0; i<opAx.length; i++) if(opAx[i]!=null) opAx[i].setGeometry(Axis.O_Y,mTop,h-mTop-mBottom); break;
		    case 2: for(int i=0; i<opAx.length; i++) if(opAx[i]!=null) opAx[i].setGeometry(Axis.O_X,w-mRight,mLeft+mRight-w); break;
		    case 3: for(int i=0; i<opAx.length; i++) if(opAx[i]!=null) opAx[i].setGeometry(Axis.O_Y,h-mBottom,mTop+mBottom-h); break;
		}
	    }
	    marginsAdjusted = (autoAdjustMargins && adjustMargin(g));
	    updateGeometry = updateGeometry || marginsAdjusted;
	} while (marginsAdjusted);
	if (H!=h || W!=w || updateGeometry) {
	    W=w; H=h;
	    updateObjects();
	}
	updateGeometry=false;
	if (Global.DEBUG>0)
	    System.out.println("BaseCanvas.paint: [w="+w+"/h="+h+"] ax="+ax+" ay="+ay);
	
	g.setBounds(w,h);
	g.begin();
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
	    g.setColor((selDrag)?COL_SELBG:COL_ZOOMBG);
	    g.fillRect(dx1,dy1,dx2-dx1,dy2-dy1);
	    g.setColor(COL_OUTLINE);
	    g.drawRect(dx1,dy1,dx2-dx1,dy2-dy1);
	}
	
	nextLayer(g);
	if (dontCache || g.localLayerCache<0 || g.localLayerCache==3) paintPost(g);
	g.end();
	// TODO: this one makes trouble in opengl
//        setUpdateRoot(4);
    }
    
    public void paintInit(final PoGraSS g) {
	//System.out.println("BaseCanvas.paintInit");
    }
    
    public void paintBack(final PoGraSS g) {
	//System.out.println("BaseCanvas.paintBack");
    }
    
    public void paintObjects(final PoGraSS g) {
	boolean performingAlphaBlending = false;
	//System.out.println("BaseCanvas.paintObjects, (cache="+g.localLayerCache+") pp="+pp);
	final Stopwatch sw=new Stopwatch();
	if(objectClipping) g.setClip(mLeft, mTop, getBounds().width-mLeft-mRight, getBounds().height-mTop-mBottom);
	if (pp!=null) {
	    
	    g.setColor(C_OBJECT);
	    int i = 0;
	    while (i<pp.length) {
		if (pp[i]!=null && pp[i].isVisible()){
		    if(pp[i].isPerformingAlphaBlending()){
			if(!performingAlphaBlending){
			    g.setGlobalAlpha(ppAlpha);
			    performingAlphaBlending=true;
			}
		    } else{
			if(performingAlphaBlending){
			    g.resetGlobalAlpha();
			    performingAlphaBlending=false;
			}
		    }
		    
		    pp[i].paint(g, orientation,  m);
		}
		i++;
	    }
	    if(performingAlphaBlending) g.resetGlobalAlpha();
	}
	if(objectClipping) g.resetClip();
	sw.profile("BaseCanvas.paintObjects");
    }
    
    public void paintSelected(final PoGraSS g) {
	boolean performingAlphaBlending = alphaHighlighting;
	final Stopwatch sw=new Stopwatch();
	
	//System.out.println("BaseCanvas.paintSelected, pp="+pp);
	if(objectClipping) g.setClip(mLeft, mTop, getBounds().width-mLeft-mRight, getBounds().height-mTop-mBottom);
	if (pp!=null) {
	    
	    if(alphaHighlighting) g.setGlobalAlpha(seperateAlphas?ppAlphaH:ppAlpha);
	    g.setColor(C_MARKED);
	    int i = 0;
	    while (i<pp.length) {
		if (pp[i]!=null && pp[i].isVisible()){
		    if(pp[i].isPerformingAlphaBlending()){
			if(alphaHighlighting){
			    if(!performingAlphaBlending){
				g.setGlobalAlpha(ppAlpha);
				performingAlphaBlending=true;
			    }
			} else{
			    if(performingAlphaBlending){
				g.resetGlobalAlpha();
				performingAlphaBlending=false;
			    }
			}
		    }
		    pp[i].paintSelected(g,orientation,m);
		}
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
	
	final int button=ev.getButton();
	if(button==MouseEvent.BUTTON3) {
	    if(pop!=null) pop.show(getFrame(),x,y+40);
	    return;
	}
	
	if(baseDragX1==x && baseDragY1==y){
	    final Point cl=getFrame().getLocation();
	    final Point tl=getLocation(); cl.x+=tl.x; cl.y+=tl.y;
	    
	    
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
			if(p!=null && p.isQueryable()){
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
				if(showQuery) {
				    qi.setLocation(cl.x+x,cl.y+y);
				    qi.show(); hideQI=false;
				}
			    }
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
	if(pp!=null && pp[i]!=null) return queryObject(pp[i]);
	return "object ID "+i;
    }
    
    // TODO: choose queryobject(int i) xor queryobject(PlotPrimitive p)
    public String queryObject(final PlotPrimitive p) {
	if(lastQueriedPrimitive!=null && lastQueriedPrimitive==p) return queryObject(lastQueriedIndex);
	for(int i=0; i<pp.length; i++){
	    if(pp[i]==p) return queryObject(i);
	}
	
	return "object "+p.toString();
    }
    
    public String queryPlotSpace() {
	return null;
    }
    
    public void rotate(final int amount) {
	orientation=(orientation+amount)&3;
	if (!allow180) orientation&=1;
	
	switch(orientation){
	    case 0:
		mLeft=defaultMLeft;
		mRight=defaultMRight;
		mTop=defaultMTop;
		mBottom=defaultMBottom;
		break;
	    case 1:
		mLeft=defaultMLeft1;
		mRight=defaultMRight1;
		mTop=defaultMTop1;
		mBottom=defaultMBottom1;
		break;
	    case 2:
		mLeft=defaultMLeft2;
		mRight=defaultMRight2;
		mTop=defaultMTop2;
		mBottom=defaultMBottom2;
		break;
	    case 3:
		mLeft=defaultMLeft3;
		mRight=defaultMRight3;
		mTop=defaultMTop3;
		mBottom=defaultMBottom3;
		break;
	}
	
	setUpdateRoot(0);
	updateGeometry=true;
	if (resizeOnRotate && (amount==1 || amount==3)) {
	    final Frame f = getFrame();
	    final Dimension d = f.getSize();
	    f.setSize(d.height,d.width);
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
	if (xAx!=null && xAx.getType()==Axis.T_Num) {
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
	if (xAy!=null && xAy.getType()==Axis.T_Num) {
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
	if (xAx!=null && xAy.getType()==Axis.T_Num) {
	    zoomSequence.add(new ZoomDescriptorComponent(ax));
	    ax.setValueRange(xCenter-xExtent/2.0,xExtent);
	} else zoomSequence.add(new ZoomDescriptorComponent());
	ignoreNotifications=ins;
	if (xAy!=null && xAy.getType()==Axis.T_Num) {
	    zoomSequence.add(new ZoomDescriptorComponent(xAy));
	    xAy.setValueRange(yCenter-yExtent/2.0,yExtent);
	} else zoomSequence.add(new ZoomDescriptorComponent());
	updateObjects();
	setUpdateRoot(0);
	repaint();
    }
    
    public void performZoomOut(final int x, final int y) {
	if (Global.DEBUG>0) System.out.println("performZoomOut("+x+","+y+") [zoomSequence.len="+zoomSequence.size()+"]");
	final int tail=zoomSequence.size()-1;
	if (tail<1) return;
	final ZoomDescriptorComponent zx;
	zx=(ZoomDescriptorComponent)zoomSequence.get(tail-1);
	final ZoomDescriptorComponent zy;
	zy=(ZoomDescriptorComponent)zoomSequence.get(tail);
	final boolean ins=ignoreNotifications;
	ignoreNotifications=true;
	if (!zx.dummy && zx.axis!=null)
	    zx.axis.setValueRange(zx.vBegin,zx.vLen);
	ignoreNotifications=ins;
	if (!zy.dummy && zy.axis!=null)
	    zy.axis.setValueRange(zy.vBegin,zy.vLen);
	zoomSequence.remove(zy);
	zoomSequence.remove(zx);
	updateObjects();
	setUpdateRoot(0);
	repaint();
    }
    
    public void resetZoom() {
	if (Global.DEBUG>0) System.out.println("resetZoom() [zoomSequence.len="+zoomSequence.size()+"]");
	if (zoomSequence.size()>1) {
	    final ZoomDescriptorComponent zx;
	    zx=(ZoomDescriptorComponent)zoomSequence.get(0);
	    final ZoomDescriptorComponent zy;
	    zy=(ZoomDescriptorComponent)zoomSequence.get(1);
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
	zoomSequence.clear();
    }
    
    public void mousePressed(final MouseEvent ev) {
	if (Global.DEBUG>0) System.out.println("Event:"+ev);
	
	baseDragX1=baseDragX2=ev.getX(); baseDragY1=baseDragY2=ev.getY();
	qi.hide();
	selDrag=Common.isSelectTrigger(ev);
	zoomDrag=Common.isZoomTrigger(ev);
	moveDrag=Common.isMoveTrigger(ev);
	if (Global.DEBUG>0) System.out.println("BaseCanvas:mousePressed selection:"+selDrag+" zoom:"+zoomDrag+" move:"+moveDrag);
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
	    boolean setTo=true;
	    if (Common.getSelectMode(e)==1) setTo=false;
	    if (Common.getSelectMode(e)==0) m.selectNone();
	    
	    final PlotPrimitive[] pps=getPrimitivesIntersecting(sel);
	    int i=0;
	    while (i<pps.length) {
		if (pps[i]!=null) pps[i].setMark(m,setTo);
		i++;
	    }
	    m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
	    return;
	}
	if (zoomDrag)
	    performZoomIn(X1,Y1,X2,Y2);
	repaint();
    }
    
    public void mouseEntered(final MouseEvent e) {
	/*	if(!pc.getComponent().contains(e.getX(),e.getY())) {
			qi.hide();
	    }*/
    };
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
	mouseX=ev.getX();
	mouseY=ev.getY();
	
	final Point cl=getFrame().getLocation();
	final Point tl=getLocation(); cl.x+=tl.x; cl.y+=tl.y;
	
	boolean hideQI = true;
	final boolean actionQuery=Common.isQueryTrigger(ev);
	final boolean actionExtQuery=Common.isExtQuery(ev);
	if(actionExtQuery) {
	    inQuery = true;
	    isExtQuery = true;
	    if (pp!=null) {
		PlotPrimitive p = getFirstPrimitiveContaining(mouseX,mouseY);
		if(p!=null && p.isQueryable()){
		    if(useExtQuery) setQueryText(extQueryString);
		    else {
			if (p.cases()>0) {
			    if (p.getPrimaryCase()!=-1) {
				setQueryText(queryObject(p),p.getPrimaryCase());
			    } else {
				setQueryText(queryObject(p),p.getCaseIDs());
			    }
			} else {
			    setQueryText(queryObject(p));
			}
		    }
		    if(!showQuery) {
			qi.setLocation(cl.x+mouseX+10,cl.y+mouseY+20);
			qi.show(); hideQI=false;
		    }
		}
	    }
	    isExtQuery = false;
	    setUpdateRoot(3); repaint();
	} else if (actionQuery) {
	    inQuery=true;
	    if (pp!=null) {
		PlotPrimitive p = getFirstPrimitiveContaining(mouseX,mouseY);
		if(p!=null && p.isQueryable()){
		    if (p.cases()>0) {
			if (p.getPrimaryCase()!=-1) {
			    setQueryText(queryObject(p),p.getPrimaryCase());
			} else {
			    setQueryText(queryObject(p),p.getCaseIDs());
			}
		    } else {
			setQueryText(queryObject(p));
		    }
		} else {
		    String s=getAxisQuery(mouseX,mouseY);
		    if(s!=null) {
			setQueryText(s);
		    } else {
			setQueryText(queryPlotSpace());
		    }
		}
		if(showQuery) {
		    qi.setLocation(cl.x+mouseX+10,cl.y+mouseY+20);
		    qi.show(); hideQI=false;
		}
	    }
	    setUpdateRoot(3); repaint();
	} else if(inQuery) {
	    inQuery=false;
	    setUpdateRoot(3); repaint();
	}
	final boolean effect = false;
	if (effect) m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
	if (hideQI) qi.hide();
    };
    
    // should be overriden by subclasses: they know axis coordinates
    protected String getAxisQuery(int x, int y) {
	return null;
    }
    public void keyTyped(final KeyEvent e) {
	if (e.getKeyChar()=='P') run(this,M_PRINT);
	if (e.getKeyChar()=='X') run(this,"exportPGS");
	if (e.getKeyChar()=='C') run(this,M_EXPORTCASES);
	if (e.getKeyChar()==',') run(this,M_HALPHADOWN);
	if (e.getKeyChar()=='.') run(this,M_HALPHAUP);
    };
    
    public void keyPressed(final KeyEvent e) {
	final int kc=e.getKeyCode();
	if (kc==KeyEvent.VK_META && allowZoom && !inZoom && !inQuery) {
	    setCursor(Common.cur_zoom);
	    inZoom=true;
	}
	if(useObjectTranparency){
	    if (kc==KeyEvent.VK_RIGHT) run(this, M_ALPHAUP);
	    if (kc==KeyEvent.VK_LEFT) run(this, M_ALPHADOWN);
	}
    };
    
    
    public void keyReleased(final KeyEvent e) {
	final int kc=e.getKeyCode();
	if (kc==KeyEvent.VK_META && allowZoom && !inQuery) {
	    setCursor(Common.cur_arrow);
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
	    final List vars = new ArrayList();
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
	if(M_SONLYSELECTED.equals(cmd)){
	    MIsonlyselected.setLabel(showOnlyHilited?"Show only selected cases":"Show all cases");
	    showOnlyHilited = !showOnlyHilited;
	    setUpdateRoot(0);
	    repaint();
	}
	if(M_SEPERATEALPHAS.equals(cmd)){
	    MIseperatealphas.setLabel((seperateAlphas?"Different":"Same") + " transparency for hiliting.");
	    seperateAlphas = !seperateAlphas;
	    MIhalphadown.setEnabled(seperateAlphas && ppAlphaH>minAlpha);
	    MIhalphaup.setEnabled(seperateAlphas && ppAlphaH<maxAlpha);
	    setUpdateRoot(0);
	    repaint();
	}
	if (M_HALPHADOWN.equals(cmd)) {
	    final float oppAlphaH = ppAlpha;
	    ppAlphaH = alphaDown(ppAlphaH);
	    MIhalphadown.setEnabled(!alphaLimitReached);
	    MIhalphaup.setEnabled(true);
	    if(ppAlpha!=oppAlphaH){
		setUpdateRoot(0); repaint();
	    }
	    displayCurrentAlpha();
	}
	if (M_HALPHAUP.equals(cmd)) {
	    final float oppAlphaH = ppAlpha;
	    ppAlphaH = alphaUp(ppAlphaH);
	    MIhalphaup.setEnabled(!alphaLimitReached);
	    MIhalphadown.setEnabled(true);
	    if(ppAlpha!=oppAlphaH){
		setUpdateRoot(0); repaint();
	    }
	    displayCurrentAlpha();
	}
	if (M_ALPHADOWN.equals(cmd)) {
	    final float oppAlpha = ppAlpha;
	    ppAlpha = alphaDown(ppAlpha);
	    MIalphadown.setEnabled(!alphaLimitReached);
	    MIalphaup.setEnabled(true);
	    if(ppAlpha!=oppAlpha){
		setUpdateRoot(0); repaint();
	    }
	    displayCurrentAlpha();
	}
	if (M_ALPHAUP.equals(cmd)) {
	    final float oppAlpha = ppAlpha;
	    ppAlpha = alphaUp(ppAlpha);
	    MIalphaup.setEnabled(!alphaLimitReached);
	    MIalphadown.setEnabled(true);
	    if(ppAlpha!=oppAlpha){
		setUpdateRoot(0); repaint();
	    }
	    displayCurrentAlpha();
	}
	if(M_TRANSHIGHL.equals(cmd)) {
	    alphaHighlighting=!alphaHighlighting;
	    MItransHighl.setLabel(alphaHighlighting?"Opaque highlighting":"Transparent highlighting");
	    setUpdateRoot(1); repaint();
	}
	if (M_RESETCB.equals(cmd)) {
	    if (m.getSecCount()>0) {
		m.resetSec();
		m.NotifyAll(new NotifyMsg(this,Common.NM_SecMarkerChange));
	    }
	}
	if (M_SETCBBYSEL.equals(cmd)) {
	    m.setSecBySelection(16,17);
	    m.NotifyAll(new NotifyMsg(this,Common.NM_SecMarkerChange));
	}
	return null;
    };
    
    private float alphaUp(float alpha){
	alphaLimitReached = false;
	float ret;
	ret = alpha + ((alpha>0.2f)?0.10f:0.02f);
	if(ret>=maxAlpha){
	    alphaLimitReached = true;
	    ret=maxAlpha;
	}
	return ret;
    }
    
    private float alphaDown(float alpha){
	alphaLimitReached = false;
	float ret;
	ret = alpha - ((alpha>0.2f)?0.10f:0.02f);
	if (ret<=minAlpha) {
	    alphaLimitReached = true;
	    ret=minAlpha;
	}
	return ret;
    }
    
    public void actionPerformed(final ActionEvent e) {
	if (e==null) return;
	run(e.getSource(),e.getActionCommand());
    };
    
    public void setQueryText(final String s) {
	if(s==null) { showQuery=false; return; } else { showQuery=true; qi.setContent(s); }
    }
    
    public void setQueryText(final String s, final int cid) {
	if(s==null) { showQuery=false; return; } else {showQuery=true; qi.setContent(s,cid); }
    }
    
    public void setQueryText(final String s, final int[] cid) {
	if(s==null) { showQuery= false; return; } else {showQuery=true; qi.setContent(s,cid); }
    }
    
    public boolean isShowLabels() {
	return showLabels;
    }
    
    public void setShowLabels(final boolean showLabels) {
	this.showLabels = showLabels;
	xLabels.show=showLabels;
	yLabels.show=showLabels;
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
     * @return min and max values (first=min,second=max)
     */
    public double[] getBoundValues() {
	if(pp==null) return new double[]{Double.NaN,Double.NaN};
	double max=0,min=0,temp=0;
	for(int i=0;i<pp.length;i++) {
	    temp=pp[i].cases();
	    if(max<temp) max=temp;
	    if(min>temp) min=temp;
	}
	return new double[]{min,max};
    }
    
    
    /**
     * Possibly adjust mLeft etc.
     * @return Returns <code>true</code> if margins have been changed
     **/
    public boolean adjustMargin(final PoGraSS g){
	final int maxBbw = yLabels.getMaxBoundingBoxWidth(g)+5;
	if(maxBbw>0 && maxBbw<maxMLeft && mLeft<maxBbw){
	    mLeft=maxBbw;
	    return true;
	}
	return false;
    };
    
    
    protected void createMenu(Frame f, boolean rotate, boolean zoom, boolean transparency, boolean activeCB, String[] view){
	String myMenu[] = new String[((view==null)?0:(view.length)) + 37];
	int i=0;
	myMenu[i++] = "+";
	myMenu[i++] = "File";
	myMenu[i++] = "~File.Graph";
	myMenu[i++] = "~Edit";
	if((view!=null && view.length>0) || rotate || zoom || transparency){
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
	    myMenu[i++] = "Show only selected cases";
	    myMenu[i++] = M_SONLYSELECTED;
	    useObjectTranparency = transparency;
	    if(transparency){
		myMenu[i++] = "-";
		myMenu[i++] = "More transparent (left)";
		myMenu[i++] = M_ALPHADOWN;
		myMenu[i++] = "More opaque (right)";
		myMenu[i++] = M_ALPHAUP;
		myMenu[i++] = "Transparent highlighting";
		myMenu[i++] = M_TRANSHIGHL;
		myMenu[i++] = "Different transparency for hiliting.";
		myMenu[i++] = M_SEPERATEALPHAS;
		myMenu[i++] = "Hiliting more transparent.";
		myMenu[i++] = M_HALPHADOWN;
		myMenu[i++] = "Hiliting more opaque.";
		myMenu[i++] = M_HALPHAUP;
	    }
	    myMenu[i++] = "-";
	    if(activeCB){
		
		myMenu[i++] = "Set Colors (CB)";
		myMenu[i++] = M_SETCB1;
		myMenu[i++] = "Set Colors (rainbow)";
		myMenu[i++] = M_SETCB64;
	    }
	    myMenu[i++] = "Paint selection";
	    myMenu[i++] = M_SETCBBYSEL;
	    myMenu[i++] = "Clear Colors";
	    myMenu[i++] = M_RESETCB;
	    if(view!=null){
		myMenu[i++] = "-";
		for (int j=0; j<view.length; j++){
		    myMenu[i++] = view[j];
		}
	    }
	}
	myMenu[i++] = "~Window";
	myMenu[i++] = "0";
	EzMenu.getEzMenu(f,this,myMenu);
	
	if(view!=null) {
	    String[] temp=view; String[] view2=new String[temp.length+1];
	    System.arraycopy(temp,0,view2,0,temp.length);
	    view2[view2.length-1]="0"; temp=null;
	    pop=EzMenu.getEzPopup(f,this,view2);
	} else pop=null;
	
	MIsonlyselected = EzMenu.getItem(f,M_SONLYSELECTED);
	MIseperatealphas = EzMenu.getItem(f,M_SEPERATEALPHAS);
	MIalphadown = EzMenu.getItem(f,M_ALPHADOWN);
	MIalphaup = EzMenu.getItem(f,M_ALPHAUP);
	if(MIalphaup!=null) MIalphaup.setEnabled(false);
	MIhalphadown = EzMenu.getItem(f,M_HALPHADOWN);
	if(MIhalphadown!=null) MIhalphadown.setEnabled(false);
	MIhalphaup = EzMenu.getItem(f,M_HALPHAUP);
	if(MIhalphaup!=null) MIhalphaup.setEnabled(false);
    }
    
    /** needed for setting manually extended query string */
    public void setExtQueryString(String str) {
	extQueryString = str;
	useExtQuery = true;
    }
    
    public void useExtQuery(boolean b) {
	useExtQuery = b;
    }
    
    /** sets the default margins. order: left, right, top, bottom */
    public void setDefaultMargins(int[] margins){
	if(margins!=null || margins.length>=4){
	    defaultMLeft=margins[0];
	    defaultMRight=margins[1];
	    defaultMTop=margins[2];
	    defaultMBottom=margins[3];
	    
	    int shift=0;
	    
	    if(margins.length>=8) shift=4;
	    defaultMLeft1=margins[shift+0];
	    defaultMRight1=margins[shift+1];
	    defaultMTop1=margins[shift+2];
	    defaultMBottom1=margins[shift+3];
	    
	    if(margins.length>=12) shift=8;
	    defaultMLeft2=margins[shift+0];
	    defaultMRight2=margins[shift+1];
	    defaultMTop2=margins[shift+2];
	    defaultMBottom2=margins[shift+3];
	    
	    if(margins.length>=16) shift=12;
	    else if(margins.length>=16) shift=4;
	    defaultMLeft3=margins[shift+0];
	    defaultMRight3=margins[shift+1];
	    defaultMTop3=margins[shift+2];
	    defaultMBottom3=margins[shift+3];
	}
	
	switch(orientation){
	    case 0:
		mLeft=defaultMLeft;
		mRight=defaultMRight;
		mTop=defaultMTop;
		mBottom=defaultMBottom;
		break;
	    case 1:
		mLeft=defaultMLeft1;
		mRight=defaultMRight1;
		mTop=defaultMTop1;
		mBottom=defaultMBottom1;
		break;
	    case 2:
		mLeft=defaultMLeft2;
		mRight=defaultMRight2;
		mTop=defaultMTop2;
		mBottom=defaultMBottom2;
		break;
	    case 3:
		mLeft=defaultMLeft3;
		mRight=defaultMRight3;
		mTop=defaultMTop3;
		mBottom=defaultMBottom3;
		break;
	}
    }
    
    public void addXLabels(PoGraSS g, Axis axis, String[] names, int maxWidth, int[] positions, boolean ticks, boolean abbreviate) {
	addXLabels(g,axis,names,new int[]{maxWidth},positions,ticks,abbreviate,false);
    }
    
    public void addXLabels(PoGraSS g, Axis axis, String[] names, int maxWidth, int[] positions, boolean ticks, boolean abbreviate, boolean top) {
	addXLabels(g,axis,names,new int[]{maxWidth},positions,ticks,abbreviate,top);
    }
    
    public void addXLabels(PoGraSS g, Axis axis, String[] names, int[] maxWidths, int[] positions, boolean ticks, boolean abbreviate) {
	addXLabels(g,axis,names,maxWidths,positions,ticks,abbreviate,false);
    }
    
    public void addXLabels(PoGraSS g, Axis axis, String[] names, int[] maxWidths, int[] positions, boolean ticks, boolean abbreviate, boolean top) {
	double overlap=0; // used to handle overlapping labels
	boolean prevEmpty=true;
	for(int i=0; i<positions.length; i++){
	    final int maxW = maxWidths[Math.min(maxWidths.length-1,i)];
	    if (orientation==0){
		String label=null;
		if (maxW<g.getWidthEstimate(names[i])){ // if there is not enoug space for full category name
		    if(overlap<=0){ // if there is no label overlapping this label's space
			final String abbrCatName = Common.getTriGraph(names[i]);
			if(!abbreviate || maxW<g.getWidthEstimate(abbrCatName)+10){ // if there is not enough space for TriGraph
			    overlap=g.getWidthEstimate(abbrCatName)-maxW+10;
			    if(prevEmpty && abbreviate) label=abbrCatName;
			} else{
			    label=abbrCatName;
			    prevEmpty=false;
			}
		    } else{
			overlap-=maxW;
			prevEmpty=true;
		    }
		} else{
		    label=names[i];
		    prevEmpty=false;
		    if(overlap>0){ // if there is a label overlapping this label's space
			overlap-=maxW;
		    }
		}
		if(label!=null){
		    xLabels.add(positions[i],top?(mTop/2):(getBounds().height-mBottom/2),0.5,0.3,label);
		}
		if(ticks){
		    final int baseYPos = top?(mTop-3):(getBounds().height - mBottom);
		    g.drawLine(positions[i],baseYPos,positions[i],baseYPos+3);
		}
	    } else {
		final int xPos = mLeft-3;
		yLabels.add(xPos,positions[i],1,0.5,maxW,names[i]);
		if(ticks){
		    g.drawLine(mLeft-3,positions[i],mLeft,positions[i]);
		}
	    }
	}
    }
    
    public void addYLabels(PoGraSS g, Axis axis, boolean ticks, boolean abbreviate) {
	addYLabels_internal(g,axis,null,ticks,abbreviate);
    }
    
    public void addYLabels(PoGraSS g, Axis axis, SVar sVar, boolean ticks, boolean abbreviate) {
	addYLabels_internal(g,axis,sVar,ticks,abbreviate);
    }
    
    public void addYLabels(PoGraSS g, Axis axis, String[] names, int[] maxH, int[] positions, boolean ticks, boolean abbreviate) {
	final double rotRad = rotateYLabelsBy*Math.PI/180;
	final double s = Math.sin(rotRad);
	final double c = Math.cos(rotRad);
	for(int i=0; i<positions.length; i++){
	    final double[] ra = rotateAlignment(g.getWidthEstimate(names[i]),g.getHeightEstimate(names[i]),s,c);
	    yLabels.add(mLeft-4,positions[i],ra[0],ra[1],mLeft-4,maxH[i],names[i],rotateYLabels?rotateYLabelsBy:0);
	}
    }
    
    private void addYLabels_internal(PoGraSS g, Axis axis, SVar sVar, boolean ticks, boolean abbreviate) {
	ArrayList text = new ArrayList();
	ArrayList valuePos = new ArrayList();
	ArrayList maxH = new ArrayList();
	
	final double f=axis.getSensibleTickDistance(verticalMedDist,verticalMinDist);
	double fi=axis.getSensibleTickStart(f);
	int i=0;
	while (fi<axis.vBegin+axis.vLen) {
	    if(sVar==null) text.add(axis.getDisplayableValue(fi));
	    else text.add(sVar.getCatAt((int)(fi+0.5)).toString());
	    valuePos.add(new Integer(axis.getValuePos(fi)));
	    i++;
	    fi+=f;
	}
	int[] valuePosA = new int[valuePos.size()];
	i=0;
	for(Iterator it = valuePos.listIterator(); it.hasNext();){
	    valuePosA[i++] = ((Integer)it.next()).intValue();
	}
	if(valuePosA.length>1){
	    maxH.add(new Integer(2*Math.min(Math.abs(valuePosA[0]-mTop),Math.abs(valuePosA[1]-valuePosA[0])/2)));
	    for(i=1;i<valuePosA.length-1;i++){
		maxH.add(new Integer(2*Math.min(Math.abs(valuePosA[i]-valuePosA[i-1])/2,Math.abs(valuePosA[i+1]-valuePosA[i])/2)));
	    }
	    maxH.add(new Integer(2*Math.min(Math.abs(getBounds().height-mBottom-valuePosA[valuePosA.length-1]),Math.abs(valuePosA[valuePosA.length-1]-valuePosA[valuePosA.length-2])/2)));
	} else maxH.add(new Integer(Math.abs(getBounds().height-mBottom-mTop)));
	
	final int maxW = abbreviate?(getDefaultMLeft()-5):(-1);
	final int xPos = mLeft-5;
	
	final double rotRad = rotateYLabelsBy*Math.PI/180;
	final double s = Math.sin(rotRad);
	final double c = Math.cos(rotRad);
	for(i=0; i<valuePosA.length;i++){
	    final int maxHe = abbreviate?(((Integer)maxH.get(i)).intValue()):(-1);
	    double xAlign,yAlign;
	    if(rotateYLabels){
		final double[] ra = rotateAlignment(g.getWidthEstimate((String)text.get(i)),g.getHeightEstimate((String)text.get(i)),s,c);
		xAlign = ra[0];
		yAlign = ra[1];
	    } else{
		xAlign = 1;
		yAlign = 0.5;
	    }
	    
	    yLabels.add(xPos,valuePosA[i],xAlign,yAlign,maxW,maxHe,(String)text.get(i),rotateYLabels?rotateYLabelsBy:0);
	    if(ticks) g.drawLine(mLeft-2,valuePosA[i],mLeft,valuePosA[i]);
	}
    }
    
    public int getMBottom() {
	return this.mBottom;
    }
    
    public void setMBottom(final int mBottom) {
	this.mBottom = mBottom;
	updateGeometry=true;
    }
    
    public int getMLeft() {
	return this.mLeft;
    }
    
    public void setMLeft(final int mLeft) {
	this.mLeft = mLeft;
	updateGeometry=true;
    }
    
    public int getMRight() {
	return this.mRight;
    }
    
    public void setMRight(final int mRight) {
	this.mRight = mRight;
	updateGeometry=true;
    }
    
    public int getMTop() {
	return this.mTop;
    }
    
    public void setMTop(final int mTop) {
	this.mTop = mTop;
	updateGeometry=true;
    }
    
    protected void setColors(PPrimBase ppb) {
	if(fillColor!=null) ppb.fillColor = fillColor;
	if(fillColorSel!=null) ppb.fillColorSel = fillColorSel;
	if(borderColor!=null) ppb.borderColor = borderColor;
	if(borderColorSel!=null) ppb.borderColorSel = borderColorSel;
    }
    
    protected void endAddingLabels() {
	xLabels.finishAdd();
	yLabels.finishAdd();
    }
    
    protected void startAddingLabels() {
	xLabels.clear();
	yLabels.clear();
    }
    
    protected int getDefaultMLeft() {
	final int dml;
	switch(orientation){
	    case 0: dml=defaultMLeft; break;
	    case 1: dml=defaultMLeft1; break;
	    case 2: dml=defaultMLeft2; break;
	    default: dml=defaultMLeft3; break;
	}
	return dml;
    }
    
    protected void setAyDefaultGeometry() {
	final Rectangle r=getBounds();
	final int w=r.width;
	final int h=r.height;
	switch (orientation) {
	    case 0: ay.setGeometry(Axis.O_Y,h-mBottom,mTop+mBottom-h); break;
	    case 1: ay.setGeometry(Axis.O_X,mLeft,w-mLeft-mRight); break;
	    case 2: ay.setGeometry(Axis.O_Y,mTop,h-mTop-mBottom); break;
	    case 3: ay.setGeometry(Axis.O_X,w-mRight,mLeft+mRight-w); break;
	}
    }
    
    protected void setAxDefaultGeometry() {
	final Rectangle r=getBounds();
	final int w=r.width;
	final int h=r.height;
	switch (orientation) {
	    case 0: ax.setGeometry(Axis.O_X,mLeft,w-mLeft-mRight); break;
	    case 1: ax.setGeometry(Axis.O_Y,mTop,h-mTop-mBottom); break;
	    case 2: ax.setGeometry(Axis.O_X,w-mRight,mLeft+mRight-w); break;
	    case 3: ax.setGeometry(Axis.O_Y,h-mBottom,mTop+mBottom-h); break;
	}
    }
    
    private double[] rotateAlignment(final double w, final double h, final double s, final double c) {
	final double[] ret = new double[2];
	final double normalizedRot;
	if(rotateYLabelsBy<0) normalizedRot = rotateYLabelsBy-((int)(rotateYLabelsBy/360+1))*360;
	else normalizedRot = rotateYLabelsBy-((int)(rotateYLabelsBy/360))*360;
	if(normalizedRot < 90){
	    final double hcws = (h*c-w*s)/2;
	    ret[0] = 1+s*hcws/w;
	    ret[1] = c*hcws/h;
	} else if(normalizedRot < 180){
	    final double hcws = (h*c+w*s)/2;
	    ret[0] = s*hcws/w;
	    ret[1] = c*hcws/h;
	} else if(normalizedRot < 270){
	    final double hcws = (h*c-w*s)/2;
	    ret[0] = -s*hcws/w;
	    ret[1] = 1-c*hcws/h;
	} else{
	    final double hcws = (h*c+w*s)/2;
	    ret[0] = 1-s*hcws/w;
	    ret[1] = 1-c*hcws/h;
	}
	return ret;
    }
    
    private void displayCurrentAlpha() {
	StringBuffer sb = new StringBuffer();
	sb.append("Transparency: ").append(ppAlpha).append("\n")
	.append("Transparency of hiliting: ").append(seperateAlphas?ppAlphaH:ppAlpha);
	
	setQueryText(sb.toString());
	final Point cl=getFrame().getLocation();
	qi.setLocation(cl.x+mouseX,cl.y+mouseY);
	qi.show();
    }
}
