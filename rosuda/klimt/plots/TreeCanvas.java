import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

//---------------------------------------------------------------------------
// TreeCanvas
//---------------------------------------------------------------------------

/**
 * Tree Canvas - implementation of the tree display
 * @version $Id$
 */
public class TreeCanvas extends PGSCanvas implements Dependent, Commander, ActionListener, MouseListener, MouseMotionListener, KeyListener
{
    /** tool mode constant: select cases */
    public static final int Tool_Select  = 0;
    /** tool mode constant: move */
    public static final int Tool_Move    = 1;
    /** tool mode constant: zoom */
    public static final int Tool_Zoom    = 2;
    /** tool mode constant: select node */
    public static final int Tool_Node    = 3;
    /** tool mode constant: query */
    public static final int Tool_Query   = 4;

    /** root node */
    SNode root;
    /** display width */
    int w;
    /** vector of nodes of type {@link SNode} */
    public Vector nod;

    /** currently selected node or null if none */
    public SNode selectedNode;

    public int dragm=0, ldx, ldy;
    /** node currently being dragged (externally used by mouse listeners) */
    public SNode dragn;
    /** showing info about a node currently */
    public boolean showInfo=false;
    /** show detailed or rough info */
    public boolean showDetailed=false;
    /** info window has to be updated */
    public boolean infoUpdate=false;
    /** rotate tree by 90 deg */
    public boolean rot90=false;
    /** use node sizes proportional to the # of caes */
    public boolean nodeMode=true;
    /** link nodes with 90-deg lines */
    public boolean connMode=false;
    /** allign all leaves on the bottom */
    public boolean finalAlign=false;
    /** show deviance gain */
    public boolean showDevGain=false;
    /** show sample deviance as well (has no effect in showDevGain=false) */
    public boolean showSampleDev=true;

    /** show node labels */
    public boolean showLabels=true;

    /** deviance gain scale */
    public double devGainScale=1;

    /** state of the path winodw */
    public boolean showPathWindow=false;

    /** tool mode */
    public int toolMode=0;
    
    /** zoom factor used to determine logical zoom behavior */
    public double zoomFactor=1;

    /** temporary variable for zoom mode when <space> is used */
    int lastToolModeBeforeMove=0;

    /** max. dev gain */
    double maxDevGain;
    /** max leaf deviance */
    double maxLeafDev;
    
    /** # of cases in the root node */
    int rootCases;

    /** frame passed by constructor (parent?) */
    Frame outside;
    /** info window */
    Window w_info;
    /** info canvas */
    TInfoCanvas InfoCV;
    /** list info window */
    Frame w_listinfo;
    /** list info canvas */
    TNodeListCanvas ListInfoCV;

    /** menu items */
    MenuItem[] mis;
    int miss;

    /** marker associated with the dataset of the tree (cached) */
    SMarker m;

    /** coordinates of the initial drag point during zoom */
    int zoomDragX, zoomDragY;
    /** flag for zoom dragging mode */
    boolean zoomDrag=false;

    /** treemap associated with the tree */
    MosaicCanvas myMosaic=null;
    /** frame of the treemap associated with the tree */
    TFrame myMosaicFrame=null;
    /** frame of deviance plot associated with the tree */
    TFrame myDevFrame=null;
    
    /** construct a new display instance based on the specified tree
	@param troot root of the tree
	@param cont parent frame */
    public TreeCanvas(SNode troot, Frame cont) {		
	setFrame(cont); setTitle("Tree");
	nod=new Vector(); outside=cont;
	root=troot; 
	w=700;
	int leaves=root.getNumNodes(true);
	buildLeaf(w/2,30,w/leaves,40,w,root.getHeight(),root);	
	setBackground(new Color(255,255,160));

	updateCachedValues();

	w_info=new Window(cont);
	w_info.add(InfoCV=new TInfoCanvas());
	//w_info.setSize(new Dimension(250,130));
	w_listinfo=new Frame();
	w_listinfo.add(ListInfoCV=new TNodeListCanvas());
	w_listinfo.setTitle("Path window");	

	SVarSet src=troot.getSource();
	m=null;
	if (src!=null) {
	    m=src.getMarker();
	    if (m!=null) m.addDepend(this);
	};
	
	//--- this is a bit tricky - not really clean enough --
	String[] menuDef={"+","File","Open ...","open","New","new","-","Save as PGS ...",
                          "exportPGS","Export forest data ...","exportForest","Print","print","-","Quit","quit",
			  "+","Node","Prune","prune",
			  "+","Tools","Select cases","toolSelect","Node picker","toolNode","Move","toolMove","Zoom","toolZoom",
			  "+","View","Re-arrange","arrange","Rotate","rotate","-","Show treemap","showMosaic",
                          "Show deviance plot","devplot","-",
			  "Hide labels","labels",
			  "Show deviance","deviance","Show path window","pathwin","-",
			  "Use fixed size","size",
			  "Use vertical lines","connect","Align leaves","final",
			  "+","Help","Shortcuts","help",
			  "0"};

	mis=new MenuItem[32]; miss=0;
	MenuBar mb=cont.getMenuBar();
	if (mb==null) mb=new MenuBar();	
	Menu m=null;
	int i=0;
	while (menuDef[i]!="0") {
	    MenuItem mi;
	    if (menuDef[i]=="+") {
		i++;
		mb.add(m=new Menu(menuDef[i])); i++;
	    };
	    if (menuDef[i]=="-") { m.addSeparator(); i++; };
	    m.add(mi=mis[miss]=new MenuItem(menuDef[i])).setActionCommand(menuDef[i+1]);
	    mi.addActionListener(this);
	    i+=2; miss++;
	};
	if (m!=null) mb.setHelpMenu(m);
	if (WinTracker.current!=null) mb.add(WinTracker.current.getWindowMenu(cont));
	cont.setMenuBar(mb);	

	addMouseMotionListener(this);
	addMouseListener(this);
        addKeyListener(this); cont.addKeyListener(this);
    };

    /** supply minimal resize dimension (needed especially by MacOS X) */
    public Dimension getMinimumSize() { return new Dimension(40,40); };

    /** updates all cached values of the tree. Must be called whenever the underlying tree has changed */
    public void updateCachedValues() {
	maxDevGain=0; maxLeafDev=0;
	updateCachedValuesForNode(root);
    };

    /** this method is used internally by {@link #updateCachedValues}
	for iteration throught the tree 
	@param node to be updated (calls itself recursively for children if any)*/
    void updateCachedValuesForNode(SNode t) {
	if (t==null) return;
	if (t.devGain>maxDevGain)
	    maxDevGain=t.devGain;
	if (t.isLeaf()) {
	    if (t.F1>maxLeafDev) maxLeafDev=t.F1;
	    return;
	};
	
	for (Enumeration e=t.children(); e.hasMoreElements();)
	    updateCachedValuesForNode((SNode)e.nextElement());	
    };
    
    /** redesign nodes based on the current canvas geometry
	(result in a call to {@link #redesignNodes(Dimension)}) */
    public void redesignNodes()
    {
	Dimension os=getSize();
	redesignNodes(os);
    };

    /** redesign nodes based on the specifie geometry.
	calls {@link #buildLeaf} and {@link #paint} implicitely
	@param geom target geometry to use for design */
    public void redesignNodes(Dimension geom)
    {
	nod=new Vector();	
	w=(rot90)?geom.height:geom.width;
	w=w*9/10;
	int h=(rot90)?geom.width:geom.height;
	h=h*9/10;
	int leaves=root.getNumNodes(true);
	buildLeaf(w/2,(rot90)?70:30,(leaves>0)?w/leaves:1,h/(root.getHeight()+1),w,root.getHeight(),root);
	zoomFactor=1; // reset zoom factor
	repaint();	
    };

    /** notify handler (simply repaints the tree) ignores the parameter */
    public void Notifying(NotifyMsg msg, Object o, Vector path) { repaint(); };
    
    /** move node coordinates relative based on the x/y difference
	@param dx difference in x direction
	@param dy difference in y direction
	@param t the node to be moved
	@param recursively if set to <code>true</code> all sub-nodes are also moved respectively */
    public void moveLeaf(int dx, int dy, SNode t, boolean recursively)
    {
	t.x+=dx; t.y+=dy;
	t.x2+=dx; t.y2+=dy;
	if (recursively) {
	    int a=t.count();
	    if (a>0) {
		int i=0;
		while (i<a) { moveLeaf(dx,dy,(SNode)t.at(i),recursively); i++; };
	    };
	};
    };
    
    /** builds node structure recursively.
	the current node is drawn, then the coordinates of the children 
	@param parx parent x coordinate
	@param pary parent y coordinate
	@param splitUnit space between two leaves - smallest granularity unit of the design (in x direction)
	@param yShift space between tree levels in y direction
	@param myWidth with of this node
	@param totalHeight total height of the final tree
	@param t the node to be painted */
    public void buildLeaf(int parx, int pary, int splitUnit, int yShift, int myWidth, int totalHeight, SNode t)
    {	
	int x=parx, y=pary;
	boolean underflowWarning=false;
	
	if (t==root) rootCases=(t.Cases>0)?t.Cases:1;

	if (rot90) { y=parx; x=pary; };
	
	nod.addElement(t);

	int nodeWidth=80;

	if (nodeMode) {
	    nodeWidth=(int)(((double)t.Cases)/((double)rootCases)*80.d);
	    if (nodeWidth<5) {
		underflowWarning=true; nodeWidth=5;
	    };
	};
	t.x=x-(nodeWidth/2); t.y=y+5;
	t.x2=t.x+nodeWidth; t.y2=t.y+20;

	x=parx; y=pary;
	
	if (t.isLeaf() || t.isPruned()) return;

	int a=t.count();
	if (a>0) {
	    int i=0, myLeft=x-myWidth/2;
	    while (i<a) {
		int chLeaves=t.at(i).getNumNodes(true);
		int chWidth=chLeaves*splitUnit;
		
		int tx=myLeft+chWidth/2;
		myLeft+=chWidth;
		int ty=y+yShift;
		
		if ((finalAlign)&&(t.at(i).isLeaf()))
		    ty=ty+(totalHeight-t.at(i).getLevel())*yShift;

		buildLeaf(tx,ty,splitUnit,yShift,chWidth,totalHeight,
			  (SNode)t.at(i));
		i++;
	    };
	};
    };

    /** implementation of the abstract method of PGSCanvas
	calls {@link #paintLeaf}<code>(g,root)</code>
	@param g graphic context to paint on */

    public void paintPoGraSS(PoGraSS p) {	
	Rectangle r=getBounds();
	p.setBounds(r.width,r.height);
	p.begin();
	p.defineColor("white",255,255,255);
	p.defineColor("selected",192,192,255);
	p.defineColor("leaf",255,255,192);
	p.defineColor("path",192,255,255);
	p.defineColor("black",0,0,0);
	p.defineColor("hilite",128,255,128);
	p.defineColor("red",255,0,0);
	p.defineColor("sampleDev",0,128,64);
	p.defineColor("lines",96,96,255);	
	p.defineColor("selText",255,0,0);
	p.defineColor("shadow",160,160,160);
	p.defineColor("zoomOut",0,0,128);

	if (zoomDrag) {
	    p.setColor("white");
	    p.fillRect(ldx,ldy,zoomDragX-ldx,zoomDragY-ldy);
	    p.setColor("red");
	    p.drawRect(ldx,ldy,zoomDragX-ldx,zoomDragY-ldy);
	};

        showSampleDev=(root.sampleDev>0 && root.sampleDev!=root.F1);
	paintLeaf(p,root);

	//System.out.println("paint; zoomFactor="+zoomFactor);
	p.end();
    };

    /** paints a node and all its subnodes recursively.
	all necessary information is stored in the {@link SNode}
	contents of the {@link SNode} nodes.
	@param g graphic context to pain on
	@param t node to paint */
    public void paintLeaf(PoGraSS g,SNode t)
    {
	if (t.isPruned()&&(t.par!=null)&&((SNode)t.par).isPruned()) return;
	
	int x=t.x, y=t.y, x2=t.x2, y2=t.y2;
	int dTotal=0, dMark=0;
	
	if (zoomFactor>0.3) {
	    // shadow
	    if (!nodeMode) {
		g.setColor("shadow");
		g.fillRoundRect(x+5,y+5,x2-x,y2-y,15,15);
	    };
	    
	    // get # of marked cases (dMark) and total # of cases (dTotal) in that node
	    if (t.data!=null) dTotal=t.data.size();
	    if (t.getSource()!=null) {
		SMarker m=t.getSource().getMarker();
		if ((m!=null)&&(t.data!=null))
		    for (Enumeration e=t.data.elements(); e.hasMoreElements();) {
			int ix=((Integer)e.nextElement()).intValue();
			if (m.at(ix)) dMark++;
		    };
	    };
	    
	    // eqi-mode + marked => rectangular shadow
	    if ((dMark>0)&&(!nodeMode)) {
		g.setColor("shadow");
		g.fillRect(x+5,y2-10+5,x2-x,10);
	    };
	};

	// draw lines and other nodes
	if (!t.isPruned()) { /* paint only if not prunned */
	    int a=t.count();
	    if (a>0) {
		int i=0;
		while (i<a) {
		    SNode cn=(SNode)t.at(i);
		    
		    /* connecting lines */
		    g.setColor("lines");		
		    if (connMode) {
			if (!rot90) {
			    g.drawLine((x+x2)/2,y+10,(cn.x+cn.x2)/2,y+10);
			    g.drawLine((cn.x+cn.x2)/2,y+10,(cn.x+cn.x2)/2,cn.y+10);
			} else {
			    g.drawLine((x+x2)/2,y+10,(x+x2)/2,cn.y+10);
			    g.drawLine((x+x2)/2,cn.y+10,(cn.x+cn.x2)/2,cn.y+10);
			};
		    } else
			g.drawLine((x+x2)/2,y+10,(cn.x+cn.x2)/2,cn.y+10);
		    /* paint the leaf */
		    paintLeaf(g,(SNode)t.at(i));
		    i++;
		};
	    };
	};    

	if (zoomFactor>0.3) {
	    // paint base rect
	    String bgc="white";
	    if (t.sel==1) bgc="selected";
	    if (t.sel==3) bgc="leaf";
	    if (t.sel==2) bgc="path";
	    g.setColor(bgc);
	    if (nodeMode) { 
		g.fillRect(x,y,x2-x,y2-y);	g.setColor("black"); g.drawRect(x,y,x2-x,y2-y); 
	    } else {
		g.fillRoundRect(x,y,x2-x,y2-y,15,15); g.setColor("black"); g.drawRoundRect(x,y,x2-x,y2-y,15,15);
	    };
	    
	    // if hilighted draw it
	    if (dMark>0) {
	    // base is always white regardles of type
		g.setColor("white"); g.fillRect(x,y2-10,x2-x,10);
		//g.setColor("black"); g.drawRect(x,y2-10,x2-x,10);
		g.setColor("hilite");
		if (nodeMode) {
		    g.fillRect(x,y,(int)(((double)dMark)/((double)dTotal)*((double)(x2-x))),y2-y);
		    g.setColor("black");
		    g.drawRect(x,y,(int)(((double)dMark)/((double)dTotal)*((double)(x2-x))),y2-y);
		    g.drawRect(x,y,x2-x,y2-y);
		} else {
		    g.fillRect(x,y2-10,(int)(((double)dMark)/((double)dTotal)*((double)(x2-x))),10);
		    g.setColor("black");
		    g.moveTo(x,y2-10); g.lineTo(x,y2); g.lineTo(x2,y2); g.lineTo(x2,y2-10); g.moveTo(x,y2-10);
		};
	    };
	};

	/* deviance display */
	if (showDevGain) {
	    g.setColor("red");
	    if (t.devGain>0) {	    
		int arcSize=(int)(Math.sqrt(devGainScale*t.devGain/maxDevGain)*20);
		if (arcSize>20) {
		    g.drawOval(x-25,y,20,20);
		} else {
		    g.fillOval(x-25,y,arcSize,arcSize);
		};
	    };
	    if ((t.isLeaf())&&(t.F1>0)) {
		int arcSize=(int)(Math.sqrt(t.F1/maxLeafDev)*20);
		g.setColor("red");
		g.fillRect(x-25,y,arcSize,arcSize);
	    };

            if (showSampleDev) {
                g.setColor("sampleDev");
                if (t.sampleDevGain>0) {
                    int arcSize=(int)(Math.sqrt(devGainScale*t.sampleDevGain/maxDevGain)*20);
                    if (arcSize>20) {
                        g.drawOval(x-45,y,20,20);
                    } else {
                        g.fillOval(x-45,y,arcSize,arcSize);
                    };
                };
                if ((t.isLeaf())&&(t.sampleDev>0)) {
                    int arcSize=(int)(Math.sqrt(t.sampleDev/maxLeafDev)*20);
                    g.fillRect(x-45,y,arcSize,arcSize);
                };
            }
        };
	
	/* draw labels (class and condition) */
	if (selectedNode!=null && t.Name.compareTo(selectedNode.Name)==0)
	    g.setColor("selText"); else g.setColor("black");

	if (zoomFactor>0.3)
	    g.drawString(t.Name,x+5,y+15);
	else if (zoomFactor>0.2)
	    g.drawString(Common.getTriGraph(t.Name),x+5,y+15);

	g.setColor("black");

	if (showLabels && zoomFactor>0.3)
	    g.drawString(t.Cond,x+5,y-5);

	/* "pruned" symbol */
	if (t.isPruned()) {
	    g.setColor("white");
	    g.fillOval(x-10,y+20,10,10);
	    g.setColor("black"); 
	    g.drawOval(x-10,y+20,10,10);
	    g.drawLine(x-5,y+20,x-5,y+30);
	    g.drawLine(x-10,y+25,x,y+25);
	};

	if (zoomFactor<=0.3) {
	    g.setColor("zoomOut");
	    g.fillOval((t.x2+t.x)/2-3,(t.y2+t.y)/2-3,6,6);
	};
    };
    
    /** set currently selected node */
    public void selectNode(SNode n) {
	if (m!=null) m.setNode(n);
	if (n==selectedNode) {
	    if (InfoCV.det!=showDetailed && showInfo) {
		InfoCV.setNode(selectedNode,showDetailed);
		if (!w_info.isShowing()) {
		    w_info.pack();
		    w_info.show();
		};
	    };
	    return;
	};
	selectedNode=n;

	ListInfoCV.setNode(n);

	if (showPathWindow && !w_listinfo.isShowing()) { w_listinfo.pack(); w_listinfo.show(); };
	if (!showPathWindow && w_listinfo.isShowing()) w_listinfo.dispose();

	if (n==null || !showInfo) {
	    if (w_info.isShowing()) w_info.dispose();
	    return;
	};

	InfoCV.setNode(selectedNode,showDetailed);
	if (!w_info.isShowing()) {
	    w_info.pack();
	    w_info.show();
	};
    };

    /** commander interface implementation for all user commands */
    public Object run(Object o, String cmd) {
	super.run(o,cmd);
	MenuItem mi=getMenuItemByAction(cmd);
	if (cmd=="help") {
	    HelpFrame hf=new HelpFrame();
	    hf.pack(); hf.show();
	};
	if (cmd=="toolSelect") setToolMode(Tool_Select);
	if (cmd=="toolZoom") setToolMode(Tool_Zoom);
	if (cmd=="toolMove") { setToolMode(Tool_Move); lastToolModeBeforeMove=Tool_Move; };
	if (cmd=="toolNode") setToolMode(Tool_Node);

	if (cmd=="arrange") {
	    redesignNodes();
	};
	if (cmd=="rotate") {
	    rot90=!rot90;
	    redesignNodes();
	};
	if (cmd=="size") {
	    nodeMode=!nodeMode;
	    redesignNodes();
	    if (mi!=null) mi.setLabel(nodeMode?"Use fixed size":"Use proportional size");
	};
	if (cmd=="prune")
	    if ((selectedNode!=null)&& !selectedNode.isLeaf()) {
		selectedNode.setPrune(true);
		selectedNode.getSource().getMarker().NotifyAll(new NotifyMsg(selectedNode.getSource().getMarker(),Common.NM_MarkerChange));
	    };
	if (cmd=="print") run(o,"exportPS");
	if (cmd=="new") {
 	    TFrame f=new TFrame("Pruned copy of \""+root.getSource().getName()+"\"");
	    SNode t=InTr.makePrunedCopy(root);
	    TreeCanvas tc=InTr.newTreeDisplay(t,f);
	    //Common.mainFrame.add(f);
	    tc.repaint(); tc.redesignNodes();
	};
	if (cmd=="open") {
	    //SVarSet tvs=new SVarSet();
	    SVarSet tvs=root.getSource();
	    SNode t=InTr.openTreeFile(Common.mainFrame,null,tvs);
	    if (t!=null) {
		TFrame f=new TFrame(tvs.getName()+" - tree");
		TreeCanvas tc=InTr.newTreeDisplay(t,f);
		tc.repaint(); tc.redesignNodes();		
		//InTr.newVarDisplay(tvs);
	    };
	};
	if (cmd=="deviance") {
	    showDevGain=!showDevGain;
	    repaint();
	    if (mi!=null) mi.setLabel(showDevGain?"Hide deviance":"Show deviance");
	};
	if (cmd=="connect") {
	    connMode=!connMode;
	    repaint();
	    if (mi!=null) mi.setLabel(connMode?"Use straight lines":"Use vertical lines");
	};
	if (cmd=="final") {
	    finalAlign=!finalAlign;
	    redesignNodes();
	    if (mi!=null) mi.setLabel(finalAlign?"Scatter leaves":"Align leaves");
	};
	if (cmd=="quit") {
	    outside.dispose();
	    System.exit(0);
	};
	if (cmd=="labels") {
	    showLabels=!showLabels;
	    repaint();
	    if (mi!=null) mi.setLabel(showLabels?"Hide labels":"Show labels");
	};
	if (cmd=="pathwin") {
	    showPathWindow=!showPathWindow;
	    if (showPathWindow && !w_listinfo.isShowing()) { w_listinfo.pack(); w_listinfo.show(); };
	    if (!showPathWindow && w_listinfo.isShowing()) w_listinfo.dispose();
	    if (mi!=null) mi.setLabel(showPathWindow?"Hide path window":"Show path window");
	};
	if (cmd=="zoomDevIn") {
	    devGainScale*=2;
	    repaint();
	};
	if (cmd=="zoomDevOut") {
	    devGainScale/=2;
	    repaint();
	};
	if (cmd=="showMosaic") {
            /* since 0.95g: allow more windows at once, the use of myMosaicFrame is deprecated 
	    if (myMosaicFrame!=null) {
		myMosaicFrame.dispose();
		WinTracker.current.rm(myMosaicFrame);
		myMosaicFrame=null;
	    }; */
	    myMosaicFrame=new TFrame(InTr.lastTreeFileName+" (treemap)");
	    myMosaicFrame.add(myMosaic=new MosaicCanvas(myMosaicFrame,root));
	    myMosaicFrame.addWindowListener(Common.defaultWindowListener);
	    myMosaic.setBounds(0,0,400,300);
	    myMosaicFrame.pack(); myMosaicFrame.show();		
	};
	if (cmd=="devplot") {
            /* since 0.95g: allow more windows at once, the use of myDevFrame is deprecated

            if (myDevFrame!=null) {
		myDevFrame.dispose();
		WinTracker.current.rm(myDevFrame);
		myDevFrame=null;
	    }; */
	    myDevFrame=new TFrame(InTr.lastTreeFileName+" (deviance plot)");
	    DevCanvas dc=new DevCanvas(myDevFrame,root);
	    myDevFrame.add(dc); myDevFrame.addWindowListener(Common.defaultWindowListener);
	    dc.setBounds(0,0,400,300);
	    myDevFrame.pack(); myDevFrame.setVisible(true);
        };
        if (cmd=="exportForest") {
            try {
                PrintStream p=Tools.getNewOutputStreamDlg(myFrame,"Export forest data to ...","forest.txt");
                if (p!=null) {
                    p.println("Tree\tVar\ttree.dev\ttree.gain\ttree.size\tsample.gain\tsample.gain\tsample.size");
                    SVarSet.TreeEntry te;
                    if (Common.DEBUG>0) System.out.println("Forest export; total "+root.getSource().trees.size()+" trees associated.");
                    for (Enumeration e=root.getSource().trees.elements(); e.hasMoreElements();) {
                        te=(SVarSet.TreeEntry)e.nextElement();
                        if (Common.DEBUG>0) System.out.println("exporting tree \""+te.name+"\"...");
                        if (te.root!=null) {
                            Vector v=new Vector();
                            te.root.getAllNodes(v);
                            if (Common.DEBUG>0) System.out.println(" total "+v.size()+" nodes.");
                            for (Enumeration e2=v.elements(); e2.hasMoreElements();) {
                                SNode np=(SNode)e2.nextElement();
                                if (!np.isLeaf()) {
                                    SNode n=(SNode)np.at(0);
                                    if (n!=null) {
                                        p.println(te.name+"\t"+n.splitVar.getName()+"\t"+np.F1+"\t"+np.devGain+"\t"+n.Cases+"\t"+np.sampleDev+"\t"+np.sampleDevGain+"\t"+np.data.size());
                                    };
                                }
                            }
                        }
                    }
                    p.close();
                };
            } catch (Exception eee) {};
            
        }
	return null;
    };  

    MenuItem getMenuItemByAction(String act) {
	int i=0; while(i<miss) { if (mis[i].getActionCommand()==act) return mis[i]; i++; };
	return null;
    };

    /** action listener methods reroutes all request to the commander interface */
    public void actionPerformed(ActionEvent e) {
	if (e==null) return;
	run(e.getSource(),e.getActionCommand());
    };

    /** set tool mode and use according cursor shape */
    public void setToolMode(int mode) {
	toolMode=mode;
	if (toolMode==Tool_Move)
	    setCursor(new Cursor(Cursor.MOVE_CURSOR));
	if (toolMode==Tool_Select)
	    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	if (toolMode==Tool_Zoom)
	    setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));    
	if (toolMode==Tool_Node)
	    setCursor(new Cursor(Cursor.HAND_CURSOR));	
    };

    /* mouse listeners */
    public void mouseDragged(MouseEvent e) 
    {
	if (dragm==1||dragm==2) {
	    int x=e.getX(), y=e.getY();
	    int dx=x-ldx, dy=y-ldy;
	    if ((dx!=0)||(dy!=0)) {
		if (e.isShiftDown()) dy=0;
		if (dragm==1)
		    moveLeaf(dx,dy,dragn,!e.isControlDown());
		if (dragm==2)
		    moveLeaf(dx,dy,root,!e.isControlDown());
		ldx=x; ldy=y;
		repaint();
	    };
	};
	if (toolMode==Tool_Zoom) {
	    zoomDrag=true; zoomDragX=e.getX(); zoomDragY=e.getY();
	    repaint();
	};
    };

    public void mouseMoved(MouseEvent ev) {};

    public void mouseClicked(MouseEvent ev) 
    {
	int x=ev.getX(), y=ev.getY();
	boolean gotSel=false;
        SNode selNode=null;

	if (toolMode==Tool_Zoom) {	    
	    boolean zoomIn=true;	    
	    if (ev.isShiftDown()) zoomIn=false;
	    if (zoomIn && zoomFactor>8) return;
	    if (!zoomIn && zoomFactor<0.1) return;
	    for (Enumeration e=nod.elements(); e.hasMoreElements();) {
		SNode n=(SNode)e.nextElement();
		int cx=n.x+(n.x2-n.x)/2, cy=n.y+(n.y2-n.y)/2;
		if (zoomIn) {
		    cx=x+(cx-x)*2;
		    cy=y+(cy-y)*2;
		} else {
		    cx=x+(cx-x)/2;
		    cy=y+(cy-y)/2;
		};
		int dx=n.x2-n.x, dy=n.y2-n.y;
		n.x=cx-(dx/2); n.y=cy-(dy/2);
		n.x2=cx+(dx/2); n.y2=cy+(dy/2);
	    };
	    if (zoomIn)
		zoomFactor*=2;
	    else
		zoomFactor/=2;
	    
	    repaint();
	};

	if (toolMode==Tool_Select||toolMode==Tool_Node) {
	    // look if hit in node space
	    for (Enumeration e=nod.elements(); e.hasMoreElements();) {
		SNode n=(SNode)e.nextElement();
		
		// check for "plus" sign click
		if (n.isPruned()&&(n.x-10<=x)&&(n.x>x)&&(n.y+20<y)&&(n.y+30>=y)) {
		    if (((n.par!=null)&& !(((SNode)(n.par)).isPruned()))||
			(n.par==null))
			{ n.setPrune(false); repaint(); };
		};
		
		// check for click inside a node-box
		if ((!gotSel)&&(n.x<=x)&&(n.x2>=x)&&(n.y<=y)&&(n.y2>=y)) {
		    gotSel=true;
		    n.sel=1;
		    selNode=n;
		    
		    if (toolMode==Tool_Select && !ev.isAltDown() && (ev.getModifiers()&MouseEvent.BUTTON1_MASK)>0) {// no alt=select in node
			SMarker m=n.getSource().getMarker();
			int setTo=0;
			if (ev.isControlDown()) setTo=1;
			if (!ev.isShiftDown() && !ev.isControlDown()) m.selectNone();
			
			if ((m!=null)&&(n.data!=null)) {
			    for (Enumeration e2=n.data.elements(); e2.hasMoreElements();) {
				int j=((Integer)e2.nextElement()).intValue();
				m.set(j,m.at(j)?setTo:1);
			    };
			    m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
			};
		    };
		    
		    w_info.setLocation(n.x2+outside.getLocation().x+getLocation().x,
				       n.y2+getLocation().y+outside.getLocation().y);
		} else n.sel=0;
	    };
	};

	if(gotSel) {
	    for (Enumeration e=nod.elements(); e.hasMoreElements();) {
		SNode n=(SNode)e.nextElement();
		if ((n!=selNode)&&(n.Name.compareTo(selNode.Name)==0)) {
		    n.sel=(n.isLeaf())?2:3;
		};
	    };
	};
	
	showInfo=gotSel&&(ev.isPopupTrigger() || ev.isAltDown() ||
                          (ev.getModifiers()&(MouseEvent.BUTTON2_MASK|MouseEvent.BUTTON3_MASK))>0);	
	showDetailed=ev.isShiftDown() /* || (ev.getModifiers()&MouseEvent.BUTTON2_MASK)>0 */;
	selectNode(selNode);
    };

    public void mousePressed(MouseEvent ev) 
    {
	int x=ev.getX(), y=ev.getY();
	if (toolMode==Tool_Move) {
	    dragm=2; ldx=x; ldy=y; dragn=null;
	};
	
	if (toolMode==Tool_Select) {
	    for (Enumeration e=nod.elements(); e.hasMoreElements();) {
		SNode n=(SNode)e.nextElement();
		if ((n.x<=x)&&(n.x2>=x)&&(n.y<=y)&&(n.y2>=y)) {
		    dragm=1; ldx=x; ldy=y; dragn=n;
		    break;
		};
	    };
	};

	if (toolMode==Tool_Zoom) {
	    ldx=x; ldy=y; //zoomDrag=true; zoomDragX=x; zoomDragY=y;
	};
    };

    public void mouseReleased(MouseEvent e)
    {
	if (dragm>0) dragm=0;
	if (zoomDrag) {
	    zoomDrag=false; 
	    repaint();
	};
    };
    public void mouseEntered(MouseEvent e) {};
    public void mouseExited(MouseEvent e) {};

    /* key handling */
    public void keyTyped(KeyEvent e) 
    {
        if (Common.DEBUG>0) System.out.println("keyTyped: "+e.toString());
	if (e.getKeyChar()=='e') run(this,"toolSelect");
	if (e.getKeyChar()=='z') run(this,"toolZoom");
	if (e.getKeyChar()=='v') run(this,"toolMove");
	if (e.getKeyChar()=='n') run(this,"toolNode");

	if (e.getKeyChar()=='r') run(this,"arrange");
	if (e.getKeyChar()=='h') run(this,"help");
	if (e.getKeyChar()=='R') run(this,"rotate");
	if (e.getKeyChar()=='s') run(this,"size");
	if (e.getKeyChar()=='p') run(this,"prune");
	if (e.getKeyChar()=='P') run(this,"print");
	if (e.getKeyChar()=='m') run(this,"showMosaic");
	if (e.getKeyChar()=='N') run(this,"new");
	if (e.getKeyChar()=='o') run(this,"open");
	if (e.getKeyChar()=='d') run(this,"deviance");
	if (e.getKeyChar()=='D') run(this,"devplot");
	if (e.getKeyChar()=='c') run(this,"connect");
	if (e.getKeyChar()=='f') run(this,"final");
	if (e.getKeyChar()=='q') run(this,"quit");
	if (e.getKeyChar()=='l') run(this,"labels");
	if (e.getKeyChar()=='+') run(this,"zoomDevIn");
	if (e.getKeyChar()=='-') run(this,"zoomDevOut");
	if (e.getKeyChar()=='X') run(this,"exportPGS");
    };
    public void keyPressed(KeyEvent e) {
        if (Common.DEBUG>0) System.out.println("keyPressed: "+e.toString());
	//System.out.println("keyPressed, char='"+e.getKeyChar()+"', Shift="+e.isShiftDown()+", Ctrl="+e.isControlDown()+", PS="+e.paramString());
	if (e.getKeyChar()==' ') {
	    /*
	    if (e.isControlDown()) {
	    if (e.isShiftDown())
		    setToolMode(Tool_Zoom);
		else
		    setToolMode(Tool_Zoom);
		    } else */
	    if (toolMode!=Tool_Move) {
		lastToolModeBeforeMove=toolMode;
		setToolMode(Tool_Move);
	    };
	};
    };
    public void keyReleased(KeyEvent e) {
        if (Common.DEBUG>0) System.out.println("keyReleased: "+e.toString());
	//System.out.println("keyReleased, char='"+e.getKeyChar()+"', Shift="+e.isShiftDown()+", Ctrl="+e.isControlDown()+", PS="+e.paramString());
	if (e.getKeyChar()==' ')
	    setToolMode(lastToolModeBeforeMove);
    };
};
