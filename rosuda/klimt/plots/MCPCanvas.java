import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/** implementation of misclassification plot.
    @version $Id$
*/
public class MCPCanvas extends PGSCanvas implements Dependent, MouseListener, MouseMotionListener, KeyListener, ActionListener, Commander
{
    /** associated tree manager */
    RTree tm;
    /** associated marker */
    SMarker m;

    int count[];
    int mark[];
    int xv;

    int leftm=40, rightm=10, topm=10, botm=20;
    int dragMode; // 0=none, 1=binw, 2=anchor
    int dragX;
    
    int lastw, lasth;

    /** creates a new MCP canvas
	@param f frame owning this canvas or <code>null</code> if none
	@param var source variable
	@param mark associated marker
    */
    public MCPCanvas(Frame f,RTree tman, SMarker mark) {
        tm=tman; m=mark; setFrame(f); setTitle("MCP"); m.addDepend(this);
	setBackground(new Color(255,255,192));
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this); f.addKeyListener(this);
	MenuBar mb=null;
	String myMenu[]={"+","File","Save as PGS ...","exportPGS","Save as PostScript ...","exportPS","-","Save selected as ...","exportCases","-","Close","WTMclose","Quit","exit","0"};
	f.setMenuBar(mb=WinTracker.current.buildQuickMenuBar(f,this,myMenu,false));
        updateBoxes();
    };

    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        if (msg.getMessageID()==Common.NM_MarkerChange) updateBoxes();
	repaint();
    };

    public Dimension getMinimumSize() { return new Dimension(leftm+rightm+10,topm+botm+10); };

    void updateBoxes() {
        Vector v=tm.getTrees();
        if (v==null) return;
        int bs=v.size();
        if (Common.DEBUG>0)
            System.out.println("bs="+bs);
        if (bs==0) return;
        count=new int[bs];
        mark=new int[bs];
        int i=0;
        xv=0;
        SVar r=null;
        while(i<bs) {
            SNode n=(SNode)v.elementAt(i);
            if (r==null) r=n.response;
            SVar c=n.prediction;
            if (c!=null) {
                int j=0;
                while (j<c.size()) {
                    if (c.at(j).toString().compareTo(r.at(j).toString())!=0) {
                        count[i]++;
                        if (m.at(j)) mark[i]++;
                    }
                    j++;
                }
            }
            if (count[i]>xv) xv=count[i];
            if (Common.DEBUG>0)
                System.out.println("i="+i+", xv="+xv+", count="+count[i]+", mark="+mark[i]);
            i++;
        }
    }
    
    public void paintPoGraSS(PoGraSS g) {
	Dimension Dsize=getSize();
	int w=Dsize.width, h=Dsize.height;
        int rdx=w-leftm-rightm;
        int rdy=h-botm-topm;

        g.setBounds(w,h);
        g.begin();
        g.defineColor("fill",255,255,255);
        g.defineColor("selected",192,192,255);
        g.defineColor("outline",0,0,0);
        g.defineColor("marked",128,255,128);
        g.defineColor("red",255,0,0);

        g.setColor("outline");
        g.drawLine(leftm,h-botm,leftm,topm);
        g.drawLine(leftm,h-botm,w-rightm,h-botm);

        if (count!=null) {
            int bs=count.length;
            double ddx=((double)(rdx-bs*3))/((double)bs);
            double yf=((double)rdy)/((double)xv);
            int i=0;
            while(i<bs) {
                int x1=leftm+(int)(ddx*i)+i*3;
                g.setColor("fill");
                int ht=(int)(yf*count[i]);
                int hm=(int)(yf*mark[i]);
                g.fillRect(x1,h-botm-ht,(int)ddx,ht);
                if (mark[i]>0) {
                    g.setColor("marked");
                    g.fillRect(x1,h-botm-hm,(int)ddx,hm);
                };
                g.setColor("outline");
                g.drawRect(x1,h-botm-ht,(int)ddx,ht);
                i++;
            };
	};
        g.end();
    };
    
    public void mouseClicked(MouseEvent ev) 
    {
	int x=ev.getX(), y=ev.getY();
	int i=0, setTo=0;
	boolean effect=false;

        /*
	if (ev.isControlDown()) setTo=1;
	while (i<bars) {
	    if (Bars[i].contains(x,y)) {
		effect=true;
		if (!ev.isShiftDown()) m.selectNone();
		int j=0;
		for (Enumeration e=v.elements(); e.hasMoreElements();) {
		    Object o=e.nextElement();
		    if (o!=null) {
			double f=((Number)o).doubleValue();
			int box=(int)((f-ax.vBegin)/binw);
			if (box==i)
			    m.set(j,m.at(j)?setTo:1);
		    };
		    j++;
		};
	    };
	    i++;
	};
        */
	if (effect) m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
    };

    public void mousePressed(MouseEvent ev) {
	int x=ev.getX(), y=ev.getY();
        /*
	if (y>lasth-botm) {
	    if (x>leftm-3 && x<leftm+3) dragMode=2;
	    int bwp=ax.getValuePos(ax.vBegin+binw);
	    if (x>bwp-3 && x<bwp+3) dragMode=1;
	    dragX=x;
	};
        */
    };
    public void mouseReleased(MouseEvent e) {
        dragMode=0; repaint();
    };
    public void mouseEntered(MouseEvent e) {};
    public void mouseExited(MouseEvent e) {};

    public void mouseDragged(MouseEvent e) {
	int x=e.getX();
        /*
	if (x!=dragX) {
	    if (dragMode==1) {
		double nbv=ax.getValueForPos(x);
		if (nbv-ax.vBegin>0) {
		    binw=nbv-ax.vBegin;
		    
		    repaint();
		};
	    };
	    if (dragMode==2) {
		double na=ax.getValueForPos(x);
		anchor=na; if (anchor>v.getMin()) anchor=v.getMin();
                if (anchor<v.getMin()-binw) anchor=v.getMin()-binw;
		repaint();
	    };
	};
         */
    };
    public void mouseMoved(MouseEvent e) {
    };

    public void keyTyped(KeyEvent e) 
    {
	if (e.getKeyChar()=='C') run(this,"exportCases");
	if (e.getKeyChar()=='P') run(this,"print");
	if (e.getKeyChar()=='X') run(this,"exportPGS");
    };
    public void keyPressed(KeyEvent e) {};
    public void keyReleased(KeyEvent e) {};

    public Object run(Object o, String cmd) {
	super.run(o,cmd);
        if (cmd=="print") run(o,"exportPS");
        if (cmd=="exportCases") {
/*
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
*/
        };
        if (cmd=="exit") WinTracker.current.Exit();
        return null;        
    };

    /** action listener methods reroutes all request to the commander interface */
    public void actionPerformed(ActionEvent e) {
	if (e==null) return;
	run(e.getSource(),e.getActionCommand());
    };
};
