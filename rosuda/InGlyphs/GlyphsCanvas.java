import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

/**
 * @author Administrator
 **/
class GlyphsCanvas extends DBCanvas implements MouseListener, AdjustmentListener, Commander, ActionListener {

	/** associated window **/
	GlyphsFrame win;
	
	/** selection mask of the variables **/
	boolean[] selMask;
	
	/** data source **/
	SVarSet vs;
	
	/**
	 * # of variables (cached from data source)
	 * - do NOT use directly, access via {@link #getVars}
	 **/
	int c_vars;
	
	/**
	 * scrollbar if too many vars are present
	 **/
	Scrollbar sb;
	Dimension minDim;
	Dimension lastSize;

	int offset=0;
	int genCount=0;
	int firstSel=-1;
	
	/**
	 * constructs a new variable canvas (list of variables) for associated tree canvas
	 * @param w window in which this canvsa is displayed
	 * @param p associated tree canvas
	 **/
	GlyphsCanvas(GlyphsFrame w, SVarSet dataset,Scrollbar s) {
		setBackground(Common.backgroundColor);
		win=w;
		vs=dataset;
		c_vars=vs.count();
		selMask=new boolean[c_vars+4];
		addMouseListener(this);
		sb=s;
		minDim=new Dimension(140,100);
	}
	
	public int getVars() {
		if (vs.count()!=c_vars) rebuildVars();
		return c_vars;
	}

	public void rebuildVars() {
		if (Common.DEBUG>0) {
			System.out.println("VarFrame.VarCanvas:rebuilding variables ("+c_vars+"/"+vs.count()+")");
		}
		c_vars=vs.count();
		selMask=new boolean[c_vars+4];
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
		if (Common.useAquaBg) {
			g.setColor(Color.white);
			g.fillRect(0, 0, cd.width, cd.height);
			int y=0;
			g.setColor(Common.aquaBgColor);
			while (y<cd.height-2) {
				g.fillRect(0,y,cd.width,2); y+=4;
			}
		}
		if (c_vars!=vs.count()) {
			rebuildVars();
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
		int i=0;
		for (Enumeration e=vs.elements(); e.hasMoreElements();) {
			SVar v=(SVar)e.nextElement();
			if (selMask[i]) {
				totsel++;
			}
			g.setColor(selMask[i]?C_sel:((v.getInternalType()>0)?C_intVar:C_bg));
			g.fillRect(5,5+i*17-offset,130,15);
			g.setColor(C_frame);
			g.drawRect(5,5+i*17-offset,130,15);
			g.setFont(fo);
			g.setColor(C_info);
			
			String st="?";
			if (v.getContentsType()==SVar.CT_String) {
				st="S";
			}
			if (v.getContentsType()==SVar.CT_Number) {
				st="N";
			}
			if (v.getContentsType()==SVar.CT_Map) {
				st="Map";
			}
			g.drawString(st+(v.isCat()?"C":"")+(v.hasMissing()?"*":""),10,17+i*17-offset);
			g.setFont(f2);
			g.setColor(C_varNam);
			g.drawString(v.getName(),35,17+i*17-offset);
			i++;
		}
	}

	/**
	 *  mouse actions
	 **/

	public void mouseClicked(MouseEvent ev) {
		
		if (vs==null) {
			return;
		}
		int x=ev.getX();
		int y=ev.getY()+offset;
		int svar=-1;
		if ((x>5)&&(x<115)) {
			svar=(y-3)/17;
		}
		
		if (svar<vs.count()) {
			if (ev.isMetaDown() || ev.isControlDown()) {
				if (svar>=0) {
					selMask[svar]=!selMask[svar];
					if (vs!=null && vs.at(svar)!=null) {
						vs.at(svar).setSelected(selMask[svar]);
					}
				}
			}
			else {
				if (firstSel==-1 || !ev.isShiftDown()) {
					for(int i=0;i<selMask.length;i++) {
						selMask[i]=false;
						if (vs!=null && vs.at(i)!=null) {
							vs.at(i).setSelected(false);
						}
					}
					firstSel=-1;
					if (svar>=0) {
						selMask[svar]=true;
						if (vs!=null && vs.at(svar)!=null) {
							vs.at(svar).setSelected(true);
						}
						firstSel=svar;
					}
				}
				else {
					if (svar>=0) {
						for(int i=0;i<selMask.length;i++) {
							selMask[i]=false;
							if (vs!=null && vs.at(i)!=null) {
								vs.at(i).setSelected(false);
							}
						}
						int j=firstSel;
						int k=firstSel;
						
						if (svar>firstSel) {
							k=svar;
						}
						else {
							j=svar;
						}
						
						while(j<=k) {
							selMask[j]=true;
							if (vs!=null && vs.at(j)!=null) {
								vs.at(j).setSelected(true);
							}
							j++;
						}
					}
				}
			}
			
			repaint();
			if (ev.getClickCount()==2) {
				if (vs.at(svar).isNum()) {
					vs.at(svar).setCategorical(!vs.at(svar).isCat());
				}
				repaint();
			}
		}
		
		win.getGlyphsCmdCanvas().repaint();
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
		
		if (cmd=="exit") {
			WinTracker.current.Exit();
		}
		if (cmd=="exportForest") {
			try {
				PrintStream p=Tools.getNewOutputStreamDlg(win,"Export forest data to ...","forest.txt");
				vs.exportForest(p);
			}
			catch(Exception e) {
			}
		}
		if (cmd=="displayForest") {
			SVarSet fs=vs.getForestVarSet();
			Dimension sres=Toolkit.getDefaultToolkit().getScreenSize();
			Common.screenRes=sres;
			GlyphsFrame vf=InTr.newGlyphsDisplay(fs,sres.width-150,0,140,(sres.height>600)?600:sres.height);
		}
		
		if (cmd=="openData") {
			TFrame f=new TFrame("KLIMT "+Common.Version,TFrame.clsTree);
			SVarSet tvs = new SVarSet();
			SNode t=InTr.openTreeFile(f,null,tvs);
			if (t==null && tvs.count()<1) {
				new MsgDialog(f,"Load Error","I'm sorry, but I was unable to load the file you selected.");
			}
			else {
				f.setTitle(tvs.getName());
				Dimension sres=Toolkit.getDefaultToolkit().getScreenSize();
				Common.screenRes=sres;
				if (t!=null) {
					InTr.newTreeDisplay(t,f,0,0,sres.width-160,(sres.height>600)?600:sres.height-20);
				}
				GlyphsFrame vf=InTr.newGlyphsDisplay(tvs,sres.width-150,0,140,(sres.height>600)?600:sres.height);
			}
		}
		if (cmd=="map") {
			int i=0;
			SVar map=null;
			while (i<this.getVars()) {
				if (this.selMask[i])
				if (vs.at(i).getContentsType()==SVar.CT_Map) {
					map=vs.at(i);
				} 
				i++;
			}
			if (map!=null) {
				TFrame f=new TFrame("Map ("+map.getName()+")",TFrame.clsMap);
				f.addWindowListener(Common.defaultWindowListener);
				MapCanvas bc=new MapCanvas(f,map,vs.getMarker());
				if (vs.getMarker()!=null) {
					vs.getMarker().addDepend(bc);
				} 
				bc.setSize(new Dimension(400,300));
				f.add(bc);
				f.pack();
				f.show();
			}
		}
		if (cmd=="openTree") {
			SNode t=InTr.openTreeFile(Common.mainFrame,null,vs,true,true);
			if (t!=null) {
				this.getVars();
				this.repaint();
			}
		}
		if (cmd=="export") {
			try {
				PrintStream p=Tools.getNewOutputStreamDlg(Common.mainFrame,"Export selected variables to ...","selected.txt");
				if (p!=null) {
					int j=0,tcnt=0,fvar=0;
					j=0;
					while(j<vs.count()) {
						if(this.selMask[j]) {
							p.print(((tcnt==0)?"":"\t")+vs.at(j).getName());
							if (tcnt==0) {
								fvar=j;
							} 
							tcnt++;
						}
						j++;
					}
					p.println("");
					int i=0;
					SMarker m=vs.getMarker();
					boolean exportAll=(m==null || m.marked()==0);
					while (i<vs.at(fvar).size()) {
						if (exportAll || m.at(i)) {
							j=fvar;
							while(j<vs.count()) {
								if (this.selMask[j]) {
									Object oo=vs.at(j).at(i);
									p.print(((j==fvar)?"":"\t")+((oo==null)?"NA":oo.toString()));
								}
								j++;
							}
							p.println("");
						}
						i++;
					}
					p.close();
				}
			}
			catch (Exception eee) {
				if (Common.DEBUG>0) {
					System.out.println("* VarFrame.Export...: something went wrong during the export: "+eee.getMessage()); eee.printStackTrace();
				}
			}
		}
		if (cmd=="individuals") {
			int selC=0;
			SVar theCat=null;
			for (int i=0; i<this.getVars(); i++) {
				if (this.selMask[i]) {
					if (vs.at(i).isCat()) {
						selC++;
						theCat=vs.at(i);
					}
				}
			}
			if (selC==1) {
				
				win.indf = InTr.newIndiDisplay(vs,0,0,140,600);

				win.indf.vc.cat = theCat;
				win.indf.vc.repaint();
			}		
		}
		if (cmd=="polygons") {
			int selV=0;
			for (int i=0; i<this.getVars(); i++) {
				if (this.selMask[i]) {
					selV++;
				}
			}
			SVar[] theVars=new SVar[selV];
			String theVarName = "";
			int j=0;
			for (int i=0; i<this.getVars(); i++) {
				if (this.selMask[i]) {
					theVars[j]=vs.at(i);
					theVarName += theVars[j].getName() + ", ";
					j++;
				}
			}
			if (selV>0) {
				TFrame f=new TFrame("Polygons ("+theVarName+")",TFrame.clsBar);
				f.addWindowListener(Common.defaultWindowListener);
				PolygonCanvas pc = new PolygonCanvas(f,theVars,vs.getMarker(),"individuals","rectangle");
				if (vs.getMarker()!=null) {
					vs.getMarker().addDepend(pc);
				} 
				pc.setSize(new Dimension(400,300));
				f.add(pc);
				f.pack();
				f.show();
				f.initPlacement();
			}
		}
		if (cmd=="barchart") {
			int i=0;
			int selC=0;
			int selN=0;
			SVar theCat=null;
			SVar theNum=null;
			while (i<this.getVars()) {
				if (this.selMask[i]) {
					if (vs.at(i).isCat()) {
						selC++;
						theCat=vs.at(i);
					}
					else if (vs.at(i).isNum()) {
						selN++;
						theNum=vs.at(i);
					}
				}
				i++;
			}
			if (selC==1 && selN==1) {
				TFrame f=new TFrame("w.Barchart ("+theCat.getName()+"*"+theNum.getName()+")",TFrame.clsBar);
				f.addWindowListener(Common.defaultWindowListener);
				BarCanvas bc = new BarCanvas(f,theCat,vs.getMarker(),theNum);
				if (vs.getMarker()!=null) {
					vs.getMarker().addDepend(bc);
				} 
				bc.setSize(new Dimension(400,300));
				f.add(bc);
				f.pack();
				f.show();
				f.initPlacement();
			}
			if (selC==1 && selN==0) {
				TFrame f=new TFrame("w.Barchart ("+theCat.getName()+")",TFrame.clsBar);
				f.addWindowListener(Common.defaultWindowListener);
				BarCanvas bc = new BarCanvas(f,theCat,vs.getMarker());
				if (vs.getMarker()!=null) {
					vs.getMarker().addDepend(bc);
				} 
				bc.setSize(new Dimension(400,300));
				f.add(bc);
				f.pack();
				f.show();
				f.initPlacement();
			}
		}
		if (cmd=="histogram") {
			int i=0;
			int selC=0;
			int selN=0;
			SVar theCat=null;
			SVar theNum=null;
			while (i<this.getVars()) {
				if (this.selMask[i]) {
					if (vs.at(i).isCat()) {
						selC++;
						theCat=vs.at(i);
					}
					else if (vs.at(i).isNum()) {
						selN++;
						theNum=vs.at(i);
					}
				}
				i++;
			}
			if ((selN==1 && selC==0)) {
				for(i=0; i<this.getVars(); i++) {
					if (this.selMask[i]) {
						TFrame f=new TFrame(
							(vs.at(i).isCat()?"Barchart":"Histogram")+" ("+vs.at(i).getName()+")",
							vs.at(i).isCat()?TFrame.clsBar:TFrame.clsHist
						);
						f.addWindowListener(Common.defaultWindowListener);
						Canvas cvs=null;
						if (vs.at(i).isCat()) {
							BarCanvas bc=new BarCanvas(f,vs.at(i),vs.getMarker());
							cvs=bc;
							if (vs.getMarker()!=null) {
								vs.getMarker().addDepend(bc);
							}
						}
						else {
							HistCanvasNew hc=new HistCanvasNew(f,vs.at(i),vs.getMarker());
							cvs=hc;
							if (vs.getMarker()!=null) {
								vs.getMarker().addDepend(hc);
							} 
						}
						
						cvs.setSize(new Dimension(400,300));
						f.add(cvs);
						f.pack();
						f.show();
						f.initPlacement();
					}
				}
			}
		}
		if (cmd=="scatterplot") {
			int vnr[]=new int[2];
			int i,j=0,tsel=0;
			for(i=0; i<this.getVars(); i++) {
				if (this.selMask[i]) {
					vnr[j]=i;
					j++;
					tsel++;
				}
			}
			if (tsel==2) {
				TFrame f=new TFrame(
					"Scatterplot (" + vs.at(vnr[1]).getName()+" vs "+
					vs.at(vnr[0]).getName()+")",TFrame.clsScatter
				);
				f.addWindowListener(Common.defaultWindowListener);
				ScatterCanvas sc=new ScatterCanvas(f,vs.at(vnr[0]),vs.at(vnr[1]),vs.getMarker());
				if (vs.getMarker()!=null) vs.getMarker().addDepend(sc);
				sc.setSize(new Dimension(400,300));
				f.add(sc);
				f.pack();
				f.show();
				f.initPlacement();
			}
		}
		if (cmd=="boxplot") {
			int bI=0;
			int bJ=0;
			SVar catVar=null;
			while(bI<this.getVars()) {
				if (this.selMask[bI] && vs.at(bI).isCat()) {
					catVar=vs.at(bI);
					break;
				}
				bI++;
			}
			if (catVar==null) {
				while(bJ<this.getVars()) {
					if (this.selMask[bJ]) {
						TFrame f=new TFrame("Boxplot ("+vs.at(bJ).getName()+")",TFrame.clsBox);
						f.addWindowListener(Common.defaultWindowListener);
						BoxCanvas sc=new BoxCanvas(f,vs.at(bJ),vs.getMarker());
						if (vs.getMarker()!=null) vs.getMarker().addDepend(sc);
						sc.setSize(new Dimension(80,300));
						f.add(sc);
						f.pack();
						f.show();
					}
					bJ++;
				}
			}
			else {
				int lx=0, ly=0;
				while(bJ<this.getVars()) {
					if (this.selMask[bJ] && bJ!=bI) {
						TFrame f=new TFrame("Boxplot ("+vs.at(bJ).getName()+" grouped by "+catVar.getName()+")",TFrame.clsBox);
						f.addWindowListener(Common.defaultWindowListener);
						BoxCanvas sc=new BoxCanvas(f,vs.at(bJ),catVar,vs.getMarker());
						if (vs.getMarker()!=null) {
							vs.getMarker().addDepend(sc);
						} 
						sc.setSize(new Dimension(40+catVar.getNumCats()*40,300));
						f.add(sc);
						f.pack();
						f.show();
						f.initPlacement();
					}
					bJ++;
				}
			}
		}
		if (cmd=="fluct" || cmd=="speckle") {
			int vnr[]=new int[2];
			SVar weight=null;
			int i,j=0,tsel=0;
			for(i=0; i<this.getVars(); i++) if (this.selMask[i]) {
				if (cmd=="speckle" && j==2 && weight==null && vs.at(i).isCat()) {
					weight=vs.at(i);
				} 
				if(vs.at(i).isCat() && j<2) {
					vnr[j]=i;
					j++;
					tsel++;
				}
				if(!vs.at(i).isCat() && vs.at(i).isNum() && weight==null) {
					weight=vs.at(i);
				} 
			}
			if (tsel==2) {
				TFrame f=new TFrame(
					((weight==null)?"":"W")+ "FD ("+	vs.at(vnr[1]).getName()+" vs "+	vs.at(vnr[0]).getName()+")"+ ((weight==null)?"":"*"+weight.getName()),
					TFrame.clsFD
				);
				f.addWindowListener(Common.defaultWindowListener);
				FluctCanvas sc;
				if (cmd=="speckle" && weight!=null && weight.isCat()) {
					sc=new FCCCanvas(f,vs.at(vnr[0]),vs.at(vnr[1]),vs.getMarker(),weight);
				}
				else {
					sc=new FluctCanvas(f,vs.at(vnr[0]),vs.at(vnr[1]),vs.getMarker(),weight);
				}
				if (vs.getMarker()!=null) {
					vs.getMarker().addDepend(sc);
				} 
				sc.setSize(new Dimension(400,300));
				f.add(sc);
				f.pack();
				f.show();
				f.initPlacement();
			}
		}
		if (cmd=="PCP") {
			int i,j=0,tsel=0;
			for(i=0;i<this.getVars();i++) {
				if (this.selMask[i] && vs.at(i).isNum()) {
					tsel++;
				}
			} 
			if (tsel>0) {
				SVar[] vl=new SVar[tsel];
				for(i=0;i<this.getVars();i++) {
					if (this.selMask[i] && vs.at(i).isNum()) {
						vl[j]=vs.at(i); j++;
					}
				}
				TFrame f=new TFrame("Parallel coord. plot",TFrame.clsPCP);
				f.addWindowListener(Common.defaultWindowListener);
				PCPCanvas sc=new PCPCanvas(f,vl,vs.getMarker());
				if (vs.getMarker()!=null) {
					vs.getMarker().addDepend(sc);
				} 
				sc.setSize(new Dimension(400,300));
				f.add(sc);
				f.pack();
				f.show();
				f.initPlacement();
			}
		}
		if (cmd=="growTree") {
			ProgressDlg pd=new ProgressDlg(null,"Running tree generation plugin ...");
			pd.setText("Initializing plugin, loading R ...");
			pd.show();
			Plugin gt=PluginManager.loadPlugin("PluginGetTreeR");
			if (gt==null || !gt.initPlugin()) {
				pd.dispose();
				new MsgDialog(win,"Plugin init failed","Cannot initialize plugin.\n"+((gt==null)?"Tree generation plugin not found":gt.getLastError()));
				return null;
			}
			gt.setParameter("dataset",vs);
			gt.checkParameters();
			pd.setVisible(false);
			if (!gt.pluginDlg(win)) {
				pd.dispose();
				if (gt.cancel) {
					gt.donePlugin();
					return null;
				}
				new MsgDialog(win,"Parameter check failed","Some of your selections are invalid.\n"+gt.getLastError());
				return null;
			}
			pd.setProgress(40);
			pd.setVisible(true);
			if (!gt.execPlugin()) {
				pd.dispose();
				HelpFrame hf=new HelpFrame();
				hf.t.setText("Tree generation failed.\n"+gt.getLastError()+"\n\nDump of R output (if any):\n"+gt.getParameter("lastdump"));
				hf.setTitle("Plugin execution failed");
				hf.show();
				return null;
			}
			pd.setProgress(100);
			SNode nr=(SNode)gt.getParameter("root");
			gt.donePlugin();
			if (nr!=null) {
				genCount++;
				TFrame fff=new TFrame("Generated_"+genCount,TFrame.clsTree);
				TreeCanvas tc=InTr.newTreeDisplay(nr,fff);
			}
			pd.dispose();
		}

		if (cmd=="deriveVar") { // derive variable
			ProgressDlg pd=new ProgressDlg(null,"Running var generation plugin ...");
			pd.setText("Initializing plugin, loading R ...");
			pd.show();
			Plugin gt=PluginManager.loadPlugin("PluginDeriveVar");
			if (gt==null || !gt.initPlugin()) {
				pd.dispose();
				new MsgDialog(win,"Plugin init failed","Cannot initialize plugin.\n"+((gt==null)?"Tree generation plugin not found":gt.getLastError()));
				return null;
			}
			gt.setParameter("dataset",vs);
			gt.checkParameters();
			pd.setVisible(false);
			if (!gt.pluginDlg(win)) {
				pd.dispose();
				if (gt.cancel) {
					gt.donePlugin();
					return null;
				}
				new MsgDialog(win,"Parameter check failed","Some of your selections are invalid.\n"+gt.getLastError());
				return null;
			}
			pd.setProgress(40);
			pd.setVisible(true);
			if (!gt.execPlugin()) {
				pd.dispose();
				HelpFrame hf=new HelpFrame();
				hf.t.setText("Variable generation failed.\n"+gt.getLastError()+"\n\nDump of R output (if any):\n"+gt.getParameter("lastdump"));
				hf.setTitle("Plugin execution failed");
				hf.show();
				return null;
			}
			pd.setProgress(100);
			gt.donePlugin();
			pd.dispose();
		}

		return null;
	}

	public void actionPerformed(ActionEvent e) {
		if (e==null) {
			return;
		} 
		run(e.getSource(),e.getActionCommand());
	}
}