//
//  NodeMarker.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jul 30 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$

package org.rosuda.klimt;

import org.rosuda.ibase.*;

/** NodeMarker - marker specific for nodes. currently it supports selection of a single node */
public class NodeMarker extends Notifier {
    /** event sent on change */
    public static final int NM_Change = 0x007001;

    /** currently selected node or <code>null</code> if none */
    protected SNode currentNode;

    /** retrieve currently selected node */
    public SNode getNode() {
        return currentNode;
    }

    /** change currently selected node (and notify dependent objects if real change occured)
        @param n new node or <code>null</code> if no node should be selected */
    public void setNode(SNode n) {
        if (currentNode!=n) {
            currentNode=n;
            NotifyAll(new NotifyMsg(this,NodeMarker.NM_Change));
        }        
    }
}
