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

        ifr=new iFrame("JavaGD", iFrame.clsJavaGD) {
            public void dispose() {
                if (c!=null) executeDevOff();
                super.dispose();
            }
        };
        ifr.addWindowListener(this);

        String[] Menu = { "+","Edit","@CCopy (as image)","copyImg","~Window", "0" };
        iMenu.getMenu(ifr, this, Menu);

        ifr.setDefaultCloseOperation(ifr.DISPOSE_ON_CLOSE);
        c=new org.rosuda.javaGD.GDCanvas(w, h);
        ifr.getContentPane().add(c);
        ifr.pack();
        ifr.setVisible(true);
    }

    public void     gdNewPage(int devNr) {
        super.gdNewPage(devNr);
        ifr.setTitle("JavaGD ("+(devNr+1)+")"+(active?" *active*":""));
    }

    public void     gdActivate() {
        super.gdActivate();
        ifr.toFront();
        ifr.setTitle("JavaGD "+((devNr>0)?("("+(devNr+1)+")"):"")+" *active*");
    }

    public void     gdDeactivate() {
        super.gdDeactivate();
        ifr.setTitle("JavaGD ("+(devNr+1)+")");
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
        if (cmd.equals("copyImg"))
            org.rosuda.util.ImageSelection.copyComponent(c,false,true);
    }
}
