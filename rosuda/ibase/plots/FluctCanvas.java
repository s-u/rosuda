import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.PrintStream;

/** implementation of scatterplots
    @version $Id$
*/
class FluctCanvas extends PGSCanvas implements Dependent, MouseListener, MouseMotionListener, KeyListener, ActionListener, Commander
{
    /** array of two variables (X and Y) */
    SVar v[];
    /** associated marker */
    SMarker m;
    /** weight variable */
    SVar weight;
    
    boolean showLabels=true;

    /** use trigraph for X axis in case X is categorical */
    boolean useX3=false; 
    /** array of two axes (X and Y) */
    Axis A[];

    /** cell counts/weight sums (y*v1l+x) */
    double[] Counts;
    double[] Marked;
    /** length of var 1 and 2 in counts (ergo Counts is of size v1l*v2l) */
    int v1l,v2l;
    /** maximum in counts */
    double maxCount;
    
    int x1, y1, x2, y2;
    boolean drag=false, mvX=false, mvY=false; /* drag inside or move X/Y */
    boolean centered=false;
    int dragNew;
    int mvXstart, mvYstart;

    MenuItem MIlabels=null;

    int X,Y,W,H, TW,TH;

    /** create a new (weighted) fluctuation diagram
	@param f associated frame (or <code>null</code> if none)
	@param v1 variable 1
	@param v2 variable 2
	@param mark associated marker
        @param wght weight variable or null for counts */
    public FluctCanvas(Frame f, SVar v1, SVar v2, SMarker mark, SVar wght) {
        super(2); // 2 layers; 0=base, 1=drag
        weight=wght;
        setFrame(f); setTitle(((weight==null)?"FD":"WFD")+" ("+v1.getName()+" : "+v2.getName()+")"+((weight==null)?"":"*"+weight.getName()));
	v=new SVar[2]; A=new Axis[2];
	v[0]=v1; v[1]=v2; m=mark;
        A[0]=new Axis(v1,Axis.O_X,Axis.T_EqCat); A[0].addDepend(this);
        A[1]=new Axis(v2,Axis.O_Y,Axis.T_EqCat); A[1].addDepend(this);
	setBackground(Common.backgroundColor);
	drag=false;
	updatePoints();
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this); f.addKeyListener(this);
	MenuBar mb=null;
	String myMenu[]={"+","File","~File.Graph","~Edit","+","View","!RRotate","rotate","@LHide labels","labels","Toggle alignment","center","~Window","0"};
	EzMenu.getEzMenu(f,this,myMenu);
	MIlabels=EzMenu.getItem(f,"labels");	
    }

    public FluctCanvas(Frame f, SVar v1, SVar v2, SMarker mark) { this(f,v1,v2,mark,null); }

    public Dimension getMinimumSize() { return new Dimension(60,50); }

    public void rotate() {
	SVar h=v[0]; v[0]=v[1]; v[1]=h;
	Axis ha=A[0]; A[0]=A[1]; A[1]=ha;
	try {
	    ((Frame) getParent()).setTitle("FD ("+v[1].getName()+" vs "+v[0].getName()+")");
	} catch (Exception ee) {};
	updatePoints();
        setUpdateRoot(0);
	repaint();
    }

    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        updatePoints();
        setUpdateRoot(0);
        repaint();
    }

    public void paintPoGraSS(PoGraSS g) {
	Rectangle r=getBounds();
	g.setBounds(r.width,r.height);
	g.begin();
	g.defineColor("white",255,255,255);
        g.defineColor("marked",128,255,128);
	g.defineColor("black",0,0,0);
	g.defineColor("outline",0,0,0);
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

	//g.setColor("white");
	//g.fillRect(X,Y,W,H);

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
                    g.drawString(v[0].isCat()?((useX3)?Common.getTriGraph(v[0].getCatAt((int)fi).toString()):v[0].getCatAt((int)fi).toString()):
                                 A[0].getDisplayableValue(fi),t-5,Y+H+20);
                fi+=f;
            };
        }

        {
            double f=A[1].getSensibleTickDistance(50,18);
            double fi=A[1].getSensibleTickStart(f);
            //if (Common.DEBUG>0)
            //System.out.println("SP.A[1]:"+A[1].toString()+", distance="+f+", start="+fi);
            while (fi<A[1].vBegin+A[1].vLen) {
                int t=A[1].getValuePos(fi);
                g.drawLine(X-5,t,X,t);
                if(showLabels)
                    g.drawString(v[1].isCat()?Common.getTriGraph(v[1].getCatAt((int)fi).toString()):A[1].getDisplayableValue(fi),X-25,t+5);
                fi+=f;
            };
        }

        int pic=0;
        for (int yp=0;yp<v2l;yp++)
            for (int xp=0;xp<v1l;xp++) {
                double ct=Counts[pic]; double mct=Marked[pic];
                pic++;
                if (ct>0) {
                    int lx=A[0].getCatLow(xp);
                    int ly=A[1].getCatLow(yp);
                    int dx=A[0].getCatUp(xp)-lx;
                    int dy=A[1].getCatUp(yp)-ly;
                    if (dx<0) { lx+=dx; dx=-dx; };
                    if (dy<0) { ly+=dy; dy=-dy; };
                    g.setColor("white");
                    int rdx=(int)(((double)dx)*Math.sqrt(ct/maxCount));
                    int rdy=(int)(((double)dy)*Math.sqrt(ct/maxCount));
                    int mdy=(int)(((double)rdy)*mct/ct);
		    if (centered) {
			lx+=(dx-rdx)/2;
			ly+=(dy-rdy)/2;
		    }
		    g.fillRect(lx,ly,rdx,rdy);
                    if (mdy>0) {
                        g.setColor("marked");
                        g.fillRect(lx,ly+rdy-mdy,rdx,mdy);
                    };
                    g.setColor((mct>0)?"red":"black");
                    g.drawRect(lx,ly,rdx,rdy);
                };
            } ;

	paintDragLayer(g);

	g.end();
        setUpdateRoot(2); // by default no repaint is necessary unless resize occurs
    }

    void paintDragLayer(PoGraSS g) {
        if (drag || mvX || mvY) {
            nextLayer(g);
	    if (drag) {
		int dx1=A[0].clip(x1),dy1=A[1].clip(y1),
		    dx2=A[0].clip(x2),dy2=A[1].clip(y2);
		if (dx1>dx2) { int h=dx1; dx1=dx2; dx2=h; };
		if (dy1>dy2) { int h=dy1; dy1=dy2; dy2=h; };
		g.setColor("black");
		g.drawRect(dx1,dy1,dx2-dx1,dy2-dy1);
	    };
	    if (mvX) {
		int myXl=A[0].getCatLow(dragNew);
		int myXh=A[0].getCatUp(dragNew);
		g.setColor("red");
		if (myXh<myXl) { int h=myXl; myXl=myXh; myXh=h; };
		g.drawRect(myXl,Y+H,myXh-myXl,2);		
	    }
	    if (mvY) {
		int myYl=A[1].getCatLow(dragNew);
		int myYh=A[1].getCatUp(dragNew);
		g.setColor("red");
		if (myYh<myYl) { int h=myYl; myYl=myYh; myYh=h; };
		g.drawRect(X,myYl,2,myYh-myYl);	
	    }
	}
    }

    public void updatePoints() {
	Dimension Dsize=getSize();
	int w=Dsize.width, h=Dsize.height;
	TW=w; TH=h;
	int innerL=30, innerB=30, lshift=0;
	int innerW=w-innerL-10, innerH=h-innerB-10;
	boolean xcat=v[0].isCat(), ycat=v[1].isCat();	
	
	A[0].setGeometry(Axis.O_X,X=innerL,W=innerW);
	A[1].setGeometry(Axis.O_Y,TH-innerB,-(H=innerH));
	Y=TH-innerB-innerH;

        v1l=v[0].getNumCats(); v2l=v[1].getNumCats();
        Counts=new double[v1l*v2l]; Marked=new double[v1l*v2l]; maxCount=0;
        int pts=v[0].size();
        if (pts>v[1].size()) pts=v[1].size();
        for (int i=0;i<pts;i++) {
            int xc=v[0].getCatIndex(i);
            int yc=v[1].getCatIndex(i);
            if (xc>=0 && yc>=0) {
                double wv=(weight==null)?1.0:weight.atD(i);
                if (wv<0) wv=-wv;
                Counts[xc+yc*v1l]+=wv;
                if (Counts[xc+yc*v1l]>maxCount) maxCount=Counts[xc+yc*v1l];
                if (m.at(i))
                    Marked[xc+yc*v1l]+=wv;
            }
        };
    }

    public void mouseClicked(MouseEvent ev) 
    {
	int x=ev.getX(), y=ev.getY();
        //x1=x-2; y1=y-2; x2=x+3; y2=y+3; drag=true; mouseReleased(ev);
        boolean setTo=false;
        
        if (ev.isControlDown()) setTo=true;
        if (!ev.isShiftDown()) m.selectNone();

        int pts=v[0].size();
        if (pts>v[1].size()) pts=v[1].size();
        for (int yp=0;yp<v2l;yp++)
            for (int xp=0;xp<v1l;xp++) {
                int lx=A[0].getCatLow(xp);
                int ly=A[1].getCatLow(yp);
                int dx=A[0].getCatUp(xp)-lx;
                int dy=A[1].getCatUp(yp)-ly;
                if (dx<0) { lx+=dx; dx=-dx; };
                if (dy<0) { ly+=dy; dy=-dy; };
                if (x<lx+dx&&x>lx&&y<ly+dy&&y>ly) {
                    for (int i=0;i<pts;i++) {
                        int xc=v[0].getCatIndex(i);
                        int yc=v[1].getCatIndex(i);
                        if (xc==xp && yc==yp)
                            m.set(i,m.at(i)?setTo:true);
                    };
                };
            };

        drag=false;
        m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
        setUpdateRoot(0);
        repaint();        
    }

    public void mousePressed(MouseEvent ev) 
    {	
	int x=x1=ev.getX(); int y=y1=ev.getY();
	
	if (x<X || y>Y+H) { /* border */
	    if (x<X) {
		for (int yp=0;yp<v2l;yp++) {
                    int ly=A[1].getCatLow(yp);
                    int dy=A[1].getCatUp(yp);
		    if (ly>dy) { int h=ly; ly=dy; dy=h; };
		    if (y>ly && y<dy) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));		
			mvY=true; mvYstart=yp; return;
		    }
		}
	    } else {
		for (int xp=0;xp<v1l;xp++) {
                    int lx=A[0].getCatLow(xp);
                    int dx=A[0].getCatUp(xp);
		    if (lx>dx) { int h=lx; lx=dx; dx=h; };
		    if (x>lx && x<dx) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));		
			mvX=true; mvXstart=xp; return;
		    }
		}
	    };
	} else
	    drag=true;
    }

    public void mouseReleased(MouseEvent e)
    {
        if (e.getX()==x1 && e.getY()==y1) return; // if the current is same as entry then get out, since CLICK should handle this
	if (mvX || mvY) {
	    mvX=false; mvY=false;
	    setUpdateRoot(0);
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	    repaint();
	} else {
	    int X1=x1, Y1=y1, X2=x2, Y2=y2;
	    if (x1>x2) { X2=x1; X1=x2; };
	    if (y1>y2) { Y2=y1; Y1=y2; };
	    Rectangle sel=new Rectangle(X1,Y1,X2-X1,Y2-Y1);
	    
	    boolean setTo=false;
	    if (e.isControlDown()) setTo=true;
	    if (!e.isShiftDown()) m.selectNone();
	    
	    int pts=v[0].size();
	    if (pts>v[1].size()) pts=v[1].size();
	    for (int yp=0;yp<v2l;yp++)
		for (int xp=0;xp<v1l;xp++) {
		    int lx=A[0].getCatLow(xp);
		    int ly=A[1].getCatLow(yp);
		    int dx=A[0].getCatUp(xp)-lx;
		    int dy=A[1].getCatUp(yp)-ly;
		    if (dx<0) { lx+=dx; dx=-dx; };
		    if (dy<0) { ly+=dy; dy=-dy; };
		    if (X1<lx+dx&&X2>lx&&Y1<ly+dy&&Y2>ly) {
			for (int i=0;i<pts;i++) {
			    int xc=v[0].getCatIndex(i);
			    int yc=v[1].getCatIndex(i);
			    if (xc==xp && yc==yp)
				m.set(i,m.at(i)?setTo:true);
			};
		    };
		};
	    
	    drag=false;
	    int i=0;
	    m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
	    setUpdateRoot(0);
	    repaint();	
	};
    };
    public void mouseEntered(MouseEvent e) {};
    public void mouseExited(MouseEvent e) {};
    public void mouseDragged(MouseEvent e) 
    {
	if (drag) {
	    int x=e.getX(), y=e.getY();
	    if (x!=x2 || y!=y2) {
		x2=x; y2=y;
                setUpdateRoot(1);
		repaint();
	    };
	};
	if (mvX) {
	    int dragX=e.getX(); int dragY=e.getY();
	    dragNew=A[0].getCatByPos(dragX);
	    //System.out.println("dragX="+dragX+" dragY="+dragY+" dragNew="+dragNew);
            setUpdateRoot(1);
	    if (dragNew!=mvXstart) {
		A[0].moveCat(mvXstart,A[0].getCatSeqIndex(dragNew));
                setUpdateRoot(0);
	    };
	    repaint();
	};
	if (mvY) {
	    int dragX=e.getX(); int dragY=e.getY();
	    dragNew=A[1].getCatByPos(dragY);
	    //System.out.println("dragX="+dragX+" dragY="+dragY+" dragNew="+dragNew);
            setUpdateRoot(1);
	    if (dragNew!=mvYstart) {
		A[1].moveCat(mvYstart,A[1].getCatSeqIndex(dragNew));
                setUpdateRoot(0);
	    };
	    repaint();
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
	if (e.getKeyChar()=='c') run(this,"center");
 	if (e.getKeyChar()=='t') run(this,"trigraph");
    }
    
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

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
	if (cmd=="center") {
	    centered=!centered;
	    setUpdateRoot(0);
	    repaint();
	};
	if (cmd=="print") run(o,"exportPS");
        if (cmd=="trigraph") { useX3=!useX3; setUpdateRoot(0); repaint(); }
	if (cmd=="exit") WinTracker.current.Exit();
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
    }

    public void actionPerformed(ActionEvent e) {
	if (e==null) return;
	run(e.getSource(),e.getActionCommand());
    }
}
