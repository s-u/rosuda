package org.rosuda.JGR.toolkit;

//JGR - Java Gui for R, see http://www.rosuda.org/JGR/
//Copyright (C) 2003 - 2005 Markus Helbig
//--- for licensing information see LICENSE file in the original JGR distribution ---


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;

/**
 * 
 * iFrame - every window in JGR is based on iFrame, iFrame takes care about the window menu and the same look and feel for every frame.
 * 
 * @author Markus Helbig
 * 
 * RoSuDA 2003 - 2005
 *
 */

public class iFrame extends JFrame {


    public static final int clsMain     = 1;
    public static final int clsSplash   = 2;
    public static final int clsHelp     = 16;
    public static final int clsPlot     = 128;
    public static final int clsTable    = 143;
    public static final int clsEditor   = 150;
    public static final int clsAbout    = 151;
    public static final int clsPrefs    = 152;
    public static final int clsObjBrowser = 153;
    public static final int clsPackageUtil = 154;
    public static final int clsJavaGD   = 160;
    public static final int clsUser     = 8192;

    private Dimension minimumSize;

    /**
     * WindowEntry, used for window-management.
     */
    public WTentrySwing MYEntry;

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
        this.getContentPane().setBackground(UIManager.getColor("Label.background"));
        this.addWindowListener(new org.rosuda.JGR.toolkit.WinListener());
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
        /* wenn er des oefteren haengen bleibt die naechste Zeile rausnehmen */
        //progress = new ProgressLabel(24);
    }

    public iFrame() {
        this("<unnamed>", 0);
    }

    /**
     * Get the windowmanager entry.
     * @return window-entry
     */
    public WTentry getMYEntry() {
        return MYEntry;
    }

    /**
     * When removing this object from screen, remove it from window-manager
     */
    public void finalize() {
        WinTracker.current.rm(MYEntry);
    }

    /**
     * Set minimum size of frames (only works on windows machines).
     * @param d dimension
     */
    public void setMinimumSize(Dimension d) {
        minimumSize = d;
    }

    /**
     * Show frame.
     */
    public void show() {
        this.setState(Frame.NORMAL);
        super.show();
    }

    static int lastClass=-1;
    static int lastPlaceX=0, lastPlaceY=0;
    static int lastOffset=0;

    /**
     * Place the windows not above each other, instead add offsets.
     */
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

    /**
     * While JGR is working show spinningwheel.
     * @param work true if working, false if idle.
     */
    public synchronized void setWorking(final boolean work) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (work) {
                    cursorWait();
                } else {
                    cursorDefault();
                }
            }
        });
    }

    /**
     * Show waitcursor (speeningwheel or sandglass).
     */
    public void cursorWait() {
        Component gp = getRootPane().getGlassPane();
        gp.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        gp.setVisible(true);
    }

    /**
     * Show default cursor.
     *
     */
    public void cursorDefault() {
        Component gp = getRootPane().getGlassPane();
        gp.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        gp.setVisible(false);
    }
}