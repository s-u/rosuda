//
//  KapMeNodeCanvas.java
//  Klimt
//
//  Created by Simon Urbanek on Fri Jul 02 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.klimt.plots;

import java.awt.Frame;
import java.util.Vector;
import org.rosuda.ibase.*;
import org.rosuda.klimt.*;

public class KapMeNodeCanvas extends org.rosuda.ibase.plots.KapMeCanvas {
    NodeMarker nm;
    
    public KapMeNodeCanvas(Frame f, SVar time, SVar event, SMarker mark, NodeMarker nm) {
        super(f, time, event, mark);
        this.nm=nm;
        nm.addDepend(this);
    }

    public void Notifying(NotifyMsg msg, Object o, Vector path) {
        if (msg.getMessageID()==NodeMarker.NM_Change) {
            if (nm.getNode()==null)
                setFilter(null);
            else
                setFilter(nm.getNode().data);
        } else super.Notifying(msg, o, path);
    }
}
