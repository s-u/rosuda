package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.util.*;
import org.rosuda.pograss.*;


/** query popup
 * @version $Id$
 */
// TODO: maybe implement MouseMotionListener to see items through popup window
public class AwtQueryPopup extends AWTGraphicsDevice implements MouseListener, QueryPopup {
    class QPCanvas extends AWTGraphicsDevice {
        String[] content;
        int width;
        
        Window win;
        SVarSet vs;
        
        int topMargin=8;
        int bottomMargin=8;
        int leftMargin=10;
        int rightMargin=10; // only for autoWidth, i.e. width<0
        
        public boolean doUpdate=true;
        
        // these are only temporary, QPCanvas is not a PlotComponent
        public void paintPoGraSS(PoGraSS p) {}
        public void beginPaint(PoGraSS p) {}
        public void endPaint(PoGraSS p) {}
                
        
        QPCanvas(final Window wn, final SVarSet uvs, final String ct, final int w, final int cid) {
            width=w; win=wn; vs=uvs;
            //		 setContent(ct,cid);
        }
        
        public void setContent(final String t, final int cid) {
            String s=t;
            if (vs!=null && cid>=0) {
                
                s+=" \n";
                int i = 0;
                while (i<vs.count()) {
                    if (vs.at(i)!=null && vs.at(i).isSelected()) {
                        s+=vs.at(i).getName()+": "+vs.at(i).atS(cid)+"\n";
                    }
                    i++;
                }
            }
            setContentString(s);
        }
        
        public void setContent(final String t, final int[] cid) {
            String s=t;
            if (vs!=null && cid!=null && cid.length>0) {
                
                s+="\n";
                boolean isFirst=true;
                int i = 0;
                while (i<vs.count()) {
                    if (vs.at(i)!=null && vs.at(i).isSelected()) {
                        if (isFirst) { isFirst=false; s+=" "; }
                        s += "\n";
                        if (vs.at(i).isNum()) {
                            int j=0;
                            double sum = 0.0;
                            double ct = 0.0;
                            while (j<cid.length) {
                                if (vs.at(i).at(cid[j])!=null) {
                                    sum+=vs.at(i).atD(cid[j]);
                                    ct+=1.0;
                                }
                                j++;
                            }
                            if (ct>0) {
                                double sd = 0.0;
                                final double mean=sum/ct;
                                j=0;
                                while (j<cid.length) {
                                    if (vs.at(i).at(cid[j])!=null) {
                                        final double dif=vs.at(i).atD(cid[j])-mean;
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
        
        void setContentString(final String s) {
            final StringTokenizer st=new StringTokenizer(s,"\n");
            final int tok=st.countTokens();
            if (tok<1) {
                content=new String[1]; content[0]="";
            } else {
                content=new String[tok];
                int i=0;
                for(;st.hasMoreElements();) {
                    final String c=st.nextToken();
                    content[i]=c; i++;
                }
            }
            //	    win.setVisible(true);
            //	    pcomp.initializeGraphics(win);
            doUpdate=true;
            setBackground(Common.popupColor);
            repaint();
        }
        
        int xh;
        
        void updateGeometry(final Graphics g) {
            if(content==null) return;
            int rw=width;
            final FontMetrics fm=g.getFontMetrics();
            
            xh=fm.getHeight();
            final int rh=xh*content.length+topMargin+bottomMargin;
            int j=0;
            int xw = 0;
            while(j<content.length) {
                final int lw=fm.stringWidth(content[j]);
                if (lw>xw) xw=lw;
                j++;
            }
            
            if (rw<30) rw=leftMargin+rightMargin+xw;
            doUpdate=false;
            final Dimension csz=getSize();
            if (csz.width!=rw || csz.height!=rh) {
                setSize(rw,rh);
                win.setSize(rw,rh);
                win.pack();
            }
        }
        
        public void paintLayer(final Graphics g, final int layer) {
            if (doUpdate) updateGeometry(g);
            final Dimension s=getSize();
            g.setColor(Color.black);
            g.drawRect(0,0,s.width-1,s.height-1);
            int y=topMargin+xh*3/4;
            int i=0;
            while (i<content.length) {
                g.drawString(content[i],leftMargin,y);
                y+=xh;
                i++;
            }
        }
        
    }
    
    QPCanvas cvs;
    Window win;
    Window owner;
    PlotComponent pc;
    
    public AwtQueryPopup(final Window ow, final SVarSet vs, final String ct, final int w, final int cid) {
    	if(ct==null) return;
        owner = ow; if (owner==null) owner=getParentWindow();
        win=new Window(owner);
        cvs=new QPCanvas(win,vs,ct,w,cid);
        win.add(cvs.getComponent());
        //	cvs.setContent(ct,cid);
        cvs.setSize(100,50);
        win.setBackground(Common.popupColor);
        win.addMouseListener(this);
        cvs.addMouseListener(this);
        win.pack();
    }
    
    public AwtQueryPopup(final Window ow, final SVarSet vs, final String ct) {
        this(ow,vs,ct,-1,-1);
    }

	public Window getParentWindow() {
		//System.out.println("AwtPlotComponent["+this+"].getParentWindow().comp="+comp);
		Container p = cvs.getParent();
		//System.out.println("  container: "+p);
		while (p!=null && !(p instanceof Window)) {
			//System.out.println("  container: "+p);
			p=p.getParent();
		}
//		System.out.println("-->"+p);
		return (Window)p;
	}
    
    public void setContent(final String s, final int[] cid) {
        // so far we ignore multi-case queries
        cvs.setContent(s,cid);
    }
    
    public void setContent(final String s, final int cid) {
        cvs.setContent(s,cid);
    }
    
    public void setContent(final String s) {
        cvs.setContent(s,-1);
    }
    
    public void show() {
        if (Global.DEBUG>0) System.out.println("QueryPopup.show");
        if (!win.isVisible()) {
            if (Global.DEBUG>0) System.out.println("rendering win visible");
            (win).pack(); win.setVisible(true);
        }
    }
    
    public void hide() {
        if (Global.DEBUG>0) System.out.println("QueryPopup.hide");
        if (win.isVisible()) {
            if (Global.DEBUG>0) System.out.println("hiding win");
            (win).setVisible(false);
            // old: ((Window)win).dispose();
        }
    }
    
    public void setLocation(int x, int y) {
        final Dimension d=win.getSize();
        if (y+d.height+5>Common.getScreenRes().height)
            y=Common.screenRes.height-d.height-5;
        if (x+d.width+5>Common.getScreenRes().width)
            x=Common.screenRes.width-d.width-5;
        win.setLocation(x,y);
    }
    
    public void mouseClicked(final MouseEvent e) {
        hide();
    }
    public void mousePressed(final MouseEvent e) {}
    public void mouseReleased(final MouseEvent e) {}
    public void mouseEntered(final MouseEvent e) {}
    public void mouseExited(final MouseEvent e) {
        hide();
    }
    
    public Component getQueryComponent() {
        return win;
    }
}

