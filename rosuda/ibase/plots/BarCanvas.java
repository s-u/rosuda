import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/** BarCanvas - implementation of the barcharts
    @version $Id$
*/
class BarCanvas extends PGSCanvas implements Dependent, MouseListener, MouseMotionListener, KeyListener, Commander, ActionListener
{
    /** corresponding variable */
    SVar v;
    /** corresponding marker */
    SMarker m;
    /** axes */
    Axis ax,ay;
    
    Rectangle[] Bars;
    int[] cat_seq;
    String[] cat_nam;
    int cats;

    int botSpace=20;
    int topSpace=10;
    int sideSpace=10;

    int countMax=0;
    int count[];
    int marked[];

    int ow=0,oh=0;

    int bars=20;
    boolean dragMode=false;
    boolean isSpine=false;
    int dragBar, dragX, dragY, dragW, dragH, dragNew;

    MenuItem MIspine=null;

    /** creates a barchart
	@param f associated frame (or <code>null</code> if common default frame is to be used)
	@param var associated variable
	@param mark associated marker */
    public BarCanvas(Frame f, SVar var, SMarker mark) {
        super(2); // 2 layers; 0=bars, 1=drag
	v=var; m=mark; setFrame(f); setTitle("Barchart ("+v.getName()+")");
	ax=new Axis(v,Axis.O_X,Axis.T_EqCat);
	ax.addDepend(this);
	ay=new Axis(v,Axis.O_Y,Axis.T_EqSize);
	ay.addDepend(this);
	setBackground(new Color(255,255,192));
	addMouseListener(this);
	addMouseMotionListener(this);
        addKeyListener(this); f.addKeyListener(this);
	cats=v.getNumCats();
	bars=cats;
	if (v.hasMissing()) bars++;
	
	Bars=new Rectangle[bars];
	updateBars();
	MenuBar mb=null;
	String myMenu[]={"+","File","Save as PGS ...","exportPGS","Save as PostScript ...","exportPS","-","Save selected as ...","exportCases","-","Close","WTMclose","Quit","exit","+","View","Spineplot","spine","0"};
	f.setMenuBar(mb=WinTracker.current.buildQuickMenuBar(f,this,myMenu,false));
	MIspine=mb.getMenu(1).getItem(0);
    };

    /** notification handler - rebuilds bars and repaints */
    public void Notifying(NotifyMsg msg, Object o, Vector path) {
	updateBars();
        setUpdateRoot(0);
	repaint();
    };

    /** rebuilds bars */
    public void updateBars() {
	countMax=0;
	count=new int[bars];
	marked=new int[bars];
	Object[] cts=v.getCategories();
	cat_nam=new String[cts.length+1];
	int j=0;
	while (j<cats) {
	    cat_nam[j]=cts[j].toString();
	    j++;
        };
        cat_nam[j]="n/a"; // if you see this category, then somehting's wrong as getCatIndex returns -1
	j=0;
	while (j<v.size()) {
	    int i=v.getCatIndex(j);
	    if (i==-1) i=cats;
	    count[i]++;
	    if ((m!=null)&&(m.at(j))) marked[i]++;
	    if (count[i]>countMax) countMax=count[i];
	    j++;
	};
	ay.setValueRange(countMax);
	/*
	Dimension Dsize=getSize();
	int w=Dsize.width, h=Dsize.height;
	if (oh!=h) 
	    ay.setGeometry(Axis.O_Y,topSpace,h-topSpace-botSpace);
	if (ow!=w)
	    ax.setGeometry(Axis.O_X,sideSpace,w-2*sideSpace);
	    ow=w; oh=h; */
	//System.out.println("BarCanvas.updateBars\n ax="+ax.toString()+"\n ay="+ay.toString());
    };

    public Dimension getMinimumSize() { return new Dimension(sideSpace*2+30,topSpace+botSpace+30); };

    /** actual paint method */
    public void paintPoGraSS(PoGraSS g) {
	Rectangle r=getBounds();
	int w=r.width, h=r.height;
	if (oh!=h) 
	    ay.setGeometry(Axis.O_Y,botSpace,h-topSpace-botSpace);
	if (ow!=w)
	    ax.setGeometry(Axis.O_X,sideSpace,w-2*sideSpace);
	//System.out.println("BarCanvas.paint:\n ax="+ax.toString()+"\n ay="+ay.toString());
	ow=w; oh=h;
	int basey=h-botSpace;
	g.setBounds(w,h);
	g.begin();
	g.defineColor("axes",0,0,0);
	g.defineColor("outline",0,0,0);
	g.defineColor("fill",255,255,255);
	g.defineColor("sel",128,255,128);
	g.defineColor("drag",255,0,0);

	if (bars==0) return;
	
	g.drawLine(sideSpace,basey,w-2*sideSpace,basey); 

	int i=0;
	int lh=ay.getCasePos(0);
	while(i<bars) {
	    g.setColor("fill");
	    int cl=ax.getCatLow(i);
	    int cu=ax.getCatUp(i);
	    int cd=cu-cl;
	    cu-=cd/10;
	    cl+=cd/10;
	    int ch=ay.getCasePos(count[i]);
	    if (isSpine) ch=lh+ay.gLen;
	    //System.out.println(">>Bar["+i+"] cl="+cl+", cu="+cu+", ch="+ch+" (w="+w+",h="+h+")");
	    g.fillRect(cl,h-ch,cu-cl,ch-lh);
	    Bars[i]=new Rectangle(cl,h-ch,cu-cl,ch-lh);
	    if (marked[i]>0) {
		int mh=0;
		if (isSpine)
		    mh=lh+(ch-lh)*marked[i]/count[i];
		else
		    mh=ay.getCasePos(marked[i]);
		g.setColor("sel");
		g.fillRect(cl,h-mh,cu-cl,mh-lh);
	    };
	    g.setColor("outline");
	    g.drawRect(cl,h-ch,cu-cl,ch-lh);
	    if (cu-cl<cat_nam[i].length()*8)
		g.drawString(Common.getTriGraph(cat_nam[i]),cl+5,h-botSpace/2);
	    else
		g.drawString(cat_nam[i],cl+5,h-botSpace/2);
	    i++;
	};

	if(dragMode) {
            g.nextLayer();
	    int myX=ax.getCatCenter(ax.getCatByPos(dragX));
	    g.setColor(192,192,192);
	    g.fillRect(dragX-dragW/2,basey-dragH,dragW,dragH);	
	    g.setColor("outline");
	    g.drawRect(dragX-dragW/2,basey-dragH,dragW,dragH);	
	    g.setColor("drag");
	    g.fillRect(myX-dragW/2,basey,dragW,4);
	};

	g.end();
        setUpdateRoot(2);
    };
    
    public void mouseClicked(MouseEvent ev) 
    {
	int x=ev.getX(), y=ev.getY();
	int i=0, bars=cats, setTo=0;
	boolean effect=false;
	if (ev.isControlDown()) setTo=1;
	while (i<bars) {
	    if (Bars[i]!=null && Bars[i].contains(x,y)) {
		effect=true;
		if (!ev.isShiftDown()) m.selectNone();
		int j=0, pts=v.size();
		while (j<pts) {
		    if (v.getCatIndex(j)==i) 
			m.set(j,m.at(j)?setTo:1);			
		    j++;
		};
		break; // one can be inside one bar only
	    };
	    i++;
	};
	if (effect) m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
    };

    public void mousePressed(MouseEvent ev) {
	int x=ev.getX(), y=ev.getY();
	int i=0, bars=cats, setTo=0;
	while (i<bars) {
	    if (Bars[i]!=null && Bars[i].contains(x,y)) {
		dragMode=true;
		dragBar=i; dragNew=i; dragW=Bars[i].width; dragH=Bars[i].height;
		setCursor(new Cursor(Cursor.HAND_CURSOR));		
		break;
	    };
	    i++;
	};	
    };
    public void mouseReleased(MouseEvent e) {
	if (dragMode) {
	    dragMode=false;
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            setUpdateRoot(1);
	    if (dragNew!=dragBar) {
		ax.moveCat(dragBar,ax.getCatSeqIndex(dragNew));
		updateBars();
                setUpdateRoot(0);
	    };
	    repaint();
	};
    };

    public void mouseDragged(MouseEvent e) 
    {
	if (dragMode) {
	    dragX=e.getX(); dragY=e.getY();
	    dragNew=ax.getCatByPos(dragX);
	    //System.out.println("dragX="+dragX+" dragY="+dragY+" dragNew="+dragNew);
            setUpdateRoot(1);
	    repaint();
	};
    };

    public void mouseMoved(MouseEvent ev) {};

    public void mouseEntered(MouseEvent e) {};
    public void mouseExited(MouseEvent e) {};

    public void keyTyped(KeyEvent e) 
    {
	if (e.getKeyChar()=='R') run(this,"rotate");
	if (e.getKeyChar()=='s') run(this,"spine");
	//if (e.getKeyChar()=='l') run(this,"labels");
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
	if (cmd=="labels") {
	    //    showLabels=!showLabels;
            setUpdateRoot(0);
	    repaint();
	};
	if (cmd=="print") run(o,"exportPS");
	if (cmd=="spine") {
	    if (isSpine) {
		ax.setType(Axis.T_EqCat);
		MIspine.setLabel("Spineplot");
		isSpine=false;
	    } else {
		ax.setType(Axis.T_PropCat);
		MIspine.setLabel("Barchart");
		isSpine=true;
	    };
	};
        if (cmd=="exportCases") {
	    try {
		PrintStream p=Tools.getNewOutputStreamDlg(myFrame,"Export selected cases to ...","selected.txt");
		if (p!=null) {
		    p.println(v.getName());
		    int i=0;
		    for (Enumeration e=v.elements(); e.hasMoreElements();) {
			Object oo=e.nextElement();
			if (m.at(i)) {
			    if (oo!=null)
				p.println(oo.toString());
			    else 
				p.println("NA");
			};
			i++;
		    };
		    p.close();
		};
	    } catch (Exception eee) {};
	};
	if (cmd=="exit") WinTracker.current.Exit();
	return null;
    };

    public void actionPerformed(ActionEvent e) {
	if (e==null) return;
	run(e.getSource(),e.getActionCommand());
    };
};
