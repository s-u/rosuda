package org.rosuda.javaGD;

import java.util.*;
import java.awt.*;
import java.lang.reflect.Method;

public class GDCanvas extends Canvas implements GDContainer {
    Vector l;

    boolean listChanged;
    
    public static boolean forceAntiAliasing=true;

    GDState gs;
    
    Refresher r;
    
    Dimension lastSize;

    public int devNr=-1;
    
    public GDCanvas(double w, double h) {
        this((int)w, (int)h);
    }
    
    public GDCanvas(int w, int h) {
        l=new Vector();
        gs=new GDState();
        gs.f=new Font(null,0,12);
        setSize(w,h);
        lastSize=getSize();
        setBackground(Color.white);
        (r=new Refresher(this)).start();
    }

    public GDState getGState() { return gs; }

    public void setDeviceNumber(int dn) { devNr=dn; }
    public int getDeviceNumber() { return devNr; }
    public void closeDisplay() {}
    
    public synchronized void cleanup() {
        r.active=false;
        r.interrupt();
        reset();
        r=null;
        l=null;
    }

    public void syncDisplay(boolean finish) {
        repaint();
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

    public synchronized Vector getGDOList() { return l; }

    long lastUpdate;
    long lastUpdateFinished;
    boolean updatePending=false;
    
    public void update(Graphics g) {
        if (System.currentTimeMillis()-lastUpdate<200) {
            updatePending=true;
            if (System.currentTimeMillis()-lastUpdateFinished>700) {
                g.setColor(Color.white);
                g.fillRect(0,0,250,25);
                g.setColor(Color.blue);
                g.drawString("Building plot... ("+l.size()+" objects)",10,10);
                lastUpdateFinished=System.currentTimeMillis();
            }
            lastUpdate=System.currentTimeMillis();
            return;
        }
        updatePending=false;
        super.update(g);
        lastUpdateFinished=lastUpdate=System.currentTimeMillis();
    }

    class Refresher extends Thread {
        GDCanvas c;
        boolean active;

        public Refresher(GDCanvas c) {
            this.c=c;
        }

        public void run() {
            active=true;
            while (active) {
                try {
                    Thread.sleep(300);
                } catch (Exception e) {}
                if (!active) break;
                if (c.updatePending && (System.currentTimeMillis()-lastUpdate>200)) {
                    c.repaint();
                }
            }
            c=null;
        }
    }
    
    public synchronized void paint(Graphics g) {
        updatePending=false;
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
        while (i<j) {
            GDObject o=(GDObject) l.elementAt(i++);
            o.paint(this, gs, g);
        }
        lastUpdate=System.currentTimeMillis();
    }
}
