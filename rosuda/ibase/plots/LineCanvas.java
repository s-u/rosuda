import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.PrintStream;

/** implementation of line plot
    @version $Id$
*/
class LineCanvas extends PGSCanvas implements Dependent, MouseListener, MouseMotionListener, KeyListener, ActionListener, Commander
{
    public static final int LT_DIRECT = 0;
    public static final int LT_RECT   = 1;

    /** line type */
    int type=LT_DIRECT;
    
    /** variables; 0=x, 1,2,3...=Y */
    SVar v[];
    /** associated marker */
    SMarker m;

    /** flag whether axis labels should be shown */
    boolean showLabels=true;

    /** flag whether jittering should be used in case X is categorical */
    boolean jitter=false;

    /** flag whether alternative selection style should be used */
    boolean selRed=false;

    /** use trigraph for X axis in case X is categorical */
    boolean useX3=false; 
    
    /** array of axes */
    Axis A[];

    int x1, y1, x2, y2;
    boolean drag;

    MenuItem MIlabels=null;

    int X,Y,W,H, TW,TH;
    double totMin, totMax;
    /** create a new lineplot
	@param f associated frame (or <code>null</code> if none)
	@param v1 variable 1
	@param v2 variable 2
	@param mark associated marker */
    public LineCanvas(Frame f, SVar xv, SVar[] yvs, SMarker mark) {
        super(3); // 3 layers; 0=base+points, 1=selected, 2=drag
	setFrame(f); setTitle("Lineplot");
	v=new SVar[yvs.length+1];
	A=new Axis[2];
	m=mark;
	int i=0;
	while(i<yvs.length) {
	    if (i==0) {
		totMin=yvs[i].getMin(); totMax=yvs[i].getMax();
	    } else {
		if (yvs[i].getMin()<totMin) totMin=yvs[i].getMin();
		if (yvs[i].getMax()>totMax) totMax=yvs[i].getMax();
	    };
	    v[i+1]=yvs[i]; i++;
	};
	A[1]=new Axis(yvs[0],Axis.O_Y,Axis.T_Num); A[1].addDepend(this);
	A[1].setValueRange(totMin,totMax);
	if (xv==null) {
	    xv=new SVar("index.LC");
	    i=1; while(i<=v[1].size()) { xv.add(new Integer(i)); i++; };
	};
	v[0]=xv; A[0]=new Axis(v[0],Axis.O_X,v[0].isCat()?Axis.T_EqCat:Axis.T_Num); A[0].addDepend(this);
	setBackground(Common.backgroundColor);
	drag=false;
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this); f.addKeyListener(this);
	MenuBar mb=null;
	String myMenu[]={"+","File","Save as PGS ...","exportPGS","Save as PostScript ...","exportPS","-","Save selected as ...","exportCases","-","Close","WTMclose","Quit","exit","+","Edit","Select all","selAll","Select none","selNone","Invert selection","selInv","+","View","Rotate","rotate","Hide labels","labels","Toggle hilight. style","selRed","Toggle jittering","jitter","0"};
	f.setMenuBar(mb=WinTracker.current.buildQuickMenuBar(f,this,myMenu,false));
	MIlabels=mb.getMenu(2).getItem(1);	
    };

    public Axis getXAxis() { return A[0]; };
    
    public Dimension getMinimumSize() { return new Dimension(60,50); };

    public void rotate() {
	/*
	SVar h=v[0]; v[0]=v[1]; v[1]=h;
	Axis ha=A[0]; A[0]=A[1]; A[1]=ha;
	try {
	    ((Frame) getParent()).setTitle("Scatterplot ("+v[1].getName()+" vs "+v[0].getName()+")");
	} catch (Exception ee) {};
        setUpdateRoot(0);
	repaint();
	*/
    };

    public void setLineType(int nt) {
        type=nt; setUpdateRoot(0); repaint();
    }
    
    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        setUpdateRoot((msg.getMessageID()==Common.NM_MarkerChange)?1:0);
        repaint();
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
	g.defineColor("line",0,0,128); // color of line plot
	g.defineColor("lines",96,96,255);	
	g.defineColor("selText",255,0,0);
	g.defineColor("selBg",255,255,192);
	g.defineColor("splitRects",128,128,255);

	Dimension Dsize=getSize();
	if (Dsize.width!=TW || Dsize.height!=TH) {
	    int w=Dsize.width, h=Dsize.height;
	    TW=w; TH=h;
	    int innerL=30, innerB=30, lshift=0;
	    int innerW=w-innerL-10, innerH=h-innerB-10;
	
	    A[0].setGeometry(Axis.O_X,X=innerL,W=innerW);
	    A[1].setGeometry(Axis.O_Y,innerB,H=innerH);
	    Y=TH-innerB-innerH;
	};

	if (TW<50||TH<50) {
	    g.setColor("red");
	    g.drawLine(0,0,TW,TH); 
	    g.drawLine(0,TH,TW,0); 
	    return;
	};

	g.setColor("white");
	g.fillRect(X,Y,W,H);
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
                                 A[0].getDisplayableValue(fi),t-5,Y+H+20);
                fi+=f;
            };
        }

	/* draw ticks and labels for Y axis */
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

	g.setColor("line");
	for (int j=1;j<v.length;j++) {
	    for (int i=1;i<v[0].size();i++)
                if (type==LT_DIRECT) {
                    g.drawLine(A[0].getCasePos(i-1),TH-A[1].getValuePos(v[j].atD(i-1)),
                               A[0].getCasePos(i),TH-A[1].getValuePos(v[j].atD(i)));
                } else {
                    g.drawLine(A[0].getCasePos(i-1),TH-A[1].getValuePos(v[j].atD(i-1)),
                               A[0].getCasePos(i),TH-A[1].getValuePos(v[j].atD(i-1)));
                    g.drawLine(A[0].getCasePos(i),TH-A[1].getValuePos(v[j].atD(i-1)),
                               A[0].getCasePos(i),TH-A[1].getValuePos(v[j].atD(i)));
                };
	};
	
        g.nextLayer();
        
	/*
        if (m.marked()>0) {
            g.setColor("marked");
            for (int i=0;i<m.size();i++)
                if (m.at(i))
                    if (selRed)
                        g.fillOval(Pts[i].x-2,Pts[i].y-2,4,4);
                    else
                        g.fillOval(Pts[i].x-1,Pts[i].y-1,3,3);
			};*/
                
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
	/*
	int i=0;
	while (i<pts) {
	    if (Pts[i]!=null && sel.contains(Pts[i]))
		m.set(i,m.at(i)?setTo:1);
	    i++;
	    }; */
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
	if (e.getKeyChar()=='t') run(this,"trigraph");
    };
    public void keyPressed(KeyEvent e) {};
    public void keyReleased(KeyEvent e) {};

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
	if (cmd=="print") run(o,"exportPS");
	if (cmd=="exit") WinTracker.current.Exit();
        if (cmd=="selRed") { selRed=!selRed; setUpdateRoot(1); repaint(); };
        if (cmd=="jitter") {
            jitter=!jitter; setUpdateRoot(0); repaint();
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
    };
};
