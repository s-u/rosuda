//
//  iButton.java
//  iWidgets
//
//  Created by Simon Urbanek on Wed Jul 21 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.iWidgets;

import javax.swing.*;


public class iButton extends JButton {
    public iButton(String text, String action) {
        super(text);
        setActionCommand(action);
    }
}
