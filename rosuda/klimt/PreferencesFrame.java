//
//  PreferencesFrame.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jun 11 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$

package org.rosuda.klimt;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JColorChooser;

import org.rosuda.ibase.*;
import org.rosuda.ibase.toolkit.*;
import org.rosuda.util.*;

public class PreferencesFrame extends Frame implements WindowListener, MouseListener, ActionListener, ItemListener {
    PrefCanvas pc;
    Choice cs;
    String[] schemes = {
        "KLIMT classic","#ffffc0","#ffffff","#80ff80",
        "Terra di Siena","#dfb860","#c0c0c0","#b46087",
        "Xtra red","#ffffe0","#c0c0c0","#ff0000",
        null
    };

    static PreferencesFrame last=null;
    
    public static PreferencesFrame showPrefsDialog() {
        if (last==null)
            last=new PreferencesFrame();
        last.setVisible(true);
        return last;
    }
    
    public PreferencesFrame() {
        super("Preferences");
        setLayout(new BorderLayout());
        add(pc=new PrefCanvas());
        pc.addMouseListener(this);
        Panel p=new Panel();
        Panel pp=new Panel();
        pp.setLayout(new BorderLayout());
        Panel ppp=new Panel();
        pp.add(p,BorderLayout.SOUTH);
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
        Button b=null;
        p.add(b=new Button("Save")); b.addActionListener(this);
        p.add(b=new Button("Apply")); b.addActionListener(this);
        p.add(b=new Button("Close")); b.addActionListener(this);
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

        public void paint(Graphics g) {
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

    public void windowClosing(WindowEvent e) {
        setVisible(false);
    }
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    public void itemStateChanged(ItemEvent e) {
        String s=cs.getSelectedItem();
        int i=0;
        while (schemes[i]!=null) {
            if (schemes[i]==s) {
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
        
    public void mouseClicked(MouseEvent ev) {
        int x=ev.getX(), y=ev.getY();
        if (x>170 && x<200 && y>20 && y<100) {
            int a=(y-15)/30;
            Color cl=null;
            cl=JColorChooser.showDialog(Common.mainFrame,"Choose color",pc.c[a]);
            if (cl!=null) {
                cs.select("Custom ...");
                pc.c[a]=cl;
                pc.repaint();
            }
        }            
    }
    public void mousePressed(MouseEvent ev) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent ev) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    
    public void actionPerformed(ActionEvent e) {
        String cmd=e.getActionCommand();
        if (cmd=="Close") {
            setVisible(false);
        }
        if (cmd=="Apply" || cmd=="Save") {
            Common.backgroundColor=pc.c[0];
            Common.objectsColor=pc.c[1];
            Common.selectColor=pc.c[2];
            PGSCanvas.getGlobalNotifier().NotifyAll(new NotifyMsg(this,Common.NM_PrefsChanged));            
        }
        if (cmd=="Save") {
            GlobalConfig gc=GlobalConfig.getGlobalConfig();
            gc.setParS("Common.color.background",Tools.color2hrgb(Common.backgroundColor));
            gc.setParS("Common.color.objects",Tools.color2hrgb(Common.objectsColor));
            gc.setParS("Common.color.select",Tools.color2hrgb(Common.selectColor));
            gc.saveSettings();
            setVisible(false);
        }
    }
}
