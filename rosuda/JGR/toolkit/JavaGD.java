//
//  JavaGD.java
//  JRGui
//
//  Created by Simon Urbanek on Wed Apr 28 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//

package org.rosuda.JGR.toolkit;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/** Implementation of JavaGD which uses iFrame instead of Frame */
public class JavaGD extends org.rosuda.javaGD.JavaGD implements ActionListener {
    iFrame jfr;

    public void     gdOpen(double w, double h) {
      if (jfr!=null) gdClose();

        jfr=new iFrame("JavaGD", iFrame.clsJavaGD) {
            public void dispose() {
                if (c!=null) executeDevOff();
                super.dispose();
            }
        };
        jfr.addWindowListener(this);

        String[] Menu = { "+","Edit","@CCopy (as image)","copyImg","~Window", "0" };
        iMenu.getMenu(jfr, this, Menu);

        jfr.setDefaultCloseOperation(jfr.DISPOSE_ON_CLOSE);
        c=new org.rosuda.javaGD.GDCanvas(w, h);
        jfr.getContentPane().add(c);
        jfr.pack();
        jfr.setVisible(true);
    }

    public void     gdNewPage(int devNr) {
        super.gdNewPage(devNr);
        jfr.setTitle("JavaGD ("+(devNr+1)+")"+(active?" *active*":""));
    }

    public void     gdActivate() {
        super.gdActivate();
        jfr.toFront();
        jfr.setTitle("JavaGD "+((devNr>0)?("("+(devNr+1)+")"):"")+" *active*");
    }

    public void     gdDeactivate() {
        super.gdDeactivate();
        jfr.setTitle("JavaGD ("+(devNr+1)+")");
    }
    
    public void     gdClose() {
        if (jfr!=null) {
            c=null;
            jfr.getContentPane().removeAll();
            jfr.dispose();
            jfr=null;
        }
    }

    // we'll use this once the menu is available ...
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("copyImg"))
            org.rosuda.util.ImageSelection.copyComponent(c,false,true);
    }
}
