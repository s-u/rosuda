package org.rosuda.JGR.toolkit;

/**
*  SplashScreen
 *
 * @author Markus Helbig
 *
 * RoSuDA 2003 - 2004
 */

import java.awt.*;
import java.net.*;
import javax.swing.*;

import org.rosuda.ibase.*;

import org.rosuda.JGR.JGR;
import org.rosuda.JGR.util.*;

public class SplashScreen extends JWindow implements Runnable {

    private Thread thread;
    private Dimension screenSize = Common.getScreenRes();
    private Dimension splashSize = new Dimension(300,200);
    private Image splash;

    /** SplashScreen
     *
     */


    public SplashScreen() {
        try {
            splash = loadSplash(JGR.SPLASH);
        }
        catch (Exception e) {
            if (JGRPrefs.DEBUG>0) System.out.println("Missing Splashlogo: "+e.getMessage());
        }
        this.setSize(splashSize);
        this.setLocation((screenSize.width-300)/2,(screenSize.height-200)/2);
        this.setBackground(Color.white);
        thread = new Thread(this);
    }

    public void paint(Graphics g) {
        try {
            if (splash != null)
                g.drawImage(splash, 0, 0, splash.getWidth(this),splash.getHeight(this),this);
            g.setFont(new Font("Dialog", Font.BOLD, 26));
            g.drawString(JGR.TITLE, 175,
                         40);
            g.setFont(new Font("Dialog", Font.BOLD, 16));
            g.drawString(JGR.SUBTITLE, 150,
                         70);
            g.setFont(new Font("Dialog", 0, 11));
            g.drawString("Version: " + JGR.VERSION,
                         175, 85);
            g.setFont(new Font("Dialog", Font.ITALIC, 13));
            g.drawString("" + JGR.AUTHOR1,
                         163, 119);
            g.drawString("" + JGR.AUTHOR2,
                         160, 135);
            g.setFont(new Font("Dialog", 0, 12));
            g.setColor(Color.blue);
            g.drawString(JGR.WEBSITE,150, splashSize.height - 35);
            g.setColor(Color.black);
            g.setFont(new Font("Dialog", 0, 12));
            g.drawString("(c) " + JGR.DEVELTIME+ ", " +JGR.INSTITUTION, 10,
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