//
//  SplashScreen.java
//  Klimt
//
//  Created by Simon Urbanek on Fri Jun 06 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.zip.*;

public class SplashScreen extends Frame implements ActionListener, WindowListener, Commander {
    class SplashImageCanvas extends Canvas implements ImageObserver {
        Image logo;
        int w,h;
        boolean hasSize=false;
        SplashScreen par;
        
        SplashImageCanvas(Image img, SplashScreen p) {
            logo=img; par=p;
            img.getWidth(this);
            img.getHeight(this);
        }

        public void paint(Graphics g) {
            g.drawImage(logo,0,0,this);
        }
        
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
        {
            //System.out.println("imageUpdate: "+infoflags+", x="+x+", y="+y+", width="+width+", height="+height);
            
            if ((((infoflags&ImageObserver.WIDTH)!=0)&&((infoflags&ImageObserver.HEIGHT)!=0)) && !hasSize) {
                w=width;
                h=height;
                hasSize=true;
                setSize(w,h); par.pack();
            }
            if ((infoflags&ImageObserver.ALLBITS)==ImageObserver.ALLBITS) {
                par.setVisible(true);
            }
            return true;
        }
    }
    
    public SplashScreen() {
        super("Klimt "+Common.Version);
        addWindowListener(this);
        byte[] arrayLogo;
        try {
            ZipFile MJF;
            try {
                MJF = new ZipFile("KlimtLite.app/Contents/Resources/Java/KlimtLite.jar");
            } catch (Exception e) {
                MJF = new ZipFile(System.getProperty("java.class.path"));
            }
            ZipEntry LE = MJF.getEntry("splash.jpg");
            InputStream inputLogo = MJF.getInputStream(LE);
            arrayLogo = new byte[(int)LE.getSize()];
            for( int i=0; i<arrayLogo.length; i++ ) {
                arrayLogo[i] = (byte)inputLogo.read();
            }
            System.out.println("Logo OK, "+arrayLogo.length+" bytes.");
        } catch (Exception e) {
            System.out.println("Logo Exception: "+e);
            arrayLogo = new byte[1];
        }

        setLayout(new BorderLayout());
        Panel p=new Panel();
        p.add(new SplashImageCanvas(Toolkit.getDefaultToolkit().createImage(arrayLogo),this));
        add(p);
        p=new Panel();
        add(p, BorderLayout.SOUTH);
        Label l=new Label("Klimt v"+Common.Version+" (release "+Common.Release+")");
        l.setFont(new Font("SansSerif",Font.BOLD,14));
        p.add(l);
        
        String myMenu[]={"+","File","@OOpen dataset ...","openData","-",
            "@QQuit","exit",
            "~Window","0"};
        EzMenu.getEzMenu(this,this,myMenu);
        pack();
    }

    public Object run(Object o, String cmd) {
        if (cmd=="exit") WinTracker.current.Exit();
        if (cmd=="openData") {
            SVarSet tvs=new SVarSet();
            SNode t=InTr.openTreeFile(this,null,tvs);
            if (t==null && tvs.count()<1) {
                Frame f=new Frame("dummy");
                new MsgDialog(f,"Load Error","I'm sorry, but I was unable to load the file you selected.");
                f=null;
            } else {
                if (t!=null) {
                    TFrame f=new TFrame("Tree "+tvs.getName(),TFrame.clsTree);
                    InTr.newTreeDisplay(t,f,0,0,Common.screenRes.width-160,(Common.screenRes.height>600)?600:Common.screenRes.height-20);
                }
                VarFrame vf=InTr.newVarDisplay(tvs,Common.screenRes.width-150,0,140,(Common.screenRes.height>600)?600:Common.screenRes.height);
            }
        }
        return null;
    }

    public void actionPerformed(ActionEvent e) {
        if (e==null) return;
        run(e.getSource(),e.getActionCommand());
    }

    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {
        System.exit(0);
    }
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}
