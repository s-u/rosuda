package org.rosuda.InGlyphs;

import java.awt.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.pograss.*;

/**
 * Variables window - central place for operations on a dataset
 * @version $Id$
 **/

public class IndiFrame extends TFrame {

	IndiCanvas vc;
	IndiCmdCanvas vcc;
	Scrollbar sb=null;
	public static final int cmdHeight=220;

	public IndiFrame(SVarSet vs, int x, int y, int w, int h) {
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
		
		add(vc=new IndiCanvas(this,vs.at(0),sb));
		
		if (rh!=h) {
			vc.minDim=new Dimension(w,rh-cmdHeight);
		}
		else {
			sb.addAdjustmentListener(vc);
		}
		
		add(vcc=new IndiCmdCanvas(this,vs),"South");

		addWindowListener(Common.getDefaultWindowListener());
		setBounds(x-minus,y,w,rh);
		vc.setBounds(x-minus,y,w,rh-cmdHeight);
		vcc.setBounds(x-minus,y+rh-cmdHeight,w,cmdHeight);
		pack();
		String myMenu[] = {"individuals","variables"};
		//EzMenu.getEzMenu(this,vc,myMenu);
		setVisible(true);
	}
	
	public IndiCanvas getIndiCanvas() {
		return vc;
	}

	public IndiCmdCanvas getIndiCmdCanvas() {
		return vcc;
	}
}