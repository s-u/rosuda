package org.rosuda.JGR.toolkit;

//
//  iFrame.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;


public class iFrame extends JFrame {


    public static final int clsMain     = 1;
    public static final int clsSplash   = 2;
    public static final int clsVars     = 8;
    public static final int clsHelp     = 16;
    public static final int clsPlot     = 128;
    public static final int clsTree     = 129;
    public static final int clsMCP      = 130;
    public static final int clsDevPlot  = 131;
    public static final int clsTreeMap  = 132;

    public static final int clsBar      = 135;
    public static final int clsHist     = 136;
    public static final int clsScatter  = 137;
    public static final int clsBox      = 138;
    public static final int clsPCP      = 139;
    public static final int clsLine     = 140;
    public static final int clsMap      = 141;
    public static final int clsFD       = 142;
    public static final int clsTable    = 143;

    public static final int clsEditor   = 150;
    public static final int clsAbout    = 151;
    public static final int clsPrefs    = 152;

    public static final int clsJavaGD   = 160;

    public static final int clsUser     = 8192;

    private Dimension minimumSize;

    public WTentrySwing MYEntry;

    public ProgressLabel progress = new ProgressLabel();


    public iFrame(String title, int wclass) {
        String nativeLF = UIManager.getSystemLookAndFeelClassName();

        // Install the look and feel
        try {
            UIManager.setLookAndFeel(nativeLF);
        } catch (InstantiationException e) {
        } catch (ClassNotFoundException e) {
        } catch (UnsupportedLookAndFeelException e) {
        } catch (IllegalAccessException e) {
        }
        this.setTitle(title);
        this.addWindowListener(new WinListener());
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        if (WinTracker.current == null) WinTracker.current = new WinTracker();
        MYEntry = new WTentrySwing(WinTracker.current, this, title, wclass);
        initPlacement();
        this.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
                if (!Common.isMac()) {
                    int h = getHeight(),w= getWidth();
                    boolean resize = false;
                    if (minimumSize != null && getHeight() < minimumSize.height) { resize=true; h = minimumSize.height;}
                    if (minimumSize != null && getWidth() < minimumSize.width) { resize=true; w = minimumSize.width;}
                    if (resize) setSize(w,h);
                }
            }
            public void componentMoved(ComponentEvent e) {
            }
            public void componentShown(ComponentEvent e) {
            }
            public void componentHidden(ComponentEvent e) {
            }
        });
    }

    public iFrame() {
        this("<unnamed>", 0);
    }

    public WTentry getMYEntry() {
        return MYEntry;
    }

    public void finalize() {
        WinTracker.current.rm(MYEntry);
    }


    public void setMinimumSize(Dimension d) {
        minimumSize = d;
    }

    public void show() {
        this.setState(Frame.NORMAL);
        super.show();
    }

    static int lastClass=-1;
    static int lastPlaceX=0, lastPlaceY=0;
    static int lastOffset=0;

    public void initPlacement() { // initial frame placement
	if (MYEntry==null) return;
        if (lastClass!=MYEntry.wclass) {
            lastClass=MYEntry.wclass;
            lastPlaceX=getWidth()+10; lastPlaceY=0; lastOffset=0;
        } else {
            setLocation(lastPlaceX,lastPlaceY);
            lastPlaceX+=getWidth()+10;
            Common.getScreenRes();
            if (lastPlaceX+100>Common.screenRes.width) {
                lastPlaceY+=getHeight()+20;
                lastPlaceX=0;
                if (lastPlaceY+100>Common.screenRes.height) {
                    lastOffset+=30;
                    lastPlaceY=lastOffset; lastPlaceX=lastOffset;
                }
            }
        }
    }

    /*public void start(final String name) {
        Thread t = new Thread() {
            public void run() {
                progress.start("Working");
                setWorking(true);
                try {
                    Class c = Class.forName("org.rosuda.JGR."+name);
                    Frame f = (Frame) c.newInstance();
                    f.show();
                } catch (Exception e1) {
                    try {
                        Class c = Class.forName("org.rosuda.JGR.toolkit"+name);
                        Frame f = (Frame) c.newInstance();
                        f.show();
                    } catch (Exception e2) {}
                }
                setWorking(false);
            }
        };
        t.start();
    }*/

    public void setWorking(final boolean work) {
           SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                   if (work) {
                       cursorWait();
                   } else {
                       progress.stop();
                       cursorDefault();
                   }
               }
           });
    }

    public void cursorWait() {
        Component gp = getRootPane().getGlassPane();
        gp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        gp.setVisible(true);
    }

    public void cursorDefault() {
        Component gp = getRootPane().getGlassPane();
        gp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        gp.setVisible(false);
    }
}