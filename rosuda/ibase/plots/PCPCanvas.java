package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.PrintStream;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.ibase.toolkit.PGSCanvas.IDlgCL;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** implementation of line plot
 * @version $Id$
 */
public class PCPCanvas extends BaseCanvas {
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
    
    int x1, y1, x2, y2;
    int dragAxis=-1;
    
    MenuItem MIlabels=null;
    MenuItem MIdots=null;
    MenuItem MIaxes=null;
    MenuItem MIlines=null;
    MenuItem MItrigraph=null;
    MenuItem MInodeSizeUp=null;
    MenuItem MInodeSizeDown=null;
    MenuItem MIhideNAlines=null;
    
    int nodeSize=2;
    
    int X,Y,W,H, TW,TH;
    double totMin, totMax;
    
    int leftGap=7,rightGap=7;
    
    private final int standardMLeft=30;
    
    /** create a new PCP
     * @param f associated frame (or <code>null</code> if none)
     * @param yvs list of variables
     * @param mark associated marker */
    public PCPCanvas(PlotComponent ppc, Frame f, SVar[] yvs, SMarker mark) {
        super(ppc,f,mark);
        
        allowDragMove=true;
        objectClipping=true;
        
        mBottom=30;
        mLeft=standardMLeft;
        mRight=mTop=10;
        
        v=new SVar[yvs.length+1];
        opAy=new Axis[yvs.length-1];
        int i=0;
        SVar xv=new SVarObj("PCP.index",true);
        while(i<yvs.length) {
            if (yvs[i].isNum()) {
                if (i==0) {
                    totMin=yvs[i].getMin(); totMax=yvs[i].getMax();
                } else {
                    if (yvs[i].getMin()<totMin) totMin=yvs[i].getMin();
                    if (yvs[i].getMax()>totMax) totMax=yvs[i].getMax();
                };
            }
            xv.add(yvs[i].getName());
            v[i+1]=yvs[i]; i++;
        };
        v[0]=xv; ax=new Axis(v[0],Axis.O_X,v[0].isCat()?Axis.T_EqCat:Axis.T_Num); ax.addDepend(this);
        ay=new Axis(yvs[0],Axis.O_Y,Axis.T_Num); ay.addDepend(this);
        ay.setValueRange(totMin-(totMax-totMin)/20,(totMax-totMin)*1.1);
        pc.setBackground(Common.backgroundColor);
        MenuBar mb=null;
        String myMenu[]={"+","File","~File.Graph","~Edit","-","Set Colors (CB)","set1","Set Colors (rainbow)","set64","Clear Colors","reset","+","View","@LHide labels","labels","@TShorten lables","trigraph","Show dots","togglePts","Increase dot size (up)","nodeSizeUp","Decrease dot size (down)","nodeSizeDown","Show axes","toggleAxes","Hide lines","toggleLines","@NHide NA lines","hideNAlines","-","@CIndividual scales","common","-","Set Y Range ...","YrangeDlg","!SShow scale dialog","scaleDlg","-","More transparent (left)","alphaDown","More opaque (right)","alphaUp","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        MIlabels=EzMenu.getItem(f,"labels");
        MIdots=EzMenu.getItem(f,"togglePts");
        MIaxes=EzMenu.getItem(f,"toggleAxes");
        MIlines=EzMenu.getItem(f,"toggleLines");
        MIlines.setEnabled(false);
        MItrigraph=EzMenu.getItem(f, "trigraph");
        MInodeSizeUp=EzMenu.getItem(f, "nodeSizeUp");
        MInodeSizeUp.setEnabled(false);
        MInodeSizeDown=EzMenu.getItem(f, "nodeSizeDown");
        MInodeSizeUp.setEnabled(false);
        MIhideNAlines=EzMenu.getItem(f,"hideNAlines");
        dontPaint=false;
    }
    
    public void setCommonScale(boolean cs) {
        if (cs==commonScale) return;
        commonScale=cs;
        EzMenu.getItem(getFrame(),"common").setLabel(cs?"Individual scales":"Common scale");
        EzMenu.getItem(getFrame(),"YrangeDlg").setEnabled(cs);
        if (cs) {
            ay.setValueRange(totMin,totMax-totMin);
            //TODO: notify!
            updateObjects();
            setUpdateRoot(0); repaint();
            return;
        }
        if (opAy[0]==null) {
            Dimension Dsize=pc.getSize();
            int w=Dsize.width, h=Dsize.height;
            int lshift=0;
            int innerW=w-mLeft-mRight, innerH=h-mBottom-mTop;
            
            int i=0;
            while (i<opAy.length) {
                opAy[i]=new Axis(v[i+2],Axis.O_Y,v[i+2].isCat()?Axis.T_EqCat:Axis.T_Num);
                opAy[i].addDepend(this);
                i++;
            }
            
            updateGeometry=true;
        };
        ay.setDefaultRange();
        updateObjects();
        setUpdateRoot(0); repaint();
    }
    
    public void paintBack(PoGraSS g){
        Rectangle r=pc.getBounds();
        g.setBounds(r.width,r.height);
        g.begin();
        g.defineColor("axis",192,192,192);
        
        if (commonScale) {
            /* determine maximal label length */
            int maxLabelLength=0;
            double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                String s=v[1].isCat()?Common.getTriGraph(v[1].getCatAt((int)fi).toString()):ay.getDisplayableValue(fi);
                if(s.length()>maxLabelLength) maxLabelLength=s.length();
                fi+=f;
            };
            
            int omLeft=mLeft;
            if(maxLabelLength*8>standardMLeft){
                mLeft = maxLabelLength*8+2;
            } else mLeft=standardMLeft;
            if(mLeft!=omLeft) updateObjects();
        }
        
        Dimension Dsize=pc.getSize();
        if (Dsize.width!=TW || Dsize.height!=TH) {
            int w=Dsize.width, h=Dsize.height;
            TW=w; TH=h;
            int lshift=0;
            int innerW=w-mLeft-mRight, innerH=h-mBottom-mTop;
            
            /*int i=1;
            while (i<A.length) {
                if (A[i]!=null)
                    A[i].setGeometry(Axis.O_Y,mBottom+xtraShift,(H=innerH)); //-xtraShift
                i++;
            }*/
            Y=TH-mBottom-innerH;
        };
        
        if (TW<50||TH<50) {
            g.setColor("red");
            g.drawLine(0,0,TW,TH);
            g.drawLine(0,TH,TW,0);
            return;
        };
        
        labels.clear();
        /* draw ticks and labels for X axis */
        {
            double f=ax.getSensibleTickDistance(50,26);
            double fi=ax.getSensibleTickStart(f);
            double[] vX=new double[v[0].size()];
            double[] vY=new double[v[0].size()];
            double[] vaX=new double[v[0].size()];
            double[] vaY=new double[v[0].size()];
            String[] vtext=new String[v[0].size()];
            while (fi<ax.vBegin+ax.vLen) {
                int t=ax.getValuePos(fi);
                if (isShowLabels()){
                    labels.add(t-5, TH-Y-H-10, v[0].isCat()?((useX3)?Common.getTriGraph(v[0].getCatAt((int)fi).toString()):
                        v[0].getCatAt((int)fi).toString()):ax.getDisplayableValue(fi));
                }
                fi+=f;
            };
            int b = pc.getSize().height-mBottom;
            g.drawLine(mLeft, b, pc.getSize().width-mRight, b);
        }
        
        /* draw ticks and labels for Y axis */
        if (commonScale) {
            double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                int t=ay.getValuePos(fi);
                g.drawLine(mLeft-2,t,mLeft,t);
                if(isShowLabels())
                    labels.add(mLeft-3,(t+5),1,0, v[1].isCat()?Common.getTriGraph(v[1].getCatAt((int)fi).toString()):ay.getDisplayableValue(fi));
                fi+=f;
            };
            g.drawLine(mLeft, mTop, mLeft, pc.getSize().height-mBottom);
        }
        
        labels.finishAdd();
        
        if (drawAxes) {
            g.setColor("axis");
            int xx=0;
            while (xx<v[0].getNumCats()) {
                int t=ax.getRegularCatPos(xx++, leftGap, rightGap);
                g.drawLine(t,mTop,t,pc.getSize().height-mTop-mBottom);
            }
        }
    };
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_RIGHT) run(this, "alphaUp");
        if (e.getKeyCode()==KeyEvent.VK_LEFT) run(this, "alphaDown");
        if (e.getKeyCode()==KeyEvent.VK_UP) run(this, "nodeSizeUp");
        if (e.getKeyCode()==KeyEvent.VK_DOWN) run(this,"nodeSizeDown");
    }
    
    public Object run(Object o, String cmd) {
        if (cmd=="print") { drawHidden=false; run(o,"exportPS"); drawHidden=true; return this; }
        super.run(o,cmd);
        if (cmd=="labels") {
            setShowLabels(!isShowLabels());
            MIlabels.setLabel((isShowLabels())?"Hide labels":"Show labels");
            setUpdateRoot(0);
            repaint();
        };
        if (cmd=="alphaDown") {
            ppAlpha-=(ppAlpha>0.2)?0.10:0.02; if (ppAlpha<0.05f) ppAlpha=0.05f;
            setUpdateRoot(0); repaint();
        }
        if (cmd=="alphaUp") {
            ppAlpha+=(ppAlpha>0.2)?0.10:0.02; if (ppAlpha>1f) ppAlpha=1f;
            setUpdateRoot(0); repaint();
        }
        if (cmd=="exit") WinTracker.current.Exit();
        if (cmd=="common") { setCommonScale(!commonScale); }
        if (cmd=="trigraph") {
            useX3=!useX3;
            MItrigraph.setLabel(useX3?"Extend labels":"Shorten labels");
            setUpdateRoot(0); repaint();
        }
        if (cmd=="togglePts") {
            drawPoints=!drawPoints;
            MIdots.setLabel((drawPoints)?"Hide dots":"Show dots");
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
        if (cmd=="toggleLines") {
            drawLines=!drawLines;
            MIlines.setLabel((drawLines)?"Hide lines":"Show lines");
            for(int i=0; i<pp.length; i++){
                ((PPrimPolygon)pp[i]).drawBorder=drawLines;
            }
            MIdots.setEnabled(!drawPoints||drawLines);
            MIlines.setEnabled(drawPoints||!drawLines);
            setUpdateRoot(0);
            repaint();
        }
        if (cmd=="toggleAxes") {
            drawAxes=!drawAxes;
            MIaxes.setLabel((drawAxes)?"Hide axes":"Show axes");
            setUpdateRoot(0); repaint();
        }
        if (cmd=="YrangeDlg" || cmd=="XrangeDlg") {
            Axis rt=(cmd=="YrangeDlg")?ay:ax;
            Dialog d=intDlg=new Dialog(myFrame,(rt==ay)?"Y range":"X range",true);
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
            TextField tw=new TextField(""+rt.vBegin,6);
            TextField th=new TextField(""+(rt.vBegin+rt.vLen),6);
            cp.add(tw);
            cp.add(new Label(", end: "));
            cp.add(th);
            d.pack();
            b.addActionListener(ic);b2.addActionListener(ic);
            d.setVisible(true);
            if (!cancel) {
                double w=Tools.parseDouble(tw.getText());
                double h=Tools.parseDouble(th.getText());
                rt.setValueRange(w,h-w);
                setUpdateRoot(0);
                repaint();
            }
            d.dispose();
            updateGeometry=true;
        }
        if (cmd=="scaleDlg" && commonScale) {
            RespDialog d=new RespDialog(myFrame,"Set y scale",true,RespDialog.okCancel);
            Panel cp=d.getContentPanel();
            cp.add(new Label("begin: "));
            TextField tw=new TextField(""+ay.vBegin,6);
            TextField th=new TextField(""+(ay.vBegin+ay.vLen),6);
            cp.add(tw);
            cp.add(new Label(", end: "));
            cp.add(th);
            d.pack();
            d.setVisible(true);
            if (!cancel) {
                double vb=Tools.parseDouble(tw.getText());
                double ve=Tools.parseDouble(th.getText());
                if (ve-vb>0) ay.setValueRange(vb,ve-vb);
                if (myFrame!=null) myFrame.pack();
            }
            d.dispose();
            updateGeometry=true;
        }
        if (cmd=="set1") {
            if (pp!=null && pp.length>0) {
                int i=0;
                while (i<pp.length) {
                    int cs[] = ((PPrimBase)pp[ax.getCatAtSeqIndex(i)]).getCaseIDs();
                    int j=0;
                    if (cs!=null)
                        while (j<cs.length)
                            m.setSec(cs[j++],i+16);
                    i++;
                }
                m.NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
            }
        }
        if (cmd=="set64") {
            if (pp!=null && pp.length>0) {
                int i=0;
                while (i<pp.length) {
                    //System.out.println("set64: "+i+" (of "+pp.length+") mapped to "+ax.getCatAtSeqIndex(i)+", pp="+pp[i]);
                    int cs[] = ((PPrimBase)pp[ax.getCatAtSeqIndex(i)]).getCaseIDs();
                    int j=0;
                    if (cs!=null)
                        while (j<cs.length)
                            m.setSec(cs[j++],64+(64*i/pp.length));
                    i++;
                }
                m.NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
            }
        }
        if (cmd=="reset") {
            if (m.getSecCount()>0) {
                m.resetSec();
                m.NotifyAll(new NotifyMsg(this,Common.NM_MarkerChange));
            }
        }
        if (cmd=="nodeSizeUp") {
            if(pp[0]!=null){
                nodeSize++;
                for(int i=0; i<pp.length; i++)
                    if(pp[i]!=null)
                        ((PPrimPolygon)pp[i]).setNodeSize(nodeSize);
                setUpdateRoot(0); repaint();
            }
        };
        if (cmd=="nodeSizeDown") {
            if(pp[0]!=null){
                nodeSize--;
                for(int i=0; i<pp.length; i++)
                    if(pp[i]!=null)
                        ((PPrimPolygon)pp[i]).setNodeSize(nodeSize);
                setUpdateRoot(0); repaint();
            }
        };
        if (cmd=="hideNAlines"){
            drawNAlines=!drawNAlines;
            for(int i=0; i<pp.length; i++){
                if(pp[i]!=null) ((PPrimPolygon)pp[i]).showInvisibleLines = drawNAlines;
            }
            MIhideNAlines.setLabel(drawNAlines?"Hide NA lines":"Show NA lines");
            setUpdateRoot(0); repaint();
        }
        
        return null;
    }
    
    public void updateObjects() {
        
        if (pp==null || pp.length!=v[1].size()) {
            pp=new PlotPrimitive[v[1].size()];
        }
        
        TW = pc.getSize().width;
        TH = pc.getSize().height;
        
        boolean isZ=false;
        int[][] xs = new int[v[1].size()][v.length-1];
        int[][] ys = new int[v[1].size()][v.length-1];
        //boolean[] na = new boolean[v[1].size()];
        int[][] na = new int[v[1].size()][];
        int[] naIndices = new int[v.length];
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
            ((PPrimPolygon)pp[j]).selectByCorners=true;
            ((PPrimPolygon)pp[j]).drawCorners = drawPoints;
            ((PPrimPolygon)pp[j]).ref = new int[] {j};
            ((PPrimPolygon)pp[j]).setNodeSize(nodeSize);
            ((PPrimPolygon)pp[j]).showInvisibleLines=drawNAlines;
            boolean[] nas = new boolean[xs[j].length];
            boolean[] gap = new boolean[xs[j].length];
            
            if(na[j]!=null){
                boolean[] nod = new boolean[xs[j].length];
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
        }
    }
    
    public SVar getData(int id) { return (id>=0 && id<v.length-1)?v[id+1]:null; }
    
    public void mousePressed(MouseEvent ev) {
        super.mousePressed(ev);
        int x=ev.getX(), y=ev.getY();
        Common.printEvent(ev);
        
        if (Common.isMoveTrigger(ev)) {
            dragAxis = labels.getTextAt(x,y);
            if(dragAxis>-1){
                pc.setCursor(Common.cur_move);
            }
        }
    };
    
    public void mouseReleased(MouseEvent e) {
        if (baseDrag && moveDrag) {
            int pos = (orientation==0)?e.getX():e.getY();
            int dragNew = ax.getCatByPos(pos);
            int difference;
            int myX1=ax.getCatLow(dragNew);
            int myX2=ax.getCatUp(dragNew);
            if(Math.abs(difference=pos-ax.getRegularCatPos(dragNew, leftGap, rightGap)) > (myX2-myX1)/4){
                int newPos=ax.getCatSeqIndex(dragNew);
                if(difference>0) newPos += 1;
                if(dragAxis<newPos) newPos -=1;
                ax.moveCat(dragAxis, newPos);
            } else{
                if(orientation==0) ax.swapCats(dragNew, ax.getCatByPos(baseDragX1));
                else ax.swapCats(dragNew, ax.getCatByPos(baseDragY1));
            }
            
            baseDrag=false;
            updateObjects();
            setUpdateRoot(0);
            repaint();
        } else super.mouseReleased(e);
    }
    
    public void paintPost(PoGraSS g) {
        if(baseDrag && moveDrag){
            int basey=pc.getBounds().height-mBottom;
            int pos = (orientation==0)?baseDragX2:baseDragY2;
            int dragNew = ax.getCatByPos(pos);
            int myX1=ax.getCatLow(dragNew);
            int myX2=ax.getCatUp(dragNew);
            int difference;
            if(Math.abs(difference=pos-ax.getRegularCatPos(dragNew, leftGap, rightGap)) > (myX2-myX1)/4){
                int x,w;
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
    
    public void performZoomIn(int x1, int y1, int x2, int y2) {
        if(commonScale) super.performZoomIn(x1, y1, x2, y2, null,ay);
        else{
            int minZoomAxis=0;
            int maxZoomAxis=v.length-2;
            
            while(ax.getRegularCatPos(ax.getCatAtSeqIndex(minZoomAxis), leftGap, rightGap) < x1) minZoomAxis++;
            while(ax.getRegularCatPos(ax.getCatAtSeqIndex(maxZoomAxis), leftGap, rightGap) > x2) maxZoomAxis--;
            
            dontPaint=true;
            for(int i=minZoomAxis; i<=maxZoomAxis; i++){
                int csi=ax.getCatAtSeqIndex(i);
                super.performZoomIn(x1, y1, x2, y2, null, (csi==0)?ay:opAy[csi-1]);
            }
            dontPaint=false;
        }
    }
    
    
};