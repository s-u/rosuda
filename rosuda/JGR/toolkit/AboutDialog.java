package org.rosuda.JGR.toolkit;

/**
 *  AboutDialog
 * 
 * 	show splashscreen with authors and copyright information
 * 
 *	@author Markus Helbig
 *  
 * 	RoSuDA 2003 - 2004 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.rosuda.ibase.*;

public class AboutDialog extends SplashScreen implements MouseListener {



    /** ScreenSize*/
    private Dimension screenSize = Common.getScreenRes();
    private Image splash;

    public AboutDialog() {
        this(null);
    }

    public AboutDialog(JFrame f) {
        this.addMouseListener(this);
        this.setVisible(true);
    }

    public void mouseClicked(MouseEvent e) {
        this.dispose();
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }


    /** create new AboutDialog */
    /*public AboutDialog() {
        this(null);
    }

    /** create new AboutDialog
     * @param f Parent JFrame */
    /*public AboutDialog(JFrame f) {
        super(f,"About JGR",false);
        try {
            splash = loadSplash(Preferences.SPLASH);
        }
        catch (Exception e) {
            if (Preferences.DEBUG>0) System.out.println("Missing Splashlogo: "+e.getMessage());
            new iError(e);
        }
        this.setBackground(Color.white);
        this.setResizable(false);
        this.setSize(new Dimension(300, 220));
        this.setLocation((screenSize.width-300)/2,(screenSize.height-220)/2);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                setVisible(false);
            }
        });
        this.show();
    }

    public void paint(Graphics g) {
        try {
            if (splash != null)
                g.drawImage(splash, 0, 10, splash.getWidth(this),splash.getHeight(this),this);
            g.setFont(new Font("Dialog", Font.BOLD, 26));
            g.drawString(Preferences.TITLE, 160,
                         80);
            g.setFont(new Font("Dialog", Font.BOLD, 16));
            g.drawString(Preferences.SUBTITLE, 140,
                         110);
            g.setFont(new Font("Dialog", 0, 14));
            g.drawString("Version: " + Preferences.VERSION,
                         150, 130);
            g.setFont(new Font("Dialog", 0, 12));
            //g.drawString("Authors: " + Preferences.AUTHORS,40, 175);
            g.setFont(new Font("Dialog", 0, 12));
            g.drawString("(c) " + Preferences.DEVELTIME +", "+ Preferences.INSTITUTION
                         , 10,
                         getSize().height - 10);
            g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
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
    } */
}