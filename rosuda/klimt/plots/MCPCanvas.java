package org.rosuda.klimt.plots;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;
import org.rosuda.klimt.*;

/** implementation of misclassification plot.
    @version $Id$
*/
public class MCPCanvas extends PGSCanvas implements Dependent, MouseListener, MouseMotionListener, KeyListener, ActionListener, Commander
{
    /** associated tree manager */
    TreeRegistry tm;
    /** associated marker */
    SMarker m;

    /** # of misclassified cases for each tree */
    int count[];
    /** # of selected misclassified cases for each tree */
    int mark[];
    /** maximum of misclassified cases per tree */
    int xv;
    /** maximal range */
    int xr;

    /** alternate display (horizontal) */
    boolean xdisp=false;
    
    /** query popup handler */
    QueryPopup qi;
    
    /** margins */
    int leftm=10, rightm=10, topm=10, botm=20;
    int dragMode; // 0=none, 1=binw, 2=anchor
    int dragX,dragY;
    
    /** creates a new MCP canvas
	@param f frame owning this canvas or <code>null</code> if none
	@param var source variable
	@param mark associated marker
    */
    public MCPCanvas(Frame f,TreeRegistry tman, SMarker mark) {
        tm=tman; m=mark; setFrame(f); setTitle("MCP"); m.addDepend(this);
	setBackground(Common.backgroundColor);
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this); f.addKeyListener(this);
	MenuBar mb=null;
	String myMenu[]={"+","File","~File.Graph","~Edit","+","View","Toggle mode","rotate","~Window","0"};
	EzMenu.getEzMenu(f,this,myMenu);
        updateBoxes();
        qi=new QueryPopup(f,(mark==null)?null:mark.getMasterSet(),"MC-plot");
    };

    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        if (msg.getMessageID()==Common.NM_MarkerChange) updateBoxes();
	repaint();
    };

    public Dimension getMinimumSize() { return new Dimension(leftm+rightm+10,topm+botm+10); };

    void updateBoxes() {
        Vector v=tm.getTreeEntries();
        if (v==null) return;
        int bs=v.size();
        if (Global.DEBUG>0)
            System.out.println("bs="+bs);
        if (bs==0) return;
        count=new int[bs];
        mark=new int[bs];
        int i=0;
        xv=xr=0;
        SVar r=null;
        while(i<bs) {
            TreeEntry te=(TreeEntry)v.elementAt(i);
            SNode n=te.root;
            if (r==null) r=n.getRootInfo().response;
            SVar c=n.getRootInfo().prediction;
            if (c!=null) {
                int j=0;
                while (j<c.size()) {
                    if (c.at(j).toString().compareTo(r.at(j).toString())!=0) {
                        count[i]++;
                        if (m.at(j)) mark[i]++;
                    };
                    j++;
                }
            } else {
		System.out.println("Warning: n.prediction=null! n="+n+", "+((n==n.getRoot())?"is its root":"is not root"));
	    }
            if (count[i]>xv) xv=count[i];
            if (c!=null && c.size()-count[i]+xv>xr) xr=c.size()-count[i]+xv;
            if (Global.DEBUG>0)
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

        if (!xdisp) {
            g.setColor("outline");
            g.drawLine(leftm,h-botm,leftm,topm);
            g.drawLine(leftm,h-botm,w-rightm,h-botm);

            if (count!=null) { // misclass-only-view
                Vector v=tm.getTreeEntries();
                int bs=count.length;
                double ddx=((double)(rdx-bs*3))/((double)bs);
                double yf=((double)rdy)/((double)xv);
                int i=0;
                while(i<bs) {
                    TreeEntry te=(TreeEntry)v.elementAt(i);
                    SNode r=te.root;
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
                    g.drawString(Common.getTriGraph(r.getRootInfo().name),x1,h-botm/2);
                    i++;
                };
            };
        } else {
            if (count!=null) {
                int bs=count.length;
                double ddy=((double)(rdy))/((double)bs);
                double xf=((double)rdx)/((double)xr);
                int base=leftm+(int)(xf*xv);
                int i=0;
                int sc=(int)(xf*m.size());
                while(i<bs) {
                    int y1=topm+(int)(ddy*(0.15+(double)i));
                    g.setColor("fill");
                    int ht=(int)(xf*count[i]);
                    int hm=(int)(xf*mark[i]);
                    int rm=(int)(xf*(m.marked()-mark[i]));
                    g.fillRect(base-ht,y1,sc,(int)(ddy*0.7));
                    if (m.marked()>0) {
                        g.setColor("marked");
                        g.fillRect(base-hm,y1,hm+rm,(int)(ddy*0.7));
                    };
                    g.setColor("outline");
                    g.drawRect(base-ht,y1,sc,(int)(ddy*0.7));
                    i++;
                };
                g.setColor("outline");
                g.drawLine(base,topm,base,h-botm);
            }
            
        }
        g.end();
    };
    
    public void mouseClicked(MouseEvent ev) 
    {
        if (xdisp) return;
	Point cl=getFrame().getLocation();
	int x=ev.getX(), y=ev.getY();
        int i=0; boolean setTo=false;
	boolean effect=false;
        Dimension Dsize=getSize();
        int w=Dsize.width, h=Dsize.height;
        int rdx=w-leftm-rightm;
        int rdy=h-botm-topm;
        Vector v=tm.getTreeEntries();
        if (v==null) return;

        boolean hideQI=true;
        if (ev.isControlDown()) setTo=true;
        if (count!=null) {
            int bs=count.length;
            double ddx=((double)(rdx-bs*3))/((double)bs);
            double yf=((double)rdy)/((double)xv);
            if (!ev.isShiftDown()) m.selectNone();
            i=0;
            SVar r=null;
            while(i<bs) {
                int x1=leftm+(int)(ddx*i)+i*3;
                int ht=(int)(yf*count[i]);
                if (ht<10) ht=10;
                if (x>=x1&&x<x1+(int)ddx&&y<=h-botm&&y>=h-botm-ht) {
                    TreeEntry te=(TreeEntry)v.elementAt(i);
                    SNode rt=te.root;
                    if (Common.isQueryTrigger(ev)) {
                        String qs="Tree "+rt.getRootInfo().name+"\nMiscl. "+count[i]+" ("+mark[i]+" sel.)";
                        if (ev.isShiftDown()) {
                            qs="Tree "+rt.getRootInfo().name+" ("+i+")\nMisclassified:\n   "+count[i]+" of "+m.size()+" ("+
                            Tools.getDisplayableValue(100.0*((double)count[i])/((double)m.size()),2)+
                            "%)\nSelected:\n  "+mark[i]+" ("+
                            Tools.getDisplayableValue(100.0*((double)mark[i])/((double)count[i]),2)+
                            "% of miscl., " +
                            Tools.getDisplayableValue(100.0*((double)mark[i])/((double)m.size()),2)+"% of total)";
                        }
                        qi.setContent(qs);
                        qi.setLocation(cl.x+x,cl.y+y);
                        qi.show(); hideQI=false;                    
                    } else {
                        SNode n=te.root;
                        if (r==null) r=n.getRootInfo().response;
                        SVar c=n.getRootInfo().prediction;
                        if (c!=null) {
                            int j=0;
                            while (j<c.size()) {
                                if (c.at(j).toString().compareTo(r.at(j).toString())!=0)
                                    m.set(j,m.at(j)?setTo:true);
                                j++;
                            };
                        }; break;
                    };
                };
                i++;
            };
        };
        if (effect) m.NotifyAll(new NotifyMsg(m,Common.NM_MarkerChange));
        if (hideQI) qi.hide();
    }
    
    public void mousePressed(MouseEvent ev) {
        int x=ev.getX(), y=ev.getY();
        dragMode=1;
        dragX=x; dragY=y;
    }

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
        if (e.getKeyChar()=='R') run(this,"rotate");
    };
    public void keyPressed(KeyEvent e) {};
    public void keyReleased(KeyEvent e) {};

    public Object run(Object o, String cmd) {
	super.run(o,cmd);
        if (m!=null) m.run(o,cmd);
        if (cmd=="print") run(o,"exportPS");
        if (cmd=="rotate") {
            xdisp=!xdisp;
            repaint();
        };
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
