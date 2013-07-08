package org.rosuda.JGR;

// JGR - Java Gui for R, see http://www.rosuda.org/JGR/
// Copyright (C) 2003 - 2005 Markus Helbig
// --- for licensing information see LICENSE file in the original JGR
// distribution ---

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.rosuda.JGR.toolkit.ConsoleSync;
import org.rosuda.JGR.toolkit.JGRListener;
import org.rosuda.JGR.toolkit.JGRPrefs;
import org.rosuda.JGR.util.ErrorMsg;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.JRI.JRIEngine;
import org.rosuda.ibase.SVar;
import org.rosuda.ibase.toolkit.EzMenuSwing;
import org.rosuda.util.Global;

/**
 * JGR, Java Gui for R JGR is just a new Gui for R <a
 * href="http://www.r-project.org">http://www.r-project.org </a>, written in
 * Java. <br>
 * Thus it is (should) be platform-indepent. Currently we have several problems
 * on *nix machines. <br>
 * <br>
 * <a href="http://www.rosuda.org/JGR">JGR </a> uses JRI and rJava for talking
 * with R, and the <a href="http://www.rosuda.org/R/JavaGD">JavaGD- Device </a>
 * all written by Simon Urbanek. <br>
 * <br>
 * <a href="http://www.rosuda.org">RoSuDa </a> 2003 - 2005
 * 
 * @author Markus Helbig
 */

public class JGR {

	/** Version number of JGR */
	public static final String VERSION = "1.7-16";

	/** Title (used for displaying the splashscreen) */
	public static final String TITLE = "JGR";

	/** Subtitle (used for displaying the splashscreen) */
	public static final String SUBTITLE = "Java Gui for R";

	/** Develtime (used for displaying the splashscreen) */
	public static final String DEVELTIME = "2003 - 2013";

	/** Organization (used for displaying the splashscreen) */
	public static final String INSTITUTION = "RoSuDa, Univ. Augsburg";

	/** Author JGR (used for displaying the splashscreen) */
	public static final String AUTHOR1 = "Markus Helbig";

	/** Author JRI, rJava and JavaGD (used for displaying the splashscreen) */
	public static final String AUTHOR2 = "Simon Urbanek";

	/** Author JGR parts since 2009 */
	public static final String AUTHOR3 = "Ian Fellows";

	/** Website of organization (used for displaying the splashscreen) */
	public static final String WEBSITE = "http://www.rosuda.org";

	/** Filename of the splash-image (used for displaying the splashscreen) */
	public static final String SPLASH = "jgrsplash.jpg";

	/** Main-console window */
	public static JGRConsole MAINRCONSOLE = null;

	/**
	 * The history of current session. If there was a .Rhistory file, it will be
	 * loaded into this vector
	 */
	public static Vector RHISTORY = null;

	/** RHOME path of current used R */
	public static String RHOME = null;

	/** Rengine {@link org.rosuda.JRI.Rengine}, needed for executing R-commands */
	private static REngine rEngine = null;

	/** ConsoleSnyc {@link org.rosuda.JRG.toolkit.ConsoleSync} */
	public static ConsoleSync rSync = new ConsoleSync();

	/** Current data-sets (data.frames, matrix, ...) */
	public static Vector DATA = new Vector();

	/** Current models */
	public static Vector MODELS = new Vector();

	/** Current data not in DATA {@link DATA} */
	public static Vector OTHERS = new Vector();

	/** Current functions */
	public static Vector FUNCTIONS = new Vector();

	/** Current objects in workspace */
	public static Vector OBJECTS = new Vector();

	/** Keywords for syntaxhighlighting */
	public static Vector KEYWORDS = new Vector();

	/** Keywords (objects) for syntaxhighlighting */
	public static Vector KEYWORDS_OBJECTS = new Vector();

	/** Indicates wether the Rengine is up or not */
	public static boolean STARTED = false;

	/**
	 * JGRListener, is uses for listening to java-commands coming from JGR's
	 * R-process
	 */
	private static JGRListener jgrlistener = null;

	/** Splashscreen */
	public static org.rosuda.JGR.toolkit.SplashScreen splash;

	/** arguments for Rengine */
	private static String[] rargs = { "--save" };

	/**
	 * Set to <code>true</code> when JGR is running as the main program and
	 * <code>false</code> if the classes are loaded, but not run via main.
	 */
	private static boolean JGRmain = false;

	private static String tempWD;

	/**
	 * Starting the JGR Application (javaside)
	 */
	public JGR() {
		SVar.int_NA = -2147483648;
		
		Object dummy = new Object();
		JGRPackageManager.neededPackages.put("base", dummy);
		JGRPackageManager.neededPackages.put("graphics", dummy);
		JGRPackageManager.neededPackages.put("grDevices", dummy);
		JGRPackageManager.neededPackages.put("utils", dummy);
		JGRPackageManager.neededPackages.put("methods", dummy);
		JGRPackageManager.neededPackages.put("stats", dummy);
		JGRPackageManager.neededPackages.put("datasets", dummy);

		JGRPackageManager.neededPackages.put("JGR", dummy);
		JGRPackageManager.neededPackages.put("rJava", dummy);
		JGRPackageManager.neededPackages.put("JavaGD", dummy);
		JGRPackageManager.neededPackages.put("iplots", dummy);

		org.rosuda.util.Platform.initPlatform("org.rosuda.JGR.toolkit.");
		JGRPrefs.initialize();
		splash = new org.rosuda.JGR.toolkit.SplashScreen();
		splash.start();
		readHistory();
		MAINRCONSOLE = new JGRConsole();
		MAINRCONSOLE.setWorking(true);
		splash.toFront();
		if (System.getProperty("os.name").startsWith("Window")) {
			splash.stop();
			JGRPrefs.isWindows = true;
		}

		// let's preemptively load JRI - if we do it here, we can show an error
		// message
		try {
			System.loadLibrary("jri");
		} catch (UnsatisfiedLinkError e) {
			String errStr = "all environment variables (PATH, LD_LIBRARY_PATH, etc.) are setup properly (see supplied script)";
			String libName = "libjri.so";
			if (System.getProperty("os.name").startsWith("Window")) {
				errStr = "you start JGR by double-clicking the JGR.exe program";
				libName = "jri.dll";
			}
			if (System.getProperty("os.name").startsWith("Mac")) {
				errStr = "you start JGR by double-clicking the JGR application";
				libName = "libjri.jnilib";
			}
			JOptionPane.showMessageDialog(null, "Cannot find Java/R Interface (JRI) library (" + libName + ").\nPlease make sure " + errStr + ".",
					"Cannot find JRI library", JOptionPane.ERROR_MESSAGE);
			System.err.println("Cannot find JRI native library!\n");
			e.printStackTrace();
			System.exit(1);
		}

		if (!org.rosuda.JRI.Rengine.versionCheck()) {
			JOptionPane.showMessageDialog(null,
					"Java/R Interface (JRI) library doesn't match this JGR version.\nPlease update JGR and JRI to the latest version.",
					"Version Mismatch", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		}
		try {
			rEngine = new JRIEngine(rargs, MAINRCONSOLE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,
					"Unable to start R: " + e.getMessage(),
					"REngine problem", JOptionPane.ERROR_MESSAGE);
			System.err.println("Cannot start REngine " + e);
			System.exit(1);
		}

		try {
			// to avoid quoting hell we use an assignment
			rEngine.assign(".$JGR", new REXPString(JGRPrefs.workingDirectory));
			JGR.eval("try({setwd(`.$JGR`); rm(`.$JGR`)},silent=T)");
		} catch (REngineException e) {
			new ErrorMsg(e);
		} catch (REXPMismatchException e) {
			new ErrorMsg(e);
		}

		// load packages requested by the user
		// in theory we could have checked for jgr.load.pkgs property, but in
		// practice it doesn't
		// hurt if we load them despite the fact that R_DEFAULT_PACKAGES has
		// been set.
		if (JGRPrefs.defaultPackages != null)
			RController.requirePackages(JGRPrefs.defaultPackages);
		RController.requirePackages("JGR"); // ensure JGR is loaded
		JGRPackageManager.defaultPackages = RController.getJgrDefaultPackages();

		STARTED = true;
		if (!System.getProperty("os.name").startsWith("Win"))
			splash.stop();
		// make sure we get a clean prompt after all packages have been loaded
		JGR.MAINRCONSOLE.execute("", false);
		MAINRCONSOLE.toFront();
		MAINRCONSOLE.input.requestFocus();
		JGR.threadedEval("options(width=" + MAINRCONSOLE.getFontWidth() + ")");
		// redefine Java Output streams to be written to JGR console
		System.setOut(MAINRCONSOLE.getStdOutPrintStream());
		//System.setErr(MAINRCONSOLE.getStdErrPrintStream());
		
		//kludge to fix infinite recursion crash on mac os x
		//get rid of this when bug is fixed
		//UPDATE: this is fixed as of 1.7-16 provided the -Xss10m opion is used
		//		to start java. This is default on the mac 2.0 launcher.
		//if (System.getProperty("os.name").startsWith("Mac"))
		//	try{JGR.threadedEval("options(expressions=1000)");}catch(Exception e){}
		
		new Refresher().run();
	}

	public static REXP idleEval(String cmd) throws REngineException, REXPMismatchException {
		if (getREngine() == null)
			throw new REngineException(null, "REngine not available");
		REXP x = null;
		int lock = getREngine().tryLock();
		if (lock != 0) {
			try {
				x = getREngine().parseAndEval(cmd, null, true);
			} finally {
				getREngine().unlock(lock);
			}
		}
		return x;
	}

	public static REXP eval(String cmd) throws REngineException, REXPMismatchException {
		if (getREngine() == null)
			throw new REngineException(null, "REngine not available");
		REXP x = getREngine().parseAndEval(cmd, null, true);
		return x;
	}
	
	public static void threadedEval(String cmd){
		final String c = cmd;
		new Thread(new Runnable(){

			public void run() {
				try {
					JGR.eval(c);
				} catch (Exception e) {}
			}
	
		}).start();
	}
	
	public static REXP timedEval(String cmd){
		return timedEval(cmd,15000,true);
	}
	
	public static REXP timedEval(String cmd,boolean ask){
		return timedEval(cmd,15000,ask);
	}
	
	public static REXP timedEval(String cmd,int interval,boolean ask){
		return new MonitoredEval(interval,ask).run(cmd);
	}

	public static void timedAssign(String symbol, REXP value){
		timedAssign(symbol, value,15000,true);
	}
	
	public static void timedAssign(String symbol, REXP value,boolean ask){
		timedAssign(symbol, value,15000,ask);
	}
	
	public static void timedAssign(String symbol, REXP value,int interval,boolean ask){
		new MonitoredEval(interval,ask).assign(symbol, value);
	}
	
	public static REngine getREngine() {
		return rEngine;
	}

	public static void setREngine(REngine e){
		rEngine=e;
	}
	/**
	 * Exits JGR, but not before asked the user if he wants to save his
	 * workspace.
	 * 
	 * @return users's answer (yes/no/cancel)
	 */
	public static String exit() {
		int exit = 1;

		if (JGRPrefs.askForSavingWorkspace) {
			exit = JOptionPane
					.showConfirmDialog(null, "Save workspace?", "Close JGR", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		}

		if (exit == 0) {
			writeHistory();
			JGRPrefs.writeCurrentPackagesWhenExit();
			return "y\n";
		} else if (exit == 1) {
			JGRPrefs.writeCurrentPackagesWhenExit();
			return "n\n";
		} else
			return "c\n";
	}

	
	/**
	 * Add new Menu at runtime to Console.
	 * 
	 * @param name
	 *            MenuName
	 */
	public static void addMenu(String name) {
		if (MAINRCONSOLE == null)
			return;
		EzMenuSwing.addMenu(MAINRCONSOLE, name);
	}

	
	/**
	 * Insert new Menu at runtime to Console.
	 * 
	 * @param name
	 *            MenuName
	 * @param pos
	 *            position at which to insert
	 */
	public static void insertMenu(String name,int pos) {
		if (MAINRCONSOLE == null)
			return;
		insertMenu(MAINRCONSOLE, name,pos);
	}
	
	//temporary should be in EzMenuSwing
	private static void insertMenu(JFrame f, String name,int index) {
		JMenuBar mb = f.getJMenuBar();
		JMenu m = EzMenuSwing.getMenu(f,name);
		if (m == null && index<mb.getMenuCount()){
			JMenuBar mb2 = new JMenuBar(); 
			int cnt = mb.getMenuCount();
			for(int i=0;i<cnt;i++){
				if(i==index)
					mb2.add(new JMenu(name));
				mb2.add(mb.getMenu(0));
			}
			f.setJMenuBar(mb2);			
		}else if(m==null && index==mb.getMenuCount())
			EzMenuSwing.addMenu(f,name);
	}
	
	/**
	 * Add MenuItem at runtime to ConsoleMenu.
	 * 
	 * @param menu
	 *            MenuName
	 * @param name
	 *            ItemName
	 * @param cmd
	 *            Command
	 * @param silent
	 *            Don't display command
	 */
	public static void addMenuItem(String menu, String name, String cmd,boolean silent) {
		if (MAINRCONSOLE == null)
			return;
		
		ActionListener listener = new JGRListener(silent);
		EzMenuSwing.addJMenuItem(MAINRCONSOLE, menu, name, cmd, listener);
	}
	
	/**
	 * Add MenuItem at runtime to ConsoleMenu.
	 * 
	 * @param menu
	 *            MenuName
	 * @param name
	 *            ItemName
	 * @param cmd
	 *            Command
	 */
	public static void addMenuItem(String menu, String name, String cmd) {
		addMenuItem(menu,name,cmd,true);
	}
	
	/**
	 * Add MenuSeparator at runtime.
	 * 
	 * @param menu
	 *            MenuName
	 */
	public static void addMenuSeparator(String menu) {
		if (MAINRCONSOLE == null)
			return;
		EzMenuSwing.addMenuSeparator(MAINRCONSOLE, menu);
	}
	
	/**
	 * insert MenuSeparator at runtime.
	 * 
	 * @param menu
	 *            MenuName
	 * @param pos
	 * 			  index
	 */
	public static void insertMenuSeparator(String menu,int pos) {
		if (MAINRCONSOLE == null)
			return;
		JMenu m = EzMenuSwing.getMenu(MAINRCONSOLE, menu);
		m.insertSeparator(pos);
	}
	
	
	
	/**
	 * Insert a MenuItem at runtime to ConsoleMenu.
	 * 
	 * @param menu
	 *            MenuName
	 * @param name
	 *            ItemName
	 * @param cmd
	 *            Command
	 * @param silent
	 *            Don't display command
	 * @param pos
	 *            position
	 */
	public static void insertMenuItem(String menu, String name, String cmd,boolean silent,int pos) {
		if (MAINRCONSOLE == null)
			return;
		
		ActionListener listener = new JGRListener(silent);
		insertJMenuItem(MAINRCONSOLE, menu, name, cmd, listener,pos);
	}
	
	//temporary should be in EzMenuSwing
	private static void insertJMenuItem(JFrame f, String menu, String name,String command, ActionListener al,int index) {
		JMenu m = EzMenuSwing.getMenu(f, menu);
		JMenuItem mi = new JMenuItem(name);
		mi.addActionListener(al);
		mi.setActionCommand(command);
		m.insert(mi,index);
	}
	
	/**
	 * Add MenuItem at runtime to ConsoleMenu.
	 * 
	 * @param menu
	 *            MenuName
	 * @param name
	 *            ItemName
	 * @param cmd
	 *            Command
	 */
	public static void insertMenuItem(String menu, String name, String cmd,int pos) {
		insertMenuItem(menu,name,cmd,true,pos);
	}
	
	/**
	 * 
	 * 
	 * @return the names of the menus
	 */
	public static String[] getMenuNames(){
		if (MAINRCONSOLE == null)
			return new String[]{};
		JMenuBar mb = MAINRCONSOLE.getJMenuBar();
		String[] names = new String[mb.getMenuCount()];
		for(int i=0;i<mb.getMenuCount();i++){
			names[i] = mb.getMenu(i).getText();
		}
		return names;
	}

	/**
	 * 
	 * 
	 * 
	 * @param menuName menu
	 * @return the names of the items
	 */
	public static String[] getMenuItemNames(String menuName){
		if (MAINRCONSOLE == null)
			return new String[]{};		
		JMenu m = EzMenuSwing.getMenu(MAINRCONSOLE, menuName);
		String[] names = new String[m.getItemCount()];
		for(int i=0; i<m.getItemCount();i++){
			names[i] = m.getItem(i)!=null ? m.getItem(i).getText() : "-";
		}
		return names;
	}
	
	/**
	 * Remove a menu
	 * 
	 * @param pos index
	 */
	public static void removeMenu(int pos){
		if (MAINRCONSOLE == null)
			return ;
		MAINRCONSOLE.getJMenuBar().remove(pos);
	}
	
	/**
	 * Remove a menu
	 * @param name menu to remove
	 */
	public static void removeMenu(String name){
		String[] names = getMenuNames();
		for(int i=0;i<names.length;i++)
			if(names[i].equals(name))
				MAINRCONSOLE.remove(i);
	}
	
	/**
	 * remove a menu item
	 * @param menuName name of menu
	 * @param pos index of item
	 */
	public static void removeMenuItem(String menuName,int pos){
		JMenu menu = EzMenuSwing.getMenu(MAINRCONSOLE, menuName);
		menu.remove(pos);
	}
	
	/**
	 * remove a menu item
	 * @param menuName name of menu
	 * @param itemName name of item
	 */
	public static void removeMenuItem(String menuName,String itemName){
		JMenu menu = EzMenuSwing.getMenu(MAINRCONSOLE, menuName);
		String[] names = getMenuItemNames(menuName);
		for(int i=0;i<names.length;i++)
			if(names[i].equals(itemName))
				menu.remove(i);
	}
	
	public static void insertSubMenu(String menuName,String subMenuName,int pos, String[] labels, String[] cmds){
		
		JMenu sm = new JMenu(subMenuName);
		sm.setMnemonic(KeyEvent.VK_S);
		for(int i = 0;i<labels.length;i++){
			JMenuItem mi = new JMenuItem();
			mi.setText(labels[i]);
			mi.setActionCommand(cmds[i]);
			mi.addActionListener(new JGRListener(true));
			sm.add(mi);
		}
		JMenu menu = EzMenuSwing.getMenu(MAINRCONSOLE, menuName);
		menu.insert(sm, pos);
	}
	
	public static void addSubMenu(String menuName,String subMenuName, String[] labels, String[] cmds){
		
		JMenu sm = new JMenu(subMenuName);
		sm.setMnemonic(KeyEvent.VK_S);
		for(int i = 0;i<labels.length;i++){
			JMenuItem mi = new JMenuItem();
			mi.setText(labels[i]);
			mi.setActionCommand(cmds[i]);
			mi.addActionListener(new JGRListener(true));
			sm.add(mi);
		}
		JMenu menu = EzMenuSwing.getMenu(MAINRCONSOLE, menuName);
		menu.add(sm);
	}
	
	

	/**
	 * Set R_HOME (in java app).
	 * 
	 * @param rhome
	 *            RHOME path
	 */
	public static void setRHome(String rhome) {
		RHOME = rhome;
	}


	/**
	 * Set keywords for highlighting.
	 * 
	 * @param word
	 *            This word will be highlighted
	 */
	public static void setKeyWords(String word) {
		setKeyWords(new String[] { word });
	}

	/**
	 * Set keywords for highlighting.
	 * 
	 * @param words
	 *            These words will be highlighted
	 */
	public static void setKeyWords(String[] words) {
		KEYWORDS.clear();
		for (int i = 0; i < words.length; i++)
			KEYWORDS.add(words[i]);
	}

	/**
	 * Set objects for hightlighting.
	 * 
	 * @param object
	 *            This object will be highlighted
	 */
	public static void setObjects(String object) {
		setObjects(new String[] { object });
	}

	/**
	 * Set objects for hightlighting.
	 * 
	 * @param objects
	 *            These words will be highlighted
	 */
	public static void setObjects(String[] objects) {
		OBJECTS.clear();
		KEYWORDS_OBJECTS.clear();
		for (int i = 0; i < objects.length; i++) {
			String object = objects[i];
			if (!(RController.TEMP_MATRIX_CONTENT_JGR.equals(object) || RController.TEMP_MATRIX_DIM_NAMES_JGR.equals(object) || RController.TEMP_VARIABLE_NAME
					.equals(object))) {
				KEYWORDS_OBJECTS.add(object);
				OBJECTS.add(object);
			}
		}
	}

	/**
	 * If there is a file named .Rhistory in the user's home path we load his
	 * commands to current history.
	 */
	public static void readHistory() {
		File hist = null;
		try {
			tempWD = JGRPrefs.workingDirectory;
			if ((hist = new File(JGRPrefs.workingDirectory + File.separator + ".JGRhistory")).exists()) {

				BufferedReader reader = new BufferedReader(new FileReader(hist));
				RHISTORY = new Vector();
				String cmd = null;
				while (reader.ready()) {
					cmd = (cmd == null ? "" : cmd + "\n") + reader.readLine();
					if (cmd.endsWith("#")) {
						RHISTORY.add(cmd.substring(0, cmd.length() - 1));
						cmd = null;
					}
				}
				reader.close();
			}
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}

	/**
	 * Write the commands of current session to .Rhistory.
	 */
	public static void writeHistory() {
		File hist = null;
		try {
			hist = new File(tempWD + File.separator + ".JGRhistory");
			BufferedWriter writer = new BufferedWriter(new FileWriter(hist));
			Enumeration e = JGR.RHISTORY.elements();
			while (e.hasMoreElements()) {
				writer.write(e.nextElement().toString() + "#\n");
				writer.flush();
			}
			writer.close();
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}

	/** return the value of the {@link #JGRmain} flag */
	public static boolean isJGRmain() {
		return JGRmain;
	}

	private void checkForMissingPkg() {
		try {
			String previous = JGRPrefs.previousPackages;

			if (previous == null)
				return;
			String current = RController.getCurrentPackages();

			if (current == null)
				return;

			Vector currentPkg = new Vector();
			Vector previousPkg = new Vector();

			StringTokenizer st = new StringTokenizer(current, ",");
			while (st.hasMoreTokens())
				currentPkg.add(st.nextToken().toString().replaceFirst(",", ""));

			st = new StringTokenizer(previous, ",");
			while (st.hasMoreTokens())
				previousPkg.add(st.nextToken().toString().replaceFirst(",", ""));

			for (int i = 0; i < currentPkg.size(); i++)
				previousPkg.remove(currentPkg.elementAt(i));

			if (previousPkg.size() > 0)
				new JGRPackageManager(previousPkg);
		} catch (Exception e) {
		}
	}

	/**
	 * Starts JGR <br>
	 * options: <br>
	 * <ol>
	 * <li>--debug: enable debug information</li>
	 * </ol>
	 * or any other option supported by R.
	 */
	public static void main(String[] args) {
		JGRmain = true;
		if (args.length > 0) {
			Vector args2 = new Vector();
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--debug")) {
					org.rosuda.util.Global.DEBUG = 1;
					org.rosuda.JRI.Rengine.DEBUG = 1;
					System.out.println("JGR version " + VERSION);
				} else
					args2.add(args[i]);
				if (args[i].equals("--version")) {
					System.out.println("JGR version " + VERSION);
					System.exit(0);
				}
				if (args[i].equals("--help") || args[i].equals("-h")) {
					System.out.println("JGR version " + VERSION);
					System.out.println("\nOptions:");
					System.out.println("\n\t-h, --help\t Print short helpmessage and exit");
					System.out.println("\t--version\t Print version end exit");
					System.out.println("\t--debug\t Print more information about JGR's process");
					System.out.println("\nMost other R options are supported too");
					System.exit(0);
				}
			}
			Object[] arguments = args2.toArray();
			if (arguments.length > 0) {
				rargs = new String[arguments.length + 1];
				for (int i = 0; i < rargs.length - 1; i++)
					rargs[i] = arguments[i].toString();
				rargs[rargs.length - 1] = "--save";
			}
		}

		if (Global.DEBUG > 0)
			for (int i = 0; i < rargs.length; i++)
				System.out.println(rargs[i]);

		String nativeLF = UIManager.getSystemLookAndFeelClassName();

		// Install the look and feel
		try {
			UIManager.setLookAndFeel(nativeLF);
		} catch (InstantiationException e) {
		} catch (ClassNotFoundException e) {
		} catch (UnsupportedLookAndFeelException e) {
		} catch (IllegalAccessException e) {
		}

		try {
			new JGR();
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}

	public static void refreshObjects() {
		REXP x;
		try {
			x = idleEval("try(.refreshObjects(),silent=TRUE)");
			String[] r = null;
			if (x != null && (r = x.asStrings()) != null)
				JGR.setObjects(r);
		} catch (REngineException e) {
			new ErrorMsg(e);
		} catch (REXPMismatchException e) {
			new ErrorMsg(e);
		}
	}

	/**
	 * Refresher, which is looking for new keywords and objects in workspace and
	 * refreshes highlight and autocompletion information.
	 */
	class Refresher implements Runnable {

		public Refresher() {
			checkForMissingPkg();
		}

		public void run() {
			while (true)
				try {
					Thread.sleep(5000);
					REXP x = idleEval("try(.refreshKeyWords(),silent=TRUE)");
					String[] r = null;
					if (x != null && (r = x.asStrings()) != null)
						setKeyWords(r);
					x = idleEval("try(.refreshObjects(),silent=TRUE)");
					r = null;
					if (x != null && (r = x.asStrings()) != null)
						setObjects(r);
					RController.refreshObjects();
				} catch (Exception e) {
					new ErrorMsg(e);
				}
		}
	}
	
	
}

final class MonitoredEval{
	volatile boolean done;
	volatile REXP result;
	int interval;
	int checkInterval;
	boolean ask;
	public MonitoredEval(int inter,boolean ak){
		done = false;
		interval = inter;
		checkInterval = interval;
		ask=ak;
	}
	
	protected void startMonitor(){
		int t = 0;
		while(true){
			try {
				Thread.sleep(checkInterval);
				
			} catch (InterruptedException e) {
				return;
			}
			if(done)
				return;
			if(t+checkInterval <interval){
				t = t + checkInterval;
				continue;
			}
			int cancel;
			if(ask){
				cancel = JOptionPane.showConfirmDialog(null, 
					"This R process is taking some time.\nWould you like to cancel it?",
					"Cancel R Process",
						 JOptionPane.YES_NO_OPTION);
			}else
				cancel = JOptionPane.YES_OPTION;
			if(cancel==JOptionPane.YES_OPTION){
				((org.rosuda.REngine.JRI.JRIEngine) JGR.getREngine())
				.getRni().rniStop(0);
				return;
			}else{
				t=0;
			}
		}			
	}

	public REXP run(String cmd) {
		
		try{
			if(SwingUtilities.isEventDispatchThread() && ask){
				final String c = cmd;
				new Thread(new Runnable(){
					public void run() {
						try {
							result = JGR.eval(c);
						} catch (REngineException e) {
							result = null;
						} catch (REXPMismatchException e) {
							result=null;
						}
						done = true;
					}
				}).start();	
				checkInterval = 10;
				startMonitor();
			}else{
				new Thread(new Runnable(){
					public void run() {
						startMonitor();
					}
				}).start();	
					
				result = JGR.eval(cmd);
			}
			done = true;				
			return result;
		}catch(Exception e){
			return null;
		}
	}
	
	public void assign(String symbol, REXP value) {
		if(SwingUtilities.isEventDispatchThread() && ask){
			final String sym = symbol;
			final REXP val = value;
			new Thread(new Runnable(){
				public void run() {
					try {
						JGR.getREngine().assign(sym, val);
					} catch (REngineException e) {
						result = null;
					} catch (REXPMismatchException e) {
						result=null;
					}
					done = true;
				}
			}).start();	
			checkInterval = 10;
			startMonitor();
		}else{
			new Thread(new Runnable(){
				public void run() {
					startMonitor();
				}
			}).start();	
			try{
				JGR.getREngine().assign(symbol, value);
				done = true;
			}catch(Exception e){}
		}
	}
}
