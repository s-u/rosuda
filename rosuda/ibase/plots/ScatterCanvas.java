import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.PrintStream;

/** implementation of scatterplots
    @version $Id$
*/
class ScatterCanvas extends PGSCanvas implements Dependent, MouseListener, MouseMotionListener, KeyListener, ActionListener, Commander
{
    /** array of two variables (X and Y) */
    SVar v[];
    /** associated marker */
    SMarker m;

    /** flag whether axis labels should be shown */
    boolean showLabels=true;

    /** flag whether jittering shoul dbe used for categorical vars */
    boolean jitter=false;

    /** flag whether alternative selection style should be used */
    boolean selRed=false;

    /** use trigraph for X axis in case X is categorical */
    boolean useX3=false; 

    /** use shading of background according to depth */
    boolean shading=false;
    
    /** if true partition nodes above current node only */
    public boolean bgTopOnly=false;

    /** diameter of a point */
    public int ptDiam=3;

    public int fieldBg=0; // 0=none, 1=objects, 2=white
    
    /** array of two axes (X and Y) - note that it is in fact just a copy of ax and ay for
	compatibility with older implementations */
    Axis A[];

    /** array of points (in geometrical coordinates) */
    Point[] Pts;
    /** # of points */
    int pts;

    int x1, y1, x2, y2;
    boolean drag;

    MenuItem MIlabels=null;

    int X,Y,W,H, TW,TH;

    int []filter=null;

    boolean querying=false;
    int qx,qy;

    boolean zoomRetainsAspect=false;
    
    /** create a new scatterplot
	@param f associated frame (or <code>null</code> if none)
	@param v1 variable 1
	@param v2 variable 2
	@param mark associated marker */
    public ScatterCanvas(Frame f, SVar v1, SVar v2, SMarker mark) {
        super(4); // 4 layers; 0=base, 1=points, 2=selected, 3=drag
	setFrame(f); setTitle("Scatterplot ("+v1.getName()+" : "+v2.getName()+")");
	v=new SVar[2]; A=new Axis[2];
	v[0]=v1; v[1]=v2; m=mark;
        ax=A[0]=new Axis(v1,Axis.O_X,v1.isCat()?Axis.T_EqCat:Axis.T_Num); A[0].addDepend(this);
        ay=A[1]=new Axis(v2,Axis.O_Y,v2.isCat()?Axis.T_EqCat:Axis.T_Num); A[1].addDepend(this);
        if (!v1.isCat()) ax.setValueRange(v1.getMin()-(v1.getMax()-v1.getMin())/20,(v1.getMax()-v1.getMin())*1.1);
        if (!v2.isCat()) ay.setValueRange(v2.getMin()-(v2.getMax()-v2.getMin())/20,(v2.getMax()-v2.getMin())*1.1);
        if (!v1.isCat() && v1.getMax()-v1.getMin()==0) ax.setValueRange(v1.getMin()-0.5,1);
        if (!v2.isCat() && v2.getMax()-v2.getMin()==0) ay.setValueRange(v2.getMin()-0.5,1);
	setBackground(Common.backgroundColor);
        zoomSequence=new Vector();
	drag=false;
	updatePoints();
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this); f.addKeyListener(this);
	MenuBar mb=null;
	String myMenu[]={"+","File","~File.Graph","~Edit","+","View","!RRotate","rotate","@0Reset zoom","resetZoom","Same scale","equiscale","-","Hide labels","labels","Toggle hilight. style","selRed","Change background","nextBg","Toggle jittering","jitter","Toggle shading","shading","~Window","0"};
        EzMenu.getEzMenu(f,this,myMenu);
        MIlabels=EzMenu.getItem(f,"labels");
        if (!v1.isCat() && !v2.isCat())
            EzMenu.getItem(f,"jitter").setEnabled(false);
        if (Common.AppType==Common.AT_Framework)
            EzMenu.getItem(f,"shading").setEnabled(false);
    }

    public SVar getData(int id) { return (id<0||id>1)?null:v[id]; }
    
    public Dimension getMinimumSize() { return new Dimension(60,50); };

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
	    ((Frame) getParent()).setTitle("Scatterplot ("+v[1].getName()+" vs "+v[0].getName()+")");
	} catch (Exception ee) {};
	updatePoints();
        setUpdateRoot(0);
	repaint();
    };

    public void Notifying(NotifyMsg msg, Object o, Vector path) {
	if((msg.getMessageID()&Common.NM_MASK)==Common.NM_VarChange || msg.getMessageID()==Common.NM_AxisChange)
	    updatePoints();
        setUpdateRoot((msg.getMessageID()==Common.NM_MarkerChange)?2:0);
        repaint();
    };

    SNode paint_cn;
    
    /** paints partitioning for a single node (and descends recursively) */	
    public void paintNode(PoGraSS g, SNode n, int x1, int y1, int x2, int y2, boolean sub) {
	if (n.tmp==2) {
	    g.setColor("selBg");
	    g.fillRect(x1,y1,x2-x1,y2-y1);
        } else {
            if (shading && (n.splitVar==v[0] || n.splitVar==v[1])) {
                int level=255-n.getLevel()*16; if (level<128) level=128;
                g.setColor(level,level,level);
                g.fillRect(x1,y1,x2-x1,y2-y1);                
            }
        }
	g.setColor("splitRects");
	g.drawRect(x1,y1,x2-x1,y2-y1);
	if (n.isLeaf() || n.isPruned() || (bgTopOnly && n==paint_cn)) return;
	for(Enumeration e=n.children();e.hasMoreElements();) {
	    SNode c=(SNode)e.nextElement();
	    int nx1=x1, nx2=x2, ny1=y1, ny2=y2;
	    if (c.splitVar==v[0]) {
		if (!c.splitVar.isCat()) {
		    int spl=A[0].getValuePos(c.splitValF);
		    if (c.splitComp==-1) nx2=spl;
		    if (c.splitComp==1) nx1=spl;
		};
	    };
	    if (c.splitVar==v[1]) {
		if (!c.splitVar.isCat()) {
		    int spl=A[1].getValuePos(c.splitValF);
		    if (c.splitComp==-1) ny1=spl;
		    if (c.splitComp==1) ny2=spl;
		};
	    };
	    paintNode(g,c,nx1,ny1,nx2,ny2,(n.tmp==2)?true:sub);
	};
    };

    // clipping warnings
    boolean hasLeft, hasTop, hasRight, hasBot;
    
    public void paintPoGraSS(PoGraSS g) {
	Rectangle r=getBounds();
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

	Dimension Dsize=getSize();
	if (Dsize.width!=TW || Dsize.height!=TH)
	    updatePoints();

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
        
	SNode cn=(m!=null)?m.getNode():null;
        paint_cn=cn;

	if (cn!=null) {
            if (Common.DEBUG>0) System.out.println("ScatterCanvas: current node present, constructing partitions"); 
	    ((SNode)cn.getRoot()).setAllTmp(0);
	    SNode t=cn;
	    t.tmp=2;
	    while (t.getParent()!=null) {
		t=(SNode)t.getParent();
		t.tmp=1;
	    };
	    paintNode(g,t,X,Y,X+W,Y+H,false);
	};

        g.setColor("black");
        g.drawLine(X,Y,X,Y+H);
        g.drawLine(X,Y+H,X+W,Y+H);

	/* draw ticks and labels for X axis */
        {
            double f=A[0].getSensibleTickDistance(50,26);
            double fi=A[0].getSensibleTickStart(f);
            //if (Common.DEBUG>0)
            //System.out.println("SP.A[0]:"+A[0].toString()+", distance="+f+", start="+fi);
            while (fi<A[0].vBegin+A[0].vLen) {
                int t=A[0].getValuePos(fi);
                g.drawLine(t,Y+H,t,Y+H+5);
                if (showLabels)
                    g.drawString(v[0].isCat()?((useX3)?Common.getTriGraph(v[0].getCatAt((int)fi).toString()):v[0].getCatAt((int)fi).toString()):
                                 A[0].getDisplayableValue(fi),t,Y+H+20,0.5,0);
                fi+=f;
            };
        }

	/* draw ticks and labels for Y axis */
        {
            double f=A[1].getSensibleTickDistance(30,18);
            double fi=A[1].getSensibleTickStart(f);
            //if (Common.DEBUG>0)
            //System.out.println("SP.A[1]:"+A[1].toString()+", distance="+f+", start="+fi);
            while (fi<A[1].vBegin+A[1].vLen) {
                int t=A[1].getValuePos(fi);
                g.drawLine(X-5,t,X,t);
                if(showLabels)
                    g.drawString(v[1].isCat()?Common.getTriGraph(v[1].getCatAt((int)fi).toString()):A[1].getDisplayableValue(fi),X-8,t,1,0.3);
                fi+=f;
            };
        }

        nextLayer(g);
        
        int lastSM=0;
	g.setColor("point");
        if (filter==null) {
            for (int i=0;i<pts;i++)
                if (Pts[i]!=null) {
                    int mm=m.getSec(i);
                    if (mm!=lastSM) {
                        if (mm==0) g.setColor("point"); else g.setColor(ColorBridge.main.getColor(mm));
                        lastSM=mm;
                    }
                    g.fillOval(Pts[i].x-ptDiam/2,Pts[i].y-ptDiam/2,ptDiam,ptDiam);
                }
        } else {
            for (int i=0;i<filter.length;i++)
                if (Pts[filter[i]]!=null)
                    g.fillOval(Pts[filter[i]].x-ptDiam/2,Pts[filter[i]].y-ptDiam/2,ptDiam,ptDiam);
        }

            
            
        nextLayer(g);
        
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
        };
                
	nextLayer(g);
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
        if (querying) {
            g.setColor("black");
            if (qx==A[0].clip(qx) && qy==A[1].clip(qy)) {
                g.drawLine(A[0].gBegin,qy,A[0].gBegin+A[0].gLen,qy);
                g.drawLine(qx,A[1].gBegin,qx,A[1].gBegin+A[1].gLen);
                g.drawString(A[0].getDisplayableValue(A[0].getValueForPos(qx)),qx+2,qy-2);
                g.drawString(A[1].getDisplayableValue(A[1].getValueForPos(qy)),qx+2,qy+11);
            }
        }
	g.end();
        setUpdateRoot(4); // by default no repaint is necessary unless resize occurs
    };

    public void updatePoints() {
	Dimension Dsize=getSize();
	int w=Dsize.width, h=Dsize.height;
	TW=w; TH=h;
	int innerL=30, innerB=30, lshift=0;
	int innerW=w-innerL-10, innerH=h-innerB-10;
	boolean xcat=v[0].isCat(), ycat=v[1].isCat();	
	
	A[0].setGeometry(Axis.O_X,X=innerL,W=innerW);
	A[1].setGeometry(Axis.O_Y,h-innerB,-(H=innerH));
	Y=TH-innerB-innerH;

        hasLeft=hasRight=hasTop=hasBot=false;
        
	pts=v[0].size();
	if (v[1].size()<pts) pts=v[1].size();
	
	Pts=new Point[pts];
        for (int i=0;i<pts;i++) {
            int jx=0, jy=0;
            if (v[0].isCat() && jitter) {
                double d=Math.random()-0.5; d=Math.tan(d*2.5)/4.0;
                jx=(int)(d*((double)(A[0].getCatLow(v[0].getCatIndex(i))-A[0].getCasePos(i))));
            }
            if (v[1].isCat() && jitter) {
                double d=Math.random()-0.5; d=Math.tan(d*2.5)/4.0;
                jy=(int)(d*((double)(A[1].getCatLow(v[1].getCatIndex(i))-A[1].getCasePos(i))));                
            }
            if ((!v[0].isMissingAt(i) || v[0].isCat()) && (!v[1].isMissingAt(i) || v[1].isCat())) {
                int x=jx+A[0].getCasePos(i),y=jy+A[1].getCasePos(i);
                Pts[i]=null;
                if (x<innerL) hasLeft=true;
                else if (y<10) hasTop=true;
                else if (x>w-10) hasRight=true;
                else if (y>h-innerB) hasBot=true;
                else
                    Pts[i]=new Point(x,y);
            } else { // place missings on the other side of the axes
                int x,y;
                if (v[0].isMissingAt(i)) x=innerL-4; else x=jx+A[0].getCasePos(i);
                if (v[1].isMissingAt(i)) y=h-innerB+4; else y=jy+A[1].getCasePos(i);
                Pts[i]=new Point(x,y);
            }
        };
    };

    public void mouseClicked(MouseEvent ev) 
    {
        int x=ev.getX(), y=ev.getY();
        if (Common.isZoomTrigger(ev)) {
            performZoomOut(x,y);
            return;
        }
        //x1=x-2; y1=y-2; x2=x+3; y2=y+3; drag=true; mouseReleased(ev);
    }

    boolean zoomDrag;
    
    public void mousePressed(MouseEvent ev) 
    {	
	x1=ev.getX(); y1=ev.getY();
	drag=true;
        zoomDrag=Common.isZoomTrigger(ev);        
    }
    
    public void mouseReleased(MouseEvent e)
    {
	int X1=x1, Y1=y1, X2=x2, Y2=y2;
	if (x1>x2) { X2=x1; X1=x2; };
	if (y1>y2) { Y2=y1; Y1=y2; };

        if (zoomDrag) {
            drag=false;
            if (X2-X1<2 || X2-X1<2) return;
            performZoomIn(X1,Y1,X2,Y2);
            repaint();
        } else {
            Rectangle sel=new Rectangle(X1,Y1,X2-X1,Y2-Y1);

            boolean setTo=false;
            if (e.isControlDown()) setTo=true;
            if (!e.isShiftDown()) m.selectNone();

            drag=false;
            int i=0;
            while (i<pts) {
                if (Pts[i]!=null && sel.contains(Pts[i]))
                    m.set(i,m.at(i)?setTo:true);
                i++;
            };
            m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
            setUpdateRoot(2);
            repaint();
        }
    };
    public void mouseEntered(MouseEvent e) {};
    public void mouseExited(MouseEvent e) {};
    public void mouseDragged(MouseEvent e) 
    {
	if (drag) {
	    int x=e.getX(), y=e.getY();
	    if (x!=x2 || y!=y2) {
		x2=x; y2=y;
                setUpdateRoot(3);
		repaint();
	    };
	};
    };
    public void mouseMoved(MouseEvent ev) {
        if (querying) {
            qx=ev.getX(); qy=ev.getY();
            setUpdateRoot(3);
            repaint();
        }
    };

    public void keyTyped(KeyEvent e) 
    {
	if (e.getKeyChar()=='R') run(this,"rotate");
	if (e.getKeyChar()=='l') run(this,"labels");
	if (e.getKeyChar()=='P') run(this,"print");
	if (e.getKeyChar()=='X') run(this,"exportPGS");
        if (e.getKeyChar()=='g') run(this,"nextBg");
	if (e.getKeyChar()=='C') run(this,"exportCases");
	if (e.getKeyChar()=='e') run(this,"selRed");
	if (e.getKeyChar()=='j') run(this,"jitter");
	if (e.getKeyChar()=='t') run(this,"trigraph");
        if (e.getKeyChar()=='s') run(this,"shading");
    };
    public void keyPressed(KeyEvent e) {
        if (Common.DEBUG>0)
            System.out.println("ScatterCanvas: "+e);
        if (e.getKeyCode()==KeyEvent.VK_ALT && !querying) {
            querying=true;
            qx=qy=-1;
            setCursor(Common.cur_aim);
        }
        if (e.getKeyCode()==KeyEvent.VK_UP) {
            ptDiam+=2; setUpdateRoot(0); repaint();
        }
        if (e.getKeyCode()==KeyEvent.VK_DOWN && ptDiam>2) {
            ptDiam-=2; setUpdateRoot(0); repaint();
        }
    };
    public void keyReleased(KeyEvent e) {
        if (Common.DEBUG>0)
            System.out.println("ScatterCanvas: "+e);
        if (e.getKeyCode()==KeyEvent.VK_ALT) {
            querying=false;
            setCursor(Common.cur_arrow);
            setUpdateRoot(3); repaint();
        }
    };

    public Object run(Object o, String cmd) {
	super.run(o,cmd);
        if (m!=null) m.run(o,cmd);
	if (cmd=="rotate") {
	    rotate();
	};
	if (cmd=="labels") {
	    showLabels=!showLabels;
	    MIlabels.setLabel((showLabels)?"Hide labels":"Show labels");
            setUpdateRoot(0);
            repaint();
	};
        if (cmd=="equiscale") {
            double sfx,sfy, usfx,usfy;
            sfx=((double)ax.gLen)/ax.vLen; usfx=(sfx<0)?-sfx:sfx;
            sfy=((double)ay.gLen)/ay.vLen; usfy=(sfy<0)?-sfy:sfy;
            if (usfx<usfy) {
                ay.setValueRange(ay.vBegin,ay.vLen*(usfy/usfx));
            } else {
                ax.setValueRange(ax.vBegin,ax.vLen*(usfx/usfy));
            }
            updatePoints();
            setUpdateRoot(0);
            repaint();
        }
        if (cmd=="nextBg") { fieldBg++; if (fieldBg>2) fieldBg=0; setUpdateRoot(0); repaint(); };
        if (cmd=="resetZoom") { resetZoom(); repaint(); }
	if (cmd=="print") run(o,"exportPS");
	if (cmd=="exit") WinTracker.current.Exit();
        if (cmd=="selRed") { selRed=!selRed; setUpdateRoot(2); repaint(); };
        if (cmd=="jitter") {
            jitter=!jitter; updatePoints(); setUpdateRoot(1); repaint();
        }
        if (cmd=="shading") {
            shading=!shading; updatePoints(); setUpdateRoot(0); repaint();
        }
        if (cmd=="trigraph") { useX3=!useX3; setUpdateRoot(0); repaint(); }
        
        if (cmd=="exportCases") {
	    try {
		PrintStream p=Tools.getNewOutputStreamDlg(myFrame,"Export selected cases to ...","selected.txt");
		if (p!=null) {
		    p.println(v[0].getName()+"\t"+v[1].getName());
		    int i=0;
		    for (Enumeration e=v[0].elements(); e.hasMoreElements();) {
			Object oo=e.nextElement();
			if (m.at(i))
			    p.println(((oo==null)?"NA":oo.toString())+"\t"+((v[1].at(i)==null)?"NA":v[1].at(i).toString()));
			i++;
		    };
		    p.close();
		};
	    } catch (Exception eee) {};
	};
	
	return null;
    };

    public void actionPerformed(ActionEvent e) {
	if (e==null) return;
	run(e.getSource(),e.getActionCommand());
    }

// "borrowed" from BaseCanvas until we have BaseCanvas-based scatter plot

    class ZoomDescriptorComponent {
        double vBegin, vLen;
        int gBegin, gLen, dc;
        boolean dummy;

        ZoomDescriptorComponent() {
            dummy=true;
        }

        ZoomDescriptorComponent(Axis a) {
            vBegin=a.vBegin; vLen=a.vLen; gBegin=a.gBegin; gLen=a.gLen;
            dc=a.datacount;
            dummy=false;
        }
    }

    /** if set to <code>true</code> all notifications are rejected. Any subclass is free to use it, BaseCanvas modifies this flag in default zoom processing methods to prevent partial updates when ax and ay are updated sequentially. Any method changing this flag should always restore the state of the flag after it finishes! Also use with care in multi-threaded applications to prevent deadlocks. */
    boolean ignoreNotifications=false;

    /** this vector can be used to track the sequence of zooms. Zoom out should return to the state before last zoom in (if sensible in the context of the given plot). Any implementation of {@link #performZoomIn} and {@link #performZoomOut} is free to use this vector in any way which suits the implementation.<p>The current default implementation uses pairs of {@link ZoomDescriptorComponent} objects to store status of {@link #ax} and {@link #ay} axes. The vector is automatically initilized to an empty vector by the base constructor. */
    Vector zoomSequence;

    public void performZoomIn(int x1, int y1, int x2, int y2) {
        if (Common.DEBUG>0) System.out.println("performZoomIn("+x1+","+y1+","+x2+","+y2+") [zoomSequence.len="+zoomSequence.size()+"]");
        boolean ins=ignoreNotifications;
        ignoreNotifications=true;
        double ax1=1.0, ax2=1.0, ay1=1.0, ay2=1.0;
        double xExtent=1.0, yExtent=1.0, xCenter=1.0, yCenter=1.0;
        if (ax!=null) {
            ax1=ax.getValueForPos(x1); ax2=ax.getValueForPos(x2);
            if ((ax2-ax1)*ax.vLen<0.0) { // fix signum - must be same as vLen
                double ah=ax2; ax2=ax1; ax1=ah;
            }
            xExtent=(x1==x2)?ax.vLen/2.0:ax2-ax1;
            xCenter=(ax1+ax2)/2.0;
        }
        if (ay!=null) {
            ay1=ay.getValueForPos(y1); ay2=ay.getValueForPos(y2);
            if ((ay2-ay1)*ay.vLen<0.0) { // fix signum - must be same as vLen
                double ah=ay2; ay2=ay1; ay1=ah;
            }
            yExtent=(y1==y2)?ay.vLen/2.0:ay2-ay1;
            yCenter=(ay1+ay2)/2;
        }
        if (ax!=null && ay!=null && zoomRetainsAspect) {
            double ratioPre=ax.vLen/ay.vLen;
            if (ratioPre<0.0) ratioPre=-ratioPre;
            double ratioPost=xExtent/yExtent;
            if (ratioPost<0.0) ratioPost=-ratioPost;
            if (ratioPost>ratioPre) // x1/y1 < x2/y2 => inflate y
                yExtent*=ratioPost/ratioPre;
            else // otherwise inflate x
                xExtent*=ratioPost/ratioPre;
        }
        if (ax!=null) {
            zoomSequence.addElement(new ZoomDescriptorComponent(ax));
            ax.setValueRange(xCenter-xExtent/2.0,xExtent);
        } else zoomSequence.addElement(new ZoomDescriptorComponent());
        ignoreNotifications=ins;
        if (ay!=null) {
            zoomSequence.addElement(new ZoomDescriptorComponent(ay));
            ay.setValueRange(yCenter-yExtent/2.0,yExtent);
        } else zoomSequence.addElement(new ZoomDescriptorComponent());
        updatePoints();
        setUpdateRoot(0);
        repaint();
    }

    public void performZoomOut(int x, int y) {
        if (Common.DEBUG>0) System.out.println("performZoomOut("+x+","+y+") [zoomSequence.len="+zoomSequence.size()+"]");
        int tail=zoomSequence.size()-1;
        if (tail<1) return;
        ZoomDescriptorComponent zx,zy;
        zx=(ZoomDescriptorComponent)zoomSequence.elementAt(tail-1);
        zy=(ZoomDescriptorComponent)zoomSequence.elementAt(tail);
        boolean ins=ignoreNotifications;
        ignoreNotifications=true;
        if (ax!=null && !zx.dummy)
            ax.setValueRange(zx.vBegin,zx.vLen);
        ignoreNotifications=ins;
        if (ay!=null && !zy.dummy)
            ay.setValueRange(zy.vBegin,zy.vLen);
        zoomSequence.removeElement(zy);
        zoomSequence.removeElement(zx);
        updatePoints();
        setUpdateRoot(0);
        repaint();
    }

    public void resetZoom() {
        if (Common.DEBUG>0) System.out.println("resetZoom() [zoomSequence.len="+zoomSequence.size()+"]");
        if (zoomSequence.size()>1) {
            ZoomDescriptorComponent zx,zy;
            zx=(ZoomDescriptorComponent)zoomSequence.elementAt(0);
            zy=(ZoomDescriptorComponent)zoomSequence.elementAt(1);
            boolean ins=ignoreNotifications;
            ignoreNotifications=true; // prevent processing of AxisChanged notification for ax
            if (ax!=null && !zx.dummy)
                ax.setValueRange(zx.vBegin,zx.vLen);
            ignoreNotifications=ins;
            if (ay!=null && !zy.dummy)
                ay.setValueRange(zy.vBegin,zy.vLen);
            updatePoints();
            setUpdateRoot(0);
            repaint();
        }
        zoomSequence.removeAllElements();
    }
    
};

/* Changes Glasgow:
   - update Notifying to rebuild points on NM_VarChange
*/
