import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** Variables window - central place for operations on a dataset
    @version $Id$
*/

public class VarFrame extends TFrame {
    public class VarCanvas extends DBCanvas implements MouseListener, AdjustmentListener
    {
	/** associated window */
	VarFrame win;
	/** selection mask of the variables */
	boolean[] selMask;
	/** data source */
	SVarSet vs;
	/** # of variables (cached from data source) */
	int vars;
	/** scrollbar if too many vars are present */
	Scrollbar sb;
	Dimension minDim;
	Dimension lastSize;

	int offset=0;

	/** constructs a new variable canvas for associated tree canvas
	    @param w window in which this canvsa is displayed
	    @param p associated tree canvas
	*/
	VarCanvas(VarFrame w, SVarSet dataset,Scrollbar s) {
	    setBackground(new Color(255,255,192));
	    win=w; vs=dataset;
	    vars=vs.count();
	    selMask=new boolean[vars];
	    addMouseListener(this);
	    sb=s;
	    minDim=new Dimension(140,100);
	};
	public void adjustmentValueChanged(AdjustmentEvent e) {
	    offset=e.getValue();
	    repaint();
	};
	public Dimension getMinimumSize() { return minDim; };

	/** implementation of the {@link DBCanvas#paintBuffer} method
	    @param g graphic context to paint on */
	public void paintBuffer(Graphics g) {
	    int totsel=0;	    
	    Dimension cd=getSize();

	    if (lastSize==null || cd.width!=lastSize.width || cd.height!=lastSize.height) {
		int minh=vars*17+6;
		if (minh>200) minh=200;
		if (cd.width<140 || cd.height<minh) {
		    setSize((cd.width<140)?140:cd.width,(cd.height<minh)?minh:cd.height);
		    win.pack();
                    cd=getSize();
		}; 
		minh=vars*17+6;
                if (sb!=null) {
                    if (minh-cd.height+17<=0) {
                        sb.setValue(offset=0); vc.repaint();
                        sb.setMaximum(0);
                    } else {
                        sb.setMaximum(minh-cd.height+17);
                    };
                };
		lastSize=cd;
	    };

	    Font fo=getFont();
	    Font f2=new Font(fo.getName(),Font.BOLD,fo.getSize());
	    Color C_varNam=new Color(0,0,128);
	    Color C_info=new Color(128,0,0);
	    Color C_bg=new Color(255,255,255);
	    Color C_sel=new Color(128,255,128);
	    Color C_frame=new Color(128,128,128);

	    int i=0;
	    for (Enumeration e=vs.elements(); e.hasMoreElements();) {
		SVar v=(SVar)e.nextElement();	    
		if (selMask[i]) totsel++;
		g.setColor(selMask[i]?C_sel:C_bg);
		g.fillRect(5,5+i*17-offset,130,15);
		g.setColor(C_frame);
		g.drawRect(5,5+i*17-offset,130,15);
		g.setFont(fo); g.setColor(C_info);	
		g.drawString((v.isNum()?"N":"S")+(v.isCat()?"C":"")+(v.hasMissing()?"*":""),10,17+i*17-offset);
		g.setFont(f2); g.setColor(C_varNam);	
		g.drawString(v.getName(),35,17+i*17-offset);
		i++;
	    };

	    /*
	    if (17+i*17>cd.height) {
		setSize(cd.width,17+i*17);
		win.pack();
		win.repaint();
		}; 
	    */
	};
    
	/* mouse actions */

	public void mouseClicked(MouseEvent ev) 
	{
	    if (vs==null) return;

	    int x=ev.getX(), y=ev.getY()+offset;
	    int svar=-1;
	    if ((x>5)&&(x<115)) svar=(y-3)/17;
	
	    if (svar<vs.count()) {
		if (ev.isShiftDown()) {
		    if (svar>=0) selMask[svar]=!selMask[svar];
		    repaint();
		} else {
		    for(int i=0;i<vs.count();i++) selMask[i]=false;
		    if (svar>=0) selMask[svar]=true;
		    repaint();
		};
		if (ev.getClickCount()==2) {
		    vs.at(svar).categorize();
		    repaint();
		};
	    };

	    win.vcc.repaint();
	};
	public void mousePressed(MouseEvent ev) {};
	public void mouseReleased(MouseEvent e) {};
	public void mouseEntered(MouseEvent e) {};
	public void mouseExited(MouseEvent e) {};
    };

    public class VarCmdCanvas extends DBCanvas implements MouseListener
    {
	/** associated window */
        VarFrame win;
	/** data source */
	SVarSet vs;
	/** # of variables (cached from data source) */
	int vars;
	VarCanvas vc;
	Dimension minDim;

	/** constructs a new variable canvas for associated tree canvas
	    @param w window in which this canvsa is displayed
	    @param p associated tree canvas
	*/
	VarCmdCanvas(VarFrame w, SVarSet dataset) {
	    setBackground(new Color(255,255,192));
	    win=w; vs=dataset;
	    vars=vs.count();
	    addMouseListener(this);
	    vc=w.vc;
	    minDim=new Dimension(140,115);
	};

	public Dimension getMinimumSize() { return minDim; };

	/** implementation of the {@link DBCanvas#paintBuffer} method
	    @param g graphic context to paint on */
	public void paintBuffer(Graphics g) {
	    int totsel=0;
	    int i=0;
	    while (i<vars) {
		if (vc.selMask[i]) totsel++;
		i++;
	    };
	    Dimension cd=getSize();

	    Font fo=getFont();
	    Font f2=new Font(fo.getName(),Font.BOLD,fo.getSize());
	    Color C_varNam=new Color(0,0,128);
	    Color C_info=new Color(128,0,0);
	    Color C_bg=new Color(255,255,255);
	    Color C_sel=new Color(128,255,128);
	    Color C_frame=new Color(128,128,128);

            g.setColor(Color.black);
            g.drawString("Total "+vs.at(0).size()+" cases",10,16);
            
	    i=1;
	    String menu[]={"Exit","Open tree...","Hist/Barchar","Scatterplot","Boxplot"};
	    int j=0;
	    while (j<menu.length) {
		boolean boxValid=false;
		if (j==4 && totsel>0) { /* boxplot */
		    int bI=0, bJ=0, bK=0;
		    boolean crap=false;
		    while(bI<vars && bJ<2) {
			if (vc.selMask[bI]) {
			    if (vs.at(bI).isCat()) bJ++;
			    else {
				if (!vs.at(bI).isNum()) { crap=true; break; };			
				bK++;
			    };
			};
			bI++;
		    };
		    if (!crap && bJ<2 && bK>0) boxValid=true;
		};
		if ((j<2)||
		    ((j==2)&&(totsel>0))||boxValid||
		    ((j==3)&&(totsel==2))) {
		    g.setColor(C_bg);
		    g.fillRect(5,5+i*17,130,15);
		};
		g.setColor(C_frame);
		g.drawRect(5,5+i*17,130,15);
		g.setFont(f2); g.setColor(Color.black);	
		g.drawString(menu[j],20,17+i*17);
		i++; j++;
	    };
	};
    
	/* mouse actions */

	public void mouseClicked(MouseEvent ev) 
	{
	    if (vs==null) return;

	    int x=ev.getX(), y=ev.getY();
	    int svar=-1;
	    if ((x>5)&&(x<115)) svar=(y-3)/17;
	    int cmd=svar-1;
	    if (cmd==0) {
		if (WinTracker.current!=null)
		    WinTracker.current.disposeAll();
		System.exit(0);
	    };
	    if (cmd==1) {
		SNode t=InTr.openTreeFile(Common.mainFrame,null,vs);
		if (t!=null) {
		    TFrame f=new TFrame(InTr.lastTreeFileName);
		    TreeCanvas tc=InTr.newTreeDisplay(t,f);
		    tc.repaint(); tc.redesignNodes();		
		    //InTr.newVarDisplay(tvs);
		};    
	    };
	    if (cmd==2) {
		int i=0;
		for(i=0;i<vars;i++)
		    if (vc.selMask[i]) {		    
			TFrame f=new TFrame((vs.at(i).isCat()?"Barchart":"Histogram")+" ("+vs.at(i).getName()+")");
			f.addWindowListener(Common.defaultWindowListener);
			Canvas cvs=null;
			if (vs.at(i).isCat()) {
			    BarCanvas bc=new BarCanvas(f,vs.at(i),vs.getMarker()); cvs=bc;
			    if (vs.getMarker()!=null) vs.getMarker().addDepend(bc);	    
			} else {
			    HistCanvas hc=new HistCanvas(f,vs.at(i),vs.getMarker()); cvs=hc;
			    if (vs.getMarker()!=null) vs.getMarker().addDepend(hc);
			};
			cvs.setSize(new Dimension(400,300));
			f.add(cvs); f.pack(); f.show();
		    };
	    };
	    if (cmd==3) {
		int vnr[]=new int[2];
		int i,j=0,tsel=0;
		for(i=0;i<vars;i++) if (vc.selMask[i]) { vnr[j]=i; j++; tsel++; };
		if (tsel==2) {
		    TFrame f=new TFrame("Scatterplot ("+
					vs.at(vnr[1]).getName()+" vs "+
					vs.at(vnr[0]).getName()+")");
		    f.addWindowListener(Common.defaultWindowListener);
		    ScatterCanvas sc=new ScatterCanvas(f,vs.at(vnr[0]),vs.at(vnr[1]),vs.getMarker());
		    if (vs.getMarker()!=null) vs.getMarker().addDepend(sc);	    
		    sc.setSize(new Dimension(400,300));
		    f.add(sc); f.pack(); f.show();
		};
	    };
	    if (cmd==4) {
		int bI=0; int bJ=0;
		SVar catVar=null;
		while(bI<vars) {
		    if (vc.selMask[bI] && vs.at(bI).isCat()) {
			catVar=vs.at(bI); break;
		    };
		    bI++;
		};
		if (catVar==null) {
		    while(bJ<vars) {
			if (vc.selMask[bJ]) {
			    TFrame f=new TFrame("Boxplot ("+vs.at(bJ).getName()+")");
			    f.addWindowListener(Common.defaultWindowListener);
			    BoxCanvas sc=new BoxCanvas(f,vs.at(bJ),vs.getMarker());
			    if (vs.getMarker()!=null) vs.getMarker().addDepend(sc);	    
			    sc.setSize(new Dimension(50,300));
			    f.add(sc); f.pack(); f.show();
			};
			bJ++;
		    };
		} else {
		    while(bJ<vars) {
			if (vc.selMask[bJ] && bJ!=bI) {
			    TFrame f=new TFrame("Boxplot ("+vs.at(bJ).getName()+" grouped by "+catVar.getName()+")");
			    f.addWindowListener(Common.defaultWindowListener);
			    BoxCanvas sc=new BoxCanvas(f,vs.at(bJ),catVar,vs.getMarker());
			    if (vs.getMarker()!=null) vs.getMarker().addDepend(sc);	    
			    sc.setSize(new Dimension(catVar.getNumCats()*40,300));
			    f.add(sc); f.pack(); f.show();
			};
			bJ++;
		    };
		};
	    };
		
	};
	public void mousePressed(MouseEvent ev) {};
	public void mouseReleased(MouseEvent e) {};
	public void mouseEntered(MouseEvent e) {};
	public void mouseExited(MouseEvent e) {};
    };

    VarCanvas vc;
    VarCmdCanvas vcc;
    Scrollbar sb=null;

    public VarFrame(SVarSet vs, int x, int y, int w, int h) {
	super(vs.getName());
        setBackground(new Color(255,255,192));
	int rh=h;
	if (rh>vs.count()*17+6+115)
	    rh=vs.count()*17+6+115;
	setLayout(new BorderLayout());
	int minus=0;
	if (rh==h) {
	    add(sb=new Scrollbar(Scrollbar.VERTICAL,0,17,0,vs.count()*17+23+115-h),"East");
	    pack();
	    Dimension sbd=sb.getSize();
	    minus=sb.getWidth();
            sb.setBlockIncrement(17*4);
	};
	add(vc=new VarCanvas(this,vs,sb));
	if (rh!=h)
	    vc.minDim=new Dimension(w,rh-115);
	else
	    sb.addAdjustmentListener(vc);
	
	add(vcc=new VarCmdCanvas(this,vs),"South");
	if (Common.defaultWindowListener==null)
	    Common.defaultWindowListener=new DefWinL();
	addWindowListener(Common.defaultWindowListener);
	setBounds(x-minus,y,w,rh);
	vc.setBounds(x-minus,y,w,rh-115);
	vcc.setBounds(x-minus,y+rh-115,w,115);
	pack(); 
	show();
    };
};
