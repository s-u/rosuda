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

import org.rosuda.ibase.*;

import org.rosuda.JRI.*;
import org.rosuda.JGR.toolkit.*;
import org.rosuda.JGR.util.*;


public class JGR {


    public static Vector RHISTORY = null;
    public static JGRConsole MAINRCONSOLE = null;
    public static String RHOME = "";
    public static String[] RLIBS;
    public static Rengine R = null;
    public static ConsoleSync rSync = new ConsoleSync();

    public static boolean STARTED = false;

    public static Vector DATA = new Vector();
    public static Vector MODELS = new Vector();
    public static Vector OTHERS = new Vector();

    public static Vector OBJECTS = new Vector();
    public static HashMap KEYWORDS = new HashMap();
    public static HashMap KEYWORDS_OBJECTS = new HashMap();

    public static int SLEEPTIME = 50;
    public static int STRINGBUFFERSIZE = 80;

    private static JGRListener jgrlistener  = null;

    public static org.rosuda.JGR.toolkit.SplashScreen splash;

    public JGR() {
        SVar.int_NA=-2147483648;
        
        Object dummy = new Object();
        JGRPackageManager.neededPackages.put("base",dummy);
        JGRPackageManager.neededPackages.put("graphics",dummy);
        JGRPackageManager.neededPackages.put("utils",dummy);
        JGRPackageManager.neededPackages.put("methods",dummy);
        JGRPackageManager.neededPackages.put("stats",dummy);
        
        JGRPackageManager.neededPackages.put("JGR",dummy);
        JGRPackageManager.neededPackages.put("rJava",dummy);
        JGRPackageManager.neededPackages.put("JavaGD",dummy);
        
        org.rosuda.util.Platform.initPlatform("org.rosuda.JGR.toolkit.");
        JGRPrefs.initialize();
        splash = new org.rosuda.JGR.toolkit.SplashScreen();
        splash.start();
        readHistory();
        MAINRCONSOLE = new JGRConsole();
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
        JGRPackageManager.defaultPackages = RController.getDefaultPackages();
        setRLibs();
        MAINRCONSOLE.setWorking(false);
        STARTED = true;
        if (!System.getProperty("os.name").startsWith("Win")) splash.stop();
        MAINRCONSOLE.end = MAINRCONSOLE.output.getText().length();
        rSync.triggerNotification("library(JGR, warn.conflicts=FALSE)");
        MAINRCONSOLE.input.requestFocus();
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
        if (JGRHelp.current == null) new JGRHelp(location);
        else {
            JGRHelp.current.show();
            JGRHelp.current.refresh();
        }
        if (keyword!=null && file !=null) JGRHelp.current.goTo(keyword, file);
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
        if (type.equals("data.frame")) new DataTable(RController.getVarSet(RController.createDataFrame(data)));
        else if (type.equals("matrix")) new DataTable(RController.getVarSet(RController.createMatrix(data)));
    }

    public static void setRHome(String rhome) {
        RHOME = rhome;
    }

    public static void setRLibs() {
    	RLIBS = RController.getRLIBS();
        for (int i = 0; i< RLIBS.length; i++) {
            if(RLIBS[i].startsWith("~")) RLIBS[i] = RLIBS[i].replaceFirst("~",System.getProperty("user.home"));
        }
    }

    public static void setKeyWords() {
    	String[] words = RController.getKeyWords();
      	KEYWORDS.clear();
        Object dummy = new Object();
        for (int i = 0; i < words.length; i++) {
        	KEYWORDS.put(words[i],dummy);
        }
    }

    public static void setObjects() {
    	String[] objects = RController.getObjects();
       	OBJECTS.clear();
       	KEYWORDS_OBJECTS.clear();
        Object dummy = new Object();
        for (int i = 0; i < objects.length; i++) {
        	KEYWORDS_OBJECTS.put(objects[i],dummy);
        	OBJECTS.add(objects[i]);
        }
    }
    
    public static void refreshAll(String blabla) {
        setKeyWords();
        setObjects();
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
            new ErrorMsg(e);
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
            new ErrorMsg(e);
        }


    }

    public static void main(String[] args) {
        try {
            new JGR();
        }
        catch (Exception e) {
            new ErrorMsg(e);
        }
    }
}
