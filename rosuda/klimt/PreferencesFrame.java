//
//  PreferencesFrame.java
//  Klimt
//
//  Created by Simon Urbanek on Wed Jun 11 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import javax.swing.JColorChooser;

public class PreferencesFrame extends Frame implements WindowListener, MouseListener, ActionListener {
    PrefCanvas pc;
    
    public PreferencesFrame() {
        super("Preferences");
        setLayout(new BorderLayout());
        add(pc=new PrefCanvas());
        pc.addMouseListener(this);
        Panel p=new Panel();
        p.setLayout(new FlowLayout());
        Button b=null;
        p.add(b=new Button("Save")); b.addActionListener(this);
        p.add(b=new Button("Apply")); b.addActionListener(this);
        p.add(b=new Button("Close")); b.addActionListener(this);
        add(p,BorderLayout.SOUTH);
        pack();
        addWindowListener(this);
    }

    class PrefCanvas extends Canvas {
        Color c[];
        PrefCanvas() {
            setSize(300,200);
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
        pc=null;
        dispose();
    }
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    public void handlePrefs() {
        Color sc=JColorChooser.showDialog(Common.mainFrame,"Choose selection color",Common.selectColor);
        if (sc!=null) {
            Common.selectColor=sc;
            PGSCanvas.getGlobalNotifier().NotifyAll(new NotifyMsg(this,Common.NM_PrefsChanged));
        }
    }

    public void mouseClicked(MouseEvent ev) {
        int x=ev.getX(), y=ev.getY();
        if (x>170 && x<200 && y>20 && y<100) {
            int a=(y-15)/30;
            Color cl=null;
            cl=JColorChooser.showDialog(Common.mainFrame,"Choose color",pc.c[a]);
            if (cl!=null) {
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
            pc=null;
            dispose();
        }
        if (cmd=="Apply" || cmd=="Save") {
            Common.backgroundColor=pc.c[0];
            Common.objectsColor=pc.c[1];
            Common.selectColor=pc.c[2];
            PGSCanvas.getGlobalNotifier().NotifyAll(new NotifyMsg(this,Common.NM_PrefsChanged));            
        }
        if (cmd=="Save") {
            setVisible(false);
            pc=null;
            dispose();
        }
    }
}
