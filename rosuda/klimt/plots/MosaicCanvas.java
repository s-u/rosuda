import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** Node with geometry, internally used by {@link MosaicCanvas} */
class SNodeGeometry {
    SNode n;
    int x1,x2,y1,y2;
    SNodeGeometry (SNode N,int X1,int Y1, int X2, int Y2) { n=N; x1=X1; x2=X2; y1=Y1; y2=Y2; };
};

/** Implementation of treemaps and spineplots for leaves
    @version $Id$
*/
class MosaicCanvas extends PGSCanvas implements Dependent, MouseListener, MouseMotionListener, KeyListener, Commander, ActionListener
{
    /** root of the tree */
    SNode root=null;
    /** associated marker (cached) */
    SMarker m=null;
    /** associated dataset (cached) */
    SVarSet vs=null;

    /** list of nodes, stored are objects of the class {@link SNodeGeometry} */
    Vector nodes;

    /** coordinates of rubber rectangle */
    int x1, y1, x2, y2;
    /** dragging active */
    boolean drag;
    /** determines orientation of the first split */
    boolean firstVertical;
    /** if true orientations are alternated for each split */
    boolean alternate;

    /** current width and height of the canvas */
    int TW,TH;

    /** menu item of the "alternate" */
    MenuItem MIalt;
    /** constructs a treemap based on the passed tree
	@param f associated frame or <code>null</code> if none
	@param tree root of the associated tree */
    MosaicCanvas(Frame f, SNode tree) {
	setFrame(f); setTitle("Treemap");
	nodes=new Vector();
	root=tree; firstVertical=true; alternate=true;
	if (root!=null) {
	    vs=root.getSource();
	    if (vs!=null) {
		m=vs.getMarker();
		if (m!=null)
		    m.addDepend(this);
	    };
	};
	setBackground(new Color(255,255,192));
	drag=false;	
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this);
	MenuBar mb=null;
	String myMenu[]={"+","File","Close","close","Quit","quit","+","View","Rotate","rotate","Spineplot of leaves","alternate","0"};
	f.setMenuBar(mb=WinTracker.current.buildQuickMenuBar(f,this,myMenu,false));
	MIalt=mb.getMenu(1).getItem(1);
    };
    
    public Dimension getMinimumSize() { return new Dimension(40,40); };

    /** rotate = swap axes */
    public void rotate() {
	firstVertical=!firstVertical;
	repaint();
    };

    public void Notifying(Object o, Vector path) {
	repaint();
    };

    public void paintNode(PoGraSS g, SNode n, int x1, int y1, int x2, int y2, boolean vertical) {
	int myCases=n.data.size();
	int im=0;

	if(myCases==0) {
	    g.setColor("missing");
	    g.drawLine(x1,y1,x2,y2);
	    return;
	};
	if (n.isLeaf() || n.isPruned()) {
	    nodes.addElement(new SNodeGeometry(n,x1,y1,x2,y2));
	    g.setColor("white");
	    if (n.tmp==2)
		g.setColor("selBg");
	    g.fillRect(x1,y1,x2-x1,y2-y1);	    

	    int dMark=0; // # of selected cases in the node
	    if (n.getSource()!=null) {
		SMarker m=n.getSource().getMarker();
		if ((m!=null)&&(n.data!=null))
		    for (Enumeration e=n.data.elements(); e.hasMoreElements();) {
			int ix=((Integer)e.nextElement()).intValue();
			if (m.at(ix)) dMark++;
		    };
	    };
	    if(dMark>0) {
		g.setColor("marked");
		g.fillRect(x1,y1+(y2-y1)*(myCases-dMark)/myCases,x2-x1,y2-y1-(y2-y1)*(myCases-dMark)/myCases);
	    };
	    g.setColor("splitRects");
	    g.drawRect(x1,y1,x2-x1,y2-y1);
	    return;
	};

	int start=0, totalSpace=0;
	if (vertical) {
	    totalSpace=x2-x1-2*n.count()+2;
	    start=x1;
	} else {
	    totalSpace=y2-y1-2*n.count()+2;
	    start=y1;
	};
	for(Enumeration e=n.children();e.hasMoreElements();) {
	    SNode c=(SNode)e.nextElement();
	    boolean empty=false;
	    int nx1=x1, nx2=x2, ny1=y1, ny2=y2;
	    if (vertical) {
		nx1=start;
		if (c.Cases>0) {
		    nx2=start+c.Cases*totalSpace/myCases;
		    if (!e.hasMoreElements())
			nx2=x2;
		    start=nx2+2;
		} else {
		    nx2=nx1; empty=true;
		};
	    } else {
		ny1=start;
		if (c.Cases>0) {
		    ny2=start+c.Cases*totalSpace/myCases;
		    if (!e.hasMoreElements())
			ny2=y2;
		    start=ny2+2;
		} else {
		    ny2=ny1; empty=true;
		};
	    };
	    paintNode(g,c,nx1,ny1,nx2,ny2,(alternate)?!vertical:vertical);
	};
    };

    public void paintPoGraSS(PoGraSS g) {
	Rectangle r=getBounds();
	g.setBounds(r.width,r.height);
	g.begin();
	g.defineColor("white",255,255,255);
	g.defineColor("selected",192,192,255);
	g.defineColor("black",0,0,0);
	g.defineColor("outline",0,0,0);
	g.defineColor("point",0,0,128);
	g.defineColor("marked",128,255,128);
	g.defineColor("red",255,0,0);
	g.defineColor("missing",255,0,0);
	g.defineColor("lines",96,96,255);	
	g.defineColor("selText",255,0,0);
	g.defineColor("selBg",255,255,192);
	g.defineColor("splitRects",128,128,255);

	Dimension Dsize=getSize();
	if (Dsize.width!=TW || Dsize.height!=TH) {
	    TW=Dsize.width; TH=Dsize.height;
	};

	if (TW<50||TH<50) {
	    g.setColor("red");
	    g.drawLine(0,0,TW,TH); 
	    g.drawLine(0,TH,TW,0); 
	    return;
	};

	nodes.removeAllElements();
	paintNode(g,root,5,5,TW-5,TH-5,firstVertical);

	if (drag) {
	    int dx1=x1,dy1=y1,
		dx2=x2,dy2=y2;
	    if (dx1>dx2) { int h=dx1; dx1=dx2; dx2=h; };
	    if (dy1>dy2) { int h=dy1; dy1=dy2; dy2=h; };
	    g.setColor("black");
	    g.drawRect(dx1,dy1,dx2-dx1,dy2-dy1);
	};
	
	g.end();
    };

    public void mouseClicked(MouseEvent ev) 
    {
	int x=ev.getX(), y=ev.getY();
	//x1=x-2; y1=y-2; x2=x+3; y2=y+3; drag=true; mouseReleased(ev);
	for (Enumeration e=nodes.elements(); e.hasMoreElements();) {
	    SNodeGeometry ng=(SNodeGeometry)e.nextElement();
	    if (x>=ng.x1&&x<=ng.x2&&y>=ng.y1&&y<=ng.y2) {
		if (!ev.isAltDown()) {// no alt=select in node
		    SMarker m=ng.n.getSource().getMarker();
		    int setTo=0;
		    if (ev.isControlDown()) setTo=1;
		    if (!ev.isShiftDown()) m.selectNone();

		    if ((m!=null)&&(ng.n.data!=null)) {
			for (Enumeration e2=ng.n.data.elements(); e2.hasMoreElements();) {
			    int j=((Integer)e2.nextElement()).intValue();
			    m.set(j,m.at(j)?setTo:1);
			};
			m.NotifyAll();
		    };
		};		
	    };
	};

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
	    if (sel.contains(Pts[i])) m.set(i,m.at(i)?setTo:1);
	    i++;
	    };*/
	m.NotifyAll();
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
		repaint();
	    };
	};
    };
    public void mouseMoved(MouseEvent ev) {};

    public void keyTyped(KeyEvent e) 
    {
	if (e.getKeyChar()=='R') run(this,"rotate");
	if (e.getKeyChar()=='X') run(this,"exportPGS");
	if (e.getKeyChar()=='P') run(this,"print");
	if (e.getKeyChar()=='a') run(this,"alternate");
    };
    public void keyPressed(KeyEvent e) {};
    public void keyReleased(KeyEvent e) {};

    /** implementation of the {@link Commander} interface */
    public Object run(Object o, String cmd) {
	super.run(o,cmd);
	if (cmd=="rotate") {
	    rotate();
	};
	if (cmd=="alternate") {
	    alternate=!alternate;
	    MIalt.setLabel((alternate)?"Spineplot of leaves":"Treemap");
	    repaint();
	};
	if (cmd=="print") run(o,"exportPS");
	return null;
    };

    public void actionPerformed(ActionEvent e) {
	if (e==null) return;
	run(e.getSource(),e.getActionCommand());
    };
};
