package org.rosuda.JGR;

/**
*  JGR main application
 *
 *	@author Markus Helbig
 *
 * 	RoSuDA 2003 - 2004
 */

import java.io.*;
import java.util.*;
import javax.swing.*;

import org.rosuda.ibase.*;

import org.rosuda.JRI.*;
import org.rosuda.JGR.toolkit.*;
import org.rosuda.JGR.util.*;


public class JGR {

    /* Copyright information and other stuff */

    public static final String VERSION = "1.0";
    public static final String TITLE = "JGR";
    public static final String SUBTITLE = "Java Gui for R";
    public static final String DEVELTIME = "2003 - 2004";
    public static final String INSTITUTION = "RoSuDa, Univ. Augsburg";
    public static final String AUTHOR1 = "Markus Helbig";
    public static final String AUTHOR2 = "Simon Urbanek";
    public static final String WEBSITE = "http://www.rosuda.org";
    public static final String LOGO = "logo.jpg";
    public static final String SPLASH = "splash.jpg";

    /* global variables for whole JGR application */

    public static Vector RHISTORY = null;
    public static JGRConsole MAINRCONSOLE = null;
    public static String RHOME = null;
    public static String[] RLIBS = null;
    public static Rengine R = null;
    public static ConsoleSync rSync = new ConsoleSync();

    public static Vector DATA = new Vector();
    public static Vector MODELS = new Vector();
    public static Vector OTHERS = new Vector();
    public static Vector FUNCTIONS = new Vector();

    public static Vector  OBJECTS = new Vector();
    public static HashMap KEYWORDS = new HashMap();
    public static HashMap KEYWORDS_OBJECTS = new HashMap();

    public static boolean STARTED = false;

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
        MAINRCONSOLE.setWorking(true);
        splash.toFront();
        if (System.getProperty("os.name").startsWith("Window")) splash.stop();
		
		// let's preemptively load JRI - if we do it here, we can show an error message
		try {
            System.loadLibrary("jri");
        } catch (UnsatisfiedLinkError e) {
			String errStr="all environment variables (PATH, LD_LIBRARY_PATH, etc.) are setup properly (see supplied script)";
			String libName="libjri.so";
			if (System.getProperty("os.name").startsWith("Window"))
			{ errStr="you start JGR by double-clicking the JGR.exe program"; libName="jri.dll"; }
			if (System.getProperty("os.name").startsWith("Mac"))
			{ errStr="you start JGR by double-clicking the JGR application"; libName="libjri.jnilib"; }
			JOptionPane.showMessageDialog(null, "Cannot find Java/R Interface (JRI) library ("+libName+").\nPlease make sure "+errStr+".", "Cannot find JRI library", JOptionPane.ERROR_MESSAGE);
			System.err.println("Cannot find JRI native library!\n");
            e.printStackTrace();
            System.exit(1);
        }
		
		if (!Rengine.versionCheck()) {
			JOptionPane.showMessageDialog(null, "Java/R Interface (JRI) library doesn't match this JGR version.\nPlease update JGR and JRI to the latest version.", "Version Mismatch", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		}
        String[] args={"--save"};
        R=new Rengine(args,true,MAINRCONSOLE);
        if (org.rosuda.util.Global.DEBUG>0)
			System.out.println("Rengine created, waiting for R");
        if (!R.waitForR()) {
            System.out.println("Cannot load R");
            System.exit(1);
        }
        JGRPackageManager.defaultPackages = RController.getDefaultPackages();
        STARTED = true;
        if (!System.getProperty("os.name").startsWith("Win")) splash.stop();
        MAINRCONSOLE.end = MAINRCONSOLE.output.getText().length();
		if (JGR.R != null && JGR.STARTED) JGR.R.eval("options(width="+JGR.MAINRCONSOLE.getFontWidth()+")");
        MAINRCONSOLE.input.requestFocus();
        new Refresher().run();
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

    /**
        * add new Menu at runtime to Console
     * @param name MenuName
     */
    public static void addMenu(String name) {
        iMenu.addMenu(MAINRCONSOLE,name);
    }

    /**
        * add MenuItem at runtime to ConoleMenu
     * @param menu MenuName
     * @param name ItemName
     * @param cmd  Command
     */
    public static void addMenuItem(String menu, String name, String cmd) {
        if (jgrlistener == null) jgrlistener = new JGRListener();
        iMenu.addMenuItem(MAINRCONSOLE,menu,name,cmd,jgrlistener);
    }

    /**
        * add MenuSeparator at runtime
     * @param menu MenuName
     */
    public static void addMenuSeparator(String menu) {
        iMenu.addMenuSeparator(MAINRCONSOLE,menu);
    }

    /**
        * set R_HOME (in java app)
     * @param rhome
     */
    public static void setRHome(String rhome) {
        RHOME = rhome;
    }
	
	public static void setRLibs(String lib) {
		setRLibs(new String[] {lib});
	}

    /**
        * set R_LIBS (in java app)
     *
     */
    public static void setRLibs(String[] libs) {
        RLIBS = libs;
        for (int i = 0; i< RLIBS.length; i++) {
            if(RLIBS[i].startsWith("~")) RLIBS[i] = RLIBS[i].replaceFirst("~",System.getProperty("user.home"));
        }
    }
	
	public static void setKeyWords(String word) {
		setKeyWords(new String[] {word});
	}

    /**
        * set keywords for highlighting
     *
     */
    public static void setKeyWords(String[] words) {
		KEYWORDS.clear();
        Object dummy = new Object();
        for (int i = 0; i < words.length; i++) {
            KEYWORDS.put(words[i],dummy);
        }
    }
	
	
	public static void setObjects(String obj) {
		setObjects(new String[] {obj});
	}

    /**
        * set objects for hightlighting
     *
     */
    public static void setObjects(String[] objects) {
        OBJECTS.clear();
       	KEYWORDS_OBJECTS.clear();
        Object dummy = new Object();
        for (int i = 0; i < objects.length; i++) {
            KEYWORDS_OBJECTS.put(objects[i],dummy);
            OBJECTS.add(objects[i]);
        }
    }


    public static void readHistory() {
        File hist = null;
        try {
            if ((hist = new File(System.getProperty("user.home") +
                                 File.separator + ".Rhistory")).exists()) {

                BufferedReader reader = new BufferedReader(new FileReader(hist));
                RHISTORY = new Vector();
                String cmd = null;
                while (reader.ready()) {
                    cmd = (cmd==null?"":cmd+"\n")+reader.readLine();
                    if (cmd.endsWith("#")) {
                        RHISTORY.add(cmd.substring(0,cmd.length()-1));
                        cmd = null;
                    }
                }
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
            while(e.hasMoreElements()) writer.write(e.nextElement().toString()+"#\n");
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            new ErrorMsg(e);
        }


    }

    public static void main(String[] args) {
		if (args.length>0 && args[0].equals("--debug")) {
			org.rosuda.util.Global.DEBUG=1;
			org.rosuda.JRI.Rengine.DEBUG=1;
			System.out.println("JGR version " + VERSION);
		}
        try {
            new JGR();
        }
        catch (Exception e) {
            new ErrorMsg(e);
        }
    }

    class Refresher implements Runnable {
        public void run() {
            while(true) {
                try {
                    Thread.sleep(60000);
                    REXP x = R.idleEval("try(.refreshKeyWords(),silent=TRUE)");
					String[] r = null;
					if (x != null && (r=x.asStringArray()) != null) 
						setKeyWords(r);
					x = R.idleEval("try(.refreshObjects(),silent=TRUE)");
					r = null;
					if (x != null && (r=x.asStringArray()) != null) 
						setObjects(r);
                }
                catch (Exception e){
                    new ErrorMsg(e);
                }
            }
        }
    }
}
