//
//  HistCanvasEx.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.klimt.plots;

import java.awt.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.ibase.plots.*;
import org.rosuda.pograss.*;
import org.rosuda.klimt.*;

/** extended histogram which plots currently selected split if any */
public class HistCanvasEx extends HistCanvas {
    NodeMarker nm;
    
    public HistCanvasEx(PlotComponent ppc, Frame f, SVar v, SMarker mark, NodeMarker nm) {
        super(ppc,f,v,mark);
        this.nm=nm;
        nm.addDepend(this);
    }
    
    public void paintPost(PoGraSS g) {
        SNode cn=(nm!=null)?nm.getNode():null;
        if (cn!=null) cn=(SNode) cn.at(0); // we must go for a child since split info is one level deeper
        if (cn!=null && cn.splitVar==v) {
            g.setColor(255,0,0);
            g.drawLine(ax.getValuePos(cn.splitValF),mTop,ax.getValuePos(cn.splitValF),H-mBottom);
        }
    }    
}
