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
import java.awt.*;

public class iGD extends org.rosuda.javaGD.JavaGD {   
    public iGD() {
        super();
        f=null;
    }

    public Canvas getCanvas() { return c; }

    public void     gdOpen(double w, double h) {
        c=new GDCanvas(w, h);
        if (f!=null) {
            f.add(c);
            f.validate();
        }
    }

    public void     gdClose() {
        if (f!=null) {
            f.remove(c);
            f.validate();
        }
        c=null;
    }
}
