//
//  iRadio.java
//  JRGui
//
//  Created by Simon Urbanek on Thu Jul 22 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.iWidgets;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Vector;

public class iRadio extends iGroup implements ActionListener {
    ButtonGroup bg;
    String[] it;
    JRadioButton[] rb;
    
    public iRadio(String[] items, int selected, boolean horizontal) {
        super(horizontal);
        it=items;
        ls=new Vector();
        rb=new JRadioButton[items.length];
        bg=new ButtonGroup();
        int i=0;
        while (i<items.length) {
            rb[i]=new JRadioButton(items[i], (i==selected));
            rb[i].addActionListener(this);
            add(rb[i]);
            bg.add(rb[i]);
            i++;
        }
        validate();
    }
   
    Vector ls;

    public int getSelectedID() {
        int i=0;
        while (i<rb.length) {
            if (rb[i].isSelected()) return i;
            i++;
        }
        return -1;
    }

    public void setSelectedID(int id) {
        if (id>=0 && id<rb.length)
            rb[id].setSelected(true);
    }
    
    public void addActionListener(ActionListener l) {
        ls.addElement(l);
    }

    public void removeActionListener(ActionListener l) {
        ls.removeElement(l);
    }

    public void actionPerformed(ActionEvent e) {
        int i=0;
        ActionEvent ne=new ActionEvent(this, e.getID(), e.getActionCommand(), e.getModifiers());
        while (i<ls.size()) {
            ActionListener l=(ActionListener) ls.elementAt(i++);
            l.actionPerformed(ne);
        }
    }
}
