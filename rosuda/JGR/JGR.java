package org.rosuda.JGR;

import java.io.*;
import java.util.*;

import javax.swing.*;

import org.rosuda.ibase.*;
import org.rosuda.util.Global;

import org.rosuda.JRI.*;
import org.rosuda.JGR.toolkit.*;
import org.rosuda.JGR.util.*;

/**
 * JGR, Java Gui for R JGR is just a new Gui for R <a
 * href="http://www.r-project.org">http://www.r-project.org </a>, written in
 * Java. <br>
 * Thus it is (should) be platform-indepent. Currently we have several problems
 * on *nix machines. <br>
 * <br>
 * <a href="http://www.rosuda.org/JGR">JGR </a> uses JRI and rJava for talking with R, and
 * the <a href="http://www.rosuda.org/R/JavaGD">JavaGD- Device </a> all written
 * by Simon Urbanek. <br>
 * <br>
 * <a href="http://www.rosuda.org">RoSuDa </a> 2003 - 2005
 * 
 * @author Markus Helbig
 */

public class JGR {

	/* Copyright information and other stuff */

	/** Version number of JGR */
	public static final String VERSION = "1.1a-pre";

	/** Title (used for displaying the splashscreen) */
	public static final String TITLE = "JGR";

	/** Subtitle (used for displaying the splashscreen) */
	public static final String SUBTITLE = "Java Gui for R";

	/** Develtime (used for displaying the splashscreen) */
	public static final String DEVELTIME = "2003 - 2005";

	/** Organization (used for displaying the splashscreen) */
	public static final String INSTITUTION = "RoSuDa, Univ. Augsburg";

	/** Author JGR (used for displaying the splashscreen) */
	public static final String AUTHOR1 = "Markus Helbig";

	/** Author JRI and JavaGD (used for displaying the splashscreen) */
	public static final String AUTHOR2 = "Simon Urbanek";

	/** Website of organization (used for displaying the splashscreen) */
	public static final String WEBSITE = "http://www.rosuda.org";

	/** Filename of the splash-image (used for displaying the splashscreen) */
	public static final String SPLASH = "splash.jpg";

	/** Path to user's home, globally used for nearly every filechooser */
	public static String directory = System.getProperty("user.home");

	/** Main-console window */
	public static JGRConsole MAINRCONSOLE = null;

	/**
	 * The history of current session. If there was a .Rhistory file, it will be
	 * loaded into this vector
	 */
	public static Vector RHISTORY = null;

	/** RHOME path of current used R */
	public static String RHOME = null;

	/** RLIB pathes, where we can find the user's R-packages */
	public static String[] RLIBS = null;

	/** Rengine {@link org.rosuda.JRI.Rengine}, needed for executing R-commands */
	public static Rengine R = null;

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
	public static HashMap KEYWORDS = new HashMap();

	/** Keywords (objects) for syntaxhighlighting */
	public static HashMap KEYWORDS_OBJECTS = new HashMap();

	/** Indicates wether the Rengine is up or not */
	public static boolean STARTED = false;

	/**
	 * JGRListener, is uses for listening to java-commands coming from JGR's
	 * R-process
	 */
	private static JGRListener jgrlistener = null;

	/** Splashscreen */
	public static org.rosuda.JGR.toolkit.SplashScreen splash;
	
	/** arguments for Rengine*/
	private static String[] rargs = {"--save"};

	/**
	 * Starting the JGR Application (javaside)
	 */
	public JGR() {
		SVar.int_NA = -2147483648;

		Object dummy = new Object();
		JGRPackageManager.neededPackages.put("base", dummy);
		JGRPackageManager.neededPackages.put("graphics", dummy);
		JGRPackageManager.neededPackages.put("utils", dummy);
		JGRPackageManager.neededPackages.put("methods", dummy);
		JGRPackageManager.neededPackages.put("stats", dummy);
		JGRPackageManager.neededPackages.put("datasets", dummy);

		JGRPackageManager.neededPackages.put("JGR", dummy);
		JGRPackageManager.neededPackages.put("rJava", dummy);
		JGRPackageManager.neededPackages.put("JavaGD", dummy);

		org.rosuda.util.Platform.initPlatform("org.rosuda.JGR.toolkit.");
		JGRPrefs.initialize();
		splash = new org.rosuda.JGR.toolkit.SplashScreen();
		splash.start();
		readHistory();
		MAINRCONSOLE = new JGRConsole();
		MAINRCONSOLE.setWorking(true);
		splash.toFront();
		if (System.getProperty("os.name").startsWith("Window"))
			splash.stop();

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
			JOptionPane.showMessageDialog(null,
					"Cannot find Java/R Interface (JRI) library (" + libName
							+ ").\nPlease make sure " + errStr + ".",
					"Cannot find JRI library", JOptionPane.ERROR_MESSAGE);
			System.err.println("Cannot find JRI native library!\n");
			e.printStackTrace();
			System.exit(1);
		}

		if (!Rengine.versionCheck()) {
			JOptionPane
					.showMessageDialog(
							null,
							"Java/R Interface (JRI) library doesn't match this JGR version.\nPlease update JGR and JRI to the latest version.",
							"Version Mismatch", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		}
		R = new Rengine(rargs, true, MAINRCONSOLE);
		if (org.rosuda.util.Global.DEBUG > 0)
			System.out.println("Rengine created, waiting for R");
		if (!R.waitForR()) {
			System.out.println("Cannot load R");
			System.exit(1);
		}
		JGRPackageManager.defaultPackages = RController.getDefaultPackages();
		STARTED = true;
		if (!System.getProperty("os.name").startsWith("Win"))
			splash.stop();
		MAINRCONSOLE.end = MAINRCONSOLE.output.getText().length();
		if (JGR.R != null && JGR.STARTED)
			JGR.R
					.eval("options(width=" + JGR.MAINRCONSOLE.getFontWidth()
							+ ")");
		MAINRCONSOLE.input.requestFocus();
		new Refresher().run();
	}

	/**
	 * Exits JGR, but not before asked the user if he wants to save his
	 * workspace.
	 * 
	 * @return users's answer (yes/no/cancel)
	 */
	public static String exit() {
		int exit = JOptionPane.showConfirmDialog(null, "Save workspace?",
				"Close JGR", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE);

		if (exit == 0) {
			writeHistory();
			return "y\n";
		} else if (exit == 1)
			return "n\n";
		else
			return "c\n";
	}

	/**
	 * Add new Menu at runtime to Console.
	 * 
	 * @param name MenuName
	 */
	public static void addMenu(String name) {
		iMenu.addMenu(MAINRCONSOLE, name);
	}

	/**
	 * Add MenuItem at runtime to ConsoleMenu.
	 * 
	 * @param menu MenuName
	 * @param name ItemName
	 * @param cmd Command
	 */
	public static void addMenuItem(String menu, String name, String cmd) {
		if (jgrlistener == null)
			jgrlistener = new JGRListener();
		iMenu.addMenuItem(MAINRCONSOLE, menu, name, cmd, jgrlistener);
	}

	/**
	 * Add MenuSeparator at runtime.
	 * 
	 * @param menu MenuName
	 */
	public static void addMenuSeparator(String menu) {
		iMenu.addMenuSeparator(MAINRCONSOLE, menu);
	}

	/**
	 * Set R_HOME (in java app).
	 * 
	 * @param rhome RHOME path
	 */
	public static void setRHome(String rhome) {
		RHOME = rhome;
	}

	/**
	 * Set R_LIBS (in java app).
	 * 
	 * @param lib Library path
	 */
	public static void setRLibs(String lib) {
		setRLibs(new String[] { lib });
	}

	/**
	 * Set R_LIBS (in java app).
	 * 
	 * @param libs Library pathes
	 */
	public static void setRLibs(String[] libs) {
		RLIBS = libs;
		for (int i = 0; i < RLIBS.length; i++) {
			if (RLIBS[i].startsWith("~"))
				RLIBS[i] = RLIBS[i].replaceFirst("~", System
						.getProperty("user.home"));
		}
	}

	/**
	 * Set keywords for highlighting.
	 * 
	 * @param word This word will be highlighted
	 */
	public static void setKeyWords(String word) {
		setKeyWords(new String[] { word });
	}

	/**
	 * Set keywords for highlighting.
	 * 
	 * @param words These words will be highlighted
	 */
	public static void setKeyWords(String[] words) {
		KEYWORDS.clear();
		Object dummy = new Object();
		for (int i = 0; i < words.length; i++) {
			KEYWORDS.put(words[i], dummy);
		}
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
		Object dummy = new Object();
		for (int i = 0; i < objects.length; i++) {
			KEYWORDS_OBJECTS.put(objects[i], dummy);
			OBJECTS.add(objects[i]);
		}
	}

	/**
	 * If there is a file named .Rhistory in the user's home path we load his
	 * commands to current history.
	 */
	public static void readHistory() {
		File hist = null;
		try {
			if ((hist = new File(System.getProperty("user.home")
					+ File.separator + ".Rhistory")).exists()) {

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
			hist = new File(System.getProperty("user.home") + File.separator
					+ ".Rhistory");
			BufferedWriter writer = new BufferedWriter(new FileWriter(hist));
			Enumeration e = JGR.RHISTORY.elements();
			int i = 0;
			while (e.hasMoreElements())
				writer.write(e.nextElement().toString() + "#\n");
			writer.flush();
			writer.close();
		} catch (Exception e) {
			new ErrorMsg(e);
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
		if (args.length > 0) {
			Vector args2 = new Vector();
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("--debug")) {
					org.rosuda.util.Global.DEBUG = 1;
					org.rosuda.JRI.Rengine.DEBUG = 1;
					System.out.println("JGR version " + VERSION);
				}
				else args2.add(args[i]);
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
				rargs = new String[arguments.length];
				for (int i = 0; i < rargs.length; i++)
					rargs[i] = arguments[i].toString();
			}
			if (Global.DEBUG > 0)
				for (int i = 0; i < rargs.length; i++)
					System.out.println(rargs[i]);
		}

		try {
			new JGR();
		} catch (Exception e) {
			new ErrorMsg(e);
		}
	}

	/**
	 * Refresher, which is looking for new keywords and objects in workspace and refreshes highlight and autocompletion information.
	 */
	class Refresher implements Runnable {
		public void run() {
			while (true) {
				try {
					Thread.sleep(60000);
					REXP x = R.idleEval("try(.refreshKeyWords(),silent=TRUE)");
					String[] r = null;
					if (x != null && (r = x.asStringArray()) != null)
						setKeyWords(r);
					x = R.idleEval("try(.refreshObjects(),silent=TRUE)");
					r = null;
					if (x != null && (r = x.asStringArray()) != null)
						setObjects(r);
				} catch (Exception e) {
					new ErrorMsg(e);
				}
			}
		}
	}
}