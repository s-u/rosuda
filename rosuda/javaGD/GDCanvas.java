package org.rosuda.javaGD;

import java.util.*;
import java.awt.*;

class GDObject {
    public void paint(GDCanvas c, Graphics g) {};
}

class GDCanvas extends Canvas {
    Vector l;

    Color fill;
    Color col;

    Font f;
    
    public GDCanvas(double w, double h) {
        this((int)w, (int)h);
    }
    
    public GDCanvas(int w, int h) {
        l=new Vector();
        f=new Font(null,0,12);
        setSize(w,h);
    }

    public void add(GDObject o) { l.add(o); }
    public void reset() { l.removeAllElements(); }
    public void paint(Graphics g) {
        int i=0, j=l.size();
        g.setFont(f);
        while (i<j) {
            GDObject o=(GDObject) l.elementAt(i++);
            o.paint(this, g);
        }
    }
}

class GDLine extends GDObject {
    double x1,y1,x2,y2;
    public GDLine(double x1, double y1, double x2, double y2) {
        System.out.println(">> LINE("+x1+","+y1+","+x2+","+y2+")");
        this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
    }

    public void paint(GDCanvas c, Graphics g) {
        if (c.col!=null)
            g.drawLine((int)x1,(int)y1,(int)x2,(int)y2);
    }
}

class GDRect extends GDObject {
    double x1,y1,x2,y2;
    public GDRect(double x1, double y1, double x2, double y2) {
        double tmp;
        if (x1>x2) { tmp=x1; x1=x2; x2=tmp; }
        if (y1>y2) { tmp=y1; y1=y2; y2=tmp; }
        System.out.println(">> RECT("+x1+","+y1+","+x2+","+y2+")");
        this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
    }

    public void paint(GDCanvas c, Graphics g) {
        if (c.fill!=null) {
            g.setColor(c.fill);
            g.fillRect((int)x1,(int)y1,(int)(x2-x1),(int)(y2-y1));
            if (c.col!=null) g.setColor(c.col);
        }
        if (c.col!=null)
            g.drawRect((int)x1,(int)y1,(int)(x2-x1),(int)(y2-y1));
    }
}

class GDCircle extends GDObject {
    double x,y,r;
    public GDCircle(double x, double y, double r) {
        System.out.println(">> CIRCLE("+x+","+y+","+r+")");
        this.x=x; this.y=y; this.r=r;
    }

    public void paint(GDCanvas c, Graphics g) {
        if (c.fill!=null) {
            g.setColor(c.fill);
            g.fillOval((int)(x-r),(int)(y-r),(int)(r+r),(int)(r+r));
            if (c.col!=null) g.setColor(c.col);
        }
        if (c.col!=null)
            g.drawOval((int)(x-r),(int)(y-r),(int)(r+r),(int)(r+r));
    }
}

class GDText extends GDObject {
    double x,y,r,h;
    String txt;
    public GDText(double x, double y, double r, double h, String txt) {
        System.out.println(">> TEXT("+x+","+y+","+r+","+h+",\""+txt+"\")");
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
                double rr=r/180d*Math.PI;
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
        System.out.println(">> FONT("+cex+","+ps+","+lineheight+","+face+",\""+family+"\")");
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
        System.out.println(">> POLYGON("+n+" points, type="+(isPolyline?"PolyLine":"Polygon")+")");
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
        System.out.println(">> COLOR: "+Integer.toString(col,16));
        if (col==-1) gc=null;
        else
            gc=new Color(((float)(col&255))/255f,
                         ((float)((col>>8)&255))/255f,
                         ((float)((col>>16)&255))/255f,
                         1f-((float)((col>>24)&255))/255f);
        System.out.println("          "+gc);
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
        System.out.println(">> FILL COLOR: "+Integer.toString(col,16));
        if (col==-1)
            gc=null;
        else
            gc=new Color(((float)(col&255))/255f,
                         ((float)((col>>8)&255))/255f,
                         ((float)((col>>16)&255))/255f,
                         1f-((float)((col>>24)&255))/255f);
        System.out.println("          "+gc);
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
        System.out.println(">> LINE TYPE: width="+lwd+", type="+Integer.toString(lty,16));
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
