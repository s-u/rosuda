package org.rosuda.JGR;

//
//  JGR.java
//  JGR
//
//  Created by Markus Helbig on Fri Mar 05 2004.
//  Copyright (c) 2004 __MyCompanyName__. All rights reserved.
//




import java.io.*;
import java.util.*;
import javax.swing.*;


import org.rosuda.JRI.*;
import org.rosuda.JGR.toolkit.*;
import org.rosuda.ibase.*;

public class JGR {


    public static Vector RHISTORY = null;
    public static RConsole MAINRCONSOLE = null;
    public static String RHOME = "";
    public static String[] RLIBS;
    public static Rengine R = null;
    public static ConsoleSync rSync = new ConsoleSync();

    public static boolean STARTED = false;

    public static Vector DATA = new Vector();
    public static Vector MODELS = new Vector();
    public static Vector OTHERS = new Vector();

    public static int SLEEPTIME = 50;
    public static int STRINGBUFFERSIZE = 80;

    private static JGRListener jgrlistener  = null;

    public static org.rosuda.JGR.toolkit.SplashScreen splash;

    public JGR() {
        SVar.int_NA=-2147483648;
        
        Object dummy = new Object();
        RPackageManager.neededPackages.put("base",dummy);
        RPackageManager.neededPackages.put("graphics",dummy);
        RPackageManager.neededPackages.put("utils",dummy);
        RPackageManager.neededPackages.put("methods",dummy);
        RPackageManager.neededPackages.put("stats",dummy);
        
        RPackageManager.neededPackages.put("JGR",dummy);
        RPackageManager.neededPackages.put("rJava",dummy);
        RPackageManager.neededPackages.put("JavaGD",dummy);
        
        org.rosuda.util.Platform.initPlatform("org.rosuda.JGR.toolkit.");
        iPreferences.initialize();
        splash = new org.rosuda.JGR.toolkit.SplashScreen();
        splash.start();
        readHistory();
        MAINRCONSOLE = new RConsole();
        splash.toFront();
        if (System.getProperty("os.name").startsWith("Window")) splash.stop();
        MAINRCONSOLE.progress.start("Starting R");
        MAINRCONSOLE.setWorking(true);
        String[] args={"--save"};
        R=new Rengine(args,true,MAINRCONSOLE);
        System.out.println("Rengine created, waiting for R");
        if (!R.waitForR()) {
            System.out.println("Cannot load R");
            System.exit(1);
        }
        RHOME = RTalk.getRHome();
        RLIBS = RTalk.getRLIBS();
        for (int i = 0; i< RLIBS.length; i++) {
            if(RLIBS[i].startsWith("~")) RLIBS[i] = RLIBS[i].replaceFirst("~",System.getProperty("user.home"));
        }
        RPackageManager.defaultPackages = RTalk.getDefaultPackages();
        iPreferences.refreshKeyWords();
        MAINRCONSOLE.setWorking(false);
        MAINRCONSOLE.input.requestFocus();
        STARTED = true;
        if (!System.getProperty("os.name").startsWith("Window")) splash.stop();
        MAINRCONSOLE.end = MAINRCONSOLE.output.getText().length();
        rSync.triggerNotification("library(JGR, warn.conflicts=FALSE)");
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
        }
        else if (exit == 1) return "n\n";
        else return "c\n";
    }


    public static void help(String keyword, String file, String location) {
        if (file.trim().equals("null")) file = null;
        if (keyword.trim().equals("null")) keyword = null;
        if (location.trim().equals("null")) location = null;
        if (RHelp.current == null) new RHelp(location);
        else {
            RHelp.current.show();
            RHelp.current.refresh();
        }
        if (keyword!=null && file !=null) RHelp.current.goTo(keyword, file);
    }

    public static void addMenu(String name) {
        iMenu.addMenu(MAINRCONSOLE,name);
    }

    public static void addMenuItem(String menu, String name, String cmd) {
        if (jgrlistener == null) jgrlistener = new JGRListener();
        iMenu.addMenuItem(MAINRCONSOLE,menu,name,cmd,jgrlistener);
    }


    public static void addMenuSeparator(String menu) {
        iMenu.addMenuSeparator(MAINRCONSOLE,menu);
    }
    

    public static void fix(String data, String type) {
        System.out.println(type);
        if (type.equals("data.frame")) new DataTable(RTalk.getVarSet(RTalk.createDataFrame(data)));
        else if (type.equals("matrix")) new DataTable(RTalk.getVarSet(RTalk.createMatrix(data)));
    }

    public static void setRHome(String rhome) {
        RHOME = rhome;
    }

    public static void setRLibs(String[] libs) {
        RLIBS = libs;
        for (int i = 0; i< RLIBS.length; i++) {
            if(RLIBS[i].startsWith("~")) RLIBS[i] = RLIBS[i].replaceFirst("~",System.getProperty("user.home"));
        }
    }

    public static void setKeyWords(String[] words) {
        Object dummy = new Object();
        for (int i = 0; i < words.length; i++) iPreferences.KEYWORDS.put(words[i],dummy);
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
            new JGR();
        }
        catch (Exception e) {
            new iError(e);
        }
    }
}
