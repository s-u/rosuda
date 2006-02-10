package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.ibase.toolkit.PGSCanvas.IDlgCL;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** implementation of line plot
 * @version $Id$
 */
public class PCPCanvas extends BaseCanvas {
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
    static final String M_AXIS = "axis";
    static final String M_TRANSHIGHL = "transparentHighlighting";
    /** variables; 0=x, 1,2,3...=Y */
    SVar v[];
    
    
    /** use trigraph for X axis in case X is categorical */
    boolean useX3=false;
    
    boolean drawPoints=false;
    boolean drawAxes=false;
    boolean drawLines=true;
    boolean drawNAlines=true;
    
    boolean commonScale=true;
    boolean drawHidden=true; // if true then hidden lines are drawn (default because if set to false, one more layer has to be updated all the time; export functions may want to set it to false for output omtimization)
    
    MenuItem MIlabels=null;
    MenuItem MIdots=null;
    MenuItem MIaxes=null;
    MenuItem MIlines=null;
    MenuItem MItrigraph=null;
    MenuItem MInodeSizeUp=null;
    MenuItem MInodeSizeDown=null;
    MenuItem MIhideNAlines=null;
    protected MenuItem MItransHighl=null;
    
    int nodeSize=2;
    
    int X,Y, TW,TH;
    double totMin, totMax;
    
    int leftGap=7,rightGap=7;
    
    private final int standardMLeft=30;
    
    /** create a new PCP
     * @param f associated frame (or <code>null</code> if none)
     * @param yvs list of variables
     * @param mark associated marker */
    public PCPCanvas(final PlotComponent ppc, final Frame f, final SVar[] yvs, final SMarker mark) {
        super(ppc,f,mark);
        
        allowDragMove=true;
        objectClipping=true;
        
        mBottom=mTop=20;
        mLeft=standardMLeft;
        mRight=10;
        
        v=new SVar[yvs.length+1];
        opAy=new Axis[yvs.length-1];
        int i=0;
        final SVar xv=new SVarObj("PCP.index",true);
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
            v[i+1]=yvs[i]; i++;
        }
        v[0]=xv; ax=new Axis(v[0],Axis.O_X,v[0].isCat()?Axis.T_EqCat:Axis.T_Num); ax.addDepend(this);
        ay=new Axis(yvs[0],Axis.O_Y,Axis.T_Num); ay.addDepend(this);
        ay.setValueRange(totMin-(totMax-totMin)/20,(totMax-totMin)*1.1);
        pc.setBackground(Common.backgroundColor);
        
        final String myMenu[]={M_PLUS,"File","~File.Graph","~Edit",M_MINUS,"Set Colors (CB)",M_SET1,"Set Colors (rainbow)",M_SET64,"Clear Colors",M_RESET,M_PLUS,"View","@HReset zoom",M_RESETZOOM,"@LHide labels",M_LABELS,"@TShorten lables",M_TRIGRAPH,M_SHOWDOTS,M_TOGGLEPTS,"Increase dot size (up)",M_NODESIZEUP,"Decrease dot size (down)",M_NODESIZEDOWN,M_SHOWAXES,M_TOGGLEAXES,M_HIDELINES,M_TOGGLELINES,"@NHide NA lines",M_HIDENALINES,M_MINUS,"Individual scales",M_COMMON,M_MINUS,"Set Y Range ...",M_YRANGEDLG,"!SShow scale dialog",M_SCALEDLG,M_MINUS,"More transparent (left)",M_ALPHADOWN,"More opaque (right)",M_ALPHAUP,"Transparent highlighting",M_TRANSHIGHL,"~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
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
        dontPaint=false;
    }
    
    public void setCommonScale(final boolean cs) {
        if(cs==commonScale) return;
        commonScale=cs;
        EzMenu.getItem(getFrame(),M_COMMON).setLabel(cs?"Individual scales":"Common scale");
        EzMenu.getItem(getFrame(),M_YRANGEDLG).setEnabled(cs);
        if (cs) {
            ay.setValueRange(totMin,totMax-totMin);
            //TODO: notify!
            updateObjects();
            setUpdateRoot(0); repaint();
            return;
        }
        if (opAy[0]==null) {
            
            int i=0;
            while (i<opAy.length) {
                opAy[i]=new Axis(v[i+2],Axis.O_Y,v[i+2].isCat()?Axis.T_EqCat:Axis.T_Num);
                opAy[i].addDepend(this);
                i++;
            }
            
            updateGeometry=true;
        }
        ay.setDefaultRange();
        updateObjects();
        setUpdateRoot(0); repaint();
    }
    
    public void paintBack(final PoGraSS g){
        final Rectangle r=pc.getBounds();
        g.setBounds(r.width,r.height);
        g.begin();
        g.defineColor(M_AXIS,192,192,192);
        
        if (commonScale) {
            /* determine maximal label length */
            int maxLabelLength=0;
            final double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                final String s=v[1].isCat()?Common.getTriGraph(v[1].getCatAt((int)fi).toString()):ay.getDisplayableValue(fi);
                if(s.length()>maxLabelLength) maxLabelLength=s.length();
                fi+=f;
            }
            
            final int omLeft=mLeft;
            if(maxLabelLength*8>standardMLeft){
                mLeft = maxLabelLength*8+2;
            } else mLeft=standardMLeft;
            if(mLeft!=omLeft) updateObjects();
        }
        
        final Dimension Dsize=pc.getSize();
        if (Dsize.width!=TW || Dsize.height!=TH) {
            final int w=Dsize.width;
            final int h=Dsize.height;
            TW=w; TH=h;
            
            
            final int innerH = h-mBottom-mTop;
            
            /*int i=1;
            while (i<A.length) {
                if (A[i]!=null)
                    A[i].setGeometry(Axis.O_Y,mBottom+xtraShift,(H=innerH)); //-xtraShift
                i++;
            }*/
            Y=TH-mBottom-innerH;
        }
        
        if (TW<50||TH<50) {
            g.setColor("red");
            g.drawLine(0,0,TW,TH);
            g.drawLine(0,TH,TW,0);
            return;
        }
        
        labels.clear();
        /* draw ticks and labels for X axis */
        {
            final double f=ax.getSensibleTickDistance(50,26);
            double fi=ax.getSensibleTickStart(f);
            
            final int[] valuePoss = new int[(int)((ax.vBegin+ax.vLen-fi)/f)+5];
            final String[] labs = new String[(int)((ax.vBegin+ax.vLen-fi)/f)+5];
            int i=0;
            while (fi<ax.vBegin+ax.vLen) {
                valuePoss[i] = ax.getValuePos(fi);
                labs[i] = v[0].isCat()?((useX3)?Common.getTriGraph(v[0].getCatAt((int)fi).toString()):
                    v[0].getCatAt((int)fi).toString()):ax.getDisplayableValue(fi);
                fi+=f;
                i++;
            }
            
            for(i=0; i<valuePoss.length; i++) {
                if (isShowLabels() && labs[i]!=null){
                    labels.add(valuePoss[i]-5,
                            ((i&1)==0)?(H-mBottom+2):(mTop-5),
                            0.5,
                            ((i&1)==0)?1:0,
                            (i==0)?(2*(valuePoss[1]-valuePoss[0])):((i==valuePoss.length-1)?(2+(valuePoss[i]-valuePoss[i-1])):(valuePoss[i+1]-valuePoss[i-1])),
                            labs[i]);
                }
            }
            final int b = pc.getSize().height-mBottom;
            g.drawLine(mLeft, b, pc.getSize().width-mRight, b);
            g.drawLine(mLeft, mTop, pc.getSize().width-mRight, mTop);
            
            int xx=0;
            while (xx<v[0].getNumCats()) {
                final int t=ax.getRegularCatPos(xx, leftGap, rightGap);
                if((xx&1)==0) g.drawLine(t,b,t,b+2);
                else g.drawLine(t,mTop,t,mTop-2);
                xx++;
            }
        }
        
        /* draw ticks and labels for Y axis */
        if (commonScale) {
            final double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                final int t=ay.getValuePos(fi);
                g.drawLine(mLeft-2,t,mLeft,t);
                if(isShowLabels())
                    labels.add(mLeft-3,(t+5),1,0, v[1].isCat()?Common.getTriGraph(v[1].getCatAt((int)fi).toString()):ay.getDisplayableValue(fi));
                fi+=f;
            }
            g.drawLine(mLeft, mTop, mLeft, pc.getSize().height-mBottom);
        }
        
        labels.finishAdd();
        
        if (drawAxes) {
            g.setColor(M_AXIS);
            int xx=0;
            while (xx<v[0].getNumCats()) {
                final int t=ax.getRegularCatPos(xx++, leftGap, rightGap);
                g.drawLine(t,mTop,t,pc.getSize().height-mTop-mBottom);
            }
        }
    };
    
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
        if (M_COMMON.equals(cmd)) { setCommonScale(!commonScale); }
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
    
    public void updateObjects() {
        
        if (pp==null || pp.length!=v[1].size()) {
            pp=new PlotPrimitive[v[1].size()];
        }
        
        TW = pc.getSize().width;
        TH = pc.getSize().height;
        
        
        final int[][] xs = new int[v[1].size()][v.length-1];
        final int[][] ys = new int[v[1].size()][v.length-1];
        //boolean[] na = new boolean[v[1].size()];
        final int[][] na = new int[v[1].size()][];
        final int[] naIndices = new int[v.length];
        for (int i=0;i<v[1].size();i++){
            int numNAs=0;
            for (int j=0;j<v.length-1;j++){
                if ((drawHidden || !m.at(i)) && (v[j+1].at(i)!=null)) {
                    xs[i][ax.getCatSeqIndex(j)] = ax.getRegularCatPos(j, leftGap, rightGap);
                    ys[i][ax.getCatSeqIndex(j)] = ((commonScale||j==0)?ay:opAy[j-1]).getValuePos(v[j+1].atD(i));
                } else{
                    xs[i][ax.getCatSeqIndex(j)] = ax.getRegularCatPos(j, leftGap, rightGap);
                    ys[i][ax.getCatSeqIndex(j)] = ((commonScale||j==0)?ay:opAy[j-1]).getValuePos(v[j+1].atD(i));
                    naIndices[numNAs++] = j;
                }
            }
            if(numNAs>0){
                na[i] = new int[numNAs];
                System.arraycopy(naIndices, 0, na[i], 0, numNAs);
            }
        }
        
        for(int j=0; j<xs.length; j++){
            pp[j] = new PPrimPolygon();
            ((PPrimPolygon)pp[j]).pg = new Polygon(xs[j], ys[j], xs[j].length);
            ((PPrimPolygon)pp[j]).closed=false;
            ((PPrimPolygon)pp[j]).fill=false;
            ((PPrimPolygon)pp[j]).selectByCorners=!drawLines;
            ((PPrimPolygon)pp[j]).drawCorners = drawPoints;
            ((PPrimPolygon)pp[j]).ref = new int[] {j};
            ((PPrimPolygon)pp[j]).setNodeSize(nodeSize);
            ((PPrimPolygon)pp[j]).drawBorder=drawLines;
            ((PPrimPolygon)pp[j]).showInvisibleLines=drawNAlines;
            final boolean[] nas = new boolean[xs[j].length];
            final boolean[] gap = new boolean[xs[j].length];
            
            if(na[j]!=null){
                final boolean[] nod = new boolean[xs[j].length];
                for(int i=0; i<na[j].length; i++) {
                    nas[na[j][i]]=true;
                    if(na[j][i]>0) nas[na[j][i]-1]=true;
                    nod[na[j][i]]=true;
                }
                ((PPrimPolygon)pp[j]).noDotsAt = nod;
                for(int i=0; i<na[j].length-1; i++){
                    if(na[j][i+1]-na[j][i]==2) gap[na[j][i]+1]=true;
                }
                if(na[j][0]==1) gap[0]=true;
                if(na[j][na[j].length-1]==gap.length-2) gap[gap.length-1]=true;
            }
            ((PPrimPolygon)pp[j]).invisibleLines=nas;
            ((PPrimPolygon)pp[j]).gapDots=gap;
            ((PPrimPolygon)pp[j]).setNoInterior();
        }
    }
    
    public SVar getData(final int id) { return (id>=0 && id<v.length-1)?v[id+1]:null; }
    
    public void mousePressed(final MouseEvent ev) {
        super.mousePressed(ev);
        
        
        Common.printEvent(ev);
        
        /*if (Common.isMoveTrigger(ev)) {
            dragAxis = labels.getTextAt(x,y);
            if(dragAxis>-1){
                pc.setCursor(Common.cur_move);
            }
        }*/
    };
    
    public void mouseReleased(final MouseEvent e) {
        if (baseDrag && moveDrag) {
            final int pos = (orientation==0)?e.getX():e.getY();
            final int dragNew = ax.getCatByPos(pos);
            final int dragAxis = ax.getCatByPos(baseDragY1);
            final int difference;
            final int myX1=ax.getCatLow(dragNew);
            final int myX2=ax.getCatUp(dragNew);
            if(Math.abs(difference=pos-ax.getRegularCatPos(dragNew, leftGap, rightGap)) > (myX2-myX1)/4){
                int newPos=ax.getCatSeqIndex(dragNew);
                if(difference>0) newPos += 1;
                if(dragAxis<newPos) newPos -=1;
                ax.moveCat(dragAxis, newPos);
            } else{
                if(orientation==0) ax.swapCats(dragNew, ax.getCatByPos(baseDragX1));
                else ax.swapCats(dragNew, dragAxis);
            }
            
            baseDrag=false;
            updateObjects();
            setUpdateRoot(0);
            repaint();
        } else super.mouseReleased(e);
    }
    
    public void paintPost(final PoGraSS g) {
        if(baseDrag && moveDrag){
            final int basey=pc.getBounds().height-mBottom;
            final int pos = (orientation==0)?baseDragX2:baseDragY2;
            final int dragNew = ax.getCatByPos(pos);
            final int myX1=ax.getCatLow(dragNew);
            final int myX2=ax.getCatUp(dragNew);
            final int difference;
            if(Math.abs(difference=pos-ax.getRegularCatPos(dragNew, leftGap, rightGap)) > (myX2-myX1)/4){
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
        }
        super.paintPost(g);
    }
    
    public void performZoomIn(final int x1, final int y1, final int x2, final int y2) {
        if(commonScale) super.performZoomIn(x1, y1, x2, y2, null,ay);
        else{
            int minZoomAxis=0;
            int maxZoomAxis=v.length-2;
            
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
                opAy[i]=new Axis(v[i+2],Axis.O_Y,v[i+2].isCat()?Axis.T_EqCat:Axis.T_Num);
                opAy[i].addDepend(this);
                i++;
            }
            
            updateGeometry=true;
            ay.setDefaultRange();
            updateObjects();
            setUpdateRoot(0); repaint();
        }
    }
    
    public String queryObject(PlotPrimitive p) {
        
        String retValue="";
        
        for(int i=1; i<v.length; i++){
            retValue += v[i].getName() + ": ";
            if(v[i].isCat()){
                    retValue += v[i].getCatAt((int)((commonScale||i==1)?ay:opAy[i-2]).getValueForPos(((PPrimPolygon)p).pg.ypoints[i-1])) + "\n";
            } else{
                    retValue += ((commonScale||i==1)?ay:opAy[i-2]).getValueForPos(((PPrimPolygon)p).pg.ypoints[i-1]) + "\n";
            }
            
        }
        
        return retValue;
    }
    
    public String queryObject(int i) {
        return queryObject(pp[i]);
    }
};