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
    
    public XGDcanvas(int w, int h) {
        l=new Vector();
        setSize(w,h);
    }

    public void add(XGDobject o) { l.add(o); }
    public void reset() { l.removeAllElements(); }
    public void paint(Graphics g) {
        int i=0, j=l.size();
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
        //if (c.col!=null)
        g.drawLine((int)x1,(int)y1,(int)x2,(int)y2);
    }
}

class XGDrect extends XGDobject {
    double x1,y1,x2,y2;
    public XGDrect(double x1, double y1, double x2, double y2) {
        System.out.println(">> RECT("+x1+","+y1+","+x2+","+y2+")");
        this.x1=x1; this.y1=y1; this.x2=x2; this.y2=y2;
    }

    public void paint(XGDcanvas c, Graphics g) {
        if (c.fill!=null) {
            g.setColor(c.fill);
            g.fillRect((int)x1,(int)y1,(int)(x2-x1),(int)(y2-y1));
            if (c.col!=null) g.setColor(c.col);
        }
        //if (c.col!=null)
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
        //if (c.col!=null)
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
        //if (c.col!=null)
        g.drawString(txt,(int)x,(int)y);
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
                s.setTcpNoDelay(true);
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
                    System.out.println("Connected to XGD version "+id[3]+" on PPC-endian machine");
                    isBE=true;
                } else if (id[3]==0x58 && id[2]==0x47 && id[1]==0x44) {
                    System.out.println("Connected to XGD version "+id[0]+" on Intel-endian machine");
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
                    dump("Got header: ",hdr);
                    int len =
                        ((int)hdr[2])+
                        (((int)hdr[1])<<8)+
                        (((int)hdr[0])<<16);
                    int cmd = hdr[3];
                    if (!isBE) {
                        len = hdr[1]+(hdr[2]<<8)+(hdr[3]<<16);
                        cmd = hdr[0];
                    }
                    System.out.println("CMD: "+hdr[3]+", length: "+len);

                    byte[] par=new byte[len];
                    int n2=sis.read(par);
                    if (n2!=len) {
                        System.out.println("Needed "+len+" bytes, got "+n2);
                        break;
                    }

                    dump("Got pars: ",par);
                    
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

                    if (cmd == 18 && c!=null) {
                        c.add(new XGDtext(getDouble(par,0), getDouble(par,8), getDouble(par,16), getDouble(par,24), new String(par,32,par.length-33)));
                    }
                    
                    if (cmd == 0x51) { // StrWidth
                        String s=new String(par, 0, par.length-1);
                        System.out.println("Request: get string width of \""+s+"\"");
                        byte[] b= new byte[12];
                        setInt((0x51 | 0x80) | 0x800,b,0);
                        setDouble((double)(8*s.length()),b,4);
                        dump("Sending: ",b);
                        sos.write(b);
                    }

                    if (cmd == 0x4a) { // MetricInfo
                        int ch=getInt(par, 0);
                        System.out.println("Request: metric info for char "+ch);
                        byte[] b= new byte[4 + 3*8];
                        setInt((0x4a | 0x80) | ((3*8)<<8),b,0);
                        setDouble(8d,b,4);
                        setDouble(8d,b,12);
                        setDouble(8d,b,20);
                        dump("Sending: ",b);
                        sos.write(b);
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