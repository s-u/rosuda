package org.rosuda.JGR.toolkit;

//
//  SplashScreen.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import org.rosuda.ibase.*;
import org.rosuda.util.*;


public class SplashScreen extends JWindow implements Runnable {

    private Thread thread;
    private Dimension screenSize = Common.getScreenRes();
    private Dimension splashSize = new Dimension(300,200);
    private Image splash;

    /** SplashScreen
     */


    public SplashScreen() {
        try {
            splash = loadSplash(iPreferences.SPLASH);
        }
        catch (Exception e) {
            if (iPreferences.DEBUG>0) System.out.println("Missing Splashlogo: "+e.getMessage());
            new iError(e);
        }
        this.setSize(splashSize);
        this.setLocation((screenSize.width-300)/2,(screenSize.height-200)/2);
        this.setBackground(Color.white);
        thread = new Thread(this);
        //this.start();
    }

    public void paint(Graphics g) {
        try {
            if (splash != null)
                g.drawImage(splash, 0, 0, splash.getWidth(this),splash.getHeight(this),this);
            g.setFont(new Font("Dialog", Font.BOLD, 26));
            g.drawString(iPreferences.TITLE, 160,
                         80);
            g.setFont(new Font("Dialog", Font.BOLD, 16));
            g.drawString(iPreferences.SUBTITLE, 140,
                         110);
            g.setFont(new Font("Dialog", 0, 14));
            g.drawString("Version: " + iPreferences.VERSION,
                         150, 130);
            g.setFont(new Font("Dialog", 0, 12));
            g.drawString(iPreferences.WEBSITE,150, splashSize.height - 35);
            g.setFont(new Font("Dialog", 0, 12));
            g.drawString("(c) " + iPreferences.DEVELTIME+ ", " +iPreferences.INSTITUTION, 10,
                         splashSize.height - 10);
            g.drawRect(0, 0, splashSize.width - 1, splashSize.height - 1);
        }
        catch (Exception e) {
            g.setFont(new Font("Dialog", 0, 12));
            g.drawString("SplashScreen (something has gone wrong)",10,10);
            new iError(e);
        }
    }

    public Image loadSplash(String logo) {
        URL location = getClass().getResource("/"+logo);
        Image img = Toolkit.getDefaultToolkit().getImage(location);
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(img,0);
        try {
            mt.waitForAll();
        }
        catch (Exception e) {
            new iError(e);
        }
        return img;
    }

    public void start() {
        setVisible(true);
        thread.start();
    }

    public boolean isAlive() {
        return thread.isAlive();
    }

    public void run() {
        while (thread != null) {
            try {
            }
            catch (Exception e) {
                new iError(e);
            }
        }
    }

    public void stop() {
        setVisible(false);
        dispose();
        thread = null;
    }

    public static void main(String[] args) {
        new SplashScreen();
    }
}