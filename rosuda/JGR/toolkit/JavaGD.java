package org.rosuda.JGR.toolkit;

//
//JavaGD.java
//JRGui
//
//Created by Simon Urbanek on Wed Apr 28 2004.
//Copyright (c) 2004 Simon Urbanek. All rights reserved.
//
//$Id$



import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import org.rosuda.javaGD.GDInterface;

/** Implementation of JavaGD which uses iFrame instead of Frame */
public class JavaGD extends GDInterface implements ActionListener, WindowListener {
    iFrame jfr;

    public void     gdOpen(double w, double h) {
        open=true;
        if (jfr!=null) gdClose();

        jfr=new iFrame("JavaGD", iFrame.clsJavaGD) {
        	public void dispose() {
                if (c!=null) executeDevOff();
				super.dispose();
        	}
        };        
        jfr.addWindowListener(this);

        String[] Menu = { "+","File","Save as PDF...","savePDF","Save as EPS...","saveEPS","+","Edit","@CCopy (as image)","copyImg","~Window", "0" };
        iMenu.getMenu(jfr, this, Menu);

        jfr.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        c=new org.rosuda.javaGD.JGDBufferedPanel(w, h);
        jfr.getContentPane().add((org.rosuda.javaGD.JGDPanel)c);
        jfr.setSize((int)w,(int)h);
        jfr.pack();
        jfr.setVisible(true);
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
        super.gdClose();
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
		if (cmd.equals("savePDF"))
			org.rosuda.JRI.Rengine.getMainEngine().eval(".jgr.save.JavaGD.as(useDevice=pdf, source="+(getDeviceNumber()+1)+", onefile=TRUE, paper=\"special\")");
		if (cmd.equals("saveEPS"))
			org.rosuda.JRI.Rengine.getMainEngine().eval(".jgr.save.JavaGD.as(useDevice=postscript, "+(getDeviceNumber()+1)+", onefile=TRUE, paper=\"special\")");
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
