//
//  JavaGD.java
//  JRGui
//
//  Created by Simon Urbanek on Wed Apr 28 2004.
//  Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//  $Id$

package org.rosuda.JGR.toolkit;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.GDContainer;

/** Implementation of JavaGD which uses iFrame instead of Frame */
public class JavaGD extends GDInterface implements ActionListener, WindowListener {
    iFrame jfr;

    public void     gdOpen(double w, double h) {
        System.out.println("gdOpen");
        open=true;
        if (jfr!=null) gdClose();

        jfr=new iFrame("JavaGD", iFrame.clsJavaGD);        
        jfr.addWindowListener(this);

        String[] Menu = { "+","Edit","@CCopy (as image)","copyImg","~Window", "0" };
        iMenu.getMenu(jfr, this, Menu);

        jfr.setDefaultCloseOperation(jfr.DISPOSE_ON_CLOSE);
        System.out.println("gdOpen:creating JGDPanel");
        c=new org.rosuda.javaGD.JGDPanel(w, h);
        System.out.println("gdOpen:creating JGDPanel done");
        jfr.getContentPane().add((org.rosuda.javaGD.JGDPanel)c);
        jfr.pack();
        System.out.println("gdOpen:visible");
        jfr.setVisible(true);
        System.out.println("gdOpen:returning");
    }
    
    public void     gdNewPage(int devNr) {
        super.gdNewPage(devNr);
        jfr.setTitle("JavaGD ("+(getDeviceNumber()+1)+")"+(active?" *active*":""));
    }

    public void     gdActivate() {
        super.gdActivate();
        jfr.toFront();
        jfr.setTitle("JavaGD "+((getDeviceNumber()>0)?("("+(getDeviceNumber()+1)+")"):"")+" *active*");
    }

    public void     gdDeactivate() {
        super.gdDeactivate();
        jfr.setTitle("JavaGD ("+(getDeviceNumber()+1)+")");
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
            org.rosuda.util.ImageSelection.copyComponent((java.awt.Component)c,false,true);
    }

    public void windowClosing(WindowEvent e) {
        if (c!=null) executeDevOff();
    }
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}    
}
