/**
 * PoligonCanvas.java
 * InGlyphs
 * Created by Daniela Di Benedetto.
 **/
package org.rosuda.InGlyphs;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;
import org.rosuda.util.*;

/**
 * implementation of polygons
 * (new version - based on BaseCanvas).
 * @version $Id: PoligonCanvas.java,v 1.2
 * 2003/03/17 09:50:05 starsoft Exp $
 **/

public class PolygonCanvas extends BaseCanvas {

	/** all variables */
	SVar[] weights;

	/** associated category */
	SVar cat;
	int catPos;

	/** associated variables */
	int catVars;
	int catVarCats[];
	int totCatVarCats;
	int numVars;

	/** chart parameters */
	String chartType = "individuals"; 
	String dispType = "rectangle";
	String polRay = "same";
	String order = "areaorder";
	boolean centerPolygon = true;

	int cats;
	int triangles;
	int polygons;

	double cumulated[][];
	double normalized[][];
	double angles[][];
	double areas[];
	double rays[];
	
	double c_marked[];
	int positions[];

	String varType[];
	double sumNum[];
	double sumCat[];

	double anchor, binw;

	boolean inTick=false;

	int dragMode; // 0=none, 1=binw, 2=anchor
	int dragX;

	/**
	 * creates a new polygon canvas
	 * @param f frame owning this canvas
	 * or <code>null</code> if none
	 * @param var source variable
	 * @param mark associated marker
	 **/
	public PolygonCanvas(Frame f, SVar[] wvars, SMarker mark, String poltype, String disptype) {
		super(f,mark);

		weights=wvars;

		chartType = poltype;
		dispType = disptype;
		updatePolygons();
		updatePositions();

		setTitle("Polygons ("+cat.getName()+")");
		anchor=cat.getMin()-binw;
		String myMenu[]={"+","File","~File.Graph","~Edit","+","View","@RRotate","rotate","+","Polygons","Disp. circle","circle","Disp. rectangle","rectangle","Ray prop.","proportional","Ray same","same","Area order","areaorder","Init.order","initialorder","Variables","variables","Individuals","individuals","~Window","0"};
		EzMenu.getEzMenu(f,this,myMenu);
		mLeft=40;
		mRight=10;
		mTop=10;
		mBottom=20;
		allow180=true;
		allowDragZoom=false;
	}

	/**
	 * rebuilds vectors
	 **/
	public void updatePolygons() {

		if (chartType == "variables") { 
			if (triangles!=cat.getNumCats()) {
				cats=cat.getNumCats();
				triangles=cats;
				if (cat.hasMissing()) {
					triangles++;
				} 
			}

			polygons = weights.length-1;
			cumulated=new double[polygons][triangles];
			normalized=new double[polygons][triangles];
			angles=new double[polygons][triangles];				
			//c_marked=new double[triangles];
			sumNum = new double[polygons];
			
			for (int pol=0; pol<polygons; pol++) {
				for (int tri=0; tri<cat.size(); tri++) {
					int i=cat.getCatIndex(tri);
					if (i==-1) {
						i=cats;
					}
					if (weights[pol].isNum()) {
						double wval=weights[pol].atD(tri);
						if (wval<0) {
							wval=-wval;
						}
						cumulated[pol][i]+=wval;
						/*if ((m!=null)&&(m.at(tri))) {
							c_marked[i]+=wval;
						}*/
					}
					else {
						cumulated[pol][i]++;
						if ((m!=null)&&(m.at(tri))) {
							c_marked[i]++;
						} 
					}
				}
				for (int tri=0;tri<triangles;tri++) {
					sumNum[pol] += cumulated[pol][tri];
				}
				for (int tri=0;tri<triangles;tri++) {
					normalized[pol][tri] = cumulated[pol][tri] / sumNum[pol];
					angles[pol][tri] = 2 * Math.PI * normalized[pol][tri];
				}
			}
		}

		else if (chartType.equals("individuals")) {

			catVars = 0;
			numVars = 0;

			for (int var=0; var<weights.length; var++) {
				if (cat==null && weights[var].isCat()) {
					cat=weights[var];
					catPos=var;
				}
				else if (weights[var].isCat()) {
					catVars++;
				}
				else if (weights[var].isNum()) {
					numVars++;
				}
			}
			catVarCats = new int[catVars];

			int cvCat = 0;
			totCatVarCats = 0;

			for (int var=0; var<weights.length; var++) {
				if (var!=catPos) {
					if (weights[var].isCat()) {
						catVarCats[cvCat] = weights[var].getNumCats();
						totCatVarCats += weights[var].getNumCats();
						cvCat++;
					}
				}
			}

			if (polygons!=cat.getNumCats()) {
				cats=cat.getNumCats();
				polygons=cats;
				if (cat.hasMissing()) {
					polygons++;
				}
			}
			triangles = numVars + totCatVarCats;
			cumulated = new double[polygons][triangles];
			normalized = new double[polygons][triangles];
			angles = new double[polygons][triangles];
			areas = new double[polygons];
			rays = new double[polygons];
			//c_marked=new double[triangles];
			varType=new String[triangles];
			sumNum = new double[polygons];
			sumCat = new double[polygons];

			for (int pol=0; pol<cat.size(); pol++) {
				int ind=cat.getCatIndex(pol);
				int tri = 0;
				int varCat = 0;
				for (int var=0; var<weights.length; var++) {
					if (var!=catPos) {
						if (weights[var].isNum()) {
							double wval=weights[var].atD(pol);
							if (wval<0) {
								wval=-wval;
							}
							cumulated[ind][tri]+=wval;
							varType[tri] = "num";
							/*if ((m!=null)&&(m.at(ind))) {
								c_marked[ind]+=wval;
							}*/
							tri++;
						}
						else if (weights[var].isCat()) {
							cumulated[ind][tri+weights[var].getCatIndex(pol)]++;
							varType[tri+weights[var].getCatIndex(pol)] = "cat";
							tri += catVarCats[varCat];
							varCat++;
						}
					}
				}
			}
			for (int ind=0; ind<polygons; ind++) {
				for (int tri=0;tri<triangles;tri++) {
					if (varType[tri].equals("num"))	{
						sumNum[ind] += cumulated[ind][tri];
					}
					else if (varType[tri].equals("cat")) {
						sumCat[ind] += cumulated[ind][tri];
					}
				}
				areas[ind] = 0;
				if (polRay.equals("proportional")) {
					rays[ind] = (sumNum[ind]+sumCat[ind]);
				}
				else {
					rays[ind] = 1;
				}
				for (int tri=0; tri<triangles; tri++) {
					if (varType[tri].equals("num"))	{
						normalized[ind][tri] = cumulated[ind][tri] / sumNum[ind];
					}
					else if (varType[tri].equals("cat"))	{
						normalized[ind][tri] = cumulated[ind][tri] / sumCat[ind];
					}
					if (catVars==0 || numVars==0) {
						angles[ind][tri] = 2 * Math.PI * normalized[ind][tri];
					}
					else {
						angles[ind][tri] = Math.PI * normalized[ind][tri];
					}
					areas[ind] += rays[ind] * rays[ind] * Math.sin(angles[ind][tri]/2);

				}
			}
		}
	}

	public void updatePositions() {
		positions = new int[polygons];
		double[] ordareas = new double[polygons];
		for (int pol=0; pol<polygons; pol++) {
			ordareas[pol] = areas[pol];
		}
		int[] initPositions = new int[polygons];
		for (int pol=0; pol<polygons; pol++) {
			positions[pol] = pol;
			initPositions[pol] = pol;
		}
		System.out.println(order);
		if (order.equals("areaorder")) {
			for (int pol=0; pol<polygons; pol++) {
				for (int ind=pol; ind<polygons; ind++) {
					if (ordareas[ind]<ordareas[pol]) {
						int initPos = initPositions[ind];
						initPositions[ind] = initPositions[pol];
						initPositions[pol] = initPos;
						double area = ordareas[ind];
						ordareas[ind] = ordareas[pol];
						ordareas[pol] = area;
					}
				}
			}
			for (int pol=0; pol<polygons; pol++) {
				positions[initPositions[pol]] = pol;
			}
		}
	}

	public void updateObjects() {
	}

	public void paintBack(PoGraSS g) {

		int polElems;
		if (centerPolygon) {
			polElems = triangles+1;
		}
		else {
			polElems = triangles;
		}
		pp=null;
		if (pp==null) {
			pp = new PlotPrimitive[polygons*polElems];
		}
		if (dispType.equals("circle")) {
			g.setColor("black");
			g.drawLine(this.getWidth()/2,0,this.getWidth()/2,this.getHeight());
			g.drawLine(0,this.getHeight()/2,this.getWidth(),this.getHeight()/2);			
		}
		double mindim;
		double a = 0;
		double x;
		double y;
		double r;
		double pr;
		double ppr;
		double maxSum = 0;

		if (polygons==1) {
			mindim = Math.min(this.getWidth(),this.getHeight());
			x = this.getWidth()/2;
			y = this.getHeight()/2;
			pr= mindim*0.4;
			g.setColor("black");
			g.drawOval(Double.valueOf(String.valueOf(x)).intValue(),Double.valueOf(String.valueOf(y)).intValue(),Double.valueOf(String.valueOf(pr)).intValue()*2,Double.valueOf(String.valueOf(pr)).intValue()*2);
			drawPolygon(angles[0], 0, x, y, pr, centerPolygon);
		}
		else {
			if (polRay.equals("proportional")) {
				for (int i=0; i<angles.length; i++) {
					maxSum = Math.max(maxSum,sumNum[i]+sumCat[i]);
				}
			}
			if (dispType.equals("circle")) {
				mindim = Math.min(this.getWidth(),this.getHeight());
				r = (mindim/2 * polygons)/(Math.PI*0.8 + polygons);
				pr= Math.PI*0.8 * r/polygons;
				for (int i=0; i<polygons; i++) {
					ppr = pr;
					if (polRay.equals("proportional")) {
						ppr = pr * (sumNum[i]+sumCat[i])/maxSum;
					}
					a = 2 * Math.PI / polygons * i;
					x = this.getWidth()/2 + r * Math.cos(a);
					y = this.getHeight()/2 + r * Math.sin(a);
					g.setColor("black");
					g.drawOval(Double.valueOf(String.valueOf(x-ppr)).intValue(),Double.valueOf(String.valueOf(y-ppr)).intValue(),Double.valueOf(String.valueOf(ppr)).intValue()*2,Double.valueOf(String.valueOf(ppr)).intValue()*2);
					drawPolygon(angles[i], i, x, y, ppr, centerPolygon);
				}
			}
			else if (dispType.equals("rectangle")) {
				mindim = Math.min(this.getWidth(),this.getHeight());
				int nx = 0;
				int ny = 0;
				double nsqrt = Math.sqrt(polygons);
				int nq = Double.valueOf(String.valueOf(nsqrt)).intValue()+1;
				pr = mindim/nq/2;
				System.out.println("Print...");
				for (int i=0; i<polygons; i++) {
					ppr = pr;
					if (polRay.equals("proportional")) {
						ppr = pr * (sumNum[i]+sumCat[i])/maxSum;
					}

					double xx;
					double yy;
					
					xx = (positions[i]-Math.floor(positions[i]/nq)*nq)*pr*2;
					yy = (Math.floor(positions[i]/nq))*pr*2;
					//xx = (i-Math.floor(i/nq)*nq)*pr*2;
					//yy = (Math.floor(i/nq))*pr*2;

					System.out.print(i);
					System.out.print(" - ");
					System.out.print(positions[i]);
					System.out.print(" - ");
					System.out.print(areas[i]);
					System.out.println();

					x = xx+pr;
					y = yy+pr;
					g.setColor("black");
					g.drawOval(Double.valueOf(String.valueOf(x-ppr)).intValue(),Double.valueOf(String.valueOf(y-ppr)).intValue(),Double.valueOf(String.valueOf(ppr)).intValue()*2,Double.valueOf(String.valueOf(ppr)).intValue()*2);
					drawPolygon(angles[i], i, x, y, ppr, centerPolygon);
					nx++;
				}
			}
		}
	}

	public void mousePressed(MouseEvent ev) {
		int x=ev.getX(), y=ev.getY();
		if (y>H-mBottom) {
			if (x>mLeft-3 && x<mLeft+3) {
				dragMode=2;
			} 
			dragX=x;
		}
		else {
			super.mousePressed(ev);
		} 
	}

	public void mouseReleased(MouseEvent e) {
		if (dragMode!=0) {
			dragMode=0;
			updateObjects();
			setUpdateRoot(0);
			repaint();
		}
		else {
			super.mouseReleased(e);
		} 
	}

	public void mouseMoved(MouseEvent e) {
		int x=e.getX(), y=e.getY();
	}

	public void mouseDragged(MouseEvent e) {
		if (dragMode==0) {
			super.mouseDragged(e);
			return;
		}
		int x=e.getX();
		if (x!=dragX) {
			if (dragMode==1) {
				double nbv=ax.getValueForPos(x);
				if (nbv-ax.vBegin>0) {
					binw=nbv-ax.vBegin;
					updateObjects();
					setUpdateRoot(0);
					repaint();
				}
			}
			if (dragMode==2) {
				double na=ax.getValueForPos(x);
				anchor=na;
				if (anchor>cat.getMin()) {
					anchor=cat.getMin();
				} 
				if (anchor<cat.getMin()-binw) {
					anchor=cat.getMin()-binw;
				} 
				updateObjects();
				setUpdateRoot(0);
				repaint();
			}
		}
	}

	public String queryObject(int i) {
		if (pp!=null && pp[i]!=null && pp[i].cases()>0) {
			int mark=(int)(((double) pp[i].cases())*pp[i].getMarkedProportion(m,-1)+0.5);
		}
		return "Not Available";
	}

	public Object run(Object o, String cmd) {
		super.run(o,cmd);
		if (m!=null) {
			m.run(o,cmd);
		} 
		if (cmd=="print") {
			run(o,"exportPS");
		}
		if (cmd=="exportCases") {
			try {
				PrintStream p=Tools.getNewOutputStreamDlg(myFrame,"Export selected cases to ...","selected.txt");
				if (p!=null) {
					p.println(cat.getName());
					int i=0;
					for (Enumeration e=cat.elements(); e.hasMoreElements();) {
						Object oo=e.nextElement();
						if (m.at(i)) {
							if (oo!=null) {
								p.println(oo.toString());
							} 
							else {
								p.println("NA");
							} 
						}
						i++;
					}
					p.close();
				}
			}
			catch (Exception eee) {
			}
		}
		if (cmd=="exit") {
			WinTracker.current.Exit();
		} 
		if (cmd=="variables") {
			this.chartType="variables";
			updatePolygons();
			repaint();
		} 
		if (cmd=="individuals") {
			this.chartType="individuals";
			updatePolygons();
			repaint();
		} 
		if (cmd=="rectangle") {
			this.dispType="rectangle";
			repaint();
		} 
		if (cmd=="circle") {
			this.dispType="circle";
			repaint();
		} 
		if (cmd=="same") {
			this.polRay="same";
			repaint();
		} 
		if (cmd=="proportional") {
			this.polRay="proportional";
			repaint();
		} 
		if (cmd=="areaorder") {
			this.order="areaorder";
			updatePositions();
			repaint();
		} 
		if (cmd=="initialorder") {
			this.order="initialorder";
			updatePositions();
			repaint();
		} 
		return null;
	}

	public void drawPolygon(double[] a, int pol, double glyphCenterX, double glyphCenterY, double r, boolean centerPolygon) {
                PPrimPolygon centerPol=null;
		int nElems;
		if (centerPolygon) {
                    centerPol = new PPrimPolygon();
                    nElems = a.length+1;
                    pp[pol * nElems + nElems-1] = centerPol;
                    centerPol.pg = new Polygon();
                    centerPol.col=Color.red;
		}
		else {
			nElems = a.length;
		}

		/*System.out.print("n elements: ");
		System.out.print(nElems);
		System.out.println();*/

		double angle1=0,angle2=0;
		double x1,y1,x2,y2,x3,y3,x4,y4;
		double cpr = r/5;

		for ( int tri = 0; tri < a.length; tri++ ) {

			angle1 = angle2;
			angle2 += a[tri];

                        PPrimPolygon pr = new PPrimPolygon();
                        pp[pol * nElems + tri] = pr;
			pr.pg = new Polygon();
			pr.col=Common.getHCLcolor(tri*360/a.length);

			x1 = glyphCenterX + r * Math.cos(angle1);
			y1 = glyphCenterY + r * Math.sin(angle1);
			x2 = glyphCenterX + r * Math.cos(angle2);
			y2 = glyphCenterY + r * Math.sin(angle2);
			pr.pg.addPoint(Float.valueOf(String.valueOf(x1)).intValue(),Float.valueOf(String.valueOf(y1)).intValue());
			pr.pg.addPoint(Float.valueOf(String.valueOf(x2)).intValue(),Float.valueOf(String.valueOf(y2)).intValue());

			if (centerPolygon) {
				x3 = glyphCenterX + cpr * Math.cos(angle2);
				y3 = glyphCenterY + cpr * Math.sin(angle2);
				x4 = glyphCenterX + cpr * Math.cos(angle1);
				y4 = glyphCenterY + cpr * Math.sin(angle1);
				pr.pg.addPoint(Float.valueOf(String.valueOf(x3)).intValue(),Float.valueOf(String.valueOf(y3)).intValue());
				pr.pg.addPoint(Float.valueOf(String.valueOf(x4)).intValue(),Float.valueOf(String.valueOf(y4)).intValue());
				centerPol.pg.addPoint(Float.valueOf(String.valueOf(x3)).intValue(),Float.valueOf(String.valueOf(y3)).intValue());
			}
			else {
				pr.pg.addPoint(Float.valueOf(String.valueOf(glyphCenterX)).intValue(),Float.valueOf(String.valueOf(glyphCenterY)).intValue());
			}

			//pr.refType="variable";
			pr.ref=new int[2];
			pr.ref[0]=pol;
			pr.ref[1]=tri;
			/*System.out.print("pp: ");
			System.out.print(pol * nElems + tri);
			System.out.print("ref0: ");
			System.out.print(tri);
			System.out.println();*/
		}
		if (centerPolygon) {
			//centerPol.refType="individual";
			centerPol.ref=new int[2];
			centerPol.ref[0]=pol;
			centerPol.ref[1]=nElems-1 ;
		}
	}
}