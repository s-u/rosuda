package org.rosuda.InGlyphs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.MenuItem;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.rosuda.ibase.Common;
import org.rosuda.ibase.DBCanvas;
import org.rosuda.ibase.SVar;
import org.rosuda.ibase.toolkit.EzMenu;
import org.rosuda.util.Global;

/**
 * @author Administrator
 **/
class IndiCanvas extends DBCanvas implements MouseListener, AdjustmentListener, ActionListener {

	/** associated window **/
	IndiFrame win;

	/** selection mask of the variables **/
	boolean[] selMask;

	/** data source **/
	//SVarSet vs;
	SVar cat;
	int cats = 0;
	String[] cat_nam;

	/** # of variables (cached from data source) - do NOT use directly, access via {@link #getVars} **/
	//int c_vars;

	/** scrollbar if too many vars are present **/
	Scrollbar sb;
	Dimension minDim;
	Dimension lastSize;

	int offset=0;
	int genCount=0;
	int firstSel=-1;

	/** constructs a new variable canvas (list of variables) for associated tree canvas
	 * @param w window in which this canvsa is displayed
	 * @param p associated tree canvas
	 **/
	IndiCanvas(IndiFrame w, SVar var,Scrollbar s) {
		setBackground(Common.backgroundColor);
		win=w;
		cat=var;
		cats = cat.getNumCats();
		selMask=new boolean[cats+4];
		addMouseListener(this);
		sb=s;
		minDim=new Dimension(140,100);
		MenuItem mi=null;
		mi=EzMenu.getItem(win,"individuals");
		//if(mi!=null) mi.setEnabled(true);
		mi=EzMenu.getItem(win,"polygons");
	}

	public int getVars() {
		return 0;
	}

	public void rebuildVars() {
		if (Global.DEBUG>0) {
			System.out.println("VarFrame.VarCanvas:rebuilding variable ("+cat.getName()+"/"+cats+")");
		}
		selMask=new boolean[cats+4];
		lastSize=null;
		repaint();
	}

	public void adjustmentValueChanged(AdjustmentEvent e) {
		offset=e.getValue();
		repaint();
	}

	public Dimension getMinimumSize() {
		return minDim;
	}

	/**
	 * implementation of the {@link DBCanvas#paintBuffer} method
	 * @param g graphic context to paint on
	 **/
	public void paintBuffer(Graphics g) {

		int totsel=0;
		Dimension cd=getSize();
		if (Global.useAquaBg) {
			g.setColor(Color.white);
			g.fillRect(0, 0, cd.width, cd.height);
			int y=0;
			g.setColor(Common.aquaBgColor);
			while (y<cd.height-2) {
				g.fillRect(0,y,cd.width,2); y+=4;
			}
		}

		if (lastSize==null || cd.width!=lastSize.width || cd.height!=lastSize.height) {
			int minh=getVars()*17+6;
			if (minh>200) {
				minh=200;
			}
			if (cd.width<140 || cd.height<minh) {
				setSize((cd.width<140)?140:cd.width,(cd.height<minh)?minh:cd.height);
				win.pack();
				cd=getSize();
			}
			minh=getVars()*17+6;
			if (sb!=null) {
				if (minh-cd.height+17<=0) {
					sb.setValue(offset=0);
					this.repaint();
					sb.setMaximum(0);
				}
				else {
					sb.setMaximum(minh-cd.height+17);
				}
			}
			lastSize=cd;
		}

		Font fo=getFont();
		Font f2=new Font(fo.getName(),Font.BOLD,fo.getSize());
		Color C_varNam=new Color(0,0,128);
		Color C_info=new Color(128,0,0);
		Color C_bg=new Color(255,255,255);
		Color C_sel=new Color(128,255,128);
		Color C_frame=new Color(128,128,128);
		Color C_intVar=new Color(216,216,255);
		if (cat!=null) {
			cats=cat.getNumCats();
			Object[] cts=cat.getCategories();
			cat_nam=new String[cats];
			for (int j=0;j<cats;j++) {
				cat_nam[j]=cts[j].toString();
				g.setColor(selMask[j]?C_sel:((cat.getInternalType()>0)?C_intVar:C_bg));
				g.fillRect(5,5+j*17-offset,130,15);
				g.setColor(C_frame);
				g.drawRect(5,5+j*17-offset,130,15);
				g.setFont(fo);
				g.setColor(C_info);
				g.setFont(f2);
				g.drawString(cat_nam[j],35,17+j*17-offset);					
				if (selMask[j]) {
					totsel++;
				}		
			}
		}
	}

	/**
	 *  mouse actions
	 **/

	public void mouseClicked(MouseEvent ev) {
		
		if (cat==null) {
			return;
		}
		int x=ev.getX();
		int y=ev.getY()+offset;
		int scat=-1;
		if ((x>5)&&(x<115)) {
			scat=(y-3)/17;
		}
		
		if (scat<cat.getNumCats()) {
			if (ev.isMetaDown() || ev.isControlDown()) {
				if (scat>=0) {
					selMask[scat]=!selMask[scat];
					if (cat!=null && cat.at(scat)!=null) {
						//cat.at(scat).setSelected(selMask[scat]);
					}
				}
			}
			else {
				if (firstSel==-1 || !ev.isShiftDown()) {
					for(int i=0;i<selMask.length;i++) {
						selMask[i]=false;
						/*if (cat!=null && cat.at(i)!=null) {
							cat.at(i).setSelected(false);
						}*/
					}
					firstSel=-1;
					if (scat>=0) {
						selMask[scat]=true;
						/*if (cat!=null && cat.at(scat)!=null) {
							cat.at(scat).setSelected(true);
						}*/
						firstSel=scat;
					}
				}
				else {
					if (scat>=0) {
						for(int i=0;i<selMask.length;i++) {
							selMask[i]=false;
							/*if (cat!=null && cat.at(i)!=null) {
								cat.at(i).setSelected(false);
							}*/
						}
						int j=firstSel;
						int k=firstSel;
						
						if (scat>firstSel) {
							k=scat;
						}
						else {
							j=scat;
						}
						
						while(j<=k) {
							selMask[j]=true;
							/*if (cat!=null && cat.at(j)!=null) {
								cat.at(j).setSelected(true);
							}*/
							j++;
						}
					}
				}
			}
			
			repaint();
			if (ev.getClickCount()==2) {
				repaint();
			}
		}
		
		win.getIndiCmdCanvas().repaint();
	}
	
	public void mousePressed(MouseEvent e) {
	}
	
	public void mouseReleased(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}

	public Object run(Object o, String cmd) {

		return null;
	}


	public void actionPerformed(ActionEvent e) {
		if (e==null) {
			return;
		}
	}
}