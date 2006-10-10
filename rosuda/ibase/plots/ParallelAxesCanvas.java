package org.rosuda.ibase.plots;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.ibase.toolkit.PGSCanvas.*;
import org.rosuda.pograss.PoGraSS;
import org.rosuda.util.RespDialog;
import org.rosuda.util.SpacingPanel;
import org.rosuda.util.Tools;

public class ParallelAxesCanvas extends BaseCanvas {
    
    private boolean valid=true;
    
    public static final int TYPE_PCP=0;
    public static final int TYPE_BOX=1;
    public static final int TYPE_PCPBOX=2;
    private int type;
    
    /**
     * width, height and margins fields
     */
    private int MINWIDTH=60;
    private int MINHEIGHT=50;
    
    private int bigMLeft=30;
    private int bigMTop=20;
    private int bigMBottom=20;
    private int bigMRight=30;
    private int smallMLeft=10;
    private int smallMTop=10;
    private int smallMBottom=10;
    private int smallMRight=10;
    
    private int defaultMLeft;
    private int defaultMRight;
    private int defaultMTop;
    private int defaultMBottom;
    
    /**
     * axes and labels fields
     */
    private boolean drawAxes=false;
    private boolean commonScale=false;
    private boolean useRegularPositioning=false;
    private int leftGap=7;
    private int rightGap=7;
    public boolean alterningLabels = true;
    
    /**
     * variables fields
     */
    
    /** y variables */
    private SVar v[];
    /** x variable */
    private SVar xv;
    /** categorical variable */
    private SVar cv;
    private double totMin;
    private double totMax;
    
    /**
     * menu and command fields
     */
    private static final String M_MINUS = "-";
    private static final String M_LABELS = "labels";
    private static final String M_SHOWDOTS = "Show dots";
    private static final String M_TOGGLEPTS = "togglePts";
    private static final String M_NODESIZEUP = "nodeSizeUp";
    private static final String M_NODESIZEDOWN = "nodeSizeDown";
    private static final String M_SHOWAXES = "Show axes";
    private static final String M_TOGGLEAXES = "toggleAxes";
    private static final String M_HIDELINES = "Hide lines";
    private static final String M_TOGGLELINES = "toggleLines";
    private static final String M_HIDENALINES = "hideNAlines";
    private static final String M_COMMON = "common";
    private static final String M_YRANGEDLG = "YrangeDlg";
    private static final String M_SCALEDLG = "scaleDlg";
    private static final String M_PCP = "only pcp";
    private static final String M_BOX = "only box";
    private static final String M_BOTHPCPBOX = "both pcp and box";
    private static final String M_SORTBYCOUNT = "sortByCount";
    private static final String M_SORTBYMARKED = "sortByMarked";
    private static final String M_SORTBYMARKEDREL = "sortByMarkedRelative";
    private static final String M_SORTBYMEDIAN = "sortByMedian";
    private static final String M_SORTBYMAX = "sortByMax";
    private static final String M_SORTBYMIN = "sortByMin";
    private static final String M_SORTBYMARKEDMEDIAN = "sortByMarkedMedian";
    private static final String M_SORTBYMARKEDMAX = "sortByMarkedMax";
    private static final String M_SORTBYMARKEDMIN = "sortByMarkedMin";
    private static final String M_ALTERNINGLABELS = "alterningLabels";
    
    private MenuItem MIlabels=null;
    private MenuItem MIdots=null;
    private MenuItem MIaxes=null;
    private MenuItem MIlines=null;
    private MenuItem MInodeSizeUp=null;
    private MenuItem MInodeSizeDown=null;
    private MenuItem MIhideNAlines=null;
    private MenuItem MIPCP=null;
    private MenuItem MIBox=null;
    private MenuItem MIPCPBox=null;
    private MenuItem MIsortByCount=null;
    private MenuItem MIsortByMarked=null;
    private MenuItem MIsortByMarkedRel=null;
    private MenuItem MIsortByMedian=null;
    private MenuItem MIsortByMax=null;
    private MenuItem MIsortByMin=null;
    private MenuItem MIsortByMarkedMedian=null;
    private MenuItem MIsortByMarkedMax=null;
    private MenuItem MIsortByMarkedMin=null;
    private MenuItem MIAlterningLabels=null;
    
    /**
     * Box plot specific fields
     */
    private int boxwidth=20;
    private int posBoxwidth=20; // possible boxwidth. used to determine maximal label length
    private final int MAX_BOXWIDTH=32;
    private final int MIN_BOXWIDTH=4;
    // invisible Points to allow selection of box plots
    private ArrayList invisiblePoints=null;
    // list of boxes
    private ArrayList boxes=null;
    /** if <code>true</code> then side-by-side boxplots grouped by {@link #cv} are drawn,
     * otherwise draw just a single boxpolot */
    private boolean vsCat=false;
    
    // for vsCat version
    private int rk[][];
    private int rs[];
    private int cs;
    private Object cats[];
    private OrdStats oss[];
    
    // for plain version
    private OrdStats OSdata;
    
    // Array mapping each PPrimBox to the OrdStats object which contains its selections
    private OrdStats markStats[];
    
    /**
     * PCP specific fields
     */
    private boolean drawPoints=false;
    private boolean drawLines=true;
    private boolean drawNAlines=true;
    private boolean drawHidden=true;
    
    private ArrayList polylines=null;
    
    private int nodeSize=2;
    
    public Color COL_AXES=Color.WHITE;
    
    public boolean isMouseOnHilite=false;
    
    /** create a boxplot canvas for a multiple grouped boxplots side-by-side
     * @param f associated frame (or <code>null</code> if none)
     * @param var source numerical variable
     * @param cvar categorical variable for grouping
     * @param mark associated marker */
    public ParallelAxesCanvas(final int gd, final Frame f, final SVar var, final SVar cvar, final SMarker mark, final int type) {
        super(gd,f,mark);
        
        this.type=type;
        
        initFlagsAndFields();
        
        allowDragMove=true;
        objectClipping=true;
        commonScale=false;
        
        setDefaultMargins(new int[] {smallMLeft,smallMRight,smallMTop,bigMBottom, bigMLeft,smallMRight,smallMTop,smallMBottom});
        
        mBottom=smallMBottom;
        mTop=smallMTop;
        mLeft=smallMLeft;
        mRight=smallMRight;
        
        v = new SVar[]{var};
        cv = cvar;
        
        resetAxesCoord();
        
        xv=new SVarObj(getShortClassName() + ".index",true);
        for(int i=0; i<cv.getNumCats(); i++){
            xv.add(cv.getCatAt(i).toString());
        }
        ax=new Axis(xv,Axis.O_X,xv.isCat()?Axis.T_EqCat:Axis.T_Num); ax.addDepend(this);
        ay=new Axis(v[0],Axis.O_Y,Axis.T_Num); ay.addDepend(this);
        // get some space around (this comes from the scatterplots)
        ay.setValueRange(v[0].getMin()-(v[0].getMax()-v[0].getMin())/20,(v[0].getMax()-v[0].getMin())*1.1);
        
        vsCat=true;
        createMenu(f);
        MIPCP.setEnabled(false);
        MIPCPBox.setEnabled(false);
        setCommonScale(commonScale);
        EzMenu.getItem(getFrame(),M_COMMON).setEnabled(false);
        
        updateMargins();
        
        setTitle("Boxplot ("+v[0].getName()+" grouped by "+cv.getName()+")");
        
        if (var!=null && !var.isCat() && var.isNum() && cvar.isCat())
            valid=true; // valid are only numerical vars non-cat'd, cvar is cat
        if (valid) { // split into ranked chunks by cat.
            cs=cv.getNumCats();
            cats=cv.getCategories();
            final int[] r=v[0].getRanked();
            oss=new OrdStats[cs*2+2];
            rk=new int[cs*2+2][];
            rs=new int[cs*2+2];
            int i=0;
            while (i<cs) {
                rs[i]=0;
                final int j=cv.getSizeCatAt(i);
                rk[i]=new int[j];
                rk[cs+1+i]=new int[j];
                oss[i]=new OrdStats();
                oss[cs+1+i]=new OrdStats();
                i++;
            }
            i=0;
            while(i<r.length) {
                int x=cv.getCatIndex(cv.at(r[i]));
                if (x<0) x=cs;
                rk[x][rs[x]]=r[i];
                rs[x]++;
                i++;
            }
            i=0;
            while(i<cs) {
                oss[i].update(v[0],rk[i],rs[i]);
                i++;
            }
            final boolean[] validOss = new boolean[cs];
            int invalid=0;
            for(i=0; i<cs; i++){
                if(oss[i].lastR==null){
                    validOss[i]=false;
                    invalid++;
                } else validOss[i]=true;
            }
            if(invalid>0){
                final OrdStats[] newOss = new OrdStats[2*(cs-invalid)+2];
                int j=0;
                for(i=0;i<cs; i++){
                    if(validOss[i]) newOss[j++]=oss[i];
                }
                newOss[cs]=oss[cs];
                j=0;
                for(i=0;i<cs; i++){
                    if(validOss[i]) newOss[cs-invalid+1+j++]=oss[cs+1+i];
                }
                oss=newOss;
                cs-=invalid;
            }
            updateObjects();
        }
        objectClipping=true;
        dontPaint=false;
    }
    
    /**
     * @param f associated frame (or <code>null</code> if none)
     * @param var source variable
     * @param mark associated marker */
    public ParallelAxesCanvas(final int gd, final Frame f, final SVar var, final SMarker mark, final int type) {
        this(gd,f,new SVar[]{var},mark,type);
    }
    
    /** basic constructor. Every subclass must call this constructor
     * @param f frame owning this canvas. since BaseCanvas itself doesn't modify any attribute of the frame except for title it is possible to put more canvases into one frame. This doesn't have to hold for subclasses, especially those providing their own menus.
     * @param mark marker which will be used for selection/linked highlighting
     */
    public ParallelAxesCanvas(final int gd, final Frame f, final SVar[] yvs, final SMarker mark, final int type) {
        super(gd, f, mark);
        
        this.type=type;
        initFlagsAndFields();
        
        allowDragMove=true;
        objectClipping=true;
        if(yvs.length==1) commonScale=true;
        
        if(type==TYPE_BOX || type==TYPE_PCPBOX) setDefaultMargins(new int[] {smallMLeft,smallMRight,smallMTop,smallMBottom});
        else setDefaultMargins(new int[] {smallMLeft,smallMRight,bigMTop,bigMBottom, bigMLeft,smallMRight,smallMTop,smallMBottom});
        
        v=new SVar[yvs.length];
        opAy=new Axis[yvs.length-1];
        
        xv=new SVarObj(getShortClassName() + ".index",true);
        int i = 0;
        while(i<yvs.length) {
            if (yvs[i].isNum()) {
                if (i==0) {
                    totMin=yvs[i].getMin(); totMax=yvs[i].getMax();
                } else {
                    if (yvs[i].getMin()<totMin) totMin=yvs[i].getMin();
                    if (yvs[i].getMax()>totMax) totMax=yvs[i].getMax();
                }
            }
            xv.add(yvs[i].getName());
            v[i]=yvs[i]; i++;
        }
        ax=new Axis(xv,Axis.O_X,xv.isCat()?Axis.T_EqCat:Axis.T_Num); ax.addDepend(this);
        ay=new Axis(yvs[0],Axis.O_Y,yvs[0].isCat()?Axis.T_EqCat:Axis.T_Num); ay.addDepend(this);
        
        createMenu(f);
        if(v.length==1) {
            MIPCPBox.setEnabled(false);
            MIPCP.setEnabled(false);
        }
        setCommonScale(commonScale);
        updateMargins();
        
        if(type==TYPE_BOX || type==TYPE_PCPBOX) initOss(yvs);
        
        dontPaint=false;
    }
    
    private void createMenu(final Frame f){
        createMenu(f,true,true,true,false,new String[]{
            "@LHide Labels",M_LABELS,
            "Alternating Labels",M_ALTERNINGLABELS,
            M_SHOWDOTS,M_TOGGLEPTS,
            "Increase Dot Size (up)",M_NODESIZEUP,
            "Decrease Dot Size (down)",M_NODESIZEDOWN,
            M_SHOWAXES,M_TOGGLEAXES,
            M_HIDELINES,M_TOGGLELINES,
            "@NHide NA Lines",M_HIDENALINES,
            M_MINUS,
            "@TCommon Scale",M_COMMON,
            M_MINUS,
            "Set Y Range ...",M_YRANGEDLG,
            "!SShow Scale Dialog",M_SCALEDLG,
            M_MINUS,
            "PCP",M_PCP,
            "Box Plot",M_BOX,
            "PCP Over Boxes",M_BOTHPCPBOX,
            M_MINUS,
            "@OSort by Count",M_SORTBYCOUNT,
            "!OSort by Highlighted",M_SORTBYMARKED,
            "Sort by Highlighted Proportion",M_SORTBYMARKEDREL,
            "@ESort by Median",M_SORTBYMEDIAN,
            "@MSort by Minimum",M_SORTBYMIN,
            "!MSort by maximum",M_SORTBYMAX,
            "Sort by Median of Highlighted",M_SORTBYMARKEDMEDIAN,
            "Sort by Minimum of Highlighted",M_SORTBYMARKEDMIN,
            "Sort by Maximum of Highlighted",M_SORTBYMARKEDMAX
        });
        
        MIlabels=EzMenu.getItem(f,M_LABELS);
        MIdots=EzMenu.getItem(f,M_TOGGLEPTS);
        MIaxes=EzMenu.getItem(f,M_TOGGLEAXES);
        MIlines=EzMenu.getItem(f,M_TOGGLELINES);
        MIlines.setEnabled(false);
        MInodeSizeUp=EzMenu.getItem(f, M_NODESIZEUP);
        MInodeSizeUp.setEnabled(false);
        MInodeSizeDown=EzMenu.getItem(f, M_NODESIZEDOWN);
        MInodeSizeUp.setEnabled(false);
        MIhideNAlines=EzMenu.getItem(f,M_HIDENALINES);
        MIhideNAlines.setEnabled(type==TYPE_PCP);
        MItransHighl=EzMenu.getItem(f,M_TRANSHIGHL);
        MIPCP=EzMenu.getItem(f,M_PCP);
        MIBox=EzMenu.getItem(f,M_BOX);
        MIPCPBox=EzMenu.getItem(f,M_BOTHPCPBOX);
        MIsortByCount=EzMenu.getItem(f,M_SORTBYCOUNT);
        MIsortByMarked=EzMenu.getItem(f,M_SORTBYMARKED);
        MIsortByMarkedRel=EzMenu.getItem(f,M_SORTBYMARKEDREL);
        MIsortByMedian=EzMenu.getItem(f,M_SORTBYMEDIAN);
        MIsortByMax=EzMenu.getItem(f,M_SORTBYMAX);
        MIsortByMin=EzMenu.getItem(f,M_SORTBYMIN);
        MIsortByMarkedMedian=EzMenu.getItem(f,M_SORTBYMARKEDMEDIAN);
        MIsortByMarkedMax=EzMenu.getItem(f,M_SORTBYMARKEDMAX);
        MIsortByMarkedMin=EzMenu.getItem(f,M_SORTBYMARKEDMIN);
        MIAlterningLabels=EzMenu.getItem(f,M_ALTERNINGLABELS);
        
        updateSortingMenus();
    }
    
    public void keyPressed(final KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_UP) run(this, M_NODESIZEUP);
        if (e.getKeyCode()==KeyEvent.VK_DOWN) run(this,M_NODESIZEDOWN);
        super.keyPressed(e);
    }
    
    public Object run(final Object o, final String cmd) {
        if ("print".equals(cmd)) { drawHidden=false; run(o,"exportPS"); drawHidden=true; return this; }
        super.run(o,cmd);
        if (M_LABELS.equals(cmd)) {
            setShowLabels(!isShowLabels());
            MIlabels.setLabel((isShowLabels())?"Hide Labels":"Show Labels");
            setUpdateRoot(0);
            repaint();
        }
        if ("exit".equals(cmd)) WinTracker.current.Exit();
        if (M_COMMON.equals(cmd)) { setCommonScale(!commonScale); updateObjects(); setUpdateRoot(0); repaint();}
        if (M_TOGGLEPTS.equals(cmd)) {
            drawPoints=!drawPoints;
            MIdots.setLabel((drawPoints)?"Hide Dots":M_SHOWDOTS);
            for(Iterator it = polylines.iterator(); it.hasNext();){
                ((PPrimPolygon)it.next()).drawCorners=drawPoints;
            }
            MIdots.setEnabled(!drawPoints||drawLines);
            MIlines.setEnabled(drawPoints||!drawLines);
            MInodeSizeDown.setEnabled(drawPoints);
            MInodeSizeUp.setEnabled(drawPoints);
            setUpdateRoot(0);
            repaint();
        }
        if (M_TOGGLELINES.equals(cmd)) {
            drawLines=!drawLines;
            MIlines.setLabel((drawLines)?M_HIDELINES:"Show Lines");
            for(Iterator it = polylines.iterator(); it.hasNext();){
                final PPrimPolygon ppp = ((PPrimPolygon)it.next());
                ppp.drawBorder=drawLines;
                ppp.selectByCorners=!drawLines;
            }
            MIdots.setEnabled(!drawPoints||drawLines);
            MIlines.setEnabled(drawPoints||!drawLines);
            setUpdateRoot(0);
            repaint();
        }
        if (M_TOGGLEAXES.equals(cmd)) {
            drawAxes=!drawAxes;
            MIaxes.setLabel((drawAxes)?"Hide Axes":M_SHOWAXES);
            setUpdateRoot(0); repaint();
        }
        if (M_YRANGEDLG.equals(cmd) || "XrangeDlg".equals(cmd)) {
            final Axis rt=(M_YRANGEDLG.equals(cmd))?ay:ax;
            final Dialog d=intDlg=new Dialog(myFrame,(rt==ay)?"Y Range":"X Range",true);
            
            d.setBackground(Color.white);
            d.setLayout(new BorderLayout());
            d.add(new SpacingPanel(),BorderLayout.WEST);
            d.add(new SpacingPanel(),BorderLayout.EAST);
            final Panel bp=new Panel(); bp.setLayout(new FlowLayout());
            final Button b;
            bp.add(b=new Button("OK"));final Button b2;
            bp.add(b2=new Button("Cancel"));
            d.add(bp,BorderLayout.SOUTH);
            d.add(new Label(" "),BorderLayout.NORTH);
            final Panel cp=new Panel(); cp.setLayout(new FlowLayout());
            d.add(cp);
            cp.add(new Label("start: "));
            final TextField tw=new TextField(""+rt.vBegin,6);
            final TextField th=new TextField(""+(rt.vBegin+rt.vLen),6);
            cp.add(tw);
            cp.add(new Label(", end: "));
            cp.add(th);
            d.pack();
            final IDlgCL ic = new IDlgCL(this);
            b.addActionListener(ic);b2.addActionListener(ic);
            d.setVisible(true);
            if (!cancel) {
                final double w=Tools.parseDouble(tw.getText());
                final double h=Tools.parseDouble(th.getText());
                rt.setValueRange(w,h-w);
                setUpdateRoot(0);
                repaint();
            }
            d.dispose();
            updateGeometry=true;
        }
        if (M_SCALEDLG.equals(cmd) && commonScale) {
            final RespDialog d=new RespDialog(myFrame,"Set Y Scale",true,RespDialog.okCancel);
            final Panel cp=d.getContentPanel();
            cp.add(new Label("begin: "));
            final TextField tw=new TextField(""+ay.vBegin,6);
            final TextField th=new TextField(""+(ay.vBegin+ay.vLen),6);
            cp.add(tw);
            cp.add(new Label(", end: "));
            cp.add(th);
            d.pack();
            d.setVisible(true);
            if (!cancel) {
                final double vb=Tools.parseDouble(tw.getText());
                final double ve=Tools.parseDouble(th.getText());
                if (ve-vb>0) ay.setValueRange(vb,ve-vb);
                if (myFrame!=null) myFrame.pack();
            }
            d.dispose();
            updateGeometry=true;
        }
        if (M_NODESIZEUP.equals(cmd)) {
            nodeSize++;
            if(polylines!=null && polylines.size()>0){
                for(Iterator it = polylines.iterator(); it.hasNext();){
                    ((PPrimPolygon)it.next()).setNodeSize(nodeSize);
                }
                setUpdateRoot(0); repaint();
            }
        }
        if (M_NODESIZEDOWN.equals(cmd)) {
            nodeSize--;
            if(polylines!=null && polylines.size()>0){
                for(Iterator it = polylines.iterator(); it.hasNext();){
                    ((PPrimPolygon)it.next()).setNodeSize(nodeSize);
                }
                setUpdateRoot(0); repaint();
            }
        }
        if (M_HIDENALINES.equals(cmd)){
            drawNAlines=!drawNAlines;
            if(polylines!=null && polylines.size()>0){
                for(Iterator it = polylines.iterator(); it.hasNext();){
                    ((PPrimPolygon)it.next()).showInvisibleLines = drawNAlines;
                }
                setUpdateRoot(0); repaint();
            }
            MIhideNAlines.setLabel(drawNAlines?"Hide NA Lines":"Show NA Lines");
        }
        if(M_PCP.equals(cmd)) {
            type=TYPE_PCP;
            initFlagsAndFields();
            updateMargins();
            updateObjects();
            updateSortingMenus();
            setUpdateRoot(0); repaint();
        }
        if(M_BOX.equals(cmd)) {
            type=TYPE_BOX;
            initFlagsAndFields();
            updateMargins();
            if(oss==null) initOss(v);
            updateObjects();
            updateSortingMenus();
            setUpdateRoot(0); repaint();
        }
        if(M_BOTHPCPBOX.equals(cmd)) {
            type=TYPE_PCPBOX;
            initFlagsAndFields();
            updateMargins();
            if(oss==null) initOss(v);
            updateObjects();
            updateSortingMenus();
            setUpdateRoot(0); repaint();
        }
        if(M_SORTBYCOUNT.equals(cmd)) {
            final int axes = pp.length - invisiblePoints.size();
            final int[] count = new int[axes];
            for (int i=0; i<axes; i++){
                count[i] = getCount(i);
            }
            sortAxesBy(count);
        }
        if(M_SORTBYMARKED.equals(cmd)) {
            final int axes = pp.length - invisiblePoints.size();
            final int[] marked = new int[axes];
            for (int i=0; i<axes; i++){
                marked[i] = getMarked(i);
            }
            sortAxesBy(marked);
        }
        if(M_SORTBYMARKEDREL.equals(cmd)) {
            final int axes = pp.length - invisiblePoints.size();
            final int[] markedrel = new int[axes];
            for (int i=0; i<axes; i++){
                markedrel[i] = getMarked(i)/getCount(i);
            }
            sortAxesBy(markedrel);
        }
        if(M_SORTBYMEDIAN.equals(cmd)) {
            double[] medians = new double[pp.length - invisiblePoints.size()];
            
            for(int i=0; i<medians.length; i++){
                medians[i] = ((PPrimBox)pp[i]).medValue;
            }
            
            sortAxesBy(medians);
        }
        if(M_SORTBYMIN.equals(cmd)) {
            double[] mins = new double[pp.length - invisiblePoints.size()];
            
            for(int i=0; i<mins.length; i++){
                mins[i] = v[i].getMin();
            }
            
            sortAxesBy(mins);
        }
        if(M_SORTBYMAX.equals(cmd)) {
            double[] maxs = new double[pp.length - invisiblePoints.size()];
            
            for(int i=0; i<maxs.length; i++){
                maxs[i] = v[i].getMax();
            }
            
            sortAxesBy(maxs);
        }
        if(M_SORTBYMARKEDMEDIAN.equals(cmd)) {
            double[] medians = new double[pp.length - invisiblePoints.size()];
            
            for(int i=0; i<medians.length; i++){
                medians[i] = markStats[i].med;
            }
            
            sortAxesBy(medians);
        }
        if(M_SORTBYMARKEDMIN.equals(cmd)) {
            double[] mins = new double[pp.length - invisiblePoints.size()];
            
            for(int i=0; i<mins.length; i++){
                mins[i] = ((PPrimBox)pp[i]).sminValue;
            }
            
            sortAxesBy(mins);
        }
        if(M_SORTBYMARKEDMAX.equals(cmd)) {
            double[] maxs = new double[pp.length - invisiblePoints.size()];
            
            for(int i=0; i<maxs.length; i++){
                maxs[i] = ((PPrimBox)pp[i]).smaxValue;
            }
            
            sortAxesBy(maxs);
        }
        if(M_ALTERNINGLABELS.equals(cmd)){
            MIAlterningLabels.setLabel(alterningLabels?"Alternating Labels":"Bottom Labels");
            alterningLabels = !alterningLabels;
            repaint();
        }
        
        return null;
    }
    
    public SVar getData(final int id) {
        switch(type){
            case TYPE_BOX:
                if(cv!=null) return (id==0)?v[0]:((id==1)?cv:null);
            default:
                return (id>=0 && id<v.length)?v[id]:null;
        }
    }
    
    public void performZoomIn(final int x1, final int y1, final int x2, final int y2) {
        if(commonScale) super.performZoomIn(x1, y1, x2, y2, null,ay);
        else{
            int minZoomAxis=0;
            int maxZoomAxis=v.length-1;
            
            while(getAxCatPos(ax.getCatAtSeqIndex(minZoomAxis)) < x1) minZoomAxis++;
            while(getAxCatPos(ax.getCatAtSeqIndex(maxZoomAxis)) > x2) maxZoomAxis--;
            
            dontPaint=true;
            for(int i=minZoomAxis; i<=maxZoomAxis; i++){
                final int csi=ax.getCatAtSeqIndex(i);
                super.performZoomIn(x1, y1, x2, y2, null, (csi==0)?ay:opAy[csi-1]);
            }
            dontPaint=false;
        }
    }
    
    public void resetZoom() {
        if(commonScale) super.resetZoom();
        else{
            // this regenerates the y axes instead of resetting the ranges... quick and dirty...
            int i=0;
            while (i<opAy.length) {
                opAy[i]=new Axis(v[i+1],Axis.O_Y,v[i+1].isCat()?Axis.T_EqCat:Axis.T_Num);
                opAy[i].addDepend(this);
                i++;
            }
            
            updateGeometry=true;
            ay.setDefaultRange();
            updateObjects();
            setUpdateRoot(0); repaint();
        }
    }
    
    private void setCommonScale(final boolean cs) {
        //if(cs==commonScale) return;
        commonScale=cs;
        updateGeometry=true;
        updateMargins();
        EzMenu.getItem(getFrame(),M_COMMON).setLabel(cs?"Individual Scales":"Common Scale");
        EzMenu.getItem(getFrame(),M_YRANGEDLG).setEnabled(cs);
        if (cs) {
            ay.setType(Axis.T_Num);
            ay.setValueRange(totMin-(totMax-totMin)/20,(totMax-totMin)*1.1);
        } else{
            if (opAy!=null && opAy.length>0 && opAy[0]==null) {
                
                int i=0;
                while (i<opAy.length) {
                    opAy[i]=new Axis(v[i+1],Axis.O_Y,v[i+1].isCat()?Axis.T_EqCat:Axis.T_Num);
                    opAy[i].addDepend(this);
                    opAy[i].setValueRange(v[i+1].getMin()-(v[i+1].getMax()-v[i+1].getMin())/20,(v[i+1].getMax()-v[i+1].getMin())*1.1);
                    i++;
                }
                
                
            }
            ay.setType(v[0].isCat()?Axis.T_EqCat:Axis.T_Num);
            ay.setValueRange(v[0].getMin()-(v[0].getMax()-v[0].getMin())/20,(v[0].getMax()-v[0].getMin())*1.1);
        }
        
    }
    
    private String getShortClassName(){
        switch(type){
            case TYPE_BOX:
                return "Box";
            case TYPE_PCP:
                return "PCP";
        }
        return "PA";
    }
    
    public void paintBack(final PoGraSS g) {
        if (drawAxes) {
            g.setColor(COL_AXES);
            g.setLineWidth(1.5f);
            int xx=0;
            while (xx<xv.getNumCats()) {
                final int t=getAxCatPos(xx++);
                if(orientation==0)
                    g.drawLine(t,mTop,t,getSize().height-mBottom);
                else
                    g.drawLine(mLeft,t,getSize().width-mRight,t);
            }
            g.setLineWidth(1.0f);
        }
        
        final Rectangle r=getBounds();
        g.setBounds(r.width,r.height);
        
        if(!getValid()){
            final int h=getHeight();
            final int w=getWidth();
            g.setColor(COL_INVALID);
            g.drawLine(0,0,w,h);
            g.drawLine(0,w,h,0);
            return;
        }
        
        g.setColor(COL_OUTLINE);
        startAddingLabels();
        addLabelsAndTicks(g);
        endAddingLabels();
    }
    
    private boolean getValid() {
        return valid && getWidth()>=MINWIDTH && getHeight()>=MINHEIGHT;
    }
    
    private void addLabelsAndTicks(final PoGraSS g) {
        /* draw labels for X axis */
        if(type==TYPE_PCP || (vsCat || v.length>1)){
            if(isShowLabels()){
                final int numCats=xv.getNumCats();
                final int[] valuePoss = new int[numCats];
                final String[] labs = new String[numCats];
                for(int i=0; i<numCats; i++){
                    if((type==TYPE_BOX || type==TYPE_PCPBOX) && ((PPrimBase)pp[i]).isDragging())
                        valuePoss[ax.getCatSeqIndex(i)] = ((PPrimBox)pp[i]).x + boxwidth/2;
                    else
                        valuePoss[ax.getCatSeqIndex(i)] = getAxCatPos(i);
                    labs[ax.getCatSeqIndex(i)] = (String)ax.getVariable().getCatAt(i);
                }
                
                for(int i=0; i<numCats; i++) {
                    if (labs[i]!=null){
                        
                        if(orientation==0){
                            final boolean bottom = alterningLabels?((i&1)==0):true;
                            int maxWidth=-1;
                            double xAlign=0.5;
                            double yAlign=0.5;
                            
                            // determine maxWidth, xAlign and yAlign;
                            switch(type){
                                case TYPE_BOX:
                                    if(alterningLabels){
                                        if(valuePoss.length>1){
                                            final int sup,sub;
                                            if(i==0){
                                                if(type==TYPE_BOX && ((PPrimBase)pp[1]).isDragging()){
                                                    if(valuePoss.length>2) sup = (valuePoss[2]-valuePoss[0])/2;
                                                    else sup = getBounds().width-mRight;
                                                } else{
                                                    sup = valuePoss[1];
                                                }
                                                sub = valuePoss[0]-posBoxwidth/2;
                                            } else if (i==valuePoss.length-1){
                                                sup = valuePoss[i]+posBoxwidth/2;
                                                if(type==TYPE_BOX && ((PPrimBase)pp[i-1]).isDragging()){
                                                    if(i>1) sub = (valuePoss[i]-valuePoss[i-2])/2;
                                                    else sub = mLeft;
                                                } else{
                                                    sub = valuePoss[i-1];
                                                }
                                            } else{
                                                if(type==TYPE_BOX && ((PPrimBase)pp[i+1]).isDragging()){
                                                    if(valuePoss.length>i+2) sup = (valuePoss[i+2]-valuePoss[i])/2;
                                                    else sup = getBounds().width-mRight;
                                                } else{
                                                    sup = valuePoss[i+1];
                                                }
                                                if(type==TYPE_BOX && ((PPrimBase)pp[i-1]).isDragging()){
                                                    if(i>1) sub = (valuePoss[i]-valuePoss[i-2])/2;
                                                    else sub = mLeft;
                                                } else{
                                                    sub = valuePoss[i-1];
                                                }
                                            }
                                            maxWidth = sup-sub;
                                        }
                                    } else{
                                        maxWidth = posBoxwidth;
                                    }
                                    break;
                                case TYPE_PCP:
                                    if(alterningLabels){
                                        if(i==0){
                                            if(valuePoss.length>1) maxWidth=valuePoss[1]-valuePoss[0];
                                        } else if (i==valuePoss.length-1){
                                            if(i>0) maxWidth=valuePoss[i]-valuePoss[i-1];
                                        } else{
                                            if(i+1<valuePoss.length && i-1>=0) maxWidth=valuePoss[i+1]-valuePoss[i-1];
                                        }
                                    } else {
                                        if(i==0){
                                            maxWidth=(valuePoss[1]-valuePoss[0])/2;
                                        } else if (i==valuePoss.length-1){
                                            maxWidth=(valuePoss[i]-valuePoss[i-1])/2;
                                        } else{
                                            maxWidth=(valuePoss[i+1]-valuePoss[i-1])/2;
                                        }
                                        
                                    }
                                    
                                    xAlign=(i==0)?0:
                                        ((i==valuePoss.length-1)?1:
                                            0.5);
                                    yAlign=bottom?1:0;
                                    break;
                            }
                            
                            xLabels.add(valuePoss[i],
                                    (bottom||!alterningLabels)?(H-mBottom+2):(mTop-5),
                                    xAlign,
                                    yAlign,
                                    maxWidth,
                                    labs[i]);
                        } else
                            yLabels.add(mLeft-4,
                                    valuePoss[i],
                                    1,
                                    0.5,
                                    mLeft-4,
                                    labs[i]);
                    }
                }
            }
            
            // draw ticks for x axis
            if(type==TYPE_PCP){
                final int b = (orientation==0)?(getSize().height-mBottom):(getSize().width-mRight);
                
                int xx=0;
                while (xx<xv.getNumCats()) {
                    final int t=getAxCatPos(xx);
                    if(orientation==0){
                        if(!alterningLabels || (ax.getCatSeqIndex(xx)&1)==0) g.drawLine(t,b,t,b+2);
                        else g.drawLine(t,mTop,t,mTop-2);
                    } else{
                        g.drawLine(mLeft,t,mLeft-2,t);
                    }
                    xx++;
                }
            }
        }
        
        /* draw ticks and labels for Y axis */
        if (commonScale || (type==TYPE_BOX && vsCat)) {
            if(orientation==0){
                if(isShowLabels()) addYLabels(g,ay,true,false);
            } else {
                final double f=((orientation==0)?ay.getSensibleTickDistance(verticalMedDist,verticalMinDist):ay.getSensibleTickDistance(horizontalMedDist,horizontalMinDist));
                double fi=ay.getSensibleTickStart(f);
                while (fi<ay.vBegin+ay.vLen) {
                    final int t=ay.getValuePos(fi);
                    g.drawLine(t,getHeight()-mBottom,t,getHeight()-mBottom+2);
                    if(isShowLabels())
                        xLabels.add(t,getHeight()-mBottom+2,0.5,1, ay.getDisplayableValue(fi));
                    fi+=f;
                }
            }
            if(orientation==0)
                g.drawLine(mLeft, mTop, mLeft, getSize().height-mBottom);
            else
                g.drawLine(mLeft, getHeight()-mBottom, getWidth()-mRight,getHeight()-mBottom);
        }
    }
    
    public boolean adjustMargin(final PoGraSS g){
        switch (type){
            case TYPE_PCP:
                if(orientation==0 && !commonScale) return false;
                break;
            case TYPE_BOX:
                if(orientation==0 && (!commonScale && (!vsCat && v.length>1))) return false;
                break;
        }
        
        if(orientation==0){
            final double f=ay.getSensibleTickDistance(verticalMedDist,verticalMinDist);
            double fi=ay.getSensibleTickStart(f);
            int maxLabelLength=0;
            while (fi<ay.vBegin+ay.vLen) {
                final String s = ay.getDisplayableValue(fi);
                final int wi=g.getWidthEstimate(s);
                if(wi>maxLabelLength) maxLabelLength=wi;
                fi+=f;
            }
            return adjustMargin(maxLabelLength);
        } else{
            int maxWidth=0;
            for(int i=0; i<xv.getNumCats(); i++){
                final String s=(String)ax.getVariable().getCatAt(i);
                final int wi=g.getWidthEstimate(s);
                if(wi>maxWidth) maxWidth=wi;
            }
            return adjustMargin(maxWidth);
        }
    }
    
    private boolean adjustMargin(int maxWidth){
        final int omLeft=mLeft;
        maxWidth+=6;
        if(maxWidth>defaultMLeft-3){
            mLeft = maxWidth+3;
        } else mLeft=defaultMLeft;
        if(mLeft>omLeft) return true;
        else mLeft=omLeft;
        return false;
    }
    
    private void updateMargins() {
        switch(type){
            case TYPE_BOX:
                switch(orientation){
                    case 0:
                        if(vsCat){
                            mLeft=defaultMLeft=commonScale?bigMLeft:smallMLeft;
                        } else{
                            mLeft=defaultMLeft=(commonScale || v.length==1)?bigMLeft:smallMLeft;
                            mBottom=defaultMBottom=(v.length==1)?smallMBottom:bigMBottom;
                        }
                        break;
                    case 1:
                        if(vsCat){
                            mBottom=defaultMBottom=commonScale?bigMBottom:smallMBottom;
                        } else{
                            mLeft=defaultMLeft=(v.length==1)?smallMLeft:bigMLeft;
                            mBottom=defaultMBottom=(commonScale || v.length==1)?bigMBottom:smallMBottom;
                        }
                        break;
                }
                break;
            case TYPE_PCP:
                switch(orientation){
                    case 0:
                        mLeft=defaultMLeft=commonScale?bigMLeft:smallMLeft;
                        break;
                    case 1:
                        mBottom=defaultMBottom=commonScale?bigMBottom:smallMBottom;
                }
                break;
        }
    }
    
    public void paintInit(final PoGraSS g) {
        super.paintInit(g);
        if(type==TYPE_BOX && ax!=null && (v.length>1 || vsCat)){
            final int oBoxwidth = boxwidth;
            final int newBoxwidth = Math.max(((getAxCatPos(ax.getCatAtSeqIndex(1))-getAxCatPos(ax.getCatAtSeqIndex(0)))*8)/10,MIN_BOXWIDTH);
            if(MAX_BOXWIDTH>0) boxwidth = Math.min(newBoxwidth,MAX_BOXWIDTH);
            else boxwidth = newBoxwidth;
            posBoxwidth = newBoxwidth;
            if(boxwidth!=oBoxwidth) updateObjects();
        }
    }
    
    private int getAxCasePos(final int i) {
        return useRegularPositioning?
            getAxCatPos(ax.getCatByPos(ax.getCasePos(i))):
            ax.getCasePos(i);
    }
    
    private int getAxCatPos(final int i) {
        return useRegularPositioning?
            ax.getRegularCatPos(i,leftGap,rightGap):
            ax.getCatCenter(i);
    }
    
    private void initFlagsAndFields() {
        switch(type){
            case TYPE_BOX:
                useRegularPositioning=false;
                bigMLeft=bigMRight=30;
                if(invisiblePoints==null) invisiblePoints = new ArrayList(250);
                if(boxes==null) boxes = new ArrayList(8);
                break;
            case TYPE_PCP:
                useRegularPositioning=true;
                bigMLeft=bigMRight=15;
                if(polylines==null) polylines = new ArrayList(500);
                break;
            case TYPE_PCPBOX:
                useRegularPositioning=false;
                bigMLeft=bigMRight=30;
                if(invisiblePoints==null) invisiblePoints = new ArrayList(250);
                if(boxes==null) boxes = new ArrayList(8);
                if(polylines==null) polylines = new ArrayList(500);
                break;
        }
    }
    
    public void mouseMoved(final MouseEvent e) {
        int x=e.getX(); int y=e.getY();
        PlotPrimitive p=getFirstPrimitiveContaining(x,y);
        if(p!=null) isMouseOnHilite=p.hilitcontains(x,y);
        super.mouseMoved(e);
    }
    
    public void mouseReleased(final MouseEvent e) {
        if (baseDrag && moveDrag) {
            final int pos = (orientation==0)?e.getX():e.getY();
            final int dragNew = ax.getCatByPos(pos);
            final int dragAxis = ax.getCatByPos((orientation==0)?baseDragX1:baseDragY1);
            final int oldPos = ax.getCatSeqIndex(dragAxis);
            final int difference;
            final int myX1=ax.getCatLow(dragNew);
            final int myX2=ax.getCatUp(dragNew);
            if(Math.abs(difference=pos-getAxCatPos(dragNew)) > (myX2-myX1)/4){
                int newPos=ax.getCatSeqIndex(dragNew);
                if(difference>0) newPos += 1;
                if(oldPos<newPos) newPos -=1;
                ax.moveCat(dragAxis, newPos);
            } else{
                ax.swapCats(dragNew, dragAxis);
            }
            
            baseDrag=false;
            updateObjects();
            setUpdateRoot(0);
            repaint();
        } else super.mouseReleased(e);
    }
    
    public Dimension getMinimumSize() { return new Dimension(MINWIDTH,MINHEIGHT); };
    
    private PPrimBox createBox(final OrdStats os, final int x, final int w, final int rank){
        final Axis axis = (commonScale || rank==0)?ay:opAy[rank-1];
        final PPrimBox box = new PPrimBox();
        box.x=x;
        box.w=w;
        box.med = axis.getValuePos(os.med);
        box.lh = axis.getValuePos(os.lh);
        box.uh = axis.getValuePos(os.uh);
        box.lh15 = axis.getValuePos(os.lh15);
        box.uh15 = axis.getValuePos(os.uh15);
        box.medValue = os.med;
        box.lhValue = os.lh;
        box.uhValue = os.uh;
        box.lh15Value = os.lh15;
        box.uh15Value = os.uh15;
        box.lh3 = os.lh3;
        box.uh3 = os.uh3;
        box.lowEdge = os.lowEdge;
        box.lastR = new double[os.lastR.length];
        box.valPos = new int[os.lastR.length];
        for(int i=0; i< box.lastR.length; i++){
            box.lastR[i] = v[rank].atF(os.lastR[i]);
            box.valPos[i] = axis.getValuePos(box.lastR[i]);
        }
        box.lastTop = os.lastTop;
        box.highEdge = os.highEdge;
        
        //System.out.println("x: " + x + ", w: " + w + ", med: " + ay.getValuePos(os.med) + ", lh: " + ay.getValuePos(os.lh) + ", uh: " + ay.getValuePos(os.uh)
        //+  ", lh15: " + ay.getValuePos(os.lh15) + ", uh15: " + ay.getValuePos(os.uh15) + ", lh3:" +  os.lh3 + ", uh3: " + os.uh3 + ", lowedge: " + os.lowEdge);
        return box;
    }
    
    public void updateObjects() {
        if(!getValid()) return;
        
        switch(type){
            case TYPE_BOX:
                initBoxes(0);
                break;
            case TYPE_PCP:
                if (pp==null || pp.length!=v[0].size()) {
                    pp=new PlotPrimitive[v[0].size()];
                }
                
                initPolylines(0);
                break;
            case TYPE_PCPBOX:
                initBoxes(v[0].size());
                initPolylines(v.length);
        }
    }
    
    public void paintPost(final PoGraSS g) {
        if(baseDrag && moveDrag){
            if(type==TYPE_PCP){
                final int basey=getBounds().height-mBottom;
                final int pos = (orientation==0)?baseDragX2:baseDragY2;
                final int dragNew = ax.getCatByPos(pos);
                final int myX1=ax.getCatLow(dragNew);
                final int myX2=ax.getCatUp(dragNew);
                final int difference;
                if(Math.abs(difference=pos-getAxCatPos(dragNew)) > (myX2-myX1)/4){
                    final int x;
                    final int w;
                    if(difference>0){
                        x=ax.getCatCenter(dragNew);
                        w=2*(myX2-x);
                    } else{
                        w=2*(ax.getCatCenter(dragNew)-myX1);
                        x=ax.getCatCenter(dragNew)-w;
                        
                    }
                    if(orientation==0) g.fillRect(x,basey,w,4);
                    else g.fillRect(mLeft,x,4,w);
                } else{
                    if(orientation==0) g.fillRect(myX1,basey,myX2-myX1,4);
                    else g.fillRect(mLeft,myX1,4,myX2-myX1);
                }
            } else{
                final int dragAxis = ax.getCatByPos((orientation==0)?baseDragX1:baseDragY1);
                if(dragAxis>-1) {
                    pp[dragAxis].setVisible(true);
                    ((PPrimBase)pp[dragAxis]).setDragging(true);
                    pp[dragAxis].paint(g,orientation,m);
                }
            }
        }
        super.paintPost(g);
    }
    
    public String queryObject(final PlotPrimitive p) {
        int mark=getMarked(p);
        if(p!=null){
            if(p instanceof PPrimBox){
                String qs="";
                final PPrimBox box = (PPrimBox)p;
                if(box.queriedOutlier!=null)
                    qs+="Outlier: " + Tools.getDisplayableValue(box.queriedOutlier.getValue());
                else
                    qs+="lower whisker: " + Tools.getDisplayableValue(box.lh15Value) + "\n" +
                            "lower hinge: " + Tools.getDisplayableValue(box.lhValue) + "\n" +
                            "median: " + Tools.getDisplayableValue(box.medValue) + "\n" +
                            "upper hinge: " + Tools.getDisplayableValue(box.uhValue) + "\n" +
                            "upper whisker: " + Tools.getDisplayableValue(box.uh15Value);
                if(isExtQuery) {
                    qs+="\ncases: "+p.cases();
                    if(isMouseOnHilite || mark>0) qs+="\nhighlighted: "+mark+" ("+Tools.getDisplayableValue(100*(double)mark/p.cases(),2)+"%)";
                }
                if(!isExtQuery)
                    if(isMouseOnHilite || mark>0) {
                    qs+="\n\nhighlighted: "+Tools.getDisplayableValue(100*(double)mark/p.cases(),2)+"%";
                    }
                return qs;
            }
            if(p instanceof PPrimPolygon){
                String retValue="";
                final int[] pts = (orientation==0)?(((PPrimPolygon)p).pg.ypoints):(((PPrimPolygon)p).pg.xpoints);
                
                if(isExtQuery){
                    for(int i=0; i<v.length; i++){
                        retValue += v[i].getName() + ": ";
                        if(v[i].isCat()){
                            retValue += v[i].getCatAt((int)((commonScale||i==0)?ay:opAy[i-1]).getValueForPos(pts[i])) + "\n";
                        } else{
                            retValue += Tools.getDisplayableValue(
                                    ((commonScale||i==0)?ay:opAy[i-1]).getValueForPos(pts[i])) + "\n";
                        }
                        
                    }
                } else{
                    final int c = ax.getCatByPos((orientation==0)?mouseX:mouseY);
                    final int i = ax.getCatSeqIndex(c);
                    retValue += v[c].getName() + ": ";
                    if(v[c].isCat()){
                        retValue += v[c].getCatAt((int)((commonScale||i==0)?ay:opAy[i-1]).getValueForPos(pts[i])) + "\n";
                    } else{
                        retValue += Tools.getDisplayableValue(
                                ((commonScale||c==0)?ay:opAy[c-1]).getValueForPos(pts[i]));
                    }
                }
                return retValue;
            }
        }
        return null;
    }
    
    
    public String queryPlotSpace() {
        switch(type) {
            case TYPE_BOX:
                return "Boxplot"+(cv!=null?"("+cv.getName()+")":"")
                +(v!=null?"\nvariables: "+v.length:"");
            case TYPE_PCP:
                int size=(v!=null&&v[0]!=null)?v[0].size():-1;
                return "PCP"+(cv!=null?"("+cv.getName()+")":"")
                +(size!=-1?"\nsize: "+v[0].size():"")
                +"\nselected: "+m.marked()+(size>0?" ("+Tools.getDisplayableValue(100*((double)m.marked()/(double)size),2)+" %)":"")
                +(v!=null?"\nvariables: "+v.length:"");
        }
        return super.queryPlotSpace();
    }
    
    public void rotate(final int amount) {
        super.rotate(amount);
        updateMargins();
    }
    
    public void paintSelected(final PoGraSS g) {
        // in boxplots painting of selected primitives is handled by the canvas itself, not by the primitive
        if(type==TYPE_BOX || type==TYPE_PCPBOX){
            if(markStats!=null){
                final int md[][] = new int[v.length][];
                for(int i=0; i<v.length; i++) md[i] = v[i].getRanked(m,-1);
                //if(md==null) return;
                if (vsCat) {
                    if(md[0]==null) return;
                    int i=0;
                    while (i<cs) { rs[cs+1+i]=0; i++; }
                    i=0;
                    while(i<md[0].length) {
                        int x=cv.getCatIndex(cv.at(md[0][i]));
                        if (x<0) x=cs;
                        x+=cs+1;
                        rk[x][rs[x]]=md[0][i];
                        rs[x]++;
                        i++;
                    }
                    i=cs+1;
                    while(i<2*cs+1) {
                        oss[i].update(v[0],rk[i],rs[i]);
                        i++;
                    }
                } else {
                    for(int i=0; i<v.length; i++) {
                        if(md[i]!=null) markStats[i].update(v[i],md[i]);
                        else markStats[i].update(v[i],new int[]{});
                    }
                }
                for(int i=0; i<boxes.size(); i++){
                    final PPrimBox box = (PPrimBox)boxes.get(i);
                    if(markStats[i].lastTop==0){
                        box.slastR=null;
                    } else{
                        final Axis axis = (commonScale || i==0 || vsCat)?ay:opAy[i-1];
                        box.sx = box.x + box.w/4;
                        box.sw = box.w/2;
                        box.smed = axis.getValuePos(markStats[i].med);
                        box.slh = axis.getValuePos(markStats[i].lh);
                        box.suh = axis.getValuePos(markStats[i].uh);
                        box.slh15 = axis.getValuePos(markStats[i].lh15);
                        box.suh15 = axis.getValuePos(markStats[i].uh15);
                        box.slh3 = markStats[i].lh3;
                        box.suh3 = markStats[i].uh3;
                        box.slowEdge = markStats[i].lowEdge;
                        final int vind = (vsCat?0:i);
                        box.sminValue = v[vind].atD(md[vind][0]);
                        box.smaxValue = v[vind].atD(md[vind][md[vind].length-1]);
                        if(markStats[i].lastR!=null){
                            box.slastR = new double[markStats[i].lastR.length];
                            box.svalPos = new int[markStats[i].lastR.length];
                            for(int j=0; j< box.slastR.length; j++){
                                box.slastR[j] = v[vsCat?0:i].atF(markStats[i].lastR[j]);
                                box.svalPos[j] = axis.getValuePos(box.slastR[j]);
                            }
                        } else{
                            box.slastR = null;
                            box.svalPos = null;
                        }
                        box.slastTop = markStats[i].lastTop;
                        box.shighEdge = markStats[i].highEdge;
                    }
                }
            }
        }
        super.paintSelected(g);
    }
    
    private void initOss(final SVar[] yvs) {
        if(yvs.length==1){
            if (v[0]!=null && !v[0].isCat() && v[0].isNum())
                valid=true; // valid are only numerical vars non-cat'd
            else valid=false;
            if (valid) {
                OSdata=new OrdStats();
                final int dr[]=v[0].getRanked();
                OSdata.update(v[0],dr);
                //updateObjects();
            }
        } else{
            oss = new OrdStats[v.length];
            for(int i=0; i<v.length; i++){
                if (v[i]!=null && !v[i].isCat() && v[i].isNum())
                    valid=true; // valid are only numerical vars non-cat'd
                if (valid) {
                    oss[i]=new OrdStats();
                    final int dr[]=v[i].getRanked();
                    oss[i].update(v[i],dr);
                }
            }
        }
    }
    
    private PPrimCircle createInvisiblePoint(final int x, final int y, final int caseID) {
        final PPrimCircle ppc = new PPrimCircle();
        ppc.x=(orientation==0)?x:y;
        ppc.y=(orientation==0)?y:x;
        ppc.diam=1;
        ppc.ref = new int[]{caseID};
        ppc.setVisible(false);
        ppc.queryable = false;
        ppc.fillColor = ppc.borderColor;
        return ppc;
    }
    
    private void sortAxesBy(final double[] ranks){
        final int[] ix;
        ix=Tools.sortDoublesIndex(ranks);
        ignoreNotifications=true;
        int i=ix.length-1;
        while (i>=0) {
            ax.moveCat(ix[i],ix.length-i);
            i--;
        }
        updateObjects();
        ignoreNotifications=false;
        setUpdateRoot(0);
        repaint();
    }
    
    private void sortAxesBy(final int[] ranks){
        final int[] ix;
        ix=Tools.sortIntegersIndex(ranks);
        ignoreNotifications=true;
        int i=ix.length-1;
        while (i>=0) {
            ax.moveCat(ix[i],ix.length-i);
            i--;
        }
        updateObjects();
        ignoreNotifications=false;
        setUpdateRoot(0);
        repaint();
    }
    
    private void sortAxes(final boolean bySelected) {
        // works only for box plots
        if(type!=TYPE_BOX) return;
        
        final int axes = pp.length - invisiblePoints.size();
        final int[] coumar = new int[axes];
        if(bySelected){
            for (int i=0; i<axes; i++){
                coumar[i] = getMarked(i);
            }
        } else{
            for (int i=0; i<axes; i++){
                coumar[i] = getCount(i);
            }
        }
        
        sortAxesBy(coumar);
    }
    
    private int getMarked(final int axis){
        // works only for box plots
        return (int)Math.round(pp[axis].cases()*pp[axis].getMarkedProportion(m,-1));
    }
    
    private int getMarked(final PlotPrimitive p) {
        if(p!=null)
            return (int)Math.round(p.cases()*p.getMarkedProportion(m,-1));
        else return -1;
    }
    
    private int getCount(final int axis) {
        // works only for box plots
        return pp[axis].cases();
    }
    
    protected Axis getMouseOverAxis(final int x, final int y) {
        if(axcoordX==null || axcoordY==null) return null;
        if(x>=axcoordX[0]-2 && x<= axcoordX[1]+2 && y>=axcoordY[0]-2 && y<=axcoordY[1]+2) return ax;
        else {
            for(int i=0;i<v.length;i++) {
                if(x>=aycoordX[i][0]-2 && x<= aycoordX[i][1]+2 && y>=aycoordY[i][0]-2 && y<=aycoordY[i][1]+2) return i==0?ay:opAy[i-1];
            }
            return null;
        }
    }
    
    protected String getAxisQuery(final int x, final int y) {
//    	System.out.println("x: " + x + ", y: " + y + ", axX: " + axcoordX[0] + ", axY: " + axcoordY[0] + ", ayX: " + aycoordX[0] + ", ayY: " + aycoordY[0]);
        final Axis a=getMouseOverAxis(x,y);
        if(a==null) return null;
        else return "axis name: " + a.getVariable().getName();
    }
    
    protected void resetAxesCoord() {
        axcoordY=new int[2]; axcoordX=new int[2];
        aycoordY=new int[v.length][2]; aycoordX=new int[v.length][2];
        // just setting some maybe graphically impossible values to reach
        for(int i=0; i<axcoordY.length; i++) {axcoordY[i]=-256;axcoordX[i]=-256;}
        for(int i=0; i<aycoordY.length; i++) {
            for(int j=0; j<aycoordY[i].length; j++) {
                aycoordY[i][j]=-256; aycoordX[i][j]=-256;
            }
        }
    }
    
    // needed for axis-query
    protected int[] axcoordX, axcoordY;
    protected int[][] aycoordX, aycoordY;
    
    public void mouseDragged(final MouseEvent e) {
        super.mouseDragged(e);
        if(type==TYPE_BOX || type==TYPE_PCPBOX){
            final int dragAxis = ax.getCatByPos((orientation==0)?baseDragX1:baseDragY1);
            if(baseDrag && moveDrag && dragAxis>-1){
                if(orientation==0){
                    ((PPrimBase)pp[dragAxis]).moveX(e.getX()-boxwidth/2);
                } else{
                    ((PPrimBase)pp[dragAxis]).moveY(e.getY()-boxwidth/2);
                }
                
                setUpdateRoot(0);repaint();
            }
        }
    }
    
    private void updateSortingMenus() {
        final boolean enable = type==TYPE_BOX && vsCat;
        MIsortByCount.setEnabled(enable);
        MIsortByMarked.setEnabled(enable);
        MIsortByMarkedRel.setEnabled(enable);
        MIsortByMedian.setEnabled(enable);
        MIsortByMax.setEnabled(enable);
        MIsortByMin.setEnabled(enable);
        MIsortByMarkedMedian.setEnabled(enable);
        MIsortByMarkedMax.setEnabled(enable);
        MIsortByMarkedMin.setEnabled(enable);
    }
    
    private void initBoxes(int additionalSpace) {
        boxes.clear();
        invisiblePoints.clear();
        if (!vsCat) {
            invisiblePoints.ensureCapacity(v.length*v[0].size());
            for(int i=0; i<v.length; i++) {
                final int x = getAxCatPos(i);
                final Axis yAxis = commonScale?ay:((i==0)?ay:opAy[i-1]);
                for(int j=0; j<v[i].size(); j++){
                    final int y = yAxis.getValuePos(v[i].atD(j));
                    invisiblePoints.add(createInvisiblePoint(x,y,j));
                }
            }
            pp = new PlotPrimitive[v.length + invisiblePoints.size() + additionalSpace];
            markStats = new OrdStats[v.length];
            for(int i=0; i<v.length; i++){
                pp[i] = createBox((v.length==1)?OSdata:oss[i], getAxCasePos(i)-boxwidth/2,boxwidth,i);
                boxes.add(pp[i]);
                final PPrimBase ppb = (PPrimBase)pp[i];
                ppb.ref = v[i].getRanked();
                ppb.performAlphaBlending = false;
                markStats[i] = new OrdStats();
            }
            for(int i=v.length; i<pp.length-additionalSpace; i++){
                pp[i] = (PlotPrimitive)invisiblePoints.get(i-v.length);
            }
        } else {
            ArrayList pplist = new ArrayList(v[0].size()+10);
            for(int i=0; i<cs; i++){
                final PPrimBox box = createBox(oss[i],getAxCasePos(i)-boxwidth/2,boxwidth,0);
                box.ref = rk[i];
                box.performAlphaBlending = false;
                boxes.add(box);
            }
            invisiblePoints.ensureCapacity(v[0].size());
            for(int i=0; i<cs; i++){
                final int x = getAxCasePos(i);
                for(int j=0; j<rk[i].length; j++){
                    final int y = ay.getValuePos(v[0].atD(rk[i][j]));
                    invisiblePoints.add(createInvisiblePoint(x,y,rk[i][j]));
                }
            }
            pplist.addAll(boxes);
            pplist.addAll(invisiblePoints);
            pp = new PlotPrimitive[pplist.size()];
            pplist.toArray(pp);
            markStats = new OrdStats[boxes.size()];
            System.arraycopy(oss, cs+1, markStats, 0, cs);
        }
        final int iPsize = invisiblePoints.size();
        for(int i=0; i<pp.length-iPsize; i++){
            if(pp[i] instanceof PPrimBox) ((PPrimBox)pp[i]).slastR=null;
            setColors((PPrimBase)pp[i]);
        }
    }
    
    private void initPolylines(int offset) {
        polylines.clear();
        final int[][] xs = new int[v[0].size()][v.length];
        final int[][] ys = new int[v[0].size()][v.length];
        //boolean[] na = new boolean[v[0].size()];
        final int[][] na = new int[v[0].size()][];
        final int[] naIndices = new int[v.length+1];
        for (int i=0;i<v[0].size();i++){
            int numNAs=0;
            for (int j=0;j<v.length;j++){
                xs[i][ax.getCatSeqIndex(j)] = getAxCatPos(j);
                if(v[j].isCat()){
                    ys[i][ax.getCatSeqIndex(j)] = ((commonScale||j==0)?ay:opAy[j-1]).getValuePos(v[j].getCatIndex(i));
                } else {
                    ys[i][ax.getCatSeqIndex(j)] = ((commonScale||j==0)?ay:opAy[j-1]).getValuePos(v[j].atD(i));
                }
                if ((!drawHidden && m.at(i)) || v[j].at(i)==null) {
                    naIndices[numNAs++] = j;
                }
            }
            if(numNAs>0){
                na[i] = new int[numNAs];
                System.arraycopy(naIndices, 0, na[i], 0, numNAs);
            }
        }
        
        for(int j=0; j<xs.length; j++){
            pp[j+offset] = new PPrimPolygon();
            polylines.add(pp[j+offset]);
            if(orientation==0) ((PPrimPolygon)pp[j+offset]).pg = new Polygon(xs[j], ys[j], xs[j].length);
            else               ((PPrimPolygon)pp[j+offset]).pg = new Polygon(ys[j], xs[j], xs[j].length);
            ((PPrimPolygon)pp[j+offset]).closed=false;
            ((PPrimPolygon)pp[j+offset]).fill=false;
            ((PPrimPolygon)pp[j+offset]).selectByCorners=!drawLines;
            ((PPrimPolygon)pp[j+offset]).drawCorners = drawPoints;
            ((PPrimPolygon)pp[j+offset]).ref = new int[] {j};
            ((PPrimPolygon)pp[j+offset]).setNodeSize(nodeSize);
            ((PPrimPolygon)pp[j+offset]).drawBorder=drawLines;
            ((PPrimPolygon)pp[j+offset]).showInvisibleLines=drawNAlines;
            setColors((PPrimBase)pp[j+offset]);
            final boolean[] nas = new boolean[xs[j].length];
            final boolean[] gap = new boolean[xs[j].length];
            
            if(na[j]!=null){
                final boolean[] nod = new boolean[xs[j].length];
                for(int i=0; i<na[j].length; i++) {
                    nas[na[j][i]]=true;
                    if(na[j][i]>0) nas[na[j][i]-1]=true;
                    nod[na[j][i]]=true;
                }
                ((PPrimPolygon)pp[j+offset]).noDotsAt = nod;
                for(int i=0; i<na[j+offset].length-1; i++){
                    if(na[j][i+1]-na[j][i]==2) gap[na[j][i]+1]=true;
                }
                if(na[j][0]==1) gap[0]=true;
                if(na[j][na[j].length-1]==gap.length-2) gap[gap.length-1]=true;
            }
            ((PPrimPolygon)pp[j+offset]).invisibleLines=nas;
            ((PPrimPolygon)pp[j+offset]).setGapDots(gap);
        }
    }
}




/** OrdStats - ordinal statistics of a variable, used internally by {@link BoxCanvas}
 * to get necessary information to plot bopxplots */
class OrdStats { // get ordinal statistics to be used in boxplot
    double med, uh, lh, uh15, lh15, uh3, lh3;
    int[] lastR;
    int lastTop;
    /** indexes of points just above/below the 1.5 hinge
     * beware, this is relative to the used r[] so
     * use with care and only with the corresponding r[] */
    int lowEdge, highEdge;
    
    OrdStats() { med=uh=lh=uh3=lh3=0; };
    
    private double medFrom(final SVar v,final int[] r,final int min,final int max) {
        return (((max-min)&1)==0)
        ?v.atF(r[min+(max-min)/2])
        :((v.atF(r[min+(max-min)/2])+v.atF(r[min+(max-min)/2+1]))/2);
    };
    
    void update(final SVar v, final int[] r) {
        update(v,r,r.length);
    };
    
    /* v=variable, r=ranked index as returned by getRanked, n=# of el to use */
    void update(final SVar v, final int[] r, final int n) {
        lastTop=n;
        if (n<1) return;
        med=medFrom(v,r,0,n-1);
        uh=medFrom(v,r,n/2,n-1);
        if (n>1 && (n&1)==1)
            lh=medFrom(v,r,0,n/2-1);
        else
            lh=medFrom(v,r,0,n/2);
        lh15=lh-1.5*(uh-lh);
        lh3=lh-3*(uh-lh);
        double x=lh;
        int i=n/4; // find lh15 as extreme between lh and lh15
        while (i>=0) {
            final double d=v.atF(r[i]);
            if (d<lh15) break;
            if (d<x) x=d;
            i--;
        }
        lowEdge=i;
        lh15=x;
        uh15=uh+1.5*(uh-lh);
        uh3=uh+3*(uh-lh);
        x=uh;
        i=n*3/4-1; if (i<0) i=0; // find uh15
        while (i<n) {
            final double d=v.atF(r[i]);
            if (d>uh15) break;
            if (d>x) x=d;
            i++;
        }
        uh15=x;
        highEdge=i;
        lastR=r;
    };
};
