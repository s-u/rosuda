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
import javax.swing.JFrame;

public class PreferencesFrame extends Frame implements WindowListener, MouseListener, ActionListener, ItemListener {
  PrefCanvas pc;
  Join frame;
  Choice cs;
  static String[] schemes = {
    "RoSuDa classic","#ffff99","#c0c0c0","#00ff00",
    "Terra di Siena","#dfb860","#c0c0c0","#b46087",
    "Xtra red","#ffff99","#c0c0c0","#ff0000",
    null
  };

  static PreferencesFrame last=null;

  public static PreferencesFrame showPrefsDialog(Join frame) {
    if (last==null)
      last=new PreferencesFrame();
    last.frame = frame;
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
      if (schemes[i+1].compareTo(Util.color2hrgb(pc.c[0]))==0 &&
          schemes[i+2].compareTo(Util.color2hrgb(pc.c[1]))==0 &&
          schemes[i+3].compareTo(Util.color2hrgb(pc.c[2]))==0)
        cs.select(schemes[i]);
      i+=4;
    }
    cs.addItemListener(this);
    p.setLayout(new FlowLayout());
    Button b=null;
    p.add(b=new Button("Save")); b.addActionListener(this);
    b.setEnabled(false);
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
      c[0]=MFrame.backgroundColor;
      c[1]=Color.lightGray;
      c[2]=DragBox.hiliteColor;
    }

    public void paint(Graphics g) {
      g.setFont(new Font("SansSerif",0,11));
      g.drawString("background color:",30,35);
      g.setColor(Color.gray);
      g.drawString("objects color:",30,65);
      g.setColor(Color.black);
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
        Color cl=Util.hrgb2color(schemes[++i]);
        if (cl!=null) pc.c[0]=cl;
        cl=Util.hrgb2color(schemes[++i]);
        if (cl!=null) pc.c[1]=cl;
        cl=Util.hrgb2color(schemes[++i]);
        if (cl!=null) pc.c[2]=cl;
        pc.repaint();
        return;
      }
      i+=4;
    }
  }

  static void setScheme(int dragan) {
    int i=dragan*4;
    Color cl=Util.hrgb2color(schemes[++i]);
    if (cl!=null)
      MFrame.backgroundColor=cl;
    cl=Util.hrgb2color(schemes[++i]);
    //    if (cl!=null)
    //      pc.c[1]=cl;
    cl=Util.hrgb2color(schemes[++i]);
    if (cl!=null)
      DragBox.hiliteColor=cl;
  }	
  
  
  public void mouseClicked(MouseEvent ev) {
    int x=ev.getX(), y=ev.getY();
    if (x>170 && x<200 && y>20 && y<100) {
      int a=(y-15)/30;
      if( a == 1 )
        return;
      Color cl=null;
      cl=JColorChooser.showDialog(this,"Choose color",pc.c[a]);
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
      MFrame.backgroundColor=pc.c[0];
      //            Common.objectsColor=pc.c[1];
      DragBox.hiliteColor=pc.c[2];
      frame.updateSelection();
    }
    /*        if (cmd=="Save") {
      PluginManager pm=PluginManager.getManager();
    pm.setParS("Common","color.background",Util.color2hrgb(Common.backgroundColor));
    pm.setParS("Common","color.objects",Util.color2hrgb(Common.objectsColor));
    pm.setParS("Common","color.select",Util.color2hrgb(Common.selectColor));
    pm.saveSettings();
    setVisible(false);
    }*/
  }
}
