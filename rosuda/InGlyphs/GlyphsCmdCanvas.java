package org.rosuda.InGlyphs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/**
 * VarCmdCanvas is the canvas of commands for variables
 **/

class GlyphsCmdCanvas extends DBCanvas implements MouseListener, Dependent {
	
	/** associated window */
	GlyphsFrame win;
	
	/** data source */
	SVarSet vs;
	GlyphsCanvas vc;
	Dimension minDim;
	SMarker sm;
	
	int c_vars;
	
	int genCount=0;
	
	/** selection mask of the variables **/
	boolean[] selMask;

	/** constructs a new variable commands canvas for associated tree canvas
	 * @param w window in which this canvas is displayed
	 * @param p associated tree canvas
	 **/
	
	GlyphsCmdCanvas(GlyphsFrame w, SVarSet dataset) {
		setBackground(Common.backgroundColor);
		win=w;
		vs=dataset;
		addMouseListener(this);
		vc=w.vc;
		c_vars=vs.count();
		selMask=new boolean[c_vars+4];
		sm=vs.getMarker();
		if (sm!=null) {
			sm.addDepend(this);
		}
		minDim = new Dimension(140,162);
	}

	public void Notifying(NotifyMsg msg, Object o, Vector path) {
		repaint();
	}

	public Dimension getMinimumSize() {
		return minDim;
	}

	/** implementation of the {@link DBCanvas#paintBuffer} method
		@param g graphic context to paint on */
	public void paintBuffer(Graphics g) {
		
		int totsel=0;
		int selCat=0;
		int selNum=0;
		boolean selMap=false;
		int i=0;
		
		while (i<vc.getVars()) {
			if (vc.selMask[i]) {
				totsel++;
				if (vs.at(i).isCat()) {
					selCat++;
				} 
				else if(vs.at(i).isNum()) {
					selNum++;
				} 
				if (vs.at(i).getContentsType()==SVar.CT_Map) {
					selMap=true;
				} 
			}
			i++;
		}

		MenuItem mi=null;
		mi=EzMenu.getItem(win,"individuals");
		if(mi!=null) mi.setEnabled(selNum>0);
		mi=EzMenu.getItem(win,"polygons");
		if(mi!=null) mi.setEnabled(selNum>0);
		mi=EzMenu.getItem(win,"histogram");
		if(mi!=null) mi.setEnabled(selNum>0);
		mi=EzMenu.getItem(win,"barchart");
		if(mi!=null) mi.setEnabled(selCat>0);
		mi=EzMenu.getItem(win,"boxplot");
		if(mi!=null) mi.setEnabled(selCat<2 && selNum>0);
		mi=EzMenu.getItem(win,"scatterplot");
		if(mi!=null) mi.setEnabled(selCat+selNum>1);
		mi=EzMenu.getItem(win,"fluct");
		if(mi!=null) mi.setEnabled(selCat==2);
		mi=EzMenu.getItem(win,"speckle");
		if(mi!=null) mi.setEnabled(selCat==3);
		mi=EzMenu.getItem(win,"PCP");
		if(mi!=null) mi.setEnabled(selCat+selNum>1);
		mi=EzMenu.getItem(win,"map");
		if(mi!=null) mi.setEnabled(selMap);
		mi=EzMenu.getItem(win,"lineplot");
		if(mi!=null) mi.setEnabled(selNum>0);
		mi=EzMenu.getItem(win,"lineplot2");
		if(mi!=null) mi.setEnabled(selNum>1);
		
		Dimension cd = getSize();

		if (Global.useAquaBg) {
			g.setColor(Color.white);
			g.fillRect(0, 0, cd.width, cd.height);
			int y=0;
			g.setColor(Common.aquaBgColor);
			while (y<cd.height-2) {
				g.fillRect(0,y,cd.width,2); y+=4;
			}
		}
	
		Font fo=getFont();
		Font f2=new Font(fo.getName(),Font.BOLD,fo.getSize());
		Color C_varNam=new Color(0,0,128);
		Color C_info=new Color(128,0,0);
		Color C_bg=new Color(255,255,255);
		Color C_sel=new Color(128,255,128);
		Color C_frame=new Color(128,128,128);

		g.setColor(Color.black);
		sm = vs.getMarker();
		if (sm!=null) {
			g.drawString("Selected "+sm.marked()+" of "+vs.at(0).size()+" cases",10,16);
		}
		else {
			g.drawString("Total "+vs.at(0).size()+" cases",10,16);
		}
		
		i=1;
		String menu[] = {
			"Exit",
			"Open tree...",
			"Individuals",
			"Polygons",
			"Barchart",
			"Histogram",
			"Scatterplot",
			"Boxplot",
			"Fluct.Diag.",
			"PCP",
			"Grow tree...",
			"Export..."
		};
		int j=0;
		while (j<menu.length) {
			boolean boxValid=false;
			if (j==7 && totsel>0) {
				/* boxplot */
				int bI=0, bJ=0, bK=0;
				boolean crap=false;
				while(bI<vc.getVars() && bJ<2) {
					if (vc.selMask[bI]) {
						if (vs.at(bI).isCat()) {
							bJ++;
						}
						else {
							if (!vs.at(bI).isNum()) {
								crap=true;
								break;
							}			
							bK++;
						}
					}
					bI++;
				}
				if (!crap && bJ<2 && bK>0) {
					boxValid=true;
				} 
			}
			if (
				(j<2) ||
				(j==2 && selCat==1) ||
				(j==3 && selCat>0) ||
				(j==4 && selCat==1 && selNum<2) ||
				(j==5 && selCat==0 && selNum==1) ||
				(j==6 && totsel==2) ||
				(j==7 && selCat==0 && boxValid) ||
				(j==8 && ((totsel==2 && selCat==2) || (totsel==3 && selCat==2 && selNum==1)	)) ||
				(j==9 && totsel>0) ||
				(j==10) ||
				(j==11 && totsel>0)
			) {
				g.setColor(C_bg);
				g.fillRect(5,5+i*17,130,15);
			}
			g.setColor(C_frame);
			g.drawRect(5,5+i*17,130,15);
			g.setFont(f2);
			g.setColor(Color.black);
			g.drawString(menu[j],20,17+i*17);
			i++;
			j++;
		}
	}

	/* mouse actions */

	public void mouseClicked(MouseEvent ev)	{

		if (vs==null) {
			return;
		}
		int x = ev.getX();
		int y = ev.getY();
		int svar = -1;
		if ((x>5)&&(x<115)) {
			svar = (y-3)/17;
		} 
		int cmd=svar-1;
		if (cmd==0) {
			if (WinTracker.current!=null) {
				WinTracker.current.disposeAll();
			}
			System.exit(0);
		}
		if (cmd==1) vc.run(this,"openTree");
		if (cmd==2) vc.run(this,"individuals");
		if (cmd==3) vc.run(this,"polygons");
		if (cmd==4) vc.run(this,"barchart");
		if (cmd==5) vc.run(this,"histogram");
		if (cmd==6) vc.run(this,"scatterplot");
		if (cmd==7) vc.run(this,"boxplot");
		if (cmd==8) vc.run(this,"fluct");
		if (cmd==9) vc.run(this,"PCP");
		if (cmd==10) vc.run(this,"growTree");
		if (cmd==11) vc.run(this,"export");

	}

	public void mousePressed(MouseEvent ev) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

}