//
//  iGroup.java
//  iWidgets
//
//  Created by Simon Urbanek on Wed Jul 21 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.iWidgets;

import javax.swing.*;

public class iGroup extends Box {
    public iGroup(boolean horizontal) {
        super(horizontal?BoxLayout.X_AXIS:BoxLayout.Y_AXIS);
    }

    public void addGlue() {
        add(Box.createGlue());
    }

    public void addVSpace(int pix) {
        add(Box.createVerticalStrut(pix));
    }

    public void addHSpace(int pix) {
        add(Box.createHorizontalStrut(pix));
    }
}
