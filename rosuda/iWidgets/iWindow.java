//
//  iWindow.java
//  iWidgets
//
//  Created by Simon Urbanek on Wed Jul 21 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.iWidgets;

import java.awt.*;
import javax.swing.*;
import org.rosuda.util.*;
import org.rosuda.JGR.toolkit.*;

public class iWindow extends iFrame {
    public static final int clsIWindow = clsUser+1;
    BoxLayout rootLayout;
    
    public iWindow(String title, boolean horizontal) {
        super("iWindow: "+title, clsIWindow);
        getContentPane().setLayout(rootLayout=new BoxLayout(getContentPane(), horizontal?BoxLayout.X_AXIS:BoxLayout.Y_AXIS));
    }

    public Component add(Component c) {
        return getContentPane().add(c);
    }

    public void addGlue() {
        getContentPane().add(Box.createGlue());
    }

    public void addVSpace(int pix) {
        getContentPane().add(Box.createVerticalStrut(pix));
    }

    public void addHSpace(int pix) {
        getContentPane().add(Box.createHorizontalStrut(pix));
    }
    
}
