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
import org.rosuda.ibase.plots.*;
import org.rosuda.pograss.*;
import org.rosuda.klimt.*;

/** extended histogram which plots currently selected split if any */
public class HistCanvasEx extends HistCanvasNew {
    NodeMarker nm;
    
    public HistCanvasEx(Frame f, SVar v, SMarker mark, NodeMarker nm) {
        super(f,v,mark);
        this.nm=nm;
    }
    
    public void paintPost(PoGraSS g) {
        SNode cn=(nm!=null)?nm.getNode():null;
        if (cn!=null && cn.splitVar==v) {
            g.setColor("red");
            g.drawLine(ax.getValuePos(cn.splitValF),mTop,ax.getValuePos(cn.splitValF),H-mBottom);
        }
    }    
}
