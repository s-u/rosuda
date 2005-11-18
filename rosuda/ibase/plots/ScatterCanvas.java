package org.rosuda.ibase.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.PrintStream;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/** implementation of scatterplots
 * @version $Id$
 */
public class ScatterCanvas extends BaseCanvas {
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
    
    /** use shading of background according to depth */
    protected boolean shading=false;
    
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
    
    protected int X,Y,W,H, TW,TH;
    
    protected int []filter=null;
    
    protected boolean querying=false;
    protected int qx,qy;
    
    protected boolean zoomRetainsAspect=false;
    
    /** create a new scatterplot
     * @param f associated frame (or <code>null</code> if none)
     * @param v1 variable 1
     * @param v2 variable 2
     * @param mark associated marker */
    public ScatterCanvas(PlotComponent pc, Frame f, SVar v1, SVar v2, SMarker mark) {
        super(pc,f,mark);
        
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
        String myMenu[]={"+","File","~File.Graph","~Edit","+","View","@RRotate","rotate","@0Reset zoom","resetZoom","Same scale","equiscale","-","Hide labels","labels","Change background","nextBg","Toggle jittering","jitter","Toggle stacking","stackjitter","Toggle shading","shading","-","Set X Range ...","XrangeDlg","Set Y Range ...","YrangeDlg","-","Bigger points (up)","points+","Smaller points (down)","points-","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        MIlabels=EzMenu.getItem(f,"labels");
        if (!v1.isCat() && !v2.isCat())
            EzMenu.getItem(f,"jitter").setEnabled(false);
        if (Global.AppType==Common.AT_Framework)
            EzMenu.getItem(f,"shading").setEnabled(false);
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
        int innerL=45, innerB=30, lshift=0;
        int innerW=w-innerL-10, innerH=h-innerB-10;
        boolean xcat=v[0].isCat(), ycat=v[1].isCat();
        
        ((orientation==0)?ax:ay).setGeometry(Axis.O_X,X=innerL,W=innerW);
        ((orientation==0)?ay:ax).setGeometry(Axis.O_Y,h-innerB,-(H=innerH));
        Y=TH-innerB-innerH;
        
        hasLeft=hasRight=hasTop=hasBot=false;
        
        pts=v[0].size();
        if (v[1].size()<pts) pts=v[1].size();
        
        pp = new PlotPrimitive[pts];
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
                pp[i]=null;
                int oX = (orientation==0)?x:y;
                int oY = (orientation==0)?y:x;
                if (oX<innerL) hasLeft=true;
                else if (oY<10) hasTop=true;
                else if (oX>w-10) hasRight=true;
                else if (oY>h-innerB) hasBot=true;
                else {
                    if (stackjitter && jitter && v[0].isCat() && i>0) {
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
                    }
                    pp[i]=new PPrimCircle();
                    if(orientation==0){
                        ((PPrimCircle)pp[i]).x = x;
                        ((PPrimCircle)pp[i]).y = y;
                    } else{
                        ((PPrimCircle)pp[i]).x = y;
                        ((PPrimCircle)pp[i]).y = x;
                    }
                    ((PPrimCircle)pp[i]).diam = ptDiam;
                    ((PPrimCircle)pp[i]).ref = new int[] {i};
                }
            } else { // place missings on the other side of the axes
                int x,y;
                if (v[0].isMissingAt(i)) x=innerL-4; else x=jx+ax.getCasePos(i);
                if (v[1].isMissingAt(i)) y=h-innerB+4; else y=jy+ay.getCasePos(i);
                pp[i]=new PPrimCircle();
                if(orientation==0){
                    ((PPrimCircle)pp[i]).x = x;
                    ((PPrimCircle)pp[i]).y = y;
                } else{
                    ((PPrimCircle)pp[i]).x = y;
                    ((PPrimCircle)pp[i]).y = x;
                }
                ((PPrimCircle)pp[i]).diam = ptDiam;
                ((PPrimCircle)pp[i]).ref = new int[] {i};
            }
        };
    };
    
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar()=='l') run(this,"labels");
        if (e.getKeyChar()=='P') run(this,"print");
        if (e.getKeyChar()=='X') run(this,"exportPGS");
        if (e.getKeyChar()=='g') run(this,"nextBg");
        if (e.getKeyChar()=='C') run(this,"exportCases");
        if (e.getKeyChar()=='j') run(this,"jitter");
        if (e.getKeyChar()=='J') run(this,"stackjitter");
        if (e.getKeyChar()=='t') run(this,"trigraph");
        if (e.getKeyChar()=='s') run(this,"shading");
        if (e.getKeyChar()=='.') run(this,"alphaUp");
        if (e.getKeyChar()==',') run(this,"alphaDown");
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
        if (cmd=="shading") {
            shading=!shading; updateObjects(); setUpdateRoot(0); repaint();
        }
        if (cmd=="trigraph") { useX3=!useX3; setUpdateRoot(0); repaint(); }
        if (cmd=="alphaDown") {
            ppAlpha-=(ppAlpha>0.2)?0.10:((ppAlpha>0.1)?0.05:((ppAlpha>0.02)?0.01:0.0025)); if (ppAlpha<0.005f) ppAlpha=0.005f;
            setUpdateRoot(0); repaint();
        }
        if (cmd=="alphaUp") {
            ppAlpha+=(ppAlpha>0.2)?0.10:((ppAlpha>0.1)?0.05:((ppAlpha>0.02)?0.01:0.0025)); if (ppAlpha>1f) ppAlpha=1f;
            setUpdateRoot(0); repaint();
        }
        
        return null;
    }
    
    public void paintBack(PoGraSS g) {
        g.defineColor("objects",Common.objectsColor.getRed(),Common.objectsColor.getGreen(),Common.objectsColor.getBlue());
        g.defineColor("red",255,0,0);
        
        Dimension Dsize=pc.getSize();
        if (Dsize.width!=TW || Dsize.height!=TH)
            updateObjects();
        
        if (TW<50||TH<50) {
            g.setColor("red");
            g.drawLine(0,0,TW,TH);
            g.drawLine(0,TH,TW,0);
            return;
        };
        
        if (fieldBg!=0) {
            g.setColor((fieldBg==1)?"white":"objects");
            g.fillRect(X,Y,W,H);
        }
        
        g.setColor("black");
        g.drawLine(X,Y,X,Y+H);
        g.drawLine(X,Y+H,X+W,Y+H);
        
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
                        g.drawString(v[ori].isCat()?((useX3)?Common.getTriGraph(v[ori].getCatAt((int)(fi+0.5)).toString()):v[ori].getCatAt((int)(fi+0.5)).toString()):
                            axis.getDisplayableValue(fi),t,Y+H+20,0.5,0);
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
                    g.drawLine(X-5,t,X,t);
                    if(showLabels)
                        g.drawString(v[ori].isCat()?Common.getTriGraph(v[ori].getCatAt((int)(fi+0.5)).toString()):axis.getDisplayableValue(fi),X-8,t,1,0.3);
                    fi+=f;
                }
            } catch (Exception pae) { // catch problems (especially in getCatAt being 0)
            }
        }
        
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
        return v[0].getName() + ": " + v[0].atD(i) + "\n"
                + v[1].getName() + ": " + v[1].atD(i);
    }
    
    public void mouseMoved(MouseEvent ev) {
        if (querying) {
            qx=ev.getX(); qy=ev.getY();
            setUpdateRoot(3);
            repaint();
        }
    };
    
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
};