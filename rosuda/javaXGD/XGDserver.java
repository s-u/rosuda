package org.rosuda.javaXGD;

import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;

class XGDobject {
    public void paint(XGDcanvas c, Graphics g) {};
}

class XGDcanvas extends Canvas {
    Vector l;

    Color fill;
    Color col;

    Font f;
    
    public XGDcanvas(int w, int h) {
        l=new Vector();
        f=new Font(null,0,12);
        setSize(w,h);
    }

    public void add(XGDobject o) { l.add(o); }
    public void reset() { l.removeAllElements(); }
    public void paint(Graphics g) {
        int i=0, j=l.size();
        g.setFont(f);
        while (i<j) {
            XGDobject o=(XGDobject) l.elementAt(i++);
            o.paint(this, g);
        }
    }
}

class XGDline extends XGDobject {
    double x1,y1,x2,y2;
    public XGDline(double x1, double y1, double x2, double y2) {
        System.out.println(">> LINE("+x1+","+y1+","+x2+","+y2+")");
        this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
    }

    public void paint(XGDcanvas c, Graphics g) {
        if (c.col!=null)
            g.drawLine((int)x1,(int)y1,(int)x2,(int)y2);
    }
}

class XGDrect extends XGDobject {
    double x1,y1,x2,y2;
    public XGDrect(double x1, double y1, double x2, double y2) {
        double tmp;
        if (x1>x2) { tmp=x1; x1=x2; x2=tmp; }
        if (y1>y2) { tmp=y1; y1=y2; y2=tmp; }
        System.out.println(">> RECT("+x1+","+y1+","+x2+","+y2+")");
        this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
    }

    public void paint(XGDcanvas c, Graphics g) {
        if (c.fill!=null) {
            g.setColor(c.fill);
            g.fillRect((int)x1,(int)y1,(int)(x2-x1),(int)(y2-y1));
            if (c.col!=null) g.setColor(c.col);
        }
        if (c.col!=null)
            g.drawRect((int)x1,(int)y1,(int)(x2-x1),(int)(y2-y1));
    }
}

class XGDcircle extends XGDobject {
    double x,y,r;
    public XGDcircle(double x, double y, double r) {
        System.out.println(">> CIRCLE("+x+","+y+","+r+")");
        this.x=x; this.y=y; this.r=r;
    }

    public void paint(XGDcanvas c, Graphics g) {
        if (c.fill!=null) {
            g.setColor(c.fill);
            g.fillOval((int)(x-r),(int)(y-r),(int)(r+r),(int)(r+r));
            if (c.col!=null) g.setColor(c.col);
        }
        if (c.col!=null)
            g.drawOval((int)(x-r),(int)(y-r),(int)(r+r),(int)(r+r));
    }
}

class XGDtext extends XGDobject {
    double x,y,r,h;
    String txt;
    public XGDtext(double x, double y, double r, double h, String txt) {
        System.out.println(">> TEXT("+x+","+y+","+r+","+h+",\""+txt+"\")");
        this.x=x; this.y=y; this.r=r; this.h=h; this.txt=txt;
    }

    public void paint(XGDcanvas c, Graphics g) {
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


class XGDfont extends XGDobject {
    double cex,ps,lineheight;
    int face;
    String family;

    Font font;
    
    public XGDfont(double cex, double ps, double lineheight, int face, String family) {
        System.out.println(">> FONT("+cex+","+ps+","+lineheight+","+face+",\""+family+"\")");
        this.cex=cex; this.ps=ps; this.lineheight=lineheight; this.face=face; this.family=family;
        int jFT=Font.PLAIN;
        if (face==2) jFT=Font.BOLD;
        if (face==3) jFT=Font.ITALIC;
        if (face==4) jFT=Font.BOLD|Font.ITALIC;
        font=new Font(family.equals("")?null:family, jFT, (int)(cex*ps+0.5));
    }

    public void paint(XGDcanvas c, Graphics g) {
        g.setFont(font);
        c.f=font;
    }
}
        
class XGDpolygon extends XGDobject {
    int n;
    double x[],y[];
    int xi[], yi[];
    boolean isPolyline;
    public XGDpolygon(int n, double[] x, double[] y, boolean isPolyline) {
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

    public void paint(XGDcanvas c, Graphics g) {
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

class XGDcolor extends XGDobject {
    int col;
    Color gc;
    public XGDcolor(int col) {
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

    public void paint(XGDcanvas c, Graphics g) {
        c.col=gc;
        if (gc!=null) g.setColor(gc);
    }
}

class XGDfill extends XGDobject {
    int col;
    Color gc;
    public XGDfill(int col) {
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
    
    public void paint(XGDcanvas c, Graphics g) {
        c.fill=gc;
    }
}

class XGDlinePar extends XGDobject {
    double lwd;
    int lty;
    BasicStroke bs;

    public XGDlinePar(double lwd, int lty) {
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

    public void paint(XGDcanvas c, Graphics g) {
        if (bs!=null)
            ((Graphics2D)g).setStroke(bs);
    }
}

public class XGDserver extends Thread {
    class XGDworker extends Thread {
        public Socket s;
        boolean isBE;
        XGDcanvas c;
        Frame f;

        int getInt(byte[] b, int o) {
            return  (isBE)?
            ((b[o+3]&255)|((b[o+2]&255)<<8)|((b[o+1]&255)<<16)|((b[o]&255)<<24))
            :
            ((b[o]&255)|((b[o+1]&255)<<8)|((b[o+2]&255)<<16)|((b[o+3]&255)<<24));
        }

        long getLong(byte[] b, int offset) {
            long l1, l2;
            if (isBE) {
                l1=((long)getInt(b,offset+4))&0xffffffffL;
                l2=((long)getInt(b,offset))&0xffffffffL;
            } else {
                l1=((long)getInt(b,offset))&0xffffffffL;
                l2=((long)getInt(b,offset+4))&0xffffffffL;
            }
            return l1|(l2<<32);
        }

        double getDouble(byte[] b, int offset) {
            return Double.longBitsToDouble(getLong(b, offset));
        }

        void setInt(int v, byte[] buf, int o) {
            if (!isBE) {
                buf[o]=(byte)(v&255); o++;
                buf[o]=(byte)((v&0xff00)>>8); o++;
                buf[o]=(byte)((v&0xff0000)>>16); o++;
                buf[o]=(byte)((v&0xff000000)>>24);
            } else {
                buf[o+3]=(byte)(v&255); 
                buf[o+2]=(byte)((v&0xff00)>>8);
                buf[o+1]=(byte)((v&0xff0000)>>16);
                buf[o]=(byte)((v&0xff000000)>>24);
            }
        }

        void setLong(long l, byte[] buf, int o) {
            setInt((int)(l&0xffffffffL),buf,isBE?o+4:o);
            setInt((int)(l>>32),buf,isBE?o:o+4);
        }

        void setDouble(double d, byte[] buf, int o) {
            setLong(Double.doubleToLongBits(d),buf,o);
        }
        
        void dump(String s, byte[] b) {
            System.out.print(s);
            int i=0;
            while(i<b.length) {
                System.out.print(Integer.toString((int)b[i],16)+" ");
                i++;
            }
            System.out.println("");
        }
        
        public void run() {
            try {
                s.setTcpNoDelay(true); // send packets immediately (important, because R is waiting for the response)
                //s.setSoTimeout(1);
                System.out.println("XGDworker started with socket "+s);
                InputStream sis = s.getInputStream();
                OutputStream sos = s.getOutputStream();
                byte[] id = new byte[16];
                int n=sis.read(id);
                if (n!=16) {
                    System.out.println("Required 16 bytes, but got "+n+". Invalid protocol.");
                    s.close();
                    return;
                }
                if (id[0]==0x58 && id[1]==0x47 && id[2]==0x44) {
                    System.out.println("Connected to XGD version "+(id[3]-48)+" on PPC-endian machine");
                    isBE=true;
                } else if (id[3]==0x58 && id[2]==0x47 && id[1]==0x44) {
                    System.out.println("Connected to XGD version "+(id[0]-48)+" on Intel-endian machine");
                    isBE=false;
                } else {
                    System.out.println("Unknown protocol, bailing out");
                    s.close();
                    return;
                }

                byte[] hdr = new byte[4];
                while (true) {
                    n=sis.read(hdr);
                    if (n<4) {
                        System.out.println("Needed 4 bytes, got "+n);
                        break;
                    }
                    //dump("Got header: ",hdr);
                    int len = getInt(hdr,0);
                    int cmd = len&0xff;
                    len = len >> 8;
                    System.out.println("CMD: "+hdr[3]+", length: "+len);

                    byte[] par=new byte[len];

                    if (len>0) {
                        int n2=sis.read(par);
                        if (n2!=len) {
                            System.out.println("Needed "+len+" bytes, got "+n2);
                            break;
                        }
                    }

                    //dump("Got pars: ",par);
                    
                    if (cmd == 1) {
                        double w=getDouble(par, 0);
                        double h=getDouble(par, 8);
                        System.out.println("Open("+w+",+"+h+")");

                        if (f!=null) {
                            f.removeAll();
                            f.dispose();
                            f=null;
                            if (c!=null) c=null;
                        }
                        f=new Frame();
                        c=new XGDcanvas((int)w, (int)h);
                        f.add(c);
                        f.pack();
                        f.setVisible(true);
                    }

                    if (cmd == 2) {
                        if (f!=null) {
                            f.removeAll();
                            f.dispose();
                            f=null;
                            if (c!=null) c=null;
                        }
                        System.out.println("Device closed.");
                        return;
                    }

                    if (cmd == 9 && c!=null) {
                        c.add(new XGDline(getDouble(par,0), getDouble(par,8), getDouble(par,16), getDouble(par,24)));
                    }

                    if (cmd == 15 && c!=null) {
                        c.add(new XGDrect(getDouble(par,0), getDouble(par,8), getDouble(par,16), getDouble(par,24)));
                    }

                    if (cmd == 5 && c!=null) {
                        c.add(new XGDcircle(getDouble(par,0), getDouble(par,8), getDouble(par,16)));
                    }

                    if (cmd == 11 && c!=null) {
                        if (getInt(par, 0)==0) c.repaint();
                    }

                    if (cmd == 12 && c!=null) {
                        c.reset();
                    }

                    if ((cmd == 13 || cmd == 14) && c!=null) {
                        int pn=getInt(par,0);
                        int i=0;
                        double x[], y[];
                        x=new double[pn];
                        y=new double[pn];
                        while (i<pn) {
                            x[i]=getDouble(par, 4 + i*8);
                            y[i]=getDouble(par, 4 + i*8 + pn*8);
                            i++;
                        }
                        c.add(new XGDpolygon(pn, x, y, cmd==14));
                    }
                    
                    if (cmd == 18 && c!=null) {
                        c.add(new XGDtext(getDouble(par,0), getDouble(par,8), getDouble(par,16), getDouble(par,24), new String(par,32,par.length-33)));
                    }

                    if (cmd == 0x13 && c!=null) {
                        c.add(new XGDcolor(getInt(par,0)));
                    }

                    if (cmd == 0x14 && c!=null) {
                        c.add(new XGDfill(getInt(par,0)));
                    }

                    if (cmd == 0x15 && c!=null) {
                        XGDfont xf=new XGDfont(getDouble(par,0), getDouble(par,8), getDouble(par,16), getInt(par,24), new String(par,32,par.length-33));
                        c.add(xf);
                        // we need to set Canvas' internal font to this new font for further metrics calculations
                        c.f=xf.font;
                    }

                    if (cmd == 0x16 && c!=null) {
                        c.add(new XGDlinePar(getDouble(par,0), getInt(par,8)));
                    }
                    
                    if (cmd == 0x50) {
                        byte[] b = new byte[4*8 + 4];
                        setInt((0x50 | 0x80) | ((4*8)<<8), b, 0);
                        double width=0d, height=0d;
                        if (c != null) {
                            Dimension d = c.getSize();
                            width = d.getWidth();
                            height = d.getHeight();
                        }
                        setDouble(0d, b, 4);
                        setDouble(width, b, 12);
                        setDouble(height, b, 20);
                        setDouble(0d, b, 28);
                        sos.write(b);
                        sos.flush();
                    }
                    
                    if (cmd == 0x51) { // StrWidth
                        String s=new String(par, 0, par.length-1);
                        System.out.println("Request: get string width of \""+s+"\"");
                        byte[] b= new byte[12];
                        setInt((0x51 | 0x80) | 0x800,b,0);
                        double width=(double)(8*s.length()); // rough estimate
                        if (c!=null) {
                            Graphics g=c.getGraphics();
                            if (g!=null) {
                                FontMetrics fm=g.getFontMetrics(c.f);
                                if (fm!=null) width=(double)fm.stringWidth(s);
                            }
                        }
                        System.out.println(">> WIDTH: "+width);
                        setDouble(width,b,4);
                        //dump("Sending: ",b);
                        sos.write(b);
                        sos.flush();
                    }

                    if (cmd == 0x4a) { // MetricInfo
                        int ch=getInt(par, 0);
                        System.out.println("Request: metric info for char "+ch);
                        byte[] b= new byte[4 + 3*8];

                        double ascent=0.0, descent=0.0, width=8.0;
                        if (c!=null) {
                            Graphics g=c.getGraphics();
                            if (g!=null) {
                                FontMetrics fm=g.getFontMetrics(c.f);
                                if (fm!=null) {
                                    ascent=(double) fm.getAscent();
                                    descent=(double) fm.getDescent();
                                    width=(double) fm.charWidth((ch==0)?77:ch);
                                }
                            }
                        }
                        System.out.println(">> MI: ascent="+ascent+", descent="+descent+", width="+width);
                        setInt((0x4a | 0x80) | ((3*8)<<8),b,0);
                        setDouble(ascent,b,4);
                        setDouble(descent,b,12);
                        setDouble(width,b,20);
                        //dump("Sending: ",b);
                        sos.write(b);
                        sos.flush();
                    }
                }
            } catch (Exception e) {
                System.out.println("XGDworker"+this+", exception: "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            ServerSocket s=new ServerSocket(1427);
            while(true) {
                Socket cs=s.accept();
                System.out.println("Accepted connection, spawning new worker thread.");
                XGDworker w=new XGDworker();
                w.s=cs;
                w.start();
                w=null;
            }
        } catch (Exception e) {
            System.out.println("XGDserver, exception: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static XGDserver startServer() {
        XGDserver s=new XGDserver();
        s.start();
        return s;
    }

    public static void main(String[] args) {
        System.out.println("Starting XGDserver.");
        startServer();
    }
}