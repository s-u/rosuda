//
//  JGDPanel.java
//  JGR
//
//  Created by Simon Urbanek on Thu Aug 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//

package org.rosuda.javaGD;

import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import java.lang.reflect.Method;

public class JGDPanel extends JPanel implements GDContainer {
    Vector l;
    boolean listChanged;
    public static boolean forceAntiAliasing=true;
    GDState gs;
    Dimension lastSize;
    public int devNr=-1;
    Dimension prefSize;

    public JGDPanel(double w, double h) {
        this((int)w, (int)h);
    }

    public JGDPanel(int w, int h) {
        super(true);
        setOpaque(true);
        setSize(w, h);
        prefSize=new Dimension(w,h);
        l=new Vector();
        gs=new GDState();
        gs.f=new Font(null,0,12);
        setSize(w,h);
        lastSize=getSize();
        setBackground(Color.white);
    }

    public GDState getGState() { return gs; }

    public void setDeviceNumber(int dn) { devNr=dn; }
    public int getDeviceNumber() { return devNr; }

    public synchronized void cleanup() {
        reset();
        l=null;
    }

    public void initRefresh() {
        //System.out.println("resize requested");
        try { // for now we use no cache - just pure reflection API for: Rengine.getMainEngine().eval("...")
            Class c=Class.forName("org.rosuda.JRI.Rengine");
            if (c==null)
                System.out.println(">> can't find Rengine, automatic resizing disabled. [c=null]");
            else {
                Method m=c.getMethod("getMainEngine",null);
                Object o=m.invoke(null,null);
                if (o!=null) {
                    Class[] par=new Class[1];
                    par[0]=Class.forName("java.lang.String");
                    m=c.getMethod("eval",par);
                    Object[] pars=new Object[1];
                    pars[0]="try(.C(\"javaGDresize\",as.integer("+devNr+")),silent=TRUE)";
                    m.invoke(o, pars);
                }
            }
        } catch (Exception e) {
            System.out.println(">> can't find Rengine, automatic resizing disabled. [x:"+e.getMessage()+"]");
        }
    }

    public synchronized void add(GDObject o) {
        l.add(o);
        listChanged=true;
    }

    public synchronized void reset() {
        l.removeAllElements();
        listChanged=true;
    }

    public Dimension getPreferredSize() {
        return new Dimension(prefSize);
    }
    
    public synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension d=getSize();
        if (!d.equals(lastSize)) {
            initRefresh();
            lastSize=d;
            return;
        }

        if (forceAntiAliasing) {
            Graphics2D g2=(Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        int i=0, j=l.size();
        g.setFont(gs.f);
        g.setClip(0,0,d.width,d.height); // reset clipping rect
        g.setColor(Color.white);
        g.fillRect(0,0,d.width,d.height);
        while (i<j) {
            GDObject o=(GDObject) l.elementAt(i++);
            o.paint(this, gs, g);
        }
    }
    
}
