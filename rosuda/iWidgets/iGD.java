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

public class iGD extends org.rosuda.javaGD.JavaGD {
    public iGD(java.awt.Frame f) {
        this.f=f;
    }

    public void     gdOpen(double w, double h) {
        c=new GDCanvas(w, h);
        f.add(c);
        f.validate();
    }

    public void     gdClose() {
        f.remove(c);
        f.validate();
        c=null;
    }
}
