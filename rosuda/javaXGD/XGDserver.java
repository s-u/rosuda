import java.net.*;
import java.io.*;

public class XGDserver extends Thread {
    class XGDworker extends Thread {
        public Socket s;
        boolean isBE;
        public void run() {
            try {
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
                    System.out.println("Got "+hdr[0]+" "+hdr[1]+" "+hdr[2]+" "+hdr[3]);
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