import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * VarCmdCanvas is the canvas of commands for variables
 **/

class IndiCmdCanvas extends DBCanvas implements MouseListener, Dependent {
	
	/** associated window */
	IndiFrame win;
	
	/** data source */
	SVarSet vs;
	IndiCanvas vc;
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
	
	IndiCmdCanvas(IndiFrame w, SVarSet dataset) {
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
		minDim = new Dimension(140,132);
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
		if(mi!=null) mi.setEnabled(true);
		mi=EzMenu.getItem(win,"variables");
		if(mi!=null) mi.setEnabled(true);

		Dimension cd = getSize();

		if (Common.useAquaBg) {
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
		String menu[] = {"Polygons"};
		int j=0;
		while (j<menu.length) {
			if (
				(j==0 && totsel>0)
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

		if (cmd==0) vc.run(this,"polygons");
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