package org.rosuda.JGR.toolkit;

//
//  SplashScreen.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.net.*;
import javax.swing.*;

import org.rosuda.ibase.*;

import org.rosuda.JGR.util.*;

public class SplashScreen extends JWindow implements Runnable {

    private Thread thread;
    private Dimension screenSize = Common.getScreenRes();
    private Dimension splashSize = new Dimension(300,200);
    private Image splash;

    /** SplashScreen
     */


    public SplashScreen() {
        try {
            splash = loadSplash(JGRPrefs.SPLASH);
        }
        catch (Exception e) {
            if (JGRPrefs.DEBUG>0) System.out.println("Missing Splashlogo: "+e.getMessage());
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
            g.drawString(JGRPrefs.TITLE, 170,
                         40);
            g.setFont(new Font("Dialog", Font.BOLD, 16));
            g.drawString(JGRPrefs.SUBTITLE, 150,
                         70);
            g.setFont(new Font("Dialog", 0, 11));
            g.drawString("Version: " + JGRPrefs.VERSION,
                         175, 85);
            g.setFont(new Font("Dialog", Font.ITALIC, 13));
            g.drawString("" + JGRPrefs.AUTHOR1,
                         163, 119);
            g.drawString("" + JGRPrefs.AUTHOR2,
                         160, 135);
            g.setFont(new Font("Dialog", 0, 12));
            g.setColor(Color.blue);
            g.drawString(JGRPrefs.WEBSITE,150, splashSize.height - 35);
            g.setColor(Color.black);
            g.setFont(new Font("Dialog", 0, 12));
            g.drawString("(c) " + JGRPrefs.DEVELTIME+ ", " +JGRPrefs.INSTITUTION, 10,
                         splashSize.height - 10);
            g.drawRect(0, 0, splashSize.width - 1, splashSize.height - 1);
        }
        catch (Exception e) {
            g.setFont(new Font("Dialog", 0, 12));
            g.drawString("SplashScreen (something has gone wrong)",10,10);
            new ErrorMsg(e);
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
            new ErrorMsg(e);
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
                new ErrorMsg(e);
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