package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---


import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.rosuda.JGR.*;
import org.rosuda.JGR.robjects.*;

/**
 * FunctionList - implemenation of a JList with some JGR specific features.
 * 
 * @author Markus Helbig
 *
 *	RoSuDa 2003 - 2005
 */

public class FunctionList extends JList implements MouseListener {

    private JGRObjectManager objmgr;

    public FunctionList(JGRObjectManager obm, Collection functions) {
        super(new Vector(functions));
        this.objmgr = obm;
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.addMouseListener(this);
    }

    /**
     * Refresh list and set supplied functions.
     * @param functions new functions
     */
    public void refresh(Collection functions) {
        this.setListData(new Vector(functions));
    }

    /**
     * mouseClicked: handle mouse event: double-click -> show function.
     */
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount()==2) {
            RObject o = null;
            try {
                o = (RObject) this.getSelectedValue();
            }
            catch (Exception ex) {
            }
            if (o != null)
                RController.newFunction(o);
        }
    }

    /**
     * mouseEntered: handle mouse event.
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * mouseExited: handle mouse event.
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * mousePressed: handle mouse event.
     */
    public void mousePressed(MouseEvent e) {
        if (objmgr.summary != null)	objmgr.summary.hide();
    }

    /**
     * mouseReleased: handle mouse event.
     */
    public void mouseReleased(MouseEvent e) {
    }

}
