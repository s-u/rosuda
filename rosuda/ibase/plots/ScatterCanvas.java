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

    boolean showLabels=true;

    boolean jitter=false;

    boolean selRed=false;

    /** array of two axes (X and Y) */
    Axis A[];

    /** array of points (in geometrical coordinates) */
    Point[] Pts;
    /** # of points */
    int pts;

    int x1, y1, x2, y2;
    boolean drag;

    MenuItem MIlabels=null;

    int X,Y,W,H, TW,TH;

    /** create a new scatterplot
	@param f associated frame (or <code>null</code> if none)
	@param v1 variable 1
	@param v2 variable 2
	@param mark associated marker */
    public ScatterCanvas(Frame f, SVar v1, SVar v2, SMarker mark) {
        super(3); // 3 layers; 0=base+points, 1=selected, 2=drag
	setFrame(f); setTitle("Scatterplot ("+v1.getName()+" : "+v2.getName()+")");
	v=new SVar[2]; A=new Axis[2];
	v[0]=v1; v[1]=v2; m=mark;
        A[0]=new Axis(v1,Axis.O_X,v1.isCat()?Axis.T_EqCat:Axis.T_Num); A[0].addDepend(this);
        A[1]=new Axis(v2,Axis.O_Y,v2.isCat()?Axis.T_EqCat:Axis.T_Num); A[1].addDepend(this);
	setBackground(new Color(255,255,192));
	drag=false;
	updatePoints();
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this); f.addKeyListener(this);
	MenuBar mb=null;
	String myMenu[]={"+","File","Save as PGS ...","exportPGS","Save as PostScript ...","exportPS","-","Save selected as ...","exportCases","-","Close","WTMclose","Quit","exit","+","View","Rotate","rotate","Hide labels","labels","Toggle hilight. style","selRed","Toggle jittering","jitter","0"};
	f.setMenuBar(mb=WinTracker.current.buildQuickMenuBar(f,this,myMenu,false));
	MIlabels=mb.getMenu(1).getItem(1);	
    };

    public Dimension getMinimumSize() { return new Dimension(60,50); };

    public void rotate() {
	SVar h=v[0]; v[0]=v[1]; v[1]=h;
	Axis ha=A[0]; A[0]=A[1]; A[1]=ha;
	try {
	    ((Frame) getParent()).setTitle("Scatterplot ("+v[1].getName()+" vs "+v[0].getName()+")");
	} catch (Exception ee) {};
	updatePoints();
        setUpdateRoot(0);
	repaint();
    };

    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        setUpdateRoot((msg.getMessageID()==Common.NM_MarkerChange)?1:0);
        repaint();
    };

    /** paints partitioning for a single node (and descends recursively) */	
    public void paintNode(PoGraSS g, SNode n, int x1, int y1, int x2, int y2, boolean sub) {
	if (n.tmp==2) {
	    g.setColor("selBg");
	    g.fillRect(x1,y1,x2-x1,y2-y1);
	};
	g.setColor("splitRects");
	g.drawRect(x1,y1,x2-x1,y2-y1);
	if (n.isLeaf() || n.isPruned()) return;
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
		    if (c.splitComp==-1) ny1=TH-spl;
		    if (c.splitComp==1) ny2=TH-spl;
		};
	    };
	    paintNode(g,c,nx1,ny1,nx2,ny2,(n.tmp==2)?true:sub);
	};
    };

    public void paintPoGraSS(PoGraSS g) {
	Rectangle r=getBounds();
	g.setBounds(r.width,r.height);
	g.begin();
	g.defineColor("white",255,255,255);
	if (selRed)
	    g.defineColor("marked",255,0,0);
	else
	    g.defineColor("marked",128,255,128);
	g.defineColor("black",0,0,0);
	g.defineColor("outline",0,0,0);
	g.defineColor("point",0,0,128);	
	g.defineColor("red",255,0,0);
	g.defineColor("lines",96,96,255);	
	g.defineColor("selText",255,0,0);
	g.defineColor("selBg",255,255,192);
	g.defineColor("splitRects",128,128,255);

	Dimension Dsize=getSize();
	if (Dsize.width!=TW || Dsize.height!=TH)
	    updatePoints();

	if (TW<50||TH<50) {
	    g.setColor("red");
	    g.drawLine(0,0,TW,TH); 
	    g.drawLine(0,TH,TW,0); 
	    return;
	};

	g.setColor("white");
	g.fillRect(X,Y,W,H);

	SNode cn=(m!=null)?m.getNode():null;

	if (cn!=null) {
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

        {
            double f=A[0].getSensibleTickDistance(50,26);
            double fi=A[0].getSensibleTickStart(f);
            //if (Common.DEBUG>0)
            //System.out.println("SP.A[0]:"+A[0].toString()+", distance="+f+", start="+fi);
            while (fi<A[0].vBegin+A[0].vLen) {
                int t=A[0].getValuePos(fi);
                g.drawLine(t,Y+H,t,Y+H+5);
                if (showLabels)
                    g.drawString(v[0].isCat()?v[0].getCatAt((int)fi).toString():A[0].getDisplayableValue(fi),t-5,Y+H+20);
                fi+=f;
            };
        }

        {
            double f=A[1].getSensibleTickDistance(50,18);
            double fi=A[1].getSensibleTickStart(f);
            //if (Common.DEBUG>0)
            //System.out.println("SP.A[1]:"+A[1].toString()+", distance="+f+", start="+fi);
            while (fi<A[1].vBegin+A[1].vLen) {
                int t=TH-A[1].getValuePos(fi);
                g.drawLine(X-5,t,X,t);
                if(showLabels)
                    g.drawString(v[1].isCat()?Common.getTriGraph(v[1].getCatAt((int)fi).toString()):A[1].getDisplayableValue(fi),X-25,t+5);
                fi+=f;
            };
        }

        g.setColor("point");
        for (int i=0;i<pts;i++)
            if (Pts[i]!=null)
                g.fillOval(Pts[i].x-1,Pts[i].y-1,3,3);

        g.nextLayer();
        
        if (m.marked()>0) {
            g.setColor("marked");
            for (int i=0;i<pts;i++)
                if (Pts[i]!=null && m.at(i))
                    if (selRed)
                        g.fillOval(Pts[i].x-2,Pts[i].y-2,4,4);
                    else
                        g.fillOval(Pts[i].x-1,Pts[i].y-1,3,3);
        };
                
        if (drag) {
            g.nextLayer();
	    int dx1=A[0].clip(x1),dy1=TH-A[1].clip(TH-y1),
		dx2=A[0].clip(x2),dy2=TH-A[1].clip(TH-y2);
	    if (dx1>dx2) { int h=dx1; dx1=dx2; dx2=h; };
	    if (dy1>dy2) { int h=dy1; dy1=dy2; dy2=h; };
	    g.setColor("black");
	    g.drawRect(dx1,dy1,dx2-dx1,dy2-dy1);
	};

	g.end();
        setUpdateRoot(3); // by default no repaint is necessary unless resize occurs
    };

    public void updatePoints() {
	Dimension Dsize=getSize();
	int w=Dsize.width, h=Dsize.height;
	TW=w; TH=h;
	int innerL=30, innerB=30, lshift=0;
	int innerW=w-innerL-10, innerH=h-innerB-10;
	boolean xcat=v[0].isCat(), ycat=v[1].isCat();	
	
	A[0].setGeometry(Axis.O_X,X=innerL,W=innerW);
	A[1].setGeometry(Axis.O_Y,innerB,H=innerH);
	Y=TH-innerB-innerH;


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
	    if (!v[0].isMissingAt(i) && !v[1].isMissingAt(i))
		Pts[i]=new Point(jx+A[0].getCasePos(i),jy+TH-A[1].getCasePos(i));
            else
		Pts[i]=null;
        };
    };

    public void mouseClicked(MouseEvent ev) 
    {
	int x=ev.getX(), y=ev.getY();
	x1=x-2; y1=y-2; x2=x+3; y2=y+3; drag=true; mouseReleased(ev);
    };

    public void mousePressed(MouseEvent ev) 
    {	
	x1=ev.getX(); y1=ev.getY();
	drag=true;
    };
    public void mouseReleased(MouseEvent e)
    {
	int X1=x1, Y1=y1, X2=x2, Y2=y2;
	if (x1>x2) { X2=x1; X1=x2; };
	if (y1>y2) { Y2=y1; Y1=y2; };
	Rectangle sel=new Rectangle(X1,Y1,X2-X1,Y2-Y1);

	int setTo=0;
	if (e.isControlDown()) setTo=1;
	if (!e.isShiftDown()) m.selectNone();
	
	drag=false; 
	int i=0;
	while (i<pts) {
	    if (Pts[i]!=null && sel.contains(Pts[i]))
		m.set(i,m.at(i)?setTo:1);
	    i++;
	};
	m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
        setUpdateRoot(1);
	repaint();	
    };
    public void mouseEntered(MouseEvent e) {};
    public void mouseExited(MouseEvent e) {};
    public void mouseDragged(MouseEvent e) 
    {
	if (drag) {
	    int x=e.getX(), y=e.getY();
	    if (x!=x2 || y!=y2) {
		x2=x; y2=y;
                setUpdateRoot(2);
		repaint();
	    };
	};
    };
    public void mouseMoved(MouseEvent ev) {};

    public void keyTyped(KeyEvent e) 
    {
	if (e.getKeyChar()=='R') run(this,"rotate");
	if (e.getKeyChar()=='l') run(this,"labels");
	if (e.getKeyChar()=='P') run(this,"print");
	if (e.getKeyChar()=='X') run(this,"exportPGS");
	if (e.getKeyChar()=='C') run(this,"exportCases");
	if (e.getKeyChar()=='e') run(this,"selRed");
	if (e.getKeyChar()=='j') run(this,"jitter");
    };
    public void keyPressed(KeyEvent e) {};
    public void keyReleased(KeyEvent e) {};

    public Object run(Object o, String cmd) {
	super.run(o,cmd);
	if (cmd=="rotate") {
	    rotate();
	};
	if (cmd=="labels") {
	    showLabels=!showLabels;
	    MIlabels.setLabel((showLabels)?"Hide labels":"Show labels");
            setUpdateRoot(0);
            repaint();
	};
	if (cmd=="print") run(o,"exportPS");
	if (cmd=="exit") WinTracker.current.Exit();
        if (cmd=="selRed") { selRed=!selRed; setUpdateRoot(1); repaint(); };
        if (cmd=="jitter") {
            jitter=!jitter; updatePoints(); setUpdateRoot(0); repaint();
        }

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
    };
};
