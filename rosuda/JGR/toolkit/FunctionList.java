package org.rosuda.JGR.toolkit;
//
//  FunctionList.java
//  JGR
//
//  Created by Markus Helbig on Tue Aug 17 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.rosuda.JGR.*;
import org.rosuda.JGR.robjects.*;

public class FunctionList extends JList implements MouseListener {

    private JGRObjectManager objmgr;

    public FunctionList(JGRObjectManager obm, Collection functions) {
        super(new Vector(functions));
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.addMouseListener(this);
    }

    public void refresh(Collection functions) {
        this.setListData(new Vector(functions));
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount()==2) {
            RObject o = null;
            try {
                o = (RObject) this.getSelectedValue();
            }
            catch (Exception ex) {
            }
            if (o != null) {
                org.rosuda.ibase.SVarSet vs = RController.newSet(o);
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (objmgr.summary != null)	objmgr.summary.hide();
    }

    public void mouseReleased(MouseEvent e) {
    }

}
