//
//  iSlider.java
//  iWidgets
//
//  Created by Simon Urbanek on Wed Jul 21 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.iWidgets;

import javax.swing.*;

public class iSlider extends JSlider {
    public iSlider(boolean hor, int min, int max, int value) {
        super(hor?HORIZONTAL:VERTICAL, min, max, value);
    }
}
