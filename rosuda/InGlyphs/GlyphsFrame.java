package org.rosuda.InGlyphs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Scrollbar;

import org.rosuda.ibase.Common;
import org.rosuda.ibase.SVarSet;
import org.rosuda.ibase.toolkit.EzMenu;
import org.rosuda.ibase.toolkit.TFrame;

/**
 * Variables window - central place for operations on a dataset
 * @version $Id$
 **/

public class GlyphsFrame extends TFrame {

	GlyphsCanvas vc;
	GlyphsCmdCanvas vcc;
	IndiFrame indf;
	Scrollbar sb=null;
	public static final int cmdHeight=250;

	public GlyphsFrame(SVarSet vs, int x, int y, int w, int h) {
		super(vs.getName()+" (Variables)",TFrame.clsVars);
		setBackground(Common.backgroundColor);
		int rh=h;
		if (rh>vs.count()*17+6+cmdHeight+40) {
			rh=vs.count()*17+6+cmdHeight+40;
		}
		setLayout(new BorderLayout());
		int minus=0;
		if (rh==h) {
	    	add(sb=new Scrollbar(Scrollbar.VERTICAL,0,17,0,vs.count()*17+23+cmdHeight-h),"East");
			pack();
			Dimension sbd=sb.getSize();
			minus=sbd.width;
			sb.setBlockIncrement(17*4);
		}
		
		add(vc=new GlyphsCanvas(this,vs,sb));
		
		if (rh!=h) {
			vc.minDim=new Dimension(w,rh-cmdHeight);
		}
		else {
			sb.addAdjustmentListener(vc);
		}
		
		add(vcc=new GlyphsCmdCanvas(this,vs),"South");

		addWindowListener(Common.getDefaultWindowListener());
		setBounds(x-minus,y,w,rh);
		vc.setBounds(x-minus,y,w,rh-cmdHeight);
		vcc.setBounds(x-minus,y+rh-cmdHeight,w,cmdHeight);
		pack();
		String myMenu[] = {
			"+",
			"File",
			"@OOpen dataset ...",
			"openData",
			"!OOpen tree ...",
			"openTree",
			"-",
			"New derived variable ...",
			"deriveVar",
			"Grow tree ...",
			"growTree",
			"-",
			"Export forest ...",
			"exportForest",
			"Display Forest",
			"displayForest",
			"-",
			"@QQuit",
			"exit",
			"+",
			"Plot",
			"Barchart",
			"barchart",
			"Histogram",
			"histogram",
			"Boxplot",
			"boxplot",
			"-",
			"Scatterplot",
			"scatterplot",
			"Fluctuation diagram",
			"fluct",
			"-",
			"Speckle plot",
			"speckle",
			"Parallel coord. plot",
			"PCP",
			"Series plot",
			"lineplot",
			"Series plot with index",
			"lineplot2",
			"-",
			"Map",
			"map",
			"~Window",
			"0"
		};
		EzMenu.getEzMenu(this,vc,myMenu);
		setVisible(true);
	}
	
	public GlyphsCanvas getGlyphsCanvas() {
		return vc;
	}

	public GlyphsCmdCanvas getGlyphsCmdCanvas() {
		return vcc;
	}
}