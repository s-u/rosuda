//
//  PrefsColorFrame.java (based on PreferencesFrame from Klimt)
//  Klimt
//
//  Created by Simon Urbanek on Wed Jun 11 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$

package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JColorChooser;

import org.rosuda.ibase.*;
import org.rosuda.util.*;

/** This class implements a preference dialog which allows to change default colors (in Common.xxxColor). If saved Common.color.xxx setting properties are used. Use {@link #showPrefsDialog()} to show this dialog.
*/
public class PrefsColorFrame extends Frame implements WindowListener, MouseListener, ActionListener, ItemListener {
    static final String M_SAVE = "Save";
    static final String M_APPLY = "Apply";
    static final String M_CLOSE = "Close";
    PrefCanvas pc;
    Choice cs;
    String[] schemes = {
        "KLIMT classic","#ffffc0","#ffffff","#80ff80",
        "Terra di Siena","#dfb860","#c0c0c0","#b46087",
        "Xtra red","#ffffe0","#c0c0c0","#ff0000",
        null
    };

    static PrefsColorFrame last=null;

    public static PrefsColorFrame showPrefsDialog() {
        if (last==null)
            last=new PrefsColorFrame();
        last.setVisible(true);
        return last;
    }

    public PrefsColorFrame() {
        super("Preferences");
        setLayout(new BorderLayout());
        add(pc=new PrefCanvas());
        pc.addMouseListener(this);
        
        final Panel pp=new Panel();
        pp.setLayout(new BorderLayout());
        
        final Panel p = new Panel();
        pp.add(p,BorderLayout.SOUTH);
        final Panel ppp = new Panel();
        pp.add(ppp);
        ppp.setLayout(new FlowLayout());
        ppp.add(new Label("Color scheme:"));
        ppp.add(cs=new Choice());
        cs.add("Custom ...");
        int i=0;
        while (schemes[i]!=null) {
            cs.add(schemes[i]);
            if (schemes[i+1].compareTo(Tools.color2hrgb(pc.c[0]))==0 &&
                schemes[i+2].compareTo(Tools.color2hrgb(pc.c[1]))==0 &&
                schemes[i+3].compareTo(Tools.color2hrgb(pc.c[2]))==0)
                cs.select(schemes[i]);
            i+=4;
        }
        cs.addItemListener(this);
        p.setLayout(new FlowLayout());
        Button b;
        p.add(b=new Button(M_SAVE)); b.addActionListener(this);
        p.add(b=new Button(M_APPLY)); b.addActionListener(this);
        p.add(b=new Button(M_CLOSE)); b.addActionListener(this);
        add(pp,BorderLayout.SOUTH);
        pack();
        addWindowListener(this);
    }

    class PrefCanvas extends Canvas {
        Color c[];
        PrefCanvas() {
            setSize(250,130);
            c=new Color[3];
            c[0]=Common.backgroundColor;
            c[1]=Common.objectsColor;
            c[2]=Common.selectColor;
        }

        public void paint(final Graphics g) {
            g.setFont(new Font("SansSerif",0,11));
            g.drawString("background color:",30,35);
            g.drawString("objects color:",30,65);
            g.drawString("highlighting color:",30,95);
            g.setColor(c[0]);
            g.fillRect(170,20,30,20);
            g.setColor(c[1]);
            g.fillRect(170,50,30,20);
            g.setColor(c[2]);
            g.fillRect(170,80,30,20);
            g.setColor(Color.black);
            g.drawRect(170,20,30,20);
            g.drawRect(170,50,30,20);
            g.drawRect(170,80,30,20);
        }
    }

    public void windowClosing(final WindowEvent e) {
        setVisible(false);
    }
    public void windowClosed(final WindowEvent e) {}
    public void windowOpened(final WindowEvent e) {}
    public void windowIconified(final WindowEvent e) {}
    public void windowDeiconified(final WindowEvent e) {}
    public void windowActivated(final WindowEvent e) {}
    public void windowDeactivated(final WindowEvent e) {}

    public void itemStateChanged(final ItemEvent e) {
        final String s=cs.getSelectedItem();
        int i=0;
        while (schemes[i]!=null) {
            if (schemes[i].equals(s)) {
                Color cl=Tools.hrgb2color(schemes[++i]);
                if (cl!=null) pc.c[0]=cl;
                cl=Tools.hrgb2color(schemes[++i]);
                if (cl!=null) pc.c[1]=cl;
                cl=Tools.hrgb2color(schemes[++i]);
                if (cl!=null) pc.c[2]=cl;
                pc.repaint();
                return;
            }
            i+=4;
        }
    }

    public void mouseClicked(final MouseEvent ev) {
        final int x=ev.getX();
        final int y=ev.getY();
        if (x>170 && x<200 && y>20 && y<100) {
            final int a=(y-15)/30;
            final Color cl;
            cl=JColorChooser.showDialog(Common.mainFrame,"Choose color",pc.c[a]);
            if (cl!=null) {
                cs.select("Custom ...");
                pc.c[a]=cl;
                pc.repaint();
            }
        }
    }
    public void mousePressed(final MouseEvent ev) {
    }
    public void mouseReleased(final MouseEvent e) {
    }
    public void mouseDragged(final MouseEvent e) {}
    public void mouseMoved(final MouseEvent ev) {}
    public void mouseEntered(final MouseEvent e) {}
    public void mouseExited(final MouseEvent e) {}

    public void actionPerformed(final ActionEvent e) {
        final String cmd=e.getActionCommand();
        if (M_CLOSE.equals(cmd)) {
            setVisible(false);
        }
        if (M_APPLY.equals(cmd) || M_SAVE.equals(cmd)) {
            Common.backgroundColor=pc.c[0];
            Common.objectsColor=pc.c[1];
            Common.selectColor=pc.c[2];
            PGSCanvas.getGlobalNotifier().NotifyAll(new NotifyMsg(this,Common.NM_PrefsChanged));
        }
        if (M_SAVE.equals(cmd)) {
            final GlobalConfig gc=GlobalConfig.getGlobalConfig();
            gc.setParS("Common.color.background",Tools.color2hrgb(Common.backgroundColor));
            gc.setParS("Common.color.objects",Tools.color2hrgb(Common.objectsColor));
            gc.setParS("Common.color.select",Tools.color2hrgb(Common.selectColor));
            gc.saveSettings();
            setVisible(false);
        }
    }
}
