package org.rosuda.ibase.plots;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.KeyEvent;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.ibase.toolkit.PGSCanvas.*;
import org.rosuda.pograss.PoGraSS;
import org.rosuda.util.*;



public class ParallelAxesCanvas extends BaseCanvas {
    
    public ParallelAxesCanvas(final PlotComponent ppc, final Frame f, final SVar var, final SVar cvar, final SMarker mark) {
        super(ppc,f,mark);
        
        allowDragMove=true;
        objectClipping=true;
        commonScale=false;
        
        mBottom=standardMBottom;
        mTop=standardMTop;
        mLeft=standardMLeft;
        mRight=standardMRight;
        
        v = new SVar[]{var};
        cv = cvar;
        
        xv=new SVarObj(getShortClassName() + ".index",true);
        for(int i=0; i<cv.getNumCats(); i++){
            xv.add(cv.getCatAt(i).toString());
        }
        ax=new Axis(xv,Axis.O_X,xv.isCat()?Axis.T_EqCat:Axis.T_Num); ax.addDepend(this);
        ay=new Axis(v[0],Axis.O_Y,Axis.T_Num); ay.addDepend(this);
        // get some space around (this comes from the scatterplots)
        ay.setValueRange(v[0].getMin()-(v[0].getMax()-v[0].getMin())/20,(v[0].getMax()-v[0].getMin())*1.1);
        
        createMenu(f);
        setCommonScale(commonScale);
        EzMenu.getItem(getFrame(),M_COMMON).setEnabled(false);
    }
    
    /** basic constructor. Every subclass must call this constructor
     * @param f frame owning this canvas. since BaseCanvas itself doesn't modify any attribute of the frame except for title it is possible to put more canvases into one frame. This doesn't have to hold for subclasses, especially those providing their own menus.
     * @param mark marker which will be used for selection/linked highlighting
     */
    public ParallelAxesCanvas(final PlotComponent ppc, final Frame f, final SVar[] yvs, final SMarker mark) {
        super(ppc, f, mark);
        
        allowDragMove=true;
        objectClipping=true;
        
        mBottom=standardMBottom;
        mTop=standardMTop;
        mLeft=standardMLeft;
        mRight=standardMRight;
        
        v=new SVar[yvs.length];
        opAy=new Axis[yvs.length-1];
        int i=0;
        xv=new SVarObj(getShortClassName() + ".index",true);
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
        ay=new Axis(yvs[0],Axis.O_Y,Axis.T_Num); ay.addDepend(this);
        ay.setValueRange(totMin-(totMax-totMin)/20,(totMax-totMin)*1.1);
        
        createMenu(f);
        setCommonScale(commonScale);
    }
    
    /** use trigraph for X axis in case X is categorical */
    boolean useX3=false;
    
    boolean drawAxes=false;
    
    boolean commonScale=false;
    
    
    protected int standardMLeft=30;
    protected int standardMTop=20;
    protected int standardMBottom=20;
    protected int standardMRight=10;
    int leftGap=7;
    int rightGap=7;
    
    /** y variables */
    SVar v[];
    /** x variable */
    SVar xv;
    /** categorical variable */
    SVar cv;
    
    double totMin, totMax;
    
    static final String M_PLUS = "+";
    static final String M_MINUS = "-";
    static final String M_SET1 = "set1";
    static final String M_SET64 = "set64";
    static final String M_RESET = "reset";
    static final String M_LABELS = "labels";
    static final String M_TRIGRAPH = "trigraph";
    static final String M_SHOWDOTS = "Show dots";
    static final String M_TOGGLEPTS = "togglePts";
    static final String M_NODESIZEUP = "nodeSizeUp";
    static final String M_NODESIZEDOWN = "nodeSizeDown";
    static final String M_SHOWAXES = "Show axes";
    static final String M_TOGGLEAXES = "toggleAxes";
    static final String M_HIDELINES = "Hide lines";
    static final String M_TOGGLELINES = "toggleLines";
    static final String M_HIDENALINES = "hideNAlines";
    static final String M_COMMON = "common";
    static final String M_YRANGEDLG = "YrangeDlg";
    static final String M_SCALEDLG = "scaleDlg";
    static final String M_ALPHADOWN = "alphaDown";
    static final String M_ALPHAUP = "alphaUp";
    static final String M_TRANSHIGHL = "transparentHighlighting";
    
    MenuItem MIlabels=null;
    MenuItem MIdots=null;
    MenuItem MIaxes=null;
    MenuItem MIlines=null;
    MenuItem MItrigraph=null;
    MenuItem MInodeSizeUp=null;
    MenuItem MInodeSizeDown=null;
    MenuItem MIhideNAlines=null;
    protected MenuItem MItransHighl=null;
    
    boolean drawPoints=false;
    boolean drawLines=true;
    boolean drawNAlines=true;
    boolean drawHidden=true;
    
    int nodeSize=2;
    
    int X,Y;
    
    protected boolean valid=true;
    
    int TW, TH;
    
    protected void createMenu(Frame f){
        createMenu(f,true,true,new String[]{
            "@LHide labels",M_LABELS,
            "@TShorten lables",M_TRIGRAPH,
            M_SHOWDOTS,M_TOGGLEPTS,
            "Increase dot size (up)",M_NODESIZEUP,
            "Decrease dot size (down)",M_NODESIZEDOWN,
            M_SHOWAXES,M_TOGGLEAXES,
            M_HIDELINES,M_TOGGLELINES,
            "@NHide NA lines",M_HIDENALINES,
            M_MINUS,
            "Common scale",M_COMMON,
            M_MINUS,
            "Set Y Range ...",M_YRANGEDLG,
            "!SShow scale dialog",M_SCALEDLG,
            M_MINUS,
            "More transparent (left)",M_ALPHADOWN,
            "More opaque (right)",M_ALPHAUP,
            "Transparent highlighting",M_TRANSHIGHL,
            "Set Colors (CB)",M_SET1,
            "Set Colors (rainbow)",M_SET64,
            "Clear Colors",M_RESET
        });
        
        MIlabels=EzMenu.getItem(f,M_LABELS);
        MIdots=EzMenu.getItem(f,M_TOGGLEPTS);
        MIaxes=EzMenu.getItem(f,M_TOGGLEAXES);
        MIlines=EzMenu.getItem(f,M_TOGGLELINES);
        MIlines.setEnabled(false);
        MItrigraph=EzMenu.getItem(f, M_TRIGRAPH);
        MInodeSizeUp=EzMenu.getItem(f, M_NODESIZEUP);
        MInodeSizeUp.setEnabled(false);
        MInodeSizeDown=EzMenu.getItem(f, M_NODESIZEDOWN);
        MInodeSizeUp.setEnabled(false);
        MIhideNAlines=EzMenu.getItem(f,M_HIDENALINES);
        MItransHighl=EzMenu.getItem(f,M_TRANSHIGHL);
    }
    
    public void keyPressed(final KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_RIGHT) run(this, M_ALPHAUP);
        if (e.getKeyCode()==KeyEvent.VK_LEFT) run(this, M_ALPHADOWN);
        if (e.getKeyCode()==KeyEvent.VK_UP) run(this, M_NODESIZEUP);
        if (e.getKeyCode()==KeyEvent.VK_DOWN) run(this,M_NODESIZEDOWN);
    }
    
    public Object run(final Object o, final String cmd) {
        if ("print".equals(cmd)) { drawHidden=false; run(o,"exportPS"); drawHidden=true; return this; }
        super.run(o,cmd);
        if (M_LABELS.equals(cmd)) {
            setShowLabels(!isShowLabels());
            MIlabels.setLabel((isShowLabels())?"Hide labels":"Show labels");
            setUpdateRoot(0);
            repaint();
        }
        if (M_ALPHADOWN.equals(cmd)) {
            ppAlpha-=(ppAlpha>0.2)?0.10:0.02; if (ppAlpha<0.05f) ppAlpha=0.05f;
            setUpdateRoot(0); repaint();
        }
        if (M_ALPHAUP.equals(cmd)) {
            ppAlpha+=(ppAlpha>0.2)?0.10:0.02; if (ppAlpha>1f) ppAlpha=1f;
            setUpdateRoot(0); repaint();
        }
        if ("exit".equals(cmd)) WinTracker.current.Exit();
        if (M_COMMON.equals(cmd)) { setCommonScale(!commonScale); updateObjects(); setUpdateRoot(0); repaint(); }
        if (M_TRIGRAPH.equals(cmd)) {
            useX3=!useX3;
            MItrigraph.setLabel(useX3?"Extend labels":"Shorten labels");
            setUpdateRoot(0); repaint();
        }
        if (M_TOGGLEPTS.equals(cmd)) {
            drawPoints=!drawPoints;
            MIdots.setLabel((drawPoints)?"Hide dots":M_SHOWDOTS);
            for(int i=0; i<pp.length; i++){
                ((PPrimPolygon)pp[i]).drawCorners=drawPoints;
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
            MIlines.setLabel((drawLines)?M_HIDELINES:"Show lines");
            for(int i=0; i<pp.length; i++){
                ((PPrimPolygon)pp[i]).drawBorder=drawLines;
                ((PPrimPolygon)pp[i]).selectByCorners=!drawLines;
            }
            MIdots.setEnabled(!drawPoints||drawLines);
            MIlines.setEnabled(drawPoints||!drawLines);
            setUpdateRoot(0);
            repaint();
        }
        if (M_TOGGLEAXES.equals(cmd)) {
            drawAxes=!drawAxes;
            MIaxes.setLabel((drawAxes)?"Hide axes":M_SHOWAXES);
            setUpdateRoot(0); repaint();
        }
        if (M_YRANGEDLG.equals(cmd) || "XrangeDlg".equals(cmd)) {
            final Axis rt=(M_YRANGEDLG.equals(cmd))?ay:ax;
            final Dialog d=intDlg=new Dialog(myFrame,(rt==ay)?"Y range":"X range",true);
            
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
            final RespDialog d=new RespDialog(myFrame,"Set y scale",true,RespDialog.okCancel);
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
        if (M_SET1.equals(cmd)) {
            if (pp!=null && pp.length>0) {
                int i=0;
                while (i<pp.length) {
                    final int cs[] = ((PPrimBase)pp[i]).getCaseIDs();
                    int j=0;
                    if (cs!=null)
                        while (j<cs.length)
                            m.setSec(cs[j++],i+16);
                    i++;
                }
                m.NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
            }
        }
        if (M_SET64.equals(cmd)) {
            if (pp!=null && pp.length>0) {
                int i=0;
                while (i<pp.length) {
                    //System.out.println("set64: "+i+" (of "+pp.length+") mapped to "+ax.getCatAtSeqIndex(i)+", pp="+pp[i]);
                    final int cs[] = ((PPrimBase)pp[i]).getCaseIDs();
                    int j=0;
                    if (cs!=null)
                        while (j<cs.length)
                            m.setSec(cs[j++],64+(64*i/pp.length));
                    i++;
                }
                m.NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
            }
        }
        if (M_RESET.equals(cmd)) {
            if (m.getSecCount()>0) {
                m.resetSec();
                m.NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
            }
        }
        if (M_NODESIZEUP.equals(cmd)) {
            if(pp[0]!=null){
                nodeSize++;
                for(int i=0; i<pp.length; i++)
                    if(pp[i]!=null)
                        ((PPrimPolygon)pp[i]).setNodeSize(nodeSize);
                setUpdateRoot(0); repaint();
            }
        }
        if (M_NODESIZEDOWN.equals(cmd)) {
            if(pp[0]!=null){
                nodeSize--;
                for(int i=0; i<pp.length; i++)
                    if(pp[i]!=null)
                        ((PPrimPolygon)pp[i]).setNodeSize(nodeSize);
                setUpdateRoot(0); repaint();
            }
        }
        if (M_HIDENALINES.equals(cmd)){
            drawNAlines=!drawNAlines;
            for(int i=0; i<pp.length; i++){
                if(pp[i]!=null) ((PPrimPolygon)pp[i]).showInvisibleLines = drawNAlines;
            }
            MIhideNAlines.setLabel(drawNAlines?"Hide NA lines":"Show NA lines");
            setUpdateRoot(0); repaint();
        }
        if(M_TRANSHIGHL.equals(cmd)) {
            alphaHighlighting=!alphaHighlighting;
            MItransHighl.setLabel(alphaHighlighting?"Opaque highlighting":"Transparent highlighting");
            setUpdateRoot(1); repaint();
        }
        
        return null;
    }
    
    public SVar getData(final int id) { return (id>=0 && id<v.length)?v[id]:null; }
    
    public void performZoomIn(final int x1, final int y1, final int x2, final int y2) {
        if(commonScale) super.performZoomIn(x1, y1, x2, y2, null,ay);
        else{
            int minZoomAxis=0;
            int maxZoomAxis=v.length-1;
            
            while(ax.getRegularCatPos(ax.getCatAtSeqIndex(minZoomAxis), leftGap, rightGap) < x1) minZoomAxis++;
            while(ax.getRegularCatPos(ax.getCatAtSeqIndex(maxZoomAxis), leftGap, rightGap) > x2) maxZoomAxis--;
            
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
    
    public String queryObject(int i) {
        return queryObject(pp[i]);
    }
    
    public void setCommonScale(final boolean cs) {
        //if(cs==commonScale) return;
        commonScale=cs;
        EzMenu.getItem(getFrame(),M_COMMON).setLabel(cs?"Individual scales":"Common scale");
        EzMenu.getItem(getFrame(),M_YRANGEDLG).setEnabled(cs);
        if (cs) {
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
                
                updateGeometry=true;
            }
            ay.setValueRange(v[0].getMin()-(v[0].getMax()-v[0].getMin())/20,(v[0].getMax()-v[0].getMin())*1.1);
        }
    }
    
    protected String getShortClassName(){
        return "PA";
    }
    
    public void paintBack(final PoGraSS g) {
        final Rectangle r=pc.getBounds();
        g.setBounds(r.width,r.height);
        
        TH=r.height;
        TW=r.width;
        
        final int innerH;
        innerH=TH-mBottom-mTop;
        Y=TH-mBottom-innerH;
        
        if(!getValid()){
            g.setColor("red");
            g.drawLine(0,0,TW,TH);
            g.drawLine(0,TH,TW,0);
            return;
        }
        
        labels.clear();
        addLabelsAndTicks(g);
        labels.finishAdd();
    }
    
    protected boolean getValid() {
        return valid;
    }
    
    protected void addLabelsAndTicks(PoGraSS g) {}
    
    public boolean adjustMargin(){
        if(orientation==0){
            double f=ay.getSensibleTickDistance(30,18);
            double fi=ay.getSensibleTickStart(f);
            int maxLabelLength=0;
            while (fi<ay.vBegin+ay.vLen) {
                final String s = ay.getDisplayableValue(fi);
                if(s.length()>maxLabelLength) maxLabelLength=s.length();
                fi+=f;
            }
            return adjustMargin(maxLabelLength);
        }
        return false;
    }
    
    protected boolean adjustMargin(int maxLabelLength){
        maxLabelLength*=1.5;
        final int omLeft=mLeft;
        if(maxLabelLength*8>20){
            mLeft = maxLabelLength*8+2;
        } else mLeft=20;
        return (mLeft!=omLeft);
    }
}
