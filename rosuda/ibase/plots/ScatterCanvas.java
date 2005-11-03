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
    
    /** flag whether alternative selection style should be used */
    protected boolean selRed=false;
    
    /** use trigraph for X axis in case X is categorical */
    protected boolean useX3=false;
    
    /** use shading of background according to depth */
    protected boolean shading=false;
    
    /** if true partition nodes above current node only */
    public boolean bgTopOnly=false;
    
    /** diameter of a point */
    public int ptDiam=3;
    public int stackOff=3;
    
    protected float ptAlpha = 1f;
    
    public int fieldBg=0; // 0=none, 1=objects, 2=white
    
    /** array of two axes (X and Y) - note that it is in fact just a copy of ax and ay for
     * compatibility with older implementations */
    protected Axis A[];
    
    /** array of points (in geometrical coordinates) */
    protected Point[] Pts;
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
        
        v=new SVar[2]; A=new Axis[2];
        v[0]=v1; v[1]=v2; m=mark;
        ax=A[0]=new Axis(v1,Axis.O_X,v1.isCat()?Axis.T_EqCat:Axis.T_Num); A[0].addDepend(this);
        ay=A[1]=new Axis(v2,Axis.O_Y,v2.isCat()?Axis.T_EqCat:Axis.T_Num); A[1].addDepend(this);
        if (!v1.isCat()) ax.setValueRange(v1.getMin()-(v1.getMax()-v1.getMin())/20,(v1.getMax()-v1.getMin())*1.1);
        if (!v2.isCat()) ay.setValueRange(v2.getMin()-(v2.getMax()-v2.getMin())/20,(v2.getMax()-v2.getMin())*1.1);
        if (!v1.isCat() && v1.getMax()-v1.getMin()==0) ax.setValueRange(v1.getMin()-0.5,1);
        if (!v2.isCat() && v2.getMax()-v2.getMin()==0) ay.setValueRange(v2.getMin()-0.5,1);
        drag=false;
        MenuBar mb=null;
        if (Global.useAquaBg) fieldBg=2;
        String myMenu[]={"+","File","~File.Graph","~Edit","+","View","!RRotate","rotate","@0Reset zoom","resetZoom","Same scale","equiscale","-","Hide labels","labels","Toggle hilight. style","selRed","Change background","nextBg","Toggle jittering","jitter","Toggle stacking","stackjitter","Toggle shading","shading","-","Set X Range ...","XrangeDlg","Set Y Range ...","YrangeDlg","-","Bigger points (up)","points+","Smaller points (down)","points-","~Window","0"};
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
        SVar h=v[0]; v[0]=v[1]; v[1]=h;
        ay=A[0]; ax=A[0]=A[1]; A[1]=ay;
        try {
            ((Frame) pc.getParent()).setTitle("Scatterplot ("+v[1].getName()+" vs "+v[0].getName()+")");
        } catch (Exception ee) {};
        updateObjects();
        setUpdateRoot(0);
        repaint();
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
        
        A[0].setGeometry(Axis.O_X,X=innerL,W=innerW);
        A[1].setGeometry(Axis.O_Y,h-innerB,-(H=innerH));
        Y=TH-innerB-innerH;
        
        hasLeft=hasRight=hasTop=hasBot=false;
        
        pts=v[0].size();
        if (v[1].size()<pts) pts=v[1].size();
        
        pp = new PlotPrimitive[pts];
        Pts=new Point[pts];
        for (int i=0;i<pts;i++) {
            int jx=0, jy=0;
            if (v[0].isCat() && jitter && !stackjitter) {
                double d=Math.random()-0.5; d=Math.tan(d*2.5)/4.0;
                jx=(int)(d*((double)(A[0].getCatLow(v[0].getCatIndex(i))-A[0].getCasePos(i))));
            }
            if (v[1].isCat() && jitter && !stackjitter) {
                double d=Math.random()-0.5; d=Math.tan(d*2.5)/4.0;
                jy=(int)(d*((double)(A[1].getCatLow(v[1].getCatIndex(i))-A[1].getCasePos(i))));
            }
            if ((!v[0].isMissingAt(i) || v[0].isCat()) && (!v[1].isMissingAt(i) || v[1].isCat())) {
                int x=jx+A[0].getCasePos(i),y=jy+A[1].getCasePos(i);
                Pts[i]=null;
                pp[i]=null;
                if (x<innerL) hasLeft=true;
                else if (y<10) hasTop=true;
                else if (x>w-10) hasRight=true;
                else if (y>h-innerB) hasBot=true;
                else {
                    if (stackjitter && jitter && v[0].isCat() && i>0) {
                        int j=0;
                        while (j<i) {
                            if (pp[j]!=null && ((PPrimCircle)pp[j]).y==y && ((PPrimCircle)pp[j]).x==x) x+=stackOff;
                            //if (Pts[j]!=null && Pts[j].y==y && Pts[j].x==x) x+=stackOff;
                            j++;
                        }
                    } else if (stackjitter && jitter && v[1].isCat() && i>0) {
                        int j=0;
                        while (j<i) {
                            if (pp[j]!=null && ((PPrimCircle)pp[j]).y==y && ((PPrimCircle)pp[j]).x==x) y-=stackOff;
                            //if (Pts[j]!=null && Pts[j].y==y && Pts[j].x==x) y-=stackOff;
                            j++;
                        }
                    }
                    Pts[i]=new Point(x,y);
                    pp[i]=new PPrimCircle();
                    ((PPrimCircle)pp[i]).x = x;
                    ((PPrimCircle)pp[i]).y = y;
                    ((PPrimCircle)pp[i]).diam = ptDiam;
                    ((PPrimCircle)pp[i]).ref = new int[] {i};
                }
            } else { // place missings on the other side of the axes
                int x,y;
                if (v[0].isMissingAt(i)) x=innerL-4; else x=jx+A[0].getCasePos(i);
                if (v[1].isMissingAt(i)) y=h-innerB+4; else y=jy+A[1].getCasePos(i);
                Pts[i]=new Point(x,y);
                pp[i]=new PPrimCircle();
                ((PPrimCircle)pp[i]).x = x;
                ((PPrimCircle)pp[i]).y = y;
                ((PPrimCircle)pp[i]).diam = ptDiam;
                ((PPrimCircle)pp[i]).ref = new int[] {i};
            }
        };
    };
    
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar()=='R') run(this,"rotate");
        if (e.getKeyChar()=='l') run(this,"labels");
        if (e.getKeyChar()=='P') run(this,"print");
        if (e.getKeyChar()=='X') run(this,"exportPGS");
        if (e.getKeyChar()=='g') run(this,"nextBg");
        if (e.getKeyChar()=='C') run(this,"exportCases");
        if (e.getKeyChar()=='e') run(this,"selRed");
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
            run(this,"alphaDown");
        }
        if (e.getKeyCode()==KeyEvent.VK_LEFT) {
            run(this,"alphaUp");
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
            int rt=(cmd=="YrangeDlg")?1:0;
            Dialog d=intDlg=new Dialog(myFrame,(rt==1)?"Y range":"X range",true);
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
            TextField tw=new TextField(""+A[rt].vBegin,6);
            TextField th=new TextField(""+(A[rt].vBegin+A[rt].vLen),6);
            cp.add(tw);
            cp.add(new Label(", end: "));
            cp.add(th);
            d.pack();
            b.addActionListener(ic);b2.addActionListener(ic);
            d.setVisible(true);
            if (!cancel) {
                double w=Tools.parseDouble(tw.getText());
                double h=Tools.parseDouble(th.getText());
                A[rt].setValueRange(w,h-w);
                setUpdateRoot(0);
                repaint();
            }
            d.dispose();
        }
        if (cmd=="nextBg") { fieldBg++; if (fieldBg>2) fieldBg=0; setUpdateRoot(0); repaint(); };
        if (cmd=="resetZoom") { resetZoom(); repaint(); }
        if (cmd=="selRed") { selRed=!selRed; setUpdateRoot(2); repaint(); };
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
            ptAlpha-=(ptAlpha>0.2)?0.10:0.02; if (ptAlpha<0f) ptAlpha=0.05f;
            setUpdateRoot(0); repaint();
        }
        if (cmd=="alphaUp") {
            ptAlpha+=(ptAlpha>0.2)?0.10:0.02; if (ptAlpha>1f) ptAlpha=1f;
            setUpdateRoot(0); repaint();
        }
        
        return null;
    }
    
    public void paintBack(PoGraSS g) {
        Rectangle r=pc.getBounds();
        g.setBounds(r.width,r.height);
        g.begin();
        g.defineColor("white",255,255,255);
        if (selRed)
            g.defineColor("marked",255,0,0);
        else
            g.defineColor("marked",Common.selectColor.getRed(),Common.selectColor.getGreen(),Common.selectColor.getBlue());
        g.defineColor("objects",Common.objectsColor.getRed(),Common.objectsColor.getGreen(),Common.objectsColor.getBlue());
        g.defineColor("black",0,0,0);
        g.defineColor("outline",0,0,0);
        g.defineColor("point",0,0,128);
        g.defineColor("red",255,0,0);
        g.defineColor("line",0,0,128); // color of line plot
        g.defineColor("lines",96,96,255);
        g.defineColor("selText",255,0,0);
        g.defineColor("selBg",255,255,192);
        g.defineColor("splitRects",128,128,255);
        float[] scc=Common.selectColor.getRGBComponents(null);
        g.defineColor("aSelBg",scc[0],scc[1],scc[2],0.3f);
        g.defineColor("aDragBg",0.0f,0.3f,1.0f,0.25f);
        
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
            double f=A[0].getSensibleTickDistance(50,26);
            double fi=A[0].getSensibleTickStart(f);
            if (Global.DEBUG>1)
                System.out.println("SP.A[0]:"+A[0].toString()+", distance="+f+", start="+fi);
            try {
                while (fi<A[0].vBegin+A[0].vLen) {
                    int t=A[0].getValuePos(fi);
                    g.drawLine(t,Y+H,t,Y+H+5);
                    if (showLabels)
                        g.drawString(v[0].isCat()?((useX3)?Common.getTriGraph(v[0].getCatAt((int)(fi+0.5)).toString()):v[0].getCatAt((int)(fi+0.5)).toString()):
                            A[0].getDisplayableValue(fi),t,Y+H+20,0.5,0);
                    fi+=f;
                }
            } catch (Exception pae) { // catch problems (especially in getCatAt being 0)
            }
        }
        
        /* draw ticks and labels for Y axis */
        {
            double f=A[1].getSensibleTickDistance(30,18);
            double fi=A[1].getSensibleTickStart(f);
            if (Global.DEBUG>1)
                System.out.println("SP.A[1]:"+A[1].toString()+", distance="+f+", start="+fi);
            try {
                while (fi<A[1].vBegin+A[1].vLen) {
                    int t=A[1].getValuePos(fi);
                    g.drawLine(X-5,t,X,t);
                    if(showLabels)
                        g.drawString(v[1].isCat()?Common.getTriGraph(v[1].getCatAt((int)(fi+0.5)).toString()):A[1].getDisplayableValue(fi),X-8,t,1,0.3);
                    fi+=f;
                }
            } catch (Exception pae) { // catch problems (especially in getCatAt being 0)
            }
        }
        
        //nextLayer(g);
        
        int lastSM=0;
        /* begin: old code
         if (ptAlpha<1f) g.setGlobalAlpha(ptAlpha);
        g.setColor("point");
        if (filter==null) {
            for (int i=0;i<pts;i++)
                if (Pts[i]!=null) {
                int mm=m.getSec(i);
                if (mm!=lastSM) {
                    if (mm==0) g.setColor("point"); else g.setColor(ColorBridge.getMain().getColor(mm));
                    lastSM=mm;
                }
                g.fillOval(Pts[i].x-ptDiam/2,Pts[i].y-ptDiam/2,ptDiam,ptDiam);
                }
        } else {
            for (int i=0;i<filter.length;i++)
                if (Pts[filter[i]]!=null)
                    g.fillOval(Pts[filter[i]].x-ptDiam/2,Pts[filter[i]].y-ptDiam/2,ptDiam,ptDiam);
        }
         
         
        g.resetGlobalAlpha();
        //nextLayer(g);
         
        if (m.marked()>0) {
            g.setColor("marked");
            if (filter==null) {
                for (int i=0;i<pts;i++)
                    if (Pts[i]!=null && m.at(i))
                        g.fillOval(Pts[i].x-ptDiam/2,Pts[i].y-ptDiam/2,ptDiam,ptDiam);
            } else {
                for (int j=0;j<filter.length;j++) {
                    int i=filter[j];
                    if (Pts[i]!=null && m.at(i))
                        g.fillOval(Pts[i].x-ptDiam/2,Pts[i].y-ptDiam/2,ptDiam,ptDiam);
                }
            }
        };*/
        
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
        return "point #" + i;
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
            if (qx==A[0].clip(qx) && qy==A[1].clip(qy)) {
                g.drawLine(A[0].gBegin,qy,A[0].gBegin+A[0].gLen,qy);
                g.drawLine(qx,A[1].gBegin,qx,A[1].gBegin+A[1].gLen);
                g.drawString(A[0].getDisplayableValue(A[0].getValueForPos(qx)),qx+2,qy-2);
                g.drawString(A[1].getDisplayableValue(A[1].getValueForPos(qy)),qx+2,qy+11);
            }
        }
        super.paintPost(g);
    }
};