package org.rosuda.JGR;

//
//  JGR.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;


import org.rosuda.JRI.*;
import org.rosuda.JGR.rhelp.*;
import org.rosuda.JGR.toolkit.*;
import org.rosuda.ibase.*;
import org.rosuda.util.*;

public class JGR {


    public static Vector RHISTORY = null;
    public static RConsole MAINRCONSOLE = null;
    public static String RHOME = "";
    public static String[] RLIBS;
    public static Rengine R = null;
    public static ConsoleSync RCSync = new ConsoleSync();
    public static boolean STARTED = false;
    public static boolean READY = false;

    public static Vector DATA = new Vector();
    public static Vector MODELS = new Vector();
    public static Vector OTHERS = new Vector();

    public static int SLEEPTIME = 50;
    public static int STRINGBUFFERSIZE = 80;

    private static JGRListener jgrlistener  = null;

    public JGR() {
        SVar.int_NA=-2147483648;
        Platform.initPlatform("org.rosuda.JGR.toolkit.");
        Preferences.initialize();
        SplashScreen splash = new SplashScreen();
        readHistory();
        new RConsole();
        MAINRCONSOLE.progress.start("Starting R");
        MAINRCONSOLE.setWorking(true);
        splash.stop();
        String[] args={"--save"};
        R=new Rengine(args,true,MAINRCONSOLE); //JGR.MAINRCONSOLE);
        System.out.println("Rengine created, waiting for R");
        if (!R.waitForR()) {
            System.out.println("Cannot load R");
            System.exit(1);
        }
        //R.eval("library(JGR)");
        Preferences.refreshKeyWords();
        RHOME = RTalk.getRHome();
        RLIBS = RTalk.getRLIBS();
        for (int i = 0; i< RLIBS.length; i++) {
            if(RLIBS[i].startsWith("~")) RLIBS[i] = RLIBS[i].replaceFirst("~",System.getProperty("user.home"));
        }
        MAINRCONSOLE.setWorking(false);
        MAINRCONSOLE.input.requestFocus();
        STARTED = true;
        RCSync.triggerNotification("library(JGR, warn.conflicts=FALSE)");
    }

    public static String exit() {
        int exit = JOptionPane.showConfirmDialog(null, "Save workspace?",
                                                 "Close JGR",
                                                 JOptionPane.
                                                 YES_NO_CANCEL_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE);

        if (exit == 0) {
            writeHistory();
            return "y\n";
            //R.eval("save.image()");
            //System.exit(0);
        }
        else if (exit == 1) return "n\n"; //System.exit(0);
        else return "c\n";
    }


    public static void help(String keyword, String file, String location) {
        if (file.trim().equals("null")) file = null;
        if (keyword.trim().equals("null")) keyword = null;
        if (location.trim().equals("null")) location = null;
        if (RHelp.last == null) new RHelp(location);
        else {
            RHelp.last.show();
            RHelp.last.refresh();
        }
        if (keyword!=null && file !=null) RHelp.last.goTo(keyword, file);
    }

    public static void addMenu(String name) {
        iMenu.addMenu(MAINRCONSOLE,name);
    }

    public static void addMenuItem(String menu, String name, String cmd) {
        if (jgrlistener == null) jgrlistener = new JGRListener();
        iMenu.addMenuItem(MAINRCONSOLE,menu,name,cmd,jgrlistener);
    }

    public static void fix(String data) {
        new DataTable(RTalk.getVarSet(RTalk.createDataFrame(data)));
    }



    public static void readHistory() {
        File hist = null;
        try {
            if ((hist = new File(System.getProperty("user.home") +
                                  File.separator + ".Rhistory")).exists()) {

                 BufferedReader reader = new BufferedReader(new FileReader(hist));
                 RHISTORY = new Vector();
                 while (reader.ready()) RHISTORY.add(reader.readLine());
                 reader.close();
            }
        }
        catch (Exception e) {
            new iError(e);
        }
    }

    public static void writeHistory() {
        File hist = null;
        try {
            hist = new File(System.getProperty("user.home") +
                                  File.separator + ".Rhistory");
            BufferedWriter writer = new BufferedWriter(new FileWriter(hist));
            Enumeration e = RHISTORY.elements(); int i = 0;
            while(e.hasMoreElements()) writer.write(e.nextElement().toString()+"\n");
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            new iError(e);
        }


    }

    public static void main(String[] args) {
        try {
            JGR JGR1 = new JGR();
        }
        catch (Exception e) {
            new iError(e);
        }
    }
}