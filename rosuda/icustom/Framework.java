//
//  Framework.java - framework glue between R and custom iplots
//
//  $Id$
//

package org.rosuda.icustom;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.ibase.plots.*;
import org.rosuda.util.*;
import java.awt.Dimension;

/** Framework glue between R and custom plots. */
public class Framework {
	org.rosuda.iplots.Framework ipfw;
	
	/** Constructs a new custom plots framework based on the root iPlots framework
	@param ipfw root framework for iPlots */
	public Framework(org.rosuda.iplots.Framework ipfw) {
		this.ipfw=ipfw;
	}

	public CustomCanvas newCustomplot(final int v, String rcall, String rid) {return newCustomplot("Custom Plot",new int[]{v},rcall,rid);}
    public CustomCanvas newCustomplot(final int v[], String rcall, String rid) {return newCustomplot("Custom Plot",v,rcall,rid);}
    public CustomCanvas newCustomplot(String name, final int[] v, String rcall, String rid) { return newCustomplot(name,ipfw.getCurrentSet(),v,rcall,rid); }
    public CustomCanvas newCustomplot(String name, final SVarSet vs, int[] v, String rcall, String rid) {
        if (v.length==0) return null;
        ipfw.updateMarker(vs,v[0]);
        
        FrameDevice frdev;
        frdev = ipfw.newFrame(name+" ("+vs.getName()+")",TFrame.clsCustom);
		frdev.initPlacement();
        frdev.setVisible(true);
        frdev.addWindowListener(Common.getDefaultWindowListener());
        final SVar[] vl=new SVar[v.length];
        int i=0;
        while(i<v.length) { vl[i]=vs.at(v[i]); i++; }
        final CustomCanvas cc=new CustomCanvas(ipfw.graphicsEngine,frdev.getFrame(),vl,vs.getMarker(),rcall,rid);
        frdev.add(cc.getComponent());
        if (vs.getMarker()!=null) vs.getMarker().addDepend(cc);
        cc.setSize(new Dimension(400,300));
        frdev.setSize(new Dimension(cc.getWidth(),cc.getHeight()));
        frdev.pack();
        cc.repaint();
        
        ipfw.addNewPlot(cc);
        return cc;
    }
}