//
//  SplashScreen.java
//  Klimt
//
//  Created by Simon Urbanek on Fri Jun 06 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//

package org.rosuda.ibase.toolkit;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.zip.*;

import org.rosuda.ibase.*;
import org.rosuda.util.*;

public class SplashScreen extends Frame implements ActionListener, WindowListener, Commander {
    public static SplashScreen main;
    public static RecentList recentOpen;
    
    class SplashImageCanvas extends Canvas implements ImageObserver {
        Image logo;
        int w,h;
        boolean hasSize=false;
        SplashScreen par;
        
        SplashImageCanvas(Image img, SplashScreen p) {
            logo=img; par=p;
	    if (img!=null) {
		img.getWidth(this);
		img.getHeight(this);
	    }
        }

        public void paint(Graphics g) {
            if (logo!=null) g.drawImage(logo,0,0,this);
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

    Menu recentMenu;
    
    public SplashScreen(String txt) {
        super("About");

        Image splash=null;
        main=this;

        addWindowListener(this);
        byte[] arrayLogo;

        // first try platform's "getResourceFile". This should work anywhere except for single .jar files
        try {
	    String fn=Platform.getPlatform().getResourceFile("splash.jpg");
	    if (new File(fn).exists()) {
		splash=Toolkit.getDefaultToolkit().getImage(fn);
		if (Global.DEBUG>0 && splash!=null)
		    System.out.println("Good, obtained logo via Platform.getResourceFile");
	    }
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
                    if (Global.DEBUG>0)
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
                    if (Global.DEBUG>0)
                        System.out.println("Logo OK, "+arrayLogo.length+" bytes.");
                    splash=Toolkit.getDefaultToolkit().createImage(arrayLogo);
                }
            } catch (Exception e) {
                if (Global.AppType==Common.AT_Framework) { // try harder if we're iplots
                    try {
                        String jar=System.getProperty("java.class.path");
                        if (jar!=null) {
                            // this is the clue - we know our own name
                            int i=jar.indexOf("iplots.jar");
                            if (i>=0) {
                                int j=jar.indexOf(File.pathSeparatorChar,i);
                                int k=jar.substring(0,i).lastIndexOf(File.pathSeparatorChar);
                                String s=null;
                                if (k<0) k=-1; // just for safety although not needed
                                if (j>=0)
                                    s=jar.substring(k+1,j);
                                else
                                    s=jar.substring(k+1);
                                ZipFile MJF = new ZipFile(s);
                                if (MJF!=null) {
                                    ZipEntry LE = MJF.getEntry("splash.jpg");
                                    InputStream inputLogo = MJF.getInputStream(LE);
                                    arrayLogo = new byte[(int)LE.getSize()];
                                    for( i=0; i<arrayLogo.length; i++ ) {
                                        arrayLogo[i] = (byte)inputLogo.read();
                                    }
                                    if (Global.DEBUG>0)
                                        System.out.println("Logo OK (iplots), "+arrayLogo.length+" bytes.");
                                    splash=Toolkit.getDefaultToolkit().createImage(arrayLogo);
                                }
                            }
                        }
                    } catch (Exception exx) { // iplots must be silent
                    }
                } else
                    System.out.println("Can't find splash image (neither via Platform.getResourceFile, nor in the jar file).");
            }
        }

        setLayout(new BorderLayout());
        Panel p=new Panel();
	p.add(new SplashImageCanvas(splash,this));
        add(p);
        p=new Panel();
        add(p, BorderLayout.SOUTH);
        Label l=null;
        l=new Label(txt);
        /*
         l=new Label("iPlots framework v"+Common.Version+" (release "+Common.Release+")");
         */
        l.setFont(new Font("SansSerif",Font.BOLD,14));
        p.add(l);

        String myMenu[]={"+","File","@OOpen dataset ...","openData","#Open Recent","","-",
            "Preferences ...","prefs","-","@QQuit","exit", "~Window","0"};
        String macMenu[]={"+","File","@OOpen dataset ...","openData","#Open Recent","","0"};
        if (Platform.isMac)
            myMenu=macMenu;
        EzMenu.getEzMenu(this,this,myMenu);
        Menu rm=recentMenu=(Menu) EzMenu.getItemByLabel(this,"Open Recent");
        if (rm!=null) {
            if (recentOpen==null)
                SplashScreen.recentOpen=new RecentList(Common.appName,"RecentOpenFiles",8);
            String[] shortNames=SplashScreen.recentOpen.getShortEntries();
            String[] longNames =SplashScreen.recentOpen.getAllEntries();
            int i=0;
            while (i<shortNames.length) {
                MenuItem mi=new MenuItem(shortNames[i]);
                mi.setActionCommand("recent:"+longNames[i]);
                mi.addActionListener(this);
                rm.add(mi);
                i++;
            }
            if (i>0) rm.addSeparator();
            MenuItem ca=new MenuItem("Clear list");
            ca.setActionCommand("recent-clear");
            ca.addActionListener(this);
            rm.add(ca);
            if (i==0) ca.setEnabled(false);
        }
        pack();
	if (splash==null) // if there's no image, we can't wait for async show
	    setVisible(true);
    }

    public Object run(Object o, String cmd) {
        if (cmd=="exit") {
            if (WinTracker.current!=null)
                WinTracker.current.Exit();
            else
                exit();
        }

        if (cmd=="recent-clear") {
            if (recentOpen!=null && recentMenu!=null) {
                recentMenu.removeAll();
                recentMenu.addSeparator();
                MenuItem ca=new MenuItem("Clear list");
                ca.setActionCommand("recent-clear");
                ca.addActionListener(this);
                ca.setEnabled(false);
                recentMenu.add(ca);
                recentOpen.reset();
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

    public static void runMainAsAbout(String txt) {
        if (main==null) main=new SplashScreen(txt);
        main.runAsAbout();
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e==null) return;
        run(e.getSource(),e.getActionCommand());
    }

    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {
        if ((aboutMode && WinTracker.current!=null && WinTracker.current.wins.size()>0) || Global.AppType!=Global.AT_standalone) {
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
