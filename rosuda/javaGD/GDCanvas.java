package org.rosuda.javaGD;

import java.util.*;
import java.awt.*;
import java.lang.reflect.Method;

class GDObject {
    public void paint(GDCanvas c, Graphics g) {};
}

public class GDCanvas extends Canvas {
    Vector l;

    boolean listChanged;
    
    public static boolean forceAntiAliasing=true;
    
    Color fill;
    Color col;

    Font f;

    Refresher r;
    
    Dimension lastSize;

    public int devNr=-1;
    
    public GDCanvas(double w, double h) {
        this((int)w, (int)h);
    }
    
    public GDCanvas(int w, int h) {
        l=new Vector();
        f=new Font(null,0,12);
        setSize(w,h);
        lastSize=getSize();
        setBackground(Color.white);
        (r=new Refresher(this)).start();
    }

    public synchronized void cleanup() {
        r.active=false;
        r.interrupt();
        reset();
        r=null;
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
        g.setFont(f);
        g.setClip(0,0,d.width,d.height); // reset clipping rect
        while (i<j) {
            GDObject o=(GDObject) l.elementAt(i++);
            o.paint(this, g);
        }
        lastUpdate=System.currentTimeMillis();
    }
}

class GDLine extends GDObject {
    double x1,y1,x2,y2;
    public GDLine(double x1, double y1, double x2, double y2) {
        this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
    }

    public void paint(GDCanvas c, Graphics g) {
        if (c.col!=null)
            g.drawLine((int)(x1+0.5),(int)(y1+0.5),(int)(x2+0.5),(int)(y2+0.5));
    }
}

class GDRect extends GDObject {
    double x1,y1,x2,y2;
    public GDRect(double x1, double y1, double x2, double y2) {
        double tmp;
        if (x1>x2) { tmp=x1; x1=x2; x2=tmp; }
        if (y1>y2) { tmp=y1; y1=y2; y2=tmp; }
        this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
    }

    public void paint(GDCanvas c, Graphics g) {
        if (c.fill!=null) {
            g.setColor(c.fill);
            g.fillRect((int)(x1+0.5),(int)(y1+0.5),(int)(x2-x1+0.5),(int)(y2-y1+0.5));
            if (c.col!=null) g.setColor(c.col);
        }
        if (c.col!=null)
            g.drawRect((int)(x1+0.5),(int)(y1+0.5),(int)(x2-x1+0.5),(int)(y2-y1+0.5));
    }
}

class GDClip extends GDObject {
    double x1,y1,x2,y2;
    public GDClip(double x1, double y1, double x2, double y2) {
        double tmp;
        if (x1>x2) { tmp=x1; x1=x2; x2=tmp; }
        if (y1>y2) { tmp=y1; y1=y2; y2=tmp; }
        this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
    }

    public void paint(GDCanvas c, Graphics g) {
        g.setClip((int)(x1+0.5),(int)(y1+0.5),(int)(x2-x1+1.7),(int)(y2-y1+1.7));
    }
}

class GDCircle extends GDObject {
    double x,y,r;
    public GDCircle(double x, double y, double r) {
        this.x=x; this.y=y; this.r=r;
    }

    public void paint(GDCanvas c, Graphics g) {
        if (c.fill!=null) {
            g.setColor(c.fill);
            g.fillOval((int)(x-r+0.5),(int)(y-r+0.5),(int)(r+r+0.5),(int)(r+r+0.5));
            if (c.col!=null) g.setColor(c.col);
        }
        if (c.col!=null)
            g.drawOval((int)(x-r+0.5),(int)(y-r+0.5),(int)(r+r+0.5),(int)(r+r+0.5));
    }
}

class GDText extends GDObject {
    double x,y,r,h;
    String txt;
    public GDText(double x, double y, double r, double h, String txt) {
        this.x=x; this.y=y; this.r=r; this.h=h; this.txt=txt;
    }

    public void paint(GDCanvas c, Graphics g) {
        if (c.col!=null) {
            double rx=x, ry=y;
            double hc=0d;
            if (h!=0d) {
                FontMetrics fm=g.getFontMetrics();
                int w=fm.stringWidth(txt);
                hc=((double)w)*h;
                rx=x-(((double)w)*h);
            }
            int ix=(int)(rx+0.5), iy=(int)(ry+0.5);
                       
            if (r!=0d) {
                Graphics2D g2d=(Graphics2D) g;
                g2d.translate(x,y);
                double rr=-r/180d*Math.PI;
                g2d.rotate(rr);
                if (hc!=0d)
                    g2d.translate(-hc,0d);
                g2d.drawString(txt,0,0);
                if (hc!=0d)
                    g2d.translate(hc,0d);
                g2d.rotate(-rr);
                g2d.translate(-x,-y);
            } else
                g.drawString(txt,ix,iy);
        }
    }
}


class GDFont extends GDObject {
    double cex,ps,lineheight;
    int face;
    String family;

    Font font;
    
    public GDFont(double cex, double ps, double lineheight, int face, String family) {
        //System.out.println(">> FONT(cex="+cex+",ps="+ps+",lh="+lineheight+",face="+face+",\""+family+"\")");
        this.cex=cex; this.ps=ps; this.lineheight=lineheight; this.face=face; this.family=family;
        int jFT=Font.PLAIN;
        if (face==2) jFT=Font.BOLD;
        if (face==3) jFT=Font.ITALIC;
        if (face==4) jFT=Font.BOLD|Font.ITALIC;
        font=new Font(family.equals("")?null:family, jFT, (int)(cex*ps+0.5));
    }

    public void paint(GDCanvas c, Graphics g) {
        g.setFont(font);
        c.f=font;
    }
}
        
class GDPolygon extends GDObject {
    int n;
    double x[],y[];
    int xi[], yi[];
    boolean isPolyline;
    public GDPolygon(int n, double[] x, double[] y, boolean isPolyline) {
        this.x=x; this.y=y; this.n=n; this.isPolyline=isPolyline;
        int i=0;
        xi=new int[n]; yi=new int[n];
        while (i<n) {
            xi[i]=(int)(x[i]+0.5);
            yi[i]=(int)(y[i]+0.5);
            i++;
        }
    }

    public void paint(GDCanvas c, Graphics g) {
        if (c.fill!=null && !isPolyline) {
            g.setColor(c.fill);
            g.fillPolygon(xi, yi, n);
            if (c.col!=null) g.setColor(c.col);
        }
        if (c.col!=null) {
            if (isPolyline)
                g.drawPolyline(xi, yi, n);
            else
                g.drawPolygon(xi, yi, n);
        }
    }
}

class GDColor extends GDObject {
    int col;
    Color gc;
    public GDColor(int col) {
        this.col=col;
        //System.out.println(">> COLOR: "+Integer.toString(col,16));
        if (col==-1 || col==0x80000000) gc=null;
        else
            gc=new Color(((float)(col&255))/255f,
                         ((float)((col>>8)&255))/255f,
                         ((float)((col>>16)&255))/255f,
                         1f-((float)((col>>24)&255))/255f);
        //System.out.println("          "+gc);
    }

    public void paint(GDCanvas c, Graphics g) {
        c.col=gc;
        if (gc!=null) g.setColor(gc);
    }
}

class GDFill extends GDObject {
    int col;
    Color gc;
    public GDFill(int col) {
        this.col=col;
        //System.out.println(">> FILL COLOR: "+Integer.toString(col,16));
        if (col==-1 || col==0x80000000)
            gc=null;
        else
            gc=new Color(((float)(col&255))/255f,
                         ((float)((col>>8)&255))/255f,
                         ((float)((col>>16)&255))/255f,
                         1f-((float)((col>>24)&255))/255f);
        //System.out.println("          "+gc);
    }
    
    public void paint(GDCanvas c, Graphics g) {
        c.fill=gc;
    }
}

class GDLinePar extends GDObject {
    double lwd;
    int lty;
    BasicStroke bs;

    public GDLinePar(double lwd, int lty) {
        this.lwd=lwd; this.lty=lty;
        //System.out.println(">> LINE TYPE: width="+lwd+", type="+Integer.toString(lty,16));
        bs=null;
        if (lty==0)
            bs=new BasicStroke((float)lwd);
        else if (lty==-1)
            bs=new BasicStroke(0f);
        else {
            int l=0;
            int dt=lty;
            while (dt>0) {
                dt>>=4;
                l++;
            }
            float[] dash=new float[l];
            dt=lty;
            l=0;
            while (dt>0) {
                int rl=dt&15;
                dash[l++]=(float)rl;
                dt>>=4;
            }
            bs=new BasicStroke((float)lwd, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 3f, dash, 0f);
        }
    }

    public void paint(GDCanvas c, Graphics g) {
        if (bs!=null)
            ((Graphics2D)g).setStroke(bs);
    }
}
