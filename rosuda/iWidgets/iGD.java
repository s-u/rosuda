//
//  iGD.java
//  iWidgets
//
//  Created by Simon Urbanek on Wed Jul 21 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.iWidgets;

import org.rosuda.javaGD.*;
import java.awt.Canvas;
import javax.swing.JPanel;

public class iGD extends GDInterface {   
    public iGD() {
        super();
    }

    public Canvas getCanvas() { return (Canvas)c; }
    public JPanel getPanel() { return (JPanel)c; }

    public void     gdOpen(double w, double h) {
        c=new JGDPanel(w, h);
    }

    public void     gdClose() {
        c=null;
    }
}
