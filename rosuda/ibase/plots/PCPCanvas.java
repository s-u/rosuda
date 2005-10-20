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
    
    /** is NA represented as 0? (false=NA's are not drawn at all) */
    boolean na0=true;
    
    boolean drawPoints=false;
    boolean drawAxes=false;
    
    int xtraShift=0; // hack,hack,hack - this allows individual scales to be move up a bit to prevent clutter on the y axis
    int nodeSize=3;
    boolean showResidLines=false;
    
    /** array of axes */
    Axis A[];
    
    boolean commonScale=true;
    boolean drawHidden=true; // if true then hidden lines are drawn (default because if set to false, one more layer has to be updated all the time; export functions may want to set it to false for output omtimization)
    
    int x1, y1, x2, y2;
    
    MenuItem MIlabels=null;
    
    int X,Y,W,H, TW,TH;
    double totMin, totMax;
    /** create a new PCP
     * @param f associated frame (or <code>null</code> if none)
     * @param yvs list of variables
     * @param mark associated marker */
    public PCPCanvas(Frame f, SVar[] yvs, SMarker mark) {
        super(f,mark);
        
        mBottom=30;
        mLeft=mRight=mTop=10;
        
        v=new SVar[yvs.length+1];
        A=new Axis[yvs.length-1];
        opAy = A;
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
        ay=new Axis(yvs[0],Axis.O_Y,Axis.T_Num); ay.addDepend(this);
        ay.setValueRange(totMin-(totMax-totMin)/20,(totMax-totMin)*1.1);
        v[0]=xv; ax=new Axis(v[0],Axis.O_X,v[0].isCat()?Axis.T_EqCat:Axis.T_Num); ax.addDepend(this);
        setBackground(Common.backgroundColor);
        baseDrag=false;
        MenuBar mb=null;
        String myMenu[]={"+","File","~File.Graph","~Edit","+","View","Hide labels","labels","Toggle nodes","togglePts","Toggle axes","toggleAxes","-","Individual scales","common","-","Set X Range ...","XrangeDlg","Set Y Range ...","YrangeDlg","-","More transparent (left)","alphaDown","More opaque (right)","alphaUp","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        MIlabels=EzMenu.getItem(f,"labels");
        EzMenu.getItem(getFrame(),"XrangeDlg").setEnabled(false);
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
        if (A[0]==null) {
            Dimension Dsize=getSize();
            int w=Dsize.width, h=Dsize.height;
            int lshift=0;
            int innerW=w-mLeft-mRight, innerH=h-mBottom-mTop;
            
            int i=0;
            while (i<A.length) {
                A[i]=new Axis(v[i+2],Axis.O_Y,v[i+2].isCat()?Axis.T_EqCat:Axis.T_Num);
                A[i].addDepend(this);
                i++;
            }
            
            updateGeometry=true;
        };
        ay.setDefaultRange();
        updateObjects();
        setUpdateRoot(0); repaint();
    }
    
    public Dimension getMinimumSize() { return new Dimension(60,50); };
    
    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        setUpdateRoot((msg.getMessageID()==Common.NM_MarkerChange)?1:0);
        repaint();
    };
    
    public void paintBack(PoGraSS g){
        Rectangle r=getBounds();
        g.setBounds(r.width,r.height);
        g.begin();
        g.defineColor("axis",192,192,192);
        g.defineColor("line",128,128,192); // color of line plot
        g.defineColor("Rlines",96,128,96); // color of the resudual thresholds
        g.defineColor("lines",96,96,255);
        g.defineColor("selText",255,0,0);
        g.defineColor("selBg",255,255,192);
        
        Dimension Dsize=getSize();
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
        
        /* draw ticks and labels for X axis */
        {
            double f=ax.getSensibleTickDistance(50,26);
            double fi=ax.getSensibleTickStart(f);
            while (fi<ax.vBegin+ax.vLen) {
                int t=ax.getValuePos(fi);
                if (showLabels)
                    g.drawString(v[0].isCat()?((useX3)?Common.getTriGraph(v[0].getCatAt((int)fi).toString()):v[0].getCatAt((int)fi).toString()):
                        ax.getDisplayableValue(fi),t-5,TH-Y-H-10,PoGraSS.TA_Center);
                fi+=f;
            };
        }
        
        /* draw ticks and labels for Y axis */
        if (commonScale) {
            double f=ay.getSensibleTickDistance(50,18);
            double fi=ay.getSensibleTickStart(f);
            while (fi<ay.vBegin+ay.vLen) {
                int t=ay.getValuePos(fi);
                g.drawLine(mLeft-2,t,mLeft,t);
                if(showLabels)
                    g.drawString(v[1].isCat()?Common.getTriGraph(v[1].getCatAt((int)fi).toString()):ay.getDisplayableValue(fi),X,(t+5));
                fi+=f;
            };
        }
        
        if (drawAxes) {
            g.setColor("axis");
            int xx=0;
            while (xx<v[0].getNumCats()) {
                int t=ax.getCatCenter(xx++);
                g.drawLine(t,mTop,t,getSize().height-mTop-mBottom);
            }
        }
        
        if (showResidLines) {
            g.setColor("Rlines");
            g.drawLine(X,TH-ay.getValuePos(0.5),X+W,TH-ay.getValuePos(0.5));
            g.drawLine(X,TH-ay.getValuePos(-0.5),X+W,TH-ay.getValuePos(-0.5));
        }
        
        if (baseDrag) {
            g.nextLayer();
            int dx1=ax.clip(x1),dy1=TH-ay.clip(TH-y1),
                    dx2=ax.clip(x2),dy2=TH-ay.clip(TH-y2);
            if (dx1>dx2) { int h=dx1; dx1=dx2; dx2=h; };
            if (dy1>dy2) { int h=dy1; dy1=dy2; dy2=h; };
            g.setColor("aSelBg");
            g.fillRect(dx1,dy1,dx2-dx1,dy2-dy1);
            g.setColor("black");
            g.drawRect(dx1,dy1,dx2-dx1,dy2-dy1);
        };
    };
    
    public void mouseClicked(MouseEvent ev) {
        int x=ev.getX(), y=ev.getY();
        x1=x-2; y1=y-2; x2=x+3; y2=y+3; baseDrag=true; mouseReleased(ev);
    };
    
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar()=='l') run(this,"labels");
        if (e.getKeyChar()=='t') run(this,"trigraph");
        if (e.getKeyChar()=='c') run(this,"common");
        if (e.getKeyChar()=='n') run(this,"toggleNA");
        if (e.getKeyChar()=='S') run(this,"scaleDlg");
        if (e.getKeyChar()=='.') { xtraShift+=5; setUpdateRoot(0); repaint(); }
        if (e.getKeyChar()==',') { xtraShift-=5; if (xtraShift<0) xtraShift=0; setUpdateRoot(0); repaint(); }
        super.keyTyped(e);
    }
    
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode()==KeyEvent.VK_RIGHT) run(this, "alphaUp");
        if (e.getKeyCode()==KeyEvent.VK_LEFT) run(this, "alphaDown");
        if (e.getKeyCode()==KeyEvent.VK_UP) { nodeSize+=1; setUpdateRoot(0); repaint(); };
        if (e.getKeyCode()==KeyEvent.VK_DOWN) { nodeSize-=1; if (nodeSize<3) nodeSize=3; setUpdateRoot(0); repaint(); };
    }
    
    public void keyReleased(KeyEvent e) {}
    
    public Object run(Object o, String cmd) {
        if (cmd=="print") { drawHidden=false; run(o,"exportPS"); drawHidden=true; return this; }
        super.run(o,cmd);
        if (cmd=="labels") {
            showLabels=!showLabels;
            MIlabels.setLabel((showLabels)?"Hide labels":"Show labels");
            setUpdateRoot(0);
            repaint();
        };
        if (cmd=="alphaDown") {
            ppAlpha-=(ppAlpha>0.2)?0.10:0.05; if (ppAlpha<0.5f) ppAlpha=0.05f;
            setUpdateRoot(0); repaint();
        }
        if (cmd=="alphaUp") {
            ppAlpha+=(ppAlpha>0.2)?0.10:0.05; if (ppAlpha>1f) ppAlpha=1f;
            setUpdateRoot(0); repaint();
        }
        if (cmd=="exit") WinTracker.current.Exit();
        if (cmd=="common") { setCommonScale(!commonScale); }
        if (cmd=="trigraph") { useX3=!useX3; setUpdateRoot(0); repaint(); }
        if (cmd=="toggleNA") { na0=!na0; setUpdateRoot(0); repaint(); }
        if (cmd=="togglePts") {
            drawPoints=!drawPoints;
            for(int i=0; i<pp.length; i++){
                ((PPrimPolygon)pp[i]).drawCorners=drawPoints;
            }
            setUpdateRoot(0);
            repaint();
        }
        if (cmd=="toggleAxes") { drawAxes=!drawAxes; setUpdateRoot(0); repaint(); }
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
        }
        
        return null;
    }
    
    public void updateObjects() {
        
        if (pp==null || pp.length!=v[1].size()) {
            pp=new PlotPrimitive[v[1].size()];
        }
        
        TW = getSize().width;
        TH = getSize().height;
        
        boolean isZ=false;
        int pd=(nodeSize>>1);
        int[][] xs = new int[v[1].size()][v.length-1];
        int[][] ys = new int[v[1].size()][v.length-1];
        for (int j=0;j<v.length-1;j++)
            for (int i=0;i<v[1].size();i++)
                if ((drawHidden || !m.at(i)) && (na0 || (v[j-1].at(i)!=null && v[j].at(i)!=null))) {
            xs[i][j] = ax.getCatCenter(j);
            ys[i][j] = ((commonScale||j==0)?ay:A[j-1]).getValuePos(v[j+1].atD(i));
                }
        for(int j=0; j<xs.length; j++){
            pp[j] = new PPrimPolygon();
            ((PPrimPolygon)pp[j]).pg = new Polygon(xs[j], ys[j], xs[j].length);
            ((PPrimPolygon)pp[j]).closed=false;
            ((PPrimPolygon)pp[j]).fill=false;
            ((PPrimPolygon)pp[j]).selectByCorners=true;
            ((PPrimPolygon)pp[j]).drawCorners = drawPoints;
            ((PPrimPolygon)pp[j]).ref = new int[] {j};
        }
    }
    
    public SVar getData(int id) { return (id>=0 && id<v.length-1)?v[id+1]:null; }
};