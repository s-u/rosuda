package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** implementation of scatterplots
 * @version $Id$
 */
public class ScatterCanvas extends BaseCanvas {
    static final String M_PLUS = "+";
    static final String M_EQUISCALE = "equiscale";
    static final String M_MINUS = "-";
    static final String M_LABELS = "labels";
    static final String M_TRIGRAPH = "trigraph";
    static final String M_NEXTBG = "nextBg";
    static final String M_JITTER = "jitter";
    static final String M_STACKJITTER = "stackjitter";
    static final String M_XRANGEDLG = "XrangeDlg";
    static final String M_YRANGEDLG = "YrangeDlg";
    static final String M_POINTSUP = "points+";
    static final String M_POINTSDOWN = "points-";
    static final String M_ALPHADOWN = "alphaDown";
    static final String M_ALPHAUP = "alphaUp";
    static final String M_TRANSHIGHL = "transparentHighlighting";
    static final String C_OBJECTS = "objects";
    static final String C_RED = "red";
    /** array of two variables (X and Y) */
    protected SVar v[];
    
    /** flag whether axis labels should be shown */
    protected boolean showLabels=true;
    
    /** flag whether jittering shoul dbe used for categorical vars */
    protected boolean jitter=false;
    
    /** in conjunction with jitter this flag determines whether random jittering or stack-plotting is to be used */
    protected boolean stackjitter=false;
    
    /** use trigraph for X axis in case X is categorical */
    protected boolean useX3=false;
    
    /** if true partition nodes above current node only */
    public boolean bgTopOnly=false;
    
    /** diameter of a point */
    public int ptDiam=3;
    public int stackOff=3;
    
    public int fieldBg=0; // 0=none, 1=objects, 2=white
    
    /** # of points */
    protected int pts;
    
    protected int x1, y1, x2, y2;
    protected boolean drag;
    
    protected MenuItem MIlabels=null;
    protected MenuItem MItrigraph=null;
    protected MenuItem MItransHighl=null;
    
    protected int Y,W,H, TW,TH;
    
    protected int []filter=null;
    
    protected boolean querying=false;
    protected int qx,qy;
    
    protected boolean zoomRetainsAspect=false;
    
    private final int standardMLeft=40;
    
    /** sorted set of the points, used to check with log(n) time cost if a point
     *  belongs to an existing primitive
     */
    protected TreeMap sortedPoints;
    
    protected class ComparablePoint implements Comparable{
        int x,y;
        
        public ComparablePoint(int x,int y){this.x=x;this.y=y;}
        
        public int compareTo(Object o) {
            ComparablePoint p = (ComparablePoint) o;
            if(x<p.x || (x==p.x && y<p.y)) return -1;
            if(x==p.x && y==p.y) return 0;
            else return 1;
        }
    }
    
    /** create a new scatterplot
     * @param f associated frame (or <code>null</code> if none)
     * @param v1 variable 1
     * @param v2 variable 2
     * @param mark associated marker */
    public ScatterCanvas(PlotComponent ppc, Frame f, SVar v1, SVar v2, SMarker mark) {
        super(ppc,f,mark);
        
        mLeft=standardMLeft;
        mBottom=30;
        mRight=mTop=10;
        
        v=new SVar[2];
        v[0]=v1; v[1]=v2; m=mark;
        ax=new Axis(v[0],Axis.O_X,v[0].isCat()?Axis.T_EqCat:Axis.T_Num); ax.addDepend(this);
        ay=new Axis(v[1],Axis.O_Y,v[1].isCat()?Axis.T_EqCat:Axis.T_Num); ay.addDepend(this);
        if (!v[0].isCat()) ax.setValueRange(v[0].getMin()-(v[0].getMax()-v[0].getMin())/20,(v[0].getMax()-v[0].getMin())*1.1);
        if (!v[1].isCat()) ay.setValueRange(v[1].getMin()-(v[1].getMax()-v[1].getMin())/20,(v[1].getMax()-v[1].getMin())*1.1);
        if (!v[0].isCat() && v[0].getMax()-v[0].getMin()==0) ax.setValueRange(v[0].getMin()-0.5,1);
        if (!v[1].isCat() && v[1].getMax()-v[1].getMin()==0) ay.setValueRange(v[1].getMin()-0.5,1);
        drag=false;
        MenuBar mb=null;
        if (Global.useAquaBg) fieldBg=2;
        createMenu(f,true,true,new String[]{
            "Same scale",M_EQUISCALE,
            M_MINUS,
            "@LHide labels",M_LABELS,
            "@TShorten labels",M_TRIGRAPH,
            "Change background",M_NEXTBG,
            "@JToggle jittering",M_JITTER,
            "!JToggle stacking",M_STACKJITTER,
            M_MINUS,
            "Set X Range ...",M_XRANGEDLG,
            "Set Y Range ...",M_YRANGEDLG,
            M_MINUS,
            "Bigger points (up)",M_POINTSUP,
            "Smaller points (down)",M_POINTSDOWN,
            M_MINUS,
            "More transparent (left)",M_ALPHADOWN,
            "More opaque (right)",M_ALPHAUP,
            "Transparent highlighting",M_TRANSHIGHL
        });
        MIlabels=EzMenu.getItem(f,M_LABELS);
        MItrigraph=EzMenu.getItem(f,M_TRIGRAPH);
        MItransHighl=EzMenu.getItem(f,M_TRANSHIGHL);
        if (!v1.isCat() && !v2.isCat())
            EzMenu.getItem(f,"jitter").setEnabled(false);
        objectClipping=true;
        dontPaint=false;
    }
    
    public SVar getData(int id) { return (id<0||id>1)?null:v[id]; }
    
    public void setFilter(int[] f) {
        filter=f;
        setUpdateRoot(1);
        repaint();
    };
    
    public void setFilter(Vector v) {
        if (v==null) { filter=null; return; };
        filter=new int[v.size()];
        int j=0; while(j<v.size()) { filter[j]=((Integer)v.elementAt(j)).intValue(); j++; };
    };
    
    public void rotate() {
        try {
            ((Frame) pc.getParent()).setTitle("Scatterplot ("+v[(orientation+1)&1].getName()+" vs "+v[orientation&1].getName()+")");
        } catch (Exception ee) {};
    };
    
    // clipping warnings
    boolean hasLeft, hasTop, hasRight, hasBot;
    
    public void updateObjects() {
        Dimension Dsize=pc.getSize();
        int w=Dsize.width, h=Dsize.height;
        TW=w; TH=h;
        int lshift=0;
        int innerW=w-mLeft-mRight, innerH=h-mBottom-mTop;
        boolean xcat=v[0].isCat(), ycat=v[1].isCat();
        
        ((orientation==0)?ax:ay).setGeometry(Axis.O_X,mLeft,W=innerW);
        ((orientation==0)?ay:ax).setGeometry(Axis.O_Y,h-mBottom,-(H=innerH));
        Y=TH-mBottom-innerH;
        
        hasLeft=hasRight=hasTop=hasBot=false;
        
        pts=v[0].size();
        if (v[1].size()<pts) pts=v[1].size();
        
        sortedPoints = new TreeMap();
        for (int i=0;i<pts;i++) {
            int jx=0, jy=0;
            if (v[0].isCat() && jitter && !stackjitter) {
                double d=Math.random()-0.5; d=Math.tan(d*2.5)/4.0;
                jx=(int)(d*((double)(ax.getCatLow(v[0].getCatIndex(i))-ax.getCasePos(i))));
            }
            if (v[1].isCat() && jitter && !stackjitter) {
                double d=Math.random()-0.5; d=Math.tan(d*2.5)/4.0;
                jy=(int)(d*((double)(ay.getCatLow(v[1].getCatIndex(i))-ay.getCasePos(i))));
            }
            if ((!v[0].isMissingAt(i) || v[0].isCat()) && (!v[1].isMissingAt(i) || v[1].isCat())) {
                int x=jx+ax.getCasePos(i),y=jy+ay.getCasePos(i);
                //pp[i]=null;
                int oX = (orientation==0)?x:y;
                int oY = (orientation==0)?y:x;
                if (oX<mLeft) hasLeft=true;
                else if (oY<mTop) hasTop=true;
                else if (oX>w-mRight) hasRight=true;
                else if (oY>h-mBottom) hasBot=true;
                else {
                    /*if (stackjitter && jitter && v[0].isCat() && i>0) {
                        int j=0;
                        while (j<i) {
                            if (pp[j]!=null && ((PPrimCircle)pp[j]).y==y && ((PPrimCircle)pp[j]).x==x) x+=stackOff;
                            j++;
                        }
                    } else if (stackjitter && jitter && v[1].isCat() && i>0) {
                        int j=0;
                        while (j<i) {
                            if (pp[j]!=null && ((PPrimCircle)pp[j]).y==y && ((PPrimCircle)pp[j]).x==x) y-=stackOff;
                            j++;
                        }
                    }*/
                    PPrimCircle p;
                    if((p=(PPrimCircle)sortedPoints.get(new ComparablePoint(x,y)))!=null){
                        int[] newRef = new int[p.ref.length+1];
                        System.arraycopy(p.ref, 0, newRef, 0, p.ref.length);
                        newRef[p.ref.length] = i;
                        p.ref=newRef;
                    } else{
                        p=new PPrimCircle();
                        if(orientation==0){
                            p.x = x;
                            p.y = y;
                        } else{
                            p.x = y;
                            p.y = x;
                        }
                        p.diam = ptDiam;
                        p.ref = new int[] {i};
                        sortedPoints.put(new ComparablePoint(x,y), p);
                    }
                }
            } else { // place missings on the other side of the axes
                int x,y;
                if (v[0].isMissingAt(i)) x=mLeft-4; else x=jx+ax.getCasePos(i);
                if (v[1].isMissingAt(i)) y=h-mBottom+4; else y=jy+ay.getCasePos(i);
                PPrimCircle p=new PPrimCircle();
                if(orientation==0){
                    p.x = x;
                    p.y = y;
                } else{
                    p.x = y;
                    p.y = x;
                }
                p.diam = ptDiam;
                p.ref = new int[] {i};
            }
        };
        pp = new PlotPrimitive[sortedPoints.values().size()];
        sortedPoints.values().toArray(pp);
    };
    
    public void keyPressed(KeyEvent e) {
        if (Global.DEBUG>0)
            System.out.println("ScatterCanvas: "+e);
        if (e.getKeyCode()==KeyEvent.VK_ALT && !querying) {
            querying=true;
            qx=qy=-1;
            pc.setCursor(Common.cur_aim);
            setUpdateRoot(3); repaint();
        }
        if (e.getKeyCode()==KeyEvent.VK_UP) {
            ptDiam+=2; setUpdateRoot(0);
            for(int i=0; i<pp.length; i++) if(pp[i]!=null) ((PPrimCircle)pp[i]).diam = ptDiam;
            repaint();
        }
        if (e.getKeyCode()==KeyEvent.VK_DOWN && ptDiam>2) {
            ptDiam-=2; setUpdateRoot(0);
            for(int i=0; i<pp.length; i++) if(pp[i]!=null) ((PPrimCircle)pp[i]).diam = ptDiam;
            repaint();
        }
        /*if (stackjitter && e.getKeyCode()==KeyEvent.VK_RIGHT) {
            stackOff++; setUpdateRoot(0); updateObjects(); repaint();
        }
        if (stackjitter && e.getKeyCode()==KeyEvent.VK_LEFT && stackOff>1) {
            stackOff--; setUpdateRoot(0); updateObjects(); repaint();
        }*/
        if (e.getKeyCode()==KeyEvent.VK_RIGHT) {
            run(this,"alphaUp");
        }
        if (e.getKeyCode()==KeyEvent.VK_LEFT) {
            run(this,"alphaDown");
        }
    };
    
    public void keyReleased(KeyEvent e) {
        if (Global.DEBUG>0)
            System.out.println("ScatterCanvas: "+e);
        if (e.getKeyCode()==KeyEvent.VK_ALT) {
            querying=false;
            pc.setCursor(Common.cur_arrow);
            setUpdateRoot(3); repaint();
        }
    };
    
    public Object run(Object o, String cmd) {
        super.run(o,cmd);
        if (cmd=="labels") {
            showLabels=!showLabels;
            MIlabels.setLabel((showLabels)?"Hide labels":"Show labels");
            setUpdateRoot(0);
            repaint();
        };
        if (cmd=="rotate") rotate();
        if (cmd=="points+") {
            ptDiam+=2; setUpdateRoot(0); repaint();
        }
        if (cmd=="points-" && ptDiam>2) {
            ptDiam-=2; setUpdateRoot(0); repaint();
        }
        if (cmd=="equiscale") {
            double sfx,sfy, usfx,usfy;
            sfx=((double)ax.gLen)/ax.vLen; usfx=(sfx<0)?-sfx:sfx;
            sfy=((double)ay.gLen)/ay.vLen; usfy=(sfy<0)?-sfy:sfy;
            if (usfx<usfy) {
                ay.setValueRange(ay.vBegin,ay.vLen*(usfy/usfx));
            } else {
                ax.setValueRange(ax.vBegin,ax.vLen*(usfx/usfy));
            }
            updateObjects();
            setUpdateRoot(0);
            repaint();
        }
        if (cmd=="YrangeDlg" || cmd=="XrangeDlg") {
            Axis axis=(cmd=="YrangeDlg")?ay:ax;
            Dialog d=intDlg=new Dialog(myFrame,(cmd=="YrangeDlg")?"Y range":"X range",true);
            IDlgCL ic=new IDlgCL(this);
            d.setBackground(Color.white);
            d.setLayout(new BorderLayout());
            d.add(new SpacingPanel(),BorderLayout.WEST);
            d.add(new SpacingPanel(),BorderLayout.EAST);
            Panel bp=new Panel(); bp.setLayout(new FlowLayout());
            Button b,b2;
            bp.add(b=new Button("OK"));bp.add(b2=new Button("Cancel"));
            d.add(bp,BorderLayout.SOUTH);
            d.add(new Label(" "),BorderLayout.NORTH);
            Panel cp=new Panel(); cp.setLayout(new FlowLayout());
            d.add(cp);
            cp.add(new Label("start: "));
            TextField tw=new TextField(""+axis.vBegin,6);
            TextField th=new TextField(""+(axis.vBegin+axis.vLen),6);
            cp.add(tw);
            cp.add(new Label(", end: "));
            cp.add(th);
            d.pack();
            b.addActionListener(ic);b2.addActionListener(ic);
            d.setVisible(true);
            if (!cancel) {
                double w=Tools.parseDouble(tw.getText());
                double h=Tools.parseDouble(th.getText());
                axis.setValueRange(w,h-w);
                updateObjects();
                setUpdateRoot(0);
                repaint();
            }
            d.dispose();
        }
        if (cmd=="nextBg") { fieldBg++; if (fieldBg>2) fieldBg=0; setUpdateRoot(0); repaint(); };
        if (cmd=="resetZoom") { resetZoom(); repaint(); }
        if (cmd=="jitter") {
            jitter=!jitter; updateObjects(); setUpdateRoot(1); repaint();
        }
        if (cmd=="stackjitter") {
            if (!jitter) jitter=true;
            stackjitter=!stackjitter; updateObjects(); setUpdateRoot(1); repaint();
        }
        if (cmd=="trigraph") { useX3=!useX3; MItrigraph.setLabel(useX3?"Expand labels":"Shorten labels"); setUpdateRoot(0); repaint(); }
        if (cmd=="alphaDown") {
            ppAlpha-=(ppAlpha>0.2)?0.10:((ppAlpha>0.1)?0.05:((ppAlpha>0.02)?0.01:0.0025)); if (ppAlpha<0.005f) ppAlpha=0.005f;
            setUpdateRoot(0); repaint();
        }
        if (cmd=="alphaUp") {
            ppAlpha+=(ppAlpha>0.2)?0.10:((ppAlpha>0.1)?0.05:((ppAlpha>0.02)?0.01:0.0025)); if (ppAlpha>1f) ppAlpha=1f;
            setUpdateRoot(0); repaint();
        }
        if(M_TRANSHIGHL.equals(cmd)) {
            alphaHighlighting=!alphaHighlighting;
            MItransHighl.setLabel(alphaHighlighting?"Opaque highlighting":"Transparent highlighting");
            setUpdateRoot(1); repaint();
        }
        
        return null;
    }
    
    public void paintBack(PoGraSS g) {
        g.defineColor("objects",Common.objectsColor.getRed(),Common.objectsColor.getGreen(),Common.objectsColor.getBlue());
        g.defineColor("red",255,0,0);
        
        /* determine maximal y label length */
        int maxLabelLength=0;
        {
            int ori = (orientation==0)?1:0;
            Axis axis = (orientation==0)?ay:ax;
            double f=axis.getSensibleTickDistance(30,18);
            double fi=axis.getSensibleTickStart(f);
            try {
                while (fi<axis.vBegin+axis.vLen) {
                    String s;
                    if(v[ori].isCat()) s=v[ori].getCatAt((int)(fi+0.5)).toString();
                    else s=axis.getDisplayableValue(fi);
                    if(s.length()>maxLabelLength) maxLabelLength=s.length();
                    fi+=f;
                }
            } catch (Exception pae) { // catch problems (especially in getCatAt being 0)
            }
        }
        
        int omLeft=mLeft;
        if(maxLabelLength*8>standardMLeft){
            mLeft = maxLabelLength*8+2;
        } else mLeft=standardMLeft;
        if(mLeft!=omLeft) updateObjects();
        
        Dimension Dsize=pc.getSize();
        if (Dsize.width!=TW || Dsize.height!=TH || mLeft!=omLeft)
            updateObjects();
        
        if (TW<50||TH<50) {
            g.setColor("red");
            g.drawLine(0,0,TW,TH);
            g.drawLine(0,TH,TW,0);
            return;
        };
        
        if (fieldBg!=0) {
            g.setColor((fieldBg==1)?C_WHITE:"objects");
            g.fillRect(mLeft,Y,W,H);
        }
        
        g.setColor("black");
        g.drawLine(mLeft,Y,mLeft,Y+H);
        g.drawLine(mLeft,Y+H,mLeft+W,Y+H);
        
        labels.clear();
        /* draw ticks and labels for X axis */
        {
            int ori = (orientation==0)?0:1;
            Axis axis = (orientation==0)?ax:ay;
            double f=axis.getSensibleTickDistance(50,26);
            double fi=axis.getSensibleTickStart(f);
            if (Global.DEBUG>1)
                System.out.println("SP.A[0]:"+axis.toString()+", distance="+f+", start="+fi);
            try {
                while (fi<axis.vBegin+axis.vLen) {
                    int t=axis.getValuePos(fi);
                    g.drawLine(t,Y+H,t,Y+H+5);
                    if (showLabels)
                        labels.add(t,Y+H+20,0.5,0,v[ori].isCat()?((useX3)?Common.getTriGraph(v[ori].getCatAt((int)(fi+0.5)).toString()):
                            v[ori].getCatAt((int)(fi+0.5)).toString()):
                            axis.getDisplayableValue(fi));
                    fi+=f;
                }
            } catch (Exception pae) { // catch problems (especially in getCatAt being 0)
            }
        }
        
        /* draw ticks and labels for Y axis */
        {
            int ori = (orientation==0)?1:0;
            Axis axis = (orientation==0)?ay:ax;
            double f=axis.getSensibleTickDistance(30,18);
            double fi=axis.getSensibleTickStart(f);
            if (Global.DEBUG>1)
                System.out.println("SP.A[1]:"+ay.toString()+", distance="+f+", start="+fi);
            try {
                while (fi<axis.vBegin+axis.vLen) {
                    int t=axis.getValuePos(fi);
                    g.drawLine(mLeft-5,t,mLeft,t);
                    if(showLabels){
                        if(v[ori].isCat())
                            labels.add(mLeft-8,t,1,0.3,mLeft,v[ori].getCatAt((int)(fi+0.5)).toString());
                        else
                            labels.add(mLeft-8,t,1,0.3,axis.getDisplayableValue(fi));
                    }
                    fi+=f;
                }
            } catch (Exception pae) { // catch problems (especially in getCatAt being 0)
            }
        }
        labels.finishAdd();
        
        //nextLayer(g);
        
        if (drag) {
            /* no clipping
            int dx1=A[0].clip(x1),dy1=A[1].clip(y1),
                dx2=A[0].clip(x2),dy2=A[1].clip(y2);
             */ int dx1=x1, dx2=x2, dy1=y1, dy2=y2;
             if (dx1>dx2) { int h=dx1; dx1=dx2; dx2=h; };
             if (dy1>dy2) { int h=dy1; dy1=dy2; dy2=h; };
             if (zoomDrag)
                 g.setColor("aDragBg");
             else
                 g.setColor("aSelBg");
             g.fillRect(dx1,dy1,dx2-dx1,dy2-dy1);
             g.setColor("black");
             g.drawRect(dx1,dy1,dx2-dx1,dy2-dy1);
        };
        
    }
    
    public String queryObject(int i) {
        return queryObject(pp[i]);
    }
    
    public String queryObject(PlotPrimitive p) {
        PPrimCircle ppc = (PPrimCircle)p;
        String qs = "";
        boolean actionExtQuery = isExtQuery;
        if(actionExtQuery) {
        	if(ppc.ref.length==1){
        		qs = v[0].getName() + ": " + v[0].atD(ppc.ref[0]) + "\n"
                    + v[1].getName() + ": " + v[1].atD(ppc.ref[0]) + "\n"
                    + ppc.ref.length + " case(s) ("+
					Tools.getDisplayableValue(100.0*((double)ppc.ref.length) / (double)v[0].size(),3)+
					"% of var, "+
					Tools.getDisplayableValue(100.0*((double)ppc.ref.length) / (double)(v[0].size()+v[1].size()),3)+
					"% of total)";
        	} else{
        		double[] mM0 = minMax(ppc.ref,0);
        		double[] mM1 = minMax(ppc.ref,1);
        		qs =  v[0].getName() + ": min " + mM0[0] + ", max " + mM0[1] + "\n"
                    + v[1].getName() + ": min " + mM1[0] + ", max " + mM1[1] + "\n"
                    + ppc.ref.length + " case(s) ("+
					Tools.getDisplayableValue(100.0*((double)ppc.ref.length) / (double)v[0].size(),3)+
					"% of var, "+
					Tools.getDisplayableValue(100.0*((double)ppc.ref.length) / (double)(v[0].size()+v[1].size()),3)+
					"% of total)";
        	}
        }
        else {
        	if(ppc.ref.length==1){
        		qs = v[0].getName() + ": " + v[0].atD(ppc.ref[0]) + "\n"
                    + v[1].getName() + ": " + v[1].atD(ppc.ref[0]) + "\n"
                    + ppc.ref.length + " case(s)";
        	} else{
        		double[] mM0 = minMax(ppc.ref,0);
        		double[] mM1 = minMax(ppc.ref,1);
        		qs =  v[0].getName() + ": min " + mM0[0] + ", max " + mM0[1] + "\n"
                    + v[1].getName() + ": min " + mM1[0] + ", max " + mM1[1] + "\n"
                    + ppc.ref.length + " case(s)";
        	}
        }
        
        return qs;
    }
    
    /* TODO: Maybe this can be done faster with the sortedPoints map */
    private double[] minMax(int[] ref, int var){
        double mM[] = new double[2];
        mM[0] = mM[1] = v[var].atD(ref[0]);
        for(int i=1; i<ref.length; i++){
            double atD=v[var].atD(ref[i]);
            if(atD<mM[0]) mM[0]=atD;
            if(atD>mM[1]) mM[1]=atD;
        }
        return mM;
    }
    
    public void paintPost(PoGraSS g) {
        if (querying) {
            g.setColor("black");
            if((orientation&1) == 0){ // no rotation or 180°
                if (qx==ax.clip(qx) && qy==ay.clip(qy)) {
                    g.drawLine(ax.gBegin,qy,ax.gBegin+ax.gLen,qy);
                    g.drawLine(qx,ay.gBegin,qx,ay.gBegin+ay.gLen);
                    g.drawString(ay.getDisplayableValue(ax.getValueForPos(qx)),qx+2,qy-2);
                    g.drawString(ay.getDisplayableValue(ay.getValueForPos(qy)),qx+2,qy+11);
                }
            } else {
                if (qx==ay.clip(qx) && qy==ax.clip(qy)) {
                    g.drawLine(qx,ax.gBegin,qx,ax.gBegin+ax.gLen);
                    g.drawLine(ay.gBegin,qy,ay.gBegin+ay.gLen,qy);
                    g.drawString(ax.getDisplayableValue(ay.getValueForPos(qx)),qx+2,qy-2);
                    g.drawString(ax.getDisplayableValue(ax.getValueForPos(qy)),qx+2,qy+11);
                }
            }
        }
        super.paintPost(g);
    }
    
    /* TODO: at the moment one has to hit the center point exactly which makes
     * things easier and faster here but more difficult for the user, so this
     * should be changed in the future.
     */
    protected PlotPrimitive getFirstPrimitiveContaining(int x, int y) {
        return (PlotPrimitive)sortedPoints.get(new ComparablePoint(x,y));
    }
    
    protected PlotPrimitive[] getPrimitivesContaining(int x, int y) {
        PlotPrimitive[] pps=getPrimitivesIntersecting(new Rectangle(x-ptDiam/2,y-ptDiam/2, ptDiam,ptDiam));
        for(int i=0; i<pps.length; i++){
            final PPrimCircle ppc = (PPrimCircle)pps[i];
            final int px = ppc.x-x;
            final int py = ppc.y-y;
            if(px*px+py*py > ptDiam*ptDiam/4) pps[i]=null;
        }
        return pps;
    }
    
    protected PlotPrimitive[] getPrimitivesIntersecting(Rectangle rec) {
        int x=(orientation==0)?rec.x:rec.y;
        int y=(orientation==0)?rec.y:rec.x;
        int w=(orientation==0)?rec.width:rec.height;
        int h=(orientation==0)?rec.height:rec.width;
        Object[] obj = sortedPoints.subMap(new ComparablePoint(x, y), new ComparablePoint(x+w, y+h)).values().toArray();
        PPrimCircle[] plp = new PPrimCircle[obj.length];
        int j=0;
        PPrimCircle ppc;
        for(int i=0; i< obj.length; i++) {
            ppc=(PPrimCircle)obj[i];
            if(orientation==0){
                if(ppc!=null && ppc.y>=rec.y && ppc.y<=rec.y+rec.height) plp[j++]=ppc;
            } else{
                if(ppc!=null && ppc.x>=rec.x && ppc.x<=rec.x+rec.width) plp[j++]=ppc;
            }
            
        }
        PlotPrimitive[] ret = new PlotPrimitive[j];
        System.arraycopy(plp,0, ret,0, j);
        return ret;
    }
};
