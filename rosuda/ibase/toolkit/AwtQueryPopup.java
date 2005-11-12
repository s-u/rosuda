package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.util.*;


/** query popup 
    @version $Id$
*/
// TODO: maybe implement MouseMotionListener to see items through popup window
public class AwtQueryPopup implements MouseListener, QueryPopup
{   
	class QPCanvas extends LayerCanvas {
	String[] content;
	int width;

	Window win;
    SVarSet vs;

	int topMargin=8;
	int bottomMargin=8;
	int leftMargin=10;
	int rightMargin=10; // only for autoWidth, i.e. width<0

	public boolean doUpdate=true;

	QPCanvas(PlotComponent pc, Window wn, SVarSet uvs, String ct, int w, int cid) {
		super(pc);
		width=w; win=wn; vs=uvs;
//		 setContent(ct,cid); 
	}

	public void setContent(String t, int cid) {
            String s=t;
            if (vs!=null && cid>=0) {
                int i=0;
                s+=" \n";
                while (i<vs.count()) {
                    if (vs.at(i)!=null && vs.at(i).isSelected()) {
                        s+=vs.at(i).getName()+": "+vs.at(i).atS(cid)+"\n";
                    }
                    i++;
                }
            }
            setContentString(s);
        }

        public void setContent(String t, int[] cid) {
            String s=t;
            if (vs!=null && cid!=null && cid.length>0) {
                int i=0;
                s+="\n";
                boolean isFirst=true;
                while (i<vs.count()) {
                    if (vs.at(i)!=null && vs.at(i).isSelected()) {
                        if (isFirst) { isFirst=false; s+=" "; }
                    	s += "\n";
                        if (vs.at(i).isNum()) {
                            int j=0;
                            double sum=0.0, ct=0.0, sd=0.0;
                            while (j<cid.length) {
                                if (vs.at(i).at(cid[j])!=null) {
                                    sum+=vs.at(i).atD(cid[j]);
                                    ct+=1.0;
                                }
                                j++;
                            }
                            if (ct>0) {
                                double mean=sum/ct;
                                j=0;
                                while (j<cid.length) {
                                    if (vs.at(i).at(cid[j])!=null) {
                                        double dif=vs.at(i).atD(cid[j])-mean;
                                        sd+=dif*dif;
                                    }
                                    j++;
                                }
                                
                                s+=vs.at(i).getName()+": mean="+Tools.getDisplayableValue(sum/ct)+" sd="+Tools.getDisplayableValue(Math.sqrt(sd/ct))+"\n";
                            }
                        } else {
                        }
                    }
                    i++;
                }
            }
            setContentString(s);
        }

        void setContentString(String s) {
            StringTokenizer st=new StringTokenizer(s,"\n");
	    int tok=st.countTokens();
	    if (tok<1) {
		content=new String[1]; content[0]="";
	    } else {
		content=new String[tok];
		int i=0;
		for(;st.hasMoreElements();) {
		    String c=st.nextToken();
		    content[i]=c; i++;
		};
	    };
//	    win.setVisible(true);
//	    pcomp.initializeGraphics(win);
	    doUpdate=true;
	    repaint();
	}

	int xh;

	void updateGeometry(Graphics g) {
	    int rw=width;
	    FontMetrics fm=g.getFontMetrics();
	    int xw=0;
	    xh=fm.getHeight();
	    int rh=xh*content.length+topMargin+bottomMargin;
	    int j=0;
	    while(j<content.length) {
		int lw=fm.stringWidth(content[j]);
		if (lw>xw) xw=lw;
		j++;
	    };

	    if (rw<30) rw=leftMargin+rightMargin+xw;
	    doUpdate=false;
	    Dimension csz=pc.getComponent().getSize();
	    if (csz.width!=rw || csz.height!=rh) {
	    pc.getComponent().setSize(rw,rh); 
		win.setSize(rw,rh);
		win.pack();
	    };
	}

	public void paintLayer(Graphics g, int layer) {
	    if (doUpdate) updateGeometry(g);
	    Dimension s=pc.getComponent().getSize();
            g.setColor(Color.black);
	    g.drawRect(0,0,s.width-1,s.height-1);
	    int y=topMargin+xh*3/4;
	    int i=0;
	    while (i<content.length) {
		g.drawString(content[i],leftMargin,y);
		y+=xh;
		i++;
	    };
	}
    }

    QPCanvas cvs;
    Window win;
    Window owner;
    PlotComponent pcomp;
    
    public AwtQueryPopup(PlotComponent pc, Frame own, SVarSet vs, String ct, int w, int cid)
    {
    pcomp = pc;
	owner=own;
	win=new Window(own);
	cvs=new QPCanvas(pcomp,(Window)win,vs,ct,w,cid);
	win.add(cvs.pc.getComponent());
//	cvs.setContent(ct,cid);
	cvs.pc.setSize(100,50);
	win.setBackground(Common.popupColor);
	win.addMouseListener(this);
	cvs.pc.getComponent().addMouseListener(this);
	win.pack();
    }

    public AwtQueryPopup(PlotComponent pc, Frame own, SVarSet vs, String ct) {
        this(pc,own,vs,ct,-1,-1);
    }

    public void setContent(PlotComponent pc, String s, int[] cid) {
        // so far we ignore multi-case queries
        cvs.setContent(s,cid);
    }

    public void setContent(PlotComponent pc, String s, int cid) {
	cvs.setContent(s,cid);
    }

    public void setContent(PlotComponent pc, String s) {
        cvs.setContent(s,-1);
    }
    
    public void show() {
	if (Global.DEBUG>0) System.out.println("QueryPopup.show");
	if (!win.isVisible()) {	 
	    if (Global.DEBUG>0) System.out.println("rendering win visible");
	    ((Window)win).pack(); win.setVisible(true);
	};
    }

    public void hide() {
	if (Global.DEBUG>0) System.out.println("QueryPopup.hide");
	if (win.isVisible()) {
	    if (Global.DEBUG>0) System.out.println("hiding win");
	    ((Window)win).setVisible(false);
	    // old: ((Window)win).dispose();
	};
    }
    
    public void setLocation(int x, int y) {
	Dimension d=win.getSize();
        if (y+d.height+5>Common.getScreenRes().height)
            y=Common.screenRes.height-d.height-5;
        if (x+d.width+5>Common.getScreenRes().width)
            x=Common.screenRes.width-d.width-5;
	win.setLocation(x,y);
    }
    
    public void mouseClicked(MouseEvent e) {
    	hide();
    }
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {
   		hide();
    }
    
    public Component getQueryComponent() {
    	return win;
    }
/*    
    public Window getOwnerWindow() {
    	return owner;
    }
    */
/*
    public Component getOwnerComponent() {
    	return pcomp.getComponent();
    }
    */
}

