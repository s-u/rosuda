//
//  JavaGD.java
//  JRGui
//
//  Created by Simon Urbanek on Wed Apr 28 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//

package org.rosuda.JGR.toolkit;

import org.rosuda.JRI.Rengine;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/** Implementation of JavaGD which uses iFrame instead of Frame */
public class JavaGD extends org.rosuda.javaGD.JavaGD implements ActionListener {
    iFrame ifr;

    public void     gdOpen(double w, double h) {
        if (ifr!=null) gdClose();

        ifr=new iFrame("JavaGD", iFrame.clsJavaGD);
        ifr.addWindowListener(this);

        /* -- too much trouble with this - especially the "close" part...
         String[] Menu = { "~Window", "0" };
         iMenu.getMenu(ifr, this, Menu);
         */
        
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

    // we'll use this once the menu is available ...
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
    }
}
