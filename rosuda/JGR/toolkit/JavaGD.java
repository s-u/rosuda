//
//  JavaGD.java
//  JRGui
//
//  Created by Simon Urbanek on Wed Apr 28 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//

package org.rosuda.JGR.toolkit;

import org.rosuda.JRI.Rengine;

/** Implementation of JavaGD which uses iFrame instead of Frame */
public class JavaGD extends org.rosuda.javaGD.JavaGD {
    iFrame ifr;
    
    public void     gdOpen(double w, double h) {
        if (ifr!=null) gdClose();

        ifr=new iFrame("JavaGD", iFrame.clsJavaGD);
        ifr.addWindowListener(this);
        ifr.setDefaultCloseOperation(ifr.DISPOSE_ON_CLOSE);
        c=new org.rosuda.javaGD.GDCanvas(w, h);
        ifr.setSize((int)w,(int)h); //added because sometimes the device isn't shown
        ifr.getContentPane().add(c);
        ifr.pack();
        ifr.setVisible(true);
    }

    public void     gdClose() {
        if (ifr!=null) {
            c=null;
            ifr.getContentPane().removeAll();
            ifr.dispose();
            ifr=null;
        }
    }    
}
