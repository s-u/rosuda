import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/** OrdStats - ordinal statistics of a variable, used internally by {@link BoxCanvas}
    to get necessary information to plot bopxplots */
class OrdStats { // get ordinal statistics to be used in boxplot
    double med, uh, lh, uh15, lh15, uh3, lh3;
    int[] lastR;
    int lastTop;
    /** indexes of points just above/below the 1.5 hinge
	beware, this is relative to the used r[] so
	use with care and only with the corresponding r[] */
    int lowEdge, highEdge; 

    OrdStats() { med=uh=lh=uh3=lh3=0; };

    double medFrom(SVar v,int[] r,int min,int max) {
	return (((max-min)&1)==0)
	    ?v.atF(r[min+(max-min)/2])
	    :((v.atF(r[min+(max-min)/2])+v.atF(r[min+(max-min)/2+1]))/2);
    };

    void update(SVar v, int[] r) {
	update(v,r,r.length);
    };
    
    /* v=variable, r=ranked index as returned by getRanked, n=# of el to use */
    void update(SVar v, int[] r, int n) {
	lastTop=n;
	med=medFrom(v,r,0,n-1);
	uh=medFrom(v,r,n/2,n-1);
	if ((n&1)==1)
	    lh=medFrom(v,r,0,n/2-1);
	else
	    lh=medFrom(v,r,0,n/2);
	lh15=lh-(double)1.5*(uh-lh);
	lh3=lh-3*(uh-lh);
	double x=lh;
	int i=n/4; // find lh15 as extreme between lh and lh15
	while (i>=0) {
	    double d=v.atF(r[i]);
	    if (d<lh15) break;
	    if (d<x) x=d;
	    i--;
	};
	lowEdge=i;
	lh15=x;
	uh15=uh+(double)1.5*(uh-lh);
	uh3=uh+3*(uh-lh);
	x=uh;
	i=n*3/4-1; if (i<0) i=0; // find uh15
	while (i<n) {
	    double d=v.atF(r[i]);
	    if (d>uh15) break;
	    if (d>x) x=d;
	    i++;
	};
	uh15=x;
	highEdge=i;
	lastR=r;
    };
};

/** BoxCanvas - implementation of the boxplots
    @version $Id$
*/
class BoxCanvas extends PGSCanvas implements Dependent, MouseListener, MouseMotionListener, KeyListener, Commander
{
    /** associated numerical variable */
    SVar v;
    /** associated categorical variable if {@link vsCat} is <code>true</code> */
    SVar cv;
    /** associated marker */
    SMarker m;
    /** if <code>true</code> then side-by-side bosplots grouped by {@link cv} are drawn,
	otherwise draw just a single boxpolot */
    boolean vsCat=false;
    boolean valid=false, dragMode=false, areMarked=false;
    int dragX, dragY;
    boolean vertical=true;
    Axis a;

    // for vsCat version
    int rk[][];
    int rs[];
    int cs;
    Object cats[];
    OrdStats oss[];

    // for plain version
    OrdStats OSdata;
    OrdStats OSsel;

    /** create a boxplot canvas for a single boxplot
	@param f associated frame (or <code>null</code> if none)
	@param var source variable
	@param mark associated marker */
    public BoxCanvas(Frame f, SVar var, SMarker mark) {
	v=var; m=mark; setFrame(f);
	setTitle("Boxplot ("+v.getName()+")");
	a=new Axis(v,Axis.O_Y,Axis.T_Num);
	setBackground(new Color(255,255,192));
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this); f.addKeyListener(this);
	if (var!=null && !var.isCat() && var.isNum())
	    valid=true; // valid are only numerical vars non-cat'd
	if (valid) {
	    OSdata=new OrdStats();
	    OSsel=new OrdStats();
	    int dr[]=v.getRanked();
	    OSdata.update(v,dr);
	    updateBoxes();
	};
    };

    /** create a boxplot canvas for a multiple grouped boxplots side-by-side
	@param f associated frame (or <code>null</code> if none)
	@param var source numerical variable
	@param cvar categorical variable for grouping
	@param mark associated marker */
    public BoxCanvas(Frame f, SVar var, SVar cvar, SMarker mark) { // multiple box vs cat
	v=var; m=mark; cv=cvar; setFrame(f);
	setTitle("Boxplot ("+v.getName()+" grouped by "+cv.getName()+")");
	a=new Axis(v,Axis.O_Y,Axis.T_Num);
	setBackground(new Color(255,255,192));
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this); f.addKeyListener(this);
	if (var!=null && !var.isCat() && var.isNum() && cvar.isCat())
	    valid=true; // valid are only numerical vars non-cat'd, cvar is cat
	if (valid) { // split into ranked chunks by cat.
	    vsCat=true;
	    cs=cv.getNumCats();
	    cats=cv.getCategories();
	    int[] r=SVar.getRanked(v,null,0);
	    oss=new OrdStats[cs*2+2];
	    rk=new int[cs*2+2][];
	    rs=new int[cs*2+2];
	    int i=0;
	    while (i<cs) {
		rs[i]=0; 
		int j=cv.getSizeCatAt(i);
		rk[i]=new int[j];
		rk[cs+1+i]=new int[j];
		oss[i]=new OrdStats();
		oss[cs+1+i]=new OrdStats();
		i++;
	    };
	    i=0;
	    while(i<r.length) {
		int x=cv.getCatIndex(cv.at(r[i]));
		if (x<0) x=cs;
		rk[x][rs[x]]=r[i];
		rs[x]++;
		i++;
	    };
	    i=0;
	    while(i<cs) {
		oss[i].update(v,rk[i],rs[i]);
		i++;
	    };
	    updateBoxes();
	};
    };

    public Dimension getMinimumSize() { return new Dimension(60,50); };

    public void Notifying(Object o, Vector path) {
	updateBoxes();
	repaint();
    };

    public void updateBoxes() {
	if (!valid) return;
	int md[]=SVar.getRanked(v,m,1);
	areMarked=(md!=null);
	if (vsCat) {
	    if (areMarked) {
		int i=0;
		while (i<cs) { rs[cs+1+i]=0; i++; };
		i=0;
		while(i<md.length) {
		    int x=cv.getCatIndex(cv.at(md[i]));
		    if (x<0) x=cs;
		    x+=cs+1;
		    rk[x][rs[x]]=md[i];
		    rs[x]++;
		    i++;
		};		
		i=cs+1;
		while(i<2*cs+1) {
		    oss[i].update(v,rk[i],rs[i]);
		    i++;
		};
	    };
	} else {
	    if (areMarked)
		OSsel.update(v,md);
	};
    };

    void drawBox(PoGraSS g, OrdStats os, int x, int w, String fillColor, String drawColor) {
	Rectangle r=getBounds();
	if (fillColor!=null) {
	    g.setColor(fillColor);
	    g.fillRect(x,a.getValuePos(os.uh),
		       w,a.getValuePos(os.lh)-a.getValuePos(os.uh));
	};
	g.setColor(drawColor);
	g.drawRect(x,a.getValuePos(os.uh),
		   w,a.getValuePos(os.lh)-a.getValuePos(os.uh));
	g.drawLine(x,a.getValuePos(os.med),
		   x+w,a.getValuePos(os.med));
	g.drawLine(x,a.getValuePos(os.uh15),
		   x+w,a.getValuePos(os.uh15));
	g.drawLine(x,a.getValuePos(os.lh15),
		   x+w,a.getValuePos(os.lh15));
	g.drawLine(x+w/2,a.getValuePos(os.uh),
		   x+w/2,a.getValuePos(os.uh15));
	g.drawLine(x+w/2,a.getValuePos(os.lh),
		   x+w/2,a.getValuePos(os.lh15));
	int i=os.lowEdge;
	while(i>=0) {
	    double val=v.atF(os.lastR[i]);
	    if (val<os.lh3)
		g.drawOval(x+w/2-2,a.getValuePos(val)-2,3,3);
	    else
		g.fillRect(x+w/2-1,a.getValuePos(val)-1,2,2);
	    i--;
	};
	i=os.highEdge;
	while(i<os.lastTop) {
	    double val=v.atF(os.lastR[i]);
	    if (val>os.uh3)
		g.drawOval(x+w/2-2,a.getValuePos(val)-2,3,3);
	    else
		g.fillRect(x+w/2-1,a.getValuePos(val)-1,2,2);
	    i++;
	};
	
    };
    
    public void paintPoGraSS(PoGraSS g) {
	Rectangle r=getBounds();
	g.setBounds(r.width,r.height);
	g.begin();
	if (!valid) {
	    g.defineColor("red",255,0,0);
	    g.drawLine(0,0,r.width,r.height);
	    g.drawLine(0,r.height,r.width,0);
	    g.end();
	    return;
	};
	g.defineColor("white",255,255,255);
	g.defineColor("black",0,0,0);
	g.defineColor("selfill",0,255,0);
	g.defineColor("sel",0,128,0);
	if (vertical)
	    a.setGeometry(Axis.O_Y,r.height-20,-r.height+30);
	else
	    a.setGeometry(Axis.O_X,40,r.width-50);
	if (!vsCat) {
	    drawBox(g,OSdata,10,20,"white","black");
	    if (areMarked)
		drawBox(g,OSsel,18,10,"selfill","sel");
	} else {
	    int i=0;
	    while(i<cs) {		
		drawBox(g,oss[i],10+40*i,20,"white","black");
		if (areMarked && rs[cs+1+i]>0)
                    drawBox(g,oss[cs+1+i],18+40*i,10,"selfill","sel");

                g.drawString(Common.getTriGraph(cv.getCatAt(i).toString()),12+40*i,r.height-10);
                i++;
	    };
	};
	g.end();
    };
    
    public void mouseClicked(MouseEvent ev) 
    {
	int x=ev.getX(), y=ev.getY();
	/*
	boolean effect=false;
	if (ev.isControlDown()) setTo=1;
	while (i<bars) {
	    if (Bars[i].contains(x,y)) {
		effect=true;
		if (!ev.isShiftDown()) m.selectNone();
		Object[] cts=v.getCategories();		    
		int j=0;
		for (Enumeration e=v.elements(); e.hasMoreElements();) {
		    Object o=e.nextElement();
		    String cn=null;
		    if (o==null) cn=SVar.missingCat; else cn=o.toString();
		    if (cts[cat_seq[i]].toString().compareTo(cn)==0)
			m.set(j,m.at(j)?setTo:1);			
		    j++;
		};
	    };
	    i++;
	};
	if (effect) m.NotifyAll();
	*/
    };

    public void mousePressed(MouseEvent ev) {
	int x=ev.getX(), y=ev.getY();
    };
    public void mouseReleased(MouseEvent e) {
	if (dragMode) {
	};
    };

    public void mouseDragged(MouseEvent e) 
    {
	if (dragMode) {
	    dragX=e.getX(); dragY=e.getY();
	    repaint();
	};
    };

    public void mouseMoved(MouseEvent ev) {};
    public void mouseEntered(MouseEvent e) {};
    public void mouseExited(MouseEvent e) {};

    public void keyTyped(KeyEvent e) 
    {
	if (e.getKeyChar()=='R') run(this,"rotate");
	if (e.getKeyChar()=='P') run(this,"print");
	if (e.getKeyChar()=='X') run(this,"exportPGS");
	if (e.getKeyChar()=='C') run(this,"exportCases");
    };
    public void keyPressed(KeyEvent e) {};
    public void keyReleased(KeyEvent e) {};

    public Object run(Object o, String cmd) {
	super.run(o,cmd);
	if (cmd=="rotate") {
	    //    rotate();
	};
	if (cmd=="print") run(o,"exportPS");
        if (cmd=="exportCases") {
	    try {
		PrintStream p=Tools.getNewOutputStreamDlg(myFrame,"Export selected cases to ...","selected.txt");
		if (p!=null) {
		    p.println(v.getName()+(vsCat?("\t"+cv.getName()):""));
		    int i=0;
		    for (Enumeration e=v.elements(); e.hasMoreElements();) {
			Object oo=e.nextElement();
			if (m.at(i)) {
			    if (vsCat)
				p.println((oo==null)?"NA":oo.toString()+"\t"+cv.at(i).toString());
			    else
				p.println((oo==null)?"NA":oo.toString());
			};
			i++;
		    };
		    p.close();
		};
	    } catch (Exception eee) {};
	};
	return null;
    };
};
