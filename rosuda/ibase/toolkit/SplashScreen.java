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
    public static SplashScreen main;
    
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

        Image splash=null;
        main=this;

        addWindowListener(this);
        byte[] arrayLogo;

        // first try platform's "getResourceFile". This should work anywhere except for single .jar files
        try {
            splash=Toolkit.getDefaultToolkit().getImage(Platform.getPlatform().getResourceFile("splash.jpg"));
            if (Common.DEBUG>0 && splash!=null)
                System.out.println("Good, obtained logo via Platform.getResourceFile");
        } catch (Exception ex) {}

        if (splash==null) { // ok, if that failed, try to extract it from the jar file
            try {
                ZipFile MJF=null;
                String jar=System.getProperty("java.class.path");
                if (jar!=null) {
                    // if there are more path entries we assume that ours is the last one
                    int i=jar.lastIndexOf(File.pathSeparatorChar);
                    if (i>-1)
                        jar=jar.substring(i+1);
                    if (Common.DEBUG>0)
                        System.out.println("my own jar file: "+jar);
                    MJF = new ZipFile(jar);
                }
                if (MJF!=null) {
                    ZipEntry LE = MJF.getEntry("splash.jpg");
                    InputStream inputLogo = MJF.getInputStream(LE);
                    arrayLogo = new byte[(int)LE.getSize()];
                    for( int i=0; i<arrayLogo.length; i++ ) {
                        arrayLogo[i] = (byte)inputLogo.read();
                    }
                    if (Common.DEBUG>0)
                        System.out.println("Logo OK, "+arrayLogo.length+" bytes.");
                    splash=Toolkit.getDefaultToolkit().createImage(arrayLogo);
                }
            } catch (Exception e) {
                System.out.println("Can't find splash image (neither via Platform.getResourceFile, nor in the jar file). Exception: "+e);
            }
        }

        setLayout(new BorderLayout());
        Panel p=new Panel();
        p.add(new SplashImageCanvas(splash,this));
        add(p);
        p=new Panel();
        add(p, BorderLayout.SOUTH);
        Label l=new Label("Klimt v"+Common.Version+" (release "+Common.Release+")");
        l.setFont(new Font("SansSerif",Font.BOLD,14));
        p.add(l);
        
        String myMenu[]={"+","File","@OOpen dataset ...","openData","-",
            "Preferences ...","prefs","-",
            "@QQuit","exit",
            "~Window","0"};
        String macMenu[]={"+","File","@OOpen dataset ...","openData","0"};
        if (Common.isMac)
            myMenu=macMenu;
        EzMenu.getEzMenu(this,this,myMenu);
        pack();
    }

    public Object run(Object o, String cmd) {
        if (cmd=="exit") {
            if (WinTracker.current!=null)
                WinTracker.current.Exit();
            else
                exit();
        }
        
        if (cmd=="openData") {
            SVarSet tvs=new SVarSet();
            SNode t=InTr.openTreeFile(this,null,tvs);
            if (t==null && tvs.count()<1) {
            } else {
                if (t!=null) {
                    TFrame f=new TFrame("Tree "+tvs.getName(),TFrame.clsTree);
                    InTr.newTreeDisplay(t,f,0,0,Common.screenRes.width-160,(Common.screenRes.height>600)?600:Common.screenRes.height-20);
                }
                VarFrame vf=InTr.newVarDisplay(tvs,Common.screenRes.width-150,0,140,(Common.screenRes.height>600)?600:Common.screenRes.height);
                setVisible(false);
            }
        }
        return null;
    }

    void exit() {
        System.exit(0);
    }

    boolean aboutMode=false;

    public void runAsAbout() {
        aboutMode=true;
        setVisible(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e==null) return;
        run(e.getSource(),e.getActionCommand());
    }

    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
        if (aboutMode && WinTracker.current!=null && WinTracker.current.wins.size()>0) {
            aboutMode=false;
            setVisible(false); return;
        }
        exit();
    }
    public void windowClosed(WindowEvent e) {
        exit();
    }
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}
